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
    private final String[] indicatorCodesArray = new String[]{"less-2-me", "less-2-ke", "2-14-me",
            "2-14-ke", "15-24-me", "15-24-ke", "25-49-me", "25-49-ke", "60-me", "60-ke"};
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
        for (String indicatorCode : indicatorCodesWithGroups) {
            jsonObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate));
        }

        for (String indicatorGroup : indicatorGroups) {
            jsonObject.put("jumla" + "-" + indicatorGroup, getTotalPerGroup(indicatorGroup));
        }

        return jsonObject;
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

    private int getTotalPerGroup(String indicatorGroup) {

        return getIndicatorTotal("less-2-me" + "-" + indicatorGroup) +
                getIndicatorTotal("less-2-ke" + "-" + indicatorGroup) + getIndicatorTotal("2-14-me" + "-" + indicatorGroup)
                + getIndicatorTotal("2-14-ke" + "-" + indicatorGroup) + getIndicatorTotal("15-24-me" + "-" + indicatorGroup) +
                getIndicatorTotal("15-24-ke" + "-" + indicatorGroup) + getIndicatorTotal("25-49-me" + "-" + indicatorGroup) +
                getIndicatorTotal("25-49-ke" + "-" + indicatorGroup) + getIndicatorTotal("60-me" + "-" + indicatorGroup) +
                getIndicatorTotal("60-ke" + "-" + indicatorGroup);

    }
}
