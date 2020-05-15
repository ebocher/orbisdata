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
package org.orbisgis.orbisdata.processmanager.process;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.orbisgis.commons.annotations.NotNull;
import org.orbisgis.commons.annotations.Nullable;
import org.orbisgis.orbisdata.processmanager.api.IProcess;
import org.orbisgis.orbisdata.processmanager.api.inoutput.IInOutPut;
import org.orbisgis.orbisdata.processmanager.api.inoutput.IInput;
import org.orbisgis.orbisdata.processmanager.api.inoutput.IOutput;
import org.orbisgis.orbisdata.processmanager.process.inoutput.Input;
import org.orbisgis.orbisdata.processmanager.process.inoutput.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Implementation of the {@link IProcess} interface dedicated to the local creation and execution of process (no link with
 * WPS process for now).
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS Lab-STICC 2019-2020)
 */
public class Process implements IProcess, GroovyObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(Process.class);

    /**
     * Title of the process
     */
    @Nullable
    private String title;
    /**
     * Process version
     */
    @Nullable
    private String version;
    /**
     * Human readable description of the process
     */
    @Nullable
    private String description;
    /**
     * List of simple keyword (one word) of the process
     */
    @Nullable
    private String[] keywords;
    /**
     * List of inputs
     */
    private LinkedList<IInput> inputs;
    /**
     * List of outputs
     */
    private LinkedList<IOutput> outputs;
    /**
     * Closure containing the code to execute on the process execution
     */
    @Nullable
    private Closure<?> closure;
    /**
     * Map of the process Result
     */
    private Map<String, Object> resultMap;
    /**
     * Unique identifier
     */
    private String identifier;
    /**
     * MetaClass use for groovy methods/properties binding
     */
    private MetaClass metaClass;
    /**
     * Map of the defaults values
     */
    private Map<String, Object> defaultValues;

    /**
     * Create a new Process with its title, description, keyword array, input map, output map, version
     * and the closure containing the code to execute on the process execution. The Closure will receive all the input
     * as parameter in the same order as the order of the input map. So the list of parameter of the closure, the
     * array of input and the map of input given on the execution should have the same size and meaning.
     *
     * @param title       Title of the process.
     * @param description Human readable description of the process.
     * @param keywords    List of simple keyword (one word) of the process.
     * @param inputs      Map of inputs with the name as key and the input class as value. The names will be used  to link
     *                    the closure parameters with the execution input data map.
     * @param outputs     Map of outputs with the name as key and the output class as value. Those names will be used to
     *                    generate the Map of the getResults Method.
     * @param version     Process version.
     * @param closure     Closure containing the code to execute on the process execution.
     */
    Process(@Nullable String title, @Nullable String description, @Nullable String[] keywords, @Nullable LinkedHashMap<String, Object> inputs,
            @Nullable LinkedHashMap<String, Object> outputs, @Nullable String version, @Nullable Closure<?> closure) {
        if (inputs != null && closure != null && closure.getMaximumNumberOfParameters() != inputs.size()) {
            LOGGER.error("The number of the closure parameters and the number of process input names are different.");
            return;
        }
        this.title = title;
        this.version = version;
        this.description = description;
        this.keywords = keywords;
        this.inputs = new LinkedList<>();
        this.defaultValues = new HashMap<>();
        if (inputs != null) {
            for (Map.Entry<String, Object> entry : inputs.entrySet()) {
                Input input;
                if (entry.getValue() instanceof Class) {
                    input = new Input().type((Class<?>) entry.getValue());
                } else if (entry.getValue() instanceof Input) {
                    input = (Input) entry.getValue();
                    if (input.isOptional()) {
                        this.defaultValues.put(entry.getKey(), input.getDefaultValue().orElse(null));
                    }
                } else {
                    input = new Input()
                            .type(entry.getValue().getClass())
                            .optional(entry.getValue());
                    this.defaultValues.put(entry.getKey(), entry.getValue());
                }
                input.process(this).name(entry.getKey());
                this.inputs.add(input);
            }
        }
        this.outputs = new LinkedList<>();
        if (outputs != null) {
            for (Map.Entry<String, Object> entry : outputs.entrySet()) {
                Output output;
                if (entry.getValue() instanceof Class) {
                    output = new Output().type((Class<?>) entry.getValue());
                } else {
                    output = ((Output) entry.getValue());
                }
                output.process(this).name(entry.getKey());
                this.outputs.add(output);
            }
        }
        this.closure = closure;
        this.resultMap = new HashMap<>();
        this.identifier = UUID.randomUUID().toString();
        this.metaClass = InvokerHelper.getMetaClass(getClass());
    }

    @Override
    @NotNull
    public IProcess newInstance() {
        Process process = new Process(title, description, keywords, null, null,
                version, closure);
        process.inputs = this.inputs;
        process.outputs = this.outputs;
        process.defaultValues = this.defaultValues;
        return process;
    }

    /**
     * Return the curry closure taking into account the optional arguments.
     *
     * @param inputDataMap Map containing the data for the execution of the closure. This map may not contains several
     *                     inputs.
     * @return The closure if the missing inputs are all optional, false otherwise.
     */
    @Nullable
    private Closure<?> getClosureWithCurry(@NotNull LinkedHashMap<String, Object> inputDataMap) {
        Closure<?> cl = closure;
        if(closure == null){
            LOGGER.error("The closure should not be null.");
            return null;
        }
        int curryIndex = 0;
        for (IInput input : inputs) {
            if (input.getName().isPresent() &&
                    !inputDataMap.containsKey(input.getName().get())) {
                if (defaultValues.get(input.getName().get()) == null) {
                    LOGGER.error("The parameter " + input.getName() + " has no default value.");
                    return null;
                }
                cl = cl.ncurry(curryIndex, defaultValues.get(input.getName().get()));
                curryIndex--;
            }
            curryIndex++;
        }
        return cl;
    }

    /**
     * Returns the input data as an Object array.
     *
     * @param inputDataMap Map containing the data for the execution of the closure.
     * @return The casted input data as an Object array.
     */
    @NotNull
    private Object[] getClosureArgs(@NotNull LinkedHashMap<String, Object> inputDataMap) {
        return inputs
                .stream()
                .map(IInOutPut::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(inputDataMap::containsKey)
                .map(inputDataMap::get)
                .toArray();
    }

    @Override
    public boolean execute(@Nullable LinkedHashMap<String, Object> inputDataMap) {
        LinkedHashMap<String, Object> map = (inputDataMap == null ? new LinkedHashMap<>() : inputDataMap);
        if (closure == null) {
            LOGGER.error("The process should have a Closure defined.");
            return false;
        }
        LOGGER.debug("Starting the execution of '" + this.getTitle() + "'.");
        if ((inputs.size() < map.size() || inputs.size() - defaultValues.size() > map.size())) {
            LOGGER.error("The number of the input data map and the number of process input are different, should" +
                    " be between " + (closure.getMaximumNumberOfParameters() - defaultValues.size()) + " and " +
                    closure.getMaximumNumberOfParameters() + ".");
            return false;
        }
        Object result;
        try {
            if (inputs.size() != 0) {
                Closure<?> cl = getClosureWithCurry(map);
                if (cl == null) {
                    return false;
                }
                result = cl.call(getClosureArgs(map));
            } else {
                result = closure.call();
            }
        } catch (Exception e) {
            LOGGER.error("Error while executing the process.", e);
            return false;
        }
        if (outputs.size() == 0) {
            resultMap = new HashMap<>();
            resultMap.put("result", result);
            return true;
        } else if (result == null) {
            return false;
        } else {
            return checkResults(result);
        }
    }

    /**
     * Check and store the results of the process execution.
     *
     * @param result Result of the process execution.
     * @return True if the execution hes been successful, false otherwise.
     */
    private boolean checkResults(@Nullable Object result) {
        Map<String, Object> map;
        if (!(result instanceof Map)) {
            map = new HashMap<>();
            map.put("result", result);
        } else {
            map = (Map<String, Object>) result;
        }
        boolean isResultValid = outputs.stream()
                .map(IInOutPut::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(map::containsKey);
        LOGGER.debug("End of the execution of '" + this.getTitle() + "'.");
        if (!isResultValid) {
            LOGGER.debug("The output of the execution are not compatible with process output.");
            return false;
        } else {
            map.forEach((key, value) -> resultMap.put(key, value));
            return true;
        }
    }

    @Override
    @Nullable
    public String getTitle() {
        return title;
    }

    @Override
    @Nullable
    public String getVersion() {
        return version;
    }

    @Override
    @Nullable
    public String getDescription() {
        return description;
    }

    @Override
    @Nullable
    public String[] getKeywords() {
        return keywords;
    }

    @Override
    @NotNull
    public Map<String, Object> getResults() {
        return resultMap;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    @Override
    @NotNull
    public List<IInput> getInputs() {
        return inputs;
    }

    @Override
    @NotNull
    public List<IOutput> getOutputs() {
        return outputs;
    }

    //Groovy object methods
    @Override
    public Object invokeMethod(String name, Object args) {
        return metaClass.invokeMethod(this, name, args);
    }

    @Override
    @Nullable
    public Object getProperty(String propertyName) {
        if(propertyName == null){
            return null;
        }
        if (inputs.stream()
                .map(IInOutPut::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(propertyName::equals)) {
            return new Input().process(this).name(propertyName);
        }
        if (outputs.stream()
                .map(IInOutPut::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(propertyName::equals)) {
            return new Output().process(this).name(propertyName);
        }
        return metaClass.getProperty(this, propertyName);
    }

    @Override
    public void setProperty(String propertyName, Object newValue) {
        this.metaClass.setProperty(this, propertyName, newValue);
    }

    @Override
    public MetaClass getMetaClass() {
        return metaClass;
    }

    @Override
    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    @Override
    public boolean call(LinkedHashMap<String, Object> inputDataMap) {
        return execute(inputDataMap);
    }
}