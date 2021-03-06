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

import com.espertech.esper.common.client.EPException;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.soda.*;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.common.internal.util.SerializableObjectCopier;
import com.espertech.esper.regressionlib.framework.*;
import com.espertech.esper.regressionlib.support.bean.*;
import com.espertech.esper.regressionlib.support.events.SupportGenericColUtil;
import com.espertech.esper.regressionlib.support.expreval.SupportEvalBuilder;
import com.espertech.esper.regressionlib.support.schedule.SupportDateTimeUtil;
import com.espertech.esper.runtime.client.EPStatement;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ExprCoreCast {

    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> executions = new ArrayList<>();
        executions.add(new ExprCoreCastDates());
        executions.add(new ExprCoreCastSimple());
        executions.add(new ExprCoreCastSimpleMoreTypes());
        executions.add(new ExprCoreCastAsParse());
        executions.add(new ExprCoreCastDoubleAndNullOM());
        executions.add(new ExprCoreCastInterface());
        executions.add(new ExprCoreCastStringAndNullCompile());
        executions.add(new ExprCoreCastBoolean());
        executions.add(new ExprCoreCastWStaticType());
        executions.add(new ExprCoreCastWArray(false));
        executions.add(new ExprCoreCastWArray(true));
        executions.add(new ExprCoreCastGeneric());
        executions.add(new ExprCoreCastBigDecimalBigInt());
        return executions;
    }

    private static class ExprCoreCastBigDecimalBigInt implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@buseventtype @public create schema MyEvent(value java.lang.Object);\n" +
                    "@name('s0') select cast(value, BigDecimal) as c0, cast(value, BigInteger) as c1 from MyEvent;\n";
            env.compileDeploy(epl).addListener("s0");

            sendAssert(env, 1, BigDecimal.valueOf(1), BigInteger.valueOf(1));
            sendAssert(env, 2L, BigDecimal.valueOf(2L), BigInteger.valueOf(2L));
            sendAssert(env, 2.4d, BigDecimal.valueOf(2.4d), BigInteger.valueOf(Double.valueOf(2.4).longValue()));

            BigDecimal bdOne = new BigDecimal("156.78");
            sendAssert(env, bdOne, bdOne, bdOne.toBigInteger());

            BigInteger biOne = new BigInteger("200");
            sendAssert(env, biOne, new BigDecimal(biOne), biOne);

            BigDecimal bdTwo = BigDecimal.valueOf(2).pow(500500).add(new BigDecimal("0.1"));
            sendAssert(env, bdTwo, bdTwo, bdTwo.toBigInteger());

            BigInteger biTwo = BigInteger.valueOf(2).pow(500500);
            sendAssert(env, biTwo, new BigDecimal(biTwo), biTwo);

            sendAssert(env, null, null, null);

            env.undeployAll();
        }

        private void sendAssert(RegressionEnvironment env, Object value, BigDecimal c0Expected, BigInteger c1Expected) {
            env.sendEventMap(Collections.singletonMap("value", value), "MyEvent");
            env.assertEventNew("s0", event -> {
                assertEquals(c0Expected, event.get("c0"));
                assertEquals(c1Expected, event.get("c1"));
            });
        }
    }

    private static class ExprCoreCastGeneric implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            StringBuilder schema = new StringBuilder();
            schema.append("@public @buseventtype create schema MyEvent(");
            String delimiter = "";
            for (SupportGenericColUtil.PairOfNameAndType pair : SupportGenericColUtil.NAMESANDTYPES) {
                schema.append(delimiter).append(pair.getName()).append(" java.lang.Object");
                delimiter = ",";
            }
            schema.append(");\n");

            delimiter = "";
            StringBuilder cast = new StringBuilder();
            cast.append("@name('s0') select ");
            for (SupportGenericColUtil.PairOfNameAndType pair : SupportGenericColUtil.NAMESANDTYPES) {
                cast.append(delimiter).append("cast(").append(pair.getName()).append(",").append(pair.getType()).append(") as ").append(pair.getName());
                delimiter = ",";
            }
            cast.append(" from MyEvent;\n");

            String epl = schema.toString() + cast.toString();
            env.compileDeploy(epl).addListener("s0");

            env.assertStatement("s0", statement -> SupportGenericColUtil.assertPropertyEPTypes(statement.getEventType()));

            env.sendEventMap(SupportGenericColUtil.getSampleEvent(), "MyEvent");
            env.assertEventNew("s0", event -> SupportGenericColUtil.compare(event));

            env.undeployAll();
        }

        public EnumSet<RegressionFlag> flags() {
            return EnumSet.of(RegressionFlag.SERDEREQUIRED);
        }
    }

    private static class ExprCoreCastWArray implements RegressionExecution {
        private boolean soda;

        public ExprCoreCastWArray(boolean soda) {
            this.soda = soda;
        }

        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            String epl =
                    "@public @buseventtype create schema MyEvent(arr_string java.lang.Object, arr_primitive java.lang.Object, " +
                    "arr_boxed_one java.lang.Object, arr_boxed_two java.lang.Object, arr_object java.lang.Object," +
                    "arr_2dim_primitive java.lang.Object, arr_2dim_object java.lang.Object," +
                    "arr_3dim_primitive java.lang.Object, arr_3dim_object java.lang.Object" +
                    ");\n" +
                    "@public create schema MyArrayEvent as " + MyArrayEvent.class.getName() + ";\n";
            env.compileDeploy(epl, path);

            String insert = "@name('s0') insert into MyArrayEvent select " +
                "cast(arr_string,string[]) as c0, " +
                "cast(arr_primitive,int[primitive]) as c1, " +
                "cast(arr_boxed_one,int[]) as c2, " +
                "cast(arr_boxed_two,java.lang.Integer[]) as c3, " +
                "cast(arr_object,java.lang.Object[]) as c4, " +
                "cast(arr_2dim_primitive,int[primitive][]) as c5, " +
                "cast(arr_2dim_object,java.lang.Object[][]) as c6, " +
                "cast(arr_3dim_primitive,int[primitive][][]) as c7, " +
                "cast(arr_3dim_object,java.lang.Object[][][]) as c8 " +
                "from MyEvent";
            env.compileDeploy(soda, insert, path).addListener("s0");

            env.assertStatement("s0", stmt -> {
                EventType eventType = stmt.getEventType();
                assertEquals(String[].class, eventType.getPropertyType("c0"));
                assertEquals(int[].class, eventType.getPropertyType("c1"));
                assertEquals(Integer[].class, eventType.getPropertyType("c2"));
                assertEquals(Integer[].class, eventType.getPropertyType("c3"));
                assertEquals(Object[].class, eventType.getPropertyType("c4"));
                assertEquals(int[][].class, eventType.getPropertyType("c5"));
                assertEquals(Object[][].class, eventType.getPropertyType("c6"));
                assertEquals(int[][][].class, eventType.getPropertyType("c7"));
                assertEquals(Object[][][].class, eventType.getPropertyType("c8"));
            });

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("arr_string", new String[]{"a"});
            map.put("arr_primitive", new int[]{1});
            map.put("arr_boxed_one", new Integer[]{2});
            map.put("arr_boxed_two", new Integer[]{3});
            map.put("arr_object", new SupportBean[]{new SupportBean("E1", 0)});
            map.put("arr_2dim_primitive", new int[][]{{10}});
            map.put("arr_2dim_object", new Integer[][]{{11}});
            map.put("arr_3dim_primitive", new int[][][]{{{12}}});
            map.put("arr_3dim_object", new Integer[][][]{{{13}}});

            env.sendEventMap(map, "MyEvent");

            env.assertEventNew("s0", event -> {
                MyArrayEvent mae = (MyArrayEvent) event.getUnderlying();
                assertEquals("a", mae.c0[0]);
                assertEquals(1, mae.c1[0]);
                assertEquals(2, mae.c2[0].intValue());
                assertEquals(3, mae.c3[0].intValue());
                assertEquals(new SupportBean("E1", 0), mae.c4[0]);
                assertEquals(10, mae.c5[0][0]);
                assertEquals(11, mae.c6[0][0]);
                assertEquals(12, mae.c7[0][0][0]);
                assertEquals(13, mae.c8[0][0][0]);
            });

            env.sendEventMap(Collections.emptyMap(), "MyEvent");

            env.undeployAll();
        }

        public String name() {
            return this.getClass().getSimpleName() + "{" +
                "soda=" + soda +
                '}';
        }
    }

    private static class ExprCoreCastWStaticType implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmt = "@name('s0') select " +
                "cast(anInt, int) as intVal, " +
                "cast(anDouble, double) as doubleVal, " +
                "cast(anLong, long) as longVal, " +
                "cast(anFloat, float) as floatVal, " +
                "cast(anByte, byte) as byteVal, " +
                "cast(anShort, short) as shortVal, " +
                "cast(intPrimitive, int) as intOne, " +
                "cast(intBoxed, int) as intTwo, " +
                "cast(intPrimitive, java.lang.Long) as longOne, " +
                "cast(intBoxed, long) as longTwo " +
                "from StaticTypeMapEvent";

            env.compileDeploy(stmt).addListener("s0");

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("anInt", "100");
            map.put("anDouble", "1.4E-1");
            map.put("anLong", "-10");
            map.put("anFloat", "1.001");
            map.put("anByte", "0x0A");
            map.put("anShort", "223");
            map.put("intPrimitive", 10);
            map.put("intBoxed", 11);

            env.sendEventMap(map, "StaticTypeMapEvent");
            env.assertEventNew("s0", row -> {
                assertEquals(100, row.get("intVal"));
                assertEquals(0.14d, row.get("doubleVal"));
                assertEquals(-10L, row.get("longVal"));
                assertEquals(1.001f, row.get("floatVal"));
                assertEquals((byte) 10, row.get("byteVal"));
                assertEquals((short) 223, row.get("shortVal"));
                assertEquals(10, row.get("intOne"));
                assertEquals(11, row.get("intTwo"));
                assertEquals(10L, row.get("longOne"));
                assertEquals(11L, row.get("longTwo"));
            });

            env.undeployAll();
        }
    }

    private static class ExprCoreCastSimpleMoreTypes implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "c0,c1,c2,c3,c4,c5,c6,c7,c8".split(",");
            SupportEvalBuilder builder = new SupportEvalBuilder("SupportBean")
                .expression(fields[0], "cast(intPrimitive, float)")
                .expression(fields[1], "cast(intPrimitive, short)")
                .expression(fields[2], "cast(intPrimitive, byte)")
                .expression(fields[3], "cast(theString, char)")
                .expression(fields[4], "cast(theString, boolean)")
                .expression(fields[5], "cast(intPrimitive, BigInteger)")
                .expression(fields[6], "cast(intPrimitive, BigDecimal)")
                .expression(fields[7], "cast(doublePrimitive, BigDecimal)")
                .expression(fields[8], "cast(theString, char)");

            builder.statementConsumer(stmt -> {
                assertTypes(stmt, fields, Float.class, Short.class, Byte.class, Character.class, Boolean.class, BigInteger.class, BigDecimal.class, BigDecimal.class, Character.class);
            });

            SupportBean bean = new SupportBean("true", 1);
            bean.setDoublePrimitive(1);
            builder.assertion(bean).expect(fields, 1.0f, (short) 1, (byte) 1, 't', true, BigInteger.valueOf(1), BigDecimal.valueOf(1), new BigDecimal(1d), 't');

            builder.run(env);
            env.undeployAll();
        }
    }

    private static class ExprCoreCastSimple implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "c0,c1,c2,c3,c4,c5,c6,c7".split(",");
            SupportEvalBuilder builder = new SupportEvalBuilder("SupportBean")
                .expression(fields[0], "cast(theString as string)")
                .expression(fields[1], "cast(intBoxed, int)")
                .expression(fields[2], "cast(floatBoxed, java.lang.Float)")
                .expression(fields[3], "cast(theString, java.lang.String)")
                .expression(fields[4], "cast(intPrimitive, java.lang.Integer)")
                .expression(fields[5], "cast(intPrimitive, long)")
                .expression(fields[6], "cast(intPrimitive, java.lang.Number)")
                .expression(fields[7], "cast(floatBoxed, long)");

            builder.statementConsumer(stmt -> {
                EventType type = stmt.getEventType();
                assertEquals(String.class, type.getPropertyType("c0"));
                assertEquals(Integer.class, type.getPropertyType("c1"));
                assertEquals(Float.class, type.getPropertyType("c2"));
                assertEquals(String.class, type.getPropertyType("c3"));
                assertEquals(Integer.class, type.getPropertyType("c4"));
                assertEquals(Long.class, type.getPropertyType("c5"));
                assertEquals(Number.class, type.getPropertyType("c6"));
                assertEquals(Long.class, type.getPropertyType("c7"));
            });

            SupportBean bean = new SupportBean("abc", 100);
            bean.setFloatBoxed(9.5f);
            bean.setIntBoxed(3);
            builder.assertion(bean).expect(fields, "abc", 3, 9.5f, "abc", 100, 100L, 100, 9L);

            bean = new SupportBean(null, 100);
            bean.setFloatBoxed(null);
            bean.setIntBoxed(null);
            builder.assertion(bean).expect(fields, null, null, null, null, 100, 100L, 100, null);

            builder.run(env);
            env.undeployAll();

            // test cast with chained and null
            String epl = "@name('s0') select cast(one as " + SupportBean.class.getName() + ").getTheString() as t0," +
                "cast(null, " + SupportBean.class.getName() + ") as t1" +
                " from SupportBeanObject";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new SupportBeanObject(new SupportBean("E1", 1)));
            env.assertPropsNew("s0", "t0,t1".split(","), new Object[]{"E1", null});
            env.assertStatement("s0", statement -> assertEquals(SupportBean.class, statement.getEventType().getPropertyType("t1")));

            env.undeployAll();
        }
    }

    private static class ExprCoreCastDoubleAndNullOM implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "select cast(item?,double) as t0 from SupportBeanDynRoot";

            EPStatementObjectModel model = new EPStatementObjectModel();
            model.setSelectClause(SelectClause.create().add(Expressions.cast("item?", "double"), "t0"));
            model.setFromClause(FromClause.create(FilterStream.create(SupportBeanDynRoot.class.getSimpleName())));
            model = SerializableObjectCopier.copyMayFail(model);
            assertEquals(epl, model.toEPL());

            model.setAnnotations(Collections.singletonList(AnnotationPart.nameAnnotation("s0")));
            env.compileDeploy(model).addListener("s0");

            env.assertStmtType("s0", "t0", EPTypePremade.DOUBLEBOXED.getEPType());

            env.sendEventBean(new SupportBeanDynRoot(100));
            env.assertEqualsNew("s0", "t0", 100d);

            env.sendEventBean(new SupportBeanDynRoot((byte) 2));
            env.assertEqualsNew("s0", "t0", 2d);

            env.sendEventBean(new SupportBeanDynRoot(77.7777));
            env.assertEqualsNew("s0", "t0", 77.7777d);

            env.sendEventBean(new SupportBeanDynRoot(6L));
            env.assertEqualsNew("s0", "t0", 6d);

            env.sendEventBean(new SupportBeanDynRoot(null));
            env.assertEqualsNew("s0", "t0", null);

            env.sendEventBean(new SupportBeanDynRoot("abc"));
            env.assertEqualsNew("s0", "t0", null);

            env.undeployAll();
        }
    }

    private static class ExprCoreCastDates implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();

            runAssertionDatetimeBaseTypes(env, true, milestone);

            runAssertionDatetimeJava8Types(env, milestone);

            runAssertionDatetimeRenderOutCol(env, milestone);

            runAssertionDynamicDateFormat(env);

            runAssertionDynamicDateFormatJava8(env);

            runAssertionConstantDate(env, milestone);

            runAssertionISO8601Date(env, milestone);

            runAssertionDateformatNonString(env, milestone);

            runAssertionDatetimeInvalid(env);
        }
    }

    private static class ExprCoreCastAsParse implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select cast(theString, int) as t0 from SupportBean";
            env.compileDeploy(epl).addListener("s0");
            env.assertStmtType("s0", "t0", EPTypePremade.INTEGERBOXED.getEPType());

            env.sendEventBean(new SupportBean("12", 1));
            env.assertPropsNew("s0", "t0".split(","), new Object[]{12});

            env.undeployAll();
        }
    }

    private static class ExprCoreCastInterface implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select cast(item?, " + SupportMarkerInterface.class.getName() + ") as t0, " +
                " cast(item?, " + ISupportA.class.getName() + ") as t1, " +
                " cast(item?, " + ISupportBaseAB.class.getName() + ") as t2, " +
                " cast(item?, " + ISupportBaseABImpl.class.getName() + ") as t3, " +
                " cast(item?, " + ISupportC.class.getName() + ") as t4, " +
                " cast(item?, " + ISupportD.class.getName() + ") as t5, " +
                " cast(item?, " + ISupportAImplSuperG.class.getName() + ") as t6, " +
                " cast(item?, " + ISupportAImplSuperGImplPlus.class.getName() + ") as t7 " +
                " from SupportBeanDynRoot";

            env.compileDeploy(epl).addListener("s0");

            env.assertStatement("s0", statement -> {
                EventType type = statement.getEventType();
                assertEquals(SupportMarkerInterface.class, type.getPropertyType("t0"));
                assertEquals(ISupportA.class, type.getPropertyType("t1"));
                assertEquals(ISupportBaseAB.class, type.getPropertyType("t2"));
                assertEquals(ISupportBaseABImpl.class, type.getPropertyType("t3"));
                assertEquals(ISupportC.class, type.getPropertyType("t4"));
                assertEquals(ISupportD.class, type.getPropertyType("t5"));
                assertEquals(ISupportAImplSuperG.class, type.getPropertyType("t6"));
                assertEquals(ISupportAImplSuperGImplPlus.class, type.getPropertyType("t7"));
            });

            Object beanOne = new SupportBeanDynRoot("abc");
            env.sendEventBean(new SupportBeanDynRoot(beanOne));
            env.assertEventNew("s0", theEvent -> assertResults(theEvent, new Object[]{beanOne, null, null, null, null, null, null, null}));

            Object beanTwo = new ISupportDImpl("", "", "");
            env.sendEventBean(new SupportBeanDynRoot(beanTwo));
            env.assertEventNew("s0", theEvent -> assertResults(theEvent, new Object[]{null, null, null, null, null, beanTwo, null, null}));

            Object beanThree = new ISupportBCImpl("", "", "");
            env.sendEventBean(new SupportBeanDynRoot(beanThree));
            env.assertEventNew("s0", theEvent -> assertResults(theEvent, new Object[]{null, null, beanThree, null, beanThree, null, null, null}));

            Object beanFour = new ISupportAImplSuperGImplPlus();
            env.sendEventBean(new SupportBeanDynRoot(beanFour));
            env.assertEventNew("s0", theEvent -> assertResults(theEvent, new Object[]{null, beanFour, beanFour, null, beanFour, null, beanFour, beanFour}));

            Object beanFive = new ISupportBaseABImpl("");
            env.sendEventBean(new SupportBeanDynRoot(beanFive));
            env.assertEventNew("s0", theEvent -> assertResults(theEvent, new Object[]{null, null, beanFive, beanFive, null, null, null, null}));

            env.undeployAll();
        }
    }

    private static class ExprCoreCastStringAndNullCompile implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select cast(item?,java.lang.String) as t0 from SupportBeanDynRoot";

            env.eplToModelCompileDeploy(epl).addListener("s0");

            env.assertStmtType("s0", "t0", EPTypePremade.STRING.getEPType());

            env.sendEventBean(new SupportBeanDynRoot(100));
            env.assertEqualsNew("s0", "t0", "100");

            env.sendEventBean(new SupportBeanDynRoot((byte) 2));
            env.assertEqualsNew("s0", "t0", "2");

            env.sendEventBean(new SupportBeanDynRoot(77.7777));
            env.assertEqualsNew("s0", "t0", "77.7777");

            env.sendEventBean(new SupportBeanDynRoot(6L));
            env.assertEqualsNew("s0", "t0", "6");

            env.sendEventBean(new SupportBeanDynRoot(null));
            env.assertEqualsNew("s0", "t0", null);

            env.sendEventBean(new SupportBeanDynRoot("abc"));
            env.assertEqualsNew("s0", "t0", "abc");

            env.undeployAll();
        }
    }

    private static class ExprCoreCastBoolean implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select cast(boolPrimitive as java.lang.Boolean) as t0, " +
                " cast(boolBoxed | boolPrimitive, boolean) as t1, " +
                " cast(boolBoxed, string) as t2 " +
                " from SupportBean";
            env.compileDeploy(epl).addListener("s0");

            env.assertStatement("s0", statement -> {
                EventType type = statement.getEventType();
                assertEquals(Boolean.class, type.getPropertyType("t0"));
                assertEquals(Boolean.class, type.getPropertyType("t1"));
                assertEquals(String.class, type.getPropertyType("t2"));
            });

            SupportBean bean = new SupportBean("abc", 100);
            bean.setBoolPrimitive(true);
            bean.setBoolBoxed(true);
            env.sendEventBean(bean);
            env.assertEventNew("s0", theEvent -> assertResults(theEvent, new Object[]{true, true, "true"}));

            bean = new SupportBean(null, 100);
            bean.setBoolPrimitive(false);
            bean.setBoolBoxed(false);
            env.sendEventBean(bean);
            env.assertEventNew("s0", theEvent -> assertResults(theEvent, new Object[]{false, false, "false"}));

            bean = new SupportBean(null, 100);
            bean.setBoolPrimitive(true);
            bean.setBoolBoxed(null);
            env.sendEventBean(bean);
            env.assertEventNew("s0", theEvent -> assertResults(theEvent, new Object[]{true, null, null}));

            env.undeployAll();
        }
    }

    private static void runAssertionDatetimeBaseTypes(RegressionEnvironment env, boolean soda, AtomicInteger milestone) {
        String[] fields = "c0,c1,c2,c3,c4,c5,c6,c7,c8".split(",");
        SupportEvalBuilder builder = new SupportEvalBuilder("MyDateType")
            .expression(fields[0], "cast(yyyymmdd,date,dateformat:\"yyyyMMdd\")")
            .expression(fields[1], "cast(yyyymmdd,java.util.Date,dateformat:\"yyyyMMdd\")")
            .expression(fields[2], "cast(yyyymmdd,long,dateformat:\"yyyyMMdd\")")
            .expression(fields[3], "cast(yyyymmdd,java.lang.Long,dateformat:\"yyyyMMdd\")")
            .expression(fields[4], "cast(yyyymmdd,calendar,dateformat:\"yyyyMMdd\")")
            .expression(fields[5], "cast(yyyymmdd,java.util.Calendar,dateformat:\"yyyyMMdd\")")
            .expression(fields[6], "cast(yyyymmdd,date,dateformat:\"yyyyMMdd\").get(\"month\")")
            .expression(fields[7], "cast(yyyymmdd,calendar,dateformat:\"yyyyMMdd\").get(\"month\")")
            .expression(fields[8], "cast(yyyymmdd,long,dateformat:\"yyyyMMdd\").get(\"month\")");

        SimpleDateFormat formatYYYYMMdd = new SimpleDateFormat("yyyyMMdd");
        Date dateYYMMddDate = null;
        try {
            dateYYMMddDate = formatYYYYMMdd.parse("20100510");
        } catch (ParseException e) {
            fail();
        }
        Calendar calYYMMddDate = Calendar.getInstance();
        calYYMMddDate.setTime(dateYYMMddDate);

        Map<String, Object> values = new HashMap<>();
        values.put("yyyymmdd", "20100510");
        builder.assertion(values).expect(fields, dateYYMMddDate, dateYYMMddDate, dateYYMMddDate.getTime(), dateYYMMddDate.getTime(),
            calYYMMddDate, calYYMMddDate, 4, 4, 4);

        builder.run(env);
        env.undeployAll();
    }

    private static void runAssertionDatetimeJava8Types(RegressionEnvironment env, AtomicInteger milestone) {
        String[] fields = "c0,c1,c2,c3,c4,c5,c6,c7".split(",");
        SupportEvalBuilder builder = new SupportEvalBuilder("MyDateType")
            .expression(fields[0], "cast(yyyymmdd,localdate,dateformat:\"yyyyMMdd\")")
            .expression(fields[1], "cast(yyyymmdd,java.time.LocalDate,dateformat:\"yyyyMMdd\")")
            .expression(fields[2], "cast(yyyymmddhhmmss,localdatetime,dateformat:\"yyyyMMddHHmmss\")")
            .expression(fields[3], "cast(yyyymmddhhmmss,java.time.LocalDateTime,dateformat:\"yyyyMMddHHmmss\")")
            .expression(fields[4], "cast(hhmmss,localtime,dateformat:\"HHmmss\")")
            .expression(fields[5], "cast(hhmmss,java.time.LocalTime,dateformat:\"HHmmss\")")
            .expression(fields[6], "cast(yyyymmddhhmmssvv,zoneddatetime,dateformat:\"yyyyMMddHHmmssVV\")")
            .expression(fields[7], "cast(yyyymmddhhmmssvv,java.time.ZonedDateTime,dateformat:\"yyyyMMddHHmmssVV\")");

        String yyyymmdd = "20100510";
        String yyyymmddhhmmss = "20100510141516";
        String hhmmss = "141516";
        String yyyymmddhhmmssvv = "20100510141516America/Los_Angeles";
        Map<String, Object> values = new HashMap<>();
        values.put("yyyymmdd", yyyymmdd);
        values.put("yyyymmddhhmmss", yyyymmddhhmmss);
        values.put("hhmmss", hhmmss);
        values.put("yyyymmddhhmmssvv", yyyymmddhhmmssvv);

        LocalDate resultLocalDate = LocalDate.parse(yyyymmdd, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDateTime resultLocalDateTime = LocalDateTime.parse(yyyymmddhhmmss, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        LocalTime resultLocalTime = LocalTime.parse(hhmmss, DateTimeFormatter.ofPattern("HHmmss"));
        ZonedDateTime resultZonedDateTime = ZonedDateTime.parse(yyyymmddhhmmssvv, DateTimeFormatter.ofPattern("yyyyMMddHHmmssVV"));
        builder.assertion(values).expect(fields, resultLocalDate, resultLocalDate,
            resultLocalDateTime, resultLocalDateTime,
            resultLocalTime, resultLocalTime,
            resultZonedDateTime, resultZonedDateTime);

        builder.run(env);
        env.undeployAll();
    }

    private static void runAssertionDynamicDateFormat(RegressionEnvironment env) {

        String[] fields = "c0,c1,c2".split(",");
        SupportEvalBuilder builder = new SupportEvalBuilder("SupportBean_StringAlphabetic")
            .expression(fields[0], "cast(a,date,dateformat:b)")
            .expression(fields[1], "cast(a,long,dateformat:b)")
            .expression(fields[2], "cast(a,calendar,dateformat:b)");

        assertDynamicDateFormat(builder, fields, "20100502", "yyyyMMdd");
        assertDynamicDateFormat(builder, fields, "20100502101112", "yyyyMMddhhmmss");
        assertDynamicDateFormat(builder, fields, null, "yyyyMMdd");

        builder.run(env);

        // invalid date
        try {
            env.sendEventBean(new SupportBean_StringAlphabetic("x", "yyyyMMddhhmmss"));
        } catch (EPException ex) {
            SupportMessageAssertUtil.assertMessageContains(ex, "Exception parsing date 'x' format 'yyyyMMddhhmmss': Unparseable date: \"x\"");
        }

        // invalid format
        try {
            env.sendEventBean(new SupportBean_StringAlphabetic("20100502", "UUHHYY"));
        } catch (EPException ex) {
            SupportMessageAssertUtil.assertMessageContains(ex, "Illegal pattern character 'U'");
        }

        env.undeployAll();
    }

    private static void runAssertionDynamicDateFormatJava8(RegressionEnvironment env) {
        String epl = "@buseventtype @public create schema ValuesAndFormats(" +
            "ldt string, ldtf string," +
            "ld string, ldf string," +
            "lt string, ltf string," +
            "zdt string, zdtf string)";
        RegressionPath path = new RegressionPath();
        env.compileDeploy(epl, path);

        String[] fields = "c0,c1,c2,c3".split(",");
        SupportEvalBuilder builder = new SupportEvalBuilder("ValuesAndFormats").withPath(path)
            .expression(fields[0], "cast(ldt,localdatetime,dateformat:ldtf)")
            .expression(fields[1], "cast(ld,localdate,dateformat:ldf)")
            .expression(fields[2], "cast(lt,localtime,dateformat:ltf)")
            .expression(fields[3], "cast(zdt,zoneddatetime,dateformat:zdtf)");

        Map<String, Object> event = new HashMap<>();
        event.put("ldtf", "yyyyMMddHHmmss");
        event.put("ldt", "19990102030405");
        event.put("ldf", "yyyyMMdd");
        event.put("ld", "19990102");
        event.put("ltf", "HHmmss");
        event.put("lt", "030405");
        event.put("zdtf", "yyyyMMddHHmmssVV");
        event.put("zdt", "20100510141516America/Los_Angeles");
        builder.assertion(event).expect(fields,
            LocalDateTime.parse("19990102030405", DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
            LocalDate.parse("19990102", DateTimeFormatter.ofPattern("yyyyMMdd")),
            LocalTime.parse("030405", DateTimeFormatter.ofPattern("HHmmss")),
            ZonedDateTime.parse("20100510141516America/Los_Angeles", DateTimeFormatter.ofPattern("yyyyMMddHHmmssVV"))
        );

        builder.run(env);
        env.undeployAll();
    }

    private static void runAssertionDatetimeRenderOutCol(RegressionEnvironment env, AtomicInteger milestone) {
        String epl = "@name('s0') select cast(yyyymmdd,date,dateformat:\"yyyyMMdd\") from MyDateType";
        env.compileDeploy(epl).addListener("s0").milestone(milestone.getAndIncrement());
        env.assertStatement("s0", statement -> assertEquals("cast(yyyymmdd,date,dateformat:\"yyyyMMdd\")", statement.getEventType().getPropertyNames()[0]));
        env.undeployAll();
    }

    private static void assertDynamicDateFormat(SupportEvalBuilder builder, String[] fields, String date, String format) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date expectedDate = null;
        try {
            expectedDate = date == null ? null : dateFormat.parse(date);
        } catch (ParseException e) {
            fail(e.getMessage());
        }
        Calendar cal = null;
        Long theLong = null;
        if (expectedDate != null) {
            cal = Calendar.getInstance();
            cal.setTime(expectedDate);
            theLong = expectedDate.getTime();
        }

        builder.assertion(new SupportBean_StringAlphabetic(date, format)).expect(fields, expectedDate, theLong, cal);
    }

    private static void runAssertionConstantDate(RegressionEnvironment env, AtomicInteger milestone) {
        String[] fields = "c0".split(",");
        SupportEvalBuilder builder = new SupportEvalBuilder("SupportBean")
            .expressions(fields, "cast('20030201',date,dateformat:\"yyyyMMdd\")");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date expectedDate = null;
        try {
            expectedDate = dateFormat.parse("20030201");
        } catch (ParseException e) {
            fail(e.getMessage());
        }
        builder.assertion(new SupportBean("E1", 1)).expect(fields, expectedDate);

        builder.run(env);
        env.undeployAll();
    }

    private static void runAssertionISO8601Date(RegressionEnvironment env, AtomicInteger milestone) {
        String epl = "@name('s0') select " +
            "cast('1997-07-16T19:20:30Z',calendar,dateformat:'iso') as c0," +
            "cast('1997-07-16T19:20:30+01:00',calendar,dateformat:'iso') as c1," +
            "cast('1997-07-16T19:20:30',calendar,dateformat:'iso') as c2," +
            "cast('1997-07-16T19:20:30.45Z',calendar,dateformat:'iso') as c3," +
            "cast('1997-07-16T19:20:30.45+01:00',calendar,dateformat:'iso') as c4," +
            "cast('1997-07-16T19:20:30.45',calendar,dateformat:'iso') as c5," +
            "cast('1997-07-16T19:20:30.45',long,dateformat:'iso') as c6," +
            "cast('1997-07-16T19:20:30.45',date,dateformat:'iso') as c7," +
            "cast(theString,calendar,dateformat:'iso') as c8," +
            "cast(theString,long,dateformat:'iso') as c9," +
            "cast(theString,date,dateformat:'iso') as c10," +
            "cast('1997-07-16T19:20:30.45',localdatetime,dateformat:'iso') as c11," +
            "cast('1997-07-16T19:20:30+01:00',zoneddatetime,dateformat:'iso') as c12," +
            "cast('1997-07-16',localdate,dateformat:'iso') as c13," +
            "cast('19:20:30',localtime,dateformat:'iso') as c14" +
            " from SupportBean";
        env.compileDeployAddListenerMile(epl, "s0", milestone.getAndIncrement());

        env.sendEventBean(new SupportBean());
        env.assertEventNew("s0", event -> {
            SupportDateTimeUtil.compareDate((Calendar) event.get("c0"), 1997, 6, 16, 19, 20, 30, 0, "GMT+00:00");
            SupportDateTimeUtil.compareDate((Calendar) event.get("c1"), 1997, 6, 16, 19, 20, 30, 0, "GMT+01:00");
            SupportDateTimeUtil.compareDate((Calendar) event.get("c2"), 1997, 6, 16, 19, 20, 30, 0, TimeZone.getDefault().getID());
            SupportDateTimeUtil.compareDate((Calendar) event.get("c3"), 1997, 6, 16, 19, 20, 30, 450, "GMT+00:00");
            SupportDateTimeUtil.compareDate((Calendar) event.get("c4"), 1997, 6, 16, 19, 20, 30, 450, "GMT+01:00");
            SupportDateTimeUtil.compareDate((Calendar) event.get("c5"), 1997, 6, 16, 19, 20, 30, 450, TimeZone.getDefault().getID());
            assertEquals(Long.class, event.get("c6").getClass());
            assertEquals(Date.class, event.get("c7").getClass());
            for (String prop : "c8,c9,c10".split(",")) {
                assertNull(event.get(prop));
            }
            assertEquals(LocalDateTime.parse("1997-07-16T19:20:30.45", DateTimeFormatter.ISO_DATE_TIME), event.get("c11"));
            assertEquals(ZonedDateTime.parse("1997-07-16T19:20:30+01:00", DateTimeFormatter.ISO_ZONED_DATE_TIME), event.get("c12"));
            assertEquals(LocalDate.parse("1997-07-16", DateTimeFormatter.ISO_DATE), event.get("c13"));
            assertEquals(LocalTime.parse("19:20:30", DateTimeFormatter.ISO_TIME), event.get("c14"));
        });

        env.undeployAll();
    }

    private static void runAssertionDateformatNonString(RegressionEnvironment env, AtomicInteger milestone) {
        SupportDateTime sdt = SupportDateTime.make("2002-05-30T09:00:00.000");
        String sdfDate = SimpleDateFormat.getInstance().format(sdt.getUtildate());
        String ldtDate = sdt.getLocaldate().format(DateTimeFormatter.ISO_DATE_TIME);
        String zdtDate = sdt.getZoneddate().format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        String ldDate = sdt.getLocaldate().toLocalDate().format(DateTimeFormatter.ISO_DATE);
        String ltDate = sdt.getLocaldate().toLocalTime().format(DateTimeFormatter.ISO_TIME);

        String epl = "@name('s0') select " +
            "cast('" + sdfDate + "',date,dateformat:SimpleDateFormat.getInstance()) as c0," +
            "cast('" + sdfDate + "',calendar,dateformat:SimpleDateFormat.getInstance()) as c1," +
            "cast('" + sdfDate + "',long,dateformat:SimpleDateFormat.getInstance()) as c2," +
            "cast('" + ldtDate + "',localdatetime,dateformat:java.time.format.DateTimeFormatter.ISO_DATE_TIME) as c3," +
            "cast('" + zdtDate + "',zoneddatetime,dateformat:java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME) as c4," +
            "cast('" + ldDate + "',localdate,dateformat:java.time.format.DateTimeFormatter.ISO_DATE) as c5," +
            "cast('" + ltDate + "',localtime,dateformat:java.time.format.DateTimeFormatter.ISO_TIME) as c6" +
            " from SupportBean";
        env.compileDeployAddListenerMile(epl, "s0", milestone.getAndIncrement());

        env.sendEventBean(new SupportBean());
        env.assertPropsNew("s0", "c0,c1,c2,c3,c4,c5,c6".split(","), new Object[]{sdt.getUtildate(), sdt.getCaldate(),
            sdt.getLongdate(), sdt.getLocaldate(), sdt.getZoneddate(), sdt.getLocaldate().toLocalDate(), sdt.getLocaldate().toLocalTime()});

        env.undeployAll();
    }

    private static void runAssertionDatetimeInvalid(RegressionEnvironment env) {
        // not a valid named parameter
        env.tryInvalidCompile("select cast(theString, date, x:1) from SupportBean",
            "Failed to validate select-clause expression 'cast(theString,date,x:1)': Unexpected named parameter 'x', expecting any of the following: [dateformat]");

        // invalid date format
        env.tryInvalidCompile("select cast(theString, date, dateformat:'BBBBMMDD') from SupportBean",
            "Failed to validate select-clause expression 'cast(theString,date,dateformat:\"BBB...(42 chars)': Invalid date format 'BBBBMMDD' (as obtained from new SimpleDateFormat): Illegal pattern character 'B'");
        env.tryInvalidCompile("select cast(theString, date, dateformat:1) from SupportBean",
            "Failed to validate select-clause expression 'cast(theString,date,dateformat:1)': Failed to validate named parameter 'dateformat', expected a single expression returning any of the following types: string,DateFormat,DateTimeFormatter");

        // invalid input
        env.tryInvalidCompile("select cast(intPrimitive, date, dateformat:'yyyyMMdd') from SupportBean",
            "Failed to validate select-clause expression 'cast(intPrimitive,date,dateformat:\"...(45 chars)': Use of the 'dateformat' named parameter requires a string-type input");

        // invalid target
        env.tryInvalidCompile("select cast(theString, int, dateformat:'yyyyMMdd') from SupportBean",
            "Failed to validate select-clause expression 'cast(theString,int,dateformat:\"yyyy...(41 chars)': Use of the 'dateformat' named parameter requires a target type of calendar, date, long, localdatetime, localdate, localtime or zoneddatetime");

        // invalid parser
        env.tryInvalidCompile("select cast('xx', date, dateformat:java.time.format.DateTimeFormatter.ofPattern(\"yyyyMMddHHmmssVV\")) from SupportBean",
            "Failed to validate select-clause expression 'cast(\"xx\",date,dateformat:java.time...(91 chars)': Invalid format, expected string-format or DateFormat but received java.time.format.DateTimeFormatter");
        env.tryInvalidCompile("select cast('xx', localdatetime, dateformat:SimpleDateFormat.getInstance()) from SupportBean",
            "Failed to validate select-clause expression 'cast(\"xx\",localdatetime,dateformat:...(66 chars)': Invalid format, expected string-format or DateTimeFormatter but received java.text.DateFormat");
    }

    private static void assertResults(EventBean theEvent, Object[] result) {
        for (int i = 0; i < result.length; i++) {
            assertEquals("failed for index " + i, result[i], theEvent.get("t" + i));
        }
    }

    private static void assertTypes(EPStatement stmt, String[] fields, Class... types) {
        for (int i = 0; i < fields.length; i++) {
            assertEquals("failed for " + i, types[i], stmt.getEventType().getPropertyType(fields[i]));
        }
    }

    public final static class MyArrayEvent {
        private final String[] c0;
        private final int[] c1;
        private final Integer[] c2;
        private final Integer[] c3;
        private final Object[] c4;
        private final int[][] c5;
        private final Object[][] c6;
        private final int[][][] c7;
        private final Object[][][] c8;

        public MyArrayEvent(String[] c0, int[] c1, Integer[] c2, Integer[] c3, Object[] c4, int[][] c5, Object[][] c6, int[][][] c7, Object[][][] c8) {
            this.c0 = c0;
            this.c1 = c1;
            this.c2 = c2;
            this.c3 = c3;
            this.c4 = c4;
            this.c5 = c5;
            this.c6 = c6;
            this.c7 = c7;
            this.c8 = c8;
        }

        public String[] getC0() {
            return c0;
        }

        public int[] getC1() {
            return c1;
        }

        public Integer[] getC2() {
            return c2;
        }

        public Integer[] getC3() {
            return c3;
        }

        public Object[] getC4() {
            return c4;
        }

        public int[][] getC5() {
            return c5;
        }

        public Object[][] getC6() {
            return c6;
        }

        public int[][][] getC7() {
            return c7;
        }

        public Object[][][] getC8() {
            return c8;
        }
    }
}
