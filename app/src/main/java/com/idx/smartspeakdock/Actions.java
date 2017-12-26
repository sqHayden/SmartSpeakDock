package com.idx.smartspeakdock;

/**
 * Created by derik on 17-12-22.
 */

public interface Actions {

    interface Calender {
        String OPEN_CALENDER = "faq_open_calender_satisfy";
        String CLOSE_CALENDER = "faq_close_calender_satisfy";
    }

    interface Weather {
        String OPEN_WEATHER = "faq_open_weather_satisfy";
        String CLOSE_WEATHER = "faq_close_weather_satisfy";
    }

    interface Map {
        String OPEN_MAP = "faq_open_map_satisfy";
        String CLOSE_MAP = "faq_close_map_satisfy";
    }

    interface Music {
        String OPEN_MUSIC = "faq_open_music_satisfy";
        String CLOSE_MUSIC = "faq_close_music_satisfy";
        String MUSIC_NAME = "music_play_user_music_index2_clarify";
        String MUSIC_PLAY = "faq_music_play_satisfy";
        String MUSIC_PAUSE = "faq_music_pause_satisfy";
        String MUSIC_STOP = "faq_music_stop_satisfy";
        String MUSIC_NEXT = "faq_music_next_satisfy";
        String MUSIC_PREVIOUS = "faq_music_previous_satisfy";
    }

    interface Shopping {
        String OPEN_SHOPPING = "faq_open_Shopping_satisfy";
        String CLOSE_SHOPPING = "faq_close_Shopping_satisfy";
    }
}
