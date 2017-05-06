package com.example.yuchen.ilive.android;

import java.nio.ByteBuffer;

/**
 * Created by yuchen on 17/5/6.
 */

public class PackerAudioAndVideo  {
    private onCodecAvailableCallback mOnCodecAvailableCallback = null;

    public PackerAudioAndVideo() {
        mOnCodecAvailableCallback = new onCodecAvailableCallback();
    }

    public class onCodecAvailableCallback implements OnVideoH264DataAvailable, OnAudioAACDataAvailable {

        @Override
        public void OnAudioCodecAvailable(byte[] audioData) {

        }

        @Override
        public void onVideoCodecAvailable(ByteBuffer buffer) {

        }
    }

    public onCodecAvailableCallback getOnCodecAvailableCallback() {
        return this.mOnCodecAvailableCallback;
    }

}
