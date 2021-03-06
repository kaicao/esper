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
package com.espertech.esper.common.internal.epl.expression.declared.compiletime;

import com.espertech.esper.common.internal.compile.stage1.spec.ExpressionDeclItem;
import com.espertech.esper.common.internal.compile.stage1.spec.ExpressionScriptProvided;

public class ExprDeclaredCompileTimeResolverEmpty implements ExprDeclaredCompileTimeResolver {
    public final static ExprDeclaredCompileTimeResolverEmpty INSTANCE = new ExprDeclaredCompileTimeResolverEmpty();

    private ExprDeclaredCompileTimeResolverEmpty() {
    }

    public ExpressionDeclItem resolve(String name) {
        return null;
    }

    public ExpressionScriptProvided resolveScript(String name, int numParameters) {
        return null;
    }
}
