package com.idx.smartspeakdock;

/**
 * Created by derik on 17-12-22.
 */

public interface Actions {
    String OPEN_MODULE_NAME = "open_user_module_name_clarify";
    String OPEN_NOW = "open_satisfy";

    interface Calender {
        String CALENDER_WEEK_INFO = "calender_week_info_satisfy";
        String CALENDER_TIME_INFO = "calender_time_info_satisfy";
        String CALENDER_FESTIVAL_INFO = "calender_festival_info_satisfy";
        String CALENDER_ACT_INFO = "calender_act_info_satisfy";
        String CALENDER_DATE_INFO = "calender_date_info_satisfy";
        String CALENDER_LUNAR_DATE_INFO = "calender_lunar_date_info_satisfy";
    }

    interface Weather {
        String WEATHER_INFO = "today_weather_info_satisfy";
        String RANGE_TEMP_INFO = "weather_temp_info_satisfy";
        String AIR_QUALITY_INFO = "weather_air_quality_info_satisfy";
        String CURRENT_TEMP_INFO = "weather_current_temp_info_satisfy";
        String WEATHER_STATUS = "weather_status_info_satisfy";
        String DRESS_INFO = "weather_dress_info_satisfy";
        String UITRAVIOLET_LEVEL_INFO = "weather_uitraviolet_level_info_satisfy";
        String SMOG_INFO = "weather_smog_info_satisfy";
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
        String MUSIC_CONTINUE = "music_continue_satisfy";
        String MUSIC_NEXT = "music_next_satisfy";
        String MUSIC_PREVIOUS = "music_previous_satisfy";
    }

    interface Shopping {
        String SHOPPING_SWITCH = "shopping_switch_satisfy";
    }
}
