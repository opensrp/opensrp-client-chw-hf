package org.smartregister.chw.hf.domain.mother_champion_repots;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class MotherChampionReportObject extends ReportObject {
    final Date reportDate;
    private final String[] indicatorCodes = new String[]{"b-1", "b-2", "b-3", "b-4", "b-5", "b-6", "b-7"};

    public MotherChampionReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
    }


    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONArray dataArray = new JSONArray();

        List<Map<String, String>> motherChampionsList = ReportDao.getMotherChampions(reportDate);

        int i = 0;
        for (Map<String, String> motherChampion : motherChampionsList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", ++i);
            jsonObject.put("majina_mama_vinara", motherChampion.get("chw_name"));
            jsonObject.put("jina_lililosajiliwa", motherChampion.get("provider_id"));
            dataArray.put(jsonObject);
        }


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("reportData", dataArray);
        for (String indicatorCode : indicatorCodes) {
            jsonObject.put(indicatorCode, ReportDao.getReportPerIndicatorCode(indicatorCode, reportDate));
        }

        return jsonObject;
    }

}
