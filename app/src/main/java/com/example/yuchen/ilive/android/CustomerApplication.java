package com.example.yuchen.ilive.android;

import android.app.Application;
import android.content.SharedPreferences;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.yuchen.ilive.android.amf.StringAmf;

import java.util.Map;

/**
 * Created by yuchen on 17/5/11.
 */

public class CustomerApplication extends Application {
    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String COOKIE_USERNAME = "ilive";

    //请求队列
    private RequestQueue _requestQuene;

    //SharedPreferences,用于存储少量的数据
    private SharedPreferences _preferences;

    //本类的实例
    private static CustomerApplication _instance;

    public static CustomerApplication get() {
        return _instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        _preferences = getSharedPreferences(Config.appConstant,0);
        _requestQuene = Volley.newRequestQueue(this);
    }

    public RequestQueue getRequestQueue() {
        return _requestQuene;
    }
    /**
     * 用于检测返回头中包含的cookie
     * 并且更新本地存储的cookie
     * @param headers
     */
    public final void checkSessionCookie(Map<String, String> headers) {
        if (headers.containsKey(SET_COOKIE_KEY)) {
            String cookie = headers.get(SET_COOKIE_KEY);
            if((cookie.length()) > 0) {
                String[] splitCookie = cookie.split(";");
                String[] splitSessionId = splitCookie[0].split("=");
                cookie = splitSessionId[1];
                SharedPreferences.Editor prefEditor = _preferences.edit();
                prefEditor.putString(COOKIE_USERNAME, cookie);
                prefEditor.apply();
            }
        }
    }
    /**
     * 向请求头中加入cookie
     * @param headers
     */
    public final void addSessionCookie(Map<String, String> headers) {
        String sessionId = _preferences.getString(COOKIE_USERNAME, "");
        if (sessionId.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(COOKIE_USERNAME);
            builder.append("=");
            builder.append(sessionId);
            if (headers.containsKey(COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(COOKIE_KEY));
            }
            headers.put(COOKIE_KEY, builder.toString());
        }
    }

    public String getUser(String defaultValue) {
        return _preferences.getString(COOKIE_USERNAME, defaultValue);
    }
}
