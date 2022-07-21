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
 * Extension of {@link CustomPrinter} for the printing of data in an Html style.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class Html extends CustomPrinter {

    /**
     * Main constructor.
     *
     * @param builder Not null {@link StringBuilder} used for building the string.
     */
    public Html(StringBuilder builder) {
        super(builder);
    }

    /**
     * Main constructor.
     */
    public Html() {
        super(new StringBuilder());
    }

    @Override
    public void appendTableLineSeparator() {
        if (isDrawingTable) {
            builder.append("<tr></tr>\n");
        }
    }

    @Override
    public void startTable(int columnWidth, int columnCount) {
        builder.append("<table>\n");
        super.startTable(columnWidth, columnCount);
    }

    @Override
    public void endTable() {
        super.endTable();
        builder.append("</table>\n");
    }

    @Override
    public void appendTableValue(Object value, CellPosition position) {
        checkNotNull(value, "The value to append should not be null.");
        checkNotNull(position, "The position of the value should not be null.");
        if (isDrawingTable) {
            if (columnIndex == 0) {
                builder.append("<tr>\n");
            }
            String cut = value.toString();
            if (cut.length() > columnWidth) {
                cut = cut.substring(0, columnWidth - 3) + "...";
            }
            builder.append("<td align=\"");
            builder.append(position);
            builder.append("\">");
            builder.append(cut);
            builder.append("</td>");
            builder.append("\n");
            columnIndex++;
            if (columnIndex == columnCount) {
                builder.append("</tr>\n");
                columnIndex = 0;
            }
        }
    }

    @Override
    public void appendTableHeaderValue(Object value, CellPosition position) {
        checkNotNull(value, "The value to append should not be null.");
        checkNotNull(position, "The position of the value should not be null.");
        if (isDrawingTable) {
            if (columnIndex == 0) {
                builder.append("<tr>\n");
            }
            String cut = value.toString();
            if (cut.length() > columnWidth) {
                cut = cut.substring(0, columnWidth - 3) + "...";
            }
            builder.append("<th align=\"");
            builder.append(position);
            builder.append("\">");
            builder.append(cut);
            builder.append("</th>");
            builder.append("\n");
            columnIndex++;
            if (columnIndex == columnCount) {
                builder.append("</tr>\n");
                columnIndex = 0;
            }
        }
    }

    @Override
    public void appendTableTitle(Object title) {
        checkNotNull(title, "The title to append should not be null.");
        if (isDrawingTable) {
            String cut = title.toString();
            if (cut.length() > columnWidth) {
                cut = cut.substring(0, columnWidth - 3) + "...";
            }
            builder.append("<caption>");
            builder.append(cut);
            builder.append("</caption>\n");
        }
    }

    @Override
    public void appendValue(Object value) {
        checkNotNull(value, "The value to append should not be null.");
        if(isDrawingTable){
            appendTableValue(value);
        }
        else{
            builder.append("<p>").append(value).append("</p>\n");
        }
    }
}
