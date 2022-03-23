package org.smartregister.chw.hf.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

import org.smartregister.chw.core.job.ChwIndicatorGeneratingJob;
import org.smartregister.chw.hf.R;
import org.smartregister.view.activity.SecuredActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ReportsActivity extends SecuredActivity implements View.OnClickListener {
    protected CustomFontTextView toolBarTextView;
    protected AppBarLayout appBarLayout;
    protected ConstraintLayout pmtctReportsLayout;
    protected ConstraintLayout ancReportsLayout;

    @Override
    protected void onCreation() {
        ChwIndicatorGeneratingJob.scheduleJobImmediately(ChwIndicatorGeneratingJob.TAG);
        setContentView(R.layout.activity_reports);
        setUpToolbar();
        setUpViews();
    }

    public void setUpViews() {
        pmtctReportsLayout = findViewById(R.id.pmtct_reports);
        ancReportsLayout = findViewById(R.id.anc_reports);

        pmtctReportsLayout.setOnClickListener(this);
        ancReportsLayout.setOnClickListener(this);
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
        toolBarTextView.setText(R.string.reports_title);
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.anc_reports) {
            //TODO: implement loading ANC reports view
            Toast.makeText(this, "ANC Reports", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.pmtct_reports) {
            startActivity(new Intent(this, PmtctReportsActivity.class));
        }
    }
}