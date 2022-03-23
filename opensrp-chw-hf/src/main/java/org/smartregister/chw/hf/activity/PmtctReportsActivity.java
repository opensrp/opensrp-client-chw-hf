package org.smartregister.chw.hf.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.Menu;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.job.ChwIndicatorGeneratingJob;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.fragment.PmtctReportsFragment;
import org.smartregister.view.activity.SecuredActivity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import timber.log.Timber;

public class PmtctReportsActivity extends SecuredActivity  implements View.OnClickListener{
    protected ConstraintLayout threeMonthsReport;
    protected ConstraintLayout twelveMonthsReport;
    protected ConstraintLayout twentyFourMonthsReport;
    protected ConstraintLayout crossSectionalMonthsReport;
    protected AppBarLayout appBarLayout;

    @Override
    protected void onCreation() {
        ChwIndicatorGeneratingJob.scheduleJobImmediately(ChwIndicatorGeneratingJob.TAG);
        setContentView(R.layout.activity_pmtct_reports);
        setUpToolbar();
        setupViews();

    }

    public void setupViews() {
        threeMonthsReport = findViewById(R.id.three_months_report);
        twelveMonthsReport = findViewById(R.id.twelve_months_report);
        twentyFourMonthsReport = findViewById(R.id.twenty_four_months_report);
        crossSectionalMonthsReport = findViewById(R.id.cross_sectional_report);

        threeMonthsReport.setOnClickListener(this);
        twelveMonthsReport.setOnClickListener(this);
        twentyFourMonthsReport.setOnClickListener(this);
        crossSectionalMonthsReport.setOnClickListener(this);
    }

    public void setUpToolbar() {
        Toolbar toolbar = findViewById(org.smartregister.chw.core.R.id.back_to_nav_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            final Drawable upArrow = getResources().getDrawable(org.smartregister.chw.core.R.drawable.ic_arrow_back_white_24dp);
            actionBar.setHomeAsUpIndicator(upArrow);
            actionBar.setElevation(0);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
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
        return   false;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.three_months_report:
                PmtctReportsViewActivity.startMe(this,"taarifa-ya-miezi-3",R.string.three_months_report);
                break;
            case R.id.twelve_months_report:
                PmtctReportsViewActivity.startMe(this,"taarifa-ya-miezi-12",R.string.twelve_months_report);
                break;
            case R.id.twenty_four_months_report:
                PmtctReportsViewActivity.startMe(this,"taarifa-ya-miezi-24",R.string.twenty_four_months_report);
                break;
            case R.id.cross_sectional_report:
                PmtctReportsViewActivity.startMe(this,"taarifa-cross-sectional",R.string.eid_cross_sectional_report);
                break;
            default:
                Toast.makeText(this, "Action Not Defined", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}