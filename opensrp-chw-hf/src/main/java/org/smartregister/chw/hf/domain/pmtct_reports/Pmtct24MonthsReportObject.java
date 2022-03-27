package org.smartregister.chw.hf.domain.pmtct_reports;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Pmtct24MonthsReportObject extends ReportObject {
    DecimalFormat df = new DecimalFormat();
    private List<String> indicatorCodes = new ArrayList<>();
    private Date reportDate;

    public Pmtct24MonthsReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
        setIndicatorCodes(indicatorCodes);
    }

    public void setIndicatorCodes(List<String> indicatorCodes) {
        indicatorCodes.add("A24");
        indicatorCodes.add("B24");
        indicatorCodes.add("C24");
        indicatorCodes.add("D24");
        indicatorCodes.add("E24");
        indicatorCodes.add("F24");
        indicatorCodes.add("G24");
        indicatorCodes.add("H24");
        indicatorCodes.add("I24");
        indicatorCodes.add("J24");
        indicatorCodes.add("K24");
        indicatorCodes.add("L24");
        indicatorCodes.add("M24");
        indicatorCodes.add("N24");
        indicatorCodes.add("O24");
        indicatorCodes.add("P24");
        indicatorCodes.add("Q24");
        indicatorCodes.add("R24");
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONObject indicatorDataObject = new JSONObject();
        for (String indicatorCode : indicatorCodes) {
            indicatorDataObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate));
        }
        df.setMaximumFractionDigits(2);

        indicatorDataObject.put("D24", getIndicatorD24());
        indicatorDataObject.put("I24", df.format(getIndicatorI24()) + "%");
        indicatorDataObject.put("L24", getIndicatorL24());
        indicatorDataObject.put("R24", df.format(getIndicatorR24()) + "%");

        return indicatorDataObject;
    }

    private int getIndicatorD24() {
        //D24 = A24 + B24 - C24
        return ReportDao.getReportPerIndicatorCode("A24", reportDate)
                + ReportDao.getReportPerIndicatorCode("B24", reportDate)
                - ReportDao.getReportPerIndicatorCode("C24", reportDate);
    }

    private float getIndicatorI24() {
        //I24 = H24/D24 * 100
        if (getIndicatorD24() > 0) {
            return ((ReportDao.getReportPerIndicatorCode("H24", reportDate) * 1f) / getIndicatorD24()) * 100;
        }
        return 0;
    }

    private int getIndicatorL24() {
        //L24 = J24 - K24
        return ReportDao.getReportPerIndicatorCode("J24", reportDate) - ReportDao.getReportPerIndicatorCode("K24", reportDate);
    }

    private float getIndicatorR24() {
        //R24 = (M24 + N24)/L24 * 100
        if (getIndicatorL24() > 0) {
            return ((ReportDao.getReportPerIndicatorCode("M24", reportDate) + ReportDao.getReportPerIndicatorCode("N24", reportDate)) * 1f) / getIndicatorL24() * 100;
        }
        return 0;
    }

}
