package com.idx.smartspeakdock.shopping;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.idx.smartspeakdock.R;

/**
 * Created by ryan on 17-12-25.
 * Email: Ryan_chan01212@yeah.net
 */

public class ShoppingFragment extends Fragment {
    private static final String TAG = ShoppingFragment.class.getSimpleName();
    View view;
    WebView webView;
    ProgressDialog progDailog;

    public static ShoppingFragment newInstance(){return new ShoppingFragment();}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_shopping,container,false);
        webView = view.findViewById(R.id.shopping_web);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        webView.getSettings().setJavaScriptEnabled(true);
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
        webView.loadUrl("http://m.flnet.com");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
