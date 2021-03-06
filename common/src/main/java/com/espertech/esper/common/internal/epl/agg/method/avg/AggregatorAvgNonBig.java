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
package com.espertech.esper.common.internal.epl.agg.method.avg;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.epl.agg.method.sum.AggregatorSumNonBig;
import com.espertech.esper.common.internal.epl.expression.core.ExprNode;
import com.espertech.esper.common.internal.serde.compiletime.resolve.DataInputOutputSerdeForge;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

/**
 * Average that generates double-typed numbers.
 */
public class AggregatorAvgNonBig extends AggregatorSumNonBig {

    public AggregatorAvgNonBig(EPTypeClass optionalDistinctValueType, DataInputOutputSerdeForge optionalDistinctSerde, boolean hasFilter, ExprNode optionalFilter, EPTypeClass sumType) {
        super(optionalDistinctValueType, optionalDistinctSerde, hasFilter, optionalFilter, sumType);
    }

    @Override
    public void getValueCodegen(CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock()
                .ifCondition(equalsIdentity(cnt, constant(0)))
                .blockReturn(constantNull());
        if (sumType.getType() == double.class) {
            method.getBlock().methodReturn(op(sum, "/", cnt));
        } else {
            method.getBlock().methodReturn(op(sum, "/", cast(EPTypePremade.DOUBLEPRIMITIVE.getEPType(), cnt)));
        }
    }
}
