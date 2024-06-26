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
package org.orbisgis.process;

import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.orbisgis.process.api.IProcess;
import org.orbisgis.process.api.IProcessFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class dedicated to {@link ProcessFactory} class.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS Lab-STICC 2019-2020)
 */
public class ProcessManagerTest {

    public static Closure<?> cl;

    @BeforeAll
    public static void beforeAll() {
        String string = "({\n" +
                "            title \"simple process\"\n" +
                "            description \"description\"\n" +
                "            keywords \"key1\", \"key2\"\n" +
                "            inputs inputA: String, inputB: String\n" +
                "            outputs outputA: String\n" +
                "            version \"version\"\n" +
                "            run { inputA, inputB -> [outputA: inputA + inputB] }\n" +
                "        })";
        cl = (Closure<?>) new GroovyShell().evaluate(string);
    }

    /**
     * Test the {@link ProcessManager#getProcessManager()} method
     */
    @Test
    void getProcessManagerTest() {
        assertNotNull(ProcessManager.getProcessManager());
    }

    /**
     * Test the {@link ProcessManager#create()} and {@link ProcessManager#create(Closure)} methods.
     */
    @Test
    void testCreate() {
        ProcessManager pm = ProcessManager.getProcessManager();
        assertNotNull(pm.create());
        assertNotNull(pm.create().getProcess());

        Optional<IProcess> opt = pm.create(cl);
        assertTrue(opt.isPresent());
        IProcess process = opt.get();

        assertNotNull(process);
        assertTrue(process.getTitle().isPresent());
        assertEquals("simple process", process.getTitle().get());
        assertTrue(process.getDescription().isPresent());
        assertEquals("description", process.getDescription().get());
        assertTrue(process.getVersion().isPresent());
        assertEquals("version", process.getVersion().get());
        assertTrue(process.getKeywords().isPresent());
        assertArrayEquals(new String[]{"key1", "key2"}, process.getKeywords().get());
        assertEquals(2, process.getInputs().size());
        assertEquals(1, process.getOutputs().size());

        opt = pm.create(null);
        assertFalse(opt.isPresent());
    }

    /**
     * Test the {@link ProcessManager#factoryIds()},
     * {@link ProcessManager#factory(String)}, {@link ProcessManager#factory()},
     * {@link ProcessManager#createFactory(String)}, {@link ProcessManager#createFactory()} methods.
     */
    @Test
    void testFactories() {
        ProcessManager pm = ProcessManager.getProcessManager();
        assertNotNull(ProcessManager.createFactory());
        assertNotNull(ProcessManager.createFactory("Mayor_DeFacto_Ry"));
        assertNotNull(ProcessManager.createFactory(null));
        assertNotNull(ProcessManager.createFactory(""));

        assertNotNull(pm.factory());
        assertEquals(pm.factory(), pm.factory(null));
        assertEquals(pm.factory(), pm.factory("Default"));
        assertNotNull(pm.factory("Mayor_DeFacto_Ry"));
        assertNotNull(pm.factory("Factorio"));

        assertTrue(pm.factoryIds().contains("Default"));
        assertTrue(pm.factoryIds().contains("Mayor_DeFacto_Ry"));
        assertTrue(pm.factoryIds().contains("Factorio"));
    }

    /**
     * Test the {@link ProcessManager#process(String)} and {@link ProcessManager#process(String, String)} methods.
     */
    @Test
    void testProcess() {
        ProcessManager pm = ProcessManager.getProcessManager();
        assertNotNull(ProcessManager.createFactory("Mayor_DeFacto_Ry"));

        Optional<IProcess> opt1 = pm.factory().create(cl);
        assertTrue(opt1.isPresent());
        String id1 = opt1.get().getIdentifier();

        Optional<IProcess> opt2 = pm.factory("Mayor_DeFacto_Ry").create(cl);
        assertTrue(opt2.isPresent());
        String id2 = opt2.get().getIdentifier();

        assertTrue(pm.process(id1).isPresent());
        assertFalse(pm.process(id2).isPresent());

        assertFalse(pm.process(id1, "Mayor_DeFacto_Ry").isPresent());
        assertTrue(pm.process(id2, "Mayor_DeFacto_Ry").isPresent());

        assertFalse(pm.process(null).isPresent());
        assertFalse(pm.process(null, "Mayor_DeFacto_Ry").isPresent());
        assertFalse(pm.process(id2, null).isPresent());
        assertFalse(pm.process(null, null).isPresent());
    }

    /**
     * Test the {@link ProcessManager#setMetaClass(MetaClass)} and {@link ProcessManager#getMetaClass()} methods.
     */
    @Test
    void metaClassTest() {
        ProcessManager pm = ProcessManager.getProcessManager();
        assertEquals(InvokerHelper.getMetaClass(ProcessManager.class), pm.getMetaClass());
        pm.setMetaClass(null);
        assertNotNull(pm.getMetaClass());
        pm.setMetaClass(InvokerHelper.getMetaClass(this.getClass()));
        assertEquals(InvokerHelper.getMetaClass(this.getClass()), pm.getMetaClass());
        pm.setMetaClass(InvokerHelper.getMetaClass(ProcessManager.class));
    }

    /**
     * Test the {@link ProcessManager#invokeMethod(String, Object)} method.
     */
    @Test
    void invokeMethodTest() {
        ProcessManager pm = ProcessManager.getProcessManager();
        assertNotNull(pm.invokeMethod("create", cl));
        assertInstanceOf(IProcess.class, pm.invokeMethod("create", cl));
        assertNull(pm.invokeMethod("process", "null"));
        assertNotNull(pm.invokeMethod("factory", null));
        assertNull(pm.invokeMethod(null, null));
    }

    /**
     * Test the {@link ProcessManager#register(Map)}, {
     * @link GroovyProcessManager#registerFactory(String, IProcessFactory) methods.
     */
    @Test
    void registerTest() {
        ProcessManager pm = new ProcessManager();
        assertEquals(1, pm.factoryIds().size());

        assertTrue(pm.registerFactory("toto1", new ProcessFactory()));
        assertEquals(2, pm.factoryIds().size());
        assertTrue(pm.factoryIds().contains("toto1"));

        Map<String, IProcessFactory> map = new HashMap<>();
        map.put("toto1", new ProcessFactory());
        map.put("toto2", new ProcessFactory());
        map.put("toto3", new ProcessFactory());
        pm.register(map);
        assertEquals(4, pm.factoryIds().size());
        assertTrue(pm.factoryIds().contains("toto1"));
        assertTrue(pm.factoryIds().contains("toto2"));
        assertTrue(pm.factoryIds().contains("toto3"));

        assertFalse(pm.registerFactory(null, new ProcessFactory()));
        assertFalse(pm.registerFactory("toto1", new ProcessFactory()));
        assertFalse(pm.registerFactory("", new ProcessFactory()));
        assertFalse(pm.registerFactory("id", null));

        assertDoesNotThrow(() -> pm.register(null));
    }
}
