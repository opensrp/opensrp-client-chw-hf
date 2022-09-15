package org.smartregister.chw.hf.domain.pmtct_reports;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Pmtct3MonthsReportObject extends ReportObject {
    DecimalFormat df = new DecimalFormat();
    private List<String> indicatorCodes = new ArrayList<>();
    private Date reportDate;

    public Pmtct3MonthsReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
        setIndicatorCodes(indicatorCodes);
    }

    public void setIndicatorCodes(List<String> indicatorCodes) {
        indicatorCodes.add("B3a");
        indicatorCodes.add("B3b");
        indicatorCodes.add("B3c");
        indicatorCodes.add("B3d");
        indicatorCodes.add("C3a");
        indicatorCodes.add("C3b");
        indicatorCodes.add("D3");
        indicatorCodes.add("E3");
        indicatorCodes.add("G3");
        indicatorCodes.add("H3");
        indicatorCodes.add("I3");
        indicatorCodes.add("J3");
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONObject indicatorDataObject = new JSONObject();
        for (String indicatorCode : indicatorCodes) {
            indicatorDataObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate));
        }
        df.setMaximumFractionDigits(2);
        //F3 = A3 + D3 - E3
        indicatorDataObject.put("F3", getIndicatorA3() + getIndicatorD3() - getIndicatorE3());
        indicatorDataObject.put("K3", df.format(getIndicatorK3()) + "%");
        return indicatorDataObject;
    }

    private int getIndicatorA3() {
        // A3 = B3a + B3b + B3c + B3d
        return ReportDao.getReportPerIndicatorCode("B3a", reportDate)
                + ReportDao.getReportPerIndicatorCode("B3b", reportDate)
                + ReportDao.getReportPerIndicatorCode("B3c", reportDate)
                + ReportDao.getReportPerIndicatorCode("B3d", reportDate);
    }

    private int getIndicatorD3() {
        return ReportDao.getReportPerIndicatorCode("D3", reportDate);
    }

    private int getIndicatorE3() {
        return ReportDao.getReportPerIndicatorCode("E3", reportDate);
    }

    private float getIndicatorK3() {
        //if F3 - J3 < 0 return 0
        //else K3 = G3 /(F3 - J3) * 100 to 2 decimal places
        int denominator = getIndicatorF3() - getIndicatorJ3();
        float numerator = getIndicatorG3() * 1f;
        if (denominator > 0) {
            return ((numerator) / (denominator)) * 100;
        }
        return 0;

    }

    private int getIndicatorF3() {
        return getIndicatorA3() + getIndicatorD3() - getIndicatorE3();
    }

    private int getIndicatorJ3() {
        return ReportDao.getReportPerIndicatorCode("J3", reportDate);
    }

    private int getIndicatorG3() {
        return ReportDao.getReportPerIndicatorCode("G3", reportDate);
    }


}
