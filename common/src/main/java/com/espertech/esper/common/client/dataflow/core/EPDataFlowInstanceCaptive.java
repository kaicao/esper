/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.common.client.dataflow.core;

import com.espertech.esper.common.internal.epl.dataflow.runnables.GraphSourceRunnable;

import java.util.List;
import java.util.Map;

/**
 * Holder for captive data flow execution.
 */
public class EPDataFlowInstanceCaptive {

    private final Map<String, EPDataFlowEmitterOperator> emitters;
    private final List<GraphSourceRunnable> runnables;

    /**
     * Ctor.
     *
     * @param emitters  any emitters that are part of the data flow
     * @param runnables any runnables that represent source operators
     */
    public EPDataFlowInstanceCaptive(Map<String, EPDataFlowEmitterOperator> emitters, List<GraphSourceRunnable> runnables) {
        this.emitters = emitters;
        this.runnables = runnables;
    }

    /**
     * Map of named emitters.
     *
     * @return emitters
     */
    public Map<String, EPDataFlowEmitterOperator> getEmitters() {
        return emitters;
    }

    /**
     * List of operator source runnables.
     *
     * @return runnables
     */
    public List<GraphSourceRunnable> getRunnables() {
        return runnables;
    }
}
