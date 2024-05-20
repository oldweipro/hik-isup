package com.oldwei.hikisup.sdk.structure;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class BYTE_ARRAY extends Structure {
    public byte[] byValue;

    public BYTE_ARRAY(int iLen) {
        byValue = new byte[iLen];
    }

    @Override
    protected List<String> getFieldOrder() {
        // TODO Auto-generated method stub
        return Arrays.asList("byValue");
    }
}