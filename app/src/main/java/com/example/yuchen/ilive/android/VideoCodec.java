package com.example.yuchen.ilive.android;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by yuchen on 17/5/1.
 */

public class VideoCodec {
    private String MIME_TYPE = "video/avc";
    private int width;
    private int height;
    private int COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
    private int BIT_RATE = 125000;
    private int FRAME_RATE = 30;
    private int IFRAME_INTERVAL = 5;
    private CodecSurface mInputSurface;


    private MediaCodec mediaCodec;


    public VideoCodec(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setWidthAndHeight(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public VideoCodec(CodecSurface surface) {
        this.mInputSurface = surface;
    }

    public void prepareCodecEncoder() throws IOException{
        try {
            MediaCodecInfo codecInfo = selectCodec(MIME_TYPE);
            if(codecInfo == null) {
                Log.d("codec information", null + "");
                return;
            }
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
            mediaFormat.setInteger(mediaFormat.KEY_BIT_RATE, BIT_RATE);
            mediaFormat.setInteger(mediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            mediaFormat.setInteger(mediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
            mediaFormat.setInteger(mediaFormat.KEY_COLOR_FORMAT, COLOR_FORMAT);

            mediaCodec = MediaCodec.createByCodecName(codecInfo.getName());
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mInputSurface = new CodecSurface(mediaCodec.createInputSurface());
            mediaCodec.start();
        } finally {
            if(mediaCodec == null) {
                Log.i("prepare encode err", "init");
                mediaCodec.stop();
                mediaCodec.release();
            }
        }
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            encoder();
        }
    };

    public void encode(byte[] frames) {
        int inputBufferId = mediaCodec.dequeueInputBuffer(-1);
        if(inputBufferId >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferId);
            inputBuffer.clear();
            inputBuffer.put(frames);
            mediaCodec.queueInputBuffer(inputBufferId, 0, frames.length, System.nanoTime(), 0);
        }


        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

        while (outputBufferId >= 0) {
            if (outputBufferId >= 0) {
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferId);
                Log.i("data", outputBuffer.toString());
            } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newMediaFormat = mediaCodec.getOutputFormat();
            }
            outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MediaCodecInfo selectedCodecInfo(String mimeType) {
        MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        MediaCodecInfo[] codecInfos = codecList.getCodecInfos();
        for(int i = 0, ii = codecInfos.length; i < ii; i++) {
            MediaCodecInfo codecInfo = codecInfos[i];
            if(!codecInfo.isEncoder()) {
                continue;
            }

            String[] supportMimeTypes = codecInfo.getSupportedTypes();
            for(int j = 0, jj = supportMimeTypes.length; j < jj; j++) {
                if(supportMimeTypes[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    public CodecSurface getSurface() {
        return this.mInputSurface;
    }

    public void encoder() {
        while (true) {
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

            Log.i("-------encode", outputBufferId + "--");
            while (outputBufferId >= 0) {
                if (outputBufferId >= 0) {
                    ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferId);
                    Log.i("data", outputBuffer.toString());
                } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newMediaFormat = mediaCodec.getOutputFormat();
                    Log.i("cas-0", (newMediaFormat.getByteBuffer("csd-0")).toString());
                }
                outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }
        }
    }
}
