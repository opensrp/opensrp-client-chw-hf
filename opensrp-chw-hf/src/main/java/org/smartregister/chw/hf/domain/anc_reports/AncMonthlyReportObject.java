package org.smartregister.chw.hf.domain.anc_reports;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AncMonthlyReportObject extends ReportObject {
    private final Date reportDate;
    private final List<String> indicatorCodes = new ArrayList<>();
    public AncMonthlyReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
        setIndicatorCodes(indicatorCodes);
    }
    public void setIndicatorCodes(List<String> indicatorCodes) {
     indicatorCodes.add("2a-10-14");
     indicatorCodes.add("2a-15-19");
     indicatorCodes.add("2a-20-24");
     indicatorCodes.add("2a-25-29");
     indicatorCodes.add("2a-30-34");
     indicatorCodes.add("2a-35+");
     indicatorCodes.add("2b-10-14");
     indicatorCodes.add("2b-15-19");
     indicatorCodes.add("2b-20-24");
     indicatorCodes.add("2b-25-29");
     indicatorCodes.add("2b-30-34");
     indicatorCodes.add("2b-35+");
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
