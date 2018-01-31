package com.idx.smartspeakdock.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.util.List;

/**
 * Created by ryan on 18-1-30.
 * Email: Ryan_chan01212@yeah.net
 */

public class ActivityStatusUtils {
    private static final String TAG = ActivityStatusUtils.class.getSimpleName();
    public static final String fragment_show_activity = "MainActivity";
    private static boolean isActivityTop;
    private static Fragment isFragmentTop;

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                /*
                BACKGROUND=400 EMPTY=500 FOREGROUND=100
                GONE=1000 PERCEPTIBLE=130 SERVICE=300 ISIBLE=200
                 */
                Log.i("ryan"+context.getPackageName(), "此appimportace ="
                        + appProcess.importance
                        + ",context.getClass().getName()="
                        + context.getClass().getName());
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Log.i("ryan"+context.getPackageName(), "处于后台"
                            + appProcess.processName);
                    return true;
                } else {
                    Log.i(context.getPackageName(), "处于前台"
                            + appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean isTopActivity(Context context) {
        Log.i(TAG, "isTopActivity: ActivityStatusUtils");
        isActivityTop = false;
        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        Log.i(TAG, "activity isTopActivity = " + cn.getClassName());
        if (cn.getClassName().contains(fragment_show_activity)) {
            isActivityTop = true;
        }
        Log.i(TAG, "activity isTop = " + isActivityTop);
        return isActivityTop;
    }

    public static Fragment isTopFragment(Context context,FragmentManager fragmentManager) {
        Log.i(TAG, "isTopFragment: ActivityStatusUtils");
        isFragmentTop = null;
        List<Fragment> fragments = fragmentManager.getFragments();
        Log.i(TAG, "activity isTopFragment: size = " + fragments.size());
        for (Fragment fragment : fragments) {
//            if(fragment != null && fragment.isVisible()){
            isFragmentTop = fragment;
//            }
            Log.i(TAG, "activity isTopFragment: " + isFragmentTop.getClass().getSimpleName());
        }
        return isFragmentTop;
    }

        /*public void isActivityBackground(){
        if(!isActivityTop){
            Log.i("ryan", "isActivityBackground: ");
            ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE) ;
            am.moveTaskToFront(getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
            }
        }*/

    public static void onDestroy(){
        Log.i("ryan", "onDestroy: ActivityStatusUtils");
        isActivityTop = false;
        isFragmentTop = null;
    }
}
