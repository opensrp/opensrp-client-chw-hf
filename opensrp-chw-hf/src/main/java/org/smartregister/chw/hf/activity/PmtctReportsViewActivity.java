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

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.domain.pmtctReports.Pmtct12MonthsReportObject;
import org.smartregister.chw.hf.domain.pmtctReports.Pmtct24MonthsReportObject;
import org.smartregister.chw.hf.domain.pmtctReports.Pmtct3MonthsReportObject;
import org.smartregister.chw.hf.domain.pmtctReports.PmtctEIDMonthlyReportObject;
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

    private String computeThreeMonths(Date startDate) {
        Pmtct3MonthsReportObject pmtct3MonthsReportObject = new Pmtct3MonthsReportObject(startDate);
        try {
            return pmtct3MonthsReportObject.getIndicatorDataAsGson(pmtct3MonthsReportObject.getIndicatorData());
        } catch (JSONException e) {
            Timber.e(e);
        }
        return "";
    }

    private String computeTwelveMonths(Date startDate) {
        Pmtct12MonthsReportObject pmtct12MonthsReportObject = new Pmtct12MonthsReportObject(startDate);
        try {
            return pmtct12MonthsReportObject.getIndicatorDataAsGson(pmtct12MonthsReportObject.getIndicatorData());
        } catch (JSONException e) {
            Timber.e(e);
        }
        return "";
    }

    private String computeTwentyFourMonths(Date statDate) {
        Pmtct24MonthsReportObject pmtct24MonthsReportObject = new Pmtct24MonthsReportObject(statDate);
        try {
            return pmtct24MonthsReportObject.getIndicatorDataAsGson(pmtct24MonthsReportObject.getIndicatorData());
        } catch (JSONException e) {
            Timber.e(e);
        }
        return "";
    }

    private String computeEIDMonthly(Date statDate) {
        PmtctEIDMonthlyReportObject pmtctEIDMonthlyReportObject = new PmtctEIDMonthlyReportObject(statDate);
        try {
            return pmtctEIDMonthlyReportObject.getIndicatorDataAsGson(pmtctEIDMonthlyReportObject.getIndicatorData());
        } catch (JSONException e) {
            Timber.e(e);
        }
        return "";
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
            Date now = new Date();
            switch (key) {
                case "three_months":
                    return computeThreeMonths(now);
                case "twelve_months":
                    return computeTwelveMonths(now);
                case "twenty_four_months":
                    return computeTwentyFourMonths(now);
                case "eid_monthly":
                    return computeEIDMonthly(now);
            }
            return "";

        }
    }
}