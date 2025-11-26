package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

import java.util.Arrays;
import java.util.List;

public class BYTE_ARRAY extends HIKSDKStructure {
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