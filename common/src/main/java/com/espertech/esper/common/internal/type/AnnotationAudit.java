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
package com.espertech.esper.common.internal.type;

import com.espertech.esper.common.client.annotation.Audit;
import com.espertech.esper.common.client.type.EPTypeClass;

import java.lang.annotation.Annotation;

public class AnnotationAudit implements Audit {
    public final static EPTypeClass EPTYPE = new EPTypeClass(AnnotationAudit.class);

    private final String value;

    public AnnotationAudit(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public Class<? extends Annotation> annotationType() {
        return Audit.class;
    }
}
