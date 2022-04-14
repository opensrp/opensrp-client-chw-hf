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

    public PncMonthlyReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
        setIndicatorCodes(indicatorCodes);
    }

    public void setIndicatorCodes(List<String> indicatorCodes) {
        indicatorCodes.add("pnc-1a-10-14");
        indicatorCodes.add("pnc-1a-15-19");
        indicatorCodes.add("pnc-1a-20-24");
        indicatorCodes.add("pnc-1a-25-29");
        indicatorCodes.add("pnc-1a-30-34");
        indicatorCodes.add("pnc-1a-35+");
        indicatorCodes.add("pnc-1b-10-14");
        indicatorCodes.add("pnc-1b-15-19");
        indicatorCodes.add("pnc-1b-20-24");
        indicatorCodes.add("pnc-1b-25-29");
        indicatorCodes.add("pnc-1b-30-34");
        indicatorCodes.add("pnc-1b-35+");
        indicatorCodes.add("pnc-2a-10-14");
        indicatorCodes.add("pnc-2a-15-19");
        indicatorCodes.add("pnc-2a-20-24");
        indicatorCodes.add("pnc-2a-25-29");
        indicatorCodes.add("pnc-2a-30-34");
        indicatorCodes.add("pnc-2a-35+");
        indicatorCodes.add("pnc-3-10-14");
        indicatorCodes.add("pnc-3-15-19");
        indicatorCodes.add("pnc-3-20-24");
        indicatorCodes.add("pnc-3-25-29");
        indicatorCodes.add("pnc-3-30-34");
        indicatorCodes.add("pnc-3-35+");
        indicatorCodes.add("pnc-4-10-14");
        indicatorCodes.add("pnc-4-15-19");
        indicatorCodes.add("pnc-4-20-24");
        indicatorCodes.add("pnc-4-25-29");
        indicatorCodes.add("pnc-4-30-34");
        indicatorCodes.add("pnc-4-35+");
        indicatorCodes.add("pnc-5-10-14");
        indicatorCodes.add("pnc-5-15-19");
        indicatorCodes.add("pnc-5-20-24");
        indicatorCodes.add("pnc-5-25-29");
        indicatorCodes.add("pnc-5-30-34");
        indicatorCodes.add("pnc-5-35+");
        indicatorCodes.add("pnc-6-10-14");
        indicatorCodes.add("pnc-6-15-19");
        indicatorCodes.add("pnc-6-20-24");
        indicatorCodes.add("pnc-6-25-29");
        indicatorCodes.add("pnc-6-30-34");
        indicatorCodes.add("pnc-6-35+");
        indicatorCodes.add("pnc-7-10-14");
        indicatorCodes.add("pnc-7-15-19");
        indicatorCodes.add("pnc-7-20-24");
        indicatorCodes.add("pnc-7-25-29");
        indicatorCodes.add("pnc-7-30-34");
        indicatorCodes.add("pnc-7-35+");
        indicatorCodes.add("pnc-8a-10-14");
        indicatorCodes.add("pnc-8a-15-19");
        indicatorCodes.add("pnc-8a-20-24");
        indicatorCodes.add("pnc-8a-25-29");
        indicatorCodes.add("pnc-8a-30-34");
        indicatorCodes.add("pnc-8a-35+");
        indicatorCodes.add("pnc-8b-10-14");
        indicatorCodes.add("pnc-8b-15-19");
        indicatorCodes.add("pnc-8b-20-24");
        indicatorCodes.add("pnc-8b-25-29");
        indicatorCodes.add("pnc-8b-30-34");
        indicatorCodes.add("pnc-8b-35+");
        indicatorCodes.add("pnc-8c-10-14");
        indicatorCodes.add("pnc-8c-15-19");
        indicatorCodes.add("pnc-8c-20-24");
        indicatorCodes.add("pnc-8c-25-29");
        indicatorCodes.add("pnc-8c-30-34");
        indicatorCodes.add("pnc-8c-35+");
        indicatorCodes.add("pnc-9a-10-14");
        indicatorCodes.add("pnc-9a-15-19");
        indicatorCodes.add("pnc-9a-20-24");
        indicatorCodes.add("pnc-9a-25-29");
        indicatorCodes.add("pnc-9a-30-34");
        indicatorCodes.add("pnc-9a-35+");
        indicatorCodes.add("pnc-9b-10-14");
        indicatorCodes.add("pnc-9b-15-19");
        indicatorCodes.add("pnc-9b-20-24");
        indicatorCodes.add("pnc-9b-25-29");
        indicatorCodes.add("pnc-9b-30-34");
        indicatorCodes.add("pnc-9b-35+");
        indicatorCodes.add("pnc-9c-10-14");
        indicatorCodes.add("pnc-9c-15-19");
        indicatorCodes.add("pnc-9c-20-24");
        indicatorCodes.add("pnc-9c-25-29");
        indicatorCodes.add("pnc-9c-30-34");
        indicatorCodes.add("pnc-9c-35+");
        indicatorCodes.add("pnc-9d1-10-14");
        indicatorCodes.add("pnc-9d1-15-19");
        indicatorCodes.add("pnc-9d1-20-24");
        indicatorCodes.add("pnc-9d1-25-29");
        indicatorCodes.add("pnc-9d1-30-34");
        indicatorCodes.add("pnc-9d1-35+");
        indicatorCodes.add("pnc-9d2-10-14");
        indicatorCodes.add("pnc-9d2-15-19");
        indicatorCodes.add("pnc-9d2-20-24");
        indicatorCodes.add("pnc-9d2-25-29");
        indicatorCodes.add("pnc-9d2-30-34");
        indicatorCodes.add("pnc-9d2-35+");
        indicatorCodes.add("pnc-9e-10-14");
        indicatorCodes.add("pnc-9e-15-19");
        indicatorCodes.add("pnc-9e-20-24");
        indicatorCodes.add("pnc-9e-25-29");
        indicatorCodes.add("pnc-9e-30-34");
        indicatorCodes.add("pnc-9e-35+");
        indicatorCodes.add("pnc-9f-10-14");
        indicatorCodes.add("pnc-9f-15-19");
        indicatorCodes.add("pnc-9f-20-24");
        indicatorCodes.add("pnc-9f-25-29");
        indicatorCodes.add("pnc-9f-30-34");
        indicatorCodes.add("pnc-9f-35+");
        indicatorCodes.add("pnc-10a-10-14");
        indicatorCodes.add("pnc-10a-15-19");
        indicatorCodes.add("pnc-10a-20-24");
        indicatorCodes.add("pnc-10a-25-29");
        indicatorCodes.add("pnc-10a-30-34");
        indicatorCodes.add("pnc-10a-35+");
        indicatorCodes.add("pnc-10b-10-14");
        indicatorCodes.add("pnc-10b-15-19");
        indicatorCodes.add("pnc-10b-20-24");
        indicatorCodes.add("pnc-10b-25-29");
        indicatorCodes.add("pnc-10b-30-34");
        indicatorCodes.add("pnc-10b-35+");
        indicatorCodes.add("pnc-10c-10-14");
        indicatorCodes.add("pnc-10c-15-19");
        indicatorCodes.add("pnc-10c-20-24");
        indicatorCodes.add("pnc-10c-25-29");
        indicatorCodes.add("pnc-10c-30-34");
        indicatorCodes.add("pnc-10c-35+");
        indicatorCodes.add("pnc-10d-10-14");
        indicatorCodes.add("pnc-10d-15-19");
        indicatorCodes.add("pnc-10d-20-24");
        indicatorCodes.add("pnc-10d-25-29");
        indicatorCodes.add("pnc-10d-30-34");
        indicatorCodes.add("pnc-10d-35+");
        indicatorCodes.add("pnc-10e-10-14");
        indicatorCodes.add("pnc-10e-15-19");
        indicatorCodes.add("pnc-10e-20-24");
        indicatorCodes.add("pnc-10e-25-29");
        indicatorCodes.add("pnc-10e-30-34");
        indicatorCodes.add("pnc-10e-35+");
        indicatorCodes.add("pnc-11a-ME");
        indicatorCodes.add("pnc-11a-KE");
        indicatorCodes.add("pnc-11b-ME");
        indicatorCodes.add("pnc-11b-KE");
        indicatorCodes.add("pnc-11c-ME");
        indicatorCodes.add("pnc-11c-KE");
        indicatorCodes.add("pnc-12a-ME");
        indicatorCodes.add("pnc-12a-KE");
        indicatorCodes.add("pnc-12b-ME");
        indicatorCodes.add("pnc-12b-KE");
        indicatorCodes.add("pnc-12c-ME");
        indicatorCodes.add("pnc-12c-KE");
        indicatorCodes.add("pnc-12d-ME");
        indicatorCodes.add("pnc-12d-KE");
        indicatorCodes.add("pnc-12e-ME");
        indicatorCodes.add("pnc-12e-KE");
        indicatorCodes.add("pnc-12f-ME");
        indicatorCodes.add("pnc-12f-KE");
        indicatorCodes.add("pnc-13a-ME");
        indicatorCodes.add("pnc-13a-KE");
        indicatorCodes.add("pnc-13b-ME");
        indicatorCodes.add("pnc-13b-KE");
        indicatorCodes.add("pnc-13c-ME");
        indicatorCodes.add("pnc-13c-KE");
        indicatorCodes.add("pnc-13d-ME");
        indicatorCodes.add("pnc-13d-KE");
        indicatorCodes.add("pnc-14-ME");
        indicatorCodes.add("pnc-14-KE");
        indicatorCodes.add("pnc-15-ME");
        indicatorCodes.add("pnc-15-KE");
        indicatorCodes.add("pnc-16a-ME");
        indicatorCodes.add("pnc-16a-KE");
        indicatorCodes.add("pnc-16b-ME");
        indicatorCodes.add("pnc-16b-KE");
        indicatorCodes.add("pnc-16c-ME");
        indicatorCodes.add("pnc-16c-KE");
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONObject indicatorObject = new JSONObject();
        for (String indicatorCode : indicatorCodes) {
            indicatorObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate));
        }

        indicatorObject.put("pnc-1a+1b-10-14", getSumOfIndicator1aAnd1b10to14());
        indicatorObject.put("pnc-1a+1b-15-19", getSumOfIndicator1aAnd1b15to19());
        indicatorObject.put("pnc-1a+1b-20-24", getSumOfIndicator1aAnd1b20to24());
        indicatorObject.put("pnc-1a+1b-25-29", getSumOfIndicator1aAnd1b250to29());
        indicatorObject.put("pnc-1a+1b-30-34", getSumOfIndicator1aAnd1b30to34());
        indicatorObject.put("pnc-1a+1b-35+", getSumOfIndicator1aAnd1b35above());

        indicatorObject.put("pnc-1a+1b-jumla", getIndicator1aPlus1bTotal());

        indicatorObject.put("pnc-11a+11b-ME", getSumOfIndicator11aAnd11bMale());
        indicatorObject.put("pnc-11a+11b-KE", getSumOfIndicator11aAnd11bFemale());

        indicatorObject.put("pnc-11a+11b-jumla", getIndicator11aPlus11bTotal());



        indicatorObject.put("pnc-1a-jumla", get1aTotal());
        indicatorObject.put("pnc-1b-jumla", get1bTotal());
        indicatorObject.put("pnc-2a-jumla", get2aTotal());
        indicatorObject.put("pnc-3-jumla", get3Total());
        indicatorObject.put("pnc-4-jumla", get4Total());
        indicatorObject.put("pnc-5-jumla", get5Total());
        indicatorObject.put("pnc-6-jumla", get6Total());
        indicatorObject.put("pnc-7-jumla", get7Total());
        indicatorObject.put("pnc-8a-jumla", get8aTotal());
        indicatorObject.put("pnc-8b-jumla", get8bTotal());
        indicatorObject.put("pnc-8c-jumla", get8cTotal());

        indicatorObject.put("pnc-9a-jumla", get9aTotal());
        indicatorObject.put("pnc-9b-jumla", get9bTotal());
        indicatorObject.put("pnc-9c-jumla", get9cTotal());
        indicatorObject.put("pnc-9d1-jumla", get9d1Total());
        indicatorObject.put("pnc-9d2-jumla", get9d2Total());
        indicatorObject.put("pnc-9e-jumla", get9eTotal());
        indicatorObject.put("pnc-9f-jumla", get9fTotal());

        indicatorObject.put("pnc-10a-jumla", get10aTotal());
        indicatorObject.put("pnc-10b-jumla", get10bTotal());
        indicatorObject.put("pnc-10c-jumla", get10cTotal());
        indicatorObject.put("pnc-10d-jumla", get10dTotal());
        indicatorObject.put("pnc-10e-jumla", get10eTotal());
        indicatorObject.put("pnc-11a-jumla", get11aTotal());
        indicatorObject.put("pnc-11b-jumla", get11bTotal());
        indicatorObject.put("pnc-11c-jumla", get11cTotal());
        indicatorObject.put("pnc-12a-jumla", get12aTotal());
        indicatorObject.put("pnc-12b-jumla", get12bTotal());
        indicatorObject.put("pnc-12c-jumla", get12cTotal());
        indicatorObject.put("pnc-12d-jumla", get12dTotal());
        indicatorObject.put("pnc-12e-jumla", get12eTotal());
        indicatorObject.put("pnc-12f-jumla", get12fTotal());
        indicatorObject.put("pnc-13a-jumla", get13aTotal());
        indicatorObject.put("pnc-13b-jumla", get13bTotal());
        indicatorObject.put("pnc-13c-jumla", get13cTotal());
        indicatorObject.put("pnc-13d-jumla", get13dTotal());
        indicatorObject.put("pnc-14-jumla", get14Total());
        indicatorObject.put("pnc-15-jumla", get15Total());
        indicatorObject.put("pnc-16a-jumla", get16aTotal());
        indicatorObject.put("pnc-16b-jumla", get16bTotal());
        indicatorObject.put("pnc-16c-jumla", get16cTotal());


        return indicatorObject;
    }

    //get horizontal totals
    //get 1a + 1b indicators
    private int getSumOfIndicator1aAnd1b10to14() {
        int pnc_1a_10_14 = ReportDao.getReportPerIndicatorCode("pnc-1a-10-14", reportDate);
        int pnc_1b_10_14 = ReportDao.getReportPerIndicatorCode("pnc-1b-10-14", reportDate);
        return pnc_1b_10_14 + pnc_1a_10_14;
    }
    private int getSumOfIndicator1aAnd1b15to19() {
        int pnc_1a_15_19 = ReportDao.getReportPerIndicatorCode("pnc-1a-15-19", reportDate);
        int pnc_1b_15_19 = ReportDao.getReportPerIndicatorCode("pnc-1b-15-19", reportDate);
        return pnc_1b_15_19 + pnc_1a_15_19;
    }
    private int getSumOfIndicator1aAnd1b20to24() {
        int pnc_1a_20_24 = ReportDao.getReportPerIndicatorCode("pnc-1a-20-24", reportDate);
        int pnc_1b_20_24 = ReportDao.getReportPerIndicatorCode("pnc-1b-20-24", reportDate);
        return pnc_1b_20_24 + pnc_1a_20_24;
    }
    private int getSumOfIndicator1aAnd1b250to29() {
        int pnc_1a_25_29 = ReportDao.getReportPerIndicatorCode("pnc-1a-25-29", reportDate);
        int pnc_1b_25_29 = ReportDao.getReportPerIndicatorCode("pnc-1b-25-29", reportDate);
        return pnc_1b_25_29 + pnc_1a_25_29;
    }
    private int getSumOfIndicator1aAnd1b30to34() {
        int pnc_1a_30_34 = ReportDao.getReportPerIndicatorCode("pnc-1a-30-34", reportDate);
        int pnc_1b_30_34 = ReportDao.getReportPerIndicatorCode("pnc-1b-30-34", reportDate);
        return pnc_1b_30_34 + pnc_1a_30_34;
    }
    private int getSumOfIndicator1aAnd1b35above(){
        int pnc_1a_35_plus = ReportDao.getReportPerIndicatorCode("pnc-1a-35+", reportDate);
        int pnc_1b_35_plus = ReportDao.getReportPerIndicatorCode("pnc-1b-35+", reportDate);
        return pnc_1b_35_plus + pnc_1a_35_plus;
    }

    //get 11a + 11b indicators
    private int getSumOfIndicator11aAnd11bMale() {
        int pnc_11a_ME = ReportDao.getReportPerIndicatorCode("pnc-11a-ME", reportDate);
        int pnc_11b_ME = ReportDao.getReportPerIndicatorCode("pnc-11b-ME", reportDate);
        return pnc_11b_ME + pnc_11a_ME;
    }

    private int getSumOfIndicator11aAnd11bFemale() {
        int pnc_11a_KE = ReportDao.getReportPerIndicatorCode("pnc-11a-KE", reportDate);
        int pnc_11b_KE = ReportDao.getReportPerIndicatorCode("pnc-11b-KE", reportDate);
        return pnc_11b_KE + pnc_11a_KE;
    }


    //get vertical totals
    private int get1aTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-1a")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get1bTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-1b")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int getIndicator1aPlus1bTotal(){
        return get1aTotal() + get1bTotal();
    }
    private int get2aTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-2a")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get3Total() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-3")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get4Total() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-4")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get5Total() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-5")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get6Total() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-6")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get7Total() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-7")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get8aTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-8a")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get8bTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-8b")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get8cTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-8c")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get9aTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-9a")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get9bTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-9b")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get9cTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-9c")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get9d1Total() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-9d1")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get9d2Total() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-9d2")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get9eTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-9e")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get9fTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-9f")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get10aTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-10a")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get10bTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-10b")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get10cTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-10c")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get10dTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-10d")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get10eTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-10e")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get11aTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-11a")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get11bTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-11b")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int getIndicator11aPlus11bTotal(){
        return get11aTotal() + get11bTotal();
    }

    private int get11cTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-11c")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get12aTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-12a")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get12bTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-12b")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get12cTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-12c")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get12dTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-12d")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get12eTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-12e")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get12fTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-12f")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get13aTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-13a")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get13bTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-13b")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get13cTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-13c")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get13dTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-13d")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get14Total() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-14")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get15Total() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-15")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get16aTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-16a")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get16bTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-16b")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

    private int get16cTotal() {
        int total = 0;
        for (String indicatorCode : indicatorCodes) {
            if (indicatorCode.startsWith("pnc-16c")) {
                total += ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate);
            }
        }
        return total;
    }

}
