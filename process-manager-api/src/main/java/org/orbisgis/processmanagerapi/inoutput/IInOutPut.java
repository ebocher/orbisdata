/*
 * Bundle ProcessManager API is part of the OrbisGIS platform
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
 * ProcessManager API is distributed under GPL 3 license.
 *
 * Copyright (C) 2018 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * ProcessManager API is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * ProcessManager API is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ProcessManager API. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.processmanagerapi.inoutput;

import org.orbisgis.processmanagerapi.IProcess;

/**
 * This interface defines the methods dedicated the wrapping of input/output.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public interface IInOutPut {

    /**
     * Return the input/output name.
     *
     * @return The input/output name.
     */
    String getName();

    /**
     * Sets the name of the input/output.
     *
     * @param name Name of the input/output.
     */
    void setName(String name);

    /**
     * Return the {@link IProcess} of the input/output.
     *
     * @return The {@link IProcess} of the input/output.
     */
    IProcess getProcess();

    /**
     * Sets the {@link IProcess} of the input/output.
     *
     * @param process The {@link IProcess} of the input/output.
     */
    void setProcess(IProcess process);

    /**
     * Sets the type of the input/output.
     *
     * @param type The type of the input/output.
     */
    IInOutPut setType(Class type);

    /**
     * Return the type of the input/output.
     *
     * @return The type of the input/output.
     */
    Class getType();

    /**
     * Sets the title of the input/output.
     *
     * @param title The title of the input/output.
     */
    IInOutPut setTitle(String title);

    /**
     * Return the title or the input/output.
     *
     * @return The title of the input/output.
     */
    String getTitle();

    /**
     * Sets the description of the input/output.
     *
     * @param description The description of the input/output.
     */
    IInOutPut setDescription(String description);

    /**
     * Return the description of the input/output.
     *
     * @return The description of the input/output.
     */
    String getDescription();

    /**
     * Sets the keywords of the input/output.
     *
     * @param keywords The keywords of the input/output.
     */
    IInOutPut setKeywords(String[] keywords);

    /**
     * Return the keywords of the input/output.
     *
     * @return The keywords of the input/output.
     */
    String[] getKeywords();

    /**
     * Methods used by groovy to simplify the syntax.
     *
     * @param name Name of the methods.
     * @param args Object array of the method argument.
     *
     * @return Method result.
     */
    Object methodMissing(String name, Object args);
}
