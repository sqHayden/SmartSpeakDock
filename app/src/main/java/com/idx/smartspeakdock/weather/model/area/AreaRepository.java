package com.idx.smartspeakdock.weather.model.area;

/**
 * Created by steve on 12/19/17.
 */

public class AreaRepository implements AreaDataSource {

    private static AreaRepository INSTANCE = null;
    private LocalAreaDataSource mLocalAreaDataSource;

    // 暂仅实现本地读取
    private AreaRepository(LocalAreaDataSource localAreaDataSource){
        this.mLocalAreaDataSource = localAreaDataSource;
    }

    public static AreaRepository getInstance(LocalAreaDataSource localAreaDataSource){
        if (INSTANCE == null) {
            INSTANCE = new AreaRepository(localAreaDataSource);
        }
        return INSTANCE;
    }

    @Override
    public void saveProvince(Province provinces) {
        mLocalAreaDataSource.saveProvince(provinces);
    }

    @Override
    public void queryProvince(LoadProvinceCallback callback) {
        mLocalAreaDataSource.queryProvince(callback);
    }

    @Override
    public void saveCity(City cities) {
        mLocalAreaDataSource.saveCity(cities);
    }

    @Override
    public void queryCity(int provinceId, LoadCityCallback callback) {
        mLocalAreaDataSource.queryCity(provinceId,callback);
    }

    @Override
    public void saveCounty(County counties) {
        mLocalAreaDataSource.saveCounty(counties);
    }

    @Override
    public void queryCounty(int cityId, LoadCountyCallback callback) {
        mLocalAreaDataSource.queryCounty(cityId,callback);
    }
}
