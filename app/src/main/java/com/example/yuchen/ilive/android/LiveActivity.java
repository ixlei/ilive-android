package com.example.yuchen.ilive.android;

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;

/**
 * Created by yuchen on 17/4/27.
 */

public class LiveActivity extends AppCompatActivity implements SurfaceHolder.Callback,  Camera.PreviewCallback{
    private CameraLive cameraLive = null;
    private Camera mCamera = null;
    private LiveAudioRecord liveAudioRecord = null;
    private VideoCodec vcc;
    private SurfaceTexture mSurfaceTexture;


    @Override
    public void onPreviewFrame(final byte[] frame, Camera camera) {

        final byte[] yv12 = CameraLive.swapYV12toI420(frame, cameraLive.currCameraDeviceInfo.cameraWidth, cameraLive.currCameraDeviceInfo.cameraHeight);
    }

    @Override
    public void onCreate(Bundle saveBundleInstance) {
        super.onCreate(saveBundleInstance);
        setContentView(R.layout.live);
        this.setActionBar();
        Config.context = this;


        cameraLive = new CameraLive();
        liveAudioRecord = new LiveAudioRecord();

        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.liveSurfaceView);

        surfaceView.getHolder().addCallback(this);




    }

    public void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        cameraLive.releaseCamera(mCamera);
        liveAudioRecord.closeRecording();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        vcc = new VideoCodec(holder.getSurface());
        try {
            mCamera = cameraLive.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            CameraLive.setFocusMode(mCamera, mCamera.getParameters());
            CameraLive.setCameraPreviewFormat(mCamera, mCamera.getParameters(), ImageFormat.YV12);
            CameraLive.setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
            //CameraLive.setPreviewCallback(mCamera, this);
            Camera.Size previewSize = CameraLive.findOptimalPreviewSize(mCamera, width, height, 0, 0);
            cameraLive.currCameraDeviceInfo.setPreviewSize(previewSize.width, previewSize.height);
            CameraLive.setPreviewSize(mCamera, previewSize, mCamera.getParameters());

            if(vcc != null) {
                vcc.setWidthAndHeight(previewSize.width, previewSize.height);
                vcc.prepareCodecEncoder();
            }
            new Thread(vcc.runnable).start();
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (mCamera != null) {
            Log.i("start preview", "live");
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
                liveAudioRecord.initAudioRecord();
                liveAudioRecord.startRecording();
                short audioData[] = new short[liveAudioRecord.getMinBufferSize()];
                liveAudioRecord.readAudioData(audioData, 0, liveAudioRecord.getMinBufferSize());
                Log.i("audio data", audioData.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ExceptionClass.InitAudioRecordException e) {
                Log.i("init audio error", e.getMessage());
            }

        }
    }
}
