/*
 * Bundle JDBC is part of the OrbisGIS platform
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
 * JDBC is distributed under LGPL 3 license.
 *
 * Copyright (C) 2018 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * JDBC is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * JDBC is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JDBC. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.data.jdbc.dsl;

import groovy.lang.Closure;
import org.orbisgis.commons.printer.ICustomPrinter;
import org.orbisgis.data.api.dataset.ISpatialTable;
import org.orbisgis.data.api.dataset.ITable;
import org.orbisgis.data.api.datasource.IJdbcDataSource;
import org.orbisgis.data.api.dsl.IBuilderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link IBuilderResult}.
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
    protected abstract IJdbcDataSource getDataSource();

    /**
     * Return the query to execute.
     *
     * @return The query to execute.
     */
    protected abstract String getQuery();

    @Override
    public void eachRow(Closure<Object> closure) throws Exception{
        ISpatialTable table = ((ISpatialTable) asType(ISpatialTable.class));
        if(table != null) {
            table.eachRow(closure);
        }
    }

    @Override
    public Object asType(Class<?> clazz) throws Exception{
        if (ICustomPrinter.class.isAssignableFrom(clazz)) {
            ITable table = this.getTable();
            if(table != null) {
                return table.asType(clazz);
            }
        }
        if (ISpatialTable.class.isAssignableFrom(clazz)) {
            return getSpatialTable();
        } else if (ITable.class.isAssignableFrom(clazz)) {
            return getTable();
        }
        return null;
    }

    @Override
    public String toString() {
        return "(" + getQuery() + ") as foo";
    }

    @Override
    public ITable getTable() throws Exception{
        return getDataSource().getTable(toString(), getParams());
    }

    @Override
    public ISpatialTable getSpatialTable() throws Exception {
        return getDataSource().getSpatialTable(toString(), getParams());
    }
}
