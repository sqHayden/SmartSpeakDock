package com.idx.smartspeakdock.baidu.unit.listener;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.idx.smartspeakdock.Actions;
import com.idx.smartspeakdock.BaseActivity;
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
import com.idx.smartspeakdock.utils.MathTool;
import com.idx.smartspeakdock.utils.SharePrefrenceUtils;
import com.idx.smartspeakdock.weather.presenter.ReturnVoice;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by derik on 17-12-22.
 */

public class VoiceActionAdapter {
    private static final String TAG = VoiceActionAdapter.class.getSimpleName();
    private static final int voiceHelpNum = 3;
    private Context mContext;
    private Intent mIntent;

    private IWeatherVoiceListener mWeatherListener;
    private ICalenderVoiceListener mCalenderListener;
    private IMusicVoiceListener mMusicListener;
    private IMapVoiceListener mMapListener;
    private IShoppingVoiceListener mShoppingListener;

    private String web_sites_url;
    private String recoginize_shopping_word;
    private String reconginize_city_word;
    private String reconginize_time_word;
    private SharePrefrenceUtils mSharePrefrenceUtils;
    private HashMap<String, String> mSlots = new HashMap<>();
    private IVoiceActionListener.IActionCallback mActionCallback;
    private ISessionListener mSessionListener;
    private Handler mHandler = new Handler();

