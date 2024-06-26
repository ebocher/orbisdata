/*
 * Bundle Process is part of the OrbisGIS platform
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
 * Process is distributed under LGPL 3 license.
 *
 * Copyright (C) 2018 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * Process is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Process is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Process. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.process.inoutput;

import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.jupiter.api.Test;
import org.orbisgis.process.api.IProcess;
import org.orbisgis.process.ProcessManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class dedicated to {@link InOutPut} class.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS Lab-STICC 2020)
 */
public class InOutPutTest {

    /**
     * Test the {@link InOutPut#setName(String)} and {@link InOutPut#getName()} method.
     */
    @Test
    void nameTest() {
        DummyInOutPut dummyInOutPut = new DummyInOutPut();
        assertFalse(dummyInOutPut.getName().isPresent());
        assertInstanceOf(InOutPut.class, dummyInOutPut.name("name"));
        assertTrue(dummyInOutPut.getName().isPresent());
        assertEquals("name", dummyInOutPut.getName().get());
        dummyInOutPut.setName(null);
        assertFalse(dummyInOutPut.getName().isPresent());
    }

    /**
     * Test the {@link InOutPut#setProcess(IProcess)} and {@link InOutPut#getProcess()} method.
     */
    @Test
    void processTest() {
        IProcess process = ProcessManager.createFactory().create().getProcess();
        DummyInOutPut dummyInOutPut = new DummyInOutPut();
        assertFalse(dummyInOutPut.getProcess().isPresent());
        assertInstanceOf(InOutPut.class, dummyInOutPut.process(process));
        assertTrue(dummyInOutPut.getProcess().isPresent());
        assertEquals(process, dummyInOutPut.getProcess().get());
        dummyInOutPut.setProcess(null);
        assertFalse(dummyInOutPut.getProcess().isPresent());
    }

    /**
     * Test the {@link InOutPut#setTitle(String)} and {@link InOutPut#getTitle()} method.
     */
    @Test
    void titleTest() {
        DummyInOutPut dummyInOutPut = new DummyInOutPut();
        assertFalse(dummyInOutPut.getTitle().isPresent());
        assertInstanceOf(InOutPut.class, dummyInOutPut.title("title"));
        assertTrue(dummyInOutPut.getTitle().isPresent());
        assertEquals("title", dummyInOutPut.getTitle().get());
        dummyInOutPut.setTitle(null);
        assertFalse(dummyInOutPut.getTitle().isPresent());
    }

    /**
     * Test the {@link InOutPut#setDescription(String)} and {@link InOutPut#getDescription()} method.
     */
    @Test
    void descriptionTest() {
        DummyInOutPut dummyInOutPut = new DummyInOutPut();
        assertFalse(dummyInOutPut.getDescription().isPresent());
        assertInstanceOf(InOutPut.class, dummyInOutPut.description("descr"));
        assertTrue(dummyInOutPut.getDescription().isPresent());
        assertEquals("descr", dummyInOutPut.getDescription().get());
        dummyInOutPut.setDescription(null);
        assertFalse(dummyInOutPut.getDescription().isPresent());
    }

    /**
     * Test the {@link InOutPut#setKeywords(String[])} and {@link InOutPut#getKeywords()} method.
     */
    @Test
    void keywordsTest() {
        DummyInOutPut dummyInOutPut = new DummyInOutPut();
        assertFalse(dummyInOutPut.getKeywords().isPresent());
        assertInstanceOf(InOutPut.class, dummyInOutPut.keywords(new String[]{"key1", "key2"}));
        assertTrue(dummyInOutPut.getKeywords().isPresent());
        assertArrayEquals(new String[]{"key1", "key2"}, dummyInOutPut.getKeywords().get());
        dummyInOutPut.setKeywords(null);
        assertFalse(dummyInOutPut.getKeywords().isPresent());
    }

    /**
     * Test the {@link InOutPut#setType(Class)} and {@link InOutPut#getType()} method.
     */
    @Test
    void typeTest() {
        DummyInOutPut dummyInOutPut = new DummyInOutPut();
        assertFalse(dummyInOutPut.getType().isPresent());
        assertInstanceOf(InOutPut.class, dummyInOutPut.type(Integer.class));
        assertTrue(dummyInOutPut.getType().isPresent());
        assertEquals(Integer.class, dummyInOutPut.getType().get());
        dummyInOutPut.setType(null);
        assertFalse(dummyInOutPut.getType().isPresent());
    }

    /**
     * Test the {@link InOutPut#toString()} method.
     */
    @Test
    void toStringTest() {
        IProcess process = ProcessManager.createFactory().create().getProcess();
        DummyInOutPut dummyInOutPut = new DummyInOutPut();
        assertTrue(dummyInOutPut.toString().isEmpty());

        dummyInOutPut.setName("name");
        assertEquals("name", dummyInOutPut.toString());

        dummyInOutPut.setProcess(process);
        assertEquals("name:" + process.getIdentifier(), dummyInOutPut.toString());

        dummyInOutPut.setName(null);
        assertEquals(":" + process.getIdentifier(), dummyInOutPut.toString());
    }

    /**
     * Test the {@link InOutPut#setMetaClass(MetaClass)} and {@link InOutPut#getMetaClass()} methods.
     */
    @Test
    void metaClassTest() {
        DummyInOutPut dummyInOutPut = new DummyInOutPut();
        assertEquals(InvokerHelper.getMetaClass(DummyInOutPut.class), dummyInOutPut.getMetaClass());
        dummyInOutPut.setMetaClass(null);
        assertNotNull(dummyInOutPut.getMetaClass());
        dummyInOutPut.setMetaClass(InvokerHelper.getMetaClass(this.getClass()));
        assertEquals(InvokerHelper.getMetaClass(this.getClass()), dummyInOutPut.getMetaClass());
    }

    /**
     * Test the {@link InOutPut#setMetaClass(MetaClass)} and {@link InOutPut#getMetaClass()} methods.
     */
    @Test
    void propertyTest() {
        DummyInOutPut dummyInOutPut = new DummyInOutPut();
        dummyInOutPut.setProperty(null, null);
        dummyInOutPut.setName("toto");

        assertNull(dummyInOutPut.getProperty(null));
        assertTrue(dummyInOutPut.getName().isPresent());
        assertEquals("toto", dummyInOutPut.getName().get());

        dummyInOutPut.setProperty("name", null);
        assertFalse(dummyInOutPut.getName().isPresent());

        dummyInOutPut.setProperty("name", "tata");
        assertTrue(dummyInOutPut.getName().isPresent());
        assertEquals("tata", dummyInOutPut.getName().get());
        assertEquals("tata", dummyInOutPut.getProperty("name"));

        dummyInOutPut.setMetaClass(null);
        dummyInOutPut.setProperty("name", "tata");
        assertNull(dummyInOutPut.getProperty(null));
    }

    /**
     * Test the {@link InOutPut#invokeMethod(String, Object)} method.
     */
    @Test
    void invokeMethodTest() {
        DummyInOutPut dummyInOutPut = new DummyInOutPut();
        dummyInOutPut.setName("name");

        assertNull(dummyInOutPut.invokeMethod(null, null));
        assertEquals("name", dummyInOutPut.invokeMethod("getName", null));
        dummyInOutPut.setNotOptional("toto");
        assertEquals("toto", dummyInOutPut.invokeMethod("getNotOptional", null));
    }
}
