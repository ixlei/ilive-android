package com.example.yuchen.ilive.android.amf;

import java.nio.ByteBuffer;

/**
 * Created by yuchen on 17/5/6.
 */

public class StringAmf implements AmfInfo {
    private String value;
    private boolean isKey = false;
    private byte StringTypeVal = 0x02;

    public StringAmf(String value, boolean isKey) {
        this.value = value;
        this.isKey = isKey;
    }


    /**
     * if value is String type plus 2 byte set type value length,
     * if value is not key is value plus one byte set value type
     * @return
     */
    @Override
    public int getSize() {
        return (isKey ? 0 : 1) + 2 + value.getBytes().length;
    }

    @Override
    public byte[] getBytes() {
        int size = getSize();
        ByteBuffer res = ByteBuffer.allocate(size);

        if(!isKey) {
            res.put(StringTypeVal);
        }
        res.putShort((short)value.getBytes().length);
        res.put(value.getBytes());
        return res.array();
    }
}
