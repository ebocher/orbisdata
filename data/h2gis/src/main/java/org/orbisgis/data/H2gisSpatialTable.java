/*
 * Bundle H2GIS is part of the OrbisGIS platform
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
 * H2GIS is distributed under LGPL 3 license.
 *
 * Copyright (C) 2018 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.data;

import org.h2gis.utilities.GeometryTableUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.wrapper.ConnectionWrapper;
import org.h2gis.utilities.wrapper.SpatialResultSetImpl;
import org.h2gis.utilities.wrapper.StatementWrapper;
import org.orbisgis.data.api.dataset.ISpatialTable;
import org.orbisgis.data.api.dataset.ITable;
import org.orbisgis.data.api.datasource.IJdbcDataSource;
import org.orbisgis.data.jdbc.JdbcSpatialTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

/**
 * Implementation of {@link ISpatialTable} for H2GIS.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS Lab-STICC 2018-2019 / Chaire GEOTERA 2020)
 */
public class H2gisSpatialTable extends JdbcSpatialTable {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2gisTable.class);

    /**
     * Main constructor.
     *
     * @param tableLocation  {@link TableLocation} that identify the represented table.
     * @param baseQuery      Query for the creation of the ResultSet
     * @param statement      Statement used to request the database.
     * @param jdbcDataSource DataSource to use for the creation of the resultSet.
     */
    public H2gisSpatialTable(TableLocation tableLocation, String baseQuery,
                             Statement statement, List<Object> params,
                             IJdbcDataSource jdbcDataSource) {
        super(DBTypes.H2GIS, jdbcDataSource, tableLocation, statement, baseQuery, params);
    }

    @Override
    protected ResultSet getResultSet() {
        if (resultSet == null) {
            try {
                Statement st = getStatement();
                if(st instanceof PreparedStatement) {
                    resultSet = ((PreparedStatement)st).executeQuery();
                }
                else {
                    resultSet = getStatement().executeQuery(getBaseQuery());
                }
                resultSet = new SpatialResultSetImpl(resultSet, new StatementWrapper(getStatement(), new ConnectionWrapper(getJdbcDataSource().getConnection())));
            } catch (SQLException e) {
                LOGGER.error("Unable to execute the query '" + getBaseQuery() + "'.\n" + e.getLocalizedMessage());
                return null;
            }
        }
        return resultSet;
    }


    @Override
    public Object asType(Class<?> clazz) {
        if (ISpatialTable.class.isAssignableFrom(clazz)) {
            return new H2gisSpatialTable(getTableLocation(), getBaseQuery(), getStatement(), getParams(),
                    getJdbcDataSource());
        } else if (ITable.class.isAssignableFrom(clazz)) {
            return new H2gisTable(getTableLocation(), getBaseQuery(), getStatement(), getParams(),
                    getJdbcDataSource());
        } else {
            return super.asType(clazz);
        }
    }

    @Override
    public int getSrid() {
        if (getTableLocation() == null) {
            throw new UnsupportedOperationException();
        }
        try {
            Connection con = getJdbcDataSource().getConnection();
            if(con == null){
                LOGGER.error("Unable to get connection for the table SRID.");
                return -1;
            }
            return GeometryTableUtilities.getSRID(con, getTableLocation());
        } catch (SQLException e) {
            LOGGER.error("Unable to get the table SRID.", e);
        }
        return -1;
    }

    @Override
    public ISpatialTable reproject(int srid) {
        try {
            ResultSetMetaData meta = getMetaData();
            if(meta == null){
                return null;
            }
            int columnCount = meta.getColumnCount();
            String[] fieldNames = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                String columnName = meta.getColumnName(i);
                if (meta.getColumnTypeName(i).toLowerCase().startsWith("geometry")) {
                    fieldNames[i - 1] = "ST_TRANSFORM(" + columnName + ", " + srid + ") AS " + columnName;
                } else {
                    fieldNames[i - 1] = columnName;
                }
            }
            String query = "SELECT " + String.join(",", fieldNames) + " FROM " +
                    (getTableLocation() == null ? getBaseQuery() + " as foo " : getTableLocation().toString(getDbType()));
            return new H2gisSpatialTable(null, query, getStatement(), getParams(), getJdbcDataSource());
        } catch (SQLException e) {
            LOGGER.error("Cannot reproject the table '" + getLocation() + "' in the SRID '" + srid + "'.\n", e);
            return null;
        }
    }
}
