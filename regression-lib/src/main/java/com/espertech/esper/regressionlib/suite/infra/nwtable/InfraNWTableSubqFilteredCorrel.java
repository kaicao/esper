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
package com.espertech.esper.regressionlib.suite.infra.nwtable;

import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.common.internal.support.SupportBean_S0;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;

import java.util.ArrayList;
import java.util.Collection;

public class InfraNWTableSubqFilteredCorrel {

    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> execs = new ArrayList<>();
        // named window tests
        execs.add(new InfraNWTableSubqFilteredCorrelAssertion(true, false, false, false));  // no-share
        execs.add(new InfraNWTableSubqFilteredCorrelAssertion(true, false, false, true));   // no-share create
        execs.add(new InfraNWTableSubqFilteredCorrelAssertion(true, true, false, false));   // share no-create
        execs.add(new InfraNWTableSubqFilteredCorrelAssertion(true, true, true, false));    // disable share no-create
        execs.add(new InfraNWTableSubqFilteredCorrelAssertion(true, true, true, true));     // disable share create

        // table tests
        execs.add(new InfraNWTableSubqFilteredCorrelAssertion(false, false, false, false));  // table no-create
        execs.add(new InfraNWTableSubqFilteredCorrelAssertion(false, false, false, true));  // table create
        return execs;
    }

    private static class InfraNWTableSubqFilteredCorrelAssertion implements RegressionExecution {
        private final boolean namedWindow;
        private final boolean enableIndexShareCreate;
        private final boolean disableIndexShareConsumer;
        private final boolean createExplicitIndex;

        public InfraNWTableSubqFilteredCorrelAssertion(boolean namedWindow, boolean enableIndexShareCreate, boolean disableIndexShareConsumer, boolean createExplicitIndex) {
            this.namedWindow = namedWindow;
            this.enableIndexShareCreate = enableIndexShareCreate;
            this.disableIndexShareConsumer = disableIndexShareConsumer;
            this.createExplicitIndex = createExplicitIndex;
        }

        public void run(RegressionEnvironment env) {
            String createEpl = namedWindow ?
                "@public create window MyInfra#keepall as select * from SupportBean" :
                "@public create table MyInfra (theString string primary key, intPrimitive int primary key)";
            if (enableIndexShareCreate) {
                createEpl = "@Hint('enable_window_subquery_indexshare') " + createEpl;
            }
            env.compileDeploy(createEpl);
            env.compileDeploy("insert into MyInfra select theString, intPrimitive from SupportBean");

            if (createExplicitIndex) {
                env.compileDeploy("@name('index') create index MyIndex on MyInfra(theString)");
            }

            env.sendEventBean(new SupportBean("E1", 1));
            env.sendEventBean(new SupportBean("E2", -2));

            String consumeEpl = "@name('consume') select (select intPrimitive from MyInfra(intPrimitive<0) sw where s0.p00=sw.theString) as val from S0 s0";
            if (disableIndexShareConsumer) {
                consumeEpl = "@Hint('disable_window_subquery_indexshare') " + consumeEpl;
            }
            env.compileDeploy(consumeEpl).addListener("consume");

            env.sendEventBean(new SupportBean_S0(10, "E1"));
            assertVal(env, null);

            env.sendEventBean(new SupportBean_S0(20, "E2"));
            assertVal(env, -2);

            env.sendEventBean(new SupportBean("E3", -3));
            env.sendEventBean(new SupportBean("E4", 4));

            env.sendEventBean(new SupportBean_S0(-3, "E3"));
            assertVal(env, -3);

            env.sendEventBean(new SupportBean_S0(20, "E4"));
            assertVal(env, null);

            env.undeployModuleContaining("consume");
            env.undeployAll();
        }

        private void assertVal(RegressionEnvironment env, Object expected) {
            env.assertEqualsNew("s0", "val", expected);
        }
    }
}
