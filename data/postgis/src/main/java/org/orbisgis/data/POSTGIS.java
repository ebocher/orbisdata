/*
 * Bundle PostGIS is part of the OrbisGIS platform
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
 * PostGIS is distributed under LGPL 3 license.
 *
 * Copyright (C) 2018 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * PostGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * PostGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * PostGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.data;

import groovy.lang.GString;
import org.h2gis.postgis_jts.PostGISDBFactory;
import org.h2gis.utilities.FileUtilities;
import org.h2gis.utilities.GeometryTableUtilities;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.locationtech.jts.geom.Geometry;
import org.orbisgis.data.api.dataset.IJdbcSpatialTable;
import org.orbisgis.data.api.dataset.IJdbcTable;
import org.orbisgis.data.api.dataset.ISpatialTable;
import org.orbisgis.data.jdbc.JdbcDataSource;
import org.orbisgis.data.jdbc.JdbcSpatialTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * Implementation of the IJdbcDataSource interface dedicated to the usage of an postgres/postgis database.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS Lab-STICC 2018 / Chaire GEOTERA 2020)
 */
public class POSTGIS extends JdbcDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(POSTGIS.class);

    /**
     * Private constructor.
     *
     * @param connection {@link Connection} to the database.
     */
    private POSTGIS(Connection connection) throws Exception {
        super(connection, DBTypes.POSTGIS);
    }

    /**
     * Private constructor.
     *
     * @param dataSource {@link DataSource} to the database.
     */
    private POSTGIS(DataSource dataSource) throws Exception {
        super(dataSource, DBTypes.POSTGIS);
    }

    /**
     * Create an instance of {@link POSTGIS} from a {@link Connection}
     *
     * @param connection {@link Connection} of the DataBase.
     * @return {@link POSTGIS} object if the DataBase has been successfully open, null otherwise.
     */
    public static POSTGIS open(Connection connection) throws Exception {
        return new POSTGIS(connection);
    }

    /**
     * Create an instance of {@link POSTGIS} from a {@link DataSource}
     *
     * @param dataSource {@link Connection} of the DataBase.
     * @return {@link POSTGIS} object if the DataBase has been successfully open, null otherwise.
     */
    public static POSTGIS open(DataSource dataSource) throws Exception {
        return new POSTGIS(dataSource);
    }

    /**
     * Create an instance of {@link POSTGIS} from file
     *
     * @param file .properties file containing the information for the DataBase opening.
     * @return {@link POSTGIS} object if the DataBase has been successfully open, null otherwise.
     */
    public static POSTGIS open(File file) throws Exception {
        if (FileUtilities.isExtensionWellFormated(file, "properties")) {
            Properties prop = new Properties();
            FileInputStream fous = new FileInputStream(file);
            prop.load(fous);
            return open(prop);
        }
        throw new IllegalArgumentException("Invalid properties file");
    }

    /**
     * Create an instance of {@link POSTGIS} from properties
     *
     * @param properties Properties for the opening of the DataBase.
     * @return {@link POSTGIS} object if the DataBase has been successfully open, null otherwise.
     */
    public static POSTGIS open(Properties properties) throws Exception {
        Connection connection = PostGISDBFactory.createDataSource(properties).getConnection();
        return new POSTGIS(connection);
    }

    /**
     * Open the {@link POSTGIS} database with the given properties and return the corresponding {@link POSTGIS} object.
     *
     * @param properties Map of the properties to use for the database opening.
     * @return An instantiated {@link POSTGIS} object wrapping the Sql object connected to the database.
     */
    public static POSTGIS open(Map<String, String> properties) throws Exception {
        Properties props = new Properties();
        props.putAll(properties);
        return open(props);
    }

    /**
     * Open the {@link POSTGIS} database at the given path and return the corresponding {@link POSTGIS} object.
     *
     * @param path Path of the database to open.
     * @return An instantiated {@link POSTGIS} object wrapping the Sql object connected to the database.
     */
    public static POSTGIS open(String path) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("databaseName", path);
        return open(map);
    }

    /**
     * Open the {@link POSTGIS} database at the given path and return the corresponding {@link POSTGIS} object.
     *
     * @param path     Path of the database to open.
     * @param user     User of the database.
     * @param password Password for the user.
     * @return An instantiated {@link POSTGIS} object wrapping the Sql object connected to the database.
     */
    public static POSTGIS open(String path, String user, String password) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("databaseName", path);
        map.put("user", user);
        map.put("password", password);
        return open(map);
    }

    @Override
    public IJdbcTable getTable(String nameOrQuery, Statement statement) throws Exception {
        return getTable(nameOrQuery, null, statement);
    }

    @Override
    public IJdbcTable getTable(GString nameOrQuery, Statement statement) throws Exception {
        if (nameOrQuery.getValueCount() == 0 ||
                !nameOrQuery.toString().startsWith("(") && !nameOrQuery.toString().endsWith("(")) {
            return getTable(nameOrQuery.toString(), statement);
        }
        List<Object> params = this.getParameters(nameOrQuery);
        String sql = this.asSql(nameOrQuery, params);
        return getTable(sql, params, statement);
    }

    @Override
    public IJdbcTable getTable(String nameOrQuery, List<Object> params,
                               Statement statement) throws Exception {
        Connection connection = getConnection();
        String query;
        TableLocation location;
        if (!nameOrQuery.startsWith("(") && !nameOrQuery.endsWith(")")) {
            org.h2gis.utilities.TableLocation inputLocation = TableLocation.parse(nameOrQuery, DBTypes.POSTGIS);
            query = String.format("SELECT * FROM %s", inputLocation);
            location = new TableLocation(inputLocation.getCatalog(), inputLocation.getSchema(), inputLocation.getTable(), DBTypes.POSTGIS);
        } else {
            query = nameOrQuery;
            location = null;
        }
        try {
            if (connection != null) {
                if (location != null) {
                    boolean hasGeom = GeometryTableUtilities.hasGeometryColumn(connection, location);
                    if (!getConnection().getAutoCommit()) {
                        super.commit();
                    }
                    if (hasGeom) {
                        return new PostgisSpatialTable(location, query, statement, params, this);
                    } else {
                        return new PostgisTable(location, query, statement, params, this);
                    }
                } else {
                    ResultSet rs;
                    if (statement instanceof PreparedStatement) {
                        PreparedStatement st = connection.prepareStatement("(SELECT * FROM " + query + "AS foo WHERE 1=0)");
                        for (int i = 0; i < params.size(); i++) {
                            st.setObject(i + 1, params.get(i));
                        }
                        rs = st.executeQuery();
                    } else {
                        rs = statement.executeQuery("(SELECT * FROM " + query + "AS foo WHERE 1=0)");
                    }
                    boolean hasGeom = GeometryTableUtilities.hasGeometryColumn(rs);
                    if (!getConnection().getAutoCommit()) {
                        super.commit();
                    }
                    if (hasGeom) {
                        return new PostgisSpatialTable(location, query, statement, params, this);
                    } else {
                        return new PostgisTable(location, query, statement, params, this);
                    }
                }
            }
        } catch (SQLException e) {
            try {
                if (!getConnection().getAutoCommit()) {
                    super.rollback();
                }
            } catch (SQLException e2) {
                throw new SQLException("Unable to get the table data", e2);
            }
        }
        throw new IllegalArgumentException("Cannot read the table " + query);
    }

    @Override
    public IJdbcTable getTable(String tableName) throws Exception {
        Connection connection = getConnection();
        Statement statement;
        try {
            DatabaseMetaData dbdm = connection.getMetaData();
            int type = ResultSet.TYPE_FORWARD_ONLY;
            if (dbdm.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE)) {
                type = ResultSet.TYPE_SCROLL_SENSITIVE;
            } else if (dbdm.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE)) {
                type = ResultSet.TYPE_SCROLL_INSENSITIVE;
            }
            int concur = ResultSet.CONCUR_READ_ONLY;
            if (dbdm.supportsResultSetConcurrency(type, ResultSet.CONCUR_UPDATABLE)) {
                concur = ResultSet.CONCUR_UPDATABLE;
            }
            statement = connection.createStatement(type, concur);
        } catch (SQLException e) {
            throw new SQLException("Unable to create Statement.\n" + e.getLocalizedMessage());
        }
        return getTable(tableName, statement);
    }

    @Override
    public IJdbcTable getTable(GString nameOrQuery) throws Exception {
        if (nameOrQuery.getValueCount() == 0 ||
                !nameOrQuery.toString().startsWith("(") && !nameOrQuery.toString().endsWith("(")) {
            return getTable(nameOrQuery.toString());
        }
        List<Object> params = this.getParameters(nameOrQuery);
        String sql = this.asSql(nameOrQuery, params);
        return getTable(sql, params);
    }

    @Override
    public IJdbcTable getTable(String query, List<Object> params) throws Exception {
        if (params == null || params.isEmpty()) {
            return getTable(query);
        }
        PreparedStatement prepStatement;
        try {
            Connection connection = getConnection();
            DatabaseMetaData dbdm = connection.getMetaData();
            int type = ResultSet.TYPE_FORWARD_ONLY;
            if (dbdm.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE)) {
                type = ResultSet.TYPE_SCROLL_SENSITIVE;
            } else if (dbdm.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE)) {
                type = ResultSet.TYPE_SCROLL_INSENSITIVE;
            }
            int concur = ResultSet.CONCUR_READ_ONLY;
            if (dbdm.supportsResultSetConcurrency(type, ResultSet.CONCUR_UPDATABLE)) {
                concur = ResultSet.CONCUR_UPDATABLE;
            }
            prepStatement = connection.prepareStatement(query, type, concur);
            setStatementParameters(prepStatement, params);
        } catch (SQLException e) {
            throw new SQLException("Unable to create the prepared statement.", e);
        }
        return getTable(query, params, prepStatement);
    }

    @Override
    public IJdbcSpatialTable getSpatialTable(String tableName, Statement statement) throws Exception {
        IJdbcTable table = getTable(tableName, statement);
        if (table instanceof ISpatialTable) {
            return (JdbcSpatialTable) table;
        } else {
            throw new IllegalArgumentException("The table " + tableName + "is not a spatial table.");
        }
    }

    @Override
    public IJdbcSpatialTable getSpatialTable(GString nameOrQuery, Statement statement) throws Exception {
        if (nameOrQuery.getValueCount() == 0 ||
                !nameOrQuery.toString().startsWith("(") && !nameOrQuery.toString().endsWith("(")) {
            return getSpatialTable(nameOrQuery.toString(), statement);
        }
        List<Object> params = this.getParameters(nameOrQuery);
        String sql = this.asSql(nameOrQuery, params);
        return getSpatialTable(sql, params, statement);
    }

    @Override
    public IJdbcSpatialTable getSpatialTable(String nameOrQuery, List<Object> params, Statement statement) throws Exception {
        IJdbcTable table = getTable(nameOrQuery, params, statement);
        if (table instanceof ISpatialTable) {
            return (JdbcSpatialTable) table;
        } else {
            throw new IllegalArgumentException("The table " + nameOrQuery + "is not a spatial table.");
        }
    }

    @Override
    public IJdbcSpatialTable getSpatialTable(String query, List<Object> params) throws Exception {
        IJdbcTable table = getTable(query, params);
        if (table instanceof ISpatialTable) {
            return (JdbcSpatialTable) table;
        } else {
            throw new IllegalArgumentException("The table " + query + "is not a spatial table.");
        }
    }

    @Override
    public IJdbcSpatialTable getSpatialTable(String tableName) throws Exception {
        IJdbcTable table = getTable(tableName);
        if (table instanceof ISpatialTable) {
            return (JdbcSpatialTable) table;
        } else {
            throw new IllegalArgumentException("The table " + tableName + "is not a spatial table.");
        }
    }

    @Override
    public IJdbcSpatialTable getSpatialTable(GString nameOrQuery) throws Exception {
        if (nameOrQuery.getValueCount() == 0 ||
                !nameOrQuery.toString().startsWith("(") && !nameOrQuery.toString().endsWith("(")) {
            return getSpatialTable(nameOrQuery.toString());
        }
        List<Object> params = this.getParameters(nameOrQuery);
        String sql = this.asSql(nameOrQuery, params);
        return getSpatialTable(sql, params);
    }

    @Override
    public boolean hasTable(String tableName) throws Exception {
        try {
            return JDBCUtilities.tableExists(getConnection(), TableLocation.parse(tableName, DBTypes.POSTGIS));
        }catch (SQLException e){
            return false;
        }
    }

    @Override
    public Collection<String> getColumnNames(String location) throws Exception {
        try {
            return JDBCUtilities.getColumnNames(getConnection(), TableLocation.parse(location, DBTypes.POSTGIS).toString());
        }catch (SQLException e){
            return null;
        }
    }

    @Override
    public long getRowCount(String tableName) throws Exception {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Cannot get row count on empty or null table");
        }
        return JDBCUtilities.getRowCount(getConnection(), TableLocation.parse(tableName, DBTypes.POSTGIS));
    }

    @Override
    public String link(Map dataSourceProperties, String tableName) throws Exception {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String link(Map dataSourceProperties, String sourceTableName, boolean delete) throws Exception {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String link(Map dataSourceProperties, String sourceTableName, String targetTableName, boolean delete) throws Exception {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object asType(Class<?> clazz) {
        return null;
    }

    @Override
    public void dropColumn(String tableName, String... columnName) throws Exception {
        dropColumn(tableName, List.of(columnName));
    }

    @Override
    public void dropColumn(String tableName, List<String> columnNames) throws Exception {
        if (tableName == null || columnNames == null || columnNames.isEmpty()) {
            throw new IllegalArgumentException("Illegal argument to drop the column");
        }
        StringBuilder sb = new StringBuilder("ALTER TABLE IF EXISTS " + TableLocation.parse(tableName, DBTypes.POSTGIS));
        int count = columnNames.size();
        for (int i = 0; i < count; i++) {
            String col = columnNames.get(i);
            if (col != null && !col.isEmpty()) {
                sb.append(" DROP COLUMN IF EXISTS " + col);
            }
            if (i < count - 1) {
                sb.append(",");
            }
        }
        execute(sb.toString());
    }

    @Override
    public Geometry getExtent(String tableName) throws Exception {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Cannot get the extent on empty or null table");
        }
        return GeometryTableUtilities.getEnvelope(getConnection(), TableLocation.parse(tableName, DBTypes.POSTGIS));
    }

    @Override
    public Geometry getExtent(String tableName, String... geometryColumns) throws Exception {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Cannot get the extent on empty or null table");
        }
        return GeometryTableUtilities.getEnvelope(getConnection(), TableLocation.parse(tableName, DBTypes.POSTGIS), geometryColumns);
    }
}
