package com.example.yuchen.ilive.android;

import android.opengl.EGL14;

import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by yuchen on 17/5/3.
 */

public class RenderTexToSurface {
    private EGLDisplay mSavedEglDisplay     = null;
    private EGLSurface mSavedEglDrawSurface = null;
    private EGLSurface mSavedEglReadSurface = null;
    private EGLContext mSavedEglContext     = null;

    private int mProgram         = -1;
    private int maPositionHandle = -1;
    private int maTexCoordHandle = -1;
    private int muSamplerHandle  = -1;
    private int muPosMtxHandle   = -1;

    private int mVideoWidth = 0;
    private int mVideoHeight = 0;

    private VideoCodec mVideoEncoder;
    private int mSurfaceTextureId;

    private float[] mSymmetryMtx = new float[16];
    private FloatBuffer mCameraTexCoordBuffer;
    private final FloatBuffer mNormalVtxBuf = createVertexBuffer();

    RenderTexToSurface( VideoCodec mVideoEncoder) {
        this.mVideoEncoder = mVideoEncoder;
    }

    public FloatBuffer createVertexBuffer() {
        final float vtx[] = {
                // XYZ
                -1f,  1f, 0f,
                -1f, -1f, 0f,
                1f,   1f, 0f,
                1f,  -1f, 0f,
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * vtx.length);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(vtx);
        fb.position(0);
        return fb;
    }

    public void setSurfaceTextureId(int surfaceTextureId) {
        this.mSurfaceTextureId = surfaceTextureId;
    }

    public void setVideoSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        initCameraTexCoordBuffer();
    }

    private void initCameraTexCoordBuffer() {
//        int cameraWidth, cameraHeight;
//        CameraData cameraData = CameraHolder.instance().getCameraData();
//        int width = cameraData.cameraWidth;
//        int height = cameraData.cameraHeight;
//        //if(CameraHolder.instance().isLandscape()) {
//            cameraWidth = Math.max(width, height);
//            cameraHeight = Math.min(width, height);
////        } else {
////            cameraWidth = Math.min(width, height);
////            cameraHeight = Math.max(width, height);
////        }
//        float hRatio = mVideoWidth / ((float)cameraWidth);
//        float vRatio = mVideoHeight / ((float)cameraHeight);
//
//        float ratio;
//        if(hRatio > vRatio) {
//            ratio = mVideoHeight / (cameraHeight * hRatio);
            int ratio = 1;
            final float vtx[] = {
                    //UV
                    0f, 0.5f + ratio/2,
                    0f, 0.5f - ratio/2,
                    1f, 0.5f + ratio/2,
                    1f, 0.5f - ratio/2,
            };
            ByteBuffer bb = ByteBuffer.allocateDirect(4 * vtx.length);
            bb.order(ByteOrder.nativeOrder());
            mCameraTexCoordBuffer = bb.asFloatBuffer();
            mCameraTexCoordBuffer.put(vtx);
            mCameraTexCoordBuffer.position(0);
//        } else {
//            ratio = mVideoWidth/ (cameraWidth * vRatio);
//            final float vtx[] = {
//                    //UV
//                    0.5f - ratio/2, 1f,
//                    0.5f - ratio/2, 0f,
//                    0.5f + ratio/2, 1f,
//                    0.5f + ratio/2, 0f,
//            };
//            ByteBuffer bb = ByteBuffer.allocateDirect(4 * vtx.length);
//            bb.order(ByteOrder.nativeOrder());
//            mCameraTexCoordBuffer = bb.asFloatBuffer();
//            mCameraTexCoordBuffer.put(vtx);
//            mCameraTexCoordBuffer.position(0);
//        }
    }


    public void onDraw() {
        saveRenderState();
        {
            if(mVideoEncoder != null && !mVideoEncoder.getRrcordState()) {
                try {
                    mVideoEncoder.prepareCodecEncoder();
                    mVideoEncoder.handleHandler();
                    initGL();
                } catch (IOException e) {
                    Log.i("prepare encoder", e.getMessage());
                }
            }
            if(mVideoEncoder != null && mVideoEncoder.getRrcordState()) {
                mVideoEncoder.makeCurent();

            }

            GLES20.glViewport(0, 0, mVideoWidth, mVideoHeight);

            GLES20.glClearColor(0f, 0f, 0f, 1f);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(mProgram);

            mNormalVtxBuf.position(0);

            GLES20.glVertexAttribPointer(maPositionHandle,
                    3, GLES20.GL_FLOAT, false, 4 * 3, mNormalVtxBuf);
            GLES20.glEnableVertexAttribArray(maPositionHandle);

            mCameraTexCoordBuffer.position(0);
            GLES20.glVertexAttribPointer(maTexCoordHandle,
                    2, GLES20.GL_FLOAT, false, 4 * 2, mCameraTexCoordBuffer);
            GLES20.glEnableVertexAttribArray(maTexCoordHandle);

            GLES20.glUniform1i(muSamplerHandle, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSurfaceTextureId);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            mVideoEncoder.swapBuffers();
        }
        restoreRenderState();
    }

    private void saveRenderState() {
        mSavedEglDisplay     = EGL14.eglGetCurrentDisplay();
        mSavedEglDrawSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
        mSavedEglReadSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ);
        mSavedEglContext     = EGL14.eglGetCurrentContext();
    }

    private void restoreRenderState() {
        if (!EGL14.eglMakeCurrent(
                mSavedEglDisplay,
                mSavedEglDrawSurface,
                mSavedEglReadSurface,
                mSavedEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }
    private void initGL() {

        final String vertexShader =
                       "attribute vec4 position;\n" +
                        "attribute vec4 inputTextureCoordinate;\n" +
                        "varying   vec2 textureCoordinate;\n" +
                        "uniform   mat4 uPosMtx;\n" +
                        "void main() {\n" +
                        "  gl_Position = uPosMtx * position;\n" +
                        "  textureCoordinate   = inputTextureCoordinate.xy;\n" +
                        "}\n";
        final String fragmentShader =
                //
                "precision mediump float;\n" +
                        "uniform sampler2D uSampler;\n" +
                        "varying vec2 textureCoordinate;\n" +
                        "void main() {\n" +
                        "  gl_FragColor = texture2D(uSampler, textureCoordinate);\n" +
                        "}\n";

        mProgram         = createProgram(vertexShader, fragmentShader);
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
        maTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        muSamplerHandle  = GLES20.glGetUniformLocation(mProgram, "uSampler");
        muPosMtxHandle   = GLES20.glGetUniformLocation(mProgram, "uPosMtx");

        Matrix.scaleM(mSymmetryMtx, 0, -1, 1, 1);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    public int createProgram(String vertexCode, String fragmentCode) {
        int mProgram = GLES20.glCreateProgram();
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode);
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        return mProgram;
    }

    private  int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

}
