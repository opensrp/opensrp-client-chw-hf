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
    private final String[] indicatorCodesArray = new String[]{"2a", "2b", "2c", "2d", "2e", "3a", "4a", "4b", "4c", "4d", "5a", "5b", "5c", "5d", "5e", "5f", "5g", "5h", "5i", "6a", "6b", "6c", "6d", "6e", "6f", "6g", "6h", "7a", "7b", "7c", "7d", "7e", "7f", "8a", "8b", "8c", "8d", "8e", "9a", "9b", "9c", "9d", "9e", "9f", "9g", "10a", "10b", "10c", "10d", "10e", "10f", "10g", "10h", "11a", "11b", "11c", "11d", "11e", "11f", "11g", "11h", "12a", "12b", "12c", "13a", "13b", "13c", "14a", "15a", "15b", "15c", "15d", "15e"};
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
        indicatorCodesWithAgeGroups.add("16a-10-14-ME");
        indicatorCodesWithAgeGroups.add("16a-10-14-KE");
        indicatorCodesWithAgeGroups.add("16a-15-19-ME");
        indicatorCodesWithAgeGroups.add("16a-15-19-KE");
        indicatorCodesWithAgeGroups.add("16a-20-24-ME");
        indicatorCodesWithAgeGroups.add("16a-20-24-KE");
        indicatorCodesWithAgeGroups.add("16a-25-29-ME");
        indicatorCodesWithAgeGroups.add("16a-25-29-KE");
        indicatorCodesWithAgeGroups.add("16a-30-34-ME");
        indicatorCodesWithAgeGroups.add("16a-30-34-KE");
        indicatorCodesWithAgeGroups.add("16a-35+-ME");
        indicatorCodesWithAgeGroups.add("16a-35+-KE");
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (String indicatorCode : indicatorCodesWithAgeGroups) {
            jsonObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode("ld-" + indicatorCode, reportDate));
        }
        for (String indicator : indicatorCodesArray) {
            jsonObject.put(indicator + "-jumla", getIndicatorTotal(indicator));
        }

        jsonObject.put("2a+2b+2c+2d-10-14", getIndicatorTotal("2a-10-14") + getIndicatorTotal("2b-10-14") + getIndicatorTotal("2c-10-14"));
        jsonObject.put("2a+2b+2c+2d-15-19", getIndicatorTotal("2a-15-19") + getIndicatorTotal("2b-15-19") + getIndicatorTotal("2c-15-19"));
        jsonObject.put("2a+2b+2c+2d-20-24", getIndicatorTotal("2a-20-24") + getIndicatorTotal("2b-20-24") + getIndicatorTotal("2c-20-24"));
        jsonObject.put("2a+2b+2c+2d-25-29", getIndicatorTotal("2a-25-29") + getIndicatorTotal("2b-25-29") + getIndicatorTotal("2c-25-29"));
        jsonObject.put("2a+2b+2c+2d-30-34", getIndicatorTotal("2a-30-34") + getIndicatorTotal("2b-30-34") + getIndicatorTotal("2c-30-34"));
        jsonObject.put("2a+2b+2c+2d-35+", getIndicatorTotal("2a-35+") + getIndicatorTotal("2b-35+") + getIndicatorTotal("2c-35+"));
        jsonObject.put("2a+2b+2c+2d-jumla", getIndicatorTotal("2a") + getIndicatorTotal("2b") + getIndicatorTotal("2b"));

        jsonObject.put("4a+4b+4c+4d-10-14", getIndicatorTotal("4a-10-14") + getIndicatorTotal("4b-10-14") + getIndicatorTotal("4c-10-14") + getIndicatorTotal("4d-10-14"));
        jsonObject.put("4a+4b+4c+4d-15-19", getIndicatorTotal("4a-15-19") + getIndicatorTotal("4b-15-19") + getIndicatorTotal("4c-15-19") + getIndicatorTotal("4d-15-19"));
        jsonObject.put("4a+4b+4c+4d-20-24", getIndicatorTotal("4a-20-24") + getIndicatorTotal("4b-20-24") + getIndicatorTotal("4c-20-24") + getIndicatorTotal("4d-20-24"));
        jsonObject.put("4a+4b+4c+4d-25-29", getIndicatorTotal("4a-25-29") + getIndicatorTotal("4b-25-29") + getIndicatorTotal("4c-25-29") + getIndicatorTotal("4d-25-29"));
        jsonObject.put("4a+4b+4c+4d-30-34", getIndicatorTotal("4a-30-34") + getIndicatorTotal("4b-30-34") + getIndicatorTotal("4c-30-34") + getIndicatorTotal("4d-30-34"));
        jsonObject.put("4a+4b+4c+4d-35+", getIndicatorTotal("4a-35+") + getIndicatorTotal("4b-35+") + getIndicatorTotal("4c-35+") + getIndicatorTotal("4d-35+"));
        jsonObject.put("4a+4b+4c+4d-jumla", getIndicatorTotal("4a") + getIndicatorTotal("4b") + getIndicatorTotal("4c") + getIndicatorTotal("4d"));

        jsonObject.put("5a+5b+5c+5d+5e+5f+5g+5h-10-14", getIndicatorTotal("5a-10-14") + getIndicatorTotal("5b-10-14") + getIndicatorTotal("5c-10-14") + getIndicatorTotal("5d-10-14") + getIndicatorTotal("5e-10-14") + getIndicatorTotal("5f-10-14") + getIndicatorTotal("5g-10-14") + getIndicatorTotal("5h-10-14"));
        jsonObject.put("5a+5b+5c+5d+5e+5f+5g+5h-15-19", getIndicatorTotal("5a-15-19") + getIndicatorTotal("5b-15-19") + getIndicatorTotal("5c-15-19") + getIndicatorTotal("5d-15-19") + getIndicatorTotal("5e-15-19") + getIndicatorTotal("5f-15-19") + getIndicatorTotal("5g-15-19") + getIndicatorTotal("5h-15-19"));
        jsonObject.put("5a+5b+5c+5d+5e+5f+5g+5h-20-24", getIndicatorTotal("5a-20-24") + getIndicatorTotal("5b-20-24") + getIndicatorTotal("5c-20-24") + getIndicatorTotal("5d-20-24") + getIndicatorTotal("5e-20-24") + getIndicatorTotal("5f-20-24") + getIndicatorTotal("5g-20-24") + getIndicatorTotal("5h-20-24"));
        jsonObject.put("5a+5b+5c+5d+5e+5f+5g+5h-25-29", getIndicatorTotal("5a-25-29") + getIndicatorTotal("5b-25-29") + getIndicatorTotal("5c-25-29") + getIndicatorTotal("5d-25-29") + getIndicatorTotal("5e-25-29") + getIndicatorTotal("5f-25-29") + getIndicatorTotal("5g-25-29") + getIndicatorTotal("5h-25-29"));
        jsonObject.put("5a+5b+5c+5d+5e+5f+5g+5h-30-34", getIndicatorTotal("5a-30-34") + getIndicatorTotal("5b-30-34") + getIndicatorTotal("5c-30-34") + getIndicatorTotal("5d-30-34") + getIndicatorTotal("5e-30-34") + getIndicatorTotal("5f-30-34") + getIndicatorTotal("5g-30-34") + getIndicatorTotal("5h-30-34"));
        jsonObject.put("5a+5b+5c+5d+5e+5f+5g+5h-35+", getIndicatorTotal("5a-35+") + getIndicatorTotal("5b-35+") + getIndicatorTotal("5c-35+") + getIndicatorTotal("5d-35+") + getIndicatorTotal("5e-35+") + getIndicatorTotal("5f-35+") + getIndicatorTotal("5g-35+") + getIndicatorTotal("5h-35+"));
        jsonObject.put("5a+5b+5c+5d+5e+5f+5g+5h-jumla", getIndicatorTotal("5a") + getIndicatorTotal("5b") + getIndicatorTotal("5c") + getIndicatorTotal("5d") + getIndicatorTotal("5e") + getIndicatorTotal("5f") + getIndicatorTotal("5g") + getIndicatorTotal("5h"));


        jsonObject.put("6a+6b+6c+6d+6e+6f+6g+6h-10-14", getIndicatorTotal("6a-10-14") + getIndicatorTotal("6b-10-14") + getIndicatorTotal("6c-10-14") + getIndicatorTotal("6d-10-14") + getIndicatorTotal("6e-10-14") + getIndicatorTotal("6f-10-14") + getIndicatorTotal("6g-10-14") + getIndicatorTotal("6h-10-14"));
        jsonObject.put("6a+6b+6c+6d+6e+6f+6g+6h-15-19", getIndicatorTotal("6a-15-19") + getIndicatorTotal("6b-15-19") + getIndicatorTotal("6c-15-19") + getIndicatorTotal("6d-15-19") + getIndicatorTotal("6e-15-19") + getIndicatorTotal("6f-15-19") + getIndicatorTotal("6g-15-19") + getIndicatorTotal("6h-15-19"));
        jsonObject.put("6a+6b+6c+6d+6e+6f+6g+6h-20-24", getIndicatorTotal("6a-20-24") + getIndicatorTotal("6b-20-24") + getIndicatorTotal("6c-20-24") + getIndicatorTotal("6d-20-24") + getIndicatorTotal("6e-20-24") + getIndicatorTotal("6f-20-24") + getIndicatorTotal("6g-20-24") + getIndicatorTotal("6h-20-24"));
        jsonObject.put("6a+6b+6c+6d+6e+6f+6g+6h-25-29", getIndicatorTotal("6a-25-29") + getIndicatorTotal("6b-25-29") + getIndicatorTotal("6c-25-29") + getIndicatorTotal("6d-25-29") + getIndicatorTotal("6e-25-29") + getIndicatorTotal("6f-25-29") + getIndicatorTotal("6g-25-29") + getIndicatorTotal("6h-25-29"));
        jsonObject.put("6a+6b+6c+6d+6e+6f+6g+6h-30-34", getIndicatorTotal("6a-30-34") + getIndicatorTotal("6b-30-34") + getIndicatorTotal("6c-30-34") + getIndicatorTotal("6d-30-34") + getIndicatorTotal("6e-30-34") + getIndicatorTotal("6f-30-34") + getIndicatorTotal("6g-30-34") + getIndicatorTotal("6h-30-34"));
        jsonObject.put("6a+6b+6c+6d+6e+6f+6g+6h-35+", getIndicatorTotal("6a-35+") + getIndicatorTotal("6b-35+") + getIndicatorTotal("6c-35+") + getIndicatorTotal("6d-35+") + getIndicatorTotal("6e-35+") + getIndicatorTotal("6f-35+") + getIndicatorTotal("6g-35+") + getIndicatorTotal("6h-35+"));
        jsonObject.put("6a+6b+6c+6d+6e+6f+6g+6h-jumla", getIndicatorTotal("6a") + getIndicatorTotal("6b") + getIndicatorTotal("6c") + getIndicatorTotal("6d") + getIndicatorTotal("6e") + getIndicatorTotal("6f") + getIndicatorTotal("6g") + getIndicatorTotal("6h"));

        jsonObject.put("9b+9d-10-14", getIndicatorTotal("9b-10-14") + getIndicatorTotal("9d-10-14"));
        jsonObject.put("9b+9d-15-19", getIndicatorTotal("9b-15-19") + getIndicatorTotal("9d-15-19"));
        jsonObject.put("9b+9d-20-24", getIndicatorTotal("9b-20-24") + getIndicatorTotal("9d-20-24"));
        jsonObject.put("9b+9d-25-29", getIndicatorTotal("9b-25-29") + getIndicatorTotal("9d-25-29"));
        jsonObject.put("9b+9d-30-34", getIndicatorTotal("9b-30-34") + getIndicatorTotal("9d-30-34"));
        jsonObject.put("9b+9d-35+", getIndicatorTotal("9b-35+") + getIndicatorTotal("9d-35+"));
        jsonObject.put("9b+9d-jumla", getIndicatorTotal("9b") + getIndicatorTotal("9d"));

        jsonObject.put("10a+10d+10e-10-14", getIndicatorTotal("10a-10-14") + getIndicatorTotal("10d-10-14") + getIndicatorTotal("10e-10-14"));
        jsonObject.put("10a+10d+10e-15-19", getIndicatorTotal("10a-15-19") + getIndicatorTotal("10d-15-19") + getIndicatorTotal("10e-15-19"));
        jsonObject.put("10a+10d+10e-20-24", getIndicatorTotal("10a-20-24") + getIndicatorTotal("10d-20-24") + getIndicatorTotal("10e-20-24"));
        jsonObject.put("10a+10d+10e-25-29", getIndicatorTotal("10a-25-29") + getIndicatorTotal("10d-25-29") + getIndicatorTotal("10e-25-29"));
        jsonObject.put("10a+10d+10e-30-34", getIndicatorTotal("10a-30-34") + getIndicatorTotal("10d-30-34") + getIndicatorTotal("10e-30-34"));
        jsonObject.put("10a+10d+10e-35+", getIndicatorTotal("10a-35+") + getIndicatorTotal("10d-35+") + getIndicatorTotal("10e-35+"));
        jsonObject.put("10a+10d+10e-jumla", getIndicatorTotal("10a") + getIndicatorTotal("10d") + getIndicatorTotal("10e"));

        jsonObject.put("11a+11d+11e-10-14", getIndicatorTotal("11a-10-14") + getIndicatorTotal("11d-10-14") + getIndicatorTotal("11e-10-14"));
        jsonObject.put("11a+11d+11e-15-19", getIndicatorTotal("11a-15-19") + getIndicatorTotal("11d-15-19") + getIndicatorTotal("11e-15-19"));
        jsonObject.put("11a+11d+11e-20-24", getIndicatorTotal("11a-20-24") + getIndicatorTotal("11d-20-24") + getIndicatorTotal("11e-20-24"));
        jsonObject.put("11a+11d+11e-25-29", getIndicatorTotal("11a-25-29") + getIndicatorTotal("11d-25-29") + getIndicatorTotal("11e-25-29"));
        jsonObject.put("11a+11d+11e-30-34", getIndicatorTotal("11a-30-34") + getIndicatorTotal("11d-30-34") + getIndicatorTotal("11e-30-34"));
        jsonObject.put("11a+11d+11e-35+", getIndicatorTotal("11a-35+") + getIndicatorTotal("11d-35+") + getIndicatorTotal("11e-35+"));
        jsonObject.put("11a+11d+11e-jumla", getIndicatorTotal("11a") + getIndicatorTotal("11d") + getIndicatorTotal("11e"));
        jsonObject.put("16a-jumla-ME", getIndicatorTotalSpecific("16a", "ME"));
        jsonObject.put("16a-jumla-KE", getIndicatorTotalSpecific("16a", "KE"));

        return jsonObject;
    }

    private int getIndicatorTotal(String indicator) {
        int total = 0;
        for (String indicatorCode : indicatorCodesWithAgeGroups) {
            if (indicatorCode.startsWith(indicator)) {
                total += ReportDao.getReportPerIndicatorCode("ld-" + indicatorCode, reportDate);
            }
        }
        return total;
    }

    public int getIndicatorTotalSpecific(String indicator, String postfix){
        int total = 0;
        for(String ageGroup : indicatorAgeGroups){
            total += ReportDao.getReportPerIndicatorCode("ld-" + indicator + "-" +ageGroup + "-" + postfix, reportDate);
        }
        return total;
    }
}
