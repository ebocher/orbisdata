/*
 * Bundle ProcessManager is part of the OrbisGIS platform
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
 * ProcessManager is distributed under LGPL 3 license.
 *
 * Copyright (C) 2018 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * ProcessManager is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * ProcessManager is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ProcessManager. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.orbisdata.processmanager.process.inoutput;

import org.orbisgis.commons.annotations.NotNull;
import org.orbisgis.commons.annotations.Nullable;
import org.orbisgis.orbisdata.processmanager.api.IProcess;
import org.orbisgis.orbisdata.processmanager.api.inoutput.IInput;

import java.util.UUID;

/**
 * Implementation of the {@link IInput} interface.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS 2019-2020)
 */
public class Input extends InOutPut implements IInput {

    @Nullable
    private Object dfltValue;

    /**
     * Main constructor.
     *
     * @param process {@link IProcess} of the input/output.
     * @param name    Name of the input/output.
     */
    public Input(@Nullable IProcess process, @Nullable String name) {
        super(process, name);
    }

    /**
     * Empty constructor.
     */
    public Input() {
        super(null, "input_" + UUID.randomUUID().toString());
    }

    @NotNull
    public static Input call() {
        return new Input();
    }

    @Override
    @NotNull
    public Input optional(Object dfltValue) {
        this.dfltValue = dfltValue;
        return this;
    }

    @Override
    public boolean isOptional() {
        return dfltValue != null;
    }

    @Override
    public Object getDefaultValue() {
        return dfltValue;
    }

    @Override
    @NotNull
    public Input mandatory() {
        dfltValue = null;
        return this;
    }

    @Override
    public boolean isMandatory() {
        return dfltValue == null;
    }

    @Override
    @NotNull
    public Input setTitle(String title) {
        super.setTitle(title);
        return this;
    }

    @Override
    @NotNull
    public Input setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    @Override
    @NotNull
    public Input setKeywords(String[] keywords) {
        super.setKeywords(keywords);
        return this;
    }

    @Override
    @NotNull
    public Input setType(Class<?> type) {
        super.setType(type);
        return this;
    }
}
