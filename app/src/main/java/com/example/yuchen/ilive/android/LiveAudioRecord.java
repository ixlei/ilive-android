package com.example.yuchen.ilive.android;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ListView;

import static android.media.AudioRecord.STATE_UNINITIALIZED;
import static android.media.MediaRecorder.AudioSource.VOICE_COMMUNICATION;

/**
 * Created by yuchen on 17/4/28.
 */

public class LiveAudioRecord {
    private AudioRecord mAudioRecord = null;
    private int minBufferSize = 0;
    private AudioManager mAudioManager = null;
    private boolean isRecording = false;

    //audio record

    //default audio source
    public static final int DEFAULT = MediaRecorder.AudioSource.DEFAULT;
    //microphone
    public static final int MIC = MediaRecorder.AudioSource.MIC;
    //voice up link source
    public static final int VOICE_UPLINK = MediaRecorder.AudioSource.VOICE_UPLINK;
    //voice down link source
    public static final int VOICE_DOWNLINK = MediaRecorder.AudioSource.VOICE_DOWNLINK;
    //voice call source
    public static final int VOICE_CALL = MediaRecorder.AudioSource.VOICE_CALL;

    //sample rate
    public static final int  SAMPLE_RATE_IN_HZ = 16000;
    //audio channels configure
    public static final int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    //audio format
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    public LiveAudioRecord() {

    }

    public void initAudioRecord() throws ExceptionClass.InitAudioRecordException {
        minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AUDIO_CHANNEL, AUDIO_FORMAT);
        if(minBufferSize < 0) {
            Log.i("get audio min buffer", minBufferSize + "");
            return;
        }

        //voice communication echo cancellation
        try {
            mAudioRecord = new AudioRecord(VOICE_COMMUNICATION, SAMPLE_RATE_IN_HZ, AUDIO_CHANNEL, AUDIO_FORMAT, minBufferSize);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        if(mAudioRecord.getState() == STATE_UNINITIALIZED) {
            throw new ExceptionClass.InitAudioRecordException("init audio error");
        }

    }

    public int readAudioData(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return mAudioRecord.read(audioData, offsetInBytes, sizeInBytes);
    }

    public void startRecording() {
        if(isRecording || mAudioRecord == null) {
            return;
        }
        mAudioManager = (AudioManager)Config.context.getSystemService(Config.context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        mAudioManager.setMicrophoneMute(true);
        mAudioRecord.startRecording();
    }

    public void closeRecording() {
        if(mAudioRecord != null) {
            isRecording = false;
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
            mAudioManager.setMicrophoneMute(false);
            mAudioManager = null;
        }
    }

    public  byte[] short2byte(short[] shortArr) {
        int shortArrayLength = shortArr.length;
        byte[] bytes = new byte[shortArrayLength * 2];
        for (int i = 0; i < shortArrayLength; i++) {
            bytes[i * 2] = (byte) (shortArr[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (shortArr[i] >> 8);
            shortArr[i] = 0;
        }
        return bytes;

    }




}
