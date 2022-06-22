package org.smartregister.chw.hf.activity;

import android.view.View;
import android.widget.TextView;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.ReportUtils;

public class LdReportsActivity extends PncReportsActivity {
    private String reportPeriod = ReportUtils.getDefaultReportPeriod();

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.pnc_monthly_report) {
            LdReportsViewActivity.startMe(this, Constants.ReportConstants.ReportPaths.LD_REPORT_PATH, reportPeriod);
        }
    }

    @Override
    public void setUpToolbar() {
        super.setUpToolbar();
        TextView title = findViewById(R.id.toolbar_title);
        title.setText(R.string.ld_report_title);
    }
}
