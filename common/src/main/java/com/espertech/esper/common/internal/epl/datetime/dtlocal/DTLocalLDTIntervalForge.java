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
package com.espertech.esper.common.internal.epl.datetime.dtlocal;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRef;
import com.espertech.esper.common.internal.epl.datetime.interval.IntervalForge;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;

import java.util.TimeZone;

public class DTLocalLDTIntervalForge extends DTLocalForgeIntervalBase {

    public DTLocalLDTIntervalForge(IntervalForge intervalForge) {
        super(intervalForge);
    }

    public DTLocalEvaluator getDTEvaluator() {
        return new DTLocalLDTIntervalEval(intervalForge.getOp(), TimeZone.getDefault());
    }

    public DTLocalEvaluatorIntervalComp makeEvaluatorComp() {
        return new DTLocalLDTIntervalEval(intervalForge.getOp(), TimeZone.getDefault());
    }

    public CodegenExpression codegen(CodegenExpression inner, EPTypeClass innerType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        return DTLocalLDTIntervalEval.codegen(this, inner, codegenMethodScope, exprSymbol, codegenClassScope);
    }

    public CodegenExpression codegen(CodegenExpressionRef start, CodegenExpressionRef end, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        return DTLocalLDTIntervalEval.codegen(this, start, end, codegenMethodScope, exprSymbol, codegenClassScope);
    }
}
