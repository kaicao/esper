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
package com.espertech.esper.common.internal.epl.rowrecog.core;

import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.annotation.AppliesTo;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRef;
import com.espertech.esper.common.internal.context.aifactory.core.SAIFFInitializeSymbol;
import com.espertech.esper.common.internal.epl.expression.core.ExprNode;
import com.espertech.esper.common.internal.schedule.ScheduleHandleCallbackProvider;
import com.espertech.esper.common.internal.view.core.*;

import java.util.List;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.constant;

/**
 * View factory for match-recognize view.
 */
public class RowRecogNFAViewFactoryForge extends ViewFactoryForgeBase implements ScheduleHandleCallbackProvider {

    private RowRecogDescForge rowRecogDescForge;
    private int scheduleCallbackId = -1;

    public RowRecogNFAViewFactoryForge(RowRecogDescForge rowRecogDescForge) {
        this.rowRecogDescForge = rowRecogDescForge;
        this.eventType = rowRecogDescForge.getRowEventType();
    }

    public void setViewParameters(List<ExprNode> parameters, ViewForgeEnv viewForgeEnv, int streamNumber) throws ViewParameterException {
        // no action
    }

    public void attachValidate(EventType parentEventType, ViewForgeEnv viewForgeEnv) throws ViewParameterException {
        // no action
    }

    protected EPTypeClass typeOfFactory() {
        return RowRecogNFAViewFactory.EPTYPE;
    }

    protected String factoryMethod() {
        return "rowRecog";
    }

    protected void assign(CodegenMethod method, CodegenExpressionRef factory, SAIFFInitializeSymbol symbols, CodegenClassScope classScope) {
        if (scheduleCallbackId == -1) {
            throw new IllegalStateException("No schedule callback id");
        }
        method.getBlock()
                .exprDotMethod(factory, "setDesc", rowRecogDescForge.make(method, symbols, classScope))
                .exprDotMethod(factory, "setScheduleCallbackId", constant(scheduleCallbackId));
    }

    public String getViewName() {
        return "match-recognize";
    }

    public void accept(ViewForgeVisitor visitor) {
        visitor.visit(this);
    }

    public void setScheduleCallbackId(int id) {
        this.scheduleCallbackId = id;
    }

    public int getScheduleCallbackId() {
        return scheduleCallbackId;
    }

    public AppliesTo appliesTo() {
        return AppliesTo.WINDOW_ROWRECOG;
    }

    public <T> T accept(ViewFactoryForgeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
