package org.orbisgis.orbisdata.datamanager.jdbc.io;

import org.h2gis.api.DriverFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.functions.io.csv.CSVDriverFunction;
import org.h2gis.functions.io.dbf.DBFDriverFunction;
import org.h2gis.functions.io.geojson.GeoJsonDriverFunction;
import org.h2gis.functions.io.geojson.GeoJsonReaderDriver;
import org.h2gis.functions.io.gpx.GPXDriverFunction;
import org.h2gis.functions.io.json.JsonDriverFunction;
import org.h2gis.functions.io.kml.KMLWriterDriver;
import org.h2gis.functions.io.osm.OSMDriverFunction;
import org.h2gis.functions.io.shp.SHPDriverFunction;
import org.h2gis.functions.io.tsv.TSVDriverFunction;
import org.h2gis.functions.io.utility.FileUtil;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.URIUtilities;
import org.orbisgis.orbisdata.datamanager.jdbc.JdbcDataSource;
import org.orbisgis.orbisdata.datamanager.api.dataset.DataBaseType;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * IO methods for database
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class IOMethods {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOMethods.class);

    private static final String ENCODING_OPTION = "charset=";
    private static final String UTF_ENCODING = "UTF-8";

    private static DriverFunction getDriverFromFile(File file){
        String path = file.getAbsolutePath();
        String extension = "";
        int i = path.lastIndexOf(46);
        if (i >= 0) {
            extension = path.substring(i + 1);
        }
        switch(extension){
            case "shp":
                return new SHPDriverFunction();
            case "geojson":
                return new GeoJsonDriverFunction();
            case "json":
                return new JsonDriverFunction();
            case "tsv":
                return new TSVDriverFunction();
            case "csv":
                return new CSVDriverFunction();
            case "dbf":
                return new DBFDriverFunction();
            case "kml":
            case "kmz":
            case "osm":
            case "gz":
            case "bz":
            case "gpx":
                return null;
            default:
                LOGGER.error("Unsupported file format.\n" +
                        "Supported formats are : [shp, geojson, tsv, csv, dbf, kml, kmz, osm, gz, bz, gpx].");
                return null;
        }
    }

    private static String unsupportedEncoding(String encoding){
        if(encoding != null && !encoding.isEmpty()) {
            LOGGER.warn("Encoding is not yet supported for this file format");
        }
        return null;
    }

    /**
     * Save a table to a file
     * @param connection The connection to use for the save.
     * @param tableName Name of the table to save.
     * @param filePath Path of the destination file.
     * @param encoding Encoding of the file.
     *
     * @return True if the file has been saved, false otherwise.
     */
    public static boolean saveAsFile(Connection connection, String tableName, String filePath, String encoding){
        String enc = encoding;
        boolean isH2 = false;
        try {
            isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        } catch (SQLException e) {
            LOGGER.error("Unable to get the DataBase metadata.\n"+e.getLocalizedMessage());
        }
        File fileToSave = URIUtilities.fileFromString(filePath);
        DriverFunction driverFunction = getDriverFromFile(fileToSave);
        try {
            if (FileUtil.isExtensionWellFormated(fileToSave, "csv")) {
                if(enc==null){
                    enc=ENCODING_OPTION + UTF_ENCODING;
                }
            }
            else if (FileUtil.isExtensionWellFormated(fileToSave, "kml") ||FileUtil.isExtensionWellFormated(fileToSave, "kmz")) {
                unsupportedEncoding(enc);
                KMLWriterDriver driver = new KMLWriterDriver(connection, isH2?tableName.toUpperCase():tableName, fileToSave);
                driver.write(new EmptyProgressVisitor());
                return true;
            }
            if(driverFunction != null){
                driverFunction.exportTable(connection, isH2?tableName.toUpperCase():tableName, fileToSave,
                        new EmptyProgressVisitor(), enc);
                return true;
            }
        } catch (SQLException | IOException e) {
            LOGGER.error("Cannot save.\n"+e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * Load a file to a H2GIS database
     *
     * @param filePath the path of the file
     * @param tableName the name of the table created to store the file
     * @param encoding an encoding value to read the file
     * @param delete true to delete the table if exists
     * @param dataSource the database
     */
    //TODO reformat the code once all the driver have the same importFile signature
    public static boolean loadFile(String filePath, String tableName, String encoding, boolean delete, JdbcDataSource dataSource) {
        String enc = encoding;
        Connection connection = dataSource.getConnection();
        File fileToImport = URIUtilities.fileFromString(filePath);
        DriverFunction driverFunction = getDriverFromFile(fileToImport);
        try {
            if (FileUtil.isExtensionWellFormated(fileToImport, "geojson")) {
                dataSource.execute("DROP TABLE IF EXISTS " + tableName);
                GeoJsonReaderDriver driver = new GeoJsonReaderDriver(connection, fileToImport);
                driver.read(new EmptyProgressVisitor(), tableName);
                return true;
            }
            else if (FileUtil.isExtensionWellFormated(fileToImport, "tsv")) {
                enc = unsupportedEncoding(enc);
                driverFunction = new TSVDriverFunction();
            }
            else if (FileUtil.isExtensionWellFormated(fileToImport, "osm") ||
                        FileUtil.isExtensionWellFormated(fileToImport, "gz") ||
                        FileUtil.isExtensionWellFormated(fileToImport, "bz")) {
                enc = unsupportedEncoding(enc);
                driverFunction = new OSMDriverFunction();
            }
            else if (FileUtil.isExtensionWellFormated(fileToImport, "gpx")) {
                enc = unsupportedEncoding(enc);
                driverFunction = new GPXDriverFunction();
            }
            if(driverFunction != null){
                dataSource.execute("DROP TABLE IF EXISTS " + tableName);
                if(enc != null) {
                    driverFunction.importFile(connection, tableName, fileToImport, new EmptyProgressVisitor(), enc);
                }
                else {
                    driverFunction.importFile(connection, tableName, fileToImport, new EmptyProgressVisitor(), delete);
                }
                return true;
            }
        } catch (SQLException | IOException e) {
            LOGGER.error("Cannot load.\n"+e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * Load a table to a H2GIS database from another database
     *
     * @param properties external database properties to set up the connection
     * @param inputTableName the name of the table in the external database
     * @param outputTableName the name of the table in the H2GIS database
     * @param delete true to delete the table if exists
     * @param jdbcDataSource the database
     */
    public static void loadTable(Map<String, String> properties, String inputTableName, String outputTableName,
                           boolean delete, JdbcDataSource jdbcDataSource){
        if(jdbcDataSource.getDataBaseType() != DataBaseType.H2GIS){
            DataBaseType dbType = jdbcDataSource.getDataBaseType();
            String name = dbType == null ? "null" : dbType.name();
            LOGGER.error(name + " database not supported for file link.");
            return;
        }
        String user = properties.getOrDefault(DataSourceFactory.JDBC_USER, "sa");
        String password = properties.getOrDefault(DataSourceFactory.JDBC_PASSWORD, "");
        String driverName = "";
        String jdbc_url = properties.get("url");
        if(jdbc_url!=null) {
            if (jdbc_url.startsWith("jdbc:")) {
                String url = jdbc_url.substring("jdbc:".length());
                if (url.startsWith("h2")) {
                    driverName = "org.h2.Driver";
                } else if (url.startsWith("postgresql")) {
                    driverName = "org.orbisgis.postgis_jts.Driver";
                }
                if(!driverName.isEmpty()) {
                    if (delete) {
                        try {
                            jdbcDataSource.execute("DROP TABLE IF EXISTS " + outputTableName);
                        } catch (SQLException e) {
                            LOGGER.error("Cannot drop the table.\n" + e.getLocalizedMessage());
                        }
                    }
                    try {
                        String tmpTableName =  "TMP_"+ System.currentTimeMillis();
                        jdbcDataSource.execute(String.format("CREATE LINKED TABLE %s('%s', '%s', '%s', '%s', '%s')",
                                tmpTableName, driverName, jdbc_url, user, password, inputTableName));
                        jdbcDataSource.execute(String.format("CREATE TABLE %s as SELECT * from %s", outputTableName,
                                tmpTableName));
                        jdbcDataSource.execute("DROP TABLE IF EXISTS " + tmpTableName);
                    } catch (SQLException e) {
                        LOGGER.error("Cannot load the table.\n" + e.getLocalizedMessage());
                    }
                }
                else{
                    LOGGER.error("This database is not yet supported");
                }
            }
            else{
                LOGGER.error("JDBC Url must start with jdbc:");
            }
        }
        else {
            LOGGER.error("The URL of the external database cannot be null");
        }
    }

    /**
     * Create a dynamic link from a file
     *
     * @param filePath the path of the file
     * @param tableName the name of the table created to store the file
     * @param delete true to delete the table if exists
     * @param jdbcDataSource the database
     */
    public static void link(String filePath, String tableName, boolean delete, JdbcDataSource jdbcDataSource) {
        if(jdbcDataSource.getDataBaseType() != DataBaseType.H2GIS){
            DataBaseType dbType = jdbcDataSource.getDataBaseType();
            String name = dbType == null ? "null" : dbType.name();
            LOGGER.error(name + " database not supported for file link.");
            return;
        }
        if(delete){
            try {
                jdbcDataSource.execute("DROP TABLE IF EXISTS "+ tableName);
            } catch (SQLException e) {
                LOGGER.error("Cannot drop the table.\n"+e.getLocalizedMessage());
            }
        }

        try {
            jdbcDataSource.execute(String.format("CALL FILE_TABLE('%s','%s')", filePath, tableName));
        } catch (SQLException e) {
            LOGGER.error("Cannot link the file.\n"+e.getLocalizedMessage());
        }
    }
}
