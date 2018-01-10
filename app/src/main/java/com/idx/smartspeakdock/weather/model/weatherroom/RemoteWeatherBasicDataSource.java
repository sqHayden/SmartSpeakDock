package com.idx.smartspeakdock.weather.model.weatherroom;

import com.idx.smartspeakdock.utils.AppExecutors;

import java.util.List;

/**
 * Created by steve on 1/5/18.
 */

public class RemoteWeatherBasicDataSource implements WeatherBasicDataSource {
    private static volatile RemoteWeatherBasicDataSource INSTANCE;

    private WeatherBasicDao mWeatherBasicDao;
    private AppExecutors mAppExecutors;
    private List<WeatherBasic> mWeatherBasics;
    private WeatherBasic mWeatherBasic;

    private RemoteWeatherBasicDataSource(AppExecutors appExecutors,WeatherBasicDao weatherBasicDao) {
        mAppExecutors=appExecutors;
        mWeatherBasicDao = weatherBasicDao;
    }

    public static RemoteWeatherBasicDataSource getInstance(AppExecutors appExecutors,WeatherBasicDao weatherBasicDao){
        if (INSTANCE==null){
            synchronized (RemoteWeatherBasicDataSource.class){
                if (INSTANCE==null){
                    INSTANCE=new RemoteWeatherBasicDataSource(appExecutors,weatherBasicDao);
                }
            }
        }
        return INSTANCE;
    }

    //添加天气基本信息
    @Override
    public void addWeatherBasic(final WeatherBasic weatherBasic) {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mWeatherBasicDao.addWeatherBasic(weatherBasic);
            }
        });
    }

    //查看所有天气基本信息
    @Override
    public void getWeatherBasics(final LoadWeatherBasicsListCallback callback) {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mWeatherBasics=mWeatherBasicDao.getWeatherBasics();
                mAppExecutors.getMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mWeatherBasics.isEmpty()){
                            callback.onDataNotAvailable();
                        }else {
                            callback.onWeatherBasicsLoaded(mWeatherBasics);
                        }
                    }
                });
            }
        });
    }

    //获取指定城市天气基本信息
    @Override
    public void getWeatherBasic(final String cityName, final LoadWeatherBasicsCallback callback) {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mWeatherBasic=mWeatherBasicDao.getWeatherBasic(cityName);
                mAppExecutors.getMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mWeatherBasic!=null){
                            callback.onWeatherBasicsLoaded(mWeatherBasic);
                        }else {
                            callback.onDataNotAvailable();
                        }
                    }
                });
            }
        });
    }

    //删除天气基本信息
    @Override
    public void deleteWeatherBasics() {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mWeatherBasicDao.deleteWeatherBasics();
            }
        });
    }
}