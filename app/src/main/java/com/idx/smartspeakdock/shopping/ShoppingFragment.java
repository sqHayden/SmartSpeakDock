package com.idx.smartspeakdock.shopping;

import android.app.ProgressDialog;
import android.content.Context;
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
    private static final String TAG = ShoppingFragment.class.getSimpleName();

    View view;
    WebView webView;
    ProgressDialog progDailog;
    TextView mNetwork_error;
    SwipeRefreshLayout mNetworkRefresh;
    Context mContext;
    String web_url;

    public static ShoppingFragment newInstance(String web_url){
        ShoppingFragment shoppingFragment = new ShoppingFragment();
        Bundle args = new Bundle();
        args.putString(GlobalUtils.SHOPPING_WEBSITES_EXTRA_ID,web_url);
        shoppingFragment.setArguments(args);
        return shoppingFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if(getArguments() != null){
            web_url = getArguments().getString(GlobalUtils.SHOPPING_WEBSITES_EXTRA_ID);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.setEnable(true);
        progDailog = ProgressDialog.show(getActivity(), getActivity().getResources().getString(R.string.web_title_loading),
                getActivity().getResources().getString(R.string.web_message_loading), true);
//        web_url = "http://m.flnet.com";
//        web_url = "https://mall.flnet.com";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_shopping,container,false);
        webView = view.findViewById(R.id.shopping_web);
        mNetwork_error = view.findViewById(R.id.network_error);
        mNetworkRefresh = view.findViewById(R.id.network_swipe_refresh);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initWebsites();
        refreshWebsites();
        voiceResult();
    }

    private void initWebsites() {
        if(NetStatusUtils.isMobileConnected(getActivity()) || NetStatusUtils.isWifiConnected(getActivity())){
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

    private void voiceResult() {
        UnitManager.getInstance().setShoppingVoiceListener(new IShoppingVoiceListener() {
            @Override
            public void openSpecifyWebsites(String web_sites_url) {
                Logger.info(TAG,web_sites_url);
                if(web_sites_url != null) loadWebUrl(web_sites_url);
                else
                    ToastUtils.showError(mContext,mContext.getResources().getString(R.string.web_sites_not_exists));
            }
        });
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

    private void loadWebUrl(String webUrl) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        progDailog.setCancelable(false);
        webView.setWebChromeClient(new WebChromeClient());
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
    public void onDestroy() {
        super.onDestroy();
        if(mNetworkRefresh != null) mNetworkRefresh = null;
        if(progDailog != null) progDailog = null;
    }
}
