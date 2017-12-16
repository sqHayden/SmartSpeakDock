package com.idx.smartspeakdock.weather.ui;

import android.app.DialogFragment;
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

import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.weather.model.area.City;
import com.idx.smartspeakdock.weather.model.area.County;
import com.idx.smartspeakdock.weather.model.area.Province;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import static com.idx.smartspeakdock.weather.utils.ParseAreaUtil.sendRequestWithHttpURLConnection;


/**
 * Created by steve on 12/15/17.
 */

public class ChooseCityDialogFragment extends DialogFragment {
    /**
     * 城市级别
     */
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    /**
     * 组件
     */
    private TextView mAreaName;
    private Button mBack;
    private ListView mListView;

    /**
     * 数据适配器
     */
    private ArrayAdapter mAdapter;

    private List<String> mDataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;
    private boolean onlyOnecity;
    private List<String> provinces;
    private OnChooseCityCompleted mOnChooseCityCompleted;

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
        //查询省份
       queryProvinces();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position );
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){
                    mOnChooseCityCompleted.chooseCityCompleted(countyList.get(position).getCountyName(),selectedCity.getCityName());
                    dismiss();
                }
            }
        });

        //后退
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((currentLevel == LEVEL_CITY) || onlyOnecity) {
                    queryProvinces();
                } else if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                }

            }
        });
        return view;
    }

    /**
     * 初始化控件
     * @param view 布局view
     */
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

    /**
     * 设置对话框大小
     */
    private void setDialogSize(){
        WindowManager wm = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        getDialog().getWindow().setLayout(width/2, height/2);
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryProvinces() {
        mAreaName.setText("中国");
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            mDataList.clear();
            for (Province province : provinceList) {
                mDataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            String url = "https://cdn.heweather.com/china-city-list.txt";
            sendRequestWithHttpURLConnection(url);
        }
    }

    /**
     * 查询选中省内所有的市，从数据库查询。
     */
    private void queryCities() {
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        mAreaName.setText(selectedProvince.getProvinceName());
        mBack.setVisibility(View.VISIBLE);
        if (cityList.size()>0){
            mDataList.clear();
            for (City c:cityList){
                mDataList.add(c.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }
    }

    /**
     * 查询选中市内所有的县
     */
    private void queryCounties() {
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()  == 1){
            mOnChooseCityCompleted.chooseCityCompleted(countyList.get(0).getCountyName(),selectedCity.getCityName());
            dismiss();
        }else if (countyList.size() > 1) {
            mAreaName.setText(selectedCity.getCityName());
            mBack.setVisibility(View.VISIBLE);
            mDataList.clear();
            for (County county : countyList) {
                mDataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }
    }
}
