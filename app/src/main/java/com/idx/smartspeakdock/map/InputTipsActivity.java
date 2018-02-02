package com.idx.smartspeakdock.map;

/**
 * Created by hayden on 18-1-15.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.map.adapter.InputItemsAdapter;
import com.idx.smartspeakdock.map.adapter.InputTipsAdapter;
import com.idx.smartspeakdock.map.util.Constants;
import com.idx.smartspeakdock.map.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class InputTipsActivity extends BaseActivity implements
        TextWatcher,OnItemClickListener,View.OnClickListener,
        PoiSearch.OnPoiSearchListener,Inputtips.InputtipsListener{
    //输入搜索关键字
    private EditText mSearchView;
    //回退键控件
    private ImageView mBack;
    //listView的控件
    private ListView mInputListView;
    //周边搜索对象
    private PoiSearch.Query query;
    //PoiSearch对象
    private PoiSearch poiSearch;
    //搜索时进度条
    private ProgressDialog progDialog = null;
    //log
    private static final String TAG = "InputTipsActivity";
    //存储周边搜索数据
    private List<PoiItem> poiItems = new ArrayList<>();
    //数据存储类
    private InputItemsAdapter inputItemsAdapter;
    //定义检测标志位
    private boolean isPoiSearch;
    //poi返回的结果
    private PoiResult poiResult;
    //当前坐标
    private LatLng currentLatLng;
    //当前城市名称
    private String cityName;
    //请求对象名称
    private String activityName;
    //关键字
    private String key = "";
    //数据存储
    private Gson gson;
    //Tip对象存储
    private List<Tip> mCurrentTipList;
    private InputTipsAdapter mIntipAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity_input_tips);
        //获取传递过来的数据
        InitData();
        //获取搜索输入框
        mSearchView = findViewById(R.id.keyWord);
        //绑定service
        bindService(mControllerintent, myServiceConnection, 0);
        //设置改变时属性
        final int inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        mSearchView.setInputType(inputType);
        //设置内容变化监听
        mSearchView.addTextChangedListener(this);
        //设置回车搜索
        mSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //非空状态下的搜索
                    if(!mSearchView.getText().toString().trim().equals("")) {
                        //如果是Map请求
                        if(activityName.equals("MapFragment")) {
                            //如果是模糊关键词请求
                            if(isPoiSearch) {
                                //关闭键盘
                                closeKeyBoard(textView);
                                //发送list过去
                                Intent intent = new Intent();
                                intent.putExtra("poiItems", gson.toJson(poiItems));
                                intent.putExtra("key_name", mSearchView.getText());
                                setResult(MapFragment.RESULT_CODE_KEYWORDS, intent);
                                //结束Activity
                                finish();
                            }else{//关键字请求
                                Toast.makeText(getApplicationContext(),"请点击进行选择",Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(getApplicationContext(),"请点击进行选择",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"位置信息不能为空", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });
        //获取回退按钮
        mBack = findViewById(R.id.back);
        //设置点击监听
        mBack.setOnClickListener(this);
        //获取listView
        mInputListView = findViewById(R.id.inputtip_list);
        //设置条目监听
        mInputListView.setOnItemClickListener(this);
    }

    //传递数据获取
    private void InitData(){
        //创建对象
        gson = new Gson();
        //获取请求源
        activityName = getIntent().getStringExtra("from");
        //获取当前坐标
        String value = getIntent().getStringExtra("current_location");
        currentLatLng = gson.fromJson(value,new TypeToken<LatLng>(){}.getType());
        //当前城市
        cityName = getIntent().getStringExtra("current_city");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back://执行返回上一层
                //关闭键盘
                closeKeyBoard(v);
                finish();
                break;
        }
    }


    /**
     * 文本框改变监听
     * **/
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
    @Override
    public void afterTextChanged(Editable s) {
        //进行一次查询
        Log.d(TAG, "onPoiSearched: "+s.toString());
        if (!s.toString().equals("")&&!s.toString().trim().equals("")) {
            key = s.toString();
            //判断接收到的词
            if(chooseSearchWay(key)){
                //查询周边模糊词
                searchSurrend();
            }else{
                Log.d(TAG, "searchSurrend:进行全国查找了");
                InputtipsQuery inputquery = new InputtipsQuery(s.toString(),cityName);
                Inputtips inputTips = new Inputtips(InputTipsActivity.this.getApplicationContext(), inputquery);
                inputTips.setInputtipsListener(this);
                inputTips.requestInputtipsAsyn();
            }
        }else{
            if (mIntipAdapter != null && mCurrentTipList != null) {
                mCurrentTipList.clear();
                mIntipAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 判断通过哪种方式搜索
     * **/
    private boolean chooseSearchWay(String s){
        isPoiSearch = true;
        if(s.equals("酒店")||s.equals("宾馆")||s.equals("旅社")) {
            query = new PoiSearch.Query("", "100105", "");
        }else if(s.equals("美食")||s.equals("小吃街")){
            query = new PoiSearch.Query("", "050400", "");
        }else if(s.equals("商场")||s.equals("购物广场")){
            query = new PoiSearch.Query("", "060101", "");
        }else if(s.equals("地铁")||s.equals("地铁站")){
            query = new PoiSearch.Query("", "150500", "");
        }else if(s.equals("公交")||s.equals("公交站")){
            query = new PoiSearch.Query("", "150700", "");
        }else if(s.equals("火车")||s.equals("火车站")){
            query = new PoiSearch.Query("", "150200", "");
        }else {
            //应该去进行关键字查询
            isPoiSearch = false;
        }
        return isPoiSearch;
    }


    /**
     * 周边搜索监听
     * **/
    private void searchSurrend(){
        query.setPageSize(20);// 设置每页最多返回多少条poiitem
        query.setPageNum(0);//设置查询页码
        //构造对象并发送检索
        Log.d(TAG, "onPoiSearched: 构造对象并发送检索");
        if (currentLatLng != null) {
            Log.d(TAG, "searchSurrend: 对象不是空的");
            poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);
            if(isPoiSearch) {
                //设置搜索区域为以lp点为圆心，其周围5000米范围
                LatLonPoint latLonPoint = new LatLonPoint(currentLatLng.latitude, currentLatLng.longitude);
                poiSearch.setBound(new PoiSearch.SearchBound(latLonPoint, 5000, true));
            }
            //异步搜索
            poiSearch.searchPOIAsyn();
        }else{
            Log.d(TAG, "searchSurrend: 传过来的对象是空的");
        }
    }

    /**
     * listView的子项监听
     * **/
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick: 第"+position+"个子项被点击了");
        //关闭键盘
        closeKeyBoard(view);
        Intent intent = new Intent();
        gson = new Gson();
        //处理提示信息列表项的子项点击事件
        if(isPoiSearch){
            intent.putExtra("isPoiSearch",true);
            PoiItem poiItem = (PoiItem) parent.getItemAtPosition(position);
            intent.putExtra("poiItem",gson.toJson(poiItem));
        }else{
            intent.putExtra("isPoiSearch",false);
            Tip tip = (Tip)parent.getItemAtPosition(position);
            if(tip.getPoint()==null){
                Log.d("得到就是空得","123");
            }else{
                Log.d("没得到就是空的","123546");
            }
            intent.putExtra(Constants.EXTRA_TIP, tip);
        }
        setResult(MapFragment.RESULT_CODE_INPUTTIPS, intent);
        this.finish();
    }
    /**
     * 周边搜索结果回调监听
     * **/
    @Override
    public void onPoiSearched(PoiResult result, int rcode) {
        if (rcode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    Log.d(TAG, "onPoiSearched: 回调方法进入");
                    poiResult = result;
                    // 取得第一页的poiitem数据，页数从数字0开始
                    poiItems = poiResult.getPois();
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();
                    if (poiItems != null && poiItems.size() > 0) {
                        inputItemsAdapter = new InputItemsAdapter(getApplicationContext(), poiItems);
                        mInputListView.setAdapter(inputItemsAdapter);
                        mInputListView.setVisibility(View.VISIBLE);
                    } else {
                        ToastUtil.show(getApplicationContext(), R.string.no_result);
                    }
                }
            } else {
                ToastUtil.show(getApplicationContext(), R.string.no_result);
            }
        } else  {
            ToastUtil.showerror(getApplicationContext(), rcode);
        }
    }

    /**
     * 周边返回结果监听
     * **/
    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {
    }

    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(this, infomation);
    }

    /**
     * 关闭键盘的方法
     * **/
    private void closeKeyBoard(View v){
        //关闭键盘
        InputMethodManager imm = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0); //强制隐藏键盘
        }
    }

    /**
     * 回收
     * **/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(query!=null){
            query = null;
        }
        if(poiResult!=null){
            poiResult = null;
        }
        if(poiItems!=null){
            poiItems.clear();
            poiItems = null;
        }
        if(poiSearch!=null){
            poiSearch = null;
        }
        if(inputItemsAdapter!=null){
            inputItemsAdapter = null;
        }
        if(currentLatLng!=null){
            currentLatLng = null;
        }
        //解绑
        unbindService(myServiceConnection);
    }

    /**
     * 输入提示回调
     *
     * @param tipList
     * @param rCode
     */
    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        if (rCode == 1000) {// 正确返回
            List<String> listString = new ArrayList<String>();
            for (int i = 0; i < tipList.size(); i++) {
                if(tipList.get(i).getPoint()==null){
                    tipList.remove(i);
                }
                listString.add(tipList.get(i).getName());
            }
            mCurrentTipList = tipList;
            mIntipAdapter = new InputTipsAdapter(
                    getApplicationContext(),
                    mCurrentTipList);
            mInputListView.setAdapter(mIntipAdapter);
            mIntipAdapter.notifyDataSetChanged();
        } else {
            ToastUtil.showerror(this, rCode);
        }
    }
}

