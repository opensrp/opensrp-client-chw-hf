package org.smartregister.chw.hf.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.domain.pmtct_reports.Pmtct12MonthsReportObject;
import org.smartregister.chw.hf.domain.pmtct_reports.Pmtct24MonthsReportObject;
import org.smartregister.chw.hf.domain.pmtct_reports.Pmtct3MonthsReportObject;
import org.smartregister.chw.hf.domain.pmtct_reports.PmtctEIDMonthlyReportObject;
import org.smartregister.chw.hf.utils.HfWebAppInterface;
import org.smartregister.chw.hf.utils.LocalContentWebViewClient;
import org.smartregister.chw.hf.utils.ReportUtils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.webkit.WebViewAssetLoader;
import timber.log.Timber;

import static org.smartregister.chw.hf.utils.Constants.ReportConstants.PMTCTReportKeys.EID_MONTHLY;
import static org.smartregister.chw.hf.utils.Constants.ReportConstants.PMTCTReportKeys.THREE_MONTHS;
import static org.smartregister.chw.hf.utils.Constants.ReportConstants.PMTCTReportKeys.TWELVE_MONTHS;
import static org.smartregister.chw.hf.utils.Constants.ReportConstants.PMTCTReportKeys.TWENTY_FOUR_MONTHS;

public class PmtctReportsViewActivity extends AppCompatActivity {
    private static final String ARG_REPORT_NAME = "ARG_REPORT_NAME";
    private static final String ARG_REPORT_TITLE = "ARG_REPORT_TITLE";
    private static final String ARG_REPORT_DATE = "ARG_REPORT_DATE";
    public static WebView printWebView;
    private static Date reportDate;
    private static String reportPeriod;
    protected CustomFontTextView toolBarTextView;
    protected AppBarLayout appBarLayout;
    String printJobName;

    public static void startMe(Activity activity, String reportName, int reportTitle, String reportDate) {
        Intent intent = new Intent(activity, PmtctReportsViewActivity.class);
        intent.putExtra(ARG_REPORT_NAME, reportName);
        intent.putExtra(ARG_REPORT_TITLE, reportTitle);

        reportPeriod = reportDate;
        intent.putExtra(ARG_REPORT_DATE, reportDate);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pmtct_reports_view);
        String reportName = getIntent().getStringExtra(ARG_REPORT_NAME);
        int reportTitle = getIntent().getIntExtra(ARG_REPORT_TITLE, 0);
        try {
            reportDate = new SimpleDateFormat("MM-yyyy", Locale.getDefault()).parse(getIntent().getStringExtra(ARG_REPORT_DATE));
        } catch (ParseException e) {
            Timber.e(e);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reports_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_print) {
            if (printWebView != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ReportUtils.printTheWebPage(printWebView, PmtctReportsViewActivity.this, printJobName);
                } else {
                    Toast.makeText(this, "Not available for device below Android LOLLIPOP", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "WebPage not fully loaded", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        mWebView.addJavascriptInterface(new PmtctReportWebInterface(this), "Android");
        mWebView.loadUrl("https://appassets.androidplatform.net/assets/reports/pmtct-reports/" + reportName + ".html");
    }

    private String computeThreeMonths(Date startDate) {
        Pmtct3MonthsReportObject pmtct3MonthsReportObject = new Pmtct3MonthsReportObject(startDate);
        try {
            printJobName = "report_ya_miezi_mitatu-" + reportPeriod + ".pdf";
            return pmtct3MonthsReportObject.getIndicatorDataAsGson(pmtct3MonthsReportObject.getIndicatorData());
        } catch (JSONException e) {
            Timber.e(e);
        }
        return "";
    }

    private String computeTwelveMonths(Date startDate) {
        Pmtct12MonthsReportObject pmtct12MonthsReportObject = new Pmtct12MonthsReportObject(startDate);
        try {
            printJobName = "report_ya_miezi_kumi_na_mbili-" + reportPeriod + ".pdf";
            return pmtct12MonthsReportObject.getIndicatorDataAsGson(pmtct12MonthsReportObject.getIndicatorData());
        } catch (JSONException e) {
            Timber.e(e);
        }
        return "";
    }

    private String computeTwentyFourMonths(Date startDate) {
        Pmtct24MonthsReportObject pmtct24MonthsReportObject = new Pmtct24MonthsReportObject(startDate);
        try {
            printJobName = "report_ya_miaka_miwili-" + reportPeriod + ".pdf";
            return pmtct24MonthsReportObject.getIndicatorDataAsGson(pmtct24MonthsReportObject.getIndicatorData());
        } catch (JSONException e) {
            Timber.e(e);
        }
        return "";
    }

    private String computeEIDMonthly(Date startDate) {
        PmtctEIDMonthlyReportObject pmtctEIDMonthlyReportObject = new PmtctEIDMonthlyReportObject(startDate);
        try {
            printJobName = "report_ya_mwezi-" + reportPeriod + ".pdf";
            return pmtctEIDMonthlyReportObject.getIndicatorDataAsGson(pmtctEIDMonthlyReportObject.getIndicatorData());
        } catch (JSONException e) {
            Timber.e(e);
        }
        return "";
    }


    public class PmtctReportWebInterface extends HfWebAppInterface {

        public PmtctReportWebInterface(Context context) {
            super(context);
        }

        @JavascriptInterface
        @Override
        public String getData(String key) {
            switch (key) {
                case THREE_MONTHS:
                    return computeThreeMonths(reportDate);
                case TWELVE_MONTHS:
                    return computeTwelveMonths(reportDate);
                case TWENTY_FOUR_MONTHS:
                    return computeTwentyFourMonths(reportDate);
                case EID_MONTHLY:
                    return computeEIDMonthly(reportDate);
                default:
                    return "";
            }
        }

        @JavascriptInterface
        @Override
        public String getDataPeriod() {
            return reportPeriod;
        }
    }
}