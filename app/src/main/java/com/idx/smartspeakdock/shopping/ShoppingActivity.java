package com.idx.smartspeakdock.shopping;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.utils.Logger;
import com.idx.smartspeakdock.utils.NetStatusUtils;

/**
 * Created by ryan on 17-12-27.
 * Email: Ryan_chan01212@yeah.net
 */

public class ShoppingActivity extends AppCompatActivity {
    private static final String TAG = ShoppingActivity.class.getSimpleName();
    WebView webView;
    ProgressDialog progDailog;
    TextView mNetwork_error;
    SwipeRefreshLayout mNetworkRefresh;
    String web_url;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_shopping);
        Logger.setEnable(true);
        web_url = "http://m.flnet.com";
        init();
    }

    private void init() {
        webView = findViewById(R.id.shopping_web);
        mNetwork_error = findViewById(R.id.network_error);
        mNetworkRefresh = findViewById(R.id.network_swipe_refresh);
        if(NetStatusUtils.isMobileConnected(this) || NetStatusUtils.isWifiConnected(this)){
            mNetwork_error.setVisibility(View.GONE);
            loadWebUrl(web_url);
        }else{
            Logger.info(TAG, "onActivityCreated: mNetwork_error = "+mNetwork_error.getText().toString());
            webView.setVisibility(View.INVISIBLE);
            mNetwork_error.setVisibility(View.VISIBLE);
        }

        mNetworkRefresh.setColorSchemeResources(R.color.colorPrimary);
        mNetworkRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(NetStatusUtils.isWifiConnected(ShoppingActivity.this) || NetStatusUtils.isMobileConnected(ShoppingActivity.this)){
                    mNetwork_error.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    mNetworkRefresh.setRefreshing(false);
                    loadWebUrl(web_url);
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
        progDailog = ProgressDialog.show(this, this.getResources().getString(R.string.web_title_loading),
                this.getResources().getString(R.string.web_message_loading), true);
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
    }
}
