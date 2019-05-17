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
 * DataManager API  is distributed under GPL 3 license.
 *
 * Copyright (C) 2019 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * DataManager API  is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * DataManager API  is distributed in the hope that it will be useful, but WITHOUT ANY
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
package org.orbisgis.datamanagerapi.dataset;

import groovy.lang.Closure;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.h2gis.utilities.TableLocation;
import org.junit.jupiter.api.Test;
import org.orbisgis.datamanagerapi.dsl.IConditionOrOptionBuilder;
import org.orbisgis.datamanagerapi.dsl.IOptionBuilder;

import javax.sql.rowset.RowSetMetaDataImpl;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class dedicated to {@link IJdbcTable} interface.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class IJdbcTableTest {

    private static final String LOCATION = "caTAlog.schEma.TAbLe";

    /**
     * Test the {@link IJdbcTable#getLocation()} method.
     */
    @Test
    public void testGetLocation(){
        assertEquals("catalog.schema.\"table\"", new DummyJdbcTable(DataBaseType.POSTGIS, LOCATION, true).getLocation());
        assertEquals(LOCATION.toUpperCase(), new DummyJdbcTable(DataBaseType.H2GIS, LOCATION, true).getLocation());
    }

    /**
     * Test the {@link IJdbcTable#getName()} method.
     */
    @Test
    public void testGetName(){
        assertEquals(LOCATION.toLowerCase().substring(LOCATION.lastIndexOf(".")+1),
                new DummyJdbcTable(DataBaseType.POSTGIS, LOCATION, true).getName());
        assertEquals(LOCATION.toUpperCase().substring(LOCATION.lastIndexOf(".")+1),
                new DummyJdbcTable(DataBaseType.H2GIS, LOCATION, true).getName());
    }

    /**
     * Test the {@link IJdbcTable#invokeMethod(String, Object)} method.
     */
    @Test
    public void testInvokeMethod(){
        IJdbcTable table = new DummyJdbcTable(DataBaseType.H2GIS, LOCATION, true);
        assertEquals(table.getLocation(), table.invokeMethod("getLocation", null));
        assertEquals(table.getLocation(), table.invokeMethod("location", null));
        assertArrayEquals(new Object[]{"string", 0.2}, (Object[])table.invokeMethod("getArrayMethod", new Object[]{"string", 0.2}));
        assertArrayEquals(new Object[]{"string", 0.2}, (Object[])table.invokeMethod("arrayMethod", new Object[]{"string", 0.2}));
        assertArrayEquals(new Object[]{"string", 0.2}, (Object[])table.invokeMethod("getParametersMethod", new Object[]{"string", 0.2}));
        assertArrayEquals(new Object[]{"string", 0.2}, (Object[])table.invokeMethod("parametersMethod", new Object[]{"string", 0.2}));
        assertArrayEquals(new Object[]{"string", "0.2"}, (Object[])table.invokeMethod("getParametersMethod", new Object[]{"string", "0.2"}));
        assertArrayEquals(new Object[]{"string", "0.2"}, (Object[])table.invokeMethod("parametersMethod", new Object[]{"string", "0.2"}));
        assertEquals("string", table.invokeMethod("getParameterMethod", new Object[]{"string"}));
        assertEquals("string", table.invokeMethod("getParameterMethod", "string"));
        assertEquals("string", table.invokeMethod("parameterMethod", new Object[]{"string"}));
        assertEquals("string", table.invokeMethod("parameterMethod", "string"));
        assertEquals(RowSetMetaDataImpl.class, table.invokeMethod("metadata", null).getClass());

        assertThrows(MissingMethodException.class, () -> table.invokeMethod("getLocation", new String[]{"tata", "toto"}));
        assertThrows(MissingMethodException.class, () -> table.invokeMethod("location", new String[]{"tata", "toto"}));
        assertNull(table.invokeMethod("getPrivateMethod", null));
        assertNull(table.invokeMethod("privateMethod", null));
    }

    /**
     * Test the {@link IJdbcTable#getProperty(String)} method.
     */
    @Test
    public void testGetProperty(){
        IJdbcTable table = new DummyJdbcTable(DataBaseType.H2GIS, LOCATION, true);
        assertThrows(MissingPropertyException.class, () -> table.getProperty("getLocation"));
        assertEquals(table.getLocation(), table.getProperty("location"));
        assertEquals(RowSetMetaDataImpl.class, table.getProperty("meta").getClass());
        assertArrayEquals(new Object[]{"string", 0.2}, (Object[])table.getProperty("data"));
        assertEquals("tutu", table.getProperty("privateData"));
        assertNull(table.getProperty(null));
    }

    /**
     * Test the {@link IJdbcTable#setProperty(String, Object)} method.
     */
    @Test
    public void testSetProperty(){
        IJdbcTable table = new DummyJdbcTable(DataBaseType.H2GIS, LOCATION, true);
        assertThrows(MissingPropertyException.class, () -> table.setProperty("getLocation", "tata"));
        table.setProperty("privateData", "toto");
        assertEquals("toto", table.getProperty("privateData"));
    }

    /**
     * Test the {@link IJdbcTable#iterator()} method.
     */
    @Test
    public void testIterator() throws SQLException {
        IJdbcTable table = new DummyJdbcTable(DataBaseType.H2GIS, LOCATION, true);
        ResultSetIterator it = (ResultSetIterator)table.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertEquals("string", it.next().getObject(0));
        assertTrue(it.hasNext());
        assertEquals(Double.toString(0.2), it.next().getObject(0).toString());
        assertFalse(it.hasNext());

        table = new DummyJdbcTable(DataBaseType.H2GIS, LOCATION, false);
        assertNotNull(table.iterator());
    }

    /**
     * Test the {@link IJdbcTable#eachRow(Closure)} method.
     */
    @Test
    public void testEachRow() {
        IJdbcTable table = new DummyJdbcTable(DataBaseType.H2GIS, LOCATION, true);
        final String[] result = {""};
        table.eachRow(new Closure(this) {
            @Override
            public Object call(Object argument) {
                result[0] += ((DummyJdbcTable)argument).getObject(0);
                return argument;
            }
        });
        assertEquals("string0.2", result[0]);
    }

    /**
     * Test the {@link IJdbcTable} methods with {@link SQLException} thrown.
     */
    @Test
    public void testSQLException() {
        DummyJdbcTable table = new DummyJdbcTable(DataBaseType.H2GIS, LOCATION, true);

        table.setException(true);
        Iterator it = table.iterator();
        assertFalse(it.hasNext());

        table.setException(false);
        it = table.iterator();
        table.setException(true);
        assertFalse(it.hasNext());
        assertNotNull(it.next());

        assertNotNull(table.getProperty("data"));

        assertThrows(InvokerInvocationException.class, () -> table.invokeMethod("dupMethod", null));
    }

    /**
     * Simple implementation of Exception
     */
    private class DummyException extends Exception{}

    /**
     * Simple implementation of the {@link IJdbcTable} interface.
     */
    private static class DummyJdbcTable implements IJdbcTable{

        /** Fake data location. */
        private TableLocation location;
        /** Fake database type. */
        private DataBaseType databaseType;
        /** Fake row index. */
        private int rowIndex = 0;
        /** Fake data. */
        private Object[] data = new Object[]{"string", 0.2};
        /** True if iterable, false otherwise. */
        private boolean isIterable;
        /** True if throws exception, false otherwise. */
        private boolean sqlException = false;
        /** Private data. */
        private Object privateData = "tutu";

        /**
         * Main constructor.
         *
         * @param databaseType Fake database type.
         * @param location Fake data location.
         * @param isIterable True if iterable, false otherwise.
         */
        private DummyJdbcTable(DataBaseType databaseType, String location, boolean isIterable){
            this.location = TableLocation.parse(location, databaseType.equals(DataBaseType.H2GIS));
            this.databaseType = databaseType;
            this.isIterable = isIterable;
        }

        /**
         * True if throws exception, false otherwise.
         * @param sqlException True if throws exception, false otherwise.
         */
        private void setException(boolean sqlException){
            this.sqlException = sqlException;
        }

        private void getPrivateMethod(){/*Does nothing*/}
        public Object[] getArrayMethod(Object[] array){return array;}
        public Object[] getParametersMethod(String param1, Double param2){return new Object[]{param1, param2};}
        public Object[] getParametersMethod(Object param1, Object param2){return new Object[]{param1, param2};}
        public String getParameterMethod(String param1){return param1;}
        public void dupMethod() throws IllegalAccessException {throw new IllegalAccessException();}

        @Override public TableLocation getTableLocation() {return location;}
        @Override public DataBaseType getDbType() {return databaseType;}
        @Override public ResultSetMetaData getMetadata() {return new RowSetMetaDataImpl();}
        @Override public boolean isSpatial() {return false;}
        @Override public boolean isLinked() {return false;}
        @Override public boolean isTemporary() {return false;}
        @Override public MetaClass getMetaClass() {return InvokerHelper.getMetaClass(DummyJdbcTable.class);}
        @Override public void setMetaClass(MetaClass metaClass) {/*Does nothing*/}
        @Override public boolean next() throws SQLException {
            if(!sqlException) {
                return rowIndex++ < data.length;
            }
            else{
                throw new SQLException();
            }
        }
        @Override public void close() throws SQLException {/*Does nothing*/}
        @Override public boolean wasNull() throws SQLException {return false;}
        @Override public String getString(int i) throws SQLException {return null;}
        @Override public boolean getBoolean(int i) throws SQLException {return false;}
        @Override public byte getByte(int i) throws SQLException {return 0;}
        @Override public short getShort(int i) throws SQLException {return 0;}
        @Override public int getInt(int i) throws SQLException {return 0;}
        @Override public long getLong(int i) throws SQLException {return 0;}
        @Override public float getFloat(int i) throws SQLException {return 0;}
        @Override public double getDouble(int i) throws SQLException {return 0;}
        @Override public BigDecimal getBigDecimal(int i, int i1) throws SQLException {return null;}
        @Override public byte[] getBytes(int i) throws SQLException {return new byte[0];}
        @Override public Date getDate(int i) throws SQLException {return null;}
        @Override public Time getTime(int i) throws SQLException {return null;}
        @Override public Timestamp getTimestamp(int i) throws SQLException {return null;}
        @Override public InputStream getAsciiStream(int i) throws SQLException {return null;}
        @Override public InputStream getUnicodeStream(int i) throws SQLException {return null;}
        @Override public InputStream getBinaryStream(int i) throws SQLException {return null;}
        @Override public String getString(String s) throws SQLException {return null;}
        @Override public boolean getBoolean(String s) throws SQLException {return false;}
        @Override public byte getByte(String s) throws SQLException {return 0;}
        @Override public short getShort(String s) throws SQLException {return 0;}
        @Override public int getInt(String s) throws SQLException {return 0;}
        @Override public long getLong(String s) throws SQLException {return 0;}
        @Override public float getFloat(String s) throws SQLException {return 0;}
        @Override public double getDouble(String s) throws SQLException {return 0;}
        @Override public BigDecimal getBigDecimal(String s, int i) throws SQLException {return null;}
        @Override public byte[] getBytes(String s) throws SQLException {return new byte[0];}
        @Override public Date getDate(String s) throws SQLException {return null;}
        @Override public Time getTime(String s) throws SQLException {return null;}
        @Override public Timestamp getTimestamp(String s) throws SQLException {return null;}
        @Override public InputStream getAsciiStream(String s) throws SQLException {return null;}
        @Override public InputStream getUnicodeStream(String s) throws SQLException {return null;}
        @Override public InputStream getBinaryStream(String s) throws SQLException {return null;}
        @Override public SQLWarning getWarnings() throws SQLException {return null;}
        @Override public void clearWarnings() throws SQLException {/*Does nothing*/}
        @Override public String getCursorName() throws SQLException {return null;}
        @Override public ResultSetMetaData getMetaData() throws SQLException {return null;}
        @Override public Object getObject(int i) {return data[rowIndex-1];}
        @Override public Object getObject(String s) throws SQLException {
            if(sqlException){
                throw new SQLException();
            }
            if("data".equals(s)){
                return data;
            }
            return null;
        }
        @Override public int findColumn(String s) throws SQLException {return 0;}
        @Override public Reader getCharacterStream(int i) throws SQLException {return null;}
        @Override public Reader getCharacterStream(String s) throws SQLException {return null;}
        @Override public BigDecimal getBigDecimal(int i) throws SQLException {return null;}
        @Override public BigDecimal getBigDecimal(String s) throws SQLException {return null;}
        @Override public boolean isBeforeFirst() throws SQLException {return false;}
        @Override public boolean isAfterLast() throws SQLException {return false;}
        @Override public boolean isFirst() throws SQLException {return false;}
        @Override public boolean isLast() throws SQLException {return false;}
        @Override public void beforeFirst() throws SQLException {rowIndex = 0;}
        @Override public void afterLast() throws SQLException {/*Does nothing*/}
        @Override public boolean first() throws SQLException {return false;}
        @Override public boolean last() throws SQLException {
            if(!isIterable){
                throw new SQLException();
            }
            rowIndex = data.length;
            return true;
        }
        @Override public int getRow() throws SQLException {
            if(!sqlException) {
                return rowIndex;
            }
            else{
                throw new SQLException();
            }
        }
        @Override public boolean absolute(int i) throws SQLException {return false;}
        @Override public boolean relative(int i) throws SQLException {return false;}
        @Override public boolean previous() throws SQLException {return false;}
        @Override public void setFetchDirection(int i) throws SQLException {/*Does nothing*/}
        @Override public int getFetchDirection() throws SQLException {return 0;}
        @Override public void setFetchSize(int i) throws SQLException {/*Does nothing*/}
        @Override public int getFetchSize() throws SQLException {return 0;}
        @Override public int getType() throws SQLException {return 0;}
        @Override public int getConcurrency() throws SQLException {return 0;}
        @Override public boolean rowUpdated() throws SQLException {return false;}
        @Override public boolean rowInserted() throws SQLException {return false;}
        @Override public boolean rowDeleted() throws SQLException {return false;}
        @Override public void updateNull(int i) throws SQLException {/*Does nothing*/}
        @Override public void updateBoolean(int i, boolean b) throws SQLException {/*Does nothing*/}
        @Override public void updateByte(int i, byte b) throws SQLException {/*Does nothing*/}
        @Override public void updateShort(int i, short i1) throws SQLException {/*Does nothing*/}
        @Override public void updateInt(int i, int i1) throws SQLException {/*Does nothing*/}
        @Override public void updateLong(int i, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateFloat(int i, float v) throws SQLException {/*Does nothing*/}
        @Override public void updateDouble(int i, double v) throws SQLException {/*Does nothing*/}
        @Override public void updateBigDecimal(int i, BigDecimal bigDecimal) throws SQLException {/*Does nothing*/}
        @Override public void updateString(int i, String s) throws SQLException {/*Does nothing*/}
        @Override public void updateBytes(int i, byte[] bytes) throws SQLException {/*Does nothing*/}
        @Override public void updateDate(int i, Date date) throws SQLException {/*Does nothing*/}
        @Override public void updateTime(int i, Time time) throws SQLException {/*Does nothing*/}
        @Override public void updateTimestamp(int i, Timestamp timestamp) throws SQLException {/*Does nothing*/}
        @Override public void updateAsciiStream(int i, InputStream inputStream, int i1) throws SQLException {/*Does nothing*/}
        @Override public void updateBinaryStream(int i, InputStream inputStream, int i1) throws SQLException {/*Does nothing*/}
        @Override public void updateCharacterStream(int i, Reader reader, int i1) throws SQLException {/*Does nothing*/}
        @Override public void updateObject(int i, Object o, int i1) throws SQLException {/*Does nothing*/}
        @Override public void updateObject(int i, Object o) throws SQLException {/*Does nothing*/}
        @Override public void updateNull(String s) throws SQLException {/*Does nothing*/}
        @Override public void updateBoolean(String s, boolean b) throws SQLException {/*Does nothing*/}
        @Override public void updateByte(String s, byte b) throws SQLException {/*Does nothing*/}
        @Override public void updateShort(String s, short i) throws SQLException {/*Does nothing*/}
        @Override public void updateInt(String s, int i) throws SQLException {/*Does nothing*/}
        @Override public void updateLong(String s, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateFloat(String s, float v) throws SQLException {/*Does nothing*/}
        @Override public void updateDouble(String s, double v) throws SQLException {/*Does nothing*/}
        @Override public void updateBigDecimal(String s, BigDecimal bigDecimal) throws SQLException {/*Does nothing*/}
        @Override public void updateString(String s, String s1) throws SQLException {/*Does nothing*/}
        @Override public void updateBytes(String s, byte[] bytes) throws SQLException {/*Does nothing*/}
        @Override public void updateDate(String s, Date date) throws SQLException {/*Does nothing*/}
        @Override public void updateTime(String s, Time time) throws SQLException {/*Does nothing*/}
        @Override public void updateTimestamp(String s, Timestamp timestamp) throws SQLException {/*Does nothing*/}
        @Override public void updateAsciiStream(String s, InputStream inputStream, int i) throws SQLException {/*Does nothing*/}
        @Override public void updateBinaryStream(String s, InputStream inputStream, int i) throws SQLException {/*Does nothing*/}
        @Override public void updateCharacterStream(String s, Reader reader, int i) throws SQLException {/*Does nothing*/}
        @Override public void updateObject(String s, Object o, int i) throws SQLException {/*Does nothing*/}
        @Override public void updateObject(String s, Object o) throws SQLException {/*Does nothing*/}
        @Override public void insertRow() throws SQLException {/*Does nothing*/}
        @Override public void updateRow() throws SQLException {/*Does nothing*/}
        @Override public void deleteRow() throws SQLException {/*Does nothing*/}
        @Override public void refreshRow() throws SQLException {/*Does nothing*/}
        @Override public void cancelRowUpdates() throws SQLException {/*Does nothing*/}
        @Override public void moveToInsertRow() throws SQLException {/*Does nothing*/}
        @Override public void moveToCurrentRow() throws SQLException {/*Does nothing*/}
        @Override public Statement getStatement() throws SQLException {return null;}
        @Override public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {return null;}
        @Override public Ref getRef(int i) throws SQLException {return null;}
        @Override public Blob getBlob(int i) throws SQLException {return null;}
        @Override public Clob getClob(int i) throws SQLException {return null;}
        @Override public Array getArray(int i) throws SQLException {return null;}
        @Override public Object getObject(String s, Map<String, Class<?>> map) throws SQLException {return null;}
        @Override public Ref getRef(String s) throws SQLException {return null;}
        @Override public Blob getBlob(String s) throws SQLException {return null;}
        @Override public Clob getClob(String s) throws SQLException {return null;}
        @Override public Array getArray(String s) throws SQLException {return null;}
        @Override public Date getDate(int i, Calendar calendar) throws SQLException {return null;}
        @Override public Date getDate(String s, Calendar calendar) throws SQLException {return null;}
        @Override public Time getTime(int i, Calendar calendar) throws SQLException {return null;}
        @Override public Time getTime(String s, Calendar calendar) throws SQLException {return null;}
        @Override public Timestamp getTimestamp(int i, Calendar calendar) throws SQLException {return null;}
        @Override public Timestamp getTimestamp(String s, Calendar calendar) throws SQLException {return null;}
        @Override public URL getURL(int i) throws SQLException {return null;}
        @Override public URL getURL(String s) throws SQLException {return null;}
        @Override public void updateRef(int i, Ref ref) throws SQLException {/*Does nothing*/}
        @Override public void updateRef(String s, Ref ref) throws SQLException {/*Does nothing*/}
        @Override public void updateBlob(int i, Blob blob) throws SQLException {/*Does nothing*/}
        @Override public void updateBlob(String s, Blob blob) throws SQLException {/*Does nothing*/}
        @Override public void updateClob(int i, Clob clob) throws SQLException {/*Does nothing*/}
        @Override public void updateClob(String s, Clob clob) throws SQLException {/*Does nothing*/}
        @Override public void updateArray(int i, Array array) throws SQLException {/*Does nothing*/}
        @Override public void updateArray(String s, Array array) throws SQLException {/*Does nothing*/}
        @Override public RowId getRowId(int i) throws SQLException {return null;}
        @Override public RowId getRowId(String s) throws SQLException {return null;}
        @Override public void updateRowId(int i, RowId rowId) throws SQLException {/*Does nothing*/}
        @Override public void updateRowId(String s, RowId rowId) throws SQLException {/*Does nothing*/}
        @Override public int getHoldability() throws SQLException {return 0;}
        @Override public boolean isClosed() throws SQLException {return false;}
        @Override public void updateNString(int i, String s) throws SQLException {/*Does nothing*/}
        @Override public void updateNString(String s, String s1) throws SQLException {/*Does nothing*/}
        @Override public void updateNClob(int i, NClob nClob) throws SQLException {/*Does nothing*/}
        @Override public void updateNClob(String s, NClob nClob) throws SQLException {/*Does nothing*/}
        @Override public NClob getNClob(int i) throws SQLException {return null;}
        @Override public NClob getNClob(String s) throws SQLException {return null;}
        @Override public SQLXML getSQLXML(int i) throws SQLException {return null;}
        @Override public SQLXML getSQLXML(String s) throws SQLException {return null;}
        @Override public void updateSQLXML(int i, SQLXML sqlxml) throws SQLException {/*Does nothing*/}
        @Override public void updateSQLXML(String s, SQLXML sqlxml) throws SQLException {/*Does nothing*/}
        @Override public String getNString(int i) throws SQLException {return null;}
        @Override public String getNString(String s) throws SQLException {return null;}
        @Override public Reader getNCharacterStream(int i) throws SQLException {return null;}
        @Override public Reader getNCharacterStream(String s) throws SQLException {return null;}
        @Override public void updateNCharacterStream(int i, Reader reader, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateNCharacterStream(String s, Reader reader, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateAsciiStream(int i, InputStream inputStream, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateBinaryStream(int i, InputStream inputStream, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateCharacterStream(int i, Reader reader, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateAsciiStream(String s, InputStream inputStream, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateBinaryStream(String s, InputStream inputStream, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateCharacterStream(String s, Reader reader, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateBlob(int i, InputStream inputStream, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateBlob(String s, InputStream inputStream, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateClob(int i, Reader reader, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateClob(String s, Reader reader, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateNClob(int i, Reader reader, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateNClob(String s, Reader reader, long l) throws SQLException {/*Does nothing*/}
        @Override public void updateNCharacterStream(int i, Reader reader) throws SQLException {/*Does nothing*/}
        @Override public void updateNCharacterStream(String s, Reader reader) throws SQLException {/*Does nothing*/}
        @Override public void updateAsciiStream(int i, InputStream inputStream) throws SQLException {/*Does nothing*/}
        @Override public void updateBinaryStream(int i, InputStream inputStream) throws SQLException {/*Does nothing*/}
        @Override public void updateCharacterStream(int i, Reader reader) throws SQLException {/*Does nothing*/}
        @Override public void updateAsciiStream(String s, InputStream inputStream) throws SQLException {/*Does nothing*/}
        @Override public void updateBinaryStream(String s, InputStream inputStream) throws SQLException {/*Does nothing*/}
        @Override public void updateCharacterStream(String s, Reader reader) throws SQLException {/*Does nothing*/}
        @Override public void updateBlob(int i, InputStream inputStream) throws SQLException {/*Does nothing*/}
        @Override public void updateBlob(String s, InputStream inputStream) throws SQLException {/*Does nothing*/}
        @Override public void updateClob(int i, Reader reader) throws SQLException {/*Does nothing*/}
        @Override public void updateClob(String s, Reader reader) throws SQLException {/*Does nothing*/}
        @Override public void updateNClob(int i, Reader reader) throws SQLException {/*Does nothing*/}
        @Override public void updateNClob(String s, Reader reader) throws SQLException {/*Does nothing*/}
        @Override public <T> T getObject(int i, Class<T> aClass) throws SQLException {return null;}
        @Override public <T> T getObject(String s, Class<T> aClass) throws SQLException {return null;}
        @Override public <T> T unwrap(Class<T> aClass) throws SQLException {return null;}
        @Override public boolean isWrapperFor(Class<?> aClass) throws SQLException {return false;}
        @Override public Collection<String> getColumnNames() {return null;}
        @Override public boolean hasColumn(String columnName, Class clazz) {return false;}
        @Override public int getRowCount() {return 0;}
        @Override public Collection<String> getUniqueValues(String column) {return null;}
        @Override public boolean save(String filePath, String encoding) {return false;}
        @Override public IConditionOrOptionBuilder where(String condition) {return null;}
        @Override public IOptionBuilder groupBy(String... fields) {return null;}
        @Override public IOptionBuilder orderBy(Map<String, Order> orderByMap) {return null;}
        @Override public IOptionBuilder orderBy(String field, Order order) {return null;}
        @Override public IOptionBuilder orderBy(String field) {return null;}
        @Override public IOptionBuilder limit(int limitCount) {return null;}
        @Override public Object asType(Class clazz) {return null;}
        @Override public ITable getTable() {return null;}
        @Override public ISpatialTable getSpatialTable() {return null;}
    }
}