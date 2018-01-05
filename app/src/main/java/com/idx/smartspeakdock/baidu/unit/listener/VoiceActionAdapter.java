package com.idx.smartspeakdock.baidu.unit.listener;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.idx.smartspeakdock.Actions;
import com.idx.smartspeakdock.Modules;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.SlotsTypes;
import com.idx.smartspeakdock.Swipe.SwipeActivity;
import com.idx.smartspeakdock.baidu.control.TTSManager;
import com.idx.smartspeakdock.baidu.unit.model.CommunicateResponse;
import com.idx.smartspeakdock.map.PathWay;
import com.idx.smartspeakdock.map.SearchArea;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.Logger;
import com.idx.smartspeakdock.utils.ToastUtils;
import java.util.HashMap;


/**
 * Created by derik on 17-12-22.
 */

public class VoiceActionAdapter implements IVoiceActionListener {
    private static final String TAG = VoiceActionAdapter.class.getSimpleName();
    private Context mContext;
    private Intent mIntent;

    private IWeatherVoiceListener mWeatherListener;
    private ICalenderVoiceListener mCalenderListener;
    private IMusicVoiceListener mMusicListener;
    private IMapVoiceListener mMapListener;
    private IShoppingVoiceListener mShoppingListener;

    private String web_sites_url;
//    private String recoginize_shopping_word;
    private String reconginize_city_word;
    private String reconginize_time_word;
    private String voice_answer;
    private HashMap<String, String> mSlots = new HashMap<>();

    public VoiceActionAdapter(Context context) {
        Logger.setEnable(true);
        mContext = context;
    }

    @Override
    public boolean onAction(CommunicateResponse.Action action, CommunicateResponse.Schema schema) {
        return handleAction(action, schema);
    }

    public void setWeatherListener(IWeatherVoiceListener listener) {
        mWeatherListener = listener;
    }

    public void setCalenderListener(ICalenderVoiceListener listener) {
        mCalenderListener = listener;
    }

    public void setMapListener(IMapVoiceListener listener) {
        mMapListener = listener;
    }

    public void setMusicListener(IMusicVoiceListener listener) {
        mMusicListener = listener;
    }

    public void setShoppingListener(IShoppingVoiceListener listener) {
        mShoppingListener = listener;
    }

