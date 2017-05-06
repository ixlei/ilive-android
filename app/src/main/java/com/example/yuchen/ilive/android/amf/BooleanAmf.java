package com.example.yuchen.ilive.android.amf;

import java.nio.ByteBuffer;

/**
 * Created by yuchen on 17/5/6.
 */

public class BooleanAmf implements AmfInfo{
    //one byte set boolean value length, other byte set boolean type value
    private int SIZE = 1 + 1;
    private byte booleanTypeVal = 0x01;

    private boolean value;

    public BooleanAmf(Boolean value) {
        this.value = value;
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer bb = ByteBuffer.allocate(SIZE);
        bb.put(booleanTypeVal);
        bb.put((byte)(value ? 0x01 : 0x00));
        return bb.array();
    }


}
