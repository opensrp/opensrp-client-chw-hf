package org.smartregister.chw.hf.activity;

import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONObject;
import org.smartregister.chw.core.activity.HIA2ReportsActivity;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.domain.MonthlyTally;
import org.smartregister.chw.core.fragment.SendMonthlyDraftDialogFragment;
import org.smartregister.chw.core.task.FetchEditedMonthlyTalliesTask;
import org.smartregister.chw.core.task.StartDraftMonthlyFormTask;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.HfSectionsPagerAdapter;
import org.smartregister.chw.hf.fragment.HfDraftMonthlyFragment;
import org.smartregister.domain.Response;
import org.smartregister.repository.Hia2ReportRepository;
import org.smartregister.service.HTTPAgent;
import org.smartregister.util.Utils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HfHIA2ReportsActivity extends HIA2ReportsActivity {
    protected AppBarLayout appBarLayout;
    private HfSectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_hf_hia2_reports);
        Toolbar toolbar = findViewById(R.id.back_to_nav_toolbar);
        CustomFontTextView toolBarTextView = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            upArrow.setVisible(true, true);
            actionBar.setHomeAsUpIndicator(upArrow);
            actionBar.setElevation(0);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolBarTextView.setOnClickListener(v -> finish());
        toolBarTextView.setText(this.getString(R.string.service_activity_reporting));

        appBarLayout = findViewById(R.id.app_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setOutlineProvider(null);
        }

        tabLayout = findViewById(org.smartregister.chw.core.R.id.hia_tabs);
        mSectionsPagerAdapter = new HfSectionsPagerAdapter(getSupportFragmentManager(), this);

        mViewPager = findViewById(org.smartregister.chw.core.R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout.setupWithViewPager(mViewPager);
        refreshDraftMonthlyTitle();
        mSectionsPagerAdapter.getItem(0);
        mViewPager.setCurrentItem(0);
    }

    @Override
    protected Fragment currentFragment() {
        if (mViewPager == null || mSectionsPagerAdapter == null) {
            return null;
        }

        return mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem());
    }

    @Override
    public void refreshDraftMonthlyTitle() {
        Utils.startAsyncTask(new FetchEditedMonthlyTalliesTask(new FetchEditedMonthlyTalliesTask.TaskListener() {
            @Override
            public void onPostExecute(final List<MonthlyTally> monthlyTallies) {
                tabLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
                            TabLayout.Tab tab = tabLayout.getTabAt(i);
                            if (tab != null && tab.getText() != null && tab.getText().toString()
                                    .contains(getString(org.smartregister.chw.core.R.string.hia2_draft_monthly))) {
                                tab.setText(String.format(
                                        getString(org.smartregister.chw.core.R.string.hia2_draft_monthly_with_count),
                                        monthlyTallies == null ? 0 : monthlyTallies.size()));
                            }
                        }
                    }
                });
            }
        }), null);
    }

    @Override
    public void startMonthlyReportForm(String formName, Date date) {
        try {
            Fragment currentFragment = currentFragment();
            if (currentFragment instanceof HfDraftMonthlyFragment) {
                Utils.startAsyncTask(new StartDraftMonthlyFormTask(this, date, formName), null);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

    }

    @Override
    protected void sendReport(final Date month) {
        if (month != null) {
            FragmentTransaction ft = getFragmentManager()
                    .beginTransaction();
            android.app.Fragment prev = getFragmentManager()
                    .findFragmentByTag("SendMonthlyDraftDialogFragment");
            if (prev != null) {
                ft.remove(prev);
            }

            String monthString = new SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(month);
            // Create and show the dialog.
            SendMonthlyDraftDialogFragment newFragment = SendMonthlyDraftDialogFragment
                    .newInstance(monthString,
                            new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Calendar.getInstance().getTime()),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String[] params = new String[]{new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(month)};
                                    Utils.startAsyncTask(new HfGenerateAndSendMonthlyTask(), params);
                                }
                            });
            ft.add(newFragment, "SendMonthlyDraftDialogFragment");
            ft.commitAllowingStateLoss();
        }
    }


    protected class HfGenerateAndSendMonthlyTask extends GenerateAndSendMonthlyTask {

        @Override
        protected void onPostExecute(Boolean res) {
            super.onPostExecute(res);
            hideProgressDialog();
            // update drafts view
            refreshDraftMonthlyTitle();
            Utils.startAsyncTask(new FetchEditedMonthlyTalliesTask(new FetchEditedMonthlyTalliesTask.TaskListener() {
                @Override
                public void onPostExecute(List<MonthlyTally> monthlyTallies) {
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + org.smartregister.chw.core.R.id.container + ":" + mViewPager.getCurrentItem());
                    ((HfDraftMonthlyFragment) fragment).updateDraftsReportListView(monthlyTallies);
                }
            }), null);
        }

        @Override
        protected void pushReportsToServer() {
            final String REPORTS_SYNC_PATH = "/rest/report/add";
            final Context context = CoreChwApplication.getInstance().getContext().applicationContext();
            HTTPAgent httpAgent = CoreChwApplication.getInstance().getContext().getHttpAgent();
            Hia2ReportRepository hia2ReportRepository = CoreChwApplication.getInstance().hia2ReportRepository();
            try {
                boolean keepSyncing = true;
                int limit = 50;
                while (keepSyncing) {
                    List<JSONObject> pendingReports = hia2ReportRepository.getUnSyncedReports(limit);

                    if (pendingReports.isEmpty()) {
                        return;
                    }

                    String baseUrl = CoreChwApplication.getInstance().getContext().configuration().dristhiBaseURL();
                    if (baseUrl.endsWith(context.getString(org.smartregister.chw.core.R.string.url_separator))) {
                        baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(context.getString(org.smartregister.chw.core.R.string.url_separator)));
                    }
                    // create request body
                    JSONObject request = new JSONObject();

                    request.put("reports", pendingReports);
                    String jsonPayload = request.toString();
                    Response<String> response = httpAgent.post(
                            MessageFormat.format("{0}/{1}",
                                    baseUrl,
                                    REPORTS_SYNC_PATH),
                            jsonPayload);
                    if (response.isFailure()) {
                        Log.e(getClass().getName(), "Reports sync failed.");
                        return;
                    }
                    hia2ReportRepository.markReportsAsSynced(pendingReports);
                    Log.i(getClass().getName(), "Reports synced successfully.");

                    // update drafts view
                    refreshDraftMonthlyTitle();
                    Utils.startAsyncTask(new FetchEditedMonthlyTalliesTask(new FetchEditedMonthlyTalliesTask.TaskListener() {
                        @Override
                        public void onPostExecute(List<MonthlyTally> monthlyTallies) {
                            Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + org.smartregister.chw.core.R.id.container + ":" + mViewPager.getCurrentItem());
                            ((HfDraftMonthlyFragment) fragment).updateDraftsReportListView(monthlyTallies);
                        }
                    }), null);
                }
            } catch (Exception e) {
                Log.e(getClass().getName(), e.getMessage());
            }
        }
    }
}
