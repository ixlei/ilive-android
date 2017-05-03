package com.example.yuchen.ilive.android;

import android.app.NativeActivity;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by yuchen on 17/5/3.
 */

public class RenderTexToGLSurface {
    private int mSurfaceTextureId;

    private int mProgram;

    private int mPosAttr;
    private int mTexCoordAttr;
    private int mPosMtx;
    private int mTexCoordMtx;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mFragmentBuffer;


    private final String VERTEX_SHADER =
            "attribute ve4 vPosition;" +
            "attribute ve2 inputTextureCoordinate;" +
            "uniform mat4 uPositionMtx;" +
            "uniform mat4 uTextureMtx;" +
            "varying vec2 textureCoordinate;" +
            "void main() {" +
            "  gl_Position = vPosition * uPositionMtx;" +
            "  textureCoordinate = (uTextureMtx * inputTextureCoordinate).xy;" +
            "}";

    private final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "varying vec2 textureCoordinate;" +
            "uniform samplerExternalOES sTexture;" +
            "void main() {" +
            " gl_FragColor = texture2D(sTexture, textureCoordinate);" +
            "}";



    RenderTexToGLSurface(int mTextureId) {
        this.mSurfaceTextureId = mTextureId;
        mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);

        mPosAttr = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mTexCoordAttr = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");

        mPosMtx = GLES20.glGetUniformLocation(mProgram, "uPositionMtx");
        mTexCoordMtx = GLES20.glGetUniformLocation(mProgram, "uTextureMtx");

        mVertexBuffer = createVertexBuffer();
        mFragmentBuffer = createTexCoordBuffer();
    }

    public void draw(float[] textureMtx) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);

        mVertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(mPosAttr);
        GLES20.glVertexAttribPointer(mPosAttr, 3, GLES20.GL_FLOAT, false, 20, mVertexBuffer);

        mFragmentBuffer.position(3);
        GLES20.glEnableVertexAttribArray(mTexCoordAttr);
        GLES20.glVertexAttribPointer(mTexCoordAttr, 2, GLES20.GL_FLOAT, false, 20, mFragmentBuffer);

        GLES20.glUniformMatrix4fv(mPosMtx, 1, false, new float[16], 0);

        GLES20.glUniformMatrix4fv(mTexCoordMtx, 1, false, textureMtx, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mSurfaceTextureId);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(mPosAttr);
        GLES20.glDisableVertexAttribArray(mTexCoordAttr);


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

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public FloatBuffer createVertexBuffer() {
        final float[] vertex = {
                -1, 1, 0,
                -1, -1, 0,
                1, 1, 0,
                1, -1, 0
        };
        ByteBuffer bb = ByteBuffer.allocate(vertex.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(vertex);
        fb.position(0);
        return fb;
    };

    public FloatBuffer createTexCoordBuffer() {
        final float[] textureCoordinate = {
                0, 1,
                0, 0,
                1, 1,
                1, 0
        };
        ByteBuffer bb = ByteBuffer.allocate(textureCoordinate.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(textureCoordinate);
        fb.position(0);
        return fb;
    }

}
