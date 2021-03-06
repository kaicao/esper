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
package com.espertech.esper.common.internal.epl.agg.groupall;

import com.espertech.esper.common.client.annotation.AppliesTo;
import com.espertech.esper.common.client.serde.DataInputOutputSerde;
import com.espertech.esper.common.client.util.StateMgmtSetting;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.core.CodegenCtor;
import com.espertech.esper.common.internal.bytecodemodel.core.CodegenNamedMethods;
import com.espertech.esper.common.internal.bytecodemodel.core.CodegenTypedParam;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionMember;
import com.espertech.esper.common.internal.context.module.EPStatementInitServices;
import com.espertech.esper.common.internal.epl.agg.core.*;
import com.espertech.esper.common.internal.fabric.FabricTypeCollector;

import java.util.List;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;
import static com.espertech.esper.common.internal.context.module.EPStatementInitServices.GETAGGREGATIONSERVICEFACTORYSERVICE;
import static com.espertech.esper.common.internal.epl.agg.core.AggregationServiceCodegenNames.REF_AGGVISITOR;
import static com.espertech.esper.common.internal.epl.agg.core.AggregationServiceCodegenNames.REF_VCOL;
import static com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenNames.*;
import static com.espertech.esper.common.internal.metrics.instrumentation.InstrumentationCode.instblock;

/**
 * Aggregation service for use when only first/last/window aggregation functions are used an none other.
 */
public class AggregationServiceGroupAllForge implements AggregationServiceFactoryForgeWMethodGen {
    private final static CodegenExpressionMember MEMBER_ROW = member("row");

    protected final AggregationRowStateForgeDesc rowStateDesc;
    private StateMgmtSetting stateMgmtSetting;

    public AggregationServiceGroupAllForge(AggregationRowStateForgeDesc rowStateDesc) {
        this.rowStateDesc = rowStateDesc;
    }

    public AggregationRowStateForgeDesc getRowStateDesc() {
        return rowStateDesc;
    }

    public AppliesTo appliesTo() {
        return AppliesTo.AGGREGATION_UNGROUPED;
    }

    public void setStateMgmtSetting(StateMgmtSetting stateMgmtSetting) {
        this.stateMgmtSetting = stateMgmtSetting;
    }

    public void appendRowFabricType(FabricTypeCollector fabricTypeCollector) {
    }

    public AggregationCodegenRowLevelDesc getRowLevelDesc() {
        return AggregationCodegenRowLevelDesc.fromTopOnly(rowStateDesc);
    }

    public void providerCodegen(CodegenMethod method, CodegenClassScope classScope, AggregationClassNames classNames) {
        method.getBlock()
                .declareVar(AggregationRowFactory.EPTYPE, "rowFactory", CodegenExpressionBuilder.newInstance(classNames.getRowFactoryTop(), ref("this")))
                .declareVar(DataInputOutputSerde.EPTYPE, "rowSerde", CodegenExpressionBuilder.newInstance(classNames.getRowSerdeTop(), ref("this")))
                .declareVar(AggregationServiceFactory.EPTYPE, "svcFactory", CodegenExpressionBuilder.newInstance(classNames.getServiceFactory(), ref("this")))
                .methodReturn(exprDotMethodChain(EPStatementInitServices.REF).add(GETAGGREGATIONSERVICEFACTORYSERVICE).add("groupAll", ref("svcFactory"), ref("rowFactory"), rowStateDesc.getUseFlags().toExpression(), ref("rowSerde"), stateMgmtSetting.toExpression()));
    }

    public void makeServiceCodegen(CodegenMethod method, CodegenClassScope classScope, AggregationClassNames classNames) {
        method.getBlock().methodReturn(CodegenExpressionBuilder.newInstance(classNames.getService(), ref("o")));
    }

    public void rowCtorCodegen(AggregationRowCtorDesc rowCtorDesc) {
        AggregationServiceCodegenUtil.generateIncidentals(false, false, rowCtorDesc);
    }

    public void ctorCodegen(CodegenCtor ctor, List<CodegenTypedParam> explicitMembers, CodegenClassScope classScope, AggregationClassNames classNames) {
        explicitMembers.add(new CodegenTypedParam(classNames.getRowTop(), MEMBER_ROW.getRef()));
        ctor.getBlock().assignRef(MEMBER_ROW, CodegenExpressionBuilder.newInstance(classNames.getRowTop()));
    }

