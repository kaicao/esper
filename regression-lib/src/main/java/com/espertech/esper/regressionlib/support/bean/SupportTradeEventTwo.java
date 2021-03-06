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
package com.espertech.esper.regressionlib.support.bean;

import java.io.Serializable;

/**
 * Test event; only serializable because it *may* go over the wire  when running remote tests and serialization is just convenient. Serialization generally not used for HA and HA testing.
 */
public class SupportTradeEventTwo implements Serializable {
    private static final long serialVersionUID = 4279662131673439519L;
    private long time;
    private int securityID;
    private double price;
    private long volume;

    public SupportTradeEventTwo(long time, int securityID, double price, long volume) {
        this.time = time;
        this.securityID = securityID;
        this.price = price;
        this.volume = volume;
    }

    public int getSecurityID() {
        return securityID;
    }

    public long getTime() {
        return time;
    }

    public double getPrice() {
        return price;
    }

    public long getVolume() {
        return volume;
    }
}
