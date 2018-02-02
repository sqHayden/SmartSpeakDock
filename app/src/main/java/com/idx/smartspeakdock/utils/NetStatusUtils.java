package com.idx.smartspeakdock.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ryan on 17-12-26.
 * Email: Ryan_chan01212@yeah.net
 */

public class NetStatusUtils {
    //判断当前手机是否连上Wifi.
    public static  boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                if (mWiFiNetworkInfo.isAvailable()) {
                    return mWiFiNetworkInfo.isConnected();
                } else {
                    return false;
                }
            }
        }
        return false;
    }

/*     //判断当前手机是否连上Wifi.
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                if (mWiFiNetworkInfo.isAvailable()) {
                    if (mWiFiNetworkInfo.isConnected()){
                        boolean isOnline = isNetWorkOnline(context);
                        Log.i("ryan", "isWifiConnected: isOnline = "+isOnline);
                        if (isOnline){
                            Log.i("ryan", "isWifiConnected: true");
                            return true;
                        }
                    }
                    Log.i("ryan", "isWifiConnected: false");
                    return false;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    //判断已连WiFi是否可上网
    public static boolean isNetWorkOnline(final Context context) {
        URL url = null;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL("http://www.baidu.com");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(2000);
            urlConnection.connect();
            Log.i("ryan", "isNetWorkOnline: status network = "+urlConnection.getResponseCode());
            if (urlConnection.getResponseCode() == 200) {
                Log.i("ryan", "isNetWorkOnline: true");
                return true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            urlConnection.disconnect();
        }
        Log.i("ryan", "isNetWorkOnline: false");
        return false;
    }*/



     //判断当前手机的网络是否可用.
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                if (mMobileNetworkInfo.isAvailable()) {
                    return mMobileNetworkInfo.isConnected();
                } else {
                    return false;
                }
            }
        }
        return false;
    }

     //判断当前网络是手机网络还是WIFI.
    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }
}
