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
package com.espertech.esper.common.internal.epl.pattern.every;

import com.espertech.esper.common.client.annotation.AppliesTo;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.compile.util.CallbackAttribution;
import com.espertech.esper.common.internal.compile.stage2.FilterSpecTracked;
import com.espertech.esper.common.internal.context.aifactory.core.SAIFFInitializeSymbol;
import com.espertech.esper.common.internal.epl.pattern.core.EvalForgeNodeBase;
import com.espertech.esper.common.internal.epl.pattern.core.PatternExpressionPrecedenceEnum;
import com.espertech.esper.common.internal.schedule.ScheduleHandleTracked;

import java.io.StringWriter;
import java.util.List;
import java.util.function.Function;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.localMethod;
import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.ref;

/**
 * This class represents an 'every' operator in the evaluation tree representing an event expression.
 */
public class EvalEveryForgeNode extends EvalForgeNodeBase {

    public EvalEveryForgeNode(boolean attachPatternText) {
        super(attachPatternText);
    }

    public final String toString() {
        return "EvalEveryNode children=" + this.getChildNodes().size();
    }

    public void toPrecedenceFreeEPL(StringWriter writer) {
        writer.append("every ");
        this.getChildNodes().get(0).toEPL(writer, getPrecedence());
    }

    public PatternExpressionPrecedenceEnum getPrecedence() {
        return PatternExpressionPrecedenceEnum.UNARY;
    }

    protected EPTypeClass typeOfFactory() {
        return EvalEveryFactoryNode.EPTYPE;
    }

    protected String nameOfFactory() {
        return "every";
    }

    protected void inlineCodegen(CodegenMethod method, SAIFFInitializeSymbol symbols, CodegenClassScope classScope) {
        method.getBlock()
                .exprDotMethod(ref("node"), "setChildNode", localMethod(getChildNodes().get(0).makeCodegen(method, symbols, classScope)));
    }

    public void collectSelfFilterAndSchedule(Function<Short, CallbackAttribution> callbackAttribution, List<FilterSpecTracked> filters, List<ScheduleHandleTracked> schedules) {
    }

    public AppliesTo appliesTo() {
        return AppliesTo.PATTERN_EVERY;
    }
}
