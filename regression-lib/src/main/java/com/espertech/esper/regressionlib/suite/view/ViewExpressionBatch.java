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
package com.espertech.esper.regressionlib.suite.view;

import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.support.bean.SupportBean_A;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ViewExpressionBatch {
    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> execs = new ArrayList<>();
        execs.add(new ViewExpressionBatchNewestEventOldestEvent());
        execs.add(new ViewExpressionBatchLengthBatch());
        execs.add(new ViewExpressionBatchTimeBatch());
        execs.add(new ViewExpressionBatchUDFBuiltin());
        execs.add(new ViewExpressionBatchInvalid());
        execs.add(new ViewExpressionBatchPrev());
        execs.add(new ViewExpressionBatchEventPropBatch());
        execs.add(new ViewExpressionBatchAggregationUngrouped());
        execs.add(new ViewExpressionBatchAggregationWGroupwin());
        execs.add(new ViewExpressionBatchAggregationOnDelete());
        execs.add(new ViewExpressionBatchNamedWindowDelete());
        execs.add(new ViewExpressionBatchDynamicTimeBatch());
        execs.add(new ViewExpressionBatchVariableBatch());
        return execs;
    }

    private static class ViewExpressionBatchNewestEventOldestEvent implements RegressionExecution {
        public void run(RegressionEnvironment env) {

            // try with include-trigger-event
            AtomicInteger milestone = new AtomicInteger();
            String[] fields = new String[]{"theString"};
            String epl = "@name('s0') select irstream * from SupportBean#expr_batch(newest_event.intPrimitive != oldest_event.intPrimitive, false)";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new SupportBean("E1", 1));

            env.milestoneInc(milestone);

            env.sendEventBean(new SupportBean("E2", 1));
            env.assertListenerNotInvoked("s0");

            env.milestoneInc(milestone);

            env.sendEventBean(new SupportBean("E3", 2));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E1"}, {"E2"}}, null);

            env.milestoneInc(milestone);

            env.sendEventBean(new SupportBean("E4", 3));
            env.assertPropsPerRowIRPair("s0", fields,
                new Object[][]{{"E3"}}, new Object[][]{{"E1"}, {"E2"}});

            env.milestoneInc(milestone);

            env.sendEventBean(new SupportBean("E5", 3));
            env.sendEventBean(new SupportBean("E6", 3));
            env.assertListenerNotInvoked("s0");

            env.milestoneInc(milestone);

            env.sendEventBean(new SupportBean("E7", 2));
            env.assertPropsPerRowIRPair("s0", fields,
                new Object[][]{{"E4"}, {"E5"}, {"E6"}}, new Object[][]{{"E3"}});
            env.undeployAll();

            env.milestoneInc(milestone);

            // try with include-trigger-event
            epl = "@name('s0') select irstream * from SupportBean#expr_batch(newest_event.intPrimitive != oldest_event.intPrimitive, true)";
            env.compileDeployAddListenerMile(epl, "s0", milestone.getAndIncrement());

            env.milestoneInc(milestone);

            env.sendEventBean(new SupportBean("E1", 1));
            env.sendEventBean(new SupportBean("E2", 1));
            env.assertListenerNotInvoked("s0");

            env.milestoneInc(milestone);

            env.sendEventBean(new SupportBean("E3", 2));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E1"}, {"E2"}, {"E3"}}, null);

            env.milestoneInc(milestone);

            env.sendEventBean(new SupportBean("E4", 3));
            env.sendEventBean(new SupportBean("E5", 3));

            env.milestoneInc(milestone);

            env.sendEventBean(new SupportBean("E6", 3));
            env.assertListenerNotInvoked("s0");

            env.milestoneInc(milestone);

            env.sendEventBean(new SupportBean("E7", 2));
            env.assertPropsPerRowIRPair("s0", fields,
                new Object[][]{{"E4"}, {"E5"}, {"E6"}, {"E7"}}, new Object[][]{{"E1"}, {"E2"}, {"E3"}});

            env.undeployAll();
        }
    }

    private static class ViewExpressionBatchLengthBatch implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = new String[]{"theString"};
            String epl = "@name('s0') select irstream * from SupportBean#expr_batch(current_count >= 3, true)";
            env.compileDeployAddListenerMileZero(epl, "s0");

            env.sendEventBean(new SupportBean("E1", 1));
            env.sendEventBean(new SupportBean("E2", 2));
            env.assertListenerNotInvoked("s0");

            env.milestone(1);

            env.sendEventBean(new SupportBean("E3", 3));
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"E1"}, {"E2"}, {"E3"}});

            env.milestone(2);

            env.sendEventBean(new SupportBean("E4", 4));
            env.sendEventBean(new SupportBean("E5", 5));
            env.assertListenerNotInvoked("s0");

            env.milestone(3);

            env.sendEventBean(new SupportBean("E6", 6));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E4"}, {"E5"}, {"E6"}}, new Object[][]{{"E1"}, {"E2"}, {"E3"}});

            env.sendEventBean(new SupportBean("E7", 7));

            env.milestone(4);

            env.sendEventBean(new SupportBean("E8", 8));
            env.assertListenerNotInvoked("s0");

            env.milestone(5);

            env.sendEventBean(new SupportBean("E9", 9));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E7"}, {"E8"}, {"E9"}}, new Object[][]{{"E4"}, {"E5"}, {"E6"}});

            env.undeployAll();
        }
    }

    private static class ViewExpressionBatchTimeBatch implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            env.advanceTime(0);

            String[] fields = new String[]{"theString"};
            String epl = "@name('s0') select irstream * from SupportBean#expr_batch(newest_timestamp - oldest_timestamp > 2000)";
            env.compileDeployAddListenerMileZero(epl, "s0");

            env.advanceTime(1000);
            env.sendEventBean(new SupportBean("E1", 1));
            env.advanceTime(1500);
            env.sendEventBean(new SupportBean("E2", 2));
            env.sendEventBean(new SupportBean("E3", 3));
            env.advanceTime(3000);
            env.sendEventBean(new SupportBean("E4", 4));
            env.advanceTime(3100);
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportBean("E5", 5));
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"E1"}, {"E2"}, {"E3"}, {"E4"}, {"E5"}});

            env.sendEventBean(new SupportBean("E6", 6));
            env.advanceTime(5100);
            env.sendEventBean(new SupportBean("E7", 7));
            env.advanceTime(5101);
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportBean("E8", 8));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E6"}, {"E7"}, {"E8"}}, new Object[][]{{"E1"}, {"E2"}, {"E3"}, {"E4"}, {"E5"}});

            env.undeployAll();
        }
    }

    private static class ViewExpressionBatchVariableBatch implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            env.advanceTime(0);
            String[] fields = new String[]{"theString"};

            String epl = "create variable boolean POST = false;\n" +
                "@name('s0') select irstream * from SupportBean#expr_batch(POST);\n";
            env.compileDeployAddListenerMileZero(epl, "s0");

            env.advanceTime(1000);
            env.sendEventBean(new SupportBean("E1", 1));
            env.assertListenerNotInvoked("s0");

            env.runtimeSetVariable("s0", "POST", true);
            env.advanceTime(1001);
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E1"}}, null);

            env.sendEventBean(new SupportBean("E2", 1));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E2"}}, new Object[][]{{"E1"}});

            env.sendEventBean(new SupportBean("E3", 1));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E3"}}, new Object[][]{{"E2"}});

            env.runtimeSetVariable("s0", "POST", false);
            env.sendEventBean(new SupportBean("E4", 1));
            env.sendEventBean(new SupportBean("E5", 2));
            env.advanceTime(2000);
            env.assertListenerNotInvoked("s0");

            env.runtimeSetVariable("s0", "POST", true);
            env.advanceTime(2001);
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E4"}, {"E5"}}, new Object[][]{{"E3"}});

            env.sendEventBean(new SupportBean("E6", 1));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E6"}}, new Object[][]{{"E4"}, {"E5"}});

            env.undeployAll();
        }
    }

    private static class ViewExpressionBatchDynamicTimeBatch implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            env.advanceTime(0);
            String[] fields = new String[]{"theString"};

            String epl = "create variable long SIZE = 1000;\n" +
                "@name('s0') select irstream * from SupportBean#expr_batch(newest_timestamp - oldest_timestamp > SIZE);\n";
            env.compileDeployAddListenerMileZero(epl, "s0");

            env.advanceTime(1000);
            env.sendEventBean(new SupportBean("E1", 0));
            env.advanceTime(1900);
            env.sendEventBean(new SupportBean("E2", 0));
            env.assertListenerNotInvoked("s0");

            env.runtimeSetVariable("s0", "SIZE", 500);
            env.advanceTime(1901);
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E1"}, {"E2"}}, null);

            env.sendEventBean(new SupportBean("E3", 0));
            env.advanceTime(2300);
            env.sendEventBean(new SupportBean("E4", 0));
            env.advanceTime(2500);
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportBean("E5", 0));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E3"}, {"E4"}, {"E5"}}, new Object[][]{{"E1"}, {"E2"}});

            env.advanceTime(3100);
            env.sendEventBean(new SupportBean("E6", 0));
            env.assertListenerNotInvoked("s0");

            env.runtimeSetVariable("s0", "SIZE", 999);
            env.advanceTime(3700);
            env.sendEventBean(new SupportBean("E7", 0));
            env.assertListenerNotInvoked("s0");

            env.advanceTime(4100);
            env.sendEventBean(new SupportBean("E8", 0));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E6"}, {"E7"}, {"E8"}}, new Object[][]{{"E3"}, {"E4"}, {"E5"}});

            env.undeployAll();
        }
    }

    private static class ViewExpressionBatchUDFBuiltin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select * from SupportBean#expr_batch(udf(theString, view_reference, expired_count))";
            env.compileDeployAddListenerMileZero(epl, "s0");

            ViewExpressionWindow.LocalUDF.setResult(true);
            env.sendEventBean(new SupportBean("E1", 0));
            env.assertThat(() -> {
                assertEquals("E1", ViewExpressionWindow.LocalUDF.getKey());
                assertEquals(0, (int) ViewExpressionWindow.LocalUDF.getExpiryCount());
                assertNotNull(ViewExpressionWindow.LocalUDF.getViewref());
            });

            env.sendEventBean(new SupportBean("E2", 0));

            ViewExpressionWindow.LocalUDF.setResult(false);
            env.sendEventBean(new SupportBean("E3", 0));
            env.assertThat(() -> {
                assertEquals("E3", ViewExpressionWindow.LocalUDF.getKey());
                assertEquals(0, (int) ViewExpressionWindow.LocalUDF.getExpiryCount());
                assertNotNull(ViewExpressionWindow.LocalUDF.getViewref());
            });

            env.undeployAll();
        }
    }

    private static class ViewExpressionBatchInvalid implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            env.tryInvalidCompile("select * from SupportBean#expr_batch(1)",
                "Failed to validate data window declaration: Invalid return value for expiry expression, expected a boolean return value but received int [select * from SupportBean#expr_batch(1)]");

            env.tryInvalidCompile("select * from SupportBean#expr_batch((select * from SupportBean#lastevent))",
                "Failed to validate data window declaration: Invalid expiry expression: Sub-select, previous or prior functions are not supported in this context [select * from SupportBean#expr_batch((select * from SupportBean#lastevent))]");

            env.tryInvalidCompile("select * from SupportBean#expr_batch(null < 0)",
                "Failed to validate data window declaration: Invalid parameter expression 0 for Expression-batch view: Failed to validate view parameter expression 'null<0': Null-type value is not allow for relational operator");
        }
    }

    private static class ViewExpressionBatchNamedWindowDelete implements RegressionExecution {
        public void run(RegressionEnvironment env) {

            String[] fields = new String[]{"theString"};
            String epl = "@name('s0') create window NW#expr_batch(current_count > 3) as SupportBean;\n" +
                "insert into NW select * from SupportBean;\n" +
                "on SupportBean_A delete from NW where theString = id;\n";
            env.compileDeployAddListenerMileZero(epl, "s0");

            env.sendEventBean(new SupportBean("E1", 1));
            env.sendEventBean(new SupportBean("E2", 2));
            env.sendEventBean(new SupportBean("E3", 3));
            env.assertPropsPerRowIterator("s0", fields, new Object[][]{{"E1"}, {"E2"}, {"E3"}});

            env.sendEventBean(new SupportBean_A("E2"));
            env.assertPropsPerRowIterator("s0", fields, new Object[][]{{"E1"}, {"E3"}});

            env.sendEventBean(new SupportBean("E4", 4));
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportBean("E5", 5));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E1"}, {"E3"}, {"E4"}, {"E5"}}, null);

            env.undeployAll();
        }
    }

    private static class ViewExpressionBatchPrev implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = new String[]{"val0"};
            String epl = "@name('s0') select prev(1, theString) as val0 from SupportBean#expr_batch(current_count > 2)";
            env.compileDeployAddListenerMileZero(epl, "s0");

            env.sendEventBean(new SupportBean("E1", 1));
            env.sendEventBean(new SupportBean("E2", 2));
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportBean("E3", 3));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{null}, {"E1"}, {"E2"}}, null);

            env.undeployAll();
        }
    }

    private static class ViewExpressionBatchEventPropBatch implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = new String[]{"val0"};
            String epl = "@name('s0') select irstream theString as val0 from SupportBean#expr_batch(intPrimitive > 0)";
            env.compileDeployAddListenerMileZero(epl, "s0");

            env.sendEventBean(new SupportBean("E1", 1));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E1"}}, null);

            env.sendEventBean(new SupportBean("E2", 1));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E2"}}, new Object[][]{{"E1"}});

            env.sendEventBean(new SupportBean("E3", -1));
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportBean("E4", 2));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E3"}, {"E4"}}, new Object[][]{{"E2"}});

            env.undeployAll();
        }
    }

    private static class ViewExpressionBatchAggregationUngrouped implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = new String[]{"theString"};

            String epl = "@name('s0') select irstream theString from SupportBean#expr_batch(sum(intPrimitive) > 100)";
            env.compileDeployAddListenerMileZero(epl, "s0");

            env.sendEventBean(new SupportBean("E1", 1));
            env.sendEventBean(new SupportBean("E2", 90));
            env.assertListenerNotInvoked("s0");

            env.milestone(1);

            env.sendEventBean(new SupportBean("E3", 10));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E1"}, {"E2"}, {"E3"}}, null);

            env.milestone(2);

            env.sendEventBean(new SupportBean("E4", 101));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E4"}}, new Object[][]{{"E1"}, {"E2"}, {"E3"}});

            env.milestone(3);

            env.sendEventBean(new SupportBean("E5", 1));
            env.sendEventBean(new SupportBean("E6", 99));
            env.assertListenerNotInvoked("s0");

            env.milestone(4);

            env.sendEventBean(new SupportBean("E7", 1));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E5"}, {"E6"}, {"E7"}}, new Object[][]{{"E4"}});

            env.undeployAll();
        }
    }

    private static class ViewExpressionBatchAggregationWGroupwin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = new String[]{"theString"};

            String epl = "@name('s0') select irstream theString from SupportBean#groupwin(intPrimitive)#expr_batch(sum(longPrimitive) > 100)";
            env.compileDeployAddListenerMileZero(epl, "s0");

            sendEvent(env, "E1", 1, 10);
            sendEvent(env, "E2", 2, 10);
            sendEvent(env, "E3", 1, 90);
            sendEvent(env, "E4", 2, 80);
            sendEvent(env, "E5", 2, 10);
            env.assertListenerNotInvoked("s0");

            sendEvent(env, "E6", 2, 1);
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E2"}, {"E4"}, {"E5"}, {"E6"}}, null);

            sendEvent(env, "E7", 2, 50);
            env.assertListenerNotInvoked("s0");

            sendEvent(env, "E8", 1, 2);
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E1"}, {"E3"}, {"E8"}}, null);

            sendEvent(env, "E9", 2, 50);
            sendEvent(env, "E10", 1, 101);
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E10"}}, new Object[][]{{"E1"}, {"E3"}, {"E8"}});

            sendEvent(env, "E11", 2, 1);
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E7"}, {"E9"}, {"E11"}}, new Object[][]{{"E2"}, {"E4"}, {"E5"}, {"E6"}});

            sendEvent(env, "E12", 1, 102);
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E12"}}, new Object[][]{{"E10"}});

            env.undeployAll();
        }
    }

    private static class ViewExpressionBatchAggregationOnDelete implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = new String[]{"theString"};

            String epl = "@name('s0') create window NW#expr_batch(sum(intPrimitive) >= 10) as SupportBean;\n" +
                "insert into NW select * from SupportBean;\n" +
                "on SupportBean_A delete from NW where theString = id;\n";
            env.compileDeployAddListenerMileZero(epl, "s0");

            env.sendEventBean(new SupportBean("E1", 1));
            env.sendEventBean(new SupportBean("E2", 8));
            env.sendEventBean(new SupportBean_A("E2"));

            env.sendEventBean(new SupportBean("E3", 8));
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportBean("E4", 1));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E1"}, {"E3"}, {"E4"}}, null);

            env.undeployAll();
        }
    }

    private static void sendEvent(RegressionEnvironment env, String theString, int intPrimitive, long longPrimitive) {
        SupportBean bean = new SupportBean(theString, intPrimitive);
        bean.setLongPrimitive(longPrimitive);
        env.sendEventBean(bean);
    }
}
