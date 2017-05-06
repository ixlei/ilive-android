package com.example.yuchen.ilive.android;

import java.nio.ByteBuffer;

/**
 * encode by MediaCodec callback
 * Created by yuchen on 17/5/6.
 */

public interface OnVideoH264DataAvailable {
    public void onVideoCodecAvailable(ByteBuffer buffer);
}
