package org.smartregister.chw.hf.domain.pmtct_reports;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Pmtct12MonthsReportObject extends ReportObject {
    DecimalFormat df = new DecimalFormat();
    ;
    private List<String> indicatorCodes = new ArrayList<>();
    private Date reportDate;

    public Pmtct12MonthsReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
        setIndicatorCodes(indicatorCodes);
    }

    public void setIndicatorCodes(List<String> indicatorCodes) {
        indicatorCodes.add("A12");
        indicatorCodes.add("B12");
        indicatorCodes.add("C12");
        indicatorCodes.add("D12");
        indicatorCodes.add("E12");
        indicatorCodes.add("F12");
        indicatorCodes.add("G12");
        indicatorCodes.add("H12");
        indicatorCodes.add("I12");
        indicatorCodes.add("J12");
        indicatorCodes.add("K12");
        indicatorCodes.add("L12");
        indicatorCodes.add("M12");
        indicatorCodes.add("N12");
        indicatorCodes.add("O12");
        indicatorCodes.add("P12");
        indicatorCodes.add("Q12a");
        indicatorCodes.add("Q12b");
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONObject indicatorDataObject = new JSONObject();
        for (String indicatorCode : indicatorCodes) {
            indicatorDataObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate));
        }
        df.setMaximumFractionDigits(2);

        indicatorDataObject.put("D12", getIndicatorD12());
        indicatorDataObject.put("K12", df.format(getIndicatorK12()) + "%");

        return indicatorDataObject;
    }

    private int getIndicatorD12() {
        //D12 = A12 + B12 - C12
        return ReportDao.getReportPerIndicatorCode("A12", reportDate)
                + ReportDao.getReportPerIndicatorCode("B12", reportDate)
                - ReportDao.getReportPerIndicatorCode("C12", reportDate);
    }

    private float getIndicatorK12() {
        int denominator = (getIndicatorD12() - ReportDao.getReportPerIndicatorCode("J12", reportDate));
        if (denominator > 0) {
            return ((ReportDao.getReportPerIndicatorCode("E12", reportDate) * 1f) / denominator) * 100;
        }
        return 0;
    }


}
