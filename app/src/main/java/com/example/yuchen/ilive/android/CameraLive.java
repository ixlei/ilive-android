package com.example.yuchen.ilive.android;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by yuchen on 17/4/27.
 */

public class CameraLive {

    private Context context;
    private ArrayList<CameraInfo> cameraInfos;
    private Camera currCameraDevice = null;
    private CameraInfo currCameraDeviceInfo = null;

    public CameraLive(Context context) {
        this.context = context;
    }

    public Camera.PreviewCallback cameraPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] frame, Camera camera) {

        }
    };

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

    public void releaseCamera(Camera mCamera) {
        if(mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public static void setCameraPreviewFormat(Camera mCamera, Camera.Parameters parameters, int imageFormat) {
        try {
//            ImageFormat.NV21
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







}
