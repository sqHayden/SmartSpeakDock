package com.idx.smartspeakdock.utils;

import android.content.Context;

/**
 * Created by derik on 18-1-23.
 */

public class PreUtils {
    public interface Settings {
        String SPEAK_SERVICE_ENABLE_STATE = "speak_service_enable_state";
    }

    public enum Items {
        SETTINGS("settings");

        private String desc;

        Items(String desc){
            this.desc = desc;
        }

        public String getDesc(){
            return desc;
        }

    }

    public static void setItemObject(Context context, Items item, String key, Object object) {
        ComplexPreferences complexPreferences = new ComplexPreferences(context, item.getDesc(), Context.MODE_PRIVATE);
        complexPreferences.putObject(key, object);
        complexPreferences.commit();
    }

    public static <T> T getItemObject(Context ctx, Items item, String key, Class<T> a, T defaultValue) {
        ComplexPreferences complexPreferences = new ComplexPreferences(ctx, item.getDesc(), Context.MODE_PRIVATE);
        T t = complexPreferences.getObject(key, a, defaultValue);
        return t;
    }

    public static void clearItemObject(Context ctx, Items item) {
        ComplexPreferences complexPreferences = new ComplexPreferences(ctx, item.getDesc(), Context.MODE_PRIVATE);
        complexPreferences.clearObject();
        complexPreferences.commit();
    }
}
