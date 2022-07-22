package org.smartregister.chw.hf.domain.ltfu_summary;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LTFUSummaryObject extends ReportObject {
    private Date reportDate;
    private final List<String> indicatorCodesWithGroups = new ArrayList<>();
    private final String[] indicatorCodesArray = new String[]{"ltfu-less-2-me", "ltfu-less-2-ke",
            "ltfu-2-14-me", "ltfu-2-14-ke", "ltfu-15-24-me", "ltfu-15-24-ke",
            "ltfu-25-49-me", "ltfu-25-49-ke", "ltfu-50-me", "ltfu-50-ke"};
    private final String[] indicatorGroups = new String[]
            {"ctc", "pmtct", "tb", "wajidunga", "arv-yes", "arv-no",
                    "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                    "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2", "i2", "j2", "k2"};

    public LTFUSummaryObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
        setIndicatorCodesWithAgeGroups(indicatorCodesWithGroups);
    }

    public void setIndicatorCodesWithAgeGroups(List<String> indicatorCodesWithAgeGroups) {
        for (String indicatorCode : indicatorCodesArray) {
            for (String indicatorKey : indicatorGroups) {
                indicatorCodesWithAgeGroups.add(indicatorCode + "-" + indicatorKey);
            }
        }
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        //vertical totals
        int jumlaWateja = 0;
        int jumlaWaliopatikana =0;

        for (String indicatorCode : indicatorCodesWithGroups) {
            jsonObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate));
        }

        for (String indicatorGroup : indicatorGroups) {
            jsonObject.put("ltfu-jumla" + "-" + indicatorGroup, getTotalPerGroup(indicatorGroup));
        }
        for(String indicatorCode: indicatorCodesArray){
            jumlaWateja = getJumlaWateja(jsonObject, jumlaWateja, indicatorCode);
            jumlaWaliopatikana = getJumlaWaliopatikana(jsonObject, jumlaWaliopatikana, indicatorCode);
        }

        jsonObject.put("ltfu-jumla-jumla-wateja", jumlaWateja);
        jsonObject.put("ltfu-jumla-jumla-waliopatikana", jumlaWaliopatikana);

        return jsonObject;
    }

    private int getJumlaWateja(JSONObject jsonObject, int jumlaWateja, String indicatorCode) throws JSONException {
        //gets the vertical total and adds the horizontal total to the jsonObject
        int totalPerClinicForIndicator = getTotalForClinicPerGroup(indicatorCode);
        jumlaWateja += totalPerClinicForIndicator;
        jsonObject.put(indicatorCode + "-jumla-wateja", totalPerClinicForIndicator);
        return jumlaWateja;
    }

    private int getJumlaWaliopatikana(JSONObject jsonObject, int jumlaWaliopatikana, String indicatorCode) throws JSONException {
        //gets the vertical total and adds the horizontal total to the jsonObject
        int totalFoundClientsPerCode = getTotalFoundClients(indicatorCode);
        jumlaWaliopatikana += totalFoundClientsPerCode;
        jsonObject.put(indicatorCode +"-jumla-waliopatikana", totalFoundClientsPerCode);
        return jumlaWaliopatikana;
    }

    private int getIndicatorTotal(String indicator) {
        int total = 0;
        for (String indicatorCode : indicatorCodesWithGroups) {
            if (indicatorCode.startsWith(indicator)) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int getTotalForClinicPerGroup(String indicator) {
        final String[] clinics = new String[]{"ctc", "pmtct", "tb", "wajidunga"};
        int total = 0;
        for(String clinic : clinics) {
            total += getIndicatorTotal(indicator + "-" + clinic);
        }
       return total;
    }

    private int getTotalFoundClients(String indicator){
        final String[] foundClientsGroup = new String[]{"a1", "b1", "d1", "e1", "f1", "g1"};
        int total = 0;
        for (String foundClientGroup : foundClientsGroup) {
            total += getIndicatorTotal(indicator + "-" + foundClientGroup);
        }
        return total;
    }

    private int getTotalPerGroup(String indicatorGroup) {

        return getIndicatorTotal("ltfu-less-2-me" + "-" + indicatorGroup) +
                getIndicatorTotal("ltfu-less-2-ke" + "-" + indicatorGroup) + getIndicatorTotal("ltfu-2-14-me" + "-" + indicatorGroup)
                + getIndicatorTotal("ltfu-2-14-ke" + "-" + indicatorGroup) + getIndicatorTotal("ltfu-15-24-me" + "-" + indicatorGroup) +
                getIndicatorTotal("ltfu-15-24-ke" + "-" + indicatorGroup) + getIndicatorTotal("ltfu-25-49-me" + "-" + indicatorGroup) +
                getIndicatorTotal("ltfu-25-49-ke" + "-" + indicatorGroup) + getIndicatorTotal("ltfu-60-me" + "-" + indicatorGroup) +
                getIndicatorTotal("ltfu-60-ke" + "-" + indicatorGroup);

    }
}
