package org.smartregister.chw.hf.activity;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Menu;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;

import org.smartregister.chw.hf.R;
import org.smartregister.view.activity.SecuredActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

public class PncReportsActivity extends SecuredActivity implements View.OnClickListener {
    protected ConstraintLayout monthlyReport;
    protected AppBarLayout appBarLayout;

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_pnc_reports);
        setUpToolbar();
        setupViews();
    }

    public void setupViews() {
        monthlyReport = findViewById(R.id.pnc_monthly_report);
        monthlyReport.setOnClickListener(this);
    }

    @Override
    protected void onResumption() {
        setUpToolbar();
        setupViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
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
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.pnc_monthly_report) {
            PncReportsViewActivity.startMe(this, "pnc-taarifa-ya-mwezi");
        }
    }
}