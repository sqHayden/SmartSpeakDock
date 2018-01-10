package com.idx.smartspeakdock.weather.model.area;

import java.util.List;

/**
 * Created by danny on 12/19/17.
 */

public interface AreaDataSource {
    interface LoadProvinceCallback{
        void onProvinceLoaded(List<Province> provinces);

        void onDataNotAvailable();
    }

    interface LoadCityCallback{
        void onCityLoaded(List<City> cities);

        void onDataNotAvailable();
    }

    interface LoadCountyCallback{
        void onCountyLoaded(List<County> counties);

        void onDataNotAvailable();
    }

    void saveProvince(Province provinces);
    void queryProvince(LoadProvinceCallback callback);

    void saveCity(City cities);
    void queryCity(int provinceId, LoadCityCallback callback);

    void saveCounty(County counties);
    void queryCounty(int cityId, LoadCountyCallback callback);
}
