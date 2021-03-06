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
package com.espertech.esper.regressionlib.suite.resultset.outputlimit;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.scopetest.EPAssertionUtil;
import com.espertech.esper.common.internal.collection.UniformPair;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.framework.RegressionPath;
import com.espertech.esper.regressionlib.support.bean.SupportBeanString;
import com.espertech.esper.regressionlib.support.bean.SupportBean_A;
import com.espertech.esper.regressionlib.support.bean.SupportEventWithIntArray;
import com.espertech.esper.regressionlib.support.bean.SupportMarketDataBean;
import com.espertech.esper.regressionlib.support.epl.SupportOutputLimitOpt;
import com.espertech.esper.regressionlib.support.patternassert.ResultAssertExecution;
import com.espertech.esper.regressionlib.support.patternassert.ResultAssertTestResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class ResultSetOutputLimitRowPerGroup {
    private final static String SYMBOL_DELL = "DELL";
    private final static String SYMBOL_IBM = "IBM";

    private final static String CATEGORY = "Fully-Aggregated and Grouped";

    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> execs = new ArrayList<>();
        execs.add(new ResultSetOutputFirstWhenThen());
        execs.add(new ResultSet1NoneNoHavingNoJoin());
        execs.add(new ResultSet2NoneNoHavingJoin());
        execs.add(new ResultSet3NoneHavingNoJoin());
        execs.add(new ResultSet4NoneHavingJoin());
        execs.add(new ResultSet5DefaultNoHavingNoJoin());
        execs.add(new ResultSet6DefaultNoHavingJoin());
        execs.add(new ResultSet7DefaultHavingNoJoin());
        execs.add(new ResultSet8DefaultHavingJoin());
        execs.add(new ResultSet9AllNoHavingNoJoin());
        execs.add(new ResultSet10AllNoHavingJoin());
        execs.add(new ResultSet11AllHavingNoJoin());
        execs.add(new ResultSet12AllHavingJoin());
        execs.add(new ResultSet13LastNoHavingNoJoin());
        execs.add(new ResultSet14LastNoHavingJoin());
        execs.add(new ResultSet13LastNoHavingNoJoinWOrderBy());
        execs.add(new ResultSet14LastNoHavingJoinWOrderBy());
        execs.add(new ResultSet15LastHavingNoJoin());
        execs.add(new ResultSet16LastHavingJoin());
        execs.add(new ResultSet17FirstNoHavingNoJoin());
        execs.add(new ResultSet17FirstNoHavingJoin());
        execs.add(new ResultSet18SnapshotNoHavingNoJoin());
        execs.add(new ResultSet18SnapshotNoHavingJoin());
        execs.add(new ResultSetJoinSortWindow());
        execs.add(new ResultSetLimitSnapshot());
        execs.add(new ResultSetLimitSnapshotLimit());
        execs.add(new ResultSetGroupByAll());
        execs.add(new ResultSetGroupByDefault());
        execs.add(new ResultSetMaxTimeWindow());
        execs.add(new ResultSetNoJoinLast());
        execs.add(new ResultSetNoOutputClauseView());
        execs.add(new ResultSetNoOutputClauseJoin());
        execs.add(new ResultSetNoJoinAll());
        execs.add(new ResultSetJoinLast());
        execs.add(new ResultSetJoinAll());
        execs.add(new ResultSetCrontabNumberSetVariations());
        execs.add(new ResultSetOutputFirstHavingJoinNoJoin());
        execs.add(new ResultSetOutputFirstCrontab());
        execs.add(new ResultSetOutputFirstEveryNEvents());
        execs.add(new ResultSetOutputFirstMultikeyWArray());
        execs.add(new ResultSetOutputAllMultikeyWArray());
        execs.add(new ResultSetOutputLastMultikeyWArray());
        execs.add(new ResultSetOutputSnapshotMultikeyWArray());
        return execs;
    }

    private static class ResultSetOutputSnapshotMultikeyWArray implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "c0,c1,c2".split(",");
            env.advanceTime(0);

            String epl = "@name('s0') select theString as c0, longPrimitive as c1, sum(intPrimitive) as c2 from SupportBean group by theString, longPrimitive " +
                "output snapshot every 10 seconds";
            env.compileDeploy(epl).addListener("s0");

            sendBeanEvent(env, "A", 0, 10);
            sendBeanEvent(env, "B", 1, 11);
            sendBeanEvent(env, "A", 0, 12);
            sendBeanEvent(env, "B", 1, 13);

            env.milestone(0);

            env.advanceTime(10000);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{
                {"A", 0L, 22}, {"B", 1L, 24}});

            env.undeployAll();
        }
    }

    private static class ResultSetOutputLastMultikeyWArray implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            env.advanceTime(0);
            String[] fields = "theString,longPrimitive,thesum".split(",");
            String epl = "@name('s0') select theString, longPrimitive, sum(intPrimitive) as thesum from SupportBean#keepall " +
                "group by theString, longPrimitive output last every 1 seconds";
            env.compileDeploy(epl).addListener("s0");

            sendBeanEvent(env, "A", 0, 10);
            sendBeanEvent(env, "B", 1, 11);

            env.milestone(0);

            sendBeanEvent(env, "A", 0, 12);
            sendBeanEvent(env, "C", 0, 13);

            env.advanceTime(1000);
            env.assertListener("s0", listener -> EPAssertionUtil.assertPropsPerRowAnyOrder(listener.getAndResetLastNewData(), fields, new Object[][]{
                {"A", 0L, 22}, {"B", 1L, 11}, {"C", 0L, 13}}));

            sendBeanEvent(env, "A", 0, 14);

            env.advanceTime(2000);
            env.assertListener("s0", listener -> EPAssertionUtil.assertPropsPerRowAnyOrder(listener.getAndResetLastNewData(), fields, new Object[][]{
                {"A", 0L, 36}}));

            env.undeployAll();
        }
    }

    private static class ResultSetOutputAllMultikeyWArray implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            env.advanceTime(0);
            String[] fields = "theString,longPrimitive,thesum".split(",");
            String epl = "@name('s0') select theString, longPrimitive, sum(intPrimitive) as thesum from SupportBean#keepall " +
                "group by theString, longPrimitive output all every 1 seconds";
            env.compileDeploy(epl).addListener("s0");

            sendBeanEvent(env, "A", 0, 10);
            sendBeanEvent(env, "B", 1, 11);

            env.milestone(0);

            sendBeanEvent(env, "A", 0, 12);
            sendBeanEvent(env, "C", 0, 13);

            env.advanceTime(1000);
            env.assertListener("s0", listener -> EPAssertionUtil.assertPropsPerRowAnyOrder(listener.getAndResetLastNewData(), fields, new Object[][]{
                {"A", 0L, 22}, {"B", 1L, 11}, {"C", 0L, 13}}));

            sendBeanEvent(env, "A", 0, 14);

            env.advanceTime(2000);
            env.assertListener("s0", listener -> EPAssertionUtil.assertPropsPerRowAnyOrder(listener.getAndResetLastNewData(), fields, new Object[][]{
                {"A", 0L, 36}, {"B", 1L, 11}, {"C", 0L, 13}}));

            env.undeployAll();
        }
    }

    private static class ResultSetOutputFirstMultikeyWArray implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            env.advanceTime(0);
            String[] fields = new String[]{"thesum"};
            String epl = "@name('s0') select sum(value) as thesum from SupportEventWithIntArray group by array output first every 10 seconds";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new SupportEventWithIntArray("E1", new int[]{1, 2}, 10));
            env.assertPropsNew("s0", fields, new Object[]{10});

            env.milestone(0);

            env.sendEventBean(new SupportEventWithIntArray("E1", new int[]{1, 2}, 10));
            env.assertListenerNotInvoked("s0");

            env.undeployAll();
        }
    }

    private static class ResultSetCrontabNumberSetVariations implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            env.compileDeploy("select theString from SupportBean output all at (*/2, 8:17, lastweekday, [1, 1], *)");
            env.sendEventBean(new SupportBean());
            env.undeployAll();

            env.compileDeploy("select theString from SupportBean output all at (*/2, 8:17, 30 weekday, [1, 1], *)");
            env.sendEventBean(new SupportBean());
            env.undeployAll();
        }
    }

    private static class ResultSetOutputFirstHavingJoinNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {

            String stmtText = "@name('s0') select theString, sum(intPrimitive) as value from MyWindow group by theString having sum(intPrimitive) > 20 output first every 2 events";
            tryOutputFirstHaving(env, stmtText);

            String stmtTextJoin = "@name('s0') select theString, sum(intPrimitive) as value from MyWindow mv, SupportBean_A#keepall a where a.id = mv.theString " +
                "group by theString having sum(intPrimitive) > 20 output first every 2 events";
            tryOutputFirstHaving(env, stmtTextJoin);

            String stmtTextOrder = "@name('s0') select theString, sum(intPrimitive) as value from MyWindow group by theString having sum(intPrimitive) > 20 output first every 2 events order by theString asc";
            tryOutputFirstHaving(env, stmtTextOrder);

            String stmtTextOrderJoin = "@name('s0') select theString, sum(intPrimitive) as value from MyWindow mv, SupportBean_A#keepall a where a.id = mv.theString " +
                "group by theString having sum(intPrimitive) > 20 output first every 2 events order by theString asc";
            tryOutputFirstHaving(env, stmtTextOrderJoin);
        }
    }

    private static class ResultSetOutputFirstCrontab implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            sendTimer(env, 0);
            String[] fields = "theString,value".split(",");
            String epl = "create window MyWindow#keepall as SupportBean;\n" +
                "insert into MyWindow select * from SupportBean;\n" +
                "on SupportMarketDataBean md delete from MyWindow mw where mw.intPrimitive = md.price;\n" +
                "@name('s0') select theString, sum(intPrimitive) as value from MyWindow group by theString output first at (*/2, *, *, *, *)";
            env.compileDeploy(epl).addListener("s0");

            sendBeanEvent(env, "E1", 10);
            env.assertPropsNew("s0", fields, new Object[]{"E1", 10});

            sendTimer(env, 2 * 60 * 1000 - 1);
            sendBeanEvent(env, "E1", 11);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 2 * 60 * 1000);
            sendBeanEvent(env, "E1", 12);
            env.assertPropsNew("s0", fields, new Object[]{"E1", 33});

            sendBeanEvent(env, "E2", 20);
            env.assertPropsNew("s0", fields, new Object[]{"E2", 20});

            sendBeanEvent(env, "E2", 21);
            sendTimer(env, 4 * 60 * 1000 - 1);
            sendBeanEvent(env, "E2", 22);
            sendBeanEvent(env, "E1", 13);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 4 * 60 * 1000);
            sendBeanEvent(env, "E2", 23);
            env.assertPropsNew("s0", fields, new Object[]{"E2", 86});
            sendBeanEvent(env, "E1", 14);
            env.assertPropsNew("s0", fields, new Object[]{"E1", 60});

            env.undeployAll();
        }
    }

    private static class ResultSetOutputFirstWhenThen implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "theString,value".split(",");
            String epl = "create window MyWindow#keepall as SupportBean;\n" +
                "insert into MyWindow select * from SupportBean;\n" +
                "on SupportMarketDataBean md delete from MyWindow mw where mw.intPrimitive = md.price;\n" +
                "@name('s0') select theString, sum(intPrimitive) as value from MyWindow group by theString output first when varoutone then set varoutone = false;\n";
            env.compileDeploy(epl).addListener("s0");

            sendBeanEvent(env, "E1", 10);

            env.milestone(0);

            sendBeanEvent(env, "E1", 11);
            env.assertListenerNotInvoked("s0");

            env.runtimeSetVariable(null, "varoutone", true);
            sendBeanEvent(env, "E1", 12);
            env.assertPropsNew("s0", fields, new Object[]{"E1", 33});
            env.assertRuntime(runtime -> assertEquals(false, env.runtime().getVariableService().getVariableValue(null, "varoutone")));

            env.milestone(1);

            env.runtimeSetVariable(null, "varoutone", true);
            sendBeanEvent(env, "E2", 20);
            env.assertPropsNew("s0", fields, new Object[]{"E2", 20});
            env.assertRuntime(runtime -> assertEquals(false, env.runtime().getVariableService().getVariableValue(null, "varoutone")));

            sendBeanEvent(env, "E1", 13);
            sendBeanEvent(env, "E2", 21);
            env.assertListenerNotInvoked("s0");

            env.undeployAll();
        }
    }

    private static class ResultSetOutputFirstEveryNEvents implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "theString,value".split(",");
            RegressionPath path = new RegressionPath();
            String epl = "@public create window MyWindow#keepall as SupportBean;\n" +
                "insert into MyWindow select * from SupportBean;\n" +
                "on SupportMarketDataBean md delete from MyWindow mw where mw.intPrimitive = md.price;\n";
            env.compileDeploy(epl, path);

            epl = "@name('s0') select theString, sum(intPrimitive) as value from MyWindow group by theString output first every 3 events;\n";
            env.compileDeploy(epl, path).addListener("s0");

            sendBeanEvent(env, "E1", 10);
            env.assertPropsNew("s0", fields, new Object[]{"E1", 10});

            sendBeanEvent(env, "E1", 12);
            sendBeanEvent(env, "E1", 11);
            env.assertListenerNotInvoked("s0");

            sendBeanEvent(env, "E1", 13);
            env.assertPropsNew("s0", fields, new Object[]{"E1", 46});

            sendMDEvent(env, "S1", 12);
            sendMDEvent(env, "S1", 11);
            env.assertListenerNotInvoked("s0");

            sendMDEvent(env, "S1", 10);
            env.assertPropsNew("s0", fields, new Object[]{"E1", 13});

            sendBeanEvent(env, "E1", 14);
            sendBeanEvent(env, "E1", 15);
            env.assertListenerNotInvoked("s0");

            sendBeanEvent(env, "E2", 20);
            env.assertPropsNew("s0", fields, new Object[]{"E2", 20});
            env.undeployModuleContaining("s0");

            // test variable
            env.compileDeploy("@name('var') @public create variable int myvar_local = 1", path);
            env.compileDeploy("@name('s0') select theString, sum(intPrimitive) as value from MyWindow group by theString output first every myvar_local events", path);
            env.addListener("s0");

            sendBeanEvent(env, "E3", 10);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"E3", 10}});

            sendBeanEvent(env, "E1", 5);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"E1", 47}});

            env.runtimeSetVariable("var", "myvar_local", 2);

            sendBeanEvent(env, "E1", 6);
            env.assertListenerNotInvoked("s0");

            sendBeanEvent(env, "E1", 7);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"E1", 60}});

            sendBeanEvent(env, "E1", 1);
            env.assertListenerNotInvoked("s0");

            sendBeanEvent(env, "E1", 1);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"E1", 62}});

            env.undeployAll();
        }
    }

    private static class ResultSet1NoneNoHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec)" +
                "group by symbol " +
                "order by symbol asc";
            tryAssertion12(env, stmtText, "none", new AtomicInteger());
        }
    }

    private static class ResultSet2NoneNoHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec), " +
                "SupportBean#keepall where theString=symbol " +
                "group by symbol " +
                "order by symbol asc";
            tryAssertion12(env, stmtText, "none", new AtomicInteger());
        }
    }

    private static class ResultSet3NoneHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec) " +
                "group by symbol " +
                " having sum(price) > 50";
            tryAssertion34(env, stmtText, "none", new AtomicInteger());
        }
    }

    private static class ResultSet4NoneHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec), " +
                "SupportBean#keepall where theString=symbol " +
                "group by symbol " +
                "having sum(price) > 50";
            tryAssertion34(env, stmtText, "none", new AtomicInteger());
        }
    }

    private static class ResultSet5DefaultNoHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec) " +
                "group by symbol " +
                "output every 1 seconds order by symbol asc";
            tryAssertion56(env, stmtText, "default", new AtomicInteger());
        }
    }

    private static class ResultSet6DefaultNoHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec), " +
                "SupportBean#keepall where theString=symbol " +
                "group by symbol " +
                "output every 1 seconds order by symbol asc";
            tryAssertion56(env, stmtText, "default", new AtomicInteger());
        }
    }

    private static class ResultSet7DefaultHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec) \n" +
                "group by symbol " +
                "having sum(price) > 50" +
                "output every 1 seconds";
            tryAssertion78(env, stmtText, "default", new AtomicInteger());
        }
    }

    private static class ResultSet8DefaultHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec), " +
                "SupportBean#keepall where theString=symbol " +
                "group by symbol " +
                "having sum(price) > 50" +
                "output every 1 seconds";
            tryAssertion78(env, stmtText, "default", new AtomicInteger());
        }
    }

    private static class ResultSet9AllNoHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec) " +
                "group by symbol " +
                "output all every 1 seconds " +
                "order by symbol";
            tryAssertion9_10(env, stmtText, "all", new AtomicInteger());
        }
    }

    private static class ResultSet10AllNoHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec), " +
                "SupportBean#keepall where theString=symbol " +
                "group by symbol " +
                "output all every 1 seconds " +
                "order by symbol";
            tryAssertion9_10(env, stmtText, "all", new AtomicInteger());
        }
    }

    private static class ResultSet11AllHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                runAssertion11AllHavingNoJoin(env, outputLimitOpt, milestone);
            }
        }
    }

    private static void runAssertion11AllHavingNoJoin(RegressionEnvironment env, SupportOutputLimitOpt opt, AtomicInteger milestone) {
        String stmtText = opt.getHint() + "@name('s0') select symbol, sum(price) " +
            "from SupportMarketDataBean#time(5.5 sec) " +
            "group by symbol " +
            "having sum(price) > 50 " +
            "output all every 1 seconds";
        tryAssertion11_12(env, stmtText, "all", milestone);
    }

    private static class ResultSet12AllHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                runAssertion12AllHavingJoin(env, outputLimitOpt, milestone);
            }
        }
    }

    private static void runAssertion12AllHavingJoin(RegressionEnvironment env, SupportOutputLimitOpt opt, AtomicInteger milestone) {
        String stmtText = opt.getHint() + "@name('s0') select symbol, sum(price) " +
            "from SupportMarketDataBean#time(5.5 sec), " +
            "SupportBean#keepall where theString=symbol " +
            "group by symbol " +
            "having sum(price) > 50 " +
            "output all every 1 seconds";
        tryAssertion11_12(env, stmtText, "all", milestone);
    }

    private static class ResultSet13LastNoHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec)" +
                "group by symbol " +
                "output last every 1 seconds";
            tryAssertion13_14(env, stmtText, "last", true, new AtomicInteger());
        }
    }

    private static class ResultSet14LastNoHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec), " +
                "SupportBean#keepall where theString=symbol " +
                "group by symbol " +
                "output last every 1 seconds";
            tryAssertion13_14(env, stmtText, "last", true, new AtomicInteger());
        }
    }

    private static class ResultSet13LastNoHavingNoJoinWOrderBy implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec)" +
                "group by symbol " +
                "output last every 1 seconds " +
                "order by symbol";
            tryAssertion13_14(env, stmtText, "last", false, new AtomicInteger());
        }
    }

    private static class ResultSet14LastNoHavingJoinWOrderBy implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec), " +
                "SupportBean#keepall where theString=symbol " +
                "group by symbol " +
                "output last every 1 seconds " +
                "order by symbol";
            tryAssertion13_14(env, stmtText, "last", false, new AtomicInteger());
        }
    }

    private static class ResultSet15LastHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                runAssertion15LastHavingNoJoin(env, outputLimitOpt, milestone);
            }
        }
    }

    private static void runAssertion15LastHavingNoJoin(RegressionEnvironment env, SupportOutputLimitOpt opt, AtomicInteger milestone) {
        String stmtText = opt.getHint() + "@name('s0') select symbol, sum(price) " +
            "from SupportMarketDataBean#time(5.5 sec)" +
            "group by symbol " +
            "having sum(price) > 50 " +
            "output last every 1 seconds";
        tryAssertion15_16(env, stmtText, "last", milestone);
    }

    private static class ResultSet16LastHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                runAssertion16LastHavingJoin(env, outputLimitOpt, milestone);
            }
        }
    }

    private static void runAssertion16LastHavingJoin(RegressionEnvironment env, SupportOutputLimitOpt opt, AtomicInteger milestone) {
        String stmtText = opt.getHint() + "@name('s0') select symbol, sum(price) " +
            "from SupportMarketDataBean#time(5.5 sec), " +
            "SupportBean#keepall where theString=symbol " +
            "group by symbol " +
            "having sum(price) > 50 " +
            "output last every 1 seconds";
        tryAssertion15_16(env, stmtText, "last", milestone);
    }

    private static class ResultSet17FirstNoHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec) " +
                "group by symbol " +
                "output first every 1 seconds";
            tryAssertion17(env, stmtText, "first", new AtomicInteger());
        }
    }

    private static class ResultSet17FirstNoHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec), " +
                "SupportBean#keepall where theString=symbol " +
                "group by symbol " +
                "output first every 1 seconds";
            tryAssertion17(env, stmtText, "first", new AtomicInteger());
        }
    }

    private static class ResultSet18SnapshotNoHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec) " +
                "group by symbol " +
                "output snapshot every 1 seconds " +
                "order by symbol";
            tryAssertion18(env, stmtText, "snapshot", new AtomicInteger());
        }
    }

    private static class ResultSet18SnapshotNoHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select symbol, sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec), " +
                "SupportBean#keepall where theString=symbol " +
                "group by symbol " +
                "output snapshot every 1 seconds " +
                "order by symbol";
            tryAssertion18(env, stmtText, "snapshot", new AtomicInteger());
        }
    }

    private static class ResultSetJoinSortWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            sendTimer(env, 0);

            String[] fields = "symbol,maxVol".split(",");
            String epl = "@name('s0') select irstream symbol, max(price) as maxVol" +
                " from SupportMarketDataBean#sort(1, volume desc) as s0," +
                "SupportBean#keepall as s1 " +
                "group by symbol output every 1 seconds";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new SupportBean("JOIN_KEY", -1));

            sendMDEvent(env, "JOIN_KEY", 1d);
            sendMDEvent(env, "JOIN_KEY", 2d);
            env.listenerReset("s0");

            // moves all events out of the window,
            sendTimer(env, 1000);        // newdata is 2 eventa, old data is the same 2 events, therefore the sum is null
            env.assertListener("s0", listener -> {
                UniformPair<EventBean[]> result = listener.getDataListsFlattened();
                assertEquals(2, result.getFirst().length);
                EPAssertionUtil.assertPropsPerRow(result.getFirst(), fields, new Object[][]{{"JOIN_KEY", 1.0}, {"JOIN_KEY", 2.0}});
                assertEquals(2, result.getSecond().length);
                EPAssertionUtil.assertPropsPerRow(result.getSecond(), fields, new Object[][]{{"JOIN_KEY", null}, {"JOIN_KEY", 1.0}});
            });

            env.undeployAll();
        }
    }

    private static class ResultSetLimitSnapshot implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            sendTimer(env, 0);
            String selectStmt = "@name('s0') select symbol, min(price) as minprice from SupportMarketDataBean" +
                "#time(10 seconds) group by symbol output snapshot every 1 seconds order by symbol asc";

            env.compileDeploy(selectStmt).addListener("s0");

            sendMDEvent(env, "ABC", 20);

            sendTimer(env, 500);
            sendMDEvent(env, "IBM", 16);
            sendMDEvent(env, "ABC", 14);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 1000);
            String[] fields = new String[]{"symbol", "minprice"};
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"ABC", 14d}, {"IBM", 16d}});

            sendTimer(env, 1500);
            sendMDEvent(env, "IBM", 18);
            sendMDEvent(env, "MSFT", 30);

            sendTimer(env, 10000);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"ABC", 14d}, {"IBM", 16d}, {"MSFT", 30d}});

            sendTimer(env, 11000);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"IBM", 18d}, {"MSFT", 30d}});

            sendTimer(env, 12000);
            env.assertPropsPerRowLastNew("s0", fields, null);

            env.undeployAll();
        }
    }

    private static class ResultSetLimitSnapshotLimit implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            sendTimer(env, 0);
            String selectStmt = "@name('s0') select symbol, min(price) as minprice from SupportMarketDataBean" +
                "#time(10 seconds) as m, " +
                "SupportBean#keepall as s where s.theString = m.symbol " +
                "group by symbol output snapshot every 1 seconds order by symbol asc";
            env.compileDeploy(selectStmt).addListener("s0");

            for (String theString : "ABC,IBM,MSFT".split(",")) {
                env.sendEventBean(new SupportBean(theString, 1));
            }

            sendMDEvent(env, "ABC", 20);

            sendTimer(env, 500);
            sendMDEvent(env, "IBM", 16);
            sendMDEvent(env, "ABC", 14);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 1000);
            String[] fields = new String[]{"symbol", "minprice"};
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"ABC", 14d}, {"IBM", 16d}});

            sendTimer(env, 1500);
            sendMDEvent(env, "IBM", 18);
            sendMDEvent(env, "MSFT", 30);

            sendTimer(env, 10000);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"ABC", 14d}, {"IBM", 16d}, {"MSFT", 30d}});

            sendTimer(env, 10500);
            sendTimer(env, 11000);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{"IBM", 18d}, {"MSFT", 30d}});

            sendTimer(env, 11500);
            sendTimer(env, 12000);
            env.assertPropsPerRowLastNew("s0", fields, null);

            env.undeployAll();
        }
    }

    private static class ResultSetGroupByAll implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "symbol,sum(price)".split(",");
            String statementString = "@name('s0') select irstream symbol, sum(price) from SupportMarketDataBean#length(5) group by symbol output all every 5 events";
            env.compileDeploy(statementString).addListener("s0");

            // send some events and check that only the most recent
            // ones are kept
            sendMDEvent(env, "IBM", 1D);
            sendMDEvent(env, "IBM", 2D);
            sendMDEvent(env, "HP", 1D);
            sendMDEvent(env, "IBM", 3D);
            sendMDEvent(env, "MAC", 1D);

            env.assertListener("s0", listener -> {
                EventBean[] newData = listener.getLastNewData();
                assertEquals(3, newData.length);
                EPAssertionUtil.assertPropsPerRowAnyOrder(newData, fields, new Object[][]{
                    {"IBM", 6d}, {"HP", 1d}, {"MAC", 1d}});
                EventBean[] oldData = listener.getLastOldData();
                EPAssertionUtil.assertPropsPerRowAnyOrder(oldData, fields, new Object[][]{
                    {"IBM", null}, {"HP", null}, {"MAC", null}});
            });

            env.undeployAll();
        }
    }

    private static class ResultSetGroupByDefault implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "symbol,sum(price)".split(",");
            String epl = "@name('s0') select irstream symbol, sum(price) from SupportMarketDataBean#length(5) group by symbol output every 5 events";
            env.compileDeploy(epl).addListener("s0");

            // send some events and check that only the most recent
            // ones are kept
            sendMDEvent(env, "IBM", 1D);
            sendMDEvent(env, "IBM", 2D);
            sendMDEvent(env, "HP", 1D);
            sendMDEvent(env, "IBM", 3D);
            sendMDEvent(env, "MAC", 1D);
            env.assertListener("s0", listener -> {
                EventBean[] newData = listener.getLastNewData();
                EventBean[] oldData = listener.getLastOldData();
                assertEquals(5, newData.length);
                assertEquals(5, oldData.length);
                EPAssertionUtil.assertPropsPerRow(newData, fields, new Object[][]{
                    {"IBM", 1d}, {"IBM", 3d}, {"HP", 1d}, {"IBM", 6d}, {"MAC", 1d}});
                EPAssertionUtil.assertPropsPerRow(oldData, fields, new Object[][]{
                    {"IBM", null}, {"IBM", 1d}, {"HP", null}, {"IBM", 3d}, {"MAC", null}});
            });

            env.undeployAll();
        }
    }

    private static class ResultSetMaxTimeWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            sendTimer(env, 0);

            String[] fields = "symbol,maxVol".split(",");
            String epl = "@name('s0') select irstream symbol, max(price) as maxVol" +
                " from SupportMarketDataBean#time(1 sec) " +
                "group by symbol output every 1 seconds";
            env.compileDeploy(epl).addListener("s0");


            sendMDEvent(env, "SYM1", 1d);
            sendMDEvent(env, "SYM1", 2d);
            env.listenerReset("s0");

            // moves all events out of the window,
            sendTimer(env, 1000);        // newdata is 2 eventa, old data is the same 2 events, therefore the sum is null
            env.assertListener("s0", listener -> {
                UniformPair<EventBean[]> result = listener.getDataListsFlattened();
                assertEquals(3, result.getFirst().length);
                EPAssertionUtil.assertPropsPerRow(result.getFirst(), fields, new Object[][]{{"SYM1", 1.0}, {"SYM1", 2.0}, {"SYM1", null}});
                assertEquals(3, result.getSecond().length);
                EPAssertionUtil.assertPropsPerRow(result.getSecond(), fields, new Object[][]{{"SYM1", null}, {"SYM1", 1.0}, {"SYM1", 2.0}});
            });

            env.undeployAll();
        }
    }

    private static class ResultSetNoJoinLast implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                tryAssertionNoJoinLast(env, outputLimitOpt);
            }
        }

        private static void tryAssertionNoJoinLast(RegressionEnvironment env, SupportOutputLimitOpt opt) {
            String epl = opt.getHint() + "@name('s0') select irstream symbol," +
                "sum(price) as mySum," +
                "avg(price) as myAvg " +
                "from SupportMarketDataBean#length(3) " +
                "where symbol='DELL' or symbol='IBM' or symbol='GE' " +
                "group by symbol " +
                "output last every 2 events";

            env.compileDeploy(epl).addListener("s0");

            tryAssertionLast(env);
            env.undeployAll();
        }
    }

    private static class ResultSetNoOutputClauseView implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select irstream symbol," +
                "sum(price) as mySum," +
                "avg(price) as myAvg " +
                "from SupportMarketDataBean#length(3) " +
                "where symbol='DELL' or symbol='IBM' or symbol='GE' " +
                "group by symbol";

            env.compileDeploy(epl).addListener("s0");


            tryAssertionSingle(env);

            env.undeployAll();
        }
    }

    private static class ResultSetNoOutputClauseJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select irstream symbol," +
                "sum(price) as mySum," +
                "avg(price) as myAvg " +
                "from SupportBeanString#length(100) as one, " +
                "SupportMarketDataBean#length(3) as two " +
                "where (symbol='DELL' or symbol='IBM' or symbol='GE') " +
                "       and one.theString = two.symbol " +
                "group by symbol";

            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new SupportBeanString(SYMBOL_DELL));
            env.sendEventBean(new SupportBeanString(SYMBOL_IBM));
            env.sendEventBean(new SupportBeanString("AAA"));

            tryAssertionSingle(env);

            env.undeployAll();
        }
    }

    private static class ResultSetNoJoinAll implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                tryAssertionNoJoinAll(env, outputLimitOpt);
            }
        }

        private static void tryAssertionNoJoinAll(RegressionEnvironment env, SupportOutputLimitOpt opt) {
            String epl = opt.getHint() + "@name('s0') select irstream symbol," +
                "sum(price) as mySum," +
                "avg(price) as myAvg " +
                "from SupportMarketDataBean#length(5) " +
                "where symbol='DELL' or symbol='IBM' or symbol='GE' " +
                "group by symbol " +
                "output all every 2 events";

            env.compileDeploy(epl).addListener("s0");


            tryAssertionAll(env);

            env.undeployAll();
        }
    }

    private static class ResultSetJoinLast implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                tryAssertionJoinLast(env, outputLimitOpt);
            }
        }

        private static void tryAssertionJoinLast(RegressionEnvironment env, SupportOutputLimitOpt opt) {
            String epl = opt.getHint() + "@name('s0') select irstream symbol," +
                "sum(price) as mySum," +
                "avg(price) as myAvg " +
                "from SupportBeanString#length(100) as one, " +
                "SupportMarketDataBean#length(3) as two " +
                "where (symbol='DELL' or symbol='IBM' or symbol='GE') " +
                "       and one.theString = two.symbol " +
                "group by symbol " +
                "output last every 2 events";

            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new SupportBeanString(SYMBOL_DELL));
            env.sendEventBean(new SupportBeanString(SYMBOL_IBM));
            env.sendEventBean(new SupportBeanString("AAA"));

            tryAssertionLast(env);

            env.undeployAll();
        }
    }

    private static class ResultSetJoinAll implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                tryAssertionJoinAll(env, outputLimitOpt);
            }
        }
    }

    private static void tryAssertionJoinAll(RegressionEnvironment env, SupportOutputLimitOpt opt) {
        String epl = opt.getHint() + "@name('s0') select irstream symbol," +
            "sum(price) as mySum," +
            "avg(price) as myAvg " +
            "from SupportBeanString#length(100) as one, " +
            "SupportMarketDataBean#length(5) as two " +
            "where (symbol='DELL' or symbol='IBM' or symbol='GE') " +
            "       and one.theString = two.symbol " +
            "group by symbol " +
            "output all every 2 events";

        env.compileDeploy(epl).addListener("s0");

        env.sendEventBean(new SupportBeanString(SYMBOL_DELL));
        env.sendEventBean(new SupportBeanString(SYMBOL_IBM));
        env.sendEventBean(new SupportBeanString("AAA"));

        tryAssertionAll(env);

        env.undeployAll();
    }

    private static void tryAssertionLast(RegressionEnvironment env) {
        // assert select result type
        env.assertStatement("s0", statement -> {
            assertEquals(String.class, statement.getEventType().getPropertyType("symbol"));
            assertEquals(Double.class, statement.getEventType().getPropertyType("mySum"));
            assertEquals(Double.class, statement.getEventType().getPropertyType("myAvg"));
        });

        sendMDEvent(env, SYMBOL_DELL, 10);
        env.assertListenerNotInvoked("s0");

        sendMDEvent(env, SYMBOL_DELL, 20);
        assertEvent(env, SYMBOL_DELL,
            null, null,
            30d, 15d);
        env.listenerReset("s0");

        sendMDEvent(env, SYMBOL_DELL, 100);
        env.assertListenerNotInvoked("s0");

        sendMDEvent(env, SYMBOL_DELL, 50);
        assertEvent(env, SYMBOL_DELL,
            30d, 15d,
            170d, 170 / 3d);
    }

    private static void tryOutputFirstHaving(RegressionEnvironment env, String statementText) {
        String[] fields = "theString,value".split(",");
        String epl = "create window MyWindow#keepall as SupportBean;\n" +
            "insert into MyWindow select * from SupportBean;\n" +
            "on SupportMarketDataBean md delete from MyWindow mw where mw.intPrimitive = md.price;\n" +
            statementText;
        env.compileDeploy(epl).addListener("s0");

        env.sendEventBean(new SupportBean_A("E1"));
        env.sendEventBean(new SupportBean_A("E2"));

        sendBeanEvent(env, "E1", 10);
        sendBeanEvent(env, "E2", 15);
        sendBeanEvent(env, "E1", 10);
        sendBeanEvent(env, "E2", 5);
        env.assertListenerNotInvoked("s0");

        sendBeanEvent(env, "E2", 5);
        env.assertPropsNew("s0", fields, new Object[]{"E2", 25});

        sendBeanEvent(env, "E2", -6);    // to 19, does not count toward condition
        sendBeanEvent(env, "E2", 2);    // to 21, counts toward condition
        env.assertListenerNotInvoked("s0");
        sendBeanEvent(env, "E2", 1);
        env.assertPropsNew("s0", fields, new Object[]{"E2", 22});

        sendBeanEvent(env, "E2", 1);    // to 23, counts toward condition
        env.assertListenerNotInvoked("s0");
        sendBeanEvent(env, "E2", 1);     // to 24
        env.assertPropsNew("s0", fields, new Object[]{"E2", 24});

        sendBeanEvent(env, "E2", -10);    // to 14
        sendBeanEvent(env, "E2", 10);    // to 24, counts toward condition
        env.assertListenerNotInvoked("s0");
        sendBeanEvent(env, "E2", 0);    // to 24, counts toward condition
        env.assertPropsNew("s0", fields, new Object[]{"E2", 24});

        sendBeanEvent(env, "E2", -10);    // to 14
        sendBeanEvent(env, "E2", 1);     // to 15
        sendBeanEvent(env, "E2", 5);     // to 20
        sendBeanEvent(env, "E2", 0);     // to 20
        sendBeanEvent(env, "E2", 1);     // to 21    // counts
        env.assertListenerNotInvoked("s0");

        sendBeanEvent(env, "E2", 0);    // to 21
        env.assertPropsNew("s0", fields, new Object[]{"E2", 21});

        // remove events
        sendMDEvent(env, "E2", 0);
        env.assertPropsNew("s0", fields, new Object[]{"E2", 21});

        // remove events
        sendMDEvent(env, "E2", -10);
        env.assertPropsNew("s0", fields, new Object[]{"E2", 41});

        // remove events
        sendMDEvent(env, "E2", -6);  // since there is 3*-10 we output the next one
        env.assertPropsNew("s0", fields, new Object[]{"E2", 47});

        sendMDEvent(env, "E2", 2);
        env.assertListenerNotInvoked("s0");

        env.undeployAll();
    }

    private static void tryAssertionSingle(RegressionEnvironment env) {
        // assert select result type
        env.assertStatement("s0", statement -> {
            assertEquals(String.class, statement.getEventType().getPropertyType("symbol"));
            assertEquals(Double.class, statement.getEventType().getPropertyType("mySum"));
            assertEquals(Double.class, statement.getEventType().getPropertyType("myAvg"));
        });

        sendMDEvent(env, SYMBOL_DELL, 10);
        assertEvent(env, SYMBOL_DELL,
            null, null,
            10d, 10d);

        sendMDEvent(env, SYMBOL_IBM, 20);
        assertEvent(env, SYMBOL_IBM,
            null, null,
            20d, 20d);
    }

    private static void tryAssertionAll(RegressionEnvironment env) {
        // assert select result type
        env.assertStatement("s0", statement -> {
            assertEquals(String.class, statement.getEventType().getPropertyType("symbol"));
            assertEquals(Double.class, statement.getEventType().getPropertyType("mySum"));
            assertEquals(Double.class, statement.getEventType().getPropertyType("myAvg"));
        });

        sendMDEvent(env, SYMBOL_IBM, 70);
        env.assertListenerNotInvoked("s0");

        sendMDEvent(env, SYMBOL_DELL, 10);
        assertEvents(env, SYMBOL_IBM,
            null, null,
            70d, 70d,
            SYMBOL_DELL,
            null, null,
            10d, 10d);
        env.listenerReset("s0");

        sendMDEvent(env, SYMBOL_DELL, 20);
        env.assertListenerNotInvoked("s0");

        sendMDEvent(env, SYMBOL_DELL, 100);
        assertEvents(env, SYMBOL_IBM,
            70d, 70d,
            70d, 70d,
            SYMBOL_DELL,
            10d, 10d,
            130d, 130d / 3d);
    }


    private static void tryAssertion12(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(200, 1, new Object[][]{{"IBM", 25d}}, new Object[][]{{"IBM", null}});
        expected.addResultInsRem(800, 1, new Object[][]{{"MSFT", 9d}}, new Object[][]{{"MSFT", null}});
        expected.addResultInsRem(1500, 1, new Object[][]{{"IBM", 49d}}, new Object[][]{{"IBM", 25d}});
        expected.addResultInsRem(1500, 2, new Object[][]{{"YAH", 1d}}, new Object[][]{{"YAH", null}});
        expected.addResultInsRem(2100, 1, new Object[][]{{"IBM", 75d}}, new Object[][]{{"IBM", 49d}});
        expected.addResultInsRem(3500, 1, new Object[][]{{"YAH", 3d}}, new Object[][]{{"YAH", 1d}});
        expected.addResultInsRem(4300, 1, new Object[][]{{"IBM", 97d}}, new Object[][]{{"IBM", 75d}});
        expected.addResultInsRem(4900, 1, new Object[][]{{"YAH", 6d}}, new Object[][]{{"YAH", 3d}});
        expected.addResultInsRem(5700, 0, new Object[][]{{"IBM", 72d}}, new Object[][]{{"IBM", 97d}});
        expected.addResultInsRem(5900, 1, new Object[][]{{"YAH", 7d}}, new Object[][]{{"YAH", 6d}});
        expected.addResultInsRem(6300, 0, new Object[][]{{"MSFT", null}}, new Object[][]{{"MSFT", 9d}});
        expected.addResultInsRem(7000, 0, new Object[][]{{"IBM", 48d}, {"YAH", 6d}}, new Object[][]{{"IBM", 72d}, {"YAH", 7d}});

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void tryAssertion34(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(2100, 1, new Object[][]{{"IBM", 75d}}, null);
        expected.addResultInsRem(4300, 1, new Object[][]{{"IBM", 97d}}, new Object[][]{{"IBM", 75d}});
        expected.addResultInsRem(5700, 0, new Object[][]{{"IBM", 72d}}, new Object[][]{{"IBM", 97d}});
        expected.addResultInsRem(7000, 0, null, new Object[][]{{"IBM", 72d}});

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void tryAssertion13_14(RegressionEnvironment env, String stmtText, String outputLimit, boolean assertAllowAnyOrder, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(1200, 0, new Object[][]{{"IBM", 25d}, {"MSFT", 9d}}, new Object[][]{{"IBM", null}, {"MSFT", null}});
        expected.addResultInsRem(2200, 0, new Object[][]{{"IBM", 75d}, {"YAH", 1d}}, new Object[][]{{"IBM", 25d}, {"YAH", null}});
        expected.addResultInsRem(3200, 0, null, null);
        expected.addResultInsRem(4200, 0, new Object[][]{{"YAH", 3d}}, new Object[][]{{"YAH", 1d}});
        expected.addResultInsRem(5200, 0, new Object[][]{{"IBM", 97d}, {"YAH", 6d}}, new Object[][]{{"IBM", 75d}, {"YAH", 3d}});
        expected.addResultInsRem(6200, 0, new Object[][]{{"IBM", 72d}, {"YAH", 7d}}, new Object[][]{{"IBM", 97d}, {"YAH", 6d}});
        expected.addResultInsRem(7200, 0, new Object[][]{{"IBM", 48d}, {"MSFT", null}, {"YAH", 6d}}, new Object[][]{{"IBM", 72d}, {"MSFT", 9d}, {"YAH", 7d}});

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(assertAllowAnyOrder, milestone);
    }

    private static void tryAssertion15_16(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(1200, 0, null, null);
        expected.addResultInsRem(2200, 0, new Object[][]{{"IBM", 75d}}, null);
        expected.addResultInsRem(3200, 0, null, null);
        expected.addResultInsRem(4200, 0, null, null);
        expected.addResultInsRem(5200, 0, new Object[][]{{"IBM", 97d}}, new Object[][]{{"IBM", 75d}});
        expected.addResultInsRem(6200, 0, new Object[][]{{"IBM", 72d}}, new Object[][]{{"IBM", 97d}});
        expected.addResultInsRem(7200, 0, null, new Object[][]{{"IBM", 72d}});

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void tryAssertion78(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(1200, 0, null, null);
        expected.addResultInsRem(2200, 0, new Object[][]{{"IBM", 75d}}, null);
        expected.addResultInsRem(3200, 0, null, null);
        expected.addResultInsRem(4200, 0, null, null);
        expected.addResultInsRem(5200, 0, new Object[][]{{"IBM", 97d}}, new Object[][]{{"IBM", 75d}});
        expected.addResultInsRem(6200, 0, new Object[][]{{"IBM", 72d}}, new Object[][]{{"IBM", 97d}});
        expected.addResultInsRem(7200, 0, null, new Object[][]{{"IBM", 72d}});

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void tryAssertion56(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(1200, 0, new Object[][]{{"IBM", 25d}, {"MSFT", 9d}}, new Object[][]{{"IBM", null}, {"MSFT", null}});
        expected.addResultInsRem(2200, 0, new Object[][]{{"IBM", 49d}, {"IBM", 75d}, {"YAH", 1d}}, new Object[][]{{"IBM", 25d}, {"IBM", 49d}, {"YAH", null}});
        expected.addResultInsRem(3200, 0, null, null);
        expected.addResultInsRem(4200, 0, new Object[][]{{"YAH", 3d}}, new Object[][]{{"YAH", 1d}});
        expected.addResultInsRem(5200, 0, new Object[][]{{"IBM", 97d}, {"YAH", 6d}}, new Object[][]{{"IBM", 75d}, {"YAH", 3d}});
        expected.addResultInsRem(6200, 0, new Object[][]{{"IBM", 72d}, {"YAH", 7d}}, new Object[][]{{"IBM", 97d}, {"YAH", 6d}});
        expected.addResultInsRem(7200, 0, new Object[][]{{"IBM", 48d}, {"MSFT", null}, {"YAH", 6d}}, new Object[][]{{"IBM", 72d}, {"MSFT", 9d}, {"YAH", 7d}});

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void tryAssertion9_10(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(1200, 0, new Object[][]{{"IBM", 25d}, {"MSFT", 9d}}, new Object[][]{{"IBM", null}, {"MSFT", null}});
        expected.addResultInsRem(2200, 0, new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 1d}}, new Object[][]{{"IBM", 25d}, {"MSFT", 9d}, {"YAH", null}});
        expected.addResultInsRem(3200, 0, new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 1d}}, new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 1d}});
        expected.addResultInsRem(4200, 0, new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 3d}}, new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 1d}});
        expected.addResultInsRem(5200, 0, new Object[][]{{"IBM", 97d}, {"MSFT", 9d}, {"YAH", 6d}}, new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 3d}});
        expected.addResultInsRem(6200, 0, new Object[][]{{"IBM", 72d}, {"MSFT", 9d}, {"YAH", 7d}}, new Object[][]{{"IBM", 97d}, {"MSFT", 9d}, {"YAH", 6d}});
        expected.addResultInsRem(7200, 0, new Object[][]{{"IBM", 48d}, {"MSFT", null}, {"YAH", 6d}}, new Object[][]{{"IBM", 72d}, {"MSFT", 9d}, {"YAH", 7d}});

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void tryAssertion11_12(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(1200, 0, null, null);
        expected.addResultInsRem(2200, 0, new Object[][]{{"IBM", 75d}}, null);
        expected.addResultInsRem(3200, 0, new Object[][]{{"IBM", 75d}}, new Object[][]{{"IBM", 75d}});
        expected.addResultInsRem(4200, 0, new Object[][]{{"IBM", 75d}}, new Object[][]{{"IBM", 75d}});
        expected.addResultInsRem(5200, 0, new Object[][]{{"IBM", 97d}}, new Object[][]{{"IBM", 75d}});
        expected.addResultInsRem(6200, 0, new Object[][]{{"IBM", 72d}}, new Object[][]{{"IBM", 97d}});
        expected.addResultInsRem(7200, 0, null, new Object[][]{{"IBM", 72d}});

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void tryAssertion17(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(200, 1, new Object[][]{{"IBM", 25d}}, new Object[][]{{"IBM", null}});
        expected.addResultInsRem(800, 1, new Object[][]{{"MSFT", 9d}}, new Object[][]{{"MSFT", null}});
        expected.addResultInsRem(1500, 1, new Object[][]{{"IBM", 49d}}, new Object[][]{{"IBM", 25d}});
        expected.addResultInsRem(1500, 2, new Object[][]{{"YAH", 1d}}, new Object[][]{{"YAH", null}});
        expected.addResultInsRem(3500, 1, new Object[][]{{"YAH", 3d}}, new Object[][]{{"YAH", 1d}});
        expected.addResultInsRem(4300, 1, new Object[][]{{"IBM", 97d}}, new Object[][]{{"IBM", 75d}});
        expected.addResultInsRem(4900, 1, new Object[][]{{"YAH", 6d}}, new Object[][]{{"YAH", 3d}});
        expected.addResultInsRem(5700, 0, new Object[][]{{"IBM", 72d}}, new Object[][]{{"IBM", 97d}});
        expected.addResultInsRem(5900, 1, new Object[][]{{"YAH", 7d}}, new Object[][]{{"YAH", 6d}});
        expected.addResultInsRem(6300, 0, new Object[][]{{"MSFT", null}}, new Object[][]{{"MSFT", 9d}});
        expected.addResultInsRem(7000, 0, new Object[][]{{"IBM", 48d}, {"YAH", 6d}}, new Object[][]{{"IBM", 72d}, {"YAH", 7d}});

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void tryAssertion18(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsert(1200, 0, new Object[][]{{"IBM", 25d}, {"MSFT", 9d}});
        expected.addResultInsert(2200, 0, new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 1d}});
        expected.addResultInsert(3200, 0, new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 1d}});
        expected.addResultInsert(4200, 0, new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 3d}});
        expected.addResultInsert(5200, 0, new Object[][]{{"IBM", 97d}, {"MSFT", 9d}, {"YAH", 6d}});
        expected.addResultInsert(6200, 0, new Object[][]{{"IBM", 72d}, {"MSFT", 9d}, {"YAH", 7d}});
        expected.addResultInsert(7200, 0, new Object[][]{{"IBM", 48d}, {"YAH", 6d}});

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void assertEvent(RegressionEnvironment env, String symbol,
                                    Double oldSum, Double oldAvg,
                                    Double newSum, Double newAvg) {
        env.assertListener("s0", listener -> {
            EventBean[] oldData = listener.getLastOldData();
            EventBean[] newData = listener.getLastNewData();

            assertEquals(1, oldData.length);
            assertEquals(1, newData.length);

            assertEquals(symbol, oldData[0].get("symbol"));
            assertEquals(oldSum, oldData[0].get("mySum"));
            assertEquals(oldAvg, oldData[0].get("myAvg"));

            assertEquals(symbol, newData[0].get("symbol"));
            assertEquals(newSum, newData[0].get("mySum"));
            assertEquals("newData myAvg wrong", newAvg, newData[0].get("myAvg"));

            listener.reset();
        });
    }

    private static void assertEvents(RegressionEnvironment env, String symbolOne,
                                     Double oldSumOne, Double oldAvgOne,
                                     double newSumOne, double newAvgOne,
                                     String symbolTwo,
                                     Double oldSumTwo, Double oldAvgTwo,
                                     double newSumTwo, double newAvgTwo) {
        env.assertListener("s0", listener -> {
            EPAssertionUtil.assertPropsPerRowAnyOrder(listener.getAndResetDataListsFlattened(),
                "mySum,myAvg".split(","),
                new Object[][]{{newSumOne, newAvgOne}, {newSumTwo, newAvgTwo}},
                new Object[][]{{oldSumOne, oldAvgOne}, {oldSumTwo, oldAvgTwo}});
        });
    }

    private static void sendMDEvent(RegressionEnvironment env, String symbol, double price) {
        SupportMarketDataBean bean = new SupportMarketDataBean(symbol, price, 0L, null);
        env.sendEventBean(bean);
    }

    private static void sendBeanEvent(RegressionEnvironment env, String theString, int intPrimitive) {
        env.sendEventBean(new SupportBean(theString, intPrimitive));
    }

    private static void sendBeanEvent(RegressionEnvironment env, String theString, long longPrimitive, int intPrimitive) {
        SupportBean b = new SupportBean();
        b.setTheString(theString);
        b.setLongPrimitive(longPrimitive);
        b.setIntPrimitive(intPrimitive);
        env.sendEventBean(b);
    }

    private static void sendTimer(RegressionEnvironment env, long timeInMSec) {
        env.advanceTime(timeInMSec);
    }
}
