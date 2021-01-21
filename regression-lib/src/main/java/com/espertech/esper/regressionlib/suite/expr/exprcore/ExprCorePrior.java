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

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.common.internal.support.SupportBean_S0;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.framework.RegressionFlag;
import com.espertech.esper.regressionlib.framework.RegressionPath;
import com.espertech.esper.regressionlib.support.bean.SupportMarketDataBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static com.espertech.esper.regressionlib.support.util.SupportAdminUtil.assertStatelessStmt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExprCorePrior {
    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> execs = new ArrayList<>();
        execs.add(new ExprCorePriorBoundedMultiple());
        execs.add(new ExprCorePriorExtTimedWindow());
        execs.add(new ExprCorePriorTimeBatchWindow());
        execs.add(new ExprCorePriorNoDataWindowWhere());
        execs.add(new ExprCorePriorLengthWindowWhere());
        execs.add(new ExprCorePriorStreamAndVariable());
        execs.add(new ExprCorePriorUnbound());
        execs.add(new ExprCorePriorUnboundSceneOne());
        execs.add(new ExprCorePriorUnboundSceneTwo());
        execs.add(new ExprCorePriorBoundedSingle());
        execs.add(new ExprCorePriorLongRunningSingle());
        execs.add(new ExprCorePriorLongRunningUnbound());
        execs.add(new ExprCorePriorLongRunningMultiple());
        execs.add(new ExprCorePriorTimewindowStats());
        execs.add(new ExprCorePriorTimeWindow());
        execs.add(new ExprCorePriorLengthWindow());
        execs.add(new ExprCorePriorLengthWindowSceneTwo());
        execs.add(new ExprCorePriorSortWindow());
        execs.add(new ExprCorePriorTimeBatchWindowJoin());
        return execs;
    }

    public static class ExprCorePriorUnboundSceneOne implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String text = "@name('s0') select prior(1, symbol) as prior1 from SupportMarketDataBean";
            env.compileDeploy(text).addListener("s0");

            env.sendEventBean(makeMarketDataEvent("E0"));
            env.assertPropsPerRowNewFlattened("s0", new String[]{"prior1"}, new Object[][]{{null}});

            env.milestone(1);

            env.sendEventBean(makeMarketDataEvent("E1"));
            env.assertPropsPerRowNewFlattened("s0", new String[]{"prior1"}, new Object[][]{{"E0"}});

            env.milestone(2);

            for (int i = 2; i < 9; i++) {
                env.sendEventBean(makeMarketDataEvent("E" + i));
                env.assertPropsPerRowNewFlattened("s0", new String[]{"prior1"}, new Object[][]{{"E" + (i - 1)}});

                if (i % 3 == 0) {
                    env.milestone(i + 1);
                }
            }

            env.undeployAll();
        }
    }

    public static class ExprCorePriorUnboundSceneTwo implements RegressionExecution {

        public void run(RegressionEnvironment env) {
            String[] fields = "c0,c1,c2".split(",");

            String epl = "@Name('s0') select theString as c0, prior(1, intPrimitive) as c1, prior(2, intPrimitive) as c2 from SupportBean";
            env.compileDeploy(epl).addListener("s0");

            env.milestone(1);

            sendSupportBean(env, "E1", 10);
            env.assertPropsNew("s0", fields, new Object[]{"E1", null, null});

            env.milestone(2);

            sendSupportBean(env, "E2", 11);
            env.assertPropsNew("s0", fields, new Object[]{"E2", 10, null});

            env.milestone(3);

            sendSupportBean(env, "E3", 12);
            env.assertPropsNew("s0", fields, new Object[]{"E3", 11, 10});

            env.milestone(4);

            env.milestone(5);

            sendSupportBean(env, "E4", 13);
            env.assertPropsNew("s0", fields, new Object[]{"E4", 12, 11});

            sendSupportBean(env, "E5", 14);
            env.assertPropsNew("s0", fields, new Object[]{"E5", 13, 12});

            env.undeployAll();
        }
    }

    public static class ExprCorePriorBoundedMultiple implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "c0,c1,c2".split(",");
            String epl = "@Name('s0') select irstream theString as c0, prior(1, intPrimitive) as c1, prior(2, intPrimitive) as c2 from SupportBean#length(2)";
            env.compileDeploy(epl).addListener("s0");

            env.milestone(1);

            sendSupportBean(env, "E1", 10);
            env.assertPropsNew("s0", fields, new Object[]{"E1", null, null});

            env.milestone(2);

            sendSupportBean(env, "E2", 11);
            env.assertPropsNew("s0", fields, new Object[]{"E2", 10, null});

            env.milestone(3);

            sendSupportBean(env, "E3", 12);
            env.assertPropsIRPair("s0", fields, new Object[]{"E3", 11, 10}, new Object[]{"E1", null, null});

            env.milestone(4);

            env.milestone(5);

            sendSupportBean(env, "E4", 13);
            env.assertPropsIRPair("s0", fields, new Object[]{"E4", 12, 11}, new Object[]{"E2", 10, null});

            sendSupportBean(env, "E5", 14);
            env.assertPropsIRPair("s0", fields, new Object[]{"E5", 13, 12}, new Object[]{"E3", 11, 10});

            env.undeployAll();
        }
    }

    public static class ExprCorePriorBoundedSingle implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "c0,c1".split(",");

            env.milestone(0);

            String epl = "@Name('s0') select irstream theString as c0, prior(1, intPrimitive) as c1 from SupportBean#length(2)";
            env.compileDeploy(epl).addListener("s0");

            env.milestone(1);

            sendSupportBean(env, "E1", 10);
            env.assertPropsNew("s0", fields, new Object[]{"E1", null});

            env.milestone(2);

            sendSupportBean(env, "E2", 11);
            env.assertPropsNew("s0", fields, new Object[]{"E2", 10});

            env.milestone(3);

            sendSupportBean(env, "E3", 12);
            env.assertPropsIRPair("s0", fields, new Object[]{"E3", 11}, new Object[]{"E1", null});

            env.milestone(4);

            env.milestone(5);

            sendSupportBean(env, "E4", 13);
            env.assertPropsIRPair("s0", fields, new Object[]{"E4", 12}, new Object[]{"E2", 10});

            sendSupportBean(env, "E5", 14);
            env.assertPropsIRPair("s0", fields, new Object[]{"E5", 13}, new Object[]{"E3", 11});

            env.undeployAll();
        }
    }

    private static class ExprCorePriorTimewindowStats implements RegressionExecution {
        public void run(RegressionEnvironment env) {

            String epl = "@name('s0') SELECT prior(1, average) as value FROM SupportBean()#time(5 minutes)#uni(intPrimitive)";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new SupportBean("E1", 1));
            env.assertEqualsNew("s0", "value", null);

            env.sendEventBean(new SupportBean("E1", 4));
            env.assertEqualsNew("s0", "value", 1.0);

            env.sendEventBean(new SupportBean("E1", 5));
            env.assertEqualsNew("s0", "value", 2.5d);

            env.undeployAll();
        }
    }

    private static class ExprCorePriorStreamAndVariable implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();
            RegressionPath path = new RegressionPath();
            tryAssertionPriorStreamAndVariable(env, path, "1", milestone);

            // try variable
            tryAssertionPriorStreamAndVariable(env, path, "NUM_PRIOR", milestone);

            // must be a constant-value expression
            env.compileDeploy("@public create variable int NUM_PRIOR_NONCONST = 1", path);
            env.tryInvalidCompile(path, "@name('s0') select prior(NUM_PRIOR_NONCONST, s0) as result from SupportBean_S0#length(2) as s0",
                "Failed to validate select-clause expression 'prior(NUM_PRIOR_NONCONST,s0)': Prior function requires a constant-value integer-typed index expression as the first parameter");

            env.undeployAll();
        }
    }

    private static class ExprCorePriorTimeWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select irstream symbol as currSymbol, " +
                " prior(2, symbol) as priorSymbol, " +
                " prior(2, price) as priorPrice " +
                "from SupportMarketDataBean#time(1 min)";
            env.compileDeploy(epl).addListener("s0");

            // assert select result type
            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("priorSymbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("priorPrice"));
            });

            sendTimer(env, 0);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "D1", 1);
            assertNewEvents(env, "D1", null, null);

            sendTimer(env, 1000);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "D2", 2);
            assertNewEvents(env, "D2", null, null);

            sendTimer(env, 2000);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "D3", 3);
            assertNewEvents(env, "D3", "D1", 1d);

            sendTimer(env, 3000);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "D4", 4);
            assertNewEvents(env, "D4", "D2", 2d);

            sendTimer(env, 4000);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "D5", 5);
            assertNewEvents(env, "D5", "D3", 3d);

            sendTimer(env, 30000);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "D6", 6);
            assertNewEvents(env, "D6", "D4", 4d);

            sendTimer(env, 60000);
            assertOldEvents(env, "D1", null, null);
            sendTimer(env, 61000);
            assertOldEvents(env, "D2", null, null);
            sendTimer(env, 62000);
            assertOldEvents(env, "D3", "D1", 1d);
            sendTimer(env, 63000);
            assertOldEvents(env, "D4", "D2", 2d);
            sendTimer(env, 64000);
            assertOldEvents(env, "D5", "D3", 3d);
            sendTimer(env, 90000);
            assertOldEvents(env, "D6", "D4", 4d);

            sendMarketEvent(env, "D7", 7);
            assertNewEvents(env, "D7", "D5", 5d);
            sendMarketEvent(env, "D8", 8);
            sendMarketEvent(env, "D9", 9);
            sendMarketEvent(env, "D10", 10);
            sendMarketEvent(env, "D11", 11);
            env.listenerReset("s0");

            // release batch
            sendTimer(env, 150000);
            env.assertListener("s0", listener -> {
                EventBean[] oldData = listener.getLastOldData();
                assertNull(listener.getLastNewData());
                assertEquals(5, oldData.length);
                assertEvent(oldData[0], "D7", "D5", 5d);
                assertEvent(oldData[1], "D8", "D6", 6d);
                assertEvent(oldData[2], "D9", "D7", 7d);
                assertEvent(oldData[3], "D10", "D8", 8d);
                assertEvent(oldData[4], "D11", "D9", 9d);
            });

            env.undeployAll();
        }
    }

    private static class ExprCorePriorExtTimedWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select irstream symbol as currSymbol, " +
                " prior(2, symbol) as priorSymbol, " +
                " prior(3, price) as priorPrice " +
                "from SupportMarketDataBean#ext_timed(volume, 1 min) ";
            env.compileDeploy(epl).addListener("s0");

            // assert select result type
            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("priorSymbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("priorPrice"));
            });

            sendMarketEvent(env, "D1", 1, 0);
            assertNewEvents(env, "D1", null, null);

            sendMarketEvent(env, "D2", 2, 1000);
            assertNewEvents(env, "D2", null, null);

            sendMarketEvent(env, "D3", 3, 3000);
            assertNewEvents(env, "D3", "D1", null);

            sendMarketEvent(env, "D4", 4, 4000);
            assertNewEvents(env, "D4", "D2", 1d);

            sendMarketEvent(env, "D5", 5, 5000);
            assertNewEvents(env, "D5", "D3", 2d);

            sendMarketEvent(env, "D6", 6, 30000);
            assertNewEvents(env, "D6", "D4", 3d);

            sendMarketEvent(env, "D7", 7, 60000);
            env.assertListener("s0", listener -> {
                assertEvent(listener.getLastNewData()[0], "D7", "D5", 4d);
                assertEvent(listener.getLastOldData()[0], "D1", null, null);
                listener.reset();
            });

            sendMarketEvent(env, "D8", 8, 61000);
            env.assertListener("s0", listener -> {
                assertEvent(listener.getLastNewData()[0], "D8", "D6", 5d);
                assertEvent(listener.getLastOldData()[0], "D2", null, null);
                listener.reset();
            });

            sendMarketEvent(env, "D9", 9, 63000);
            env.assertListener("s0", listener -> {
                assertEvent(listener.getLastNewData()[0], "D9", "D7", 6d);
                assertEvent(listener.getLastOldData()[0], "D3", "D1", null);
                listener.reset();
            });

            sendMarketEvent(env, "D10", 10, 64000);
            env.assertListener("s0", listener -> {
                assertEvent(listener.getLastNewData()[0], "D10", "D8", 7d);
                assertEvent(listener.getLastOldData()[0], "D4", "D2", 1d);
                listener.reset();
            });

            sendMarketEvent(env, "D10", 10, 150000);
            env.assertListener("s0", listener -> {
                EventBean[] oldData = listener.getLastOldData();
                assertEquals(6, oldData.length);
                assertEvent(oldData[0], "D5", "D3", 2d);
            });

            env.undeployAll();
        }
    }

    private static class ExprCorePriorTimeBatchWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select irstream symbol as currSymbol, " +
                " prior(3, symbol) as priorSymbol, " +
                " prior(2, price) as priorPrice " +
                "from SupportMarketDataBean#time_batch(1 min) ";
            env.compileDeploy(epl).addListener("s0");

            // assert select result type
            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("priorSymbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("priorPrice"));
            });

            sendTimer(env, 0);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "A", 1);
            sendMarketEvent(env, "B", 2);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 60000);
            env.assertListener("s0", listener -> {
                assertEquals(2, listener.getLastNewData().length);
                assertEvent(listener.getLastNewData()[0], "A", null, null);
                assertEvent(listener.getLastNewData()[1], "B", null, null);
                assertNull(listener.getLastOldData());
                listener.reset();
            });

            sendTimer(env, 80000);
            sendMarketEvent(env, "C", 3);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 120000);
            env.assertListener("s0", listener -> {
                assertEquals(1, listener.getLastNewData().length);
                assertEvent(listener.getLastNewData()[0], "C", null, 1d);
                assertEquals(2, listener.getLastOldData().length);
                assertEvent(listener.getLastOldData()[0], "A", null, null);
                listener.reset();
            });

            sendTimer(env, 300000);
            sendMarketEvent(env, "D", 4);
            sendMarketEvent(env, "E", 5);
            sendMarketEvent(env, "F", 6);
            sendMarketEvent(env, "G", 7);
            sendTimer(env, 360000);
            env.assertListener("s0", listener -> {
                assertEquals(4, listener.getLastNewData().length);
                assertEvent(listener.getLastNewData()[0], "D", "A", 2d);
                assertEvent(listener.getLastNewData()[1], "E", "B", 3d);
                assertEvent(listener.getLastNewData()[2], "F", "C", 4d);
                assertEvent(listener.getLastNewData()[3], "G", "D", 5d);
            });

            env.undeployAll();
        }
    }

    private static class ExprCorePriorUnbound implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select symbol as currSymbol, " +
                " prior(3, symbol) as priorSymbol, " +
                " prior(2, price) as priorPrice " +
                "from SupportMarketDataBean";
            env.compileDeploy(epl).addListener("s0");

            // assert select result type
            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("priorSymbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("priorPrice"));
            });

            sendMarketEvent(env, "A", 1);
            assertNewEvents(env, "A", null, null);

            env.milestone(1);

            sendMarketEvent(env, "B", 2);
            assertNewEvents(env, "B", null, null);

            env.milestone(2);

            sendMarketEvent(env, "C", 3);
            assertNewEvents(env, "C", null, 1d);

            env.milestone(3);

            sendMarketEvent(env, "D", 4);
            assertNewEvents(env, "D", "A", 2d);

            env.milestone(4);

            sendMarketEvent(env, "E", 5);
            assertNewEvents(env, "E", "B", 3d);

            env.undeployAll();
        }
    }

    private static class ExprCorePriorNoDataWindowWhere implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String text = "@name('s0') select * from SupportMarketDataBean where prior(1, price) = 100";
            env.compileDeploy(text).addListener("s0");

            sendMarketEvent(env, "IBM", 75);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "IBM", 100);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "IBM", 120);
            env.assertListenerInvoked("s0");

            env.undeployAll();
        }
    }

    private static class ExprCorePriorLongRunningSingle implements RegressionExecution {
        @Override
        public EnumSet<RegressionFlag> flags() {
            return EnumSet.of(RegressionFlag.EXCLUDEWHENINSTRUMENTED);
        }

        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select symbol as currSymbol, " +
                " prior(3, symbol) as prior0Symbol " +
                "from SupportMarketDataBean#sort(3, symbol)";
            env.compileDeploy(epl).addListener("s0");

            Random random = new Random();
            // 200000 is a better number for a memory test, however for short unit tests this is 2000
            for (int i = 0; i < 2000; i++) {
                if (i % 10000 == 0) {
                    //System.out.println(i);
                }

                sendMarketEvent(env, Integer.toString(random.nextInt()), 4);

                if (i % 1000 == 0) {
                    env.listenerReset("s0");
                }
            }

            env.undeployAll();
        }
    }

    private static class ExprCorePriorLongRunningUnbound implements RegressionExecution {
        @Override
        public EnumSet<RegressionFlag> flags() {
            return EnumSet.of(RegressionFlag.EXCLUDEWHENINSTRUMENTED);
        }

        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select symbol as currSymbol, " +
                " prior(3, symbol) as prior0Symbol " +
                "from SupportMarketDataBean";
            env.compileDeploy(epl).addListener("s0");
            assertStatelessStmt(env, "s0", false);

            Random random = new Random();
            // 200000 is a better number for a memory test, however for short unit tests this is 2000
            for (int i = 0; i < 2000; i++) {
                if (i % 10000 == 0) {
                    //System.out.println(i);
                }

                sendMarketEvent(env, Integer.toString(random.nextInt()), 4);

                if (i % 1000 == 0) {
                    env.listenerReset("s0");
                }
            }

            env.undeployAll();
        }
    }

    private static class ExprCorePriorLongRunningMultiple implements RegressionExecution {
        @Override
        public EnumSet<RegressionFlag> flags() {
            return EnumSet.of(RegressionFlag.EXCLUDEWHENINSTRUMENTED);
        }

        public void run(RegressionEnvironment env) {

            String epl = "@name('s0') select symbol as currSymbol, " +
                " prior(3, symbol) as prior0Symbol, " +
                " prior(2, symbol) as prior1Symbol, " +
                " prior(1, symbol) as prior2Symbol, " +
                " prior(0, symbol) as prior3Symbol, " +
                " prior(0, price) as prior0Price, " +
                " prior(1, price) as prior1Price, " +
                " prior(2, price) as prior2Price, " +
                " prior(3, price) as prior3Price " +
                "from SupportMarketDataBean#sort(3, symbol)";
            env.compileDeploy(epl).addListener("s0");

            Random random = new Random();
            // 200000 is a better number for a memory test, however for short unit tests this is 2000
            for (int i = 0; i < 2000; i++) {
                if (i % 10000 == 0) {
                    //System.out.println(i);
                }

                sendMarketEvent(env, Integer.toString(random.nextInt()), 4);

                if (i % 1000 == 0) {
                    env.listenerReset("s0");
                }
            }

            env.undeployAll();
        }
    }

    private static class ExprCorePriorLengthWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select irstream symbol as currSymbol, " +
                "prior(0, symbol) as prior0Symbol, " +
                "prior(1, symbol) as prior1Symbol, " +
                "prior(2, symbol) as prior2Symbol, " +
                "prior(3, symbol) as prior3Symbol, " +
                "prior(0, price) as prior0Price, " +
                "prior(1, price) as prior1Price, " +
                "prior(2, price) as prior2Price, " +
                "prior(3, price) as prior3Price " +
                "from SupportMarketDataBean#length(3) ";
            env.compileDeploy(epl).addListener("s0");

            // assert select result type
            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("prior0Symbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prior0Price"));
            });

            sendMarketEvent(env, "A", 1);
            assertNewEvents(env, "A", "A", 1d, null, null, null, null, null, null);
            sendMarketEvent(env, "B", 2);
            assertNewEvents(env, "B", "B", 2d, "A", 1d, null, null, null, null);
            sendMarketEvent(env, "C", 3);
            assertNewEvents(env, "C", "C", 3d, "B", 2d, "A", 1d, null, null);

            sendMarketEvent(env, "D", 4);
            env.assertListener("s0", listener -> {
                EventBean newEvent = listener.getLastNewData()[0];
                EventBean oldEvent = listener.getLastOldData()[0];
                assertEventProps(env, newEvent, "D", "D", 4d, "C", 3d, "B", 2d, "A", 1d);
                assertEventProps(env, oldEvent, "A", "A", 1d, null, null, null, null, null, null);
            });

            sendMarketEvent(env, "E", 5);
            env.assertListener("s0", listener -> {
                EventBean newEvent = listener.getLastNewData()[0];
                EventBean oldEvent = listener.getLastOldData()[0];
                assertEventProps(env, newEvent, "E", "E", 5d, "D", 4d, "C", 3d, "B", 2d);
                assertEventProps(env, oldEvent, "B", "B", 2d, "A", 1d, null, null, null, null);
            });

            sendMarketEvent(env, "F", 6);
            env.assertListener("s0", listener -> {
                EventBean newEvent = listener.getLastNewData()[0];
                EventBean oldEvent = listener.getLastOldData()[0];
                assertEventProps(env, newEvent, "F", "F", 6d, "E", 5d, "D", 4d, "C", 3d);
                assertEventProps(env, oldEvent, "C", "C", 3d, "B", 2d, "A", 1d, null, null);
            });

            sendMarketEvent(env, "G", 7);
            env.assertListener("s0", listener -> {
                EventBean newEvent = listener.getLastNewData()[0];
                EventBean oldEvent = listener.getLastOldData()[0];
                assertEventProps(env, newEvent, "G", "G", 7d, "F", 6d, "E", 5d, "D", 4d);
                assertEventProps(env, oldEvent, "D", "D", 4d, "C", 3d, "B", 2d, "A", 1d);
            });

            sendMarketEvent(env, "G", 8);
            env.assertListener("s0", listener -> {
                EventBean oldEvent = listener.getLastOldData()[0];
                assertEventProps(env, oldEvent, "E", "E", 5d, "D", 4d, "C", 3d, "B", 2d);
            });

            env.undeployAll();
        }
    }

    public static class ExprCorePriorLengthWindowSceneTwo implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();
            String text = "@name('s0') select prior(1, symbol) as prior1, prior(2, symbol) as prior2 from SupportMarketDataBean#length(3)";
            env.compileDeploy(text).addListener("s0");

            env.sendEventBean(makeMarketDataEvent("E0"));
            env.assertPropsPerRowNewFlattened("s0",  new String[]{"prior1", "prior2"}, new Object[][]{{null, null}});

            env.milestoneInc(milestone);

            env.sendEventBean(makeMarketDataEvent("E1"));
            env.assertPropsPerRowNewFlattened("s0",  new String[]{"prior1", "prior2"}, new Object[][]{{"E0", null}});

            env.milestoneInc(milestone);

            env.sendEventBean(makeMarketDataEvent("E2"));
            env.assertPropsPerRowNewFlattened("s0",  new String[]{"prior1", "prior2"}, new Object[][]{{"E1", "E0"}});

            env.milestoneInc(milestone);

            for (int i = 3; i < 9; i++) {
                env.sendEventBean(makeMarketDataEvent("E" + i));
                env.assertPropsPerRowNewFlattened("s0",  new String[]{"prior1", "prior2"},
                    new Object[][]{{"E" + (i - 1), "E" + (i - 2)}});

                if (i % 3 == 0) {
                    env.milestoneInc(milestone);
                }
            }

            env.undeployAll();
        }
    }

    private static class ExprCorePriorLengthWindowWhere implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select prior(2, symbol) as currSymbol " +
                "from SupportMarketDataBean#length(1) " +
                "where prior(2, price) > 100";
            env.compileDeploy(epl).addListener("s0");

            sendMarketEvent(env, "A", 1);
            sendMarketEvent(env, "B", 130);
            sendMarketEvent(env, "C", 10);
            env.assertListenerNotInvoked("s0");
            sendMarketEvent(env, "D", 5);
            env.assertEqualsNew("s0", "currSymbol", "B");

            env.undeployAll();
        }
    }

    private static class ExprCorePriorSortWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();
            String epl = "@name('s0') select irstream symbol as currSymbol, " +
                " prior(0, symbol) as prior0Symbol, " +
                " prior(1, symbol) as prior1Symbol, " +
                " prior(2, symbol) as prior2Symbol, " +
                " prior(3, symbol) as prior3Symbol, " +
                " prior(0, price) as prior0Price, " +
                " prior(1, price) as prior1Price, " +
                " prior(2, price) as prior2Price, " +
                " prior(3, price) as prior3Price " +
                "from SupportMarketDataBean#sort(3, symbol)";
            tryPriorSortWindow(env, epl, milestone);

            epl = "@name('s0') select irstream symbol as currSymbol, " +
                " prior(3, symbol) as prior3Symbol, " +
                " prior(1, symbol) as prior1Symbol, " +
                " prior(2, symbol) as prior2Symbol, " +
                " prior(0, symbol) as prior0Symbol, " +
                " prior(2, price) as prior2Price, " +
                " prior(1, price) as prior1Price, " +
                " prior(0, price) as prior0Price, " +
                " prior(3, price) as prior3Price " +
                "from SupportMarketDataBean#sort(3, symbol)";
            tryPriorSortWindow(env, epl, milestone);
        }
    }

    private static class ExprCorePriorTimeBatchWindowJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select theString as currSymbol, " +
                "prior(2, symbol) as priorSymbol, " +
                "prior(1, price) as priorPrice " +
                "from SupportBean#keepall, SupportMarketDataBean#time_batch(1 min)";
            env.compileDeploy(epl).addListener("s0");

            // assert select result type
            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("priorSymbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("priorPrice"));
            });

            sendTimer(env, 0);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "A", 1);
            sendMarketEvent(env, "B", 2);
            sendBeanEvent(env, "X1");
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 60000);
            env.assertListener("s0", listener -> {
                assertEquals(2, listener.getLastNewData().length);
                assertEvent(listener.getLastNewData()[0], "X1", null, null);
                assertEvent(listener.getLastNewData()[1], "X1", null, 1d);
                assertNull(listener.getLastOldData());
                listener.reset();
            });

            sendMarketEvent(env, "C1", 11);
            sendMarketEvent(env, "C2", 12);
            sendMarketEvent(env, "C3", 13);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 120000);
            env.assertListener("s0", listener -> {
                assertEquals(3, listener.getLastNewData().length);
                assertEvent(listener.getLastNewData()[0], "X1", "A", 2d);
                assertEvent(listener.getLastNewData()[1], "X1", "B", 11d);
                assertEvent(listener.getLastNewData()[2], "X1", "C1", 12d);
            });

            env.undeployAll();
        }
    }

    private static void tryAssertionPriorStreamAndVariable(RegressionEnvironment env, RegressionPath path, String priorIndex, AtomicInteger milestone) {
        String text = "create constant variable int NUM_PRIOR = 1;\n @name('s0') select prior(" + priorIndex + ", s0) as result from SupportBean_S0#length(2) as s0";
        env.compileDeploy(text, path).addListener("s0");

        SupportBean_S0 e1 = new SupportBean_S0(3);
        env.sendEventBean(e1);
        env.assertEqualsNew("s0", "result", null);

        env.milestone(milestone.getAndIncrement());

        env.sendEventBean(new SupportBean_S0(3));
        env.assertEqualsNew("s0", "result", e1);
        env.assertStatement("s0", statement -> assertEquals(SupportBean_S0.class, statement.getEventType().getPropertyType("result")));

        env.undeployAll();
        path.clear();
    }

    private static void tryPriorSortWindow(RegressionEnvironment env, String epl, AtomicInteger milestone) {
        env.compileDeployAddListenerMile(epl, "s0", milestone.getAndIncrement());

        sendMarketEvent(env, "COX", 30);
        assertNewEvents(env, "COX", "COX", 30d, null, null, null, null, null, null);

        sendMarketEvent(env, "IBM", 45);
        assertNewEvents(env, "IBM", "IBM", 45d, "COX", 30d, null, null, null, null);

        sendMarketEvent(env, "MSFT", 33);
        assertNewEvents(env, "MSFT", "MSFT", 33d, "IBM", 45d, "COX", 30d, null, null);

        sendMarketEvent(env, "XXX", 55);
        env.assertListener("s0", listener -> {
            EventBean newEvent = listener.getLastNewData()[0];
            EventBean oldEvent = listener.getLastOldData()[0];
            assertEventProps(env, newEvent, "XXX", "XXX", 55d, "MSFT", 33d, "IBM", 45d, "COX", 30d);
            assertEventProps(env, oldEvent, "XXX", "XXX", 55d, "MSFT", 33d, "IBM", 45d, "COX", 30d);
        });

        sendMarketEvent(env, "BOO", 20);
        env.assertListener("s0", listener -> {
            EventBean newEvent = listener.getLastNewData()[0];
            EventBean oldEvent = listener.getLastOldData()[0];
            assertEventProps(env, newEvent, "BOO", "BOO", 20d, "XXX", 55d, "MSFT", 33d, "IBM", 45d);
            assertEventProps(env, oldEvent, "MSFT", "MSFT", 33d, "IBM", 45d, "COX", 30d, null, null);
        });

        sendMarketEvent(env, "DOR", 1);
        env.assertListener("s0", listener -> {
            EventBean newEvent = listener.getLastNewData()[0];
            EventBean oldEvent = listener.getLastOldData()[0];
            assertEventProps(env, newEvent, "DOR", "DOR", 1d, "BOO", 20d, "XXX", 55d, "MSFT", 33d);
            assertEventProps(env, oldEvent, "IBM", "IBM", 45d, "COX", 30d, null, null, null, null);
        });

        sendMarketEvent(env, "AAA", 2);
        env.assertListener("s0", listener -> {
            EventBean newEvent = listener.getLastNewData()[0];
            EventBean oldEvent = listener.getLastOldData()[0];
            assertEventProps(env, newEvent, "AAA", "AAA", 2d, "DOR", 1d, "BOO", 20d, "XXX", 55d);
            assertEventProps(env, oldEvent, "DOR", "DOR", 1d, "BOO", 20d, "XXX", 55d, "MSFT", 33d);
        });

        sendMarketEvent(env, "AAB", 2);
        env.assertListener("s0", listener -> {
            EventBean oldEvent = listener.getLastOldData()[0];
            assertEventProps(env, oldEvent, "COX", "COX", 30d, null, null, null, null, null, null);
            listener.reset();
        });

        env.undeployAll();
    }

    private static void assertNewEvents(RegressionEnvironment env, String currSymbol,
                                        String priorSymbol,
                                        Double priorPrice) {
        env.assertListener("s0", listener -> {
            EventBean[] oldData = listener.getLastOldData();
            EventBean[] newData = listener.getLastNewData();

            assertNull(oldData);
            assertEquals(1, newData.length);

            assertEvent(newData[0], currSymbol, priorSymbol, priorPrice);

            listener.reset();
        });
    }

    private static void assertEvent(EventBean eventBean,
                                    String currSymbol,
                                    String priorSymbol,
                                    Double priorPrice) {
        assertEquals(currSymbol, eventBean.get("currSymbol"));
        assertEquals(priorSymbol, eventBean.get("priorSymbol"));
        assertEquals(priorPrice, eventBean.get("priorPrice"));
    }

    private static void assertNewEvents(RegressionEnvironment env, String currSymbol,
                                        String prior0Symbol,
                                        Double prior0Price,
                                        String prior1Symbol,
                                        Double prior1Price,
                                        String prior2Symbol,
                                        Double prior2Price,
                                        String prior3Symbol,
                                        Double prior3Price) {
        env.assertListener("s0", listener -> {
            EventBean[] oldData = listener.getLastOldData();
            EventBean[] newData = listener.getLastNewData();

            assertNull(oldData);
            assertEquals(1, newData.length);
            assertEventProps(env, newData[0], currSymbol, prior0Symbol, prior0Price, prior1Symbol, prior1Price, prior2Symbol, prior2Price, prior3Symbol, prior3Price);

            listener.reset();
        });
    }

    private static void assertEventProps(RegressionEnvironment env, EventBean eventBean,
                                         String currSymbol,
                                         String prior0Symbol,
                                         Double prior0Price,
                                         String prior1Symbol,
                                         Double prior1Price,
                                         String prior2Symbol,
                                         Double prior2Price,
                                         String prior3Symbol,
                                         Double prior3Price) {
        assertEquals(currSymbol, eventBean.get("currSymbol"));
        assertEquals(prior0Symbol, eventBean.get("prior0Symbol"));
        assertEquals(prior0Price, eventBean.get("prior0Price"));
        assertEquals(prior1Symbol, eventBean.get("prior1Symbol"));
        assertEquals(prior1Price, eventBean.get("prior1Price"));
        assertEquals(prior2Symbol, eventBean.get("prior2Symbol"));
        assertEquals(prior2Price, eventBean.get("prior2Price"));
        assertEquals(prior3Symbol, eventBean.get("prior3Symbol"));
        assertEquals(prior3Price, eventBean.get("prior3Price"));

        env.listenerReset("s0");
    }

    private static void sendTimer(RegressionEnvironment env, long timeInMSec) {
        env.advanceTime(timeInMSec);
    }

    private static void sendMarketEvent(RegressionEnvironment env, String symbol, double price) {
        SupportMarketDataBean bean = new SupportMarketDataBean(symbol, price, 0L, null);
        env.sendEventBean(bean);
    }

    private static void sendMarketEvent(RegressionEnvironment env, String symbol, double price, long volume) {
        SupportMarketDataBean bean = new SupportMarketDataBean(symbol, price, volume, null);
        env.sendEventBean(bean);
    }

    private static void sendBeanEvent(RegressionEnvironment env, String theString) {
        SupportBean bean = new SupportBean();
        bean.setTheString(theString);
        env.sendEventBean(bean);
    }

    private static void sendSupportBean(RegressionEnvironment env, String theString, int intPrimitive) {
        env.sendEventBean(new SupportBean(theString, intPrimitive));
    }

    private static void assertOldEvents(RegressionEnvironment env, String currSymbol,
                                        String priorSymbol,
                                        Double priorPrice) {
        env.assertListener("s0", listener -> {
            EventBean[] oldData = listener.getLastOldData();
            EventBean[] newData = listener.getLastNewData();

            assertNull(newData);
            assertEquals(1, oldData.length);

            assertEquals(currSymbol, oldData[0].get("currSymbol"));
            assertEquals(priorSymbol, oldData[0].get("priorSymbol"));
            assertEquals(priorPrice, oldData[0].get("priorPrice"));

            listener.reset();
        });
    }

    private static SupportMarketDataBean makeMarketDataEvent(String symbol) {
        return new SupportMarketDataBean(symbol, 0, 0L, "");
    }
}