    /**
     * @param action
     * @param schema
     * @return boolean 会话是否结束，true为结束，不再监听语音输入
     */
    private boolean handleAction(CommunicateResponse.Action action, CommunicateResponse.Schema schema) {

        Log.d("handleAction", ": " + action.actionId);
        String musicIndex = null;
        String musicName = null;
        initData();
        Log.d("handleAction name", ": " + action.actionId);
        mSlots.clear();
        for (int i = 0; i < schema.botMergedSlots.size(); i++) {
            String type = ((CommunicateResponse.Schema.MergedSlots) schema.botMergedSlots.get(i)).type;
            String word = ((CommunicateResponse.Schema.MergedSlots) schema.botMergedSlots.get(i)).original_word;
            mSlots.put(type, word);
        }

        switch (action.actionId) {

            /**开启指令*/
            case Actions.OPEN_MODULE:
                return false;
            case Actions.OPEN_NOW:
                openModule();
                return true;

            /**音乐指令*/
            case Actions.Music.MUSIC_INDEX:
            case Actions.Music.MUSIC_NAME:
                return false;
            case Actions.Music.MUSIC_PLAY:
                musicPlay();
                return true;
            case Actions.Music.MUSIC_PAUSE:
                musicPause();
                return true;
            case Actions.Music.MUSIC_CONTINUE:
                musicContinue();
                return true;
            case Actions.Music.MUSIC_NEXT:
                musicNext();
                return true;
            case Actions.Music.MUSIC_PREVIOUS:
                musicPrevious();
                return true;

            /**日历指令*/
            case Actions.Calender.CALENDER_WEEK_INFO:
                queryWeekInfo();
                return true;
            case Actions.Calender.CALENDER_FESTIVAL_INFO:
                queryFestivalInfo();
                return true;
            case Actions.Calender.CALENDER_ACT_INFO:
                queryActInfo();
                return true;
            case Actions.Calender.CALENDER_DATE_INFO:
                queryDateInfo();
                return true;
            case Actions.Calender.CALENDER_LUNAR_DATE_INFO:
                queryLunarInfo();
                return true;

            /**
             * 地图指令
             */
            case Actions.Map.MAP_LOCATION_INFO:
                queryLocationInfo();
                return true;
            case Actions.Map.MAP_SEARCH_AREA:
                return false;
            case Actions.Map.MAP_SEARCH_NAME:
                return false;
            case Actions.Map.MAP_SEARCH_INFO:
                searchInfo();
                return true;
            //只支持地区搜索
            case Actions.Map.MAP_SEARCH_ADDRESS:
                searchAddressInfo();
                return true;
            //只支持地区到地区的路线
            case Actions.Map.MAP_PATH_FROM_NAME:
                return false;
            //只支持地区到地区的路线
            case Actions.Map.MAP_PATH_WAY:
                return false;
            //只支持地区到地区的路线
            case Actions.Map.MAP_PATH_INFO:
                searchPathInfo();
                return true;

            /**购物指令*/
           /* case Actions.Shopping.SHOPPING_SWITCH:
                recoginize_shopping_word = ((CommunicateResponse.Schema.MergedSlots) schema.botMergedSlots.get(1)).original_word;
                Log.i(TAG, "handleAction: recoginize_shopping_word = "+recoginize_shopping_word);
                if (mShoppingListener != null) {
                    jude_word(recoginize_shopping_word);
                }
                return true;*/

            /**天气指令*/
            /*case Actions.Weather.WEATHER_CHECK_INFO:
                reconginize_city_word = ((CommunicateResponse.Schema.MergedSlots) schema.botMergedSlots.get(0)).original_word;;
                Log.i(TAG, "handleAction: reconginize_city_word = "+reconginize_city_word);
                if (mWeatherListener != null) mWeatherListener.onWeatherInfo(reconginize_city_word);
                return true;*/
            case Actions.Weather.WEATHER_INFO:
                refreshWeatherInfo();
                return  true;
            case Actions.Weather.RANGE_TEMP_INFO:
                rangeTempInfo();
                return true;
            case Actions.Weather.AIR_QUALITY_INFO:
                airQualityInfo();
                return true;
            case Actions.Weather.CURRENT_TEMP_INFO:
                currentTempInfo();
                return true;
            case Actions.Weather.WEATHER_STATUS:
                weatherStatus();
                return true;
            case Actions.Weather.DRESS_INFO:
                derssInfo();
                return true;
            case Actions.Weather.UITRAVIOLET_LEVEL_INFO:
                uitravioletLevelInfo();
                return true;
            case Actions.Weather.SMOG_INFO:
                smogInfo();
                return true;
            default:
                return false;
        }

    }

