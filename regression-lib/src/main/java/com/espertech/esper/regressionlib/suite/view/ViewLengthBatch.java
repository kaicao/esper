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

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.scopetest.EPAssertionUtil;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.support.bean.SupportBean_A;
import com.espertech.esper.regressionlib.support.bean.SupportMarketDataBean;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertNull;

public class ViewLengthBatch {
    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> execs = new ArrayList<>();
        execs.add(new ViewLengthBatchSceneOne());
        execs.add(new ViewLengthBatchSize2());
        execs.add(new ViewLengthBatchSize1());
        execs.add(new ViewLengthBatchSize3());
        execs.add(new ViewLengthBatchInvalid());
        execs.add(new ViewLengthBatchNormal(ViewLengthBatchNormalRunType.VIEW, null));
        execs.add(new ViewLengthBatchPrev());
        execs.add(new ViewLengthBatchDelete());
        execs.add(new ViewLengthBatchNormal(ViewLengthBatchNormalRunType.NAMEDWINDOW, null));
        execs.add(new ViewLengthBatchNormal(ViewLengthBatchNormalRunType.GROUPWIN, null));
        return execs;
    }

    private static class ViewLengthBatchSceneOne implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "symbol".split(",");
            String text = "@name('s0') select irstream * from SupportMarketDataBean#length_batch(3)";
            env.compileDeployAddListenerMileZero(text, "s0");

            env.sendEventBean(makeMarketDataEvent("E1"));

            env.milestone(1);

            env.sendEventBean(makeMarketDataEvent("E2"));

            env.milestone(2);

            env.sendEventBean(makeMarketDataEvent("E3"));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E1"}, {"E2"}, {"E3"}}, null);

            env.milestone(3);

            env.sendEventBean(makeMarketDataEvent("E4"));

            env.milestone(4);

            env.sendEventBean(makeMarketDataEvent("E5"));

            env.milestone(5);

            // test iterator
            env.assertPropsPerRowIterator("s0", new String[]{"symbol"}, new Object[][]{{"E4"}, {"E5"}});

            env.sendEventBean(makeMarketDataEvent("E6"));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E4"}, {"E5"}, {"E6"}}, new Object[][]{{"E1"}, {"E2"}, {"E3"}});

            env.milestone(6);

            env.sendEventBean(makeMarketDataEvent("E7"));

            env.milestone(7);

            env.sendEventBean(makeMarketDataEvent("E8"));

            env.milestone(8);

            env.sendEventBean(makeMarketDataEvent("E9"));
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E7"}, {"E8"}, {"E9"}}, new Object[][]{{"E4"}, {"E5"}, {"E6"}});

            env.milestone(9);

            env.sendEventBean(makeMarketDataEvent("E10"));

            env.milestone(10);

            env.undeployAll();
        }
    }

    private static class ViewLengthBatchSize2 implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select irstream * from SupportBean#length_batch(2)";
            env.compileDeployAddListenerMileZero(epl, "s0");
            SupportBean[] events = get10Events();

            sendEvent(events[0], env);
            env.assertListenerNotInvoked("s0");
            assertUnderlyingIterator(env, new SupportBean[]{events[0]});

            sendEvent(events[1], env);
            assertUnderlyingPerRow(env, new SupportBean[]{events[0], events[1]}, null);
            assertUnderlyingIterator(env, null);

            sendEvent(events[2], env);
            env.assertListenerNotInvoked("s0");
            assertUnderlyingIterator(env, new SupportBean[]{events[2]});

            sendEvent(events[3], env);
            assertUnderlyingPerRow(env, new SupportBean[]{events[2], events[3]}, new SupportBean[]{events[0], events[1]});
            assertUnderlyingIterator(env, null);

            sendEvent(events[4], env);
            env.assertListenerNotInvoked("s0");
            assertUnderlyingIterator(env, new SupportBean[]{events[4]});

            sendEvent(events[5], env);
            assertUnderlyingPerRow(env, new SupportBean[]{events[4], events[5]}, new SupportBean[]{events[2], events[3]});
            assertUnderlyingIterator(env, null);

            env.undeployAll();
        }
    }

    private static class ViewLengthBatchSize1 implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select irstream * from SupportBean#length_batch(1)";
            env.compileDeployAddListenerMileZero(epl, "s0");
            SupportBean[] events = get10Events();

            sendEvent(events[0], env);
            assertUnderlyingPerRow(env, new SupportBean[]{events[0]}, null);
            assertUnderlyingIterator(env, null);

            sendEvent(events[1], env);
            assertUnderlyingPerRow(env, new SupportBean[]{events[1]}, new SupportBean[]{events[0]});
            assertUnderlyingIterator(env, null);

            sendEvent(events[2], env);
            assertUnderlyingPerRow(env, new SupportBean[]{events[2]}, new SupportBean[]{events[1]});
            assertUnderlyingIterator(env, null);

            env.undeployAll();
        }
    }

    private static class ViewLengthBatchSize3 implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select irstream * from SupportBean#length_batch(3)";
            env.compileDeployAddListenerMileZero(epl, "s0");
            SupportBean[] events = get10Events();

            sendEvent(events[0], env);
            env.assertListenerNotInvoked("s0");
            assertUnderlyingIterator(env, new SupportBean[]{events[0]});

            sendEvent(events[1], env);
            env.assertListenerNotInvoked("s0");
            assertUnderlyingIterator(env, new SupportBean[]{events[0], events[1]});

            sendEvent(events[2], env);
            assertUnderlyingPerRow(env, new SupportBean[]{events[0], events[1], events[2]}, null);
            assertUnderlyingIterator(env, null);

            sendEvent(events[3], env);
            env.assertListenerNotInvoked("s0");
            assertUnderlyingIterator(env, new SupportBean[]{events[3]});

            sendEvent(events[4], env);
            env.assertListenerNotInvoked("s0");
            assertUnderlyingIterator(env, new SupportBean[]{events[3], events[4]});

            sendEvent(events[5], env);
            assertUnderlyingPerRow(env, new SupportBean[]{events[3], events[4], events[5]}, new SupportBean[]{events[0], events[1], events[2]});
            assertUnderlyingIterator(env, null);

            env.undeployAll();
        }
    }

    private static class ViewLengthBatchInvalid implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            env.tryInvalidCompile("select * from SupportMarketDataBean#length_batch(0)",
                "Failed to validate data window declaration: Error in view 'length_batch', Length-Batch view requires a positive integer for size but received 0");
        }
    }

    public static class ViewLengthBatchPrev implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String text = "@name('s0') select irstream *, " +
                "prev(1, symbol) as prev1, " +
                "prevtail(0, symbol) as prevTail0, " +
                "prevtail(1, symbol) as prevTail1, " +
                "prevcount(symbol) as prevCountSym, " +
                "prevwindow(symbol) as prevWindowSym " +
                "from SupportMarketDataBean#length_batch(3)";
            env.compileDeployAddListenerMileZero(text, "s0");

            String[] fields = new String[]{"symbol", "prev1", "prevTail0", "prevTail1", "prevCountSym", "prevWindowSym"};
            env.sendEventBean(makeMarketDataEvent("E1"));
            env.sendEventBean(makeMarketDataEvent("E2"));

            env.milestone(1);

            env.sendEventBean(makeMarketDataEvent("E3"));
            env.assertListener("s0", listener -> {
                EventBean[] newEvents = listener.getNewDataListFlattened();
                Object[] win = new Object[]{"E3", "E2", "E1"};
                EPAssertionUtil.assertPropsPerRow(newEvents, fields,
                    new Object[][]{{"E1", null, "E1", "E2", 3L, win}, {"E2", "E1", "E1", "E2", 3L, win}, {"E3", "E2", "E1", "E2", 3L, win}});
                assertNull(listener.getLastOldData());
                listener.reset();
            });

            env.undeployAll();
        }
    }

    public static class ViewLengthBatchDelete implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "theString".split(",");

            String epl = "create window ABCWin#length_batch(3) as SupportBean;\n" +
                "insert into ABCWin select * from SupportBean;\n" +
                "on SupportBean_A delete from ABCWin where theString = id;\n" +
                "@Name('s0') select irstream * from ABCWin;\n";
            env.compileDeployAddListenerMileZero(epl, "s0");

            env.assertPropsPerRowIterator("s0", fields, null);

            sendSupportBean(env, "E1");
            env.assertListenerNotInvoked("s0");

            env.milestone(1);
            env.assertPropsPerRowIterator("s0", fields, new Object[][]{{"E1"}});

            sendSupportBean_A(env, "E1");    // delete
            env.assertListenerNotInvoked("s0");  // batch is quiet-delete

            env.milestone(2);
            env.assertPropsPerRowIterator("s0", fields, new Object[0][]);

            sendSupportBean(env, "E2");
            sendSupportBean(env, "E3");
            env.assertListenerNotInvoked("s0");

            env.milestone(3);

            env.assertPropsPerRowIterator("s0", fields, new Object[][]{{"E2"}, {"E3"}});
            sendSupportBean_A(env, "E3");    // delete
            env.assertListenerNotInvoked("s0");  // batch is quiet-delete

            env.milestone(4);

            sendSupportBean(env, "E4");
            env.assertListenerNotInvoked("s0");
            sendSupportBean(env, "E5");
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E2"}, {"E4"}, {"E5"}}, null);

            env.milestone(5);
            env.assertPropsPerRowIterator("s0", fields, new Object[0][]);

            sendSupportBean(env, "E6");
            sendSupportBean(env, "E7");
            env.assertListenerNotInvoked("s0");

            env.milestone(6);
            env.assertPropsPerRowIterator("s0", fields, new Object[][]{{"E6"}, {"E7"}});

            sendSupportBean(env, "E8");
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E6"}, {"E7"}, {"E8"}}, new Object[][]{{"E2"}, {"E4"}, {"E5"}});

            env.undeployAll();
        }
    }

    public static class ViewLengthBatchNormal implements RegressionExecution {
        private final ViewLengthBatchNormalRunType runType;
        private final String optionalDatawindow;

        public ViewLengthBatchNormal(ViewLengthBatchNormalRunType runType, String optionalDatawindow) {
            this.runType = runType;
            this.optionalDatawindow = optionalDatawindow;
        }

        public void run(RegressionEnvironment env) {
            String[] fields = "theString".split(",");

            String epl;
            if (runType == ViewLengthBatchNormalRunType.VIEW) {
                epl = "@Name('s0') select irstream theString, prev(1, theString) as prevString " +
                    "from SupportBean" + (optionalDatawindow == null ? "#length_batch(3)" : optionalDatawindow);
            } else if (runType == ViewLengthBatchNormalRunType.GROUPWIN) {
                epl = "@Name('s0') select irstream * from SupportBean#groupwin(doubleBoxed)#length_batch(3)";
            } else if (runType == ViewLengthBatchNormalRunType.NAMEDWINDOW) {
                epl = "create window ABCWin#length_batch(3) as SupportBean;\n" +
                    "insert into ABCWin select * from SupportBean;\n" +
                    "@Name('s0') select irstream * from ABCWin;\n";
            } else {
                throw new RuntimeException("Unrecognized variant " + runType);
            }
            env.compileDeployAddListenerMileZero(epl, "s0");

            env.milestone(1);
            env.assertPropsPerRowIterator("s0", fields, null);

            sendSupportBean(env, "E1");
            env.assertListenerNotInvoked("s0");

            env.milestone(2);
            env.assertPropsPerRowIterator("s0", fields, new Object[][]{{"E1"}});

            sendSupportBean(env, "E2");
            env.assertListenerNotInvoked("s0");

            env.milestone(3);
            env.assertPropsPerRowIterator("s0", fields, new Object[][]{{"E1"}, {"E2"}});

            sendSupportBean(env, "E3");
            env.assertListener("s0", listener -> {
                assertNull(listener.getLastOldData());
                if (runType == ViewLengthBatchNormalRunType.VIEW) {
                    EPAssertionUtil.assertPropsPerRow(listener.getLastNewData(), "prevString".split(","), new Object[][]{{null}, {"E1"}, {"E2"}});
                }
                EPAssertionUtil.assertPropsPerRow(listener.getAndResetLastNewData(), fields, new Object[][]{{"E1"}, {"E2"}, {"E3"}});
            });

            env.milestone(4);
            env.assertPropsPerRowIterator("s0", fields, new Object[0][]);

            sendSupportBean(env, "E4");
            sendSupportBean(env, "E5");
            env.assertListenerNotInvoked("s0");

            env.milestone(5);
            env.assertPropsPerRowIterator("s0", fields, new Object[][]{{"E4"}, {"E5"}});

            sendSupportBean(env, "E6");

            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E4"}, {"E5"}, {"E6"}}, new Object[][]{{"E1"}, {"E2"}, {"E3"}});

            env.milestone(6);
            env.assertPropsPerRowIterator("s0", fields, new Object[0][]);

            env.milestone(7);

            sendSupportBean(env, "E7");
            sendSupportBean(env, "E8");
            env.assertListenerNotInvoked("s0");

            sendSupportBean(env, "E9");
            env.assertPropsPerRowIRPair("s0", fields, new Object[][]{{"E7"}, {"E8"}, {"E9"}}, new Object[][]{{"E4"}, {"E5"}, {"E6"}});

            env.undeployAll();
        }

        public String name() {
            return this.getClass().getSimpleName() + "{" +
                "runType=" + runType +
                ", optionalDatawindow='" + optionalDatawindow + '\'' +
                '}';
        }
    }

    public enum ViewLengthBatchNormalRunType {
        VIEW,
        GROUPWIN,
        NAMEDWINDOW
    }

    private static void sendSupportBean_A(RegressionEnvironment env, String e3) {
        env.sendEventBean(new SupportBean_A(e3));
    }

    private static void sendSupportBean(RegressionEnvironment env, String e1) {
        env.sendEventBean(new SupportBean(e1, 0));
    }

    private static void sendEvent(SupportBean theEvent, RegressionEnvironment env) {
        env.sendEventBean(theEvent);
    }

    private static SupportBean[] get10Events() {
        SupportBean[] events = new SupportBean[10];
        for (int i = 0; i < events.length; i++) {
            events[i] = new SupportBean();
        }
        return events;
    }

    private static SupportMarketDataBean makeMarketDataEvent(String symbol) {
        return new SupportMarketDataBean(symbol, 0, 0L, null);
    }

    private static void assertUnderlyingIterator(RegressionEnvironment env, SupportBean[] supportBeans) {
        env.assertIterator("s0", iterator -> {
            EPAssertionUtil.assertEqualsExactOrderUnderlying(supportBeans, iterator);
        });
    }

    private static void assertUnderlyingPerRow(RegressionEnvironment env, SupportBean[] newData, SupportBean[] oldData) {
        env.assertListener("s0", listener -> {
            EPAssertionUtil.assertUnderlyingPerRow(listener.assertInvokedAndReset(), newData, oldData);
        });
    }
}