    public void getValueCodegen(CodegenMethod method, CodegenClassScope classScope, CodegenNamedMethods namedMethods) {
        method.getBlock().methodReturn(exprDotMethod(MEMBER_ROW, "getValue", REF_VCOL, REF_EPS, REF_ISNEWDATA, REF_EXPREVALCONTEXT));
    }

    public void getEventBeanCodegen(CodegenMethod method, CodegenClassScope classScope, CodegenNamedMethods namedMethods) {
        method.getBlock().methodReturn(exprDotMethod(MEMBER_ROW, "getEventBean", REF_VCOL, REF_EPS, REF_ISNEWDATA, REF_EXPREVALCONTEXT));
    }

    public void applyEnterCodegen(CodegenMethod method, CodegenClassScope classScope, CodegenNamedMethods namedMethods, AggregationClassNames classNames) {
        method.getBlock()
                .apply(instblock(classScope, "qAggregationUngroupedApplyEnterLeave", constantTrue(), constant(rowStateDesc.getNumMethods()), constant(rowStateDesc.getNumAccess())))
                .exprDotMethod(MEMBER_ROW, "applyEnter", REF_EPS, REF_EXPREVALCONTEXT)
                .apply(instblock(classScope, "aAggregationUngroupedApplyEnterLeave", constantTrue()));
    }

    public void applyLeaveCodegen(CodegenMethod method, CodegenClassScope classScope, CodegenNamedMethods namedMethods, AggregationClassNames classNames) {
        method.getBlock()
                .apply(instblock(classScope, "qAggregationUngroupedApplyEnterLeave", constantFalse(), constant(rowStateDesc.getNumMethods()), constant(rowStateDesc.getNumAccess())))
                .exprDotMethod(MEMBER_ROW, "applyLeave", REF_EPS, REF_EXPREVALCONTEXT)
                .apply(instblock(classScope, "aAggregationUngroupedApplyEnterLeave", constantFalse()));
    }

    public void stopMethodCodegen(AggregationServiceFactoryForgeWMethodGen forge, CodegenMethod method) {
        // no code
    }

    public void setRemovedCallbackCodegen(CodegenMethod method) {
        // no code
    }

    public void setCurrentAccessCodegen(CodegenMethod method, CodegenClassScope classScope, AggregationClassNames classNames) {
        // no code
    }

    public void clearResultsCodegen(CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().exprDotMethod(MEMBER_ROW, "clear");
    }

    public void getCollectionScalarCodegen(CodegenMethod method, CodegenClassScope classScope, CodegenNamedMethods namedMethods) {
        method.getBlock().methodReturn(exprDotMethod(MEMBER_ROW, "getCollectionScalar", REF_VCOL, REF_EPS, REF_ISNEWDATA, REF_EXPREVALCONTEXT));
    }

    public void getCollectionOfEventsCodegen(CodegenMethod method, CodegenClassScope classScope, CodegenNamedMethods namedMethods) {
        method.getBlock().methodReturn(exprDotMethod(MEMBER_ROW, "getCollectionOfEvents", REF_VCOL, REF_EPS, REF_ISNEWDATA, REF_EXPREVALCONTEXT));
    }

    public void acceptCodegen(CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().exprDotMethod(REF_AGGVISITOR, "visitAggregations", constant(1), MEMBER_ROW);
    }

    public void getGroupKeysCodegen(CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().methodReturn(constantNull());
    }

    public void getGroupKeyCodegen(CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().methodReturn(constantNull());
    }

    public void acceptGroupDetailCodegen(CodegenMethod method, CodegenClassScope classScope) {
        // not implemented
    }

    public void isGroupedCodegen(CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().methodReturn(constantFalse());
    }

    public void rowWriteMethodCodegen(CodegenMethod method, int level) {
    }

    public void rowReadMethodCodegen(CodegenMethod method, int level) {
    }

    public void getRowCodegen(CodegenMethod method, CodegenClassScope classScope, CodegenNamedMethods namedMethods) {
        method.getBlock().methodReturn(MEMBER_ROW);
    }

    public <T> T accept(AggregationServiceFactoryForgeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}