package com.example.yuchen.ilive.android.amf;

import java.nio.ByteBuffer;

/**
 * Created by yuchen on 17/5/6.
 */

public class NumberAmf implements AmfInfo {

    private double value;
    private byte numberTypeVal = 0x00;

    //8 bytes set value, 1 byte set number type value
    private int numberValLength = 8 + 1;

    public NumberAmf(double value) {
        this.value = value;
    }

    @Override
    public int getSize() {
        return numberValLength;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer bb = ByteBuffer.allocate(numberValLength);
        bb.put(numberTypeVal);
        bb.putDouble(value);
        return bb.array();
    }
}
