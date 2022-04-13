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
        indicatorCodes.add("pnc-1b-10-14");
        indicatorCodes.add("pnc-1b-15-19");
        indicatorCodes.add("pnc-1b-20-24");
        indicatorCodes.add("pnc-1b-25-29");
        indicatorCodes.add("pnc-1b-30-34");
        indicatorCodes.add("pnc-1b-35+");
        indicatorCodes.add("pnc-1b-35+");
        indicatorCodes.add("pnc-2a-10-14");
        indicatorCodes.add("pnc-2a-15-19");
        indicatorCodes.add("pnc-2a-20-24");
        indicatorCodes.add("pnc-2a-25-29");
        indicatorCodes.add("pnc-2a-30-34");
        indicatorCodes.add("pnc-2a-35+");
        indicatorCodes.add("pnc-3-10-14");
        indicatorCodes.add("pnc-3-15-19");
        indicatorCodes.add("pnc-3-20-24");
        indicatorCodes.add("pnc-3-25-29");
        indicatorCodes.add("pnc-3-30-34");
        indicatorCodes.add("pnc-3-35+");
        indicatorCodes.add("pnc-4-10-14");
        indicatorCodes.add("pnc-4-15-19");
        indicatorCodes.add("pnc-4-20-24");
        indicatorCodes.add("pnc-4-25-29");
        indicatorCodes.add("pnc-4-30-34");
        indicatorCodes.add("pnc-4-35+");
        indicatorCodes.add("pnc-5-10-14");
        indicatorCodes.add("pnc-5-15-19");
        indicatorCodes.add("pnc-5-20-24");
        indicatorCodes.add("pnc-5-25-29");
        indicatorCodes.add("pnc-5-30-34");
        indicatorCodes.add("pnc-5-35+");
        indicatorCodes.add("pnc-6-10-14");
        indicatorCodes.add("pnc-6-15-19");
        indicatorCodes.add("pnc-6-20-24");
        indicatorCodes.add("pnc-6-25-29");
        indicatorCodes.add("pnc-6-30-34");
        indicatorCodes.add("pnc-6-35+");
        indicatorCodes.add("pnc-7-10-14");
        indicatorCodes.add("pnc-7-15-19");
        indicatorCodes.add("pnc-7-20-24");
        indicatorCodes.add("pnc-7-25-29");
        indicatorCodes.add("pnc-7-30-34");
        indicatorCodes.add("pnc-7-35+");
        indicatorCodes.add("pnc-8a-10-14");
        indicatorCodes.add("pnc-8a-15-19");
        indicatorCodes.add("pnc-8a-20-24");
        indicatorCodes.add("pnc-8a-25-29");
        indicatorCodes.add("pnc-8a-30-34");
        indicatorCodes.add("pnc-8a-35+");
        indicatorCodes.add("pnc-8b-10-14");
        indicatorCodes.add("pnc-8b-15-19");
        indicatorCodes.add("pnc-8b-20-24");
        indicatorCodes.add("pnc-8b-25-29");
        indicatorCodes.add("pnc-8b-30-34");
        indicatorCodes.add("pnc-8b-35+");
        indicatorCodes.add("pnc-8c-10-14");
        indicatorCodes.add("pnc-8c-15-19");
        indicatorCodes.add("pnc-8c-20-24");
        indicatorCodes.add("pnc-8c-25-29");
        indicatorCodes.add("pnc-8c-30-34");
        indicatorCodes.add("pnc-8c-35+");
        indicatorCodes.add("pnc-9a-10-14");
        indicatorCodes.add("pnc-9a-15-19");
        indicatorCodes.add("pnc-9a-20-24");
        indicatorCodes.add("pnc-9a-25-29");
        indicatorCodes.add("pnc-9a-30-34");
        indicatorCodes.add("pnc-9a-35+");
        indicatorCodes.add("pnc-9b-10-14");
        indicatorCodes.add("pnc-9b-15-19");
        indicatorCodes.add("pnc-9b-20-24");
        indicatorCodes.add("pnc-9b-25-29");
        indicatorCodes.add("pnc-9b-30-34");
        indicatorCodes.add("pnc-9b-35+");
        indicatorCodes.add("pnc-9c-10-14");
        indicatorCodes.add("pnc-9c-15-19");
        indicatorCodes.add("pnc-9c-20-24");
        indicatorCodes.add("pnc-9c-25-29");
        indicatorCodes.add("pnc-9c-30-34");
        indicatorCodes.add("pnc-9c-35+");
        indicatorCodes.add("pnc-9d1-10-14");
        indicatorCodes.add("pnc-9d1-15-19");
        indicatorCodes.add("pnc-9d1-20-24");
        indicatorCodes.add("pnc-9d1-25-29");
        indicatorCodes.add("pnc-9d1-30-34");
        indicatorCodes.add("pnc-9d1-35+");
        indicatorCodes.add("pnc-9d2-10-14");
        indicatorCodes.add("pnc-9d2-15-19");
        indicatorCodes.add("pnc-9d2-20-24");
        indicatorCodes.add("pnc-9d2-25-29");
        indicatorCodes.add("pnc-9d2-30-34");
        indicatorCodes.add("pnc-9d2-35+");
        indicatorCodes.add("pnc-9e-10-14");
        indicatorCodes.add("pnc-9e-15-19");
        indicatorCodes.add("pnc-9e-20-24");
        indicatorCodes.add("pnc-9e-25-29");
        indicatorCodes.add("pnc-9e-30-34");
        indicatorCodes.add("pnc-9e-35+");
        indicatorCodes.add("pnc-9f-10-14");
        indicatorCodes.add("pnc-9f-15-19");
        indicatorCodes.add("pnc-9f-20-24");
        indicatorCodes.add("pnc-9f-25-29");
        indicatorCodes.add("pnc-9f-30-34");
        indicatorCodes.add("pnc-9f-35+");
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
