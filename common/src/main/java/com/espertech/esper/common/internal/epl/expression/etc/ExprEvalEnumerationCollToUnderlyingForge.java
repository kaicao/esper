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
package com.espertech.esper.common.internal.epl.expression.etc;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypeClassParameterized;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.*;
import com.espertech.esper.common.internal.event.core.EventBeanUtility;

import java.util.Collection;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

public class ExprEvalEnumerationCollToUnderlyingForge implements ExprForge {
    protected final ExprEnumerationForge enumerationForge;
    private final EventType targetType;

    public ExprEvalEnumerationCollToUnderlyingForge(ExprEnumerationForge enumerationForge, EventType targetType) {
        this.enumerationForge = enumerationForge;
        this.targetType = targetType;
    }

    public ExprEvaluator getExprEvaluator() {
        throw ExprNodeUtilityMake.makeUnsupportedCompileTime();
    }

    public CodegenExpression evaluateCodegen(EPTypeClass requiredType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        CodegenMethod methodNode = codegenMethodScope.makeChild(getEvaluationType(), ExprEvalEnumerationCollToUnderlyingForge.class, codegenClassScope);
        methodNode.getBlock()
                .declareVar(EPTypeClassParameterized.from(Collection.class, EventBean.class), "events", enumerationForge.evaluateGetROCollectionEventsCodegen(methodNode, exprSymbol, codegenClassScope))
                .ifRefNullReturnNull("events")
                .ifCondition(equalsIdentity(exprDotMethod(ref("events"), "size"), constant(0)))
                .blockReturn(constantNull())
                .declareVar(EventBean.EPTYPE, "event", staticMethod(EventBeanUtility.class, "getNonemptyFirstEvent", ref("events")))
                .methodReturn(cast(getEvaluationType(), exprDotUnderlying(ref("event"))));
        return localMethod(methodNode);
    }

    public EPTypeClass getEvaluationType() {
        return targetType.getUnderlyingEPType();
    }

    public ExprNodeRenderable getForgeRenderable() {
        return enumerationForge.getForgeRenderable();
    }

    public ExprForgeConstantType getForgeConstantType() {
        return ExprForgeConstantType.NONCONST;
    }
}
