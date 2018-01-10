package com.idx.smartspeakdock.weather.model.area;

import android.support.annotation.NonNull;

import com.idx.smartspeakdock.utils.AppExecutors;
import com.idx.smartspeakdock.weather.model.area.AreaDataSource;

import java.util.List;

/**
 * Created by steve on 12/19/17.
 */

public class LocalAreaDataSource implements AreaDataSource {

    private static volatile LocalAreaDataSource INSTANCE=null;

    private ProvinceDao mProvinceDao;
    private CityDao mCityDao;
    private CountyDao mCountyDao;
    private AppExecutors mAppExecutors;
    private List<Province> mProvinces;
    private List<City> mCities;
    private List<County> mCounties;

    private LocalAreaDataSource(@NonNull AppExecutors appExecutors,
                                @NonNull ProvinceDao provinceDao,@NonNull CityDao cityDao,
                                @NonNull CountyDao countyDao){
        mAppExecutors = appExecutors;
        mProvinceDao = provinceDao;
        mCityDao=cityDao;
        mCountyDao=countyDao;
    }

    public static LocalAreaDataSource getInstance(@NonNull AppExecutors appExecutors,
                                                  @NonNull ProvinceDao provinceDao,@NonNull CityDao cityDao,
                                                  @NonNull CountyDao countyDao){
        if (INSTANCE == null) {
            synchronized (LocalAreaDataSource.class){
                if (INSTANCE == null) {
                    INSTANCE = new LocalAreaDataSource(appExecutors, provinceDao,cityDao,countyDao);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void saveProvince(final Province provinces) {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mProvinceDao.saveProvince(provinces);
            }
        });
    }

    @Override
    public void queryProvince(final LoadProvinceCallback callback) {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mProvinces=mProvinceDao.queryProvince();
                mAppExecutors.getMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mProvinces.isEmpty()){
                            callback.onDataNotAvailable();
                        }else {
                            callback.onProvinceLoaded(mProvinces);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void saveCity(final City cities) {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mCityDao.saveCity(cities);
            }
        });
    }

    @Override
    public void queryCity(final int provinceId, final LoadCityCallback callback) {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mCities=mCityDao.queryCity(provinceId);
                mAppExecutors.getMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mCities.isEmpty()){
                            callback.onDataNotAvailable();
                        }else {
                            callback.onCityLoaded(mCities);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void saveCounty(final County counties) {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mCountyDao.saveCounty(counties);
            }
        });
    }

    @Override
    public void queryCounty(final int cityId, final LoadCountyCallback callback) {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mCounties=mCountyDao.queryCounty(cityId);
                mAppExecutors.getMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mCounties.isEmpty()) {
                            callback.onDataNotAvailable();
                        }else {
                            callback.onCountyLoaded(mCounties);
                        }
                    }
                });
            }
        });
    }
}
