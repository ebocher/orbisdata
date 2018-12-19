package org.orbisgis.datamanager

import org.junit.jupiter.api.Test
import org.osgi.service.jdbc.DataSourceFactory

import java.sql.SQLException

import static org.junit.jupiter.api.Assertions.*

class GroovyTest {

    @Test
    void loadH2GIS() {
        def h2GIS = H2GIS.open([databaseName: './target/loadH2GIS'])
        assertNotNull(h2GIS)
    }

    @Test
    void queryH2GIS() {
        def h2GIS = H2GIS.open([databaseName: './target/loadH2GIS'])
        h2GIS.execute("""
                DROP TABLE IF EXISTS h2gis;
                CREATE TABLE h2gis (id int, the_geom point);
                INSERT INTO h2gis VALUES (1, 'POINT(10 10)'::GEOMETRY), (2, 'POINT(1 1)'::GEOMETRY);
        """)
        def concat = ""
        h2GIS.eachRow "SELECT THE_GEOM FROM h2gis", { row -> concat += "$row.the_geom\n" }
        assertEquals("POINT (10 10)\nPOINT (1 1)\n", concat)
    }

    @Test
    void querySpatialTable() {
        def h2GIS = H2GIS.open([databaseName: './target/loadH2GIS'])
        h2GIS.execute("""
                DROP TABLE IF EXISTS h2gis;
                CREATE TABLE h2gis (id int, the_geom point);
                INSERT INTO h2gis VALUES (1, 'POINT(10 10)'::GEOMETRY), (2, 'POINT(1 1)'::GEOMETRY);
        """)
        def concat = ""
        h2GIS.getSpatialTable "h2gis" eachRow { row -> concat += "$row.id $row.the_geom\n" }
        assertEquals("1 POINT (10 10)\n2 POINT (1 1)\n", concat)
    }

    @Test
    void queryTableNames() throws SQLException {
        def h2GIS = H2GIS.open([databaseName: './target/loadH2GIS'])
        h2GIS.execute("""
                DROP TABLE IF EXISTS table1, table2;
                CREATE TABLE table1 (id int, the_geom point);
                CREATE TABLE table2 (id int, the_geom point);
        """)

        def values = h2GIS.tableNames
        assertTrue values.contains("LOADH2GIS.PUBLIC.TABLE1")
        assertTrue values.contains("LOADH2GIS.PUBLIC.TABLE2")
    }
}