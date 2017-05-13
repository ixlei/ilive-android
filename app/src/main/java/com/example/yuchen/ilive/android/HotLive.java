package com.example.yuchen.ilive.android;

import android.app.Fragment;
import android.content.Intent;
import android.icu.text.DisplayContext;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.Toast;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.yuchen.ilive.android.example.widget.media.IRenderView;
import com.example.yuchen.ilive.android.example.widget.media.IjkVideoView;
import com.example.yuchen.ilive.android.ui.HotLiveList;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by yuchen on 17/4/26.
 */

public class HotLive extends Fragment {

    private CustomerApplication mCustomerApplication;

    @Override
    public View onCreateView(LayoutInflater layoutInflater,
                             ViewGroup container, Bundle saveInstanceState) {

        final View view = layoutInflater.inflate(R.layout.hot_live, container, false);
        mCustomerApplication = (CustomerApplication)getActivity().getApplication();
        RequestQueue queue = mCustomerApplication.getRequestQueue();

        String url = "http://127.0.0.1:8001/live/hotlive/0";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if ((int)response.get("ret") == 0) {
                                JSONArray jsonArray = response.getJSONArray("results");
                                Type listType = new TypeToken<List<HashMap<String, Object>>>() {}.getType();
                                ArrayList<HashMap<String, Object>> hotLiveList = new Gson().fromJson(jsonArray.toString(), listType);

                                BaseAdapter baseAdapter = new HotLiveList(getActivity(), hotLiveList, new ClickEventListener(hotLiveList));
                                ListView listView = (ListView) view.findViewById(R.id.hotLiveList);

                                listView.setAdapter(baseAdapter);
                                return;
                            }
                            Toast.makeText(getActivity(), "错误，稍后重试", Toast.LENGTH_SHORT).show();


                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), "错误，稍后重试", Toast.LENGTH_SHORT).show();
                        }

                    }
                } , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "错误，稍后重试", Toast.LENGTH_SHORT).show();
            }

        });
        queue.add(request);
        return view;
    }
    public class ClickEventListener implements View.OnClickListener {
        private ArrayList<HashMap<String, Object>> list;

        public ClickEventListener(ArrayList<HashMap<String, Object>> list) {
            this.list = list;
        }

        @Override
        public void onClick(View v) {
            int position = (int)v.getTag();
            HashMap<String, Object> item = list.get(position);
            String codeStr = item.get("code").toString();
            String code = new BigDecimal(codeStr).toString();
            Intent intent = new Intent(getActivity(), LivePlayerActivity.class);
            code = "test1";
            intent.putExtra(Config.livePlayerIntentExtraKey, code);
            startActivity(intent);
        }
    }
}
