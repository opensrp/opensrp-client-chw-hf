package org.smartregister.chw.hf.domain.cbhs_reports;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CbhsMonthlyReportObject extends ReportObject {
    private Date reportDate;
    private final List<String> indicatorCodesWithAgeGroups = new ArrayList<>();
    private final String[] indicatorCodesArray = new String[]{"1a","1b","2a", "2b-1","2b-2",
            "2b-3", "2b-4", "2b-5","2b-6","2b-7", "2b-8","2b-9","2b-10","2b-11","2c-1","2c-2",
            "2c-3","2c-4", "2d-1","2d-2","2d-3", "2d-4", "2d-5", "2d-6", "2d-7", "2d-8", "2d-9", "2d-10", "2d-11"};
    private final String[] indicatorGenderGroups = new String[]{"jumla-me", "jumla-ke"};
    private final String[] indicatorAgeGroups = new String[]
            {"1-me", "1-ke",
            "1-5-me", "1-5-ke","6-9-me", "6-9-ke", "10-14-me", "10-14-ke", "15-19-me", "15-19-ke",
            "20-24-me", "20-24-ke", "25-49-me", "25-49-ke", "50-59-me", "50-59-ke", "60-me", "60-ke"};

    public CbhsMonthlyReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
        setIndicatorCodesWithAgeGroups(indicatorCodesWithAgeGroups);
    }

    public void setIndicatorCodesWithAgeGroups(List<String> indicatorCodesWithAgeGroups) {
        for (String indicatorCode : indicatorCodesArray) {
            for (String indicatorKey : indicatorGenderGroups) {
                indicatorCodesWithAgeGroups.add(indicatorCode + "-" + indicatorKey);
            }
        }

        indicatorCodesWithAgeGroups.add("2a-" + indicatorGenderGroups[0]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorGenderGroups[1]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[0]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[1]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[2]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[3]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[4]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[5]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[6]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[7]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[8]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[9]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[10]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[11]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[12]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[13]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[14]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[15]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[16]);
        indicatorCodesWithAgeGroups.add("2a-" + indicatorAgeGroups[17]);

        indicatorCodesWithAgeGroups.remove("2b-3-jumla-ke");
        indicatorCodesWithAgeGroups.remove("2b-4-jumla-me");

        indicatorCodesWithAgeGroups.remove("2b-10-jumla-me");
        indicatorCodesWithAgeGroups.remove("2b-11-jumla-me");
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (String indicatorCode : indicatorCodesWithAgeGroups) {
            jsonObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate));
        }

        jsonObject.put("3-jumla-me", getIndicatorTotal("1a-jumla-me") + getIndicatorTotal("2a-jumla-me"));
        jsonObject.put("3-jumla-ke", getIndicatorTotal("1a-jumla-ke") + getIndicatorTotal("2a-jumla-ke"));
        jsonObject.put("4-jumla-me", getIndicatorTotal("1b-jumla-me") + getIndicatorTotal("2a-jumla-me"));
        jsonObject.put("4-jumla-ke", getIndicatorTotal("1b-jumla-ke") + getIndicatorTotal("2a-jumla-ke"));

        return jsonObject;
    }

    private int getIndicatorTotal(String indicator) {
        int total = 0;
        for (String indicatorCode : indicatorCodesWithAgeGroups) {
            if (indicatorCode.startsWith(indicator)) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }
}
