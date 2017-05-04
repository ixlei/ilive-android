package com.example.yuchen.ilive.android;

import android.util.Log;

/**
 * Created by yuchen on 17/5/4.
 */

public class AudioEncoderHandler extends Thread {
    private LiveAudioRecord mLiveAudioRecord;
    private byte[] audioBuffer;
    private boolean isStop = false;
    private int minBufferSize = -1;
    private AudioCodec mAudioCodec;

    public AudioEncoderHandler(LiveAudioRecord mLiveAudioRecord) {
        this.mLiveAudioRecord = mLiveAudioRecord;
        minBufferSize = mLiveAudioRecord.getMinBufferSize();
        if(minBufferSize != -1) {
            audioBuffer = new byte[minBufferSize];
        }

        mAudioCodec = new AudioCodec(minBufferSize);
        mAudioCodec.prepareCodec();

    }

    public void stopEncode() {
        if(mLiveAudioRecord != null) {
            mLiveAudioRecord.closeRecording();
            mLiveAudioRecord = null;
        }

        if(mAudioCodec != null) {
            mAudioCodec.releaseCodec();
            mAudioCodec = null;
        }
    }

    public void run() {
        while (!isStop && minBufferSize != -1) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                Log.i("thread error", "");
            }
            int audioLen = mLiveAudioRecord.readAudioData(audioBuffer, 0, minBufferSize);
            if(audioLen > 0) {
                if(mAudioCodec != null) {
                    mAudioCodec.encoder(audioBuffer);
                }
            }
        }
    }
}
