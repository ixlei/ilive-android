package com.example.yuchen.ilive.android;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.yuchen.ilive.android.ui.MultiToggleImageButton;
import com.laifeng.sopcastsdk.camera.CameraListener;
import com.laifeng.sopcastsdk.configuration.AudioConfiguration;
import com.laifeng.sopcastsdk.configuration.CameraConfiguration;
import com.laifeng.sopcastsdk.configuration.VideoConfiguration;
import com.laifeng.sopcastsdk.entity.Watermark;
import com.laifeng.sopcastsdk.entity.WatermarkPosition;
import com.laifeng.sopcastsdk.stream.packer.rtmp.RtmpPacker;
import com.laifeng.sopcastsdk.stream.sender.rtmp.RtmpSender;
import com.laifeng.sopcastsdk.ui.CameraLivingView;
import com.laifeng.sopcastsdk.utils.SopCastLog;
import com.laifeng.sopcastsdk.video.effect.GrayEffect;
import com.laifeng.sopcastsdk.video.effect.NullEffect;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.INVISIBLE;
import static com.laifeng.sopcastsdk.constant.SopCastConstant.TAG;

/**
 * Created by yuchen on 17/5/11.
 */

public class ILiveActivity  extends Activity{
    private CameraLivingView mLFLiveView;
    private MultiToggleImageButton mMicBtn;
    private MultiToggleImageButton mFlashBtn;
    private MultiToggleImageButton mFaceBtn;
    private MultiToggleImageButton mBeautyBtn;
    private MultiToggleImageButton mFocusBtn;
    private GestureDetector mGestureDetector;
    private GrayEffect mGrayEffect;
    private NullEffect mNullEffect;
    private ImageButton mRecordBtn;
    private boolean isGray;
    private boolean isRecording;
    private ProgressBar mProgressConnecting;
    private RtmpSender mRtmpSender;
    private VideoConfiguration mVideoConfiguration;
    private int mCurrentBps;
    private Dialog mUploadDialog;
    private EditText mAddressET;
    private int state = 1;
    private CustomerApplication customerApplication;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iilive_activity);
        customerApplication = (CustomerApplication) getApplication();
        initUi();
        initEffects();
        initViews();
        initListeners();
        initLiveView();
        initQueue();
        initRtmpAddressDialog();
        bindLoginEvent();

    }

    public void initUi() {
        String code = customerApplication.getCode();
        if(code.equals("")) {
            Log.i("init", "uiuiuii");
            View bottomBar = findViewById(R.id.bottomBar);
            View topBar = findViewById(R.id.topBar);
            View loginAuth = findViewById(R.id.loginAuth);
            topBar.setVisibility(INVISIBLE);
            bottomBar.setVisibility(INVISIBLE);
            loginAuth.setVisibility(View.VISIBLE);
        }
    }

    private void initQueue() {
        this.queue = customerApplication.getRequestQueue();
    }


    private void initEffects() {
        mGrayEffect = new GrayEffect(this);
        mNullEffect = new NullEffect(this);
    }

    private void initViews() {
        mLFLiveView = (CameraLivingView) findViewById(R.id.liveView);
        mMicBtn = (MultiToggleImageButton) findViewById(R.id.record_mic_button);
        mFlashBtn = (MultiToggleImageButton) findViewById(R.id.camera_flash_button);
        mFaceBtn = (MultiToggleImageButton) findViewById(R.id.camera_switch_button);
        mBeautyBtn = (MultiToggleImageButton) findViewById(R.id.camera_render_button);
        mFocusBtn = (MultiToggleImageButton) findViewById(R.id.camera_focus_button);
        mRecordBtn = (ImageButton) findViewById(R.id.btnRecord);
        mProgressConnecting = (ProgressBar) findViewById(R.id.progressConnecting);
    }

    private void initListeners() {
        mMicBtn.setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {
                mLFLiveView.mute(true);
            }
        });
        mFlashBtn.setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {
                mLFLiveView.switchTorch();
            }
        });
        mFaceBtn.setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {
                mLFLiveView.switchCamera();
            }
        });
        mBeautyBtn.setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {
                if(isGray) {
                    mLFLiveView.setEffect(mNullEffect);
                    isGray = false;
                } else {
                    mLFLiveView.setEffect(mGrayEffect);
                    isGray = true;
                }
            }
        });
        mFocusBtn.setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {
                mLFLiveView.switchFocusMode();
            }
        });
        mRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecording) {
                    mProgressConnecting.setVisibility(View.GONE);
                    Toast.makeText(ILiveActivity.this, "stop living", Toast.LENGTH_SHORT).show();
                    mRecordBtn.setBackgroundResource(R.mipmap.ic_record_start);
                    mLFLiveView.stop();
                    isRecording = false;
                } else {
                    String code = customerApplication.getCode();
                    if(code.equals("")) {
                        Toast.makeText(ILiveActivity.this, "重新登陆", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String uploadUrl = "rtmp://192.168.2.1:1935/ilive/" + code;
                    mRtmpSender.setAddress(uploadUrl);
                    mProgressConnecting.setVisibility(View.VISIBLE);
                    Toast.makeText(ILiveActivity.this, "start connecting", Toast.LENGTH_SHORT).show();
                    mRecordBtn.setBackgroundResource(R.mipmap.ic_record_stop);
                    mRtmpSender.connect();
                    isRecording = true;
                }
            }
        });
    }

    private void initRtmpAddressDialog() {

    }

    private void initLiveView() {
        SopCastLog.isOpen(true);
        mLFLiveView.init();
        CameraConfiguration.Builder cameraBuilder = new CameraConfiguration.Builder();
        cameraBuilder.setOrientation(CameraConfiguration.Orientation.PORTRAIT)
                .setFacing(CameraConfiguration.Facing.BACK);
        CameraConfiguration cameraConfiguration = cameraBuilder.build();
        mLFLiveView.setCameraConfiguration(cameraConfiguration);

        VideoConfiguration.Builder videoBuilder = new VideoConfiguration.Builder();
        videoBuilder.setSize(640, 360);
        mVideoConfiguration = videoBuilder.build();
        mLFLiveView.setVideoConfiguration(mVideoConfiguration);

        //设置水印
//        Bitmap watermarkImg = BitmapFactory.decodeResource(getResources(), R.mipmap.watermark);
//        Watermark watermark = new Watermark(watermarkImg, 50, 25, WatermarkPosition.WATERMARK_ORIENTATION_BOTTOM_RIGHT, 8, 8);
//        mLFLiveView.setWatermark(watermark);

        //设置预览监听
        mLFLiveView.setCameraOpenListener(new CameraListener() {
            @Override
            public void onOpenSuccess() {
                Toast.makeText(ILiveActivity.this, "camera open success", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onOpenFail(int error) {
                Toast.makeText(ILiveActivity.this, "camera open fail", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCameraChange() {
                Toast.makeText(ILiveActivity.this, "camera switch", Toast.LENGTH_LONG).show();
            }
        });

        //设置手势识别
        mGestureDetector = new GestureDetector(this, new GestureListener());
        mLFLiveView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return false;
            }
        });

        //初始化flv打包器
        RtmpPacker packer = new RtmpPacker();
        packer.initAudioParams(AudioConfiguration.DEFAULT_FREQUENCY, 16, false);
        mLFLiveView.setPacker(packer);
        //设置发送器
        mRtmpSender = new RtmpSender();
        mRtmpSender.setVideoParams(640, 360);
        mRtmpSender.setAudioParams(AudioConfiguration.DEFAULT_FREQUENCY, 16, false);
        mRtmpSender.setSenderListener(mSenderListener);
        mLFLiveView.setSender(mRtmpSender);
        mLFLiveView.setLivingStartListener(new CameraLivingView.LivingStartListener() {
            @Override
            public void startError(int error) {
                //直播失败
                Toast.makeText(ILiveActivity.this, "start living fail", Toast.LENGTH_SHORT).show();
                mLFLiveView.stop();
            }

            @Override
            public void startSuccess() {
                //直播成功
                Toast.makeText(ILiveActivity.this, "start living", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void bindLoginEvent() {
        TextView register = (TextView) findViewById(R.id.register);
        final Button btn = (Button)findViewById(R.id.loginBtn);
        if(state == 1) {
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    state = 1;
                    TextView forgetPassword = (TextView) findViewById(R.id.forgetPassword);
                    forgetPassword.setVisibility(INVISIBLE);
                    TextView t = (TextView) findViewById(R.id.register);
                    t.setText("登陆");
                    btn.setText("注册");
                }
            });
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText usernameView = (EditText) findViewById(R.id.userCode);
                    EditText passwordView = (EditText) findViewById(R.id.password);
                    final String username = usernameView.getText().toString();
                    final String password = passwordView.getText().toString();

                    if (username.equals("")) {
                        Toast.makeText(ILiveActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (password.equals("")) {
                        Toast.makeText(ILiveActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String url = "http://192.168.2.1:8001/user/login";
                    JSONObject params = new JSONObject();
                    try {
                        params.put("nickname", username);
                        params.put("password", password);
                    } catch (JSONException e) {
                        Log.i("params", "error");
                        return;
                    }

                    JsonObjectRequest loginReq = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int ret = (Integer)response.get("ret");
                                if(ret == 0) {
                                    int code = (Integer) response.get("code");

                                    StringBuilder sb = new StringBuilder();
                                    sb.append("");
                                    sb.append(code);
                                    String codeString = sb.toString();

                                    customerApplication.setCode(codeString);

                                    Toast.makeText(ILiveActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();

                                    View loginAuth = findViewById(R.id.loginAuth);
                                    loginAuth.setVisibility(INVISIBLE);
                                    View bottomBar = findViewById(R.id.bottomBar);
                                    bottomBar.setVisibility(View.VISIBLE);
                                    View topBar = findViewById(R.id.topBar);
                                    topBar.setVisibility(View.VISIBLE);


                                    //HashMap<String, String> header = new ObjectMapper().readValue(response.getJSONObject("headers"), HashMap.class);
                                    //customerApplication.addSessionCookie(header);
                                }
                            } catch (JSONException e) {
                                Toast.makeText(ILiveActivity.this, "参数错误", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(ILiveActivity.this, "错误，稍后重试", Toast.LENGTH_SHORT).show();
                            Log.i("error", error.getMessage());
                        }
                    }) {
//                        @Override
//                        protected Map<String, String> getParams() {
//                            Map<String, String> params = new HashMap<>();
//                            params.put("nickname", username);
//                            params.put("password", password);
//                            return params;
//                        }
                        @Override
                        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                            try {
                                String jsonString = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                                JSONObject jsonResponse = new JSONObject(jsonString);
                                jsonResponse.put("headers", new JSONObject(response.headers));
                                return Response.success(jsonResponse,
                                        HttpHeaderParser.parseCacheHeaders(response));
                            } catch (UnsupportedEncodingException e) {
                                return Response.error(new ParseError(e));
                            } catch (JSONException je) {
                                return Response.error(new ParseError(je));
                            }
                        }
                    };
                    if(queue != null) {
                        queue.add(loginReq);
                    }
                }
            });
        }
        if(state == 1) {
            TextView forgetPassword = (TextView) findViewById(R.id.forgetPassword);
            forgetPassword.setVisibility(INVISIBLE);
            register.setText("登陆");

         }

    }

    private RtmpSender.OnSenderListener mSenderListener = new RtmpSender.OnSenderListener() {
        @Override
        public void onConnecting() {

        }

        @Override
        public void onConnected() {
            mProgressConnecting.setVisibility(View.GONE);
            mLFLiveView.start();
            mCurrentBps = mVideoConfiguration.maxBps;
        }

        @Override
        public void onDisConnected() {
            mProgressConnecting.setVisibility(View.GONE);
            Toast.makeText(ILiveActivity.this, "fail to live", Toast.LENGTH_SHORT).show();
            mRecordBtn.setBackgroundResource(R.mipmap.ic_record_start);
            mLFLiveView.stop();
            isRecording = false;
        }

        @Override
        public void onPublishFail() {
            mProgressConnecting.setVisibility(View.GONE);
            Toast.makeText(ILiveActivity.this, "fail to publish stream", Toast.LENGTH_SHORT).show();
            mRecordBtn.setBackgroundResource(R.mipmap.ic_record_start);
            isRecording = false;
        }

        @Override
        public void onNetGood() {
            if (mCurrentBps + 50 <= mVideoConfiguration.maxBps){
                SopCastLog.d(TAG, "BPS_CHANGE good up 50");
                int bps = mCurrentBps + 50;
                if(mLFLiveView != null) {
                    boolean result = mLFLiveView.setVideoBps(bps);
                    if(result) {
                        mCurrentBps = bps;
                    }
                }
            } else {
                SopCastLog.d(TAG, "BPS_CHANGE good good good");
            }
            SopCastLog.d(TAG, "Current Bps: " + mCurrentBps);
        }

        @Override
        public void onNetBad() {
            if (mCurrentBps - 100 >= mVideoConfiguration.minBps){
                SopCastLog.d(TAG, "BPS_CHANGE bad down 100");
                int bps = mCurrentBps - 100;
                if(mLFLiveView != null) {
                    boolean result = mLFLiveView.setVideoBps(bps);
                    if(result) {
                        mCurrentBps = bps;
                    }
                }
            } else {
                SopCastLog.d(TAG, "BPS_CHANGE bad down 100");
            }
            SopCastLog.d(TAG, "Current Bps: " + mCurrentBps);
        }
    };

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1.getX() - e2.getX() > 100
                    && Math.abs(velocityX) > 200) {
                // Fling left
                Toast.makeText(ILiveActivity.this, "Fling Left", Toast.LENGTH_SHORT).show();
            } else if (e2.getX() - e1.getX() > 100
                    && Math.abs(velocityX) > 200) {
                // Fling right
                Toast.makeText(ILiveActivity.this, "Fling Right", Toast.LENGTH_SHORT).show();
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLFLiveView.pause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLFLiveView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLFLiveView.stop();
        mLFLiveView.release();
    }
}
