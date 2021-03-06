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
package com.espertech.esper.common.internal.epl.join.exec.composite;

import com.espertech.esper.common.client.EventBean;

import java.util.Map;
import java.util.Set;

public interface CompositeIndexLookup {
    void lookup(Map<Object, Object> parent, Set<EventBean> result, CompositeIndexQueryResultPostProcessor postProcessor);
    void setNext(CompositeIndexLookup action);
}
