package org.smartregister.chw.hf.domain;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FpMonthlyReportObject extends ReportObject {

    private final List<String> indicatorCodesWithAgeGroups = new ArrayList<>();

    private final String[] indicatorCodes = new String[]{
            "fp-1","fp-1a", "fp-1b", "fp-1c",
            "fp-2a", "fp-2b", "fp-2c", "fp-2d",
            "fp-3a", "fp-3b",
            "fp-4a", "fp-4b", "fp-4c", "fp-4d", "fp-4e", "fp-4f",
            "fp-5a", "fp-5b", "fp-5c", "fp-5d", "fp-5e", "fp-5f",
            "fp-6a", "fp-6b", "fp-6c", "fp-6d", "fp-6e", "fp-6f",
            "fp-7a", "fp-7b", "fp-7c", "fp-7d", "fp-7e", "fp-7f", "fp-7g", "fp-7h", "fp-7i", "fp-7j", "fp-7k", "fp-7l",
            "fp-8a", "fp-8b", "fp-8c",
            "fp-9a", "fp-9b",
            "fp-10a", "fp-10b",
            "fp-11a", "fp-11b", "fp-11c", "fp-11d", "fp-11e",
            "fp-12a", "fp-12b", "fp-12c", "fp-12d", "fp-12e", "fp-12f", "fp-12g", "fp-12h", "fp-12i",
            "fp-13a", "fp-13b", "fp-13c", "fp-13d", "fp-13e", "fp-13f"
    };

    private final String[] clientType = new String[]{"nc", "rc"};

    private final String[] indicatorAgeGroups = new String[]{"10-14", "15-19", "20-24", "25-29", "30-34", "35+"};

    private final Date reportDate;

    public FpMonthlyReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
        setIndicatorCodesWithAgeGroups(indicatorCodesWithAgeGroups);
    }

    public static int calculateSbcSpecificTotal(HashMap<String, Integer> indicators, String specificKey) {
        int total = 0;

        for (Map.Entry<String, Integer> entry : indicators.entrySet()) {
            String key = entry.getKey().toLowerCase();
            Integer value = entry.getValue();

            if (key.startsWith(specificKey.toLowerCase())) {
                total += value;
            }
        }

        return total;
    }

    public void setIndicatorCodesWithAgeGroups(List<String> indicatorCodesWithAgeGroups) {
        for (String indicatorCode : indicatorCodes) {
            for (String clientType : clientType) {
                for (String indicatorKey : indicatorAgeGroups) {
                    indicatorCodesWithAgeGroups.add(indicatorCode + "-" + clientType + "-" + indicatorKey);
                }
            }
        }

    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        HashMap<String, Integer> indicatorsValues = new HashMap<>();
        JSONObject indicatorDataObject = new JSONObject();
        for (String indicatorCode : indicatorCodesWithAgeGroups) {
            int value = ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            indicatorsValues.put(indicatorCode, value);
            indicatorDataObject.put(indicatorCode, value);
        }

        // Calculate and add total values for "totals"
        for (String indicatorCode : indicatorCodes) {
            int newClientsTotal = calculateSbcSpecificTotal(indicatorsValues, indicatorCode + "-nc");
            int revisitClientsTotal = calculateSbcSpecificTotal(indicatorsValues, indicatorCode + "-rc");
            indicatorDataObject.put(indicatorCode + "-nc-total", newClientsTotal);
            indicatorDataObject.put(indicatorCode + "-rc-total", revisitClientsTotal);
            indicatorDataObject.put(indicatorCode + "-grand-total", newClientsTotal + revisitClientsTotal);
        }

        return indicatorDataObject;
    }

}
