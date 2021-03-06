/*
 * Bundle DataManager is part of the OrbisGIS platform
 *
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 *
 * DataManager is distributed under LGPL 3 license.
 *
 * Copyright (C) 2018 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * DataManager is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * DataManager is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * DataManager. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.orbisdata.datamanager.jdbc;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.sql.GroovyRowResult;
import groovy.sql.Sql;
import groovy.text.SimpleTemplateEngine;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.h2.util.ScriptReader;
import org.h2gis.functions.io.utility.FileUtil;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.URIUtilities;
import org.orbisgis.orbisdata.datamanager.jdbc.dsl.FromBuilder;
import org.orbisgis.orbisdata.datamanager.jdbc.io.IOMethods;
import org.orbisgis.orbisdata.datamanager.api.dataset.DataBaseType;
import org.orbisgis.orbisdata.datamanager.api.dataset.IDataSet;
import org.orbisgis.orbisdata.datamanager.api.dataset.ITable;
import org.orbisgis.orbisdata.datamanager.api.datasource.IDataSourceLocation;
import org.orbisgis.orbisdata.datamanager.api.datasource.IJdbcDataSource;
import org.orbisgis.orbisdata.datamanager.api.dsl.IFromBuilder;
import org.orbisgis.orbisdata.datamanager.api.dsl.ISelectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Abstract class used to implements the request builder methods (select, from ...) in order to give a base to all the
 * JdbcDataSource implementations.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public abstract class JdbcDataSource extends Sql implements IJdbcDataSource, ISelectBuilder {
    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcDataSource.class);
    /** MetaClass used for the implementation of the {@link GroovyObject} methods */
    private MetaClass metaClass;
    /** Type of the database */
    private DataBaseType databaseType;
    /** Wrapped {@link DataSource} */
    private DataSource dataSource;

    /**
     * Constructor to create a {@link JdbcDataSource} from a {@link Sql} object.
     * @param parent Parent {@link Sql} object.
     * @param databaseType Type of the database
     */
    public JdbcDataSource(Sql parent, DataBaseType databaseType) {
        super(parent);
        this.dataSource = parent.getDataSource();
        this.metaClass = InvokerHelper.getMetaClass(getClass());
        this.databaseType = databaseType;
        LOG.setLevel(Level.OFF);
    }

    /**
     * Constructor to create a {@link JdbcDataSource} from a {@link DataSource} object.
     * @param dataSource Parent {@link DataSource} object.
     * @param databaseType Type of the database
     */
    public JdbcDataSource(DataSource dataSource, DataBaseType databaseType) {
        super(dataSource);
        this.dataSource = dataSource;
        this.metaClass = InvokerHelper.getMetaClass(getClass());
        this.databaseType = databaseType;
        LOG.setLevel(Level.OFF);
    }

    /**
     * Constructor to create a {@link JdbcDataSource} from a {@link Connection} object.
     * @param connection Parent {@link Sql} object.
     * @param databaseType Type of the database
     */
    public JdbcDataSource(Connection connection, DataBaseType databaseType) {
        super(connection);
        this.dataSource = null;
        this.metaClass = InvokerHelper.getMetaClass(getClass());
        this.databaseType = databaseType;
        LOG.setLevel(Level.OFF);
    }

    @Override
    public Connection getConnection(String var1, String var2) throws SQLException {
        if(this.dataSource != null) {
            return this.dataSource.getConnection(var1, var2);
        }
        LOGGER.error("Unable to get the DataSource.\n");
        return null;
    }

    public PrintWriter getLogWriter() throws SQLException {
        if(this.dataSource != null) {
            return this.dataSource.getLogWriter();
        }
        LOGGER.error("Unable to get the DataSource.\n");
        return null;
    }

    public void setLogWriter(PrintWriter writer) throws SQLException {
        if(this.dataSource != null) {
            this.dataSource.setLogWriter(writer);
        }
        LOGGER.error("Unable to get the DataSource.\n");
    }

    public void setLoginTimeout(int time) throws SQLException {
        if(this.dataSource != null) {
            this.dataSource.setLoginTimeout(time);
        }
        LOGGER.error("Unable to get the DataSource.\n");
    }

    public int getLoginTimeout() throws SQLException {
        if(this.dataSource != null) {
            return this.dataSource.getLoginTimeout();
        }
        LOGGER.error("Unable to get the DataSource.\n");
        return -1;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        if(this.dataSource != null) {
            return this.dataSource.unwrap(aClass);
        }
        LOGGER.error("Unable to get the DataSource.\n");
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        if(this.dataSource != null) {
            return this.dataSource.isWrapperFor(aClass);
        }
        LOGGER.error("Unable to get the DataSource.\n");
        return false;
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        if(this.dataSource != null) {
            return this.dataSource.getParentLogger();
        }
        LOGGER.error("Unable to get the DataSource.\n");
        return null;
    }

    @Override
    public DataSource getDataSource() {
        return this.dataSource;
    }

    @Override
    public Connection getConnection(){
        Connection con = super.getConnection();
        if(con == null){
            try {
                con = getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Unable to get the connection from the DataSource.\n" + e.getLocalizedMessage());
            }
        }
        return con;
    }

    /**
     * Return the type of the database.
     *
     * @return The type of the database.
     */
    public DataBaseType getDataBaseType(){
        return databaseType;
    }

    @Override
    public boolean execute(GString gstring) throws SQLException {
        try {
            return super.execute(gstring);
        } catch (SQLException e) {
            LOGGER.debug("Unable to execute the request as a GString.\n" + e.getLocalizedMessage());
        }
        return super.execute(gstring.toString());
    }

    @Override
    public GroovyRowResult firstRow(GString gstring) throws SQLException {
        try {
            return super.firstRow(gstring);
        } catch (SQLException e) {
            LOGGER.debug("Unable to execute the request as a GString.\n" + e.getLocalizedMessage());
        }
        return super.firstRow(gstring.toString());
    }

    @Override
    public List<GroovyRowResult> rows(GString gstring) throws SQLException {
        try {
            return super.rows(gstring);
        } catch (SQLException e) {
            LOGGER.debug("Unable to execute the request as a GString.\n" + e.getLocalizedMessage());
        }
        return super.rows(gstring.toString());
    }

    @Override
    public void eachRow(GString gstring,
                      @ClosureParams(value= SimpleType.class, options="java.sql.ResultSet") Closure closure)
            throws SQLException {
        try {
            super.eachRow(gstring, closure);
        } catch (SQLException e) {
            LOGGER.debug("Unable to execute the request as a GString.\n" + e.getLocalizedMessage());
            super.eachRow(gstring.toString(), closure);
        }
    }

    @Override
    public IFromBuilder select(String... fields) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        if(fields != null && fields.length > 0){
           query.append(String.join(",", fields));
        }
        else{
            query.append("* ");
        }
        return new FromBuilder(query.toString(), this);
    }

    @Override
    public boolean executeScript(String fileName, Map<String, String> bindings) {
        File file = URIUtilities.fileFromString(fileName);
        try {
            if (FileUtil.isExtensionWellFormated(file, "sql")) {
                return executeScript(new FileInputStream(file), bindings);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to read the SQL file.\n" + e.getLocalizedMessage());
        }
        return false;
    }

    @Override
    public boolean executeScript(InputStream stream, Map<String, String> bindings) {
        SimpleTemplateEngine engine = null;
        if (bindings != null && !bindings.isEmpty()) {
            engine = new SimpleTemplateEngine();
        }
        ScriptReader scriptReader = new ScriptReader(new InputStreamReader(stream));
        scriptReader.setSkipRemarks(true);
        while (true) {
            String commandSQL = scriptReader.readStatement();
            if (commandSQL == null) {
                break;
            }
            if (!commandSQL.isEmpty()) {
                if (engine != null) {
                    try {
                        commandSQL = engine.createTemplate(commandSQL).make(bindings).toString();
                    } catch (ClassNotFoundException | IOException e) {
                        LOGGER.error("Unable to create the template for the Sql command '" + commandSQL + "'.\n" +
                                e.getLocalizedMessage());
                        return false;
                    }
                }
                try {
                    execute(commandSQL);
                } catch (SQLException e) {
                    LOGGER.error("Unable to execute the Sql command '" + commandSQL + "'.\n" + e.getLocalizedMessage());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public MetaClass getMetaClass() {
        return metaClass;
    }

    @Override
    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    @Override
    public boolean save(String tableName, String filePath) {
        return save(tableName, filePath, null);
    }

    @Override
    public boolean save(String tableName, String filePath, String encoding) {
        return IOMethods.saveAsFile(getConnection(), tableName, filePath, encoding);
    }

    @Override
    public boolean save(String tableName, URL url) {
        return save(tableName, url, null);
    }

    @Override
    public boolean save(String tableName, URL url, String encoding) {
        try {
            return save(tableName, url.toURI(), encoding);
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to get the file from the URL '" + url.toString() + "'\n" + e.getLocalizedMessage());
        }
        return false;
    }

    @Override
    public boolean save(String tableName, URI uri) {
        return save(tableName, uri, null);
    }

    @Override
    public boolean save(String tableName, URI uri, String encoding) {
        return save(tableName, new File(uri), encoding);
    }

    @Override
    public boolean save(String tableName, File file) {
        return save(tableName, file, null);
    }

    @Override
    public boolean save(String tableName, File file, String encoding) {
        return save(tableName, file.getAbsolutePath(), encoding);
    }

    private String getTableNameFromPath(String filePath){
        int start = filePath.lastIndexOf("/")+1;
        int end = filePath.lastIndexOf(".");
        if(end == -1){
            end = filePath.length();
        }
        return filePath.substring(start, end).toUpperCase();
    }

    @Override
    public ITable link(String filePath, String tableName, boolean delete) {
        IOMethods.link(filePath, tableName, delete, this);
        return getTable(tableName);
    }

    @Override
    public ITable link(String filePath, String tableName) {
        return link(filePath, tableName, false);
    }

    @Override
    public ITable link(String filePath, boolean delete) {
        String tableName = getTableNameFromPath(filePath);
        if (Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$").matcher(tableName).find()) {
            return link(filePath, tableName, delete);
        } else {
            LOGGER.error("The file name contains unsupported characters");
        }
        return null;
    }

    @Override
    public ITable link(String filePath) {
        return link(filePath, false);
    }

    @Override
    public ITable link(URL url, String tableName, boolean delete) {
        try {
            return link(url.toURI(), tableName, delete);
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to get the file from the URL '" + url.toString() + "'\n" + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public ITable link(URL url, String tableName) {
        try {
            return link(url.toURI(), tableName);
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to get the file from the URL '" + url.toString() + "'\n" + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public ITable link(URL url, boolean delete) {
        try {
            return link(url.toURI(), delete);
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to get the file from the URL '" + url.toString() + "'\n" + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public ITable link(URL url) {
        try {
            return link(url.toURI());
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to get the file from the URL '" + url.toString() + "'\n" + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public ITable link(URI uri, String tableName, boolean delete) {
        return link(new File(uri), tableName, delete);
    }

    @Override
    public ITable link(URI uri, String tableName) {
        return link(new File(uri), tableName);
    }

    @Override
    public ITable link(URI uri, boolean delete) {
        return link(new File(uri), delete);
    }

    @Override
    public ITable link(URI uri) {
        return link(new File(uri));
    }

    @Override
    public ITable link(File file, String tableName, boolean delete) {
        return link(file.getAbsolutePath(), tableName, delete);
    }

    @Override
    public ITable link(File file, String tableName) {
        return link(file.getAbsolutePath(), tableName);
    }

    @Override
    public ITable link(File file, boolean delete) {
        return link(file.getAbsolutePath(), delete);
    }

    @Override
    public ITable link(File file) {
        return link(file.getAbsolutePath());
    }

    @Override
    public ITable load(Map<String, String> properties, String inputTableName) {
        return load(properties, inputTableName, inputTableName, false);
    }

    @Override
    public ITable load(Map<String, String> properties, String inputTableName, boolean delete) {
        return load(properties, inputTableName, inputTableName, delete);
    }

    @Override
    public ITable load(Map<String, String> properties, String inputTableName, String outputTableName) {
        return load(properties, inputTableName, outputTableName, false);
    }

    @Override
    public ITable load(Map<String, String> properties, String inputTableName, String outputTableName, boolean delete) {
        IOMethods.loadTable(properties, inputTableName, outputTableName, delete, this);
        return getTable(outputTableName);
    }

    @Override
    public ITable load(String filePath, String tableName, String encoding, boolean delete) {
        IOMethods.loadFile(filePath, tableName, encoding, delete, this);
        return getTable(tableName);
    }

    @Override
    public ITable load(String filePath, String tableName) {
        return load(filePath, tableName, null, false);
    }

    @Override
    public ITable load(String filePath, String tableName, boolean delete) {
        return load(filePath, tableName, null, delete);
    }

    @Override
    public ITable load(String filePath) {
        return load(filePath, false);
    }

    @Override
    public ITable load(String filePath, boolean delete) {
        String tableName = getTableNameFromPath(filePath);
        if (Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$").matcher(tableName).find()) {
            return load(filePath,tableName, null, delete);
        } else {
            LOGGER.error("Unsupported file characters");
        }
        return null;
    }

    @Override
    public ITable load(URL url, String tableName) {
        try {
            return load(url.toURI(), tableName, null, false);
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to get the file from the URL '" + url.toString() + "'\n" + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public ITable load(URL url, String tableName, boolean delete) {
        try {
            return load(url.toURI(), tableName, null, delete);
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to get the file from the URL '" + url.toString() + "'\n" + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public ITable load(URL url) {
        try {
            return load(url.toURI(), false);
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to get the file from the URL '" + url.toString() + "'\n" + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public ITable load(URL url, boolean delete) {
        try {
            return load(url.toURI(), delete);
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to get the file from the URL '" + url.toString() + "'\n" + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public ITable load(URL url, String tableName, String encoding, boolean delete) {
        try {
            return load(url.toURI(), tableName, encoding, delete);
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to get the file from the URL '" + url.toString() + "'\n" + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public ITable load(URI uri, String tableName) {
        return load(new File(uri), tableName, null, false);
    }

    @Override
    public ITable load(URI uri, String tableName, boolean delete) {
        return load(new File(uri), tableName, null, delete);
    }

    @Override
    public ITable load(URI uri) {
        return load(new File(uri), false);
    }

    @Override
    public ITable load(URI uri, boolean delete) {
        return load(new File(uri), delete);
    }

    @Override
    public ITable load(URI uri, String tableName, String encoding, boolean delete) {
        return load(new File(uri), tableName, encoding, delete);
    }

    @Override
    public ITable load(File file, String tableName) {
        return load(file.getAbsolutePath(), tableName, null, false);
    }

    @Override
    public ITable load(File file, String tableName, boolean delete) {
        return load(file.getAbsolutePath(), tableName, null, delete);
    }

    @Override
    public ITable load(File file) {
        return load(file.getAbsolutePath(), false);
    }

    @Override
    public ITable load(File file, boolean delete) {
        return load(file.getAbsolutePath(), delete);
    }

    @Override
    public ITable load(File file, String tableName, String encoding, boolean delete) {
        return load(file.getAbsolutePath(), tableName, encoding, delete);
    }

    @Override
    public IDataSourceLocation getLocation(){
        try {
            String url = this.getConnection().getMetaData().getURL();
            return url == null ? null : new DataSourceLocation(url.substring(url.lastIndexOf(":") + 1));
        } catch (SQLException e) {
            LOGGER.error("Unable to get the connection metadata.\n" + e.getLocalizedMessage());
        }
        return null;
    }


    @Override
    public Collection<String> getTableNames() {
        try {
            return JDBCUtilities.getTableNames(getConnection().getMetaData(), null, null, null, null);
        } catch (SQLException e) {
            LOGGER.error("Unable to get the database metadata.\n" + e.getLocalizedMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public IDataSet getDataSet(String dataSetName) {
        List<String> geomFields;
        try {
            geomFields = SFSUtilities.getGeometryFields(getConnection(), new TableLocation(dataSetName));
        } catch (SQLException e) {
            LOGGER.error("Unable to get the geometric fields.\n" + e.getLocalizedMessage());
            return getTable(dataSetName);
        }
        if (geomFields.size() >= 1) {
            return getSpatialTable(dataSetName);
        }
        return getTable(dataSetName);
    }
}
