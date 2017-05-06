package com.example.yuchen.ilive.android;

import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by yuchen on 17/5/6.
 */

public class PackerAudioAndVideo  {

    private OnPackerListener mOnPackerListener = new OnPackerListenerCallback();
    private onCodecAvailableCallback mOnCodecAvailableCallback = null;

    public class OnPackerListenerCallback implements OnPackerListener {
        @Override
        public void OnPackerCallback(ByteBuffer buffer, int type) {
            Log.i("from back" + (type == 0 ? " audio" : " video"), buffer.toString());
        }
    }

    public PackerAudioAndVideo() {
        //this.mOnPackerListener = mOnPackerListener;
        mOnCodecAvailableCallback = new onCodecAvailableCallback();
    }

    public class onCodecAvailableCallback implements OnVideoH264DataAvailable, OnAudioAACDataAvailable {

        @Override
        public void OnAudioCodecAvailable(byte[] audioData) {
            ByteBuffer bb = ByteBuffer.allocate(audioData.length);
            bb.put(audioData);
            mOnPackerListener.OnPackerCallback(bb, 0);

            //Log.i("from packer", audioData.toString() + "----" + audioData.length);

        }

        @Override
        public void onVideoCodecAvailable(ByteBuffer buffer) {
            mOnPackerListener.OnPackerCallback(buffer, 1);
            //Log.i("from packer video", buffer.toString());
        }
    }

    public onCodecAvailableCallback getOnCodecAvailableCallback() {
        return this.mOnCodecAvailableCallback;
    }

}
