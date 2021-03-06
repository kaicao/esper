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
package com.espertech.esper.common.internal.context.module;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.context.aifactory.core.StatementAgentInstanceFactory;

public interface StatementAIFactoryProvider {
    EPTypeClass EPTYPE = new EPTypeClass(StatementAIFactoryProvider.class);

    StatementAgentInstanceFactory getFactory();

    default void assign(StatementAIFactoryAssignments assignments) {}

    default void unassign() {};

    default void setValue(int number, Object value) {};
}
