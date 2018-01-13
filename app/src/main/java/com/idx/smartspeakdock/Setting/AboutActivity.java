package com.idx.smartspeakdock.Setting;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;

/**
 * Created by ryan on 18-1-5.
 * Email: Ryan_chan01212@yeah.net
 */

public class AboutActivity extends BaseActivity {
    private static final String TAG = AboutActivity.class.getSimpleName();
    private Toolbar toolbar;
    private TextView app_version_show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initToolbar();
        initView();
    }

    private void initView() {
        app_version_show = findViewById(R.id.app_version);

        PackageManager pm = getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo("com.idx.smartspeakdock",0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        app_version_show.setText("Version v" + packageInfo.versionName);

    }

    private void initToolbar() {
            toolbar = (Toolbar) findViewById(R.id.about_toolbar);
            setSupportActionBar(toolbar);
            ActionBar ab = getSupportActionBar();
            ab.setHomeAsUpIndicator(R.drawable.back);
            ab.setTitle(getResources().getString(R.string.about));
            ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
