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
package com.espertech.esper.regressionlib.suite.infra.namedwindow;

import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.framework.RegressionPath;

/**
 * NOTE: More namedwindow-related tests in "nwtable"
 */
public class InfraNamedWindowOutputrate implements RegressionExecution {
    public void run(RegressionEnvironment env) {
        RegressionPath path = new RegressionPath();
        env.compileDeploy("@public create window MyWindowOne#keepall as (theString string, intv int)", path);
        env.compileDeploy("insert into MyWindowOne select theString, intPrimitive as intv from SupportBean", path);

        env.advanceTime(0);

        String[] fields = new String[]{"theString", "c"};
        env.compileDeploy("@name('s0') select irstream theString, count(*) as c from MyWindowOne group by theString output snapshot every 1 second", path).addListener("s0");

        env.sendEventBean(new SupportBean("A", 1));
        env.sendEventBean(new SupportBean("A", 2));
        env.sendEventBean(new SupportBean("B", 4));

        env.advanceTime(1000);

        env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"A", 2L}, {"B", 1L}});

        env.sendEventBean(new SupportBean("B", 5));
        env.advanceTime(2000);

        env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"A", 2L}, {"B", 2L}});

        env.advanceTime(3000);

        env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"A", 2L}, {"B", 2L}});

        env.sendEventBean(new SupportBean("A", 5));
        env.sendEventBean(new SupportBean("C", 1));
        env.advanceTime(4000);

        env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"A", 3L}, {"B", 2L}, {"C", 1L}});

        env.undeployAll();
    }
}
