package org.smartregister.chw.hf.domain.ld_reports;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LdMonthlyReportObject extends ReportObject {
    private final Date reportDate;
    private final List<String> indicatorCodesWithAgeGroups = new ArrayList<>();
    private final String[] indicatorCodesArray = new String[]{"2a", "2b", "2c", "2d", "2e", "3a", "4a", "4b", "4c", "4d", "5a", "5b", "5c", "5d", "5e", "5f", "5g", "5h", "5i","6a", "6b", "6c", "6d", "6e", "6f", "6g", "6h", "7a","7b","7c","7d","7e","7f", "8a","8b","8c","8d","8e", "9a","9b","9c","9d","9e","9f","9g", "10a","10b","10c","10d","10e","10f","10g","10h","11a","11b","11c","11d","11e","11f","11g","11h", "12a", "12b", "12c", "13a", "14a", "15a", "15b", "15c", "15d", "15e", "16a"};
    private final String[] indicatorAgeGroups = new String[]{"10-14", "15-19", "20-24", "25-29", "30-34", "35+"};


    public LdMonthlyReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
        setIndicatorCodesWithAgeGroups(indicatorCodesWithAgeGroups);
    }


    public void setIndicatorCodesWithAgeGroups(List<String> indicatorCodesWithAgeGroups) {
        for (String indicatorCode : indicatorCodesArray) {
            for (String indicatorKey : indicatorAgeGroups) {
                indicatorCodesWithAgeGroups.add(indicatorCode + "-" + indicatorKey);
            }
        }
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (String indicatorCode : indicatorCodesWithAgeGroups) {
            jsonObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode("ld-"+indicatorCode, reportDate));
        }
        for (String indicator : indicatorCodesArray) {
            jsonObject.put(indicator + "-jumla", getIndicatorTotal(indicator));
        }

        return jsonObject;
    }

    private int getIndicatorTotal(String indicator) {
        int total = 0;
        for (String indicatorCode : indicatorCodesWithAgeGroups) {
            if (indicatorCode.startsWith(indicator)) {
                total += ReportDao.getReportPerIndicatorCode("ld-"+indicatorCode, reportDate);
            }
        }
        return total;
    }
}
