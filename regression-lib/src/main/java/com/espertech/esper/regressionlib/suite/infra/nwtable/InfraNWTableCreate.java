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

import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.framework.RegressionFlag;
import com.espertech.esper.regressionlib.support.events.SupportGenericColUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import static com.espertech.esper.regressionlib.support.events.SupportGenericColUtil.assertPropertyEPTypes;

public class InfraNWTableCreate {
    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> execs = new ArrayList<>();
        execs.add(new InfraCreateGenericColType(true));
        execs.add(new InfraCreateGenericColType(false));
        return execs;
    }

    private static class InfraCreateGenericColType implements RegressionExecution {
        private final boolean namedWindow;

        public InfraCreateGenericColType(boolean namedWindow) {
            this.namedWindow = namedWindow;
        }

        public void run(RegressionEnvironment env) {
            String epl = "@public @buseventtype create schema MyInputEvent(" + SupportGenericColUtil.allNamesAndTypes() + ");\n";
            epl += "@name('infra')";
            epl += namedWindow ?
                "create window MyInfra#keepall as (" :
                "create table MyInfra as (";
            epl += SupportGenericColUtil.allNamesAndTypes();
            epl += ");\n";
            epl += "on MyInputEvent merge MyInfra insert select " + SupportGenericColUtil.allNames() + ";\n";

            env.compileDeploy(epl);
            env.assertStatement("infra", statement -> assertPropertyEPTypes(statement.getEventType()));

            env.sendEventMap(SupportGenericColUtil.getSampleEvent(), "MyInputEvent");

            env.milestone(0);

            env.assertIterator("infra", iterator -> SupportGenericColUtil.compare(env.iterator("infra").next()));

            env.undeployAll();
        }

        public String name() {
            return this.getClass().getSimpleName() + "{" +
                "namedWindow=" + namedWindow +
                '}';
        }

        public EnumSet<RegressionFlag> flags() {
            return EnumSet.of(RegressionFlag.SERDEREQUIRED);
        }
    }
}
