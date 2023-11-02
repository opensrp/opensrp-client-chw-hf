package org.smartregister.chw.hf.domain.vmmc_reports;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class VmmcTheatreRegisterObject extends ReportObject {

    private Date reportDate;

    public VmmcTheatreRegisterObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
    }


    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONArray dataArray = new JSONArray();
        List<Map<String, String>> getVmmcRegisterList = ReportDao.getVmmcTheatreRegister(reportDate);

        int i = 0;

        for (Map<String, String> getVmmcRegister : getVmmcRegisterList) {
            JSONObject reportJsonObject = new JSONObject();
            reportJsonObject.put("id", ++i);

            reportJsonObject.put("mc_procedure_date", getVmmcClientDetails(getVmmcRegister, "mc_procedure_date"));
            reportJsonObject.put("vmmc_client_id", getVmmcClientDetails(getVmmcRegister, "vmmc_client_id"));
            reportJsonObject.put("names", getVmmcClientDetails(getVmmcRegister, "names"));
            reportJsonObject.put("age", getVmmcClientDetails(getVmmcRegister, "age"));
            reportJsonObject.put("male_circumcision_method", getVmmcClientDetails(getVmmcRegister, "male_circumcision_method"));
            reportJsonObject.put("size_place", getVmmcClientDetails(getVmmcRegister, "size_place"));

            reportJsonObject.put("aneathesia_administered", getVmmcClientDetails(getVmmcRegister, "aneathesia_administered"));
            reportJsonObject.put("start_time", getVmmcClientDetails(getVmmcRegister, "start_time"));
            reportJsonObject.put("end_time", getVmmcClientDetails(getVmmcRegister, "end_time"));
            reportJsonObject.put("surgeon_name", getVmmcClientDetails(getVmmcRegister, "surgeon_name"));
            reportJsonObject.put("assistant_name", getVmmcClientDetails(getVmmcRegister, "assistant_name"));

            reportJsonObject.put("type_of_adverse_event", getVmmcClientDetails(getVmmcRegister, "type_of_adverse_event"));

            dataArray.put(reportJsonObject);
        }


        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put("reportData", dataArray);

        return resultJsonObject;
    }

    private String getVmmcClientDetails(Map<String, String> chwRegistrationFollowupClient, String key) {
        String details = chwRegistrationFollowupClient.get(key);
        if (StringUtils.isNotBlank(details)) {
            return details;
        }
        return "-";
    }

}
