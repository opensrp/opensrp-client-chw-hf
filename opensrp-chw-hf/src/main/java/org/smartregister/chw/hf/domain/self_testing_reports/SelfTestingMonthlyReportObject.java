package org.smartregister.chw.hf.domain.self_testing_reports;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.util.Date;

public class SelfTestingMonthlyReportObject extends ReportObject {


    private final String[] questionsGroups = new String[]{
            "2","2-i","2-ii","2-iii","2-iv","2-v","2-vi",
            "3","3-i","3-ii","3-iii","3-iv","3-v","3-vi",
            "7","7-i","7-ii","7-iii","7-iv","7-v","7-vi",
            "8","8-i","8-ii","8-iii","8-iv","8-v","8-vi",
            "9","9-i","9-ii","9-iii","9-iv","9-v","9-vi"
    };
    private final String[] ageGroups = new String[]{
            "18-19","20-24","25-29","30-34","35-39","40-44","45-49",">50"
    };
    private final String[] genderGroups = new String[]{
            "ME","KE"
    };

    private final Date reportDate;
    private JSONObject jsonObject ;

    public SelfTestingMonthlyReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        jsonObject = new JSONObject();
        for (String questionGroup : questionsGroups) {   //rows
            for (String ageGroup : ageGroups) {  //columns
                for (String genderGroup : genderGroups) {  //concstenate rows columns and gendergroup
                        jsonObject.put("hivst"+"-"+questionGroup+"-"+ageGroup+"-"+genderGroup,
                                ReportDao.getReportPerIndicatorCode("hivst"+"-"+questionGroup+"-"+ageGroup+"-"+genderGroup, reportDate));
                }
            }
        }
        jsonObject.put("hivst-4-a", ReportDao.getReportPerIndicatorCode("hivst-4-a", reportDate));
        jsonObject.put("hivst-5-a", ReportDao.getReportPerIndicatorCode("hivst-5-a", reportDate));
        jsonObject.put("hivst-6-a", ReportDao.getReportPerIndicatorCode("hivst-6-a", reportDate));
        jsonObject.put("hivst-4-b", ReportDao.getReportPerIndicatorCode("hivst-4-b", reportDate));
        jsonObject.put("hivst-5-b", ReportDao.getReportPerIndicatorCode("hivst-5-b", reportDate));
        jsonObject.put("hivst-6-b", ReportDao.getReportPerIndicatorCode("hivst-6-b", reportDate));

        // get total of all Male & Female in Qn 2 & 7
        //and the whole total for both of them
        funcGetTotal();

        return jsonObject;
    }

    private int getTotalPerEachIndicator(String total_indicatorCode, String question, String gender) throws JSONException {
        int totalOfGenderGiven = 0;
        for (String agegroup: ageGroups){
                totalOfGenderGiven += ReportDao.getReportPerIndicatorCode(total_indicatorCode+"-"+agegroup+"-"+gender, reportDate);
            jsonObject.put("hivst"+"-"+question+"-jumla-"+gender,totalOfGenderGiven);  //display the total for specified gender
        }
        return totalOfGenderGiven;
    }



    private void funcGetTotal() throws JSONException {
        for (String question: questionsGroups) {   //rows
            int totalOfBothMaleAndFemale = getTotalPerEachIndicator("hivst"+"-"+question,question,"ME")
                            + getTotalPerEachIndicator("hivst"+"-"+question,question,"KE");
            jsonObject.put("hivst"+"-"+question+"-jumla-both-ME",totalOfBothMaleAndFemale);
            jsonObject.put("hivst"+"-"+question+"-jumla-both-KE",totalOfBothMaleAndFemale);
        }
    }


}
