package com.idx.smartspeakdock.map;

/**
 * Created by hayden on 18-1-15.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.map.adapter.InputTipsAdapter;
import com.idx.smartspeakdock.map.util.Constants;
import com.idx.smartspeakdock.map.util.ToastUtil;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 此Activity专门用来提供地点选择及提示功能
 *
 * **/
public class InputTipsActivity extends Activity implements SearchView.OnQueryTextListener, Inputtips.InputtipsListener, OnItemClickListener, View.OnClickListener {
    private SearchView mSearchView;// 输入搜索关键字
    private ImageView mBack;
    private ListView mInputListView;
    private List<Tip> mCurrentTipList;
    private InputTipsAdapter mIntipAdapter;
    private String city_name;
    private String style;
    private String what_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        city_name = intent.getStringExtra("city_name");
        style = intent.getStringExtra("style");
        what_search = intent.getStringExtra("what_search");
        setContentView(R.layout.map_activity_input);
        initSearchView();
        mInputListView = (ListView) findViewById(R.id.inputtip_list);
        mInputListView.setOnItemClickListener(this);
        mBack = (ImageView) findViewById(R.id.back);
        mBack.setOnClickListener(this);
    }

    private void initSearchView() {
        mSearchView = (SearchView) findViewById(R.id.keyWord);
        mSearchView.setOnQueryTextListener(this);
        //设置SearchView默认为展开显示
        mSearchView.setIconified(false);
        mSearchView.onActionViewExpanded();
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setSubmitButtonEnabled(false);
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
            mCurrentTipList = tipList;
            List<String> listString = new ArrayList<String>();
            for (int i = 0; i < tipList.size(); i++) {
                listString.add(tipList.get(i).getName());
            }
            mIntipAdapter = new InputTipsAdapter(
                    getApplicationContext(),
                    mCurrentTipList);
            mInputListView.setAdapter(mIntipAdapter);
            mIntipAdapter.notifyDataSetChanged();
        } else {
            ToastUtil.showerror(this, rCode);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mCurrentTipList != null) {
            //关闭键盘
            InputMethodManager imm = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //强制隐藏键盘
            }
            Tip tip = (Tip) adapterView.getItemAtPosition(i);
            Intent intent = new Intent();
            intent.putExtra(Constants.EXTRA_TIP, tip);
            intent.putExtra("what_search_result",what_search);
            setResult(MapFragment.RESULT_CODE_INPUTTIPS, intent);
            this.finish();
        }
    }

    /**
     * 按下确认键触发，本例为键盘回车或搜索键
     *
     * @param query
     * @return
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        if(style.equals("search")) {
            Intent intent = new Intent();
            intent.putExtra(Constants.KEY_WORDS_NAME, query);
            setResult(MapFragment.RESULT_CODE_KEYWORDS, intent);
            this.finish();
        }else{
            Toast.makeText(this,"请点击选择",Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * 输入字符变化时触发
     *
     * @param newText
     * @return
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        if (!IsEmptyOrNullString(newText)) {
            Log.d("接收到的城市名为：",city_name);
            InputtipsQuery inputquery = new InputtipsQuery(newText,city_name);
            Inputtips inputTips = new Inputtips(InputTipsActivity.this.getApplicationContext(), inputquery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
        } else {
            if (mIntipAdapter != null && mCurrentTipList != null) {
                mCurrentTipList.clear();
                mIntipAdapter.notifyDataSetChanged();
            }
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back) {
            this.finish();
        }
    }

    public static boolean IsEmptyOrNullString(String s) {
        return (s == null) || (s.trim().length() == 0);
    }
}

