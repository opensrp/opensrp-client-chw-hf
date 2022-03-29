package org.smartregister.chw.hf.activity;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import org.smartregister.chw.hf.R;
import org.smartregister.view.activity.SecuredActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

public class PmtctReportsActivity extends SecuredActivity implements View.OnClickListener {
    protected ConstraintLayout threeMonthsReport;
    protected ConstraintLayout twelveMonthsReport;
    protected ConstraintLayout twentyFourMonthsReport;
    protected ConstraintLayout crossSectionalMonthsReport;
    protected AppBarLayout appBarLayout;
    protected Button monthSelector;
    protected TextView reportSelectedPeriod;
    int year = Calendar.getInstance().get(Calendar.YEAR);
    int month =Calendar.getInstance().get(Calendar.MONTH) + 1;
    String reportPeriod = month + "-" + year;

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_pmtct_reports);
        setUpToolbar();
        setupViews();
    }

    public void setupViews() {
        threeMonthsReport = findViewById(R.id.three_months_report);
        twelveMonthsReport = findViewById(R.id.twelve_months_report);
        twentyFourMonthsReport = findViewById(R.id.twenty_four_months_report);
        crossSectionalMonthsReport = findViewById(R.id.cross_sectional_report);
        monthSelector = findViewById(R.id.month_selector);
        reportSelectedPeriod = findViewById(R.id.selected_period);

        threeMonthsReport.setOnClickListener(this);
        twelveMonthsReport.setOnClickListener(this);
        twentyFourMonthsReport.setOnClickListener(this);
        crossSectionalMonthsReport.setOnClickListener(this);
        monthSelector.setOnClickListener(this);
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
        return false;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.three_months_report:
                PmtctReportsViewActivity.startMe(this, "taarifa-ya-miezi-3", R.string.three_months_report,reportPeriod);
                break;
            case R.id.twelve_months_report:
                PmtctReportsViewActivity.startMe(this, "taarifa-ya-miezi-12", R.string.twelve_months_report, reportPeriod);
                break;
            case R.id.twenty_four_months_report:
                PmtctReportsViewActivity.startMe(this, "taarifa-ya-miezi-24", R.string.twenty_four_months_report, reportPeriod);
                break;
            case R.id.cross_sectional_report:
                PmtctReportsViewActivity.startMe(this, "taarifa-cross-sectional", R.string.eid_cross_sectional_report, reportPeriod);
                break;
            case R.id.month_selector:
                showMonthPicker();
                break;
            default:
                Toast.makeText(this, "Action Not Defined", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void showMonthPicker() {
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(this, (selectedMonth, selectedYear) -> {
            int month = selectedMonth + 1;
            String monthString = String.valueOf(month);
            if (month < 10) {
                monthString = "0" + monthString;
            }
            String yearString = String.valueOf(selectedYear);
            reportPeriod = monthString + "-" + yearString;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
            Date now;
            try {
                now = simpleDateFormat.parse(reportPeriod);
                reportSelectedPeriod.setText(now.toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH));
        builder.setActivatedMonth(Calendar.getInstance().get(Calendar.MONTH));
        builder.setMinYear(2021);
        builder.setActivatedYear(Calendar.getInstance().get(Calendar.YEAR));
        builder.setMaxYear(Calendar.getInstance().get(Calendar.YEAR));
        builder.setMinMonth(Calendar.JANUARY);
        builder.setMaxMonth(Calendar.DECEMBER);
        builder.setTitle("Select Month");
        builder.setOnMonthChangedListener((selectedMonth) -> {
            int month = selectedMonth + 1;
            String monthString = String.valueOf(month);
            if (month < 10) {
                monthString = "0" + monthString;
            }
        });
        builder.build().show();
    }
}