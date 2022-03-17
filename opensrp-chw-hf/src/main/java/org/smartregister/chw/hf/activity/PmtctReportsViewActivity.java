package org.smartregister.chw.hf.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import timber.log.Timber;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.R;

public class PmtctReportsViewActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pmtct_reports_view);
        WebView mWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(this))
                .build();
        mWebView.setWebViewClient(new LocalContentWebViewClient(assetLoader));
        mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        mWebView.loadUrl("https://appassets.androidplatform.net/assets/test_report.html");


    }

    private static class LocalContentWebViewClient extends WebViewClientCompat {

        private final WebViewAssetLoader mAssetLoader;

        LocalContentWebViewClient(WebViewAssetLoader assetLoader) {
            mAssetLoader = assetLoader;
        }

       @Override
        @RequiresApi(21)
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          WebResourceRequest request) {
            return mAssetLoader.shouldInterceptRequest(request.getUrl());
        }

        @Override
        @SuppressWarnings("deprecation") // to support API < 21
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          String url) {
            return mAssetLoader.shouldInterceptRequest(Uri.parse(url));
        }

    }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public String getData() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("B3a", 10 );
                jsonObject.put("B3b", 12 );
                jsonObject.put("B3c", 13 );
                jsonObject.put("B3d", 14 );
                jsonObject.put("C3a", 15 );
                jsonObject.put("C3b", 19 );
            } catch (JSONException e) {
                Timber.e(e);
            }
            Gson gson = new Gson();
            gson.toJson(jsonObject);
            Timber.d(jsonObject.toString());
            return gson.toJson(jsonObject);
        }

    }
}