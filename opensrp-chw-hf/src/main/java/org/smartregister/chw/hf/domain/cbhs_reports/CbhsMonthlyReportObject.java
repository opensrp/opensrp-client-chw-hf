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
    private final String[] indicatorCodesArray = new String[]{"cbhs-1a", "cbhs-1b", "cbhs-2a", "cbhs-2b-1", "cbhs-2b-2",
            "cbhs-2b-3", "cbhs-2b-4", "cbhs-2b-5", "cbhs-2b-6", "cbhs-2b-7", "cbhs-2b-8", "cbhs-2b-9", "cbhs-2b-10", "cbhs-2b-11", "cbhs-2c-1", "cbhs-2c-2",
            "cbhs-2c-3", "cbhs-2c-4", "cbhs-2d-1", "cbhs-2d-2", "cbhs-2d-3", "cbhs-2d-4", "cbhs-2d-5", "cbhs-2d-6", "cbhs-2d-7", "cbhs-2d-8", "cbhs-2d-9", "cbhs-2d-10", "cbhs-2d-11"};
    private final String[] indicatorGenderGroups = new String[]{"jumla-me", "jumla-ke"};
    private final String[] indicatorAgeGroups = new String[]
            {"1-me", "1-ke",
                    "1-5-me", "1-5-ke", "6-9-me", "6-9-ke", "10-14-me", "10-14-ke", "15-19-me", "15-19-ke",
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

        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorGenderGroups[0]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorGenderGroups[1]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[0]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[1]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[2]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[3]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[4]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[5]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[6]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[7]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[8]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[9]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[10]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[11]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[12]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[13]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[14]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[15]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[16]);
        indicatorCodesWithAgeGroups.add("cbhs-2a-" + indicatorAgeGroups[17]);

        indicatorCodesWithAgeGroups.add("cbhs-5-jumla");
        indicatorCodesWithAgeGroups.add("cbhs-5-deseased");
        indicatorCodesWithAgeGroups.add("cbhs-5-continue-with-clinic-from-elsewhere");
        indicatorCodesWithAgeGroups.add("cbhs-5-moved");
        indicatorCodesWithAgeGroups.add("cbhs-5-absconded");
        indicatorCodesWithAgeGroups.add("cbhs-5-completed_and_qualified_from_the_services");

        indicatorCodesWithAgeGroups.remove("cbhs--2b-3-jumla-ke");
        indicatorCodesWithAgeGroups.remove("cbhs-2b-4-jumla-me");

        indicatorCodesWithAgeGroups.remove("cbhs-2b-10-jumla-me");
        indicatorCodesWithAgeGroups.remove("cbhs-2b-11-jumla-me");
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (String indicatorCode : indicatorCodesWithAgeGroups) {
            jsonObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate));
        }

        jsonObject.put("cbhs-3-jumla-me", getIndicatorTotal("cbhs-1a-jumla-me") + getIndicatorTotal("cbhs-2a-jumla-me"));
        jsonObject.put("cbhs-3-jumla-ke", getIndicatorTotal("cbhs-1a-jumla-ke") + getIndicatorTotal("cbhs-2a-jumla-ke"));
        jsonObject.put("cbhs-4-jumla-me", getIndicatorTotal("cbhs-1b-jumla-me") + getIndicatorTotal("cbhs-2a-jumla-me"));
        jsonObject.put("cbhs-4-jumla-ke", getIndicatorTotal("cbhs-1b-jumla-ke") + getIndicatorTotal("cbhs-2a-jumla-ke"));

        return jsonObject;
    }

    private int getIndicatorTotal(String indicator) {
        return ReportDao.getReportPerIndicatorCode(indicator, reportDate);
    }
}
