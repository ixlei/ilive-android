package com.example.yuchen.ilive.android;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by yuchen on 17/5/3.
 */

public class AudioCodec {
    public static final int  SAMPLE_RATE_IN_HZ = 44100;
    // 单声道
    public static final int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final String MINE = "audio/mp4a-latm";
    private static final int AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;

    private MediaCodec mAudioCodec = null;

    private int mMaxInputBufferSize;
    private boolean isRecording = false;

    private MediaCodec.BufferInfo mediaBufferinfo = new MediaCodec.BufferInfo();

    public AudioCodec() {

    }

    public AudioCodec(int maxInputBufferSize) {
        mMaxInputBufferSize = maxInputBufferSize;
    }

    /**
     * init audio codec
     */

    public void prepareCodec() {
        MediaFormat mAudioFormat = MediaFormat.createAudioFormat(MINE, SAMPLE_RATE_IN_HZ, AUDIO_CHANNEL);
        mAudioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, AAC_PROFILE);
        mAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1024 * 64);
        mAudioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, SAMPLE_RATE_IN_HZ);
        mAudioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mMaxInputBufferSize);

        try {
            mAudioCodec = MediaCodec.createEncoderByType(MINE);
            mAudioCodec.configure(mAudioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mAudioCodec.start();
        } catch (IOException e) {
            Log.i("prepare audio codec", e.getMessage());
        }
    }

    public void encoder(byte[] frames) {
        int inputBufferId = mAudioCodec.dequeueInputBuffer(0);
        if(inputBufferId >= 0) {
            ByteBuffer inputBuffer = mAudioCodec.getInputBuffer(inputBufferId);
            inputBuffer.clear();
            inputBuffer.put(frames);
            mAudioCodec.queueInputBuffer(inputBufferId, 0, frames.length, System.nanoTime(), 0);
        }

         //MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
         int outputBufferId = mAudioCodec.dequeueOutputBuffer(mediaBufferinfo, 0);

        if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

            MediaFormat newMediaFormat = mAudioCodec.getOutputFormat();

        } else if(outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
            Log.i("try later", outputBufferId + "");

        } else if(outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            ByteBuffer outputBuffer = mAudioCodec.getOutputBuffer(outputBufferId);

            mAudioCodec.releaseOutputBuffer(outputBufferId, false);
        } else {
            //other to do
        }
    }


    public void releaseCodec() {
        if(mAudioCodec != null) {
            mAudioCodec.stop();
            mAudioCodec.release();
            isRecording = false;
            mAudioCodec = null;
        }
    }

    public boolean getRecordState() {
        return isRecording;
    }

    public byte[] packageAccAudio(byte[] audio) {
        byte[] aacAudio = new byte[audio.length + 7];
        return aacAudio;
    }

    /**
     * AAC format contain ADTS header and audio body
     * the ADTS header length is 7 bit
     * @param packet
     * @param packetLen
     */

    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //AAC LC
        int freqIdx = 4;  //44.1KHz
        int chanCfg = 2;  //CPE

        packet[0] = (byte)0xFF;
        packet[1] = (byte)0xF9;
        packet[2] = (byte)(((profile - 1) << 6 ) + ( freqIdx << 2 ) +( chanCfg >> 2));
        packet[3] = (byte)(((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte)((packetLen & 0x7FF) >> 3);
        packet[5] = (byte)(((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte)0xFC;
    }
}
