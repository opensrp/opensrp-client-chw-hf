package org.smartregister.chw.hf.domain.mother_champion_repots;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.domain.ReportObject;

import java.util.Date;

public class MotherChampionReportObject extends ReportObject {
    final Date reportDate;
    private final String[] motherChampionDetails = new String[]{"majina_mama_vinara", "namba_ya_simu", "jina_lililosajiliwa", "sahihi"};
    private final String[] indicatorCodes = new String[]{"b-1", "b-2", "b-3", "b-4", "b-5", "b-6", "b-7"};

    public MotherChampionReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
    }


    @Override
    public JSONObject getIndicatorData() throws JSONException {
        /*

         * we'll pass a json object with the following structure:
         * reportData = [
            {
              "id": "1",
              "majina_mama_vinara": "name",
              "namba_ya_simu": "number",
              "jina_lililosajiliwa": "name",
              "sahihi": "signed",
             },
          ]
         * */
        JSONObject jsonObject = new JSONObject();
        JSONArray dataArray = new JSONArray();
        //Todo get an array of ids of mother champions from the db for that report time period
        //something like : List<String> motherChampionIds = ReportDao.getAllMotherChampionIds(reportDate);
        //Todo loop through the motherChampionIds and get the data for each motherChampion
        //something like : for (String motherChampionId : motherChampionIds) {
        //                      JSONObject motherChampionObject = new JSONObject();
        //                      motherChampionObject.put("id", motherChampionId);
        //                      for(String motherChampionDetail : motherChampionDetails) {
        //                          motherChampionObject.put(motherChampionDetail, ReportDao.getMotherChampionDetail(motherChampionId, reportDate));
        //                      }
        //                      dataArray.put(motherChampionObject);
        //                  }

        //TODO add the dataArray to the jsonObject

        //SAMPLE
        JSONObject motherChampionObject = new JSONObject();
        motherChampionObject.put("id", 1);
        for (String motherChampionDetail : motherChampionDetails) {
            motherChampionObject.put(motherChampionDetail, "test");
        }
        dataArray.put(motherChampionObject);
        //END SAMPLE

        jsonObject.put("reportData", dataArray);
        for (String indicatorCode : indicatorCodes) {
            //jsonObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode(indicatorCode));
            //TODO: remove this for test purposes
            jsonObject.put(indicatorCode, 10);
        }

        return jsonObject;
    }

}
