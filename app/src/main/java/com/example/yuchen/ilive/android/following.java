package com.example.yuchen.ilive.android;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yuchen on 17/4/26.
 */

public class Following extends Fragment {
    @Override
    public View onCreateView(LayoutInflater layoutInflater,
                             ViewGroup container, Bundle saveInstanceState) {
        return layoutInflater.inflate(R.layout.following, container, false);
    }
}
