package com.example.yuchen.ilive.android;

/**
 * Created by yuchen on 17/4/27.
 */

public class CameraInfo {
    //摄像头参数
    public int cameraId;
    public int cameraFacing;
    public int openState;
    private int cameraWidth;
    private int cameraHeight;

    public CameraInfo(int cameraId, int cameraFacing) {
        this.cameraId = cameraId;
        this.cameraFacing = cameraFacing;
        this.openState = 0;
    }

}
