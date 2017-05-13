package com.example.yuchen.ilive.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yuchen.ilive.android.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by yuchen on 17/5/13.
 */

public class HotLiveList extends BaseAdapter  {

    //private LayoutInflater layoutInflater;
    private List<HashMap<String, Object>> list;
    private Context context;
    private View.OnClickListener listener;

    public HotLiveList(Context context, List<HashMap<String, Object>> list, View.OnClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View DefineView =  LayoutInflater.from(context).inflate(R.layout.hot_live_item, null);
        HashMap<String, Object> hash = list.get(position);

        ImageView imageView = (ImageView) DefineView.findViewById(R.id.liveAvatar);
        TextView titleView = (TextView) DefineView.findViewById((R.id.nickname));
        TextView contentView = (TextView) DefineView.findViewById(R.id.audienceNum);

//        Uri uri = Uri.parse((String)hash.get("liveAvatar"));
//        imageView.setImageURI(uri);
//        Bitmap bitmap = BitmapFactory.decodeFile((String)hash.get("liveAvatar"));
//        imageView.setImageBitmap(bitmap);

        imageView.setImageResource(R.drawable.avatar);
        titleView.setText((String)hash.get("nickname"));
        Double d = new Double((double)hash.get("audience"));
        contentView.setText( d.intValue() + "人");

        DefineView.setOnClickListener(this.listener);
        DefineView.setTag(position);
        return DefineView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }
}