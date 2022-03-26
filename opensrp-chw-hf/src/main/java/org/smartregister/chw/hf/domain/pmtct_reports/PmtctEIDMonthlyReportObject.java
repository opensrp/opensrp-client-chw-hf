package org.smartregister.chw.hf.domain.pmtct_reports;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PmtctEIDMonthlyReportObject extends ReportObject {
    DecimalFormat df = new DecimalFormat();
    private List<String> indicatorCodes = new ArrayList<>();
    private Date reportDate;

    public PmtctEIDMonthlyReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
        setIndicatorCodes(indicatorCodes);
    }

    public void setIndicatorCodes(List<String> indicatorCodes) {
        indicatorCodes.add("A");
        indicatorCodes.add("B");
        indicatorCodes.add("C");
        indicatorCodes.add("D");
        indicatorCodes.add("E");
        indicatorCodes.add("A1");
        indicatorCodes.add("B1");
        indicatorCodes.add("C1");
        indicatorCodes.add("D1");
        indicatorCodes.add("E1");
        indicatorCodes.add("F");
        indicatorCodes.add("G");
        indicatorCodes.add("H");
        indicatorCodes.add("I");
        indicatorCodes.add("J");
        indicatorCodes.add("H1");
        indicatorCodes.add("I1");
        indicatorCodes.add("J1");
        indicatorCodes.add("K");
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONObject indicatorDataObject = new JSONObject();
        for (String indicatorCode : indicatorCodes) {
            indicatorDataObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate));
        }
        df.setMaximumFractionDigits(2);
        indicatorDataObject.put("E", df.format(getIndicatorE()));
        return indicatorDataObject;
    }

    private int getIndicatorE() {
        //E = B + C + D
        return ReportDao.getReportPerIndicatorCode("B", reportDate) + ReportDao.getReportPerIndicatorCode("C", reportDate) + ReportDao.getReportPerIndicatorCode("D", reportDate);
    }

}
