package com.idx.smartspeakdock.setting;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.utils.BitmapUtils;

/**
 * Created by ryan on 18-1-5.
 * Email: Ryan_chan01212@yeah.net
 */

public class AboutActivity extends BaseActivity {
    private static final String TAG = AboutActivity.class.getSimpleName();
    private Toolbar toolbar;
    private TextView app_version_show;
    private Bitmap mBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initToolbar();
        initView();
        //绑定service
        bindService(mControllerintent, myServiceConnection, 0);
    }

    private void initView() {
        app_version_show = findViewById(R.id.app_version);
        mBitmap = BitmapUtils.decodeBitmapFromResources(this, R.drawable.back);
        PackageManager pm = getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo("com.idx.smartspeakdock",0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        app_version_show.setText(getResources().getString(R.string.version)+" v" + packageInfo.versionName);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.about_toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorSelfBlack));
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        mBitmap = BitmapUtils.scaleBitmapFromResources(this, R.drawable.back,15,30);
        ab.setHomeAsUpIndicator(new BitmapDrawable(mBitmap));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        unbindService(myServiceConnection);
    }
}
