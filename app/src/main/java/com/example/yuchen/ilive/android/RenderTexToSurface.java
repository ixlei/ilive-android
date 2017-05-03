package com.example.yuchen.ilive.android;

import android.opengl.EGL14;

import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;

/**
 * Created by yuchen on 17/5/3.
 */

public class RenderTexToSurface {
    private EGLDisplay mSavedEglDisplay     = null;
    private EGLSurface mSavedEglDrawSurface = null;
    private EGLSurface mSavedEglReadSurface = null;
    private EGLContext mSavedEglContext     = null;

    private VideoCodec mVideoEncoder;
    private int mSurfaceTextureId;

    RenderTexToSurface( VideoCodec mVideoEncoder) {
        this.mVideoEncoder = mVideoEncoder;
    }

    public void setSurfaceTextureId(int surfaceTextureId) {
        this.mSurfaceTextureId = surfaceTextureId;
    }

    public void onDraw() {
        saveRenderState();

        {
            if(mVideoEncoder != null && !mVideoEncoder.getRrcordState()) {
                try {
                    mVideoEncoder.prepareCodecEncoder();
                    //new Thread(mVideoEncoder.runnable).start();
                    mVideoEncoder.handleHandler();
                } catch (IOException e) {
                    Log.i("prepare encoder", e.getMessage());
                }
            }
            if(mVideoEncoder != null && mVideoEncoder.getRrcordState()) {
                mVideoEncoder.makeCurent();

            }

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mSurfaceTextureId);
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

}
