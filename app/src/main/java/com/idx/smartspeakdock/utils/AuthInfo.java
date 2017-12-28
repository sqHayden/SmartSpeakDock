package com.idx.smartspeakdock.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fujiayi on 2017/6/24.
 */

public class AuthInfo {

    public static final String META_APP_ID = "com.baidu.speech.APP_ID";
    public static final String META_APP_KEY = "com.baidu.speech.API_KEY";
    public static final String META_APP_SECRET = "com.baidu.speech.SECRET_KEY";
    private static HashMap<String, Object> authInfo;

    private static final String TAG = "AuthInfo";

    public static Map<String, Object> getAuthParams(final Context context) {
        if (authInfo == null) {
            try {
                authInfo = new HashMap<String, Object>(3);

                ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                String appId = Integer.toString(appInfo.metaData.getInt(META_APP_ID));
                String appKey = appInfo.metaData.getString(META_APP_KEY);
                String appSecret = appInfo.metaData.getString(META_APP_SECRET);
                
                authInfo.put(META_APP_ID, appId); // 认证相关, key, 从开放平台(http://yuyin.baidu.com)中获取的key
                authInfo.put(META_APP_KEY, appKey); // 认证相关, key, 从开放平台(http://yuyin.baidu.com)中获取的key
                authInfo.put(META_APP_SECRET, appSecret); // 认证相关, secret, 从开放平台(http://yuyin.baidu.com)secret

            } catch (Exception e) {
                e.printStackTrace();
                String message = "请在AndroidManifest.xml中配置APP_ID, API_KEY 和 SECRET_KEY";
                Logger.error(TAG, message);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                return null;
            }
        }
        return authInfo;
    }


}
