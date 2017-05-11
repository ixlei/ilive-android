package com.example.yuchen.ilive.android;

import android.app.Fragment;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements TabLayout.onTabClickListener{

    private TabLayout tabLayout;
    public final String liveIntentExtraKey = "COM_EXAMPLE_YUCHEN_ILIVE_ANDROID";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        renderView();
        setActionBar();
    }

    public void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.hot_live_actionbar);
    }

    public void renderView() {
        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        ArrayList<Class<?>> fragments = new ArrayList<>();
        fragments.add(HotLive.class);
        fragments.add(Following.class);
        fragments.add(LiveActivity.class);
        fragments.add(HotList.class);
        fragments.add(User.class);
        tabLayout.initData(fragments, this);
        replaceFragment(HotLive.class, 0);
    }

    public void replaceFragment(Class<?> cls, int index) {
        //live
        if(cls == LiveActivity.class) {
            super.onResume();
            Intent liveIntent = new Intent(MainActivity.this, ILiveActivity.class);
            liveIntent.putExtra(liveIntentExtraKey, "from main activity");
            startActivity(liveIntent);
            return;
        }
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
        this.replaceFragment(cls, index);
    }

}
