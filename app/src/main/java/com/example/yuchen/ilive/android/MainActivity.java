package com.example.yuchen.ilive.android;

import android.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import static android.R.id.tabs;

public class MainActivity extends AppCompatActivity implements TabLayout.onTabClickListener{

    private TabLayout tabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        renderView();
    }

    public void renderView() {
        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        ArrayList<Class<?>> fragments = new ArrayList<>();
        fragments.add(HotLive.class);
        fragments.add(Following.class);
        fragments.add(Live.class);
        fragments.add(User.class);
        tabLayout.initData(fragments, this);
        replaceFragment(HotLive.class);
    }

    public void replaceFragment(Class<?> cls) {
        try {
            Log.i("fragment", cls.getName());
            Fragment fragment =  (Fragment)cls.newInstance();
            getFragmentManager().beginTransaction().replace(R.id.viewPage, fragment).commitAllowingStateLoss();
        } catch (IllegalAccessException e) {
            Log.i("tab click", e.getMessage());
        } catch (InstantiationException e) {
            Log.i("tab click", e.getMessage());
        }

    }

    @Override
    public void onTabClick(Class<?> cls, int index) {
        this.replaceFragment(cls);
    }

}
