package com.example.yuchen.ilive.android;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhy.autolayout.utils.AutoUtils;

/**
 * Created by yuchen on 17/4/25.
 */

public class BottomBar extends LinearLayout implements View.OnClickListener{
    private Context context;

    private View itemView;

    private Drawable icon;
    private Drawable hotIcon;
    private String tipsText;
    private float tipsTextSize;
    private float iconHeight;
    private float iconWidth;
    private float marginTop;
    private float marginBottom;
    private float itemColor;
    private float percentWidth;
    private float percentHeight;
    private float tipsTextMarginBottom;


    private TextView tipsTextView = null;
    private ImageView iconImageView = null;

    private float TIPS_TEXT_SIZE = 14;
    private float ICON_HEIGHT = 20;
    private float ICON_WIDTH = 20;
    private float MARGIN_TOP = 8;
    private float MARGIN_BOTTOM = 1;
    private float TIPS_TEXT_MARGIN_BOTTOM = 10;

    public BottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray typedArr = context.obtainStyledAttributes(attrs, R.styleable.BottomItem);
        hotIcon = typedArr.getDrawable(R.styleable.BottomItem_active_icon);
        icon = typedArr.getDrawable(R.styleable.BottomItem_icon);
        tipsText = typedArr.getString(R.styleable.BottomItem_tips_text);
        itemColor = typedArr.getColor(R.styleable.BottomItem_item_color, 0xff333333);

        tipsTextSize = typedArr.getFloat(R.styleable.BottomItem_tips_text_size, TIPS_TEXT_SIZE);
        percentWidth = typedArr.getDimension(R.styleable.BottomItem_icon_width, ICON_WIDTH);
        percentHeight = typedArr.getDimension(R.styleable.BottomItem_icon_height, ICON_HEIGHT);
        marginTop = typedArr.getDimension(R.styleable.BottomItem_margin_top, MARGIN_TOP);
        marginBottom = typedArr.getDimension(R.styleable.BottomItem_margin_bottom, MARGIN_BOTTOM);
        tipsTextMarginBottom = typedArr.getDimension(R.styleable.BottomItem_tips_text_margin_bottom, TIPS_TEXT_MARGIN_BOTTOM);
        percentWidth = AutoUtils.getPercentWidthSize((int) percentWidth);
        percentHeight = AutoUtils.getPercentHeightSize((int) percentHeight);
        marginTop = AutoUtils.getPercentWidthSize((int) marginTop);


        if (percentWidth == ICON_WIDTH) {
            resetSize();
        }

        renderView();

        //Log.i("dd", icon.toString());


        typedArr.recycle();


    }

    private void renderView() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        itemView = inflater.inflate(R.layout.bottombar_item, this, true);

        iconImageView = (ImageView)itemView.findViewById(R.id.icon);
        tipsTextView = (TextView)itemView.findViewById(R.id.tipsText);


        tipsTextView.setText(tipsText);
        tipsTextView.setTextColor((int)itemColor);
        tipsTextView.setTextSize((int)tipsTextSize);

        LayoutParams tipsLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tipsLayoutParams.setMargins(0, 0, 0, (int)tipsTextMarginBottom);
        tipsTextView.setLayoutParams(tipsLayoutParams);

        float[] imageSize = getImageSize();
        LayoutParams imageLayoutParams = new LayoutParams((int)imageSize[0], (int)imageSize[1]);
        imageLayoutParams.setMargins(0, (int)marginTop, 0, (int)marginBottom);
        iconImageView.setLayoutParams(imageLayoutParams);
        iconImageView.setBackgroundDrawable(icon);

    }

    public void setImageSourceDraw(int state) {
        if(state == 0) {
            tipsTextView.setTextColor(getResources().getColor(R.color.tab_color));
            iconImageView.setBackgroundDrawable(icon);
            return;
        }
        iconImageView.setBackgroundDrawable(hotIcon);
        tipsTextView.setTextColor(getResources().getColor(R.color.icon_color_active));


    }

    public void setDrawableColor(Drawable drawable, String color) {
        int iColor = Color.parseColor(color);
        int red   = (iColor & 0xFF0000) / 0xFFFF;
        int green = (iColor & 0xFF00) / 0xFF;
        int blue  = iColor & 0xFF;

        float[] matrix = { 0, 0, 0, 0, red,
                0, 0, 0, 0, green,
                0, 0, 0, 0, blue,
                0, 0, 0, 1, 0 };

        ColorFilter colorFilter = new ColorMatrixColorFilter(matrix);
        drawable.setColorFilter(colorFilter);
    }

    public float[] getImageSize() {
        iconHeight = icon.getIntrinsicHeight();
        iconWidth = icon.getIntrinsicWidth();

        return (percentHeight / iconHeight) <  (percentWidth / iconWidth)
                ? new float[] {(percentHeight / iconHeight) * iconWidth, (percentHeight / iconHeight) * iconHeight}
                : new float[] {(percentWidth / iconWidth) * iconWidth, (percentWidth / iconWidth) * iconHeight};

    }



    private void resetSize() {
        percentWidth = dp2px(context, percentWidth);
        percentHeight = dp2px(context, percentHeight);
        marginTop = dp2px(context, marginTop);
        marginBottom = dp2px(context, marginBottom);
    }

    private int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void onClick(View v) {
        Log.i("bottom item", "handle click");
    }

}
