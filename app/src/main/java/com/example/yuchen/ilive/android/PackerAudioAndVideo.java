package com.example.yuchen.ilive.android;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.PublicKey;

/**
 * Created by yuchen on 17/5/6.
 */

public class PackerAudioAndVideo  {

    static {
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avformat-57");
        System.loadLibrary("avutil-55");
        System.loadLibrary("swresample-2");
        System.loadLibrary("swscale-4");
        System.loadLibrary("ffmpeg-jni");
    }

    public native void pushFlvStream();
    public native void initFramesQueue(SendQueue sendQueue);

    private int flvType = 3;                //video and audio
    private int width = 640;                //video width
    private int height = 360;               //video height
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
    private SendQueue mSendQueue = null;

    public class OnPackerListenerCallback implements OnPackerListener {
        private File mTestFile;
        private FileOutputStream mOutStream;
        private BufferedOutputStream mBuffer = null;

        public OnPackerListenerCallback() {
//            Log.i("ddddd----", "ddddddhdhsdhs");
//
//            String sdcardPath = Environment.getExternalStorageDirectory().toString();
//            mTestFile = new File(sdcardPath+"/SopCast.flv");
//            Log.i("path", sdcardPath + "/SopCast.flv");
//            if(mTestFile.exists()){
//                mTestFile.delete();
//            }
//
//            try {
//                mTestFile.createNewFile();
//                mOutStream = new FileOutputStream(mTestFile);
//                mBuffer = new BufferedOutputStream(mOutStream);
//            } catch (Exception e) {
//                Log.i("ddd", "dddd");
//                e.printStackTrace();
//            }
        }
        @Override
        public void OnPackerCallback(byte[] buffer, int type) {

//            if (mBuffer != null){
//                try {
//                    mBuffer.write(buffer);
//                    mBuffer.flush();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
            //mSendQueue.addFrames(buffer);
            Log.i(type + "", buffer.length + "");
        }
    }

    public PackerAudioAndVideo(SendQueue mSendQueue) {
        this.mSendQueue = mSendQueue;
        flashVideoMux = new FlashVideoMux();
        mOnCodecAvailableCallback = new onCodecAvailableCallback(this);
        initFramesQueue(mSendQueue);
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
        private File mTestFile;
        private FileOutputStream mOutStream;
        private BufferedOutputStream mBuffer = null;
        private PackerAudioAndVideo mPackerAudioAndVideo = null;

        public onCodecAvailableCallback(PackerAudioAndVideo mPackerAudioAndVideo) {
            this.mPackerAudioAndVideo = mPackerAudioAndVideo;
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mTestFile = new File(sdcardPath+"/SopCast.flv");
            if(mTestFile.exists()){
                mTestFile.delete();
            }

            try {
                mTestFile.createNewFile();
                mOutStream = new FileOutputStream(mTestFile);
                mBuffer = new BufferedOutputStream(mOutStream);
            } catch (Exception e) {
                Log.i("ddd", "dddd");
                e.printStackTrace();
            }
        }

        @Override
        public void OnAudioCodecAvailable(byte[] audioData) {

            mPackerAudioAndVideo.packerAudio(audioData);
        }

        @Override
        public void onVideoCodecAvailable(ByteBuffer buffer) {
            buffer.position(4);
            byte[] frameBytes = new byte[buffer.limit() - 4];
            buffer.get(frameBytes);
            if (mBuffer != null){
                try {
                    mBuffer.write(frameBytes);
                    mBuffer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mPackerAudioAndVideo.packerVideo(buffer);
        }

        @Override
        public void onSPSAndPPSAvailable(byte[] sps, byte[] pps) {
            if (mBuffer != null){
                try {
                    mBuffer.write(sps);
                    mBuffer.flush();
                    mBuffer.write(pps);
                    mBuffer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mPackerAudioAndVideo.startPacker(sps, pps);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(4000);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                    pushFlvStream();
//                }
//            }).start();
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
