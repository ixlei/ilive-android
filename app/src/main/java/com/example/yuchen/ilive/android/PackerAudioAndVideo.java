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
    private int fps;                        //video fps
    private int audioSampleRate = 44100;    //audio Sample Rate
    private int audioSampleSize;            //audio sample size
    private int channelCount = 2;           //aac 2 channel
    //video information

    private final int IDR = 5;            //key frame
    private final int PPS = 8;            //h264 pps
    private final int SPS = 7;            //h264 psp
    private final int NonIDR = 1;
    private final int SEI = 6;

    private long mStartTime;                //packer start time
    private int PREVSIZE = 4;               // previous tag size


    private OnPackerListener mOnPackerListener = new OnPackerListenerCallback();
    private onCodecAvailableCallback mOnCodecAvailableCallback = null;
    private FlashVideoMux flashVideoMux = null;

    public class OnPackerListenerCallback implements OnPackerListener {
        @Override
        public void OnPackerCallback(ByteBuffer buffer, int type) {
            Log.i("from back" + (type == 0 ? " audio" : " video"), buffer.toString());
        }
    }

    public PackerAudioAndVideo() {
        flashVideoMux = new FlashVideoMux();
        mOnCodecAvailableCallback = new onCodecAvailableCallback();
    }

    public void setVideoMetadata(int width, int height) {
        this.width = width;
        this.height = height;
        this.fps = fps;
    }

    public void setAudioMetaData() {

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

        @Override
        public void onSPSAndPPSAvailable(byte[] sps, byte[] pps) {

        }
    }

    public onCodecAvailableCallback getOnCodecAvailableCallback() {
        return this.mOnCodecAvailableCallback;
    }

    public boolean isPsp(byte[] frames) {
        return frames.length != 0 && ((frames[0] & 0x1F) == SPS);
    }

    public boolean isPps(byte[] frames) {
        return frames.length != 0 && ((frames[0] & 0x1F) == PPS);
    }

    public boolean isKeyframe(byte[] frames) {
        return frames.length != 0 && ((frames[0] & 0x1F) == IDR);
    }

    public byte[] getVclDataFromACL(ByteBuffer buffer) {
        if(buffer.limit() < 4) {
            return null;
        }
        buffer.position(4);
        byte[] frameBytes = new byte[buffer.limit() - 4];
        buffer.get(frameBytes);
        return frameBytes;
    }

    public void packerFlvHeader() {
        //video and audio
        byte[] flvHeader = flashVideoMux.muxHeader(3);
        ByteBuffer buffer = ByteBuffer.allocate(flvHeader.length);
        buffer.put(flvHeader);
        mOnPackerListener.OnPackerCallback(buffer, flvTagType.header);
    }

    public void packerMetaTag() {
        ByteBuffer meta = flashVideoMux.muxFlvMetadata(width, height, fps, audioSampleRate, audioSampleSize, channelCount);
        //meta.array().length
        mOnPackerListener.OnPackerCallback(meta, flvTagType.flvMeta);
    }

    public void packerFirstVideoTag(byte[] sps, byte[] pps) {
        //flashVideoMux.muxVideoTagHeader()
    }


    public class flvTagType {
        public static final int header = 0;
        public static final int flvMeta = 1;
        public static final int firstVideoTag = 2;
        public static final int firstAudioTag = 3;
        public static final int videoTag = 4;
        public static final int audioTag = 5;
    }


}
