package com.example.yuchen.ilive.android;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Created by yuchen on 17/4/26.
 */

public class HotLive extends Fragment {
    @Override
    public View onCreateView(LayoutInflater layoutInflater,
                             ViewGroup container, Bundle saveInstanceState) {
        View view = layoutInflater.inflate(R.layout.webview, container, false);
        WebView webView = (WebView)view.findViewById(R.id.webView);
        webView.loadUrl("http://192.168.2.1:8000/");
        return view;
    }
}