    private TTSManager.SpeakCallback mSpeakCallback = new TTSManager.SpeakCallback() {
        @Override
        public void onSpeakStart() {

        }

        @Override
        public void onSpeakFinish() {
            if (mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        result(true);
                    }
                }, 1000);
            }
        }

        @Override
        public void onSpeakError() {

        }
    };

    public VoiceActionAdapter(Context context) {
        Logger.setEnable(true);
        mIntent = new Intent(context, SwipeActivity.class);
        mSharePrefrenceUtils = new SharePrefrenceUtils(context);
        mContext = context;
    }

    public boolean action(CommunicateResponse.Action action, CommunicateResponse.Schema schema, IVoiceActionListener.IActionCallback actionCallback) {
        mActionCallback = actionCallback;
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

    public void setSessionListener(ISessionListener sessionListener) {
        mSessionListener = sessionListener;
    }

    /**
     * @param action
     * @param schema
     * @return boolean 会话是否结束，true为结束，不再监听语音输入
     */
    private boolean handleAction(CommunicateResponse.Action action, CommunicateResponse.Schema schema) {
        Log.d("handleAction name", ": " + action.actionId);
        mSlots.clear();
        for (int i = 0; i < schema.botMergedSlots.size(); i++) {
            String type = ((CommunicateResponse.Schema.MergedSlots) schema.botMergedSlots.get(i)).type;
            String word = ((CommunicateResponse.Schema.MergedSlots) schema.botMergedSlots.get(i)).original_word;
            mSlots.put(type, word);
        }

        switch (action.actionId) {
            /**开启指令*/
            case Actions.OPEN_MODULE_NAME:
                return false;
            case Actions.OPEN_NOW:
                openModule();
                return true;
            case Actions.EXIT_VOICE:
                //语音退出指令，结束会话
                if (mSessionListener != null) {
                    mSessionListener.onSessionFinish();
                }
                Log.d(TAG, "voice session: end");
                return true;

            case Actions.HELP_MODULE:
                return false;
            case Actions.HELP:
                help();
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
            case Actions.Calender.CALENDER_TIME_INFO:
                queryTimeInfo();
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
                Log.d(TAG, "handleAction: MAP_LOCATION_INFO");
                return true;
            case Actions.Map.MAP_SEARCH_AREA:
                Log.d(TAG, "handleAction: MAP_SEARCH_AREA");
                return false;
            case Actions.Map.MAP_SEARCH_NAME:
                Log.d(TAG, "handleAction: MAP_SEARCH_NAME");
                return false;
            case Actions.Map.MAP_SEARCH_INFO:
                searchInfo();
                Log.d(TAG, "handleAction: MAP_SEARCH_INFO");
                return true;
            //只支持地区搜索
            case Actions.Map.MAP_SEARCH_ADDRESS:
                searchAddressInfo();
                Log.d(TAG, "handleAction: MAP_SEARCH_ADDRESS");
                return true;
            case Actions.Map.MAP_PATH_FROM_NAME:
                Log.d(TAG, "handleAction: MAP_PATH_FROM_NAME");
                return false;
            case Actions.Map.MAP_PATH_WAY:
                Log.d(TAG, "handleAction: MAP_PATH_WAY");
                return false;
            //只支持地区到地区的路线
            case Actions.Map.MAP_PATH_INFO:
                searchPathInfo();
                Log.d(TAG, "handleAction: MAP_PATH_INFO");
                return true;

            /**购物指令*/
            case Actions.Shopping.SHOPPING_SWITCH:
                shoppingSwitch();
                return true;
            case Actions.Shopping.SHOPPING_ME_CLASSIFY:
                shoppingMeClassify();
                return true;
            case Actions.Shopping.SHOPPING_DIGITAL_PHONE:
                digitalPhone();
                return true;
            case Actions.Shopping.SHOPPING_DIGITAL_PHONEACCESS:
                digitalPhoneAccess();
                return true;
            case Actions.Shopping.SHOPPING_DIGITAL_SMARTDEVICE:
                digitalSmartDevice();
                return true;
            case Actions.Shopping.SHOPPING_DIGITAL_CARVEHIELEC:
                digitalCarvehiElec();
                return true;
            case Actions.Shopping.SHOPPING_DIGITAL_IPHONEACCESSI:
                digitalIphoneAccessi();
                return true;
            case Actions.Shopping.SHOPPING_COMPUTER_DESKTOP:
                computerDesktop();
                return true;
            case Actions.Shopping.SHOPPING_COMPUTERS:
                computers();
                return true;
            case Actions.Shopping.SHOPPING_COM_PERIPHERALS:
                comPeripherals();
                return true;
            case Actions.Shopping.SHOPPING_SMART_SHARPTV:
                smartSharpTV();
                return true;
            case Actions.Shopping.SHOPPING_LIFE_ELECTRICAL:
                lifeElectrical();
                return true;
            case Actions.Shopping.SHOPPING_SMART_CARE_HEALTH:
                smartCarehealth();
                return true;
            case Actions.Shopping.SHOPPING_SMART_KITCHENSMALL:
                smartKitchensmall();
                return true;
            case Actions.Shopping.SHOPPING_SMART_FAMILYAUDIO:
                smartFamilyaudio();
                return true;
            case Actions.Shopping.SHOPPING_SMART_ICEBOX:
                smartIcebox();
                return true;
            case Actions.Shopping.SHOPPING_SMART_WASHMACHINE:
                smartWashmachine();
                return true;

            /**天气指令*/
            /*case Actions.Weather.WEATHER_CHECK_INFO:
                reconginize_city_word = ((CommunicateResponse.Schema.MergedSlots) schema.botMergedSlots.get(0)).original_word;;
                Log.i(TAG, "handleAction: reconginize_city_word = "+reconginize_city_word);
                if (mWeatherListener != null) mWeatherListener.onWeatherInfo(reconginize_city_word);
                return true;*/
            case Actions.Weather.WEATHER_INFO:
                refreshWeatherInfo();
                return true;
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
            case Actions.Weather.RAIN_INFO:
                rainInfo();
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

    private void help() {
        String name = mSlots.get(SlotsTypes.USER_MODULE_NAME);
        String[] voiceArray = null;

        switch (name) {
            case Modules.CALENDER:
                voiceArray = mContext.getResources().getStringArray(R.array.help_calender);
                break;
            case Modules.MAP:
                voiceArray = mContext.getResources().getStringArray(R.array.help_map);
                break;
            case Modules.MUSIC:
                voiceArray = mContext.getResources().getStringArray(R.array.help_music);
                break;
            case Modules.SHOPPING:
                voiceArray = mContext.getResources().getStringArray(R.array.help_shopping);
                break;
            case Modules.WEATHER:
                voiceArray = mContext.getResources().getStringArray(R.array.help_weather);
                break;
            default:
        }

        String[] preVoice = mContext.getResources().getStringArray(R.array.help_pre);
        String[] endVoice = mContext.getResources().getStringArray(R.array.help_end);
        int[] indexs = MathTool.randomValues(voiceArray.length, voiceHelpNum);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(preVoice[MathTool.randomValue(preVoice.length)]);
        for (int i = 0; i < indexs.length; i++) {
            stringBuffer.append(voiceArray[indexs[i]]);
        }
        stringBuffer.append(endVoice[MathTool.randomValue(endVoice.length)]);
        TTSManager.getInstance().speak(stringBuffer.toString(), mSpeakCallback);
    }


    private void shoppingMeClassify() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_ME_CLASSIFY);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void shoppingSwitch() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_SWITCH);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void digitalPhone() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_DIGITAL_PHONE);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void digitalPhoneAccess() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_DIGITAL_PHONEACCESS);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void digitalSmartDevice() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_DIGITAL_SMARTDEVICE);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void digitalCarvehiElec() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_DIGITAL_CARVEHIELEC);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void digitalIphoneAccessi() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_DIGITAL_IPHONEACCESSI);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void computerDesktop() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_COMPUTER_DESKTOP);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void computers() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_COMPUTERS);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void comPeripherals() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_COM_PERIPHERALS);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void smartSharpTV() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_SMART_SHARPTV);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void lifeElectrical() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_LIFE_ELECTRICAL);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void smartCarehealth() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_SMART_CARE_HEALTH);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void smartKitchensmall() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_SMART_KITCHENSMALL);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void smartFamilyaudio() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_SMART_FAMILYAUDIO);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void smartIcebox() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_SMART_ICEBOX);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void smartWashmachine() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_SMART_WASHMACHINE);
        if (mShoppingListener != null) {
            jude_word(recoginize_shopping_word);
        }
        result(true);
    }

    private void refreshWeatherInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        if (mWeatherListener != null) {
            mWeatherListener.onWeatherInfo(reconginize_city_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        Log.i(TAG, "onReturnVoice: ");
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void rangeTempInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        if (mWeatherListener != null) {
            mWeatherListener.onRangeTempInfo(reconginize_city_word, reconginize_time_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void airQualityInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        if (mWeatherListener != null) {
            mWeatherListener.onAirQualityInfo(reconginize_city_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void currentTempInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        if (mWeatherListener != null) {
            mWeatherListener.onCurrentTempInfo(reconginize_city_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void weatherStatus() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        if (mWeatherListener != null) {
            mWeatherListener.onWeatherStatus(reconginize_city_word, reconginize_time_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void rainInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        if (mWeatherListener != null) {
            mWeatherListener.onRainInfo(reconginize_city_word, reconginize_time_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void derssInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        if (mWeatherListener != null) {
            mWeatherListener.onDressInfo(reconginize_city_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void uitravioletLevelInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        if (mWeatherListener != null) {
            mWeatherListener.onUitravioletLevelInfo(reconginize_city_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void smogInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        if (mWeatherListener != null) {
            mWeatherListener.onSmogInfo(reconginize_city_word, reconginize_time_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void jude_word(String recoginize_shopping_word) {
        Log.i(TAG, "jude_word: recoginize_shopping_word = " + recoginize_shopping_word);
        if (!TextUtils.isEmpty(recoginize_shopping_word)) {
            web_sites_url = mSharePrefrenceUtils.getWebUrl(recoginize_shopping_word);
            Log.i(TAG, "jude_word: recoginize_shopping_word = " + recoginize_shopping_word + ",web_sites_url = " + web_sites_url);
            if (checkVoiceAnswer(web_sites_url)) {
                mShoppingListener.openSpecifyWebsites(web_sites_url);
            }
        }
    }

    private boolean checkVoiceAnswer(String check_voice_answer) {
        if (TextUtils.isEmpty(check_voice_answer)) {
            return false;
        }
        if (check_voice_answer.equals("")) {
            return false;
        }
        return true;
    }

    private void openModule() {
        String name = mSlots.get(SlotsTypes.USER_MODULE_NAME);
        switch (name) {
            case Modules.CALENDER:
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "");
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.CALENDAR_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.FIRST_CHANGE_FRAGMENT, true);
                break;
            case Modules.WEATHER:
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "");
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WEATHER_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.FIRST_CHANGE_FRAGMENT, true);
                break;
            case Modules.MAP:
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "");
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.MAP_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.FIRST_CHANGE_FRAGMENT, true);
                break;
            case Modules.MUSIC:
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "");
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.MUSIC_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.FIRST_CHANGE_FRAGMENT, true);
                break;
            case Modules.SHOPPING:
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "");
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "");
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.SHOPPING_FRAGMENT_INTENT_ID);
                mIntent.putExtra("weburl", "https://mall.flnet.com");
                mContext.startActivity(mIntent);
                mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.FIRST_CHANGE_FRAGMENT, true);
                break;
            /**购物指令*/
            case GlobalUtils.IPHONE:
            case GlobalUtils.SHARPE:
            case GlobalUtils.FIND:
            case GlobalUtils.BUSSIESE_GROUP:
            case GlobalUtils.LOGIN_PAGE:
            case GlobalUtils.FLNET:
            case GlobalUtils.register_page:
            case GlobalUtils.SHOPPING_CART:
                if (mShoppingListener != null) {
                    jude_word(name);
                }
                break;
        }
        result(true);
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
        result(false);
    }

    private void musicPause() {
        if (mMusicListener != null) {
            mMusicListener.onPause();
        }
        result(true);
    }

    private void musicContinue() {
        if (mMusicListener != null) {
            mMusicListener.onContinue();
        }
        result(false);
    }

    private void musicNext() {
        if (mMusicListener != null) {
            mMusicListener.onNext();
        }
        result(false);
    }

    private void musicPrevious() {
        if (mMusicListener != null) {
            mMusicListener.onPrevious();
        }
        result(false);
    }

    private void queryWeekInfo() {
        String day = mSlots.get(SlotsTypes.USER_DATE_DAY);
        if (mCalenderListener != null) {
            String weekInfo = mCalenderListener.onWeekInfo(day);
            TTSManager.getInstance().speak(weekInfo, mSpeakCallback);
        }
    }

    private void queryTimeInfo() {
        if (mCalenderListener != null) {
            String timeInfo = mCalenderListener.onTimeInfo();
            TTSManager.getInstance().speak(timeInfo, mSpeakCallback);
        }
    }

    private void queryFestivalInfo() {
        String day = mSlots.get(SlotsTypes.USER_DATE_DAY);
        if (mCalenderListener != null) {
            String festivalInfo = mCalenderListener.onFestivalInfo(day);
            TTSManager.getInstance().speak(festivalInfo, mSpeakCallback);
        }
    }

    private void queryActInfo() {
        String day = mSlots.get(SlotsTypes.USER_DATE_DAY);
        if (mCalenderListener != null) {
            String actInfo = mCalenderListener.onActInfo(day);
            TTSManager.getInstance().speak(actInfo, mSpeakCallback);
        }
    }

    private void queryDateInfo() {
        String day = mSlots.get(SlotsTypes.USER_DATE_DAY);
        if (mCalenderListener != null) {
            String dateInfo = mCalenderListener.onDateInfo(day);
            TTSManager.getInstance().speak(dateInfo, mSpeakCallback);
        }
    }

    private void queryLunarInfo() {
        String day = mSlots.get(SlotsTypes.USER_DATE_DAY);
        if (mCalenderListener != null) {
            String lunarInfo = mCalenderListener.onLunarDateInfo(day);
            TTSManager.getInstance().speak(lunarInfo, mSpeakCallback);
        }
    }

    private void queryLocationInfo() {
        if (mMapListener != null) {
            String locationInfo = mMapListener.onLocationInfo();
            Log.d(TAG, "queryLocationInfo: " + locationInfo);
            TTSManager.getInstance().speak(locationInfo, mSpeakCallback);
            Log.d(TAG, "queryLocationInfo: 123");
        }
    }

    private void searchInfo() {
        String area = mSlots.get(SlotsTypes.USER_MAP_SEARCH_AREA);
        String searchName = mSlots.get(SlotsTypes.USER_MAP_SEARCH_NAME);
        Log.d(TAG, "area: " + area + ", name:" + searchName);
        if (mMapListener != null) {
            String searchInfo = mMapListener.onSearchInfo(searchName, convertArea(area));
            TTSManager.getInstance().speak(searchInfo, mSpeakCallback);
        }
    }

    private void searchAddressInfo() {
        String address = mSlots.get(SlotsTypes.USER_SEARCH_ADDRESS);
        Log.d(TAG, "address: " + address);
        if (mMapListener != null) {
            String searchAddressInfo = mMapListener.onSearchAddress(address);
            TTSManager.getInstance().speak(searchAddressInfo, mSpeakCallback);
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
            String pathInfo = mMapListener.onPathInfo(fromName, toName, convertWay(way));
            TTSManager.getInstance().speak(pathInfo, mSpeakCallback);
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

    /**
     * 执行成功后，回调
     *
     * @param sayBye 是否开启结束询问
     */
    private void result(boolean sayBye) {
        if (mActionCallback != null) {
            mActionCallback.onResult(sayBye);
        }
    }

}
