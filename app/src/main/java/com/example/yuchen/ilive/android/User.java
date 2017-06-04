package com.example.yuchen.ilive.android;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * Created by yuchen on 17/4/26.
 */

public class User extends Fragment {
    private CustomerApplication customerApplication;

    @Override
    public View onCreateView(LayoutInflater layoutInflater,
                             ViewGroup container, Bundle saveInstanceState) {

        View view = layoutInflater.inflate(R.layout.user, container, false);
        customerApplication = (CustomerApplication)getActivity().getApplication();
        WebView webView = (WebView) view.findViewById((R.id.webView));
        TextView textView = (TextView) view.findViewById(R.id.tips);
        if(!customerApplication.getCode().equals("")) {
            textView.setVisibility(View.INVISIBLE);
            //webView.setWebViewClient(new WebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            Log.i("webview", "view");
            webView.loadUrl("http://" + Config.ipAddr + "/user/userinfo");
        } else {
            //webView.setVisibility(View.INVISIBLE);
        }
        return view;
    }
}
