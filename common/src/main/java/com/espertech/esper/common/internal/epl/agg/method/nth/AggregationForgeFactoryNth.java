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
package com.espertech.esper.common.internal.epl.agg.method.nth;

import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.type.EPType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.epl.agg.core.AggregationPortableValidation;
import com.espertech.esper.common.internal.epl.agg.method.core.AggregationForgeFactoryBase;
import com.espertech.esper.common.internal.epl.agg.method.core.AggregatorMethod;
import com.espertech.esper.common.internal.epl.expression.agg.base.ExprAggregateNodeBase;
import com.espertech.esper.common.internal.epl.expression.agg.method.ExprMethodAggUtil;
import com.espertech.esper.common.internal.epl.expression.agg.method.ExprNthAggNode;
import com.espertech.esper.common.internal.epl.expression.core.ExprForge;
import com.espertech.esper.common.internal.epl.expression.core.ExprValidationException;
import com.espertech.esper.common.internal.serde.compiletime.resolve.DataInputOutputSerdeForge;

public class AggregationForgeFactoryNth extends AggregationForgeFactoryBase {
    protected final ExprNthAggNode parent;
    protected final EPTypeClass childType;
    protected final DataInputOutputSerdeForge serde;
    protected final DataInputOutputSerdeForge distinctSerde;
    protected final int size;
    protected final AggregatorNth aggregator;

    public AggregationForgeFactoryNth(ExprNthAggNode parent, EPTypeClass childType, DataInputOutputSerdeForge serde, DataInputOutputSerdeForge distinctSerde, int size) {
        this.parent = parent;
        this.childType = childType;
        this.serde = serde;
        this.distinctSerde = distinctSerde;
        this.size = size;

        EPTypeClass distinctValueType = !parent.isDistinct() ? null : childType;
        aggregator = new AggregatorNth(this, distinctValueType, distinctSerde, false, parent.getOptionalFilter());
    }

    public EPType getResultType() {
        return childType;
    }

    public AggregatorMethod getAggregator() {
        return aggregator;
    }

    public ExprNthAggNode getParent() {
        return parent;
    }

    public ExprAggregateNodeBase getAggregationExpression() {
        return parent;
    }

    public AggregationPortableValidation getAggregationPortableValidation() {
        return new AggregationPortableValidationNth(parent.isDistinct(), parent.getOptionalFilter() != null, childType, size);
    }

    public ExprForge[] getMethodAggregationForge(boolean join, EventType[] typesPerStream) throws ExprValidationException {
        return ExprMethodAggUtil.getDefaultForges(parent.getPositionalParams(), join, typesPerStream);
    }

    public EPTypeClass getChildType() {
        return childType;
    }

    public int getSizeOfBuf() {
        return size + 1;
    }
}