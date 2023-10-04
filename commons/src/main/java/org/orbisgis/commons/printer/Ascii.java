/*
 * Bundle Commons is part of the OrbisGIS platform
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
 * Commons is distributed under LGPL 3 license.
 *
 * Copyright (C) 2018-2020 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * Commons is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Commons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Commons. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.commons.printer;


import static org.orbisgis.commons.utilities.CheckUtils.checkNotNull;

/**
 * Extension of {@link CustomPrinter} for the printing of data in an Ascii style.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class Ascii extends CustomPrinter {

    /**
     * Constructor.
     *
     * @param builder Not null {@link StringBuilder} used for building the string.
     */
    public Ascii(StringBuilder builder) {
        super(builder);
        if (this.builder.length() != 0) {
            builder.append("\n");
        }
    }

    /**
     * Main constructor.
     */
    public Ascii() {
        super(new StringBuilder());
    }

    @Override
    public void appendTableLineSeparator() {
        if (isDrawingTable) {
            builder.append("+");
            for (int i = 0; i < columnCount; i++) {
                for (int j = 0; j < columnWidth; j++) {
                    builder.append("-");
                }
                builder.append("+");
            }
            builder.append("\n");
        }
    }


    @Override
    public void appendTableValue(Object value, CellPosition position) {
        checkNotNull(value, "The value to append should not be null.");
        checkNotNull(position, "The position of the value should not be null.");
        if (isDrawingTable) {
            builder.append("|");
            String cut = value.toString();
            if (cut.length() > columnWidth) {
                cut = cut.substring(0, columnWidth - 3) + "...";
            }

            switch (position) {
                case LEFT:
                    builder.append(cut);
                    for (int i = 0; i < (columnWidth - cut.length()); i++) {
                        builder.append(" ");
                    }
                    break;
                case RIGHT:
                    for (int i = 0; i < (columnWidth - cut.length()); i++) {
                        builder.append(" ");
                    }
                    builder.append(cut);
                    break;
                case CENTER:
                default:
                    for (int i = 0; i < (columnWidth - cut.length()) / 2; i++) {
                        builder.append(" ");
                    }
                    builder.append(cut);
                    for (int i = 0; i < (columnWidth - cut.length()) - (columnWidth - cut.length()) / 2; i++) {
                        builder.append(" ");
                    }
                    break;
            }
            columnIndex++;
            if (columnIndex == columnCount) {
                columnIndex = 0;
                builder.append("|");
                builder.append("\n");
            }
        }
    }

    @Override
    public void appendTableHeaderValue(Object value, CellPosition position) {
        checkNotNull(value, "The value to append should not be null.");
        checkNotNull(position, "The position of the value should not be null.");
        this.appendTableValue(value, position);
    }

    @Override
    public void appendTableTitle(Object title) {
        checkNotNull(title, "The title to append should not be null.");
        if (isDrawingTable) {
            builder.append("+");
            for (int j = 0; j < columnWidth; j++) {
                builder.append("-");
            }
            builder.append("+");
            builder.append("\n");

            builder.append("|");
            String cut = title.toString();
            if (cut.length() > columnWidth) {
                cut = cut.substring(0, columnWidth - 3) + "...";
            }
            for (int i = 0; i < (columnWidth - cut.length()) / 2; i++) {
                builder.append(" ");
            }
            builder.append(cut);
            for (int i = 0; i < (columnWidth - cut.length()) - (columnWidth - cut.length()) / 2; i++) {
                builder.append(" ");
            }
            builder.append("|");
            builder.append("\n");
        }
    }

    @Override
    public void appendValue(Object value) {
        checkNotNull(value, "The value to append should not be null.");
        if(isDrawingTable){
            appendTableValue(value);
        }
        else{
            builder.append(value).append("\n");
        }
    }
}
