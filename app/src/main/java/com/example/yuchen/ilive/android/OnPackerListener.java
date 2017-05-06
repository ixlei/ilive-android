package com.example.yuchen.ilive.android;

import java.nio.ByteBuffer;

/**
 * Created by yuchen on 17/5/6.
 */

public interface OnPackerListener {
    public void OnPackerCallback(ByteBuffer buffer, int type);
}
