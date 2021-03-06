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
package com.espertech.esper.common.internal.serde.serdeset.multikey;

import com.espertech.esper.common.client.serde.EventBeanCollatedWriter;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.collection.MultiKeyArrayByte;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DIOMultiKeyArrayByteSerde implements DIOMultiKeyArraySerde<MultiKeyArrayByte> {
    public final static EPTypeClass EPTYPE = new EPTypeClass(DIOMultiKeyArrayByteSerde.class);

    public final static DIOMultiKeyArrayByteSerde INSTANCE = new DIOMultiKeyArrayByteSerde();

    public Class<?> componentType() {
        return byte.class;
    }

    public void write(MultiKeyArrayByte mk, DataOutput output, byte[] unitKey, EventBeanCollatedWriter writer) throws IOException {
        writeInternal(mk.getKeys(), output);
    }

    public MultiKeyArrayByte read(DataInput input, byte[] unitKey) throws IOException {
        return new MultiKeyArrayByte(readInternal(input));
    }

    private void writeInternal(byte[] object, DataOutput output) throws IOException {
        if (object == null) {
            output.writeInt(-1);
            return;
        }
        output.writeInt(object.length);
        for (byte i : object) {
            output.writeByte(i);
        }
    }

    private byte[] readInternal(DataInput input) throws IOException {
        int len = input.readInt();
        if (len == -1) {
            return null;
        }
        byte[] array = new byte[len];
        for (int i = 0; i < len; i++) {
            array[i] = input.readByte();
        }
        return array;
    }
}
