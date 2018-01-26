package com.idx.smartspeakdock.utils;

/**
 * Created by ryan on 17-12-28.
 * Email: Ryan_chan01212@yeah.net
 */

public interface GlobalUtils {

    interface WhichFragment {
        //reconginize which fragment
        String RECONGINIZE_WHICH_FRAGMENT = "reconginize.which.fragment";
        String WEATHER_FRAGMENT_INTENT_ID = "weather.fragment.intent.id";
        String CALENDAR_FRAGMENT_INTENT_ID = "calendar.fragment.intent.id";
        String MUSIC_FRAGMENT_INTENT_ID = "music.fragment.intent.id";
        String MAP_FRAGMENT_INTENT_ID = "map.fragment.intent.id";
        String SHOPPING_FRAGMENT_INTENT_ID = "shopping.fragment.intent.id";
        String START_FRAGMENT_INTENT_ID = "start.fragment.intent.id";
        String SETTING_FRAGMENT_INTENT_ID = "setting.fragment.intent.id";
        //current fragment id
        String CURRENT_FRAGMENT_ID = "current.fragment";

        //first change fragment
        String FIRST_CHANGE_FRAGMENT = "first.change.fragment";
    }

    interface Shopping {
        //shopping website id
        String SHOPPING_WEBSITES_EXTRA_ID = "shopping.websitpues.extra.id";
        String IPHONE = "苹果专区";
        String SHARPE = "夏普专区";
        String FIND = "发现";
        String BUSSIESE_GROUP = "企业团购";
        String LOGIN_PAGE = "登录页面";
        String FLNET = "富连网";
        String register_page = "注册页面";
        String SHOPPING_CART = "购物车";

        //shopping broadcast
        String SHOPPING_BROADCAST_ACTION = "shopping.broadcast.action";
    }

    interface Weather {
        //weather broadcast
        String WEATHER_BROADCAST_ACTION = "weather.broadcast.action";
        //weather part
        String WEATHER_TIME_TODAY = "今天";
        String WEATHER_TIME_TOMM = "明天";
        String WEATHER_TIME_POSTNATAL = "后天";
        //weather voice flag
        int WEATHER_VOICE_FLAG = 5;
    }

    interface Music {
        //music
        String MUSIC_NAME_ID = "music.name.id";
        String MUSIC_BROADCAST_ACTION = "music.broadcast.action";
    }

    interface Map {
        //map broadcast
        public static final String MAP_BROADCAST_ACTION = "map.broadcast.action";
        //map part
        public static final int MAP_VOICE_FLAG = 6;
    }

    interface FirstSatrt {
        //first Satrt app
        String FIRST_APP_START = "app.first.start";
    }
}
