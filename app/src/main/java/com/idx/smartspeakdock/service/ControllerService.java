package com.idx.smartspeakdock.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.idx.smartspeakdock.shopping.ShoppingCallBack;
import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.unit.listener.IShoppingVoiceListener;

/**
 * Created by ryan on 18-1-16.
 * Email: Ryan_chan01212@yeah.net
 */

public class ControllerService extends Service {
    public final String TAG = "ControllerService";
    ShoppingCallBack mShoppingCallBack;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        return new MyBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "onRebind: ");
        super.onRebind(intent);
    }

    public class MyBinder extends Binder implements ControllerServiceListener {

        @Override
        public void onReturnWeburl(ShoppingCallBack shoppingCallBack) {
            mShoppingCallBack = shoppingCallBack;
        }

        @Override
        public void onTop(boolean isTopActivity, Fragment isTopFragment) {}
    }
    //注册购物语音模块
    public void registerShoppingModule(){
        UnitManager.getInstance(getApplicationContext()).setShoppingVoiceListener(new IShoppingVoiceListener() {
            @Override
            public void openSpecifyWebsites(String web_sites_url) {
                if (mShoppingCallBack != null){
                    mShoppingCallBack.onShoppingCallback(web_sites_url);
                }
            }
        });
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind: ");
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        //注册购物语音模块
        registerShoppingModule();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        if (mShoppingCallBack != null){
            mShoppingCallBack = null;
        }
    }
}
