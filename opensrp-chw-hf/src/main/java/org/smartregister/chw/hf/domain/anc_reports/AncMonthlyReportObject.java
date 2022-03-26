package org.smartregister.chw.hf.domain.anc_reports;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.domain.ReportObject;

import java.util.Date;

public class AncMonthlyReportObject extends ReportObject {
    private final Date reportDate;
    public AncMonthlyReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        return super.getIndicatorData();
    }
}
