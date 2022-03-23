package org.smartregister.chw.hf.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.Menu;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.PmtctReportDao;
import org.smartregister.view.activity.SecuredActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.Date;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import timber.log.Timber;

public class PmtctReportsViewActivity extends SecuredActivity {
    private static final String ARG_REPORT_NAME = "ARG_REPORT_NAME";
    private static final String ARG_REPORT_TITLE = "ARG_REPORT_TITLE";
    protected CustomFontTextView toolBarTextView;
    protected AppBarLayout appBarLayout;

    public static void startMe(Activity activity, String reportName, int reportTitle) {
        Intent intent = new Intent(activity, PmtctReportsViewActivity.class);
        intent.putExtra(ARG_REPORT_NAME, reportName);
        intent.putExtra(ARG_REPORT_TITLE, reportTitle);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_pmtct_reports_view);
        String reportName = getIntent().getStringExtra(ARG_REPORT_NAME);
        int reportTitle = getIntent().getIntExtra(ARG_REPORT_TITLE, 0);
        setUpToolbar(reportTitle);
        loadReportView(reportName);
    }

    public void setUpToolbar(int reportTitle) {
        Toolbar toolbar = findViewById(org.smartregister.chw.core.R.id.back_to_nav_toolbar);
        toolBarTextView = toolbar.findViewById(org.smartregister.chw.core.R.id.toolbar_title);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            final Drawable upArrow = getResources().getDrawable(org.smartregister.chw.core.R.drawable.ic_arrow_back_white_24dp);
            actionBar.setHomeAsUpIndicator(upArrow);
            actionBar.setElevation(0);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        if (StringUtils.isNotBlank(getString(reportTitle))) {
            toolBarTextView.setText(getString(reportTitle));
        } else {
            toolBarTextView.setText(R.string.pmtct_reports_title);
        }
        toolBarTextView.setOnClickListener(v -> finish());
        appBarLayout = findViewById(org.smartregister.chw.core.R.id.app_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setOutlineProvider(null);
        }
    }

    @Override
    protected void onResumption() {
        //overridden
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadReportView(String reportName) {
        WebView mWebView = findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();
        mWebView.setWebViewClient(new LocalContentWebViewClient(assetLoader));
        mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        mWebView.loadUrl("https://appassets.androidplatform.net/assets/reports/" + reportName + ".html");
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

        /**
         * Instantiate the interface and set the context
         */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /**
         * Show a toast from the web page
         */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public String getData() {
            JSONObject jsonObject = new JSONObject();
            Date now = new Date();
            try {
                jsonObject.put("B3a", PmtctReportDao.getPmtctReportPerIndicatorCode("B3a",now));
                jsonObject.put("B3b", PmtctReportDao.getPmtctReportPerIndicatorCode("B3b",now));
                jsonObject.put("B3c", PmtctReportDao.getPmtctReportPerIndicatorCode("B3c",now));
                jsonObject.put("B3d", PmtctReportDao.getPmtctReportPerIndicatorCode("B3d",now));
                jsonObject.put("C3a", PmtctReportDao.getPmtctReportPerIndicatorCode("C3a",now));
                jsonObject.put("C3b", PmtctReportDao.getPmtctReportPerIndicatorCode("C3b",now));
                jsonObject.put("D3", PmtctReportDao.getPmtctReportPerIndicatorCode("D3",now));
                jsonObject.put("E3", PmtctReportDao.getPmtctReportPerIndicatorCode("E3",now));
                jsonObject.put("F3", PmtctReportDao.getPmtctReportPerIndicatorCode("F3",now));
                jsonObject.put("G3", PmtctReportDao.getPmtctReportPerIndicatorCode("G3",now));
                jsonObject.put("H3", PmtctReportDao.getPmtctReportPerIndicatorCode("H3",now));
                jsonObject.put("I3", PmtctReportDao.getPmtctReportPerIndicatorCode("I3",now));
                jsonObject.put("J3", PmtctReportDao.getPmtctReportPerIndicatorCode("J3",now));
                jsonObject.put("K3", PmtctReportDao.getPmtctReportPerIndicatorCode("K3",now));
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