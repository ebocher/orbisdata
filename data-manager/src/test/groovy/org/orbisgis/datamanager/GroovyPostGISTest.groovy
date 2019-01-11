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
 * DataManager is distributed under GPL 3 license.
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
package org.orbisgis.datamanager

import org.junit.jupiter.api.Test
import org.orbisgis.datamanager.postgis.POSTGIS

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

class GroovyPostGISTest {

    def dbProperties=  [databaseName: 'gisdb',
    user: '',
    password: '',
    url :'jdbc:postgresql://ns380291.ip-94-23-250.eu/'
    ]

    @Test
    void loadH2GIS() {
        def postGIS = POSTGIS.open(dbProperties)
        assertNotNull(postGIS)
    }

    @Test
    void queryH2GIS() {
        def postGIS = POSTGIS.open(dbProperties)
        postGIS.execute("""
                DROP TABLE IF EXISTS postgis;
                CREATE TABLE postgis (id int, the_geom geometry(point, 0));
                INSERT INTO postgis VALUES (1, 'POINT(10 10)'::GEOMETRY), (2, 'POINT(1 1)'::GEOMETRY);
        """)
        def concat = ""
        postGIS.eachRow "SELECT THE_GEOM FROM postgis", { row -> concat += "$row.the_geom\n" }
        assertEquals("POINT (10 10)\nPOINT (1 1)\n", concat)
    }

    @Test
    void querySpatialTable() {
        def postGIS = POSTGIS.open(dbProperties)
        postGIS.execute("""
                DROP TABLE IF EXISTS postgis;
                CREATE TABLE postgis (id int, the_geom geometry(point, 0));
                INSERT INTO postgis VALUES (1, 'POINT(10 10)'::GEOMETRY), (2, 'POINT(1 1)'::GEOMETRY);
        """)
        def concat = ""
        postGIS.getSpatialTable "postgis" eachRow { row -> concat += "$row.id $row.the_geom $row.geometry\n" }
        assertEquals("1 POINT (10 10) POINT (10 10)\n2 POINT (1 1) POINT (1 1)\n", concat)
        println(concat)
    }



    @Test
    void queryH2GISMetaData() {
        def postGIS = POSTGIS.open(dbProperties)
        postGIS.execute("""
                DROP TABLE IF EXISTS postgis;
                CREATE TABLE postgis (id int, the_geom geometry(point, 0));
                INSERT INTO postgis VALUES (1, 'POINT(10 10)'::GEOMETRY), (2, 'POINT(1 1)'::GEOMETRY);
        """)
        def concat = ""
        postGIS.rows "SELECT * FROM postgis", {  meta ->
            concat += "${meta.getTableName(1)} $meta.columnCount\n"
        }
        assertEquals("postgis 2\n", concat)
    }

    @Test
    void querySpatialTableMetaData() {
        def postGIS = POSTGIS.open(dbProperties)
        postGIS.execute("""
                DROP TABLE IF EXISTS postgis;
                CREATE TABLE postgis (id int, the_geom geometry(point, 0));
                INSERT INTO postgis VALUES (1, 'POINT(10 10)'::GEOMETRY), (2, 'POINT(1 1)'::GEOMETRY);
        """)

        def concat = ""
        postGIS.getSpatialTable("postgis").meta.each {row ->
            concat += "$row.columnLabel $row.columnType\n"
        }
        assertEquals("id 4\nthe_geom 1111\n", concat)

        concat = ""
        postGIS.getSpatialTable("postgis").metadata.each {row ->
            concat += "$row.columnLabel $row.columnType\n"
        }
        assertEquals("id 4\nthe_geom 1111\n", concat)
    }
    
    
    @Test
    void exportImportShpFile() {
        def postGIS = POSTGIS.open(dbProperties)
        postGIS.execute("""
                DROP TABLE IF EXISTS postgis;
                CREATE TABLE postgis (id int, the_geom geometry(point, 0));
                INSERT INTO postgis VALUES (1, 'POINT(10 10)'::GEOMETRY), (2, 'POINT(1 1)'::GEOMETRY);
        """)
        postGIS.save("postgis","target/postgis_imported.shp");
        postGIS.load("target/postgis_imported.shp", "postgis_imported", null, false);
        def concat = ""
        postGIS.getSpatialTable "postgis_imported" eachRow { row -> concat += "$row.id $row.the_geom $row.geometry\n" }
        assertEquals("1 POINT (10 10) POINT (10 10)\n2 POINT (1 1) POINT (1 1)\n", concat)
        println(concat)
    }

    @Test
    void exportImportTwoTimesShpFile() {
        def postGIS = POSTGIS.open(dbProperties)
        postGIS.execute("""
                DROP TABLE IF EXISTS postgis;
                CREATE TABLE postgis (id int, the_geom geometry(point, 0));
                INSERT INTO postgis VALUES (1, 'POINT(10 10)'::GEOMETRY), (2, 'POINT(1 1)'::GEOMETRY);
        """)
        postGIS.save("postgis","target/postgis_imported.shp");
        postGIS.load("target/postgis_imported.shp", "postgis_imported", null, false);
        postGIS.load("target/postgis_imported.shp", "postgis_imported", null, true);
        def concat = ""
        postGIS.getSpatialTable "postgis_imported" eachRow { row -> concat += "$row.id $row.the_geom $row.geometry\n" }
        assertEquals("1 POINT (10 10) POINT (10 10)\n2 POINT (1 1) POINT (1 1)\n", concat)
        println(concat)
    }

    @Test
    void exportImportShpFileSimple1() {
        def postGIS = POSTGIS.open(dbProperties)
        postGIS.execute("""
                DROP TABLE IF EXISTS postgis;
                CREATE TABLE postgis (id int, the_geom geometry(point, 0));
                INSERT INTO postgis VALUES (1, 'POINT(10 10)'::GEOMETRY), (2, 'POINT(1 1)'::GEOMETRY);
        """)
        postGIS.save("postgis","target/postgis_imported.shp");
        postGIS.load("target/postgis_imported.shp", "postgis_imported");
        def concat = ""
        postGIS.getSpatialTable "postgis_imported" eachRow { row -> concat += "$row.id $row.the_geom $row.geometry\n" }
        assertEquals("1 POINT (10 10) POINT (10 10)\n2 POINT (1 1) POINT (1 1)\n", concat)
        println(concat)
    }

    @Test
    void exportImportShpFileSimple2() {
        def postGIS = POSTGIS.open(dbProperties)
        postGIS.execute("""
                DROP TABLE IF EXISTS postgis;
                CREATE TABLE postgis (id int, the_geom geometry(point, 0));
                INSERT INTO postgis VALUES (1, 'POINT(10 10)'::GEOMETRY), (2, 'POINT(1 1)'::GEOMETRY);
        """)
        postGIS.save("postgis","target/postgis_imported.shp");
        postGIS.load("target/postgis_imported.shp");
        def concat = ""
        postGIS.getSpatialTable "postgis_imported" eachRow { row -> concat += "$row.id $row.the_geom $row.geometry\n" }
        assertEquals("1 POINT (10 10) POINT (10 10)\n2 POINT (1 1) POINT (1 1)\n", concat)
        println(concat)
    }
    @Test
    void exportImportGeoJsonShapeFile() {
        def postGIS = POSTGIS.open(dbProperties)
        postGIS.execute("""
                DROP TABLE IF EXISTS postgis;
                CREATE TABLE postgis (id int, the_geom geometry(point, 0));
                INSERT INTO postgis VALUES (1, 'POINT(10 10)'::GEOMETRY), (2, 'POINT(1 1)'::GEOMETRY);
        """)
        postGIS.save("postgis","target/postgis_imported.geojson");
        postGIS.load("target/postgis_imported.geojson");
        postGIS.save("postgis_imported","target/postgis_imported.shp");
        postGIS.load("target/postgis_imported.shp", true);
        def concat = ""
        postGIS.getSpatialTable "postgis_imported" eachRow { row -> concat += "$row.id $row.the_geom $row.geometry\n" }
        assertEquals("1 POINT (10 10) POINT (10 10)\n2 POINT (1 1) POINT (1 1)\n", concat)
        println(concat)
    }

    @Test
    void exportImportCSV() {
        def postGIS = POSTGIS.open(dbProperties)
        postGIS.execute("""
                DROP TABLE IF EXISTS postgis;
                CREATE TABLE postgis (id int, the_geom geometry(point, 0));
                INSERT INTO postgis VALUES (1, 'POINT(10 10)'::GEOMETRY), (2, 'POINT(1 1)'::GEOMETRY);
        """)
        postGIS.save("postgis","target/postgis_imported.csv");
        postGIS.load("target/postgis_imported.csv");
        def concat = ""
        postGIS.getSpatialTable "postgis_imported" eachRow { row -> concat += "$row.id $row.the_geom $row.geometry\n" }
        assertEquals("1 POINT (10 10) POINT (10 10)\n2 POINT (1 1) POINT (1 1)\n", concat)
        println(concat)
    }
}
