package com.idx.smartspeakdock.weather.utils;

import android.app.ProgressDialog;

import com.idx.smartspeakdock.SpeakerApplication;
import com.idx.smartspeakdock.weather.model.area.City;
import com.idx.smartspeakdock.weather.model.area.County;
import com.idx.smartspeakdock.weather.model.area.Province;

import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by danny on 12/14/17.
 */

public class ParseAreaUtil {
    /**
     * 解析和处理服务器返回的省级数据
     */
    private static ProgressDialog progressDialog;

    public static void sendRequestWithHttpURLConnection(final String urlString) {

        new Thread(new Runnable() {
            @Override
            public void run() {
//                  showProgressDialog();
//                Toast.makeText(SpeakerApplication.getContext(),"Start......",Toast.LENGTH_SHORT).show();
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    // 下面对获取到的输入流进行读取
                    reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] info = line.split("\t");
                        String weatherId = info[0];
                        if (weatherId != null && weatherId.indexOf("CN") >= 0) {
                            String countyName = info[2];
                            String provName = info[7];
                            String superiorCity = info[9];
                            List<County> counties = DataSupport.where("weatherId = ? ", info[0]).find(County.class);
                            if (counties.size() == 0) {
                                List<Province> provinces = DataSupport.where("provinceName = ? ", provName).find(Province.class);
                                if (provinces.size() == 0) {
                                    Province province = new Province();
                                    province.setProvinceName(provName);
                                    province.save();
                                    provinces.add(province);
                                }
                                List<City> cities = DataSupport.limit(1).where("cityName = ? and provinceId = ?",
                                        superiorCity, String.valueOf(provinces.get(0).getId()))
                                        .find(City.class);
                                if (cities.size() == 0) {
                                    City city = new City();
                                    city.setProvinceId(provinces.get(0).getId());
                                    city.setCityName(superiorCity);
                                    city.save();
                                    cities.add(city);
                                }
                                County county = new County();
                                county.setCityId(cities.get(0).getId());
                                county.setCountyName(countyName);
                                county.setWeatherId(weatherId);
                                county.save();
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    /**
     * 显示进度对话框
     */
    private static void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(SpeakerApplication.getContext());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private static void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
