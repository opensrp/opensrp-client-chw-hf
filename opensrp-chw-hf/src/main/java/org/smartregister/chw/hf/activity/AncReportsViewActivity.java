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

import com.google.android.material.appbar.AppBarLayout;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.domain.anc_reports.AncMonthlyReportObject;
import org.smartregister.view.activity.SecuredActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import timber.log.Timber;

import static org.smartregister.util.Utils.getAllSharedPreferences;

public class AncReportsViewActivity extends SecuredActivity {

    private static final String ARG_REPORT_NAME = "ARG_REPORT_NAME";
    private static final String ARG_REPORT_DATE = "ARG_REPORT_DATE";
    private static Date reportDate;
    private static String reportPeriod;
    protected CustomFontTextView toolBarTextView;
    protected AppBarLayout appBarLayout;

    public static void startMe(Activity activity, String reportName, String reportDate) {
        Intent intent = new Intent(activity, AncReportsViewActivity.class);
        intent.putExtra(ARG_REPORT_NAME, reportName);
        intent.putExtra(ARG_REPORT_DATE, reportDate);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_anc_reports_view);
        String reportName = getIntent().getStringExtra(ARG_REPORT_NAME);
        try {
            reportDate = new SimpleDateFormat("MM-yyyy", Locale.getDefault()).parse(getIntent().getStringExtra(ARG_REPORT_DATE));
        } catch (ParseException e) {
            Timber.e(e);
        }
        setUpToolbar();
        loadReportView(reportName);
    }

    public void setUpToolbar() {
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

        toolBarTextView.setText(R.string.anc_reports_title);

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

    private String computeAncReport(Date now) {
        String report = "";
        AncMonthlyReportObject ancMonthlyReportObject = new AncMonthlyReportObject(now);
        try {
            report = ancMonthlyReportObject.getIndicatorDataAsGson(ancMonthlyReportObject.getIndicatorData());
        } catch (Exception e) {
            Timber.e(e);
        }
        return report;
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

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public String getData(String key) {

            if (key.equals("monthly")) {
                return computeAncReport(reportDate);
            }
            return "";
        }

        @JavascriptInterface
        public String getDataPeriod() {
            return reportPeriod;
        }

        @JavascriptInterface
        public String getReportingFacility() {
            return getAllSharedPreferences().fetchCurrentLocality();
        }
    }
}