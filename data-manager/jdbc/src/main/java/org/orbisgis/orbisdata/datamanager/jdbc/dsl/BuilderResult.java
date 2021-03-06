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
package org.orbisgis.orbisdata.datamanager.jdbc.dsl;

import groovy.lang.Closure;
import org.h2gis.utilities.wrapper.ConnectionWrapper;
import org.h2gis.utilities.wrapper.StatementWrapper;
import org.orbisgis.commons.printer.ICustomPrinter;
import org.orbisgis.orbisdata.datamanager.jdbc.JdbcDataSource;
import org.orbisgis.orbisdata.datamanager.jdbc.TableLocation;
import org.orbisgis.orbisdata.datamanager.jdbc.h2gis.H2gisSpatialTable;
import org.orbisgis.orbisdata.datamanager.jdbc.h2gis.H2gisTable;
import org.orbisgis.orbisdata.datamanager.jdbc.postgis.PostgisSpatialTable;
import org.orbisgis.orbisdata.datamanager.jdbc.postgis.PostgisTable;
import org.orbisgis.orbisdata.datamanager.api.dataset.ISpatialTable;
import org.orbisgis.orbisdata.datamanager.api.dataset.ITable;
import org.orbisgis.orbisdata.datamanager.api.dsl.IBuilderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Implementation of IBuilderResult
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public abstract class BuilderResult implements IBuilderResult {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuilderResult.class);

    /**
     * Return the database to use to execute the query.
     *
     * @return The database to use to execute the query.
     */
    protected abstract JdbcDataSource getDataSource();

    /**
     * Return the query to execute.
     *
     * @return The query to execute.
     */
    protected abstract String getQuery();

    @Override
    public void eachRow(Closure closure) {
        ((ISpatialTable)asType(ISpatialTable.class)).eachRow(closure);
    }

    @Override
    public Object asType(Class clazz) {
        if(ICustomPrinter.class.isAssignableFrom(clazz)){
            return this.getTable().asType(clazz);
        }
        Statement statement;
        try {
            statement = getDataSource().getConnection()
                    .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException e) {
            LOGGER.error("Unable to create the StatementWrapper.\n" + e.getLocalizedMessage());
            return null;
        }
        String name = "SQL_QUERY_RESULT";
        switch(getDataSource().getDataBaseType()) {
            default:
            case H2GIS:
                if(!(statement instanceof StatementWrapper)){
                    LOGGER.warn("The statement class not compatible with the database.");
                    statement = new StatementWrapper(statement, new ConnectionWrapper(getDataSource().getConnection()));
                }
                if(clazz == ISpatialTable.class) {
                    return new H2gisSpatialTable(new TableLocation(getDataSource().getLocation().toString(), name),
                            getQuery(), (StatementWrapper) statement,
                            getDataSource());
                }
                else if(clazz == ITable.class) {
                    return new H2gisTable(new TableLocation(getDataSource().getLocation().toString(), name),
                            getQuery(), (StatementWrapper) statement, getDataSource());
                }
            case POSTGIS:
                if(!(statement instanceof org.h2gis.postgis_jts.StatementWrapper)){
                    LOGGER.error("The statement class not compatible with the database.");
                    break;
                }
                if(clazz == ISpatialTable.class) {
                    return new PostgisSpatialTable(new TableLocation(getDataSource().getLocation().toString(), name),
                            getQuery(), (org.h2gis.postgis_jts.StatementWrapper)statement, getDataSource());
                }
                else if(clazz == ITable.class) {
                    return new PostgisTable(new TableLocation(getDataSource().getLocation().toString(), name),
                            getQuery(), (org.h2gis.postgis_jts.StatementWrapper)statement, getDataSource());
                }
        }
        return null;
    }

    @Override
    public String toString(){
        return getQuery();
    }

    @Override
    public ITable getTable() {
        return (ITable)this.asType(ITable.class);
    }

    @Override
    public ISpatialTable getSpatialTable() {
        return (ISpatialTable)this.asType(ISpatialTable.class);
    }
}