    private void refreshWeatherInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        Logger.info(TAG, "handleAction: reconginize_city_word = "+reconginize_city_word);
        if (mWeatherListener != null) mWeatherListener.onWeatherInfo(reconginize_city_word);
    }

    private void rangeTempInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        Logger.info(TAG, "handleAction: reconginize_city_word = "+reconginize_city_word+",reconginize_time_word = "+reconginize_time_word);
        if(mWeatherListener != null){
            voice_answer = mWeatherListener.onRangeTempInfo(reconginize_city_word,reconginize_time_word);
            if(checkVoiceAnswer(voice_answer)) TTSManager.getInstance().speak(voice_answer);
        }
    }

    private void airQualityInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        Logger.info(TAG, "handleAction: reconginize_city_word = "+reconginize_city_word);
        if(mWeatherListener != null){
            voice_answer = mWeatherListener.onAirQualityInfo(reconginize_city_word);
            if(checkVoiceAnswer(voice_answer)) TTSManager.getInstance().speak(voice_answer);
        }
    }

    private void currentTempInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        Logger.info(TAG, "handleAction: reconginize_city_word = "+reconginize_city_word);
        if(mWeatherListener != null){
            voice_answer = mWeatherListener.onCurrentTempInfo(reconginize_city_word);
            if(checkVoiceAnswer(voice_answer)) TTSManager.getInstance().speak(voice_answer);
        }
    }

    private void weatherStatus() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        Logger.info(TAG, "handleAction: reconginize_city_word = "+reconginize_city_word+",reconginize_time_word = "+reconginize_time_word);
        if(mWeatherListener != null){
            voice_answer = mWeatherListener.onWeatherStatus(reconginize_city_word,reconginize_time_word);
            if(checkVoiceAnswer(voice_answer)) TTSManager.getInstance().speak(voice_answer);
        }
    }

    private void derssInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        Logger.info(TAG, "handleAction: reconginize_city_word = "+reconginize_city_word);
        if(mWeatherListener != null){
            voice_answer = mWeatherListener.onDressInfo(reconginize_city_word);
            if(checkVoiceAnswer(voice_answer)) TTSManager.getInstance().speak(voice_answer);
        }
    }

    private void uitravioletLevelInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        Logger.info(TAG, "handleAction: reconginize_city_word = "+reconginize_city_word);
        if(mWeatherListener != null){
            voice_answer = mWeatherListener.onUitravioletLevelInfo(reconginize_city_word);
            if(checkVoiceAnswer(voice_answer)) TTSManager.getInstance().speak(voice_answer);
        }
    }

    private void smogInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        Logger.info(TAG, "handleAction: reconginize_city_word = "+reconginize_city_word);
        if(mWeatherListener != null){
            voice_answer = mWeatherListener.onSmogInfo(reconginize_city_word,reconginize_time_word);
            if(checkVoiceAnswer(voice_answer)) TTSManager.getInstance().speak(voice_answer);
        }
    }

    private void jude_word(String recoginize_shopping_word) {
        web_sites_url = "";
        Logger.info(TAG, "jude_word: recoginize_shopping_word = "+recoginize_shopping_word);
        if (!TextUtils.isEmpty(recoginize_shopping_word)) {
            switch (recoginize_shopping_word) {
                case GlobalUtils.PHONE_DIGITAI:
                    web_sites_url = "https://mall.flnet.com/gallery-6.html";
                    break;
                case GlobalUtils.COMPUTER_OFFICE:
                    web_sites_url = "https://mall.flnet.com/gallery-7.html";
                    break;
                case GlobalUtils.SMART_HOME:
                    web_sites_url = "https://mall.flnet.com/gallery-1.html";
                    break;
                case GlobalUtils.IPHONE:
                    web_sites_url = "https://mall.flnet.com/gallery.html?scontent=n,%E8%8B%B9%E6%9E%9C";
                    break;
                case GlobalUtils.SHARPE:
                    web_sites_url = "https://mall.flnet.com/gallery.html?scontent=n,%E5%A4%8F%E6%99%AE";
                    break;
                case GlobalUtils.FIND:
                    web_sites_url = "http://bbs.flnet.com/forum.php";
                    break;
                case GlobalUtils.BUSSIESE_GROUP:
                    web_sites_url = "https://mall.flnet.com/enterpriseGroup.html";
                    break;
                case GlobalUtils.LOGIN_PAGE:
                    web_sites_url = "https://account.flnet.com/sso/login";
                    break;
                default:
                    ToastUtils.showError(mContext, mContext.getResources().getString(R.string.web_sites_not_exists));
                    break;
            }
            if (web_sites_url != "") mShoppingListener.openSpecifyWebsites(web_sites_url);
        } else {}
    }

    private void initData(){
        reconginize_city_word = "";
        reconginize_city_word = "";
        voice_answer = "";
    }

    private boolean checkVoiceAnswer(String check_voice_answer){
        if(TextUtils.isEmpty(check_voice_answer)) return false;
        if(check_voice_answer.equals("")) return false;
        return true;
    }

    private void openModule() {
        String name = mSlots.get(SlotsTypes.USER_MODULE_NAME);
        mIntent = new Intent(mContext, SwipeActivity.class);
        switch (name) {
            case Modules.CALENDER:
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.CALENDAR_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                break;
            case Modules.WEATHER:
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WEATHER_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                break;
            case Modules.MAP:
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.MAP_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                break;
            case Modules.MUSIC:
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.MUSIC_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                break;
            case Modules.SHOPPING:
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.SHOPPING_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                break;
            /**购物指令*/
            case GlobalUtils.PHONE_DIGITAI:
            case GlobalUtils.COMPUTER_OFFICE:
            case GlobalUtils.SMART_HOME:
            case GlobalUtils.IPHONE:
            case GlobalUtils.SHARPE:
            case GlobalUtils.FIND:
            case GlobalUtils.BUSSIESE_GROUP:
            case GlobalUtils.LOGIN_PAGE:
                Logger.info(TAG, "openModule: name = "+name);
                if(mShoppingListener != null) jude_word(name);
                break;
        }
    }

    private void musicPlay() {
        String musicIndex = mSlots.get(SlotsTypes.USER_MUSIC_INDEX);
        String musicName = mSlots.get(SlotsTypes.USER_MUSIC_NAME);
        if (mMusicListener != null) {
            if (musicIndex != null && !musicIndex.equals("")) {
                mMusicListener.onPlay(musicIndex);
            } else if (musicName != null && !musicName.equals("")) {
                mMusicListener.onPlay(musicName);
            } else {
                mMusicListener.onPlay(0);
            }
        }
    }

    private void musicPause() {
        if (mMusicListener != null) {
            mMusicListener.onPause();
        }
    }

    private void musicContinue() {
        if (mMusicListener != null) {
            mMusicListener.onContinue();
        }
    }

    private void musicNext() {
        if (mMusicListener != null) {
            mMusicListener.onNext();
        }
    }

    private void musicPrevious() {
        if (mMusicListener != null) {
            mMusicListener.onPrevious();
        }
    }

    private void queryWeekInfo() {
        if (mCalenderListener != null) {
            String weekInfo = mCalenderListener.onWeekInfo();
            if (weekInfo != null && !weekInfo.equals("")) {
                TTSManager.getInstance().speak(weekInfo);
            }
        }
    }

    private void queryFestivalInfo() {
        if (mCalenderListener != null) {
            String festivalInfo = mCalenderListener.onFestivalInfo();
            if (festivalInfo != null && !festivalInfo.equals("")) {
                TTSManager.getInstance().speak(festivalInfo);
            }
        }
    }

    private void queryActInfo() {
        if (mCalenderListener != null) {
            String actInfo = mCalenderListener.onActInfo();
            if (actInfo != null && !actInfo.equals("")) {
                TTSManager.getInstance().speak(actInfo);
            }
        }
    }

    private void queryDateInfo() {
        if (mCalenderListener != null) {
            String dateInfo = mCalenderListener.onDateInfo();
            if (dateInfo != null && !dateInfo.equals("")) {
                TTSManager.getInstance().speak(dateInfo);
            }
        }
    }

    private void queryLunarInfo() {
        if (mCalenderListener != null) {
            String lunarDateInfo = mCalenderListener.onLunarDateInfo();
            if (lunarDateInfo != null && !lunarDateInfo.equals("")) {
                TTSManager.getInstance().speak(lunarDateInfo);
            }
        }
    }

    private void queryLocationInfo() {
        if (mMapListener != null) {
            String locationInfo = mMapListener.onLocationInfo();
            if (locationInfo != null && !locationInfo.equals("")) {
                TTSManager.getInstance().speak(locationInfo);
            }
        }
    }

    private void searchInfo() {
        String area = mSlots.get(SlotsTypes.USER_MAP_SEARCH_AREA);
        String searchName = mSlots.get(SlotsTypes.USER_MAP_SEARCH_NAME);
        Log.d(TAG, "area: " + area + ", name:" + searchName);
        if (mMapListener != null) {
            mMapListener.onSearchInfo(searchName, convertArea(area));
        }
    }

    private void searchAddressInfo() {
        String address = mSlots.get(SlotsTypes.USER_SEARCH_ADDRESS);
        Log.d(TAG, "address: " + address);
        if (mMapListener != null) {
            mMapListener.onSearchAddress(address);
        }
    }

    private void searchPathInfo() {
        String fromName = "";
        if (mSlots.containsKey(SlotsTypes.USER_PATH_FROM_NAME)) {
            fromName = mSlots.get(SlotsTypes.USER_PATH_FROM_NAME);
        }
        String toName = mSlots.get(SlotsTypes.USER_PATH_TO_NAME);
        String way = mSlots.get(SlotsTypes.USER_MAP_PATH_WAY);
        Log.d(TAG, "toName:" + toName + ", fromName:" + fromName + ", way:" + way);
        if (mMapListener != null) {
            mMapListener.onPathInfo(fromName, toName, convertWay(way));
        }
    }

    private SearchArea convertArea(String area) {
        SearchArea searchArea;
        if (area.equals(SearchArea.AREA_NEARBY.getDesc())) {
            searchArea = SearchArea.AREA_NEARBY;
        } else {
            searchArea = SearchArea.AREA_CITY;
        }
        return searchArea;
    }

    private PathWay convertWay(String way) {
        PathWay pathWay;
        if (way.equals(PathWay.DRIVE.getDesc())) {
            pathWay = PathWay.DRIVE;
        } else if (way.equals(PathWay.RIDE.getDesc())) {
            pathWay = PathWay.RIDE;
        } else if (way.equals(PathWay.TRANSIT.getDesc())) {
            pathWay = PathWay.TRANSIT;
        } else {
            pathWay = PathWay.WALK;
        }

        return pathWay;
    }

}
