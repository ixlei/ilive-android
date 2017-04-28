package com.example.yuchen.ilive.android;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by yuchen on 17/4/27.
 */

public class LiveActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private CameraLive cameraLive = null;
    private Camera mCamera = null;
    private LiveAudioRecord liveAudioRecord = null;

    @Override
    public void onCreate(Bundle saveBundleInstance) {
        super.onCreate(saveBundleInstance);
        setContentView(R.layout.live);

        //config init
        Config.context = this;

        cameraLive = new CameraLive();
        liveAudioRecord = new LiveAudioRecord();

        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.liveSurfaceView);
        surfaceView.getHolder().addCallback(this);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        cameraLive.releaseCamera(mCamera);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("preview", "SurfaceView width:" + width + " height:" + height);
        try {
            mCamera = cameraLive.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            CameraLive.setCameraPreviewFormat(mCamera, mCamera.getParameters(), ImageFormat.NV21);
            CameraLive.setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
            CameraLive.setPreviewCallback(mCamera, cameraLive.cameraPreviewCallback);
            CameraLive.setPreviewSize(mCamera, CameraLive.findOptimalPreviewSize(mCamera, width, height, 0, 0), mCamera.getParameters());

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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ExceptionClass.InitAudioRecordException e) {
                Log.i("init audio error", e.getMessage());
            }

        }
    }
}
