/*
 * Bundle DataManager API is part of the OrbisGIS platform
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
 * DataManager API is distributed under LGPL 3 license.
 *
 * Copyright (C) 2019-2020 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * DataManager API is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * DataManager API is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * DataManager API. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.data.api.dataset;

import groovy.lang.Closure;
import groovy.lang.GString;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.orbisgis.data.api.datasource.IJdbcDataSource;
import org.orbisgis.data.api.dsl.IBuilderResult;
import org.orbisgis.data.api.dsl.IFilterBuilder;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class dedicated to the test of the ITable default methods.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (Lab-STICC UBS 2019)
 */
public class ITableTest {

    /**
     * {@link ITable} to test.
     */
    private static ITable table;
    /**
     * Data written in the {@link ITable}.
     */
    private static final String COL1_NAME = "Buildings";
    private static final List<Object> COL1_VALUES = Arrays.asList("build1", "build2", "build3");
    private static final String COL2_NAME = "Names";
    private static final List<Object> COL2_VALUES = Arrays.asList("Toto", "Tata", "Titi", "Tutu");
    private static final String COL3_NAME = "Data";
    private static final List<Object> COL3_VALUES = new ArrayList<>();
    private static final String COL4_NAME = "Null";
    private static final List<Object> COL4_VALUES = Arrays.asList(new String[]{null});
    private static final String COL5_NAME = "Numbers";
    private static final List<Object> COL5_VALUES = Arrays.asList(1, 25, 485, 1223333);

    private static final int COLUMN_COUNT = 5;

    /**
     * Initialize the {@link ITable} to test.
     */
    @BeforeAll
    public static void createTable() {
        DummyTable dummyTable = new DummyTable();
        dummyTable.addColumn(COL1_NAME, COL1_VALUES);
        dummyTable.addColumn(COL2_NAME, COL2_VALUES);
        dummyTable.addColumn(COL3_NAME, COL3_VALUES);
        dummyTable.addColumn(COL4_NAME, COL4_VALUES);
        dummyTable.addColumn(COL5_NAME, COL5_VALUES);
        table = dummyTable;
    }

    /**
     * Test the {@link ITable#getColumnCount()} method.
     */
    @Test
    public void testGetColumnCount() throws Exception {
        assertEquals(COLUMN_COUNT, table.getColumnCount());
        assertEquals(0, new DummyTable().getColumnCount());
    }

    /**
     * Test the {@link ITable#isEmpty()} method.
     */
    @Test
    public void testIsEmpty() throws Exception {
        assertFalse(table.isEmpty());
        assertTrue(new DummyTable().isEmpty());
    }

    /**
     * Test the {@link ITable#save(String)} ()} method.
     */
    @Test
    public void testSave() throws Exception {
        assertNull(table.save("path"));
    }

    /**
     * Test the {@link ITable#getNDim()} method.
     */
    @Test
    public void testNDim() throws Exception {
        assertEquals(2, table.getNDim());
    }

    /**
     * Test the {@link ITable#getSize()} method.
     */
    @Test
    public void testGetShape() throws Exception {
        int[] shape = table.getSize();
        assertEquals(2, shape.length);
        assertEquals(5, shape[0]);
        assertEquals(4, shape[1]);
    }

    /**
     * Test the {@link ITable#eachRow(Closure)} method.
     */
    @Test
    public void testEachRow() throws Exception {
        final String[] result = {""};
        Closure cl = new Closure(this) {
            @Override
            public Object call(Object argument) {
                result[0] += argument;
                return argument;
            }
        };
        table.eachRow(cl);
        assertEquals("12345678910", result[0]);
    }

    /**
     * Simple implementation of {@link ITable} for test purpose.
     */
    private static class DummyTable implements ITable<ResultSet, ResultSet> {

        /**
         * {@link List} of columns. A column is a list with the column name as first value.
         */
        private final List<List<Object>> columns;

        /**
         * Main constructor with an empty column list.
         */
        private DummyTable() {
            columns = new ArrayList<>();
        }

        @Override
        public boolean isEmpty() {
            return columns.isEmpty();
        }

        /**
         * Add a single column.
         *
         * @param columnName Name of the column.
         * @param values     Values of the column.
         */
        private void addColumn(String columnName, List<Object> values) {
            List<Object> list = new ArrayList<>();
            list.add(columnName);
            list.addAll(values);
            columns.add(list);
        }


        @Override
        public Collection<String> getColumns() {
            return columns.stream().map(column -> column.get(0).toString()).collect(Collectors.toList());
        }


        @Override
        public int getRowCount() {
            return columns
                    .stream()
                    .mapToInt(List::size)
                    .max()
                    .orElse(1) - 1;
        }

        @Override
        public int getRow() {
            return 0;
        }

        @Override
        public boolean next() {
            return false;
        }

        @Override
        public boolean previous() throws Exception {
            return false;
        }

        @Override
        public boolean first() throws Exception {
            return false;
        }

