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
package com.espertech.esper.common.internal.context.aifactory.core;

import com.espertech.esper.common.internal.collection.Pair;
import com.espertech.esper.common.internal.epl.agg.core.AggregationService;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.common.internal.epl.resultset.core.ResultSetProcessor;
import com.espertech.esper.common.internal.epl.resultset.core.ResultSetProcessorFactoryProvider;
import com.espertech.esper.common.internal.epl.resultset.order.OrderByProcessor;

public class StatementAgentInstanceFactoryUtil {
    public static Pair<ResultSetProcessor, AggregationService> startResultSetAndAggregation(ResultSetProcessorFactoryProvider resultSetProcessorPrototype, ExprEvaluatorContext exprEvaluatorContext, boolean isSubquery, Integer subqueryNumber) {
        AggregationService aggregationService = null;
        if (resultSetProcessorPrototype.getAggregationServiceFactory() != null) {
            aggregationService = resultSetProcessorPrototype.getAggregationServiceFactory().makeService(exprEvaluatorContext, null, subqueryNumber, null);
        }

        OrderByProcessor orderByProcessor = null;
        if (resultSetProcessorPrototype.getOrderByProcessorFactory() != null) {
            orderByProcessor = resultSetProcessorPrototype.getOrderByProcessorFactory().instantiate(exprEvaluatorContext);
        }

        ResultSetProcessor resultSetProcessor = resultSetProcessorPrototype.getResultSetProcessorFactory().instantiate(orderByProcessor, aggregationService, exprEvaluatorContext);

        return new Pair<>(resultSetProcessor, aggregationService);
    }
}
