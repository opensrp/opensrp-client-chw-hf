package org.smartregister.chw.hf.activity;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.ReportUtils;
import org.smartregister.view.customcontrols.CustomFontTextView;

public class HfReportsViewActivity extends AppCompatActivity {

    protected static final String ARG_REPORT_PATH = "ARG_REPORT_PATH";

    protected static final String ARG_REPORT_TITLE = "ARG_REPORT_TITLE";

    protected static final String ARG_REPORT_DATE = "ARG_REPORT_DATE";

    protected static final String ARG_REPORT_TYPE = "ARG_REPORT_TYPE";

    public static WebView printWebView;

    protected CustomFontTextView toolBarTextView;

    protected AppBarLayout appBarLayout;

    String reportPath;

    String reportDate;

    String reportType;

    int reportTitle;

    WebView webView;

    WebSettings webSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_view);
        reportPath = getIntent().getStringExtra(ARG_REPORT_PATH);
        reportDate = getIntent().getStringExtra(ARG_REPORT_DATE);
        reportType = getIntent().getStringExtra(ARG_REPORT_TYPE);
        reportTitle = getIntent().getIntExtra(ARG_REPORT_TITLE, 0);
        setUpToolbar(reportTitle);
        webView = findViewById(R.id.webview);
        ProgressBar progressBar = findViewById(R.id.progress_bar);

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        ReportUtils.setReportPeriod(reportDate);
        ReportUtils.loadReportView(reportPath, webView, progressBar, this, reportType);
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
            toolBarTextView.setText(R.string.reports_title);
        }
        toolBarTextView.setOnClickListener(v -> finish());
        appBarLayout = findViewById(org.smartregister.chw.core.R.id.app_bar);
        appBarLayout.setOutlineProvider(null);
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
                ReportUtils.printTheWebPage(printWebView, this);
            } else {
                Toast.makeText(this, "WebPage not fully loaded", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        if (itemId == R.id.action_refresh) {
            ReportUtils.loadReportView(webView);
            Snackbar.make(getWindow().getDecorView(), "please wait while refreshing data..", Snackbar.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
