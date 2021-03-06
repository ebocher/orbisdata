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
package org.orbisgis.orbisdata.processmanager.process.check;

import groovy.lang.Closure;
import org.orbisgis.orbisdata.processmanager.api.IProcess;
import org.orbisgis.orbisdata.processmanager.api.check.IProcessCheck;
import org.orbisgis.orbisdata.processmanager.api.inoutput.IInOutPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Implementation of the {@link IProcessCheck} interface.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class ProcessCheck implements IProcessCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessCheck.class);

    /** {@link IProcess} concerned by the check. */
    private IProcess process;
    /** Inputs/outputs to use for the check. */
    private LinkedList<IInOutPut> inOutPuts = new LinkedList<>();
    /** {@link Closure} to perform for the check. */
    private Closure cl;
    /** Action to do on fail. */
    private String failAction = STOP;
    /** Message to log on fail. */
    private String failMessage = "Check failed";
    /** Action to do on success. */
    private String successAction = CONTINUE;
    /** Message to log on success. */
    private String successMessage = "Check successful";

    /**
     * Default constructor.
     *
     * @param process {@link IProcess} concerned by the check.
     */
    public ProcessCheck(IProcess process){
        this.process = process;
    }

    @Override
    public void run(LinkedHashMap<String, Object> processInData) {
        if(cl == null){
            LOGGER.error("A closure for the process check should be defined.");
            fail();
        }
        LinkedList<Object> dataList = new LinkedList<>();
        for(IInOutPut inOutPut : inOutPuts){
            if(inOutPut.getProcess().getOutputs().stream().anyMatch(output -> output.getName().equals(inOutPut.getName()))){
                dataList.push(inOutPut.getProcess().getResults().get(inOutPut.getName()));
            }
            else{
                dataList.push(processInData.get(inOutPut.getName()));
            }
        }
        Object result = cl.call(dataList.toArray());
        if(!(result instanceof Boolean)){
            LOGGER.error("The result of the check closure should be a boolean.");
            fail();
        }
        else{
            if((Boolean)result){
                success();
            }
            else{
                fail();
            }
        }
    }

    @Override
    public void onFail(String action, String message) {
        failAction = action;
        failMessage = message;
    }

    @Override
    public void onSuccess(String action, String message) {
        successAction = action;
        successMessage = message;
    }

    @Override
    public void setInOutputs(Object... data) {
        for(Object obj : data){
            if(obj instanceof IInOutPut){
                this.inOutPuts.add((IInOutPut) obj);
            }
            else{
                LOGGER.error("The inOutPuts for the process check should be process input or output.");
            }
        }
    }

    @Override
    public void setClosure(Closure cl) {
        this.cl = cl;
    }

    @Override
    public void fail() {
        LOGGER.error(failMessage);
        switch(failAction){
            case STOP:
                throw new IllegalStateException(failMessage);
            case CONTINUE:
            default:
                break;
        }
    }

    @Override
    public void success() {
        LOGGER.info(successMessage);
        switch(successAction){
            case STOP:
                throw new IllegalStateException(successMessage);
            case CONTINUE:
            default:
                break;
        }
    }

    @Override
    public IProcess getProcess() {
        return process;
    }
}
