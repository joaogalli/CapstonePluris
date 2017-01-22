package com.github.joaogalli.capstonepluris.posts;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.joaogalli.capstonepluris.R;

public class WebViewActivity extends AppCompatActivity {

    public static final String URL_PARAM = "url_param";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        Intent intent = getIntent();

        if (intent != null && intent.getStringExtra(URL_PARAM) != null) {
            WebView webView = (WebView) findViewById(R.id.webview);
            webView.setWebChromeClient(new WebChromeClient());
            webView.setWebViewClient(new WebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(intent.getStringExtra(URL_PARAM));
        } else {
            finishActivity(RESULT_CANCELED);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

}
