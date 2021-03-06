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
package com.espertech.esper.common.internal.epl.expression.ops;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluator;
import com.espertech.esper.common.internal.epl.expression.core.ExprForge;
import com.espertech.esper.common.internal.epl.expression.core.ExprForgeConstantType;
import com.espertech.esper.common.internal.epl.expression.core.ExprNodeRenderable;
import com.espertech.esper.common.internal.event.bean.manufacturer.InstanceManufacturerFactory;

public class ExprNewInstanceNodeNonArrayForge implements ExprForge {

    private final ExprNewInstanceNode parent;
    private final EPTypeClass targetClass;
    private final InstanceManufacturerFactory manufacturerFactory;

    public ExprNewInstanceNodeNonArrayForge(ExprNewInstanceNode parent, EPTypeClass targetClass, InstanceManufacturerFactory manufacturerFactory) {
        this.parent = parent;
        this.targetClass = targetClass;
        this.manufacturerFactory = manufacturerFactory;
    }

    public ExprEvaluator getExprEvaluator() {
        return new ExprNewInstanceNodeNonArrayForgeEval(manufacturerFactory.makeEvaluator());
    }

    public CodegenExpression evaluateCodegen(EPTypeClass requiredType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        return manufacturerFactory.codegen(this, codegenMethodScope, exprSymbol, codegenClassScope);
    }

    public EPTypeClass getEvaluationType() {
        return targetClass;
    }

    public ExprNodeRenderable getForgeRenderable() {
        return parent;
    }

    public ExprForgeConstantType getForgeConstantType() {
        return ExprForgeConstantType.NONCONST;
    }
}
