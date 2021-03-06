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
package com.espertech.esper.common.internal.event.json.parser.forge;

import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.ref;

public class JsonDelegateRefs {
    private final static CodegenExpression BASEHANDLER = ref("baseHandler");

    public final static JsonDelegateRefs INSTANCE = new JsonDelegateRefs(BASEHANDLER);

    private final CodegenExpression baseHandler;

    private JsonDelegateRefs(CodegenExpression baseHandler) {
        this.baseHandler = baseHandler;
    }

    public CodegenExpression getBaseHandler() {
        return baseHandler;
    }

    public CodegenExpression getThis() {
        return ref("this");
    }
}
