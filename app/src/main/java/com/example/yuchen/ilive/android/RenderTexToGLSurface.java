package com.example.yuchen.ilive.android;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by yuchen on 17/5/3.
 */

public class RenderTexToGLSurface {
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec2 inputTextureCoordinate;" +
                    "varying vec2 textureCoordinate;" +
                    "void main()" +
                    "{"+
                    "gl_Position = vPosition;"+
                    "textureCoordinate = inputTextureCoordinate;" +
                    "}";

    private final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n"+
                    "precision mediump float;" +
                    "varying vec2 textureCoordinate;\n" +
                    "uniform samplerExternalOES s_texture;\n" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, textureCoordinate );\n" +
                    "}";

    private FloatBuffer vertexBuffer, textureVerticesBuffer;
    private ShortBuffer drawIndexBuffer;

    private final int mProgram;
    private int mPositionHandle;
    private int mTextureCoordHandle;

    private int mSurfaceTextureId;

    private static final int COORDS_PER_VERTEX = 2;

    private final int vertexStride = COORDS_PER_VERTEX * 4;

    public RenderTexToGLSurface(int mSurfaceTextureId)
    {
        this.mSurfaceTextureId = mSurfaceTextureId;

        vertexBuffer = createVertexBuffer();
        drawIndexBuffer = createDrawOrderBuffer();
        textureVerticesBuffer = createTexCoordBuffer();

        mProgram = createProgram(vertexShaderCode, fragmentShaderCode);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
    }

    public void draw(float[] mtx)
    {
        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mSurfaceTextureId);



        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);


        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);

        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, drawIndexBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
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


    public FloatBuffer createVertexBuffer() {
        final float[] vertex = {
                -1.0f,  1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f,
                1.0f,  1.0f,
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(vertex.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(vertex);
        fb.position(0);
        return fb;
    };

    public FloatBuffer createTexCoordBuffer() {
        final float[] textureCoordinate = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
                0.0f, 0.0f,
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(textureCoordinate.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(textureCoordinate);
        fb.position(0);
        return fb;
    }

    public ShortBuffer createDrawOrderBuffer() {
        short drawIndex[] = { 0, 1, 2, 0, 2, 3 };
        ByteBuffer bb = ByteBuffer.allocateDirect(drawIndex.length * 2);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer sb = bb.asShortBuffer();
        sb.put(drawIndex);
        sb.position(0);
        return sb;
    }
}
