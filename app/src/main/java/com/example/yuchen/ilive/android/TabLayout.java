package com.example.yuchen.ilive.android;

import android.app.Fragment;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.zhy.autolayout.AutoLinearLayout;

import java.util.ArrayList;

import static android.R.id.tabs;

/**
 * Created by yuchen on 17/4/26.
 */

public class TabLayout extends AutoLinearLayout implements View.OnClickListener {

    private Context context;
    private onTabClickListener listener;
    private ArrayList<View> tabItem;
    private ArrayList<Class<?>> classList;
    private View currentActView = null;

    public TabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }


    @Override
    public void onClick(View v) {
        int index = (int)v.getTag();
        if(index != 2) {
            setCurrentActView((int)v.getTag());
        }
        listener.onTabClick(classList.get((int)v.getTag()), (int)v.getTag());
    }


    public void initData(ArrayList<Class<?>> classList, onTabClickListener listener) {
        ArrayList<View> tabItem = getChildren(this);
        this.tabItem = tabItem;
        this.classList = classList;
        this.listener = listener;
        for(int i = 0, ii = tabItem.size(); i < ii; i++) {
            tabItem.get(i).setTag(i);
            tabItem.get(i).setOnClickListener(this);
        }

        setCurrentActView(0);
    }

    public void setCurrentActView(int index) {
        View currTemp = tabItem.get(index);
        if(currentActView == null) {
            currentActView = currTemp;
            ((BottomBar)currTemp).setImageSourceDraw(1);
            currTemp.setSelected(true);
            return;
        }
        if(currTemp != currentActView) {
            currTemp.setSelected(true);
            ((BottomBar)currTemp).setImageSourceDraw(1);
            ((BottomBar)currentActView).setImageSourceDraw(0);
            currentActView.setSelected(false);
            currentActView = currTemp;
        }

    }

    public ArrayList<View> getChildren(View v) {
        ViewGroup view = (ViewGroup)v;
        ArrayList<View> list = new ArrayList<>();
        for(int i = 0, ii = view.getChildCount(); i < ii; i++) {
            list.add(view.getChildAt(i));
        }
        return list;

    }

    public interface onTabClickListener {
        void onTabClick(Class<?> cls, int index);
    }
}
