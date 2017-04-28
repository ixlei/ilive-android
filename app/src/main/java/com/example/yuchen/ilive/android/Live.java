package com.example.yuchen.ilive.android;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yuchen on 17/4/26.
 */

public class Live extends Fragment {
    private CameraLive cameraLive = null;
    private Camera mCamera = null;
    @Override
    public View onCreateView(LayoutInflater layoutInflater,
                             ViewGroup container, Bundle saveInstanceState) {
        View liveView = layoutInflater.inflate(R.layout.live, container, false);

        return liveView;
    }

}
