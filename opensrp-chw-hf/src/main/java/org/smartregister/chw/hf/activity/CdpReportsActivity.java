package org.smartregister.chw.hf.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.ReportUtils;
import org.smartregister.view.activity.SecuredActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class CdpReportsActivity extends SecuredActivity implements View.OnClickListener {
    protected ConstraintLayout cdpIssuingReport;
    protected ConstraintLayout cdpReceivingReport;
    protected AppBarLayout appBarLayout;
    Menu menu;
    private String reportPeriod = ReportUtils.getDefaultReportPeriod();

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_cdp_reports);
        setUpToolbar();
        setupViews();
    }

    public void setupViews() {
        cdpIssuingReport = findViewById(R.id.cdp_issuing_report);
        cdpReceivingReport = findViewById(R.id.cdp_receiving_report);


        cdpIssuingReport.setOnClickListener(this);
        cdpReceivingReport.setOnClickListener(this);
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
        getMenuInflater().inflate(R.menu.reports_menu, menu);
        this.menu = menu;
        this.menu.findItem(R.id.action_select_month).setTitle(ReportUtils.displayMonthAndYear());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_select_month) {
            showMonthPicker(this, menu);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.cdp_issuing_report:
                cdpIssuingSelector();
                break;
            case R.id.cdp_receiving_report:
                CdpReportsViewActivity.startMe(this, Constants.ReportConstants.ReportPaths.CONDOM_DISTRIBUTION_RECEIVING_REPORT_PATH, R.string.cdp_receiving_report, reportPeriod);
                break;
            default:
                break;
        }
    }

    private void cdpIssuingSelector() {
        final String[] Options =
                {getResources().getString(R.string.cdp_issuing_at_the_facility_report),
                        getResources().getString(R.string.cdp_issuing_from_the_facility_report)};
        AlertDialog.Builder window;
        window = new AlertDialog.Builder(this);
        window.setTitle(getResources().getString(R.string.select_issued_condoms));
        window.setItems(Options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    CdpReportsViewActivity.startMe(CdpReportsActivity.this, Constants.ReportConstants.ReportPaths.CONDOM_DISTRIBUTION_ISSUING_AT_THE_FACILITY_REPORT_PATH, R.string.cdp_issuing_at_the_facility_report, reportPeriod);
                }else if(which == 1){
                    CdpReportsViewActivity.startMe(CdpReportsActivity.this, Constants.ReportConstants.ReportPaths.CONDOM_DISTRIBUTION_ISSUING_FROM_THE_FACILITY_REPORT_PATH, R.string.cdp_issuing_from_the_facility_report, reportPeriod);
                }else{
                    // cancel a simple window here
                }
            }
        });

        window.show();
    }

    private void showMonthPicker(Context context, Menu menu) {
        //shows the month picker and returns selected period and updated the menu
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(context, (selectedMonth, selectedYear) -> {
            int month = selectedMonth + 1;
            String monthString = String.valueOf(month);
            if (month < 10) {
                monthString = "0" + monthString;
            }
            String yearString = String.valueOf(selectedYear);
            reportPeriod = monthString + "-" + yearString;
            menu.findItem(R.id.action_select_month).setTitle(ReportUtils.displayMonthAndYear(selectedMonth, selectedYear));

        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH));
        try {
            Date reportDate = new SimpleDateFormat("MM-yyyy", Locale.getDefault()).parse(reportPeriod);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(reportDate);
            builder.setActivatedMonth(calendar.get(Calendar.MONTH));
            builder.setMinYear(2021);
            builder.setActivatedYear(calendar.get(Calendar.YEAR));
            builder.setMaxYear(Calendar.getInstance().get(Calendar.YEAR));
            builder.setMinMonth(Calendar.JANUARY);
            builder.setMaxMonth(Calendar.DECEMBER);
            builder.setTitle("Select Month 0");
            builder.build().show();
        } catch (ParseException e) {
            Timber.e(e);
        }
    }
}