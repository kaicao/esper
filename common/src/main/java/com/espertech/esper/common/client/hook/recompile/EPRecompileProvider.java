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
package com.espertech.esper.common.client.hook.recompile;

import com.espertech.esper.common.client.EPCompiled;

/**
 * Provider for a re-compiler that acts on existing deployment to either re-compile or re-load from an external source
 */
public interface EPRecompileProvider {
    /**
     * Provide compiler output
     * @param context deployment information
     * @return compiler output
     * @throws EPRecompileProviderException to indicate that compiler output cannot be obtained
     */
    EPCompiled provide(EPRecompileProviderContext context) throws EPRecompileProviderException;
}
