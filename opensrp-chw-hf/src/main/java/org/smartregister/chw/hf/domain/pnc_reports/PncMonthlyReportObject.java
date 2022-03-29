package org.smartregister.chw.hf.domain.pnc_reports;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PncMonthlyReportObject extends ReportObject {
    private final Date reportDate;
    private final List<String> indicatorCodes = new ArrayList<>();
    public PncMonthlyReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
        setIndicatorCodes(indicatorCodes);
    }
    public void setIndicatorCodes(List<String> indicatorCodes) {
        indicatorCodes.add("pnc-1a-10-14");
        indicatorCodes.add("pnc-1a-15-19");
        indicatorCodes.add("pnc-1a-20-24");
        indicatorCodes.add("pnc-1a-25-29");
        indicatorCodes.add("pnc-1a-30-34");
        indicatorCodes.add("pnc-1a-35+");
    }
    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (String indicatorCode : indicatorCodes) {
            jsonObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode(indicatorCode,reportDate));
        }
        return jsonObject;
    }
}
