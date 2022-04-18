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
    private final String[] indicatorCodesArray = new String[]{"2a", "2b", "2c", "2d", "2e", "3", "4a", "4b", "4c", "4d", "4e", "4f", "4g", "4h", "4i", "4j", "4k", "4l", "4m", "4n", "4o", "4p", "4q", "4r", "5a", "5b", "5c", "5d", "5e", "5f", "5g", "5h", "5i", "5j", "5k", "5l", "5m", "5n", "5o", "5p", "6a", "6b", "6c", "6d", "6e", "6f", "7", "8", "9", "10", "11"};
    private final String[] indicatorKeysArray = new String[]{"10-14", "15-19", "20-24", "25-29", "30-34", "35+"};

    public AncMonthlyReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
        setIndicatorCodes(indicatorCodes);
    }

    public void setIndicatorCodes(List<String> indicatorCodes) {
        for (String indicatorCode : indicatorCodesArray) {
            for (String indicatorKey : indicatorKeysArray) {
                indicatorCodes.add(indicatorCode + "-" + indicatorKey);
            }
        }
        indicatorCodes.remove("4a-" + indicatorKeysArray[0]);
        indicatorCodes.remove("4b-" + indicatorKeysArray[2]);
        indicatorCodes.remove("4b-" + indicatorKeysArray[3]);
        indicatorCodes.remove("4b-" + indicatorKeysArray[4]);
        indicatorCodes.remove("4b-" + indicatorKeysArray[5]);
        indicatorCodes.remove("4c-" + indicatorKeysArray[0]);
        indicatorCodes.remove("4c-" + indicatorKeysArray[1]);
        indicatorCodes.remove("4c-" + indicatorKeysArray[2]);
        indicatorCodes.remove("4c-" + indicatorKeysArray[3]);
        indicatorCodes.remove("4c-" + indicatorKeysArray[4]);
        indicatorCodes.remove("5e-" + indicatorKeysArray[3]);
        indicatorCodes.remove("5e-" + indicatorKeysArray[4]);
        indicatorCodes.remove("5e-" + indicatorKeysArray[5]);
        indicatorCodes.remove("5p-" + indicatorKeysArray[3]);
        indicatorCodes.remove("5p-" + indicatorKeysArray[4]);
        indicatorCodes.remove("5p-" + indicatorKeysArray[5]);
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (String indicatorCode : indicatorCodes) {
            jsonObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate));
        }
        return jsonObject;
    }
}
