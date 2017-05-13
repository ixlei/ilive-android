package com.example.yuchen.ilive.android;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import com.example.yuchen.ilive.android.example.widget.media.IRenderView;
import com.example.yuchen.ilive.android.example.widget.media.IjkVideoView;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by yuchen on 17/4/26.
 */

public class LivePlayerActivity extends Activity {
    private String code;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hot_live_player);
        code = getIntent().getStringExtra(Config.livePlayerIntentExtraKey);
        Log.i("code", code);

        final IjkVideoView videoView = (IjkVideoView) findViewById(R.id.video_view);
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        TableLayout tableLayout = (TableLayout) findViewById(R.id.hud_view);
        tableLayout.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT));

        videoView.setHudView(tableLayout);

        videoView.setAspectRatio(IRenderView.AR_MATCH_PARENT);
        videoView.setScaleX(1.1F);
        videoView.setVideoURI(Uri.parse("rtmp://192.168.2.1:1935/ilive/" + code));
        videoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                videoView.start();
            }
        });
    }
}