        @Override
        public boolean last() throws Exception {
            return false;
        }

        @Override
        public boolean isFirst() throws Exception {
            return false;
        }

        @Override
        public boolean isLast() throws Exception {
            return false;
        }

        @Override
        public Collection<String> getUniqueValues(String column) {
            return null;
        }

        @Override
        public String save(String filePath, boolean delete) {
            return null;
        }

        @Override
        public String save(String filePath, String encoding) {
            return null;
        }

        @Override
        public String save(IJdbcDataSource dataSource, int batchSize) {
            return null;
        }

        @Override
        public String save(IJdbcDataSource dataSource, boolean deleteTable) {
            return null;
        }

        @Override
        public String save(IJdbcDataSource dataSource, boolean deleteTable, int batchSize) {
            return null;
        }

        @Override
        public String save(IJdbcDataSource dataSource, String outputTableName, boolean deleteTable) {
            return null;
        }

        @Override
        public String save(IJdbcDataSource dataSource, String outputTableName, boolean deleteTable, int batchSize) {
            return null;
        }

        @Override
        public List<Object> getFirstRow() {
            return null;
        }

        @Override
        public boolean isSpatial() {
            return false;
        }

        @Override
        public String getString(int column) {
            return null;
        }

        @Override
        public boolean getBoolean(int column) {
            return false;
        }

        @Override
        public byte getByte(int column) {
            return 0;
        }

        @Override
        public short getShort(int column) {
            return 0;
        }

        @Override
        public int getInt(int column) {
            return 0;
        }

        @Override
        public long getLong(int column) {
            return 0;
        }

        @Override
        public float getFloat(int column) {
            return 0;
        }

        @Override
        public double getDouble(int column) {
            return 0;
        }

        @Override
        public byte[] getBytes(int column) {
            return new byte[0];
        }

        @Override
        public Date getDate(int column) {
            return null;
        }

        @Override
        public Time getTime(int column) {
            return null;
        }

        @Override
        public Timestamp getTimestamp(int column) {
            return null;
        }

        @Override
        public Object getObject(int column) {
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(int column) {
            return null;
        }

        @Override
        public String getString(String column) {
            return null;
        }

        @Override
        public boolean getBoolean(String column) {
            return false;
        }

        @Override
        public byte getByte(String column) {
            return 0;
        }

        @Override
        public short getShort(String column) {
            return 0;
        }

        @Override
        public int getInt(String column) {
            return 0;
        }

        @Override
        public long getLong(String column) {
            return 0;
        }

        @Override
        public float getFloat(String column) {
            return 0;
        }

        @Override
        public double getDouble(String column) {
            return 0;
        }

        @Override
        public byte[] getBytes(String column) {
            return new byte[0];
        }

        @Override
        public Date getDate(String column) {
            return null;
        }

        @Override
        public Time getTime(String column) {
            return null;
        }

        @Override
        public Timestamp getTimestamp(String column) {
            return null;
        }

        @Override
        public Object getObject(String column) {
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(String column) {
            return null;
        }

        @Override
        public <U> U getObject(int column, Class<U> clazz) throws Exception {
            return null;
        }

        @Override
        public <U> U getObject(String column, Class<U> clazz) throws Exception {
            return null;
        }

        @Override
        public Stream<ResultSet> stream() {
            return null;
        }


        @Override
        public Map<String, Object> firstRow() {
            return null;
        }

        @Override
        public Object get(String column) throws Exception {
            return null;
        }

        @Override
        public Object get(int column) throws Exception {
            return null;
        }

        @Override
        public String getLocation() {
            return null;
        }


        @Override
        public String getName() {
            return null;
        }


        @Override
        public Object getMetaData() {
            return null;
        }

        @Override
        public Object asType(Class clazz) {
            return null;
        }


        @Override
        public ISummary getSummary() {
            return null;
        }

        @Override
        public boolean reload() {
            return false;
        }

        @Override
        public Iterator<ResultSet> iterator() {
            return new DummyIterator();
        }


        @Override
        public Map<String, String> getColumnsTypes() {
            return null;
        }

        @Override
        public String getColumnType(String columnName) {
            return null;
        }

       
        @Override
        public ITable<?,?> getTable() {
            return null;
        }

       
        @Override
        public ISpatialTable<?> getSpatialTable() {
            return null;
        }

        @Override
        public List<Object> getParams() {
            return null;
        }

        @Override
        public IFilterBuilder filter(String filter) {
            return null;
        }

        @Override
        public IBuilderResult filter(GString filter) {
            return null;
        }

        @Override
        public IBuilderResult filter(String filter, List<Object> params) {
            return null;
        }

        @Override
        public IFilterBuilder columns(String... columns) {
            return null;
        }
    }

    private static class DummyIterator implements Iterator {

        private int index = 0;
        private final int count = 10;

        @Override
        public boolean hasNext() {
            return index < count;
        }

        @Override
        public Object next() {
            return ++index;
        }
    }
}
