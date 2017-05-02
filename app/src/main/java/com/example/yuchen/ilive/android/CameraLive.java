package com.example.yuchen.ilive.android;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuchen on 17/4/27.
 */

public class CameraLive {

    private ArrayList<CameraInfo> cameraInfos;
    private Camera currCameraDevice = null;
    public CameraInfo currCameraDeviceInfo = null;

    public CameraLive() {
        if(hasCameraDevice() && detectCameraDevice()) {
            this.cameraInfos = getCameraInfos();
        }
    }



    public static boolean hasCameraDevice() {
        return Config.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static boolean detectCameraDevice() {
        DevicePolicyManager dpm = (DevicePolicyManager) Config.context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return !dpm.getCameraDisabled(null);
    }

    public ArrayList<CameraInfo> getCameraInfos() {
        ArrayList<CameraInfo> cameraInfos = new ArrayList<>();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int numberOfCameras = Camera.getNumberOfCameras();
        for(int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            cameraInfos.add(new CameraInfo(i, cameraInfo.facing));
        }
        return cameraInfos;

    }

    public synchronized Camera openCamera(int facing) throws ExceptionClass.CameraNotSupportException{
        CameraInfo cameraInfo = getCameraByFacing(facing);
        if(currCameraDevice != null && currCameraDeviceInfo == cameraInfo) {
            return currCameraDevice;
        }

        if(currCameraDevice != null) {
            releaseCamera(currCameraDevice);
        }

        Log.i("open camera" + cameraInfo.cameraId, "start");

        currCameraDevice = Camera.open(cameraInfo.cameraId);
        if(currCameraDevice == null) {
            throw new ExceptionClass.CameraNotSupportException("camera not support");
        }

        currCameraDeviceInfo = cameraInfo;
        return currCameraDevice;

    }

    public void releaseCamera(Camera camera) {
//        if(mCamera != null) {
//            mCamera.release();
//            //mCamera = null;
//        }
        if(currCameraDevice != null) {
            currCameraDevice.release();
            currCameraDevice = null;
        }
    }

    public static void setCameraPreviewFormat(Camera mCamera, Camera.Parameters parameters, int imageFormat) {
        try {
            parameters.setPreviewFormat(imageFormat);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setPreviewCallback(Camera camera, Camera.PreviewCallback callback) {
        camera.setPreviewCallback(callback);

    }

    public CameraInfo getCameraByFacing(int facing) {
        //部分安卓手机有双摄像头 返回第一个
        for(int i = 0, ii = this.cameraInfos.size(); i < ii; i++) {
            if(cameraInfos.get(i).cameraFacing == facing) {
                return cameraInfos.get(i);
            }
        }
        return null;
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


    public static Camera.Size findOptimalPreviewSize(Camera mcamera, int width, int height, int screentOrientation, int cameraOrientation) {
        Camera.Size optmalSize = null;
        Camera.Parameters parameters = mcamera.getParameters();
        List<Camera.Size> supportedPreviewSize = parameters.getSupportedPreviewSizes();
        double minDiffWidth = Double.MAX_VALUE;
        double minDiffHeight = Double.MAX_EXPONENT;
        if(supportedPreviewSize == null){
            return null;
        }
        for(int i = 0, ii = supportedPreviewSize.size(); i < ii; i++) {
            Camera.Size tempSize = supportedPreviewSize.get(i);
            if(Math.abs(tempSize.width - width) <= minDiffWidth) {
                minDiffWidth = Math.abs(supportedPreviewSize.get(i).width - width);
                if(Math.abs(tempSize.height - height) < minDiffHeight) {
                    optmalSize = tempSize;
                    minDiffHeight = Math.abs(tempSize.height - height);
                }
            }
        }
        return optmalSize;
    }

    public static void setPreviewSize(Camera camera, Camera.Size size, Camera.Parameters parameters) {
        try {
            parameters.setPreviewSize(size.width, size.height);
            camera.setParameters(parameters);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setFocusMode(Camera camera, Camera.Parameters parameters) {
        List<String> supportedFocusModeS = parameters.getSupportedFocusModes();
        String focusMode = supportedFocusModeS.contains(parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                ? parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                : parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
        parameters.setFocusMode(focusMode);
        camera.setParameters(parameters);
    }

    public static byte[] swapYV12toI420(byte[] yv12bytes, int width, int height) {
        byte[] i420bytes = new byte[yv12bytes.length];
        for (int i = 0; i < width * height; i++) {
            i420bytes[i] = yv12bytes[i];
        }
        for (int i = width * height; i < width * height + (width/2 * height/2); i++) {
            i420bytes[i] = yv12bytes[i + (width/2*height/2)];
        }

        for (int i = width * height + (width/2 * height/2); i < width * height + 2*(width/2 * height/2); i++) {
            i420bytes[i] = yv12bytes[i - (width/2*height/2)];
        }

        return i420bytes;
    }


}
