package org.smartregister.chw.hf.activity;

import android.view.View;
import android.widget.TextView;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;

public class MotherChampionReportsActivity extends PncReportsActivity {


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.pnc_monthly_report) {
            MotherChampionReportsViewActivity.startMe(this, Constants.ReportConstants.ReportPaths.MOTHER_CHAMPION_REPORT_PATH, reportPeriod);
        }
    }

    @Override
    public void setUpToolbar() {
        super.setUpToolbar();
        TextView title = findViewById(R.id.toolbar_title);
        title.setText(R.string.mother_champion_reports);
    }
}
