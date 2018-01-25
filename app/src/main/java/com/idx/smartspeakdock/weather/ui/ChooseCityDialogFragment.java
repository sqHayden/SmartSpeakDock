/*
package com.idx.smartspeakdock.weather.ui;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.SpeakerApplication;
import com.idx.smartspeakdock.standby.HttpUtil;
import com.idx.smartspeakdock.standby.Utility;
import com.idx.smartspeakdock.weather.model.area.AreaDataSource;
import com.idx.smartspeakdock.weather.model.area.AreaInjection;
import com.idx.smartspeakdock.weather.model.area.AreaRepository;
import com.idx.smartspeakdock.weather.model.area.City;
import com.idx.smartspeakdock.weather.model.area.County;
import com.idx.smartspeakdock.weather.model.area.Province;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


*/
/**
 * Created by danny on 12/15/17.
 *//*


public class ChooseCityDialogFragment extends DialogFragment {
    */
/**
     * 城市级别
     *//*

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    */
/**
     * 组件
     *//*

    private TextView mAreaName;
    private Button mBack;
    private ListView mListView;

    */
/**
     * 数据适配器
     *//*

    private ArrayAdapter mAdapter;
    private ProgressDialog mProgressDialog;
    private List<String> mDataList = new ArrayList<>();

    */
/**
     * 省、市、县列表
     *//*

    private List<Province> mProvinces;
    private List<City> mCities;
    private List<County> mCounties;

    */
/**
     * 选中的省份
     *//*

    private Province mSelectedProvince;

    */
/**
     * 选中的城市
     *//*

    private City mSelectedCity;

    */
/**
     * 当前选中的级别
     *//*

    private int mCurrentLevel;
    private OnChooseCityCompleted mOnChooseCityCompleted;
    private AreaRepository mAreaRepository;

    public void setOnChooseCityCompleted(OnChooseCityCompleted onChooseCityCompleted) {
        mOnChooseCityCompleted = onChooseCityCompleted;
    }
    public interface OnChooseCityCompleted {
        void chooseCityCompleted(String countyName, String cityName);
    }

    @Override
    public void onStart() {
        super.onStart();
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics( dm );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_weather_city, container);
        //获取控件对象
        initView(view);
        //去除对话框标题栏
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        mAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1, mDataList);
        mListView.setAdapter(mAdapter);
        mAreaRepository= AreaInjection.provideUserRepository(SpeakerApplication.getContext());
        //查询省份
       queryProvinces();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCurrentLevel == LEVEL_PROVINCE) {
                    mSelectedProvince = mProvinces.get(position);
                    queryCities();
                } else if (mCurrentLevel == LEVEL_CITY) {
                    mSelectedCity = mCities.get(position );
                    queryCounties();
                }else if(mCurrentLevel == LEVEL_COUNTY){
                    mOnChooseCityCompleted.chooseCityCompleted(mCounties.get(position).countyName,mSelectedCity.cityName);
                    dismiss();
                }
            }
        });

        //后退
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mCurrentLevel == LEVEL_CITY)) {
                    queryProvinces();
                } else if (mCurrentLevel == LEVEL_COUNTY) {
                    queryCities();
                }

            }
        });
        return view;
    }

    */
/**
     * 初始化控件
     * @param view 布局view
     *//*

    private void initView(View view) {
        mAreaName = view.findViewById(R.id.title_text);
        mBack = view.findViewById(R.id.back_button);
        mListView = view.findViewById(R.id.weather_city);
    }

    @Override
    public void onResume() {
        super.onResume();
        setDialogSize();
    }

    */
/**
     * 设置对话框大小
     *//*

    private void setDialogSize(){
        WindowManager wm = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        getDialog().getWindow().setLayout(width/2, height/2);
    }

    */
/**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
     *//*

    private void queryProvinces() {
        mAreaName.setText("中国");
        mBack.setVisibility(View.GONE);
        mAreaRepository.queryProvince(new AreaDataSource.LoadProvinceCallback() {
            @Override
            public void onProvinceLoaded(List<Province> provinces) {
                mProvinces=provinces;

                if (provinces.size()>0){
                    mDataList.clear();
                    for (Province province:provinces){
                        mDataList.add(province.provinceName);
                    }
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(0);
                    mCurrentLevel=LEVEL_PROVINCE;
                }
            }

            @Override
            public void onDataNotAvailable() {
                String address = "http://guolin.tech/api/china";
                queryFromServer(address, "province");
            }
        });
    }

    */
/**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     *//*

    private void queryCities() {
        mAreaName.setText(mSelectedProvince.provinceName);
        mBack.setVisibility(View.VISIBLE);
        mAreaRepository.queryCity(mSelectedProvince.id, new AreaDataSource.LoadCityCallback() {
            @Override
            public void onCityLoaded(List<City> cities) {
                mCities=cities;

                if (cities.size()>0){
                    mDataList.clear();
                    for (City city:cities){
                        mDataList.add(city.cityName);
                    }
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(0);
                    mCurrentLevel = LEVEL_CITY;
                }
            }

            @Override
            public void onDataNotAvailable() {
                int provinceCode = mSelectedProvince.provinceCode;
                String address = "http://guolin.tech/api/china/" + provinceCode;
                queryFromServer(address, "city");
            }
        });
    }

    */
/**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     *//*

    private void queryCounties() {
        mAreaName.setText(mSelectedCity.cityName);
        mBack.setVisibility(View.VISIBLE);
        mAreaRepository.queryCounty(mSelectedCity.id, new AreaDataSource.LoadCountyCallback() {
            @Override
            public void onCountyLoaded(List<County> counties) {
                mCounties=counties;

                if (counties.size()>0){
                    mDataList.clear();
                    for (County county:counties){
                        mDataList.add(county.countyName);
                    }
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(0);
                    mCurrentLevel = LEVEL_COUNTY;
                }
            }

            @Override
            public void onDataNotAvailable() {
                int provinceCode = mSelectedProvince.provinceCode;
                int cityCode = mSelectedCity.cityCode;
                String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
                queryFromServer(address, "county");
            }
        });
    }

    */
/**
     * 根据传入的地址和类型从服务器上查询省市县数据。
     *//*

    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, mSelectedProvince.id);
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, mSelectedCity.id);
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(SpeakerApplication.getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        if (mProgressDialog==null){
            mProgressDialog=new ProgressDialog(getActivity());
            mProgressDialog.setTitle(getActivity().getResources().getString(R.string.weather_loading_dialog));
        }
        mProgressDialog.show();
    }

    private void closeProgressDialog() {
        if (mProgressDialog!=null){
            mProgressDialog.dismiss();
        }
    }
}
*/
