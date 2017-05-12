package com.example.yuchen.ilive.android;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TableLayout;

import com.example.yuchen.ilive.android.example.widget.media.IjkVideoView;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by yuchen on 17/4/26.
 */

public class HotLive extends Fragment {
    @Override
    public View onCreateView(LayoutInflater layoutInflater,
                             ViewGroup container, Bundle saveInstanceState) {

        View view = layoutInflater.inflate(R.layout.hot_live, container, false);
//        WebView webView = (WebView)view.findViewById(R.id.webView);
        final IjkVideoView videoView = (IjkVideoView) view.findViewById(R.id.video_view);
        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        videoView.setHudView((TableLayout) view.findViewById(R.id.hud_view));
        videoView.setVideoURI(Uri.parse("rtmp://192.168.2.1:1935/ilive/12195071"));
        videoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                videoView.start();
            }
        });
        //webView.loadUrl("http://192.168.2.1:8000");
        return view;
    }
}
