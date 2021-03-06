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
package com.espertech.esper.regressionlib.suite.expr.exprcore;

import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.support.expreval.SupportEvalBuilder;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collection;

public class ExprCoreEventIdentityEquals {
    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> execs = new ArrayList<>();
        execs.add(new ExprCoreEventIdentityEqualsSimple());
        execs.add(new ExprCoreEventIdentityEqualsDocSample());
        execs.add(new ExprCoreEventIdentityEqualsSubquery());
        execs.add(new ExprCoreEventIdentityEqualsEnumMethod());
        execs.add(new ExprCoreEventIdentityEqualsInvalid());
        return execs;
    }

    public static class ExprCoreEventIdentityEqualsSimple implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = new String[]{"event_identity_equals(e,e)"};
            SupportEvalBuilder builder = new SupportEvalBuilder("SupportBean", "e")
                .expressions(fields, fields[0]);

            builder.assertion(new SupportBean()).expect(fields, true);

            builder.run(env);
            env.undeployAll();
        }
    }

    public static class ExprCoreEventIdentityEqualsDocSample implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String docSample = "create schema OrderEvent(orderId string, amount double);\n" +
                "select * from OrderEvent as arrivingEvent \n" +
                "  where exists (select * from OrderEvent#time(5) as last5 where not event_identity_equals(arrivingEvent, last5) and arrivingEvent.orderId = last5.orderId);\n" +
                "select orderId, window(*).aggregate(0d, (result, e) => result + (case when event_identity_equals(oe, e) then 0d else e.amount end)) as c0 from OrderEvent#time(10) as oe";
            env.compileDeploy(docSample).undeployAll();
        }
    }

    public static class ExprCoreEventIdentityEqualsSubquery implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String text = "@name('s0') select * from SupportBean as e where exists (select * from SupportBean#keepall as ka where not event_identity_equals(e, ka) and e.theString = ka.theString)";
            env.compileDeploy(text).addListener("s0");

            sendAssertNotReceived(env, "E1");
            sendAssertReceived(env, "E1");

            sendAssertNotReceived(env, "E2");
            sendAssertReceived(env, "E2");
            sendAssertReceived(env, "E2");

            env.undeployAll();
        }
    }

    public static class ExprCoreEventIdentityEqualsEnumMethod implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String text = "@name('s0') select theString, window(*).aggregate(0, (result, e) => result + (case when event_identity_equals(sb, e) then 0 else e.intPrimitive end)) as c0 from SupportBean#time(10) as sb";
            env.compileDeploy(text).addListener("s0");

            sendAssert(env, "E1", 10, 0);
            sendAssert(env, "E2", 11, 10);
            sendAssert(env, "E3", 12, 10 + 11);
            sendAssert(env, "E4", 13, 10 + 11 + 12);

            env.undeployAll();
        }
    }

    public static class ExprCoreEventIdentityEqualsInvalid implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            env.tryInvalidCompile("select event_identity_equals(e) from SupportBean as e",
                "Failed to validate select-clause expression 'event_identity_equals(e)': event_identity_equalsrequires two parameters");

            env.tryInvalidCompile("select event_identity_equals(e, 1) from SupportBean as e",
                "Failed to validate select-clause expression 'event_identity_equals(e,1)': event_identity_equals requires a parameter that resolves to an event but received '1'");

            env.tryInvalidCompile("select event_identity_equals(e, s0) from SupportBean#lastevent as e, SupportBean_S0#lastevent as s0",
                "Failed to validate select-clause expression 'event_identity_equals(e,s0)': event_identity_equals received two different event types as parameter, type 'SupportBean' is not the same as type 'SupportBean_S0'");
        }
    }

    private static void sendAssert(RegressionEnvironment env, String theString, int intPrimitive, int expected) {
        env.sendEventBean(new SupportBean(theString, intPrimitive));
        env.assertPropsNew("s0", "theString,c0".split(","), new Object[]{theString, expected});
    }

    private static void sendAssertNotReceived(RegressionEnvironment env, String theString) {
        env.sendEventBean(new SupportBean(theString, 0));
        env.assertListenerNotInvoked("s0");
    }

    private static void sendAssertReceived(RegressionEnvironment env, String theString) {
        env.sendEventBean(new SupportBean(theString, 0));
        env.assertEventNew("s0", Assert::assertNotNull);
    }
}
