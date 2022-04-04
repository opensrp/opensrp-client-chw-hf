package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.ReportUtils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PmtctReportsViewActivity extends AppCompatActivity {
    private static final String ARG_REPORT_NAME = "ARG_REPORT_NAME";
    private static final String ARG_REPORT_TITLE = "ARG_REPORT_TITLE";
    private static final String ARG_REPORT_DATE = "ARG_REPORT_DATE";
    public static WebView printWebView;
    protected CustomFontTextView toolBarTextView;
    protected AppBarLayout appBarLayout;

    public static void startMe(Activity activity, String reportName, int reportTitle, String reportDate) {
        Intent intent = new Intent(activity, PmtctReportsViewActivity.class);
        intent.putExtra(ARG_REPORT_NAME, reportName);
        intent.putExtra(ARG_REPORT_TITLE, reportTitle);
        intent.putExtra(ARG_REPORT_DATE, reportDate);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pmtct_reports_view);
        String reportName = getIntent().getStringExtra(ARG_REPORT_NAME);
        int reportTitle = getIntent().getIntExtra(ARG_REPORT_TITLE, 0);
        setUpToolbar(reportTitle);
        WebView webView = findViewById(R.id.webview);
        ReportUtils.setReportPeriod(getIntent().getStringExtra(ARG_REPORT_DATE));
        ReportUtils.loadReportView(reportName, webView, this);
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
                    ReportUtils.printTheWebPage(printWebView, PmtctReportsViewActivity.this);
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


}