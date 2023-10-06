package org.smartregister.chw.hf.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

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


public class VmmcReportsActivity extends SecuredActivity implements View.OnClickListener{

    protected ConstraintLayout vmmc_monthly_report;

    protected ConstraintLayout vmmc_register_report;

    protected ConstraintLayout vmmc_theatre_register_report;

    protected AppBarLayout appBarLayout;

    Menu menu;

    private String reportPeriod = ReportUtils.getDefaultReportPeriod();

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.vmmc_monthly_report) {
            VmmcReportsViewActivity.startMe(this,  Constants.ReportConstants.ReportPaths.VMMC_REPORT_PATH,R.string.vmmc_reports_subtitle, reportPeriod);
        }
        if (id == R.id.vmmc_register_report) {
            VmmcReportsViewActivity.startMe(this,  Constants.ReportConstants.ReportPaths.VMMC_REGISTER_PATH,R.string.vmmc_register_subtitle, reportPeriod);
        }
        if (id == R.id.vmmc_theatre_register) {
            VmmcReportsViewActivity.startMe(this,  Constants.ReportConstants.ReportPaths.VMMC_THEATRE_REGISTER_PATH,R.string.vmmc_theatre_register_subtitle, reportPeriod);
        }
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_vmmc_reports);
        setUpToolbar();
        setupViews();
    }

    @Override
    protected void onResumption() {
       // implement later
    }

    public void setupViews() {
        vmmc_monthly_report = findViewById(R.id.vmmc_monthly_report);
        vmmc_register_report = findViewById(R.id.vmmc_register_report);
        vmmc_theatre_register_report = findViewById(R.id.vmmc_theatre_register);

        vmmc_monthly_report.setOnClickListener(this);
        vmmc_register_report.setOnClickListener(this);
        vmmc_theatre_register_report.setOnClickListener(this);

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

    public void setUpToolbar() {
        TextView title = findViewById(R.id.toolbar_title);
        title.setText(R.string.vmmc_reports);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reports_menu, menu);
        this.menu = menu;
        this.menu.findItem(R.id.action_select_month).setTitle(ReportUtils.displayMonthAndYear());
        return true;
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
