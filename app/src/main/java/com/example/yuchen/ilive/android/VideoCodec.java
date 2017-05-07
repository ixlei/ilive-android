package com.example.yuchen.ilive.android;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by yuchen on 17/5/1.
 */

public class VideoCodec {
    private String MIME_TYPE = "video/avc";
    private int width;
    private int height;
    private int COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
    private int BIT_RATE = 125000;
    private int FRAME_RATE = 15;
    private int IFRAME_INTERVAL = 2;

    private boolean isRecording = false;
    private CodecSurface mInputSurface;
    private MediaCodec mediaCodec;

    private HandlerThread mHandlerThread;
    private Handler mEncoderHandler;

    private ReentrantLock curentLock = new ReentrantLock();

    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

    private OnVideoH264DataAvailable dataAvailable = null;

    public VideoCodec(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public VideoCodec() {

    }

    public void setWidthAndHeight(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setCodecAvailable(OnVideoH264DataAvailable dataAvailable) {
        this.dataAvailable = dataAvailable;
    }

    public VideoCodec(CodecSurface surface) {
        this.mInputSurface = surface;
    }

    public boolean getRrcordState() {
        return isRecording;
    }

    public void prepareCodecEncoder() throws IOException{
        try {
            MediaCodecInfo codecInfo = selectCodec(MIME_TYPE);
            if(codecInfo == null) {
                Log.d("codec information", null + "");
                return;
            }

            Log.i("width", width + "-" + height);

            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
            mediaFormat.setInteger(mediaFormat.KEY_BIT_RATE, BIT_RATE);
            mediaFormat.setInteger(mediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            mediaFormat.setInteger(mediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
            //mediaFormat.setInteger(mediaFormat.KEY_COLOR_FORMAT, COLOR_FORMAT);
            mediaFormat.setInteger(mediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);

            mediaCodec = MediaCodec.createByCodecName(codecInfo.getName());
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            //mInputSurface = new CodecSurface(mediaCodec.createInputSurface());
            mHandlerThread = new HandlerThread("videoEncode");
            mHandlerThread.start();
            mEncoderHandler = new Handler(mHandlerThread.getLooper());
            mediaCodec.start();
            isRecording = true;
        } finally {
            if(mediaCodec == null) {
               releaseEncoder();
            }
        }
    }

    public void handleHandler() {
       mEncoderHandler.post(runnable);
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            encoder();
        }
    };

    public void makeCurent() {
        if(mediaCodec != null) {
            mInputSurface.makeCurrent();
        }
    }

    public void swapBuffers() {
        if(mediaCodec != null && mInputSurface != null) {
            mInputSurface.swapBuffers();
            mInputSurface.setPresentationTime(System.nanoTime());
        }
    }

    public void encode(byte[] frames) {
        int inputBufferId = mediaCodec.dequeueInputBuffer(0);
        if(inputBufferId >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferId);
            inputBuffer.clear();
            inputBuffer.put(frames);
            mediaCodec.queueInputBuffer(inputBufferId, 0, frames.length, System.nanoTime(), 0);
        }


        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

        if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            MediaFormat newMediaFormat = mediaCodec.getOutputFormat();
            ByteBuffer SPSByteBuffer = newMediaFormat.getByteBuffer("csd-0");
            ByteBuffer PPSByteBuffer = newMediaFormat.getByteBuffer("csd-1");
            Log.i("PPS frame", (SPSByteBuffer.array()[4] & 0x1F) + "");
            Log.i("pps frame", (PPSByteBuffer.array()[4] & 0x1F) + "");

            dataAvailable.onSPSAndPPSAvailable(SPSByteBuffer.array(), PPSByteBuffer.array());

        } else if (outputBufferId >= 0) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferId);
            ByteBuffer bb = ByteBuffer.allocate(outputBuffer.capacity());
            Log.i("position", outputBuffer.position() + "-" + outputBuffer.limit() + "");
            bb.put(outputBuffer);
            byte[] bby = bb.array();
            Log.i("frame type", (bby[4] & 0x1F) + "");

            if(dataAvailable != null) {
                dataAvailable.onVideoCodecAvailable(outputBuffer);
            }
            mediaCodec.releaseOutputBuffer(outputBufferId, false);
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
        ByteBuffer[] outBuffers = mediaCodec.getOutputBuffers();
        while (isRecording) {
            int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 100);
            if(outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newMediaFormat = mediaCodec.getOutputFormat();
                ByteBuffer SPSByteBuffer = newMediaFormat.getByteBuffer("csd-0");
                ByteBuffer PPSByteBuffer = newMediaFormat.getByteBuffer("csd-1");
                dataAvailable.onSPSAndPPSAvailable(SPSByteBuffer.array(), PPSByteBuffer.array());
            } else if (outputBufferId >= 0) {
                ByteBuffer bb = outBuffers[outputBufferId];
                if(dataAvailable != null) {
                    dataAvailable.onVideoCodecAvailable(bb);

                }
                byte[] b = new byte[bb.remaining()];
                bb.get(b);
                Log.i("data", b.length + "");

                mediaCodec.releaseOutputBuffer(outputBufferId, false);

            }

        }
    }

    public void releaseEncoder() {
        isRecording = false;
        if(mediaCodec != null) {
            mediaCodec.signalEndOfInputStream();
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }

        if(mInputSurface != null) {
            mInputSurface.release();
            mInputSurface = null;
        }

    }
}
