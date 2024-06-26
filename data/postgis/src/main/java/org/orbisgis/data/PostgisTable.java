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

import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.orbisgis.data.api.dataset.ISpatialTable;
import org.orbisgis.data.api.dataset.ITable;
import org.orbisgis.data.api.datasource.IJdbcDataSource;
import org.orbisgis.data.jdbc.JdbcTable;
import org.orbisgis.data.jdbc.ResultSetIterator;
import org.orbisgis.data.jdbc.resultset.ResultSetSpliterator;
import org.orbisgis.data.jdbc.resultset.StreamResultSet;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementation of {@link ITable} for PostGIG.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS Lab-STICC 2018-2019 / Chaire GEOTERA 2020)
 */
public class PostgisTable extends JdbcTable<StreamResultSet> {

    /**
     * Main constructor.
     *
     * @param tableLocation  {@link TableLocation} that identify the represented table.
     * @param baseQuery      Query for the creation of the ResultSet
     * @param statement      Statement used to request the database.
     * @param jdbcDataSource DataSource to use for the creation of the resultSet.
     */
    public PostgisTable(TableLocation tableLocation, String baseQuery,
                        Statement statement, List<Object> params,
                        IJdbcDataSource jdbcDataSource) {
        super(DBTypes.POSTGIS, jdbcDataSource, tableLocation, statement, params, baseQuery);
    }

    @Override
    public Object asType(Class<?> clazz) throws Exception {
        if (ISpatialTable.class.isAssignableFrom(clazz)) {
            return new PostgisSpatialTable(getTableLocation(), getBaseQuery(), getStatement(), getParams(),
                    getJdbcDataSource());
        } else if (ITable.class.isAssignableFrom(clazz)) {
            return new PostgisTable(getTableLocation(), getBaseQuery(), getStatement(), getParams(),
                    getJdbcDataSource());
        } else {
            return super.asType(clazz);
        }
    }

    @Override
    public ResultSetIterator iterator() {
        return new ResultSetIterator(this);
    }


    @Override
    public Stream<? extends StreamResultSet> stream() throws Exception {
        Spliterator<StreamResultSet> spliterator = new ResultSetSpliterator<>(this.getRowCount(), new StreamResultSet(getResultSet()));
        return StreamSupport.stream(spliterator, true);
    }
}