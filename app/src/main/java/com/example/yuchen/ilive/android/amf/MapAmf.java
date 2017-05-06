package com.example.yuchen.ilive.android.amf;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuchen on 17/5/6.
 */

public class MapAmf implements AmfInfo {
    private Map<String, AmfInfo> map = new HashMap<>();
    private byte[] endMarket = new byte[]{0x00, 0x00, 0x09};
    private byte arrayTypeValue = 0x08;

    public MapAmf() {

    }

    public void put(String key, AmfInfo value) {
        map.put(key, value);
    }

    /**
     * 1byte type value, 4 bytes array numbers, n bytes data, 3 bytes end market
     * @return
     */
    @Override
    public int getSize() {
        int size = 1 + 4;
        for (Map.Entry<String, AmfInfo> entry : map.entrySet()) {
            size += StringAmf.sizeOf(entry.getKey(), true);
            size += entry.getValue().getSize();
        }
        size += endMarket.length;
        return size;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer bb = ByteBuffer.allocate(getSize());
        bb.put(arrayTypeValue);
        bb.putInt(map.size());
        for (Map.Entry<String, AmfInfo> entry : map.entrySet()) {
            StringAmf stringAmf = new StringAmf(entry.getKey(), true);
            bb.put(stringAmf.getBytes());
            bb.put(entry.getValue().getBytes());
        }
        bb.put(endMarket);
        return bb.array();
    }
 }
