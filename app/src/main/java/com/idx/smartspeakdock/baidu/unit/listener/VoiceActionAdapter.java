package com.idx.smartspeakdock.baidu.unit.listener;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import com.idx.smartspeakdock.Actions;
import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.Modules;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.SlotsTypes;
import com.idx.smartspeakdock.calendar.CalendarFragment;
import com.idx.smartspeakdock.swipe.SwipeActivity;
import com.idx.smartspeakdock.baidu.control.TTSManager;
import com.idx.smartspeakdock.baidu.unit.model.CommunicateResponse;
import com.idx.smartspeakdock.map.PathWay;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.Logger;
import com.idx.smartspeakdock.utils.MathTool;
import com.idx.smartspeakdock.utils.SharePrefrenceUtils;
import com.idx.smartspeakdock.weather.presenter.ReturnVoice;

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

            case Actions.HELP_MODULE_NAME:
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
            case Actions.Music.MUSIC_STOP:
                musicStop();
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
            case Actions.Calender.CALENDER_FESTIVAL_DATE:
                queryFestivalDate();
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
            case Actions.Shopping.SHOPPING_PHONE:
                digitalPhone();
                return true;
            case Actions.Shopping.SHOPPING_PHONE_ACCESS:
                digitalPhoneAccess();
                return true;
            case Actions.Shopping.SHOPPING_SMART_DEVICE:
                digitalSmartDevice();
                return true;
            case Actions.Shopping.SHOPPING_CAR_VERHIELE:
                digitalCarvehiElec();
                return true;
            case Actions.Shopping.SHOPPING_IPHONE_ACCESS:
                digitalIphoneAccessi();
                return true;
            case Actions.Shopping.SHOPPING_COM_DESKTOP:
                computerDesktop();
                return true;
            case Actions.Shopping.SHOPPING_COM_COMPUTERS:
                computers();
                return true;
            case Actions.Shopping.SHOPPING_COM_PERIPHERALS:
                comPeripherals();
                return true;
            case Actions.Shopping.SHOPPING_SMART_SHARPTV:
                smartSharpTV();
                return true;
            case Actions.Shopping.SHOPPING_SMART_LIFEELECT:
                lifeElectrical();
                return true;
            case Actions.Shopping.SHOPPING_SMART_CAREHEALT:
                smartCarehealth();
                return true;
            case Actions.Shopping.SHOPPING_SMART_KITCHEN:
                smartKitchensmall();
                return true;
            case Actions.Shopping.SHOPPING_SMART_FAMILYAUD:
                smartFamilyaudio();
                return true;
            case Actions.Shopping.SHOPPING_SMART_ICEBOX:
                smartIcebox();
                return true;
            case Actions.Shopping.SHOPPING_SMART_WASHMACHI:
                smartWashmachine();
                return true;
            case Actions.Shopping.SHOPPING_TV_KINDS:
                tvKinds();
                return true;

            /**天气指令*/
            /*case Actions.Weather.WEATHER_CHECK_INFO:
                reconginize_city_word = ((CommunicateResponse.Schema.MergedSlots) schema.botMergedSlots.get(0)).original_word;;
                Log.i(TAG, "handleAction: reconginize_city_word = "+reconginize_city_word);
                if (mWeatherListener != null) mWeatherListener.onWeatherInfo(reconginize_city_word);
                return true;*/
            case Actions.Weather.TODAY_WEATHER_INFO:
                refreshWeatherInfo();
                return true;
            case Actions.Weather.CITY_TODAY_WEATHER_INFO:
                refreshCityWeatherINfo();
                return true;
            case Actions.Weather.TIME_TODAY_WEATHER_INFO:
                refreshTimeWeatherINfo();
                return true;
            case Actions.Weather.NO_TODAY_WEATHER_INFO:
                refreshNoWeatherINfo();
                return true;
            case Actions.Weather.WEATHER_RANGE_INFO:
                rangeTempInfo();
                return true;
            case Actions.Weather.WEATHER_CITY_RANGE_INFO:
                rangeCityTempInfo();
                return true;
            case Actions.Weather.WEATHER_TIME_RANGE_INFO:
                rangeTimeTempInfo();
                return true;
            case Actions.Weather.WEATHER_NO_RANGE_INFO:
                rangeNoTempInfo();
                return true;
            case Actions.Weather.WEATHER_AIR_QUALITY_INFO:
                airQualityInfo();
                return true;
            case Actions.Weather.WEATHER_CITY_AIR_QUALITY_INFO:
                airQualityCityInfo();
                return true;
            case Actions.Weather.WEATHER_TIME_AIR_QUALITY_INFO:
                airQualityTimeInfo();
                return true;
            case Actions.Weather.WEATHER_NO_AIR_QUALITY_INFO:
                airQualityNoInfo();
                return true;
            case Actions.Weather.WEATHER_CURRENT_TEMP_INFO:
                currentTempInfo();
                return true;
            case Actions.Weather.WEATHER_NO_CURRENT_TEMP_INFO:
                currentTempNoInfo();
                return true;
            case Actions.Weather.WEATHER_STATUS_INFO:
                weatherStatus();
                return true;
            case Actions.Weather.WEATHER_CITY_STATUS_INFO:
                weatherCityStatus();
                return true;
            case Actions.Weather.WEATHER_TIME_STATUS_INFO:
                weatherTimeStatus();
                return true;
            case Actions.Weather.WEATHER_NO_STATUS_INFO:
                weatherNoStatus();
                return true;
            case Actions.Weather.WEATHER_RAIN_INFO:
                rainInfo();
                return true;
            case Actions.Weather.WEATHER_CITY_RAIN_INFO:
                rainCityInfo();
                return true;
            case Actions.Weather.WEATHER_TIME_RAIN_INFO:
                rainTimeInfo();
                return true;
            case Actions.Weather.WEATHER_NO_RAIN_INFO:
                rainNoInfo();
                return true;
            case Actions.Weather.WEATHER_DRESS_INFO:
                derssInfo();
                return true;
            case Actions.Weather.WEATHER_CITY_DRESS_INFO:
                derssCityInfo();
                return true;
            case Actions.Weather.WEATHER_TIME_DRESS_INFO:
                derssTimeInfo();
                return true;
            case Actions.Weather.WEATHER_UITRAVIOLET_LEVEL_INFO:
                uitravioletLevelInfo();
                return true;
            case Actions.Weather.WEATHER_CITY_UITRA_LEVEL_INFO:
                uitravioletLevelCityInfo();
                return true;
            case Actions.Weather.WEATHER_TIME_UITRA_LEVEL_INFO:
                uitravioletLevelTimeInfo();
                return true;
            case Actions.Weather.WEATHER_SMOG_INFO:
                smogInfo();
                return true;
            case Actions.Weather.WEATHER_CITY_SMOG_INFO:
                smogCityInfo();
                return true;
            case Actions.Weather.WEATHER_TIME_SMOG_INFO:
                smogTimeInfo();
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
    private void tvKinds() {
        recoginize_shopping_word = mSlots.get(SlotsTypes.USER_SHOPPING_TV_KINDS);
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
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void refreshCityWeatherINfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        if (mWeatherListener != null) {
            mWeatherListener.onCityWeatherInfo(reconginize_city_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void refreshTimeWeatherINfo() {
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        if (mWeatherListener != null) {
            mWeatherListener.onTimeWeatherINfo(reconginize_time_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void refreshNoWeatherINfo() {
        if (mWeatherListener != null) {
            mWeatherListener.onNoWeatherInfo(new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
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


    private void rangeCityTempInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        if (mWeatherListener != null) {
            mWeatherListener.onCityRangeTempInfo(reconginize_city_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void rangeTimeTempInfo() {
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        if (mWeatherListener != null) {
            mWeatherListener.onTimeRangeTempInfo(reconginize_time_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void rangeNoTempInfo() {
        if (mWeatherListener != null) {
            mWeatherListener.onNoRangeTempInfo(new ReturnVoice() {
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

    private void airQualityCityInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        if (mWeatherListener != null) {
            mWeatherListener.onCityAirQualityInfo(reconginize_city_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void airQualityTimeInfo() {
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        if (mWeatherListener != null) {
            mWeatherListener.onTimeAirQualityInfo(reconginize_time_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void airQualityNoInfo() {
        if (mWeatherListener != null) {
            mWeatherListener.onNoAiqQualityInfo(new ReturnVoice() {
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

    private void currentTempNoInfo() {
        if (mWeatherListener != null) {
            mWeatherListener.onNoCurrentTempInfo(new ReturnVoice() {
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

    private void weatherCityStatus() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        if (mWeatherListener != null) {
            mWeatherListener.onCityWeatherStatus(reconginize_city_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void weatherTimeStatus() {
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        if (mWeatherListener != null) {
            mWeatherListener.onTimeWeatherStatus(reconginize_time_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void weatherNoStatus() {
        if (mWeatherListener != null) {
            mWeatherListener.onNoWeatherStatus(new ReturnVoice() {
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


    private void rainCityInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        if (mWeatherListener != null) {
            mWeatherListener.onCityRainInfo(reconginize_city_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void rainTimeInfo() {
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        if (mWeatherListener != null) {
            mWeatherListener.onTimeRainInfo(reconginize_time_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void rainNoInfo() {
        if (mWeatherListener != null) {
            mWeatherListener.onNoRainInfo(new ReturnVoice() {
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

    private void derssCityInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        if (mWeatherListener != null) {
            mWeatherListener.onCityDressInfo(reconginize_city_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void derssTimeInfo() {
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        if (mWeatherListener != null) {
            mWeatherListener.onTimeDressInfo(reconginize_time_word, new ReturnVoice() {
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

    private void uitravioletLevelCityInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        if (mWeatherListener != null) {
            mWeatherListener.onCityUitravioletLevelInfo(reconginize_city_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void uitravioletLevelTimeInfo() {
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        if (mWeatherListener != null) {
            mWeatherListener.onTimeUitravioletLevelInfo(reconginize_time_word, new ReturnVoice() {
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

    private void smogCityInfo() {
        reconginize_city_word = mSlots.get(SlotsTypes.USER_WEATHER_CITY);
        if (mWeatherListener != null) {
            mWeatherListener.onCitySmogInfo(reconginize_city_word, new ReturnVoice() {
                @Override
                public void onReturnVoice(String voice_answer) {
                    if (checkVoiceAnswer(voice_answer)) {
                        TTSManager.getInstance().speak(voice_answer, mSpeakCallback);
                    }
                }
            });
        }
    }

    private void smogTimeInfo() {
        reconginize_time_word = mSlots.get(SlotsTypes.USER_WEATHER_TIME);
        if (mWeatherListener != null) {
            mWeatherListener.onTimeSmogInfo(reconginize_time_word, new ReturnVoice() {
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
        String voice_answer = "";
        if (!TextUtils.isEmpty(recoginize_shopping_word)) {
            web_sites_url = mSharePrefrenceUtils.getWebUrl(recoginize_shopping_word);
            Log.i(TAG, "jude_word: recoginize_shopping_word = " + recoginize_shopping_word + ",web_sites_url = " + web_sites_url);
            if (checkVoiceAnswer(web_sites_url)) {
                Log.i(TAG, "jude_word: un_null");
                voice_answer = mShoppingListener.openSpecifyWebsites(web_sites_url);
            }else {
                Log.i(TAG, "jude_word: null");
                voice_answer = "抱歉,没有该网页的信息或查询信息有误";
            }
            TTSManager.getInstance().speak(voice_answer);
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
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                Log.d(TAG, "openModule: calendar");
                mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
                break;
            case Modules.WEATHER:
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.WEATHER_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                Log.d(TAG, "openModule: weather");
                mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
                break;
            case Modules.MAP:
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.MAP_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                Log.d(TAG, "openModule: map");
                mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
                break;
            case Modules.MUSIC:
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.MUSIC_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                Log.d(TAG, "openModule: music");
                mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
                break;
            case Modules.SHOPPING:
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_INTENT_ID);
                mIntent.putExtra("weburl", "https://mall.flnet.com");
                mContext.startActivity(mIntent);
                Log.d(TAG, "openModule: shoppping");
                mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
                break;
            /**购物指令*/
            case GlobalUtils.Shopping.IPHONE:
            case GlobalUtils.Shopping.SHARPE:
            case GlobalUtils.Shopping.FIND:
            case GlobalUtils.Shopping.BUSSIESE_GROUP:
            case GlobalUtils.Shopping.LOGIN_PAGE:
            case GlobalUtils.Shopping.FLNET:
            case GlobalUtils.Shopping.register_page:
            case GlobalUtils.Shopping.SHOPPING_CART:
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
//                mMusicListener.onPlay(musicIndex);
            } else if (musicName != null && !musicName.equals("")) {
                mMusicListener.onPlay(musicName, new ResultCallback() {
                    @Override
                    public void onResult(String result) {
                        if (result != null && !result.equals("")) {
                            TTSManager.getInstance().speak(result, mSpeakCallback);
                        } else {
                            result(false);
                        }
                    }
                });
            } else {
                mMusicListener.onPlay(0);
            }
        }

    }

    private void musicPause() {
        if (mMusicListener != null) {
            mMusicListener.onPause();
        }
        result(true);
    }

    private void musicStop() {
        if (mMusicListener != null) {
            mMusicListener.onStop();
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

    private void queryFestivalDate(){
        String name = mSlots.get(SlotsTypes.USER_FESTIVAL_NAME);
        if (mCalenderListener != null) {
            String festivalDate = mCalenderListener.onFestivalDate(name);
            TTSManager.getInstance().speak(festivalDate, mSpeakCallback);
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
            mMapListener.onLocationInfo(new ResultCallback() {
                @Override
                public void onResult(String result) {
                    Log.d(TAG, "queryLocationInfo: " + result);
                    TTSManager.getInstance().speak(result, mSpeakCallback);
                }
            });

        }
    }

    private void searchInfo() {
//        String area = mSlots.get(SlotsTypes.USER_MAP_SEARCH_AREA);
        String searchName = mSlots.get(SlotsTypes.USER_MAP_SEARCH_NAME);
//        Log.d(TAG, "area: " + area + ", name:" + searchName);
        if (mMapListener != null) {
            mMapListener.onSearchInfo(searchName, new ResultCallback() {
                @Override
                public void onResult(String result) {
                    TTSManager.getInstance().speak(result, mSpeakCallback);
                }
            });

        }
    }

    private void searchAddressInfo() {
        String address = mSlots.get(SlotsTypes.USER_SEARCH_ADDRESS);
        Log.d(TAG, "address: " + address);
        if (mMapListener != null) {
            mMapListener.onSearchAddress(address, new ResultCallback() {
                @Override
                public void onResult(String result) {
                    TTSManager.getInstance().speak(result, mSpeakCallback);
                }
            });
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
            mMapListener.onPathInfo(fromName, toName, way, new ResultCallback() {
                @Override
                public void onResult(String result) {
                    TTSManager.getInstance().speak(result, mSpeakCallback);
                }
            });
        }
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
