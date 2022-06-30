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
    private final String[] indicatorCodesArrayForMother = new String[]{"pnc-1a", "pnc-1b", "pnc-2a", "pnc-3", "pnc-4", "pnc-5", "pnc-6", "pnc-7", "pnc-8a", "pnc-8b", "pnc-8c", "pnc-9a", "pnc-9b", "pnc-9c", "pnc-9d1", "pnc-9d2", "pnc-9e", "pnc-9f","pnc-9g", "pnc-10a", "pnc-10b", "pnc-10c", "pnc-10d", "pnc-10e"};
    private final String[] indicatorAgeGroupsArray = new String[]{"10-14", "15-19", "20-24", "25-29", "30-34", "35+"};
    private final String[] indicatorCodesArrayForChild = new String[]{"pnc-11a", "pnc-11b", "pnc-11c", "pnc-12a", "pnc-12b", "pnc-12c", "pnc-12d", "pnc-12e", "pnc-12f", "pnc-13a", "pnc-13b", "pnc-13c", "pnc-13d", "pnc-14", "pnc-15", "pnc-16a", "pnc-16b", "pnc-16c"};
    private final String[] indicatorSexCodeArray = new String[]{"ME", "KE"};

    public PncMonthlyReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
        setIndicatorCodes(indicatorCodes);
    }

    public void setIndicatorCodes(List<String> indicatorCodes) {
        addIndicatorsForMother(indicatorCodes);
        addIndicatorsForChild(indicatorCodes);
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONObject indicatorObject = new JSONObject();
        for (String indicatorCode : indicatorCodes) {
            indicatorObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate));
        }
        //for mother
        for (String indicatorCode : indicatorCodesArrayForMother) {
            indicatorObject.put(indicatorCode + "-jumla", getIndicatorTotal(indicatorCode));
        }
        //for child
        for (String indicatorCode : indicatorCodesArrayForChild) {
            indicatorObject.put(indicatorCode + "-jumla", getIndicatorTotal(indicatorCode));
        }
        indicatorObject.put("pnc-1a+1b-10-14", getIndicatorTotal("pnc-1a-10-14") + getIndicatorTotal("pnc-1b-10-14"));
        indicatorObject.put("pnc-1a+1b-15-19", getIndicatorTotal("pnc-1a-15-19") + getIndicatorTotal("pnc-1b-15-19"));
        indicatorObject.put("pnc-1a+1b-20-24", getIndicatorTotal("pnc-1a-20-24") + getIndicatorTotal("pnc-1b-20-24"));
        indicatorObject.put("pnc-1a+1b-25-29", getIndicatorTotal("pnc-1a-25-29") + getIndicatorTotal("pnc-1b-25-29"));
        indicatorObject.put("pnc-1a+1b-30-34", getIndicatorTotal("pnc-1a-30-34") + getIndicatorTotal("pnc-1b-30-34"));
        indicatorObject.put("pnc-1a+1b-35+", getIndicatorTotal("pnc-1a-35+") + getIndicatorTotal("pnc-1b-35+"));
        indicatorObject.put("pnc-1a+1b-jumla", getIndicatorTotal("pnc-1a") + getIndicatorTotal("pnc-1b"));
        indicatorObject.put("pnc-11a+11b-ME", getIndicatorTotal("pnc-11a-ME") + getIndicatorTotal("pnc-11b-ME"));
        indicatorObject.put("pnc-11a+11b-KE", getIndicatorTotal("pnc-11a-KE") + getIndicatorTotal("pnc-11b-KE"));
        indicatorObject.put("pnc-11a+11b-jumla", getIndicatorTotal("pnc-11a") + getIndicatorTotal("pnc-11b"));
        return indicatorObject;
    }

    private void addIndicatorsForMother(List<String> indicatorCodes) {
        for (String indicatorCode : indicatorCodesArrayForMother) {
            for (String indicatorAgeGroup : indicatorAgeGroupsArray) {
                indicatorCodes.add(indicatorCode + "-" + indicatorAgeGroup);
            }
        }
    }

    private void addIndicatorsForChild(List<String> indicatorCodes) {
        for (String indicatorCode : indicatorCodesArrayForChild) {
            for (String indicatorSexCode : indicatorSexCodeArray) {
                indicatorCodes.add(indicatorCode + "-" + indicatorSexCode);
            }
        }
    }

    private int getIndicatorTotal(String indicator) {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith(indicator)) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

}
