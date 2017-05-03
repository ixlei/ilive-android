package com.example.yuchen.ilive.android;

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by yuchen on 17/4/27.
 */

public class LiveActivity extends AppCompatActivity implements SurfaceHolder.Callback,  Camera.PreviewCallback{
    private CameraLive cameraLive = null;
    private Camera mCamera = null;

    private LiveAudioRecord liveAudioRecord = null;
    private VideoCodec videoEncoder;
    private SurfaceTexture mSurfaceTexture;
    private int mSurfaceTextureId;
    private GLSurfaceView liveGLSurfaceView = null;
    private boolean isPreview = false;
    private RenderTexToGLSurface  mRenderTexToGLSurface;
    private RenderTexToSurface renderTexToSurface;


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


        //init camera
        cameraLive = new CameraLive();

        liveAudioRecord = new LiveAudioRecord();

        if(cameraLive != null) {
            //CameraInfo cameraInfo = cameraLive.currCameraDeviceInfo;
            //if(cameraInfo != null) {
                videoEncoder = new VideoCodec();
            //}

        }

        renderTexToSurface = new RenderTexToSurface(videoEncoder);

        liveGLSurfaceView = (android.opengl.GLSurfaceView)findViewById(R.id.liveGLSurfaceView);
        //set opengl version
        liveGLSurfaceView.setEGLContextClientVersion(2);
        //set render
        liveGLSurfaceView.setRenderer(new LiveGLSurfaceRender());
        //set mode
        liveGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

//        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.liveSurfaceView);
//
//        surfaceView.getHolder().addCallback(this);


    }

    private class LiveGLSurfaceRender implements  GLSurfaceView.Renderer {
        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            initTexture();
            renderTexToSurface.setSurfaceTextureId(mSurfaceTextureId);
            mRenderTexToGLSurface = new RenderTexToGLSurface(mSurfaceTextureId);


        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height) {
            startPreview(width, height);
            CameraInfo cameraInfo = cameraLive.currCameraDeviceInfo;
            Log.i("camera info", cameraInfo.cameraWidth + "," + cameraInfo.cameraHeight);

            videoEncoder.setWidthAndHeight(cameraInfo.cameraWidth, cameraInfo.cameraHeight);
        }
        @Override
        public void onDrawFrame(GL10 gl10) {
            synchronized (this) {
                GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
                float[] texMtx = createIdentityMtx();
                mSurfaceTexture.updateTexImage();
                mSurfaceTexture.getTransformMatrix(texMtx);
                mRenderTexToGLSurface.draw(texMtx);
                renderTexToSurface.onDraw();
            }
        }

    }

    public class SurfaceTexFrameAvailaListener implements SurfaceTexture.OnFrameAvailableListener {
        private GLSurfaceView mGlSurfaceView;
        public SurfaceTexFrameAvailaListener(GLSurfaceView mGlSurfaceView) {
            this.mGlSurfaceView = mGlSurfaceView;
        }
        @Override
        public void onFrameAvailable(SurfaceTexture mSurfaceTexture) {
            mGlSurfaceView.requestRender();
        }
    }

    public void initTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mSurfaceTextureId = textures[0];
        mSurfaceTexture = new SurfaceTexture(mSurfaceTextureId);

        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexFrameAvailaListener(liveGLSurfaceView));
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_BLEND);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mSurfaceTextureId);
        //线性取样
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        //线性组合取样
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        // s轴截取
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        //t轴截取
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    public void destroyTexture(int textureId) {
        GLES20.glDeleteTextures(1, new int[] {textureId}, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.stopPreview();
        cameraLive.releaseCamera();
        videoEncoder.releaseEncoder();
        liveGLSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyTexture(mSurfaceTextureId);
    }

    public void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        cameraLive.releaseCamera();
        liveAudioRecord.closeRecording();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void startPreview(int width, int height) {
        if(isPreview) {
            return;
        }

        if(!(CameraLive.hasCameraDevice() && CameraLive.detectCameraDevice())) {
           return;
        }
        try {
            mCamera = cameraLive.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (ExceptionClass.CameraNotSupportException e) {
            Log.i("camera not support", "startPreview");
        }

        if(mCamera == null) {
            return;
        }

        try {
            CameraLive.setFocusMode(mCamera, mCamera.getParameters());
        } catch (Exception e) {
            Log.i("start preview", "set focus mode error");
        }

        try {
            CameraLive.setCameraPreviewFormat(mCamera, mCamera.getParameters(), ImageFormat.YV12);
        } catch (Exception e) {

        }

        try {
            CameraLive.setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
        } catch (Exception e) {

        }

        try {
            Camera.Size previewSize = CameraLive.findOptimalPreviewSize(mCamera, width, height, 0, 0);
            cameraLive.currCameraDeviceInfo.setPreviewSize(previewSize.width, previewSize.height);
            CameraLive.setPreviewSize(mCamera, previewSize, mCamera.getParameters());
        } catch (Exception e) {

        }

        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            Log.i("set preview texture", "ioe");
        }

        mCamera.startPreview();
        isPreview = true;


    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //vcc = new VideoCodec();
        try {
            mCamera = cameraLive.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            CameraLive.setFocusMode(mCamera, mCamera.getParameters());
            CameraLive.setCameraPreviewFormat(mCamera, mCamera.getParameters(), ImageFormat.YV12);
            CameraLive.setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
            //CameraLive.setPreviewCallback(mCamera, this);
            Camera.Size previewSize = CameraLive.findOptimalPreviewSize(mCamera, width, height, 0, 0);
            cameraLive.currCameraDeviceInfo.setPreviewSize(previewSize.width, previewSize.height);
            CameraLive.setPreviewSize(mCamera, previewSize, mCamera.getParameters());

//            if(vcc != null) {
//                vcc.setWidthAndHeight(previewSize.width, previewSize.height);
//                vcc.prepareCodecEncoder();
//
//            }
//            new Thread(vcc.runnable).start();
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (mCamera != null) {
            Log.i("start preview", "live");
            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
                //mCamera.setPreviewDisplay(holder);
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

    public static float[] createIdentityMtx() {
        float[] m = new float[16];
        Matrix.setIdentityM(m, 0);
        return m;
    }
}
