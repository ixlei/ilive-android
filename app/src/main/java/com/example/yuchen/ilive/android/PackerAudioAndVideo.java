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
    private int audioSampleBit = 16;
    private int channelCount = 2;           //aac 2 channel
    //video information

    private final int IDR = 5;            //key frame
    private final int PPS = 8;            //h264 pps
    private final int SPS = 7;            //h264 psp
    private final int NonIDR = 1;
    private final int SEI = 6;

    private long mStartTime;                //packer start time
    private int PREVSIZE = 4;               // previous tag size
    private final int FLV_HEADER_SIZE = 11;


    private OnPackerListener mOnPackerListener = new OnPackerListenerCallback();
    private onCodecAvailableCallback mOnCodecAvailableCallback = null;
    private FlashVideoMux flashVideoMux = null;

    public class OnPackerListenerCallback implements OnPackerListener {
        @Override
        public void OnPackerCallback(byte[] buffer, int type) {
            Log.i("from back" + (type == 0 ? " audio" : " video"), buffer.toString());
        }
    }

    public PackerAudioAndVideo() {
        flashVideoMux = new FlashVideoMux();
        mOnCodecAvailableCallback = new onCodecAvailableCallback();
    }

    public void setVideoMetadata(int width, int height, int fps) {
        this.width = width;
        this.height = height;
        this.fps = fps;
    }

    public void setAudioMetaData(int sampleBit, int sampleRate, int channelCount) {
        audioSampleBit = sampleBit;
        audioSampleRate = sampleRate;
        this.channelCount = channelCount;
    }

    public class onCodecAvailableCallback implements OnVideoH264DataAvailable, OnAudioAACDataAvailable {

        @Override
        public void OnAudioCodecAvailable(byte[] audioData) {
            ByteBuffer bb = ByteBuffer.allocate(audioData.length);
            bb.put(audioData);
            //mOnPackerListener.OnPackerCallback(bb, 0);
        }

        @Override
        public void onVideoCodecAvailable(ByteBuffer buffer) {
            //mOnPackerListener.OnPackerCallback(buffer, 1);
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
        byte[] flvHeader = flashVideoMux.muxHeader(flvType);
        ByteBuffer buffer = ByteBuffer.allocate(flvHeader.length + PREVSIZE);
        buffer.put(flvHeader);
        buffer.putInt(0x00);  //previous size
        mOnPackerListener.OnPackerCallback(buffer.array(), FlvTagType.header);
    }

    public void packerMetaTag() {
        byte[] meta = flashVideoMux.muxFlvMetadata(width, height, fps, audioSampleRate, audioSampleBit, channelCount);
        int size = meta.length + PREVSIZE + FLV_HEADER_SIZE;

        ByteBuffer buffer = ByteBuffer.allocate(size);
        byte[] headerBuffer = flashVideoMux.muxTagHeader(TagType.script, meta.length, 0, 0, 0);
        buffer.put(headerBuffer);
        buffer.put(meta);
        buffer.putInt(size - PREVSIZE);

        mOnPackerListener.OnPackerCallback(buffer.array(), FlvTagType.flvMeta);
    }

    public void startPacker(byte[] sps, byte[] pps) {
        packerFlvHeader();
        packerMetaTag();
        packerFirstVideoTag(sps, pps);
        packerFirstAudioTag();
        mStartTime = System.currentTimeMillis();

    }

    public void packering() {

    }

    public void packerFirstVideoTag(byte[] sps, byte[] pps) {
        byte[] videoTagHeader = flashVideoMux.muxAVCTagHeader(VideoFrameType.KEY_FRAME, 0, 0);
        byte[] firstVideoTagInfo = flashVideoMux.muxVideoFirstTag(sps, pps);

        byte[] tagHeader = flashVideoMux.muxTagHeader(TagType.video, videoTagHeader.length + firstVideoTagInfo.length, 0, 0, 0);
        int dataSize = videoTagHeader.length + firstVideoTagInfo.length + tagHeader.length;
        int len = dataSize + PREVSIZE;
        ByteBuffer buffer = ByteBuffer.allocate(len);
        buffer.put(tagHeader);
        buffer.put(videoTagHeader);
        buffer.put(firstVideoTagInfo);
        buffer.putInt(dataSize);

        mOnPackerListener.OnPackerCallback(buffer.array(), FlvTagType.firstVideoTag);
    }

    public void packerFirstAudioTag() {
        byte[] sequenceAudioInfo = flashVideoMux.muxSequenceAudioInfo(audioSampleRate, channelCount);
        byte[] sequenceAudioTag = flashVideoMux.muxAudioTagHeader(SoundType.AAC, audioSampleRate, channelCount, audioSampleBit, true);
        byte[] tagHeader = flashVideoMux.muxTagHeader(TagType.audio, sequenceAudioInfo.length + sequenceAudioTag.length, 0, 0, 0);
        int dataSize = sequenceAudioInfo.length + sequenceAudioTag.length + tagHeader.length;
        ByteBuffer buffer = ByteBuffer.allocate(dataSize + PREVSIZE);
        buffer.put(tagHeader);
        buffer.put(sequenceAudioTag);
        buffer.put(sequenceAudioInfo);
        buffer.putInt(dataSize);

        mOnPackerListener.OnPackerCallback(buffer.array(), FlvTagType.firstAudioTag);
    }

    public void packerVideo(ByteBuffer videoData) {
        byte[] videoAclData = getVclDataFromACL(videoData);

        int frameType = isKeyframe(videoAclData) ? VideoFrameType.KEY_FRAME : VideoFrameType.INTER_FRAME;
        byte[] videoTagHeader = flashVideoMux.muxAVCTagHeader(frameType, 1, 0);

        int offsetTime = (int)(System.currentTimeMillis() - mStartTime);

        byte[] tagHeader = flashVideoMux.muxTagHeader(TagType.video, videoTagHeader.length + videoAclData.length, offsetTime, 0, 0);
        int dataSize = videoTagHeader.length + videoAclData.length + tagHeader.length;
        int len = dataSize + PREVSIZE;
        ByteBuffer buffer = ByteBuffer.allocate(len);
        buffer.put(tagHeader);
        buffer.put(videoTagHeader);
        buffer.put(videoAclData);
        buffer.putInt(dataSize);

        mOnPackerListener.OnPackerCallback(buffer.array(), FlvTagType.videoTag);
    }

    public void packerAudio(byte[] audioData) {
        int offsetTime = (int)(System.currentTimeMillis() - mStartTime);
        byte[] sequenceAudioTag = flashVideoMux.muxAudioTagHeader(SoundType.AAC, audioSampleRate, channelCount, audioSampleBit, false);
        byte[] tagHeader = flashVideoMux.muxTagHeader(TagType.audio, audioData.length + sequenceAudioTag.length, offsetTime, 0, 0);
        int dataSize = audioData.length + sequenceAudioTag.length + tagHeader.length;
        ByteBuffer buffer = ByteBuffer.allocate(dataSize + PREVSIZE);
        buffer.put(tagHeader);
        buffer.put(sequenceAudioTag);
        buffer.put(audioData);
        buffer.putInt(dataSize);

        mOnPackerListener.OnPackerCallback(buffer.array(), FlvTagType.audioTag);
    }


    public class FlvTagType {
        public static final int header = 0;
        public static final int flvMeta = 1;
        public static final int firstVideoTag = 2;
        public static final int firstAudioTag = 3;
        public static final int videoTag = 4;
        public static final int audioTag = 5;
    }

    public class VideoFrameType {
        public static final int KEY_FRAME = 1;
        public static final int INTER_FRAME = 2;
        public static final int DISINTERFRAME = 3;
    }

    public class TagType {
        public static final int audio = 0x08;
        public static final int video = 0x09;
        public static final int script = 0x12;
    }

    public class SoundType {
        public static final int AAC = 10;
        public static final int MP3 = 2;
    }


}
