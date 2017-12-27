package com.idx.smartspeakdock.shopping;

import android.app.ProgressDialog;
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
import com.idx.smartspeakdock.utils.NetStatusUtils;

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
    String web_url;

    public static ShoppingFragment newInstance(){return new ShoppingFragment();}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web_url = "http://m.flnet.com";
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
        if(NetStatusUtils.isMobileConnected(getActivity()) || NetStatusUtils.isWifiConnected(getActivity())){
            mNetwork_error.setVisibility(View.GONE);
            loadWebUrl(web_url);
        }else{
            Log.i(TAG, "onActivityCreated: mNetwork_error = "+mNetwork_error.getText().toString());
            webView.setVisibility(View.INVISIBLE);
            mNetwork_error.setVisibility(View.VISIBLE);
        }

        mNetworkRefresh.setColorSchemeResources(R.color.colorPrimary);
        mNetworkRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(NetStatusUtils.isWifiConnected(getActivity()) || NetStatusUtils.isMobileConnected(getActivity())){
                    mNetwork_error.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    mNetworkRefresh.setRefreshing(false);
                    loadWebUrl(web_url);
                }else{
                    Log.i(TAG, "onRefresh: network error");
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
        progDailog = ProgressDialog.show(getActivity(), getActivity().getResources().getString(R.string.web_title_loading),
                getActivity().getResources().getString(R.string.web_message_loading), true);
        progDailog.setCancelable(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "shouldOverrideUrlLoading: url = "+url);
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
