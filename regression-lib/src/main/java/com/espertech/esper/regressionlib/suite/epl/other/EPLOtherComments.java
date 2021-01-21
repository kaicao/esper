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
package com.espertech.esper.regressionlib.suite.epl.other;

import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;

import static org.junit.Assert.assertEquals;

public class EPLOtherComments implements RegressionExecution {
    public void run(RegressionEnvironment env) {
        String lineSeparator = System.getProperty("line.separator");
        String statement = "@name('s0') select theString, /* this is my string */\n" +
            "intPrimitive, // same line comment\n" +
            "/* comment taking one line */\n" +
            "// another comment taking a line\n" +
            "intPrimitive as /* rename */ myPrimitive\n" +
            "from SupportBean" + lineSeparator +
            " where /* inside a where */ intPrimitive /* */ = /* */ 100";
        env.compileDeploy(statement).addListener("s0");

        env.sendEventBean(new SupportBean("e1", 100));

        env.assertEventNew("s0", theEvent -> {
            assertEquals("e1", theEvent.get("theString"));
            assertEquals(100, theEvent.get("intPrimitive"));
            assertEquals(100, theEvent.get("myPrimitive"));
        });

        env.sendEventBean(new SupportBean("e1", -1));
        env.assertListenerNotInvoked("s0");

        env.undeployAll();
    }
}
