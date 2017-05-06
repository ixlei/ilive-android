package com.example.yuchen.ilive.android;

import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by yuchen on 17/5/6.
 */

public class PackerAudioAndVideo  {


    private int flvType = 3;                //video and audio
    private int width;                      //video width
    private int height;                     //video height
    private int videocodecid = 7;           //H.264/AVC
    private int audioSampleRate = 44100;    //audio Sample Rate
    private int audioSampleSize;            //audio sample size
    private int channelCount = 2;           //aac 2 channel
    //video information


    private OnPackerListener mOnPackerListener = new OnPackerListenerCallback();
    private onCodecAvailableCallback mOnCodecAvailableCallback = null;

    public class OnPackerListenerCallback implements OnPackerListener {
        @Override
        public void OnPackerCallback(ByteBuffer buffer, int type) {
            Log.i("from back" + (type == 0 ? " audio" : " video"), buffer.toString());
        }
    }

    public PackerAudioAndVideo() {
        mOnCodecAvailableCallback = new onCodecAvailableCallback();
    }

    public void setFlvMetadata(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public class onCodecAvailableCallback implements OnVideoH264DataAvailable, OnAudioAACDataAvailable {

        @Override
        public void OnAudioCodecAvailable(byte[] audioData) {
            ByteBuffer bb = ByteBuffer.allocate(audioData.length);
            bb.put(audioData);
            mOnPackerListener.OnPackerCallback(bb, 0);
        }

        @Override
        public void onVideoCodecAvailable(ByteBuffer buffer) {
            mOnPackerListener.OnPackerCallback(buffer, 1);
        }
    }

    public onCodecAvailableCallback getOnCodecAvailableCallback() {
        return this.mOnCodecAvailableCallback;
    }

}
