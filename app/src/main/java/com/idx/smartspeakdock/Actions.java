package com.idx.smartspeakdock;

/**
 * Created by derik on 17-12-22.
 */

public interface Actions {
    String OPEN_MODULE_NAME = "open_user_module_name_clarify";
    String OPEN_NOW = "open_satisfy";
    String EXIT_VOICE = "voice_session_exit_satisfy";
    String HELP_MODULE_NAME = "help_user_module_name_clarify";
    String HELP = "help_satisfy";

    interface Calender {
        String CALENDER_WEEK_INFO = "calender_week_info_satisfy";
        String CALENDER_TIME_INFO = "calender_time_info_satisfy";
        String CALENDER_FESTIVAL_INFO = "calender_festival_info_satisfy";
        String CALENDER_FESTIVAL_DATE = "calendar_festival_date_satisfy";
        String CALENDER_ACT_INFO = "calender_act_info_satisfy";
        String CALENDER_DATE_INFO = "calender_date_info_satisfy";
        String CALENDER_LUNAR_DATE_INFO = "calender_lunar_date_info_satisfy";
    }

    interface Weather {
        // 天气信息
        String TODAY_WEATHER_INFO = "today_weather_info_satisfy";
        String CITY_TODAY_WEATHER_INFO = "city_today_weather_info_satisfy";
        String TIME_TODAY_WEATHER_INFO = "time_today_weather_info_satisfy";
        String NO_TODAY_WEATHER_INFO = "no_today_weather_info_satisfy";
        //最温差信息
        String WEATHER_RANGE_INFO = "weather_temp_info_satisfy";
        String WEATHER_CITY_RANGE_INFO = "weather_city_temp_info_satisfy";
        String WEATHER_TIME_RANGE_INFO = "weather_time_temp_info_satisfy";
        String WEATHER_NO_RANGE_INFO = "weather_no_temp_info_satisfy";
        //空气质量信息
        String WEATHER_AIR_QUALITY_INFO = "weather_air_quality_info_satisfy";
        String WEATHER_CITY_AIR_QUALITY_INFO = "weather_city_air_quality_info_satisfy";
        String WEATHER_TIME_AIR_QUALITY_INFO = "weather_time_air_quality_info_satisfy";
        String WEATHER_NO_AIR_QUALITY_INFO = "weather_no_air_quality_info_satisfy";
        //当前温度信息
        String WEATHER_CURRENT_TEMP_INFO = "weather_current_temp_info_satisfy";
        String WEATHER_NO_CURRENT_TEMP_INFO = "weather_no_current_temp_info_satisfy";
        //天气状况信息
        String WEATHER_STATUS_INFO = "weather_status_info_satisfy";
        String WEATHER_CITY_STATUS_INFO = "weather_city_status_info_satisfy";
        String WEATHER_TIME_STATUS_INFO = "weather_time_status_info_satisfy";
        String WEATHER_NO_STATUS_INFO = "weather_no_status_info_satisfy";
        //下雨信息
        String WEATHER_RAIN_INFO = "weather_rain_info_satisfy";
        String WEATHER_CITY_RAIN_INFO = "weather_city_rain_info_satisfy";
        String WEATHER_TIME_RAIN_INFO = "weather_time_rain_info_satisfy";
        String WEATHER_NO_RAIN_INFO = "weather_no_rain_info_satisfy";
        //穿衣信息
        String WEATHER_DRESS_INFO = "weather_dress_info_satisfy";
        String WEATHER_CITY_DRESS_INFO = "weather_city_dress_info_satisfy";
        String WEATHER_TIME_DRESS_INFO = "weather_time_dress_info_satisfy";
        //紫外线强度
        String WEATHER_UITRAVIOLET_LEVEL_INFO = "weather_uitraviolet_level_info_satisfy";
        String WEATHER_CITY_UITRA_LEVEL_INFO = "weather_city_uitra_level_info_satisfy";
        String WEATHER_TIME_UITRA_LEVEL_INFO = "weather_time_uitra_level_info_satisfy";
        //雾霾信息
        String WEATHER_SMOG_INFO = "weather_smog_info_satisfy";
        String WEATHER_CITY_SMOG_INFO = "weather_city_smog_info_satisfy";
        String WEATHER_TIME_SMOG_INFO = "weather_time_smog_info_satisfy";
    }

    interface Map {
        String MAP_LOCATION_INFO = "map_location_info_satisfy";
        String MAP_SEARCH_AREA = "map_search_info_user_map_search_area_clarify";
        String MAP_SEARCH_NAME = "map_search_info_user_map_search_name_clarify";
        String MAP_SEARCH_INFO = "map_search_info_satisfy";
        String MAP_SEARCH_ADDRESS = "map_search_address_satisfy";
        //        String MAP_PATH_TO_NAME = "map_path_info_user_path_to_name_clarify";
        String MAP_PATH_FROM_NAME = "map_path_info_user_path_from_name_clarify";
        String MAP_PATH_WAY = "map_path_info_user_map_path_way_clarify";
        String MAP_PATH_INFO = "map_path_info_satisfy";
    }

    interface Music {
        String MUSIC_INDEX = "music_play_user_music_index_clarify";
        String MUSIC_NAME = "music_play_user_music_name_clarify";
        String MUSIC_PLAY = "music_play_satisfy";
        String MUSIC_PAUSE = "music_pause_satisfy";
        String MUSIC_STOP = "music_stop_satisfy";
        String MUSIC_CONTINUE = "music_continue_satisfy";
        String MUSIC_NEXT = "music_next_satisfy";
        String MUSIC_PREVIOUS = "music_previous_satisfy";
    }

    interface Shopping {
        String SHOPPING_SWITCH = "shopping_switch_satisfy";
        String SHOPPING_ME_CLASSIFY = "shopping_me_classify_satisfy";
        String SHOPPING_PHONE = "shopping_phone_satisfy";
        String SHOPPING_PHONE_ACCESS = "shopping_phone_access_satisfy";
        String SHOPPING_SMART_DEVICE = "shopping_smart_device_satisfy";
        String SHOPPING_CAR_VERHIELE = "shopping_car_verhiele_satisfy";
        String SHOPPING_IPHONE_ACCESS = "shopping_iphone_access_satisfy";
        String SHOPPING_COM_DESKTOP = "shopping_com_desktop_satisfy";
        String SHOPPING_COM_COMPUTERS = "shopping_com_computers_satisfy";
        String SHOPPING_COM_PERIPHERALS = "shopping_com_peripherals_satisfy";
        String SHOPPING_SMART_SHARPTV = "shopping_smart_sharptv_satisfy";
        String SHOPPING_SMART_LIFEELECT = "shopping_smart_lifeelect_satisfy";
        String SHOPPING_SMART_CAREHEALT = "shopping_smart_carehealt_satisfy";
        String SHOPPING_SMART_KITCHEN = "shopping_smart_kitchen_satisfy";
        String SHOPPING_SMART_FAMILYAUD = "shopping_smart_familyaud_satisfy";
        String SHOPPING_SMART_ICEBOX = "shopping_smart_icebox_satisfy";
        String SHOPPING_SMART_WASHMACHI = "shopping_smart_washmachi_satisfy";
    }
}
