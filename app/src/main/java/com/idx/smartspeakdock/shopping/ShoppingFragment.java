package com.idx.smartspeakdock.shopping;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.unit.listener.IMusicVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.IShoppingVoiceListener;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.Logger;
import com.idx.smartspeakdock.utils.NetStatusUtils;
import com.idx.smartspeakdock.utils.ToastUtils;

/**
 * Created by ryan on 17-12-25.
 * Email: Ryan_chan01212@yeah.net
 */

public class ShoppingFragment extends BaseFragment {
    private final String TAG = "ShoppingFragment";
    private View view;
    private WebView webView;
    public ProgressDialog progDailog;
    private TextView mNetwork_error;
    private SwipeRefreshLayout mNetworkRefresh;
    private Context mContext;
    private String web_url;
    private ShopBroadcastReceiver mShopBroadcastReceiver;

    public static ShoppingFragment newInstance(String web_url){
        ShoppingFragment shoppingFragment = new ShoppingFragment();
        Bundle args = new Bundle();
        args.putString(GlobalUtils.Shopping.SHOPPING_WEBSITES_EXTRA_ID,web_url);
        shoppingFragment.setArguments(args);
        return shoppingFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if(getArguments() != null){
            web_url = getArguments().getString(GlobalUtils.Shopping.SHOPPING_WEBSITES_EXTRA_ID);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.setEnable(true);
        if (savedInstanceState != null){
            web_url = savedInstanceState.getString("weburl");
        }
        progDailog = new ProgressDialog(mContext);
        progDailog.setTitle(getActivity().getResources().getString(R.string.web_message_loading));
        progDailog.setIndeterminate(true);
        progDailog.setCanceledOnTouchOutside(true);
        //注册广播
        registerBroadcast();
//        web_url = "http://m.flnet.com";
//        web_url = "https://mall.flnet.com";
    }

    private void registerBroadcast() {
        mShopBroadcastReceiver = new ShopBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalUtils.Shopping.SHOPPING_BROADCAST_ACTION);
        mContext.registerReceiver(mShopBroadcastReceiver,intentFilter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_shopping,container,false);
        webView = view.findViewById(R.id.shopping_web);
        mNetwork_error = view.findViewById(R.id.network_error);
//        mNetworkRefresh = view.findViewById(R.id.network_swipe_refresh);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
//        progDailog.setCancelable(false);
        webView.setWebChromeClient(new WebChromeClient());
        //判断是否网络
        initWebsites();
        //网络刷新
       // refreshWebsites();
    }

    private void initWebsites() {
        if(checkNetworkStatus()){
            mNetwork_error.setVisibility(View.GONE);
            if(web_url != null) loadWebUrl(web_url);
            else
                ToastUtils.showError(mContext,mContext.getResources().getString(R.string.web_sites_not_exists));
        }else{
            Logger.info(TAG, "onActivityCreated: mNetwork_error = "+mNetwork_error.getText().toString());
            webView.setVisibility(View.INVISIBLE);
            mNetwork_error.setVisibility(View.VISIBLE);
        }
    }

    private void refreshWebsites() {
        mNetworkRefresh.setColorSchemeResources(R.color.colorPrimary);
        mNetworkRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(NetStatusUtils.isWifiConnected(getActivity()) || NetStatusUtils.isMobileConnected(getActivity())){
                    mNetwork_error.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    mNetworkRefresh.setRefreshing(false);
                    if(web_url != null) loadWebUrl(web_url);
                    else
                        ToastUtils.showError(mContext,mContext.getResources().getString(R.string.web_sites_not_exists));
                }else{
                    Logger.info(TAG, "onRefresh: network error");
                    mNetworkRefresh.setRefreshing(false);
                    webView.setVisibility(View.INVISIBLE);
                    mNetwork_error.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("weburl",web_url);
    }

    private void loadWebUrl(String webUrl) {
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Logger.info(TAG, "shouldOverrideUrlLoading: url = "+url);
                progDailog.show();
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progDailog.dismiss();
            }
        });
        webView.loadUrl(webUrl);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //webView销毁
        if(webView != null){
            webView.destroy();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mNetworkRefresh != null) {
            mNetworkRefresh = null;
        }
        if(progDailog != null) {
            progDailog = null;
        }
        mContext.unregisterReceiver(mShopBroadcastReceiver);
        mContext = null;
    }

    public class ShopBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: "+intent.getAction().toString());
            switch (intent.getAction()){
                case GlobalUtils.Shopping.SHOPPING_BROADCAST_ACTION:
                    web_url = intent.getStringExtra("shoppings");
                    Log.i(TAG, "onReceive: web_url = "+web_url);
                    loadWebUrl(web_url);
                    break;
            }
        }
    }
}
