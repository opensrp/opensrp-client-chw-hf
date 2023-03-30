package org.smartregister.chw.hf.domain.cdp_reports;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.util.Date;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class CdpIssuingFromFacilityReportObject extends ReportObject {
    private Date reportDate;

    public CdpIssuingFromFacilityReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONArray dataArray = new JSONArray();
        List<Map<String, String>> getHfCdpStockissuingLogList = ReportDao.getHfIssuingCdpStockLog(reportDate);

        for (int i=0; i<getHfCdpStockissuingLogList.size(); i++){
            Timber.tag("hukuje").d("" + getHfCdpStockissuingLogList.get(i));
        }

        int i = 0;
        int flag_count_female=0;
        int flag_count_male=0;
        for (Map<String, String> getHfCdpStockLog : getHfCdpStockissuingLogList) {
            JSONObject reportJsonObject = new JSONObject();
            reportJsonObject.put("id", ++i);

            if (getCdpClientDetails(getHfCdpStockLog, "point_of_service").equals("other")){
                reportJsonObject.put("point-of-service", getCdpClientDetails(getHfCdpStockLog, "other_point_of_service"));
                reportJsonObject.put("male-condoms-offset", getCdpClientDetails(getHfCdpStockLog, "male_condoms_offset"));
                reportJsonObject.put("female-condoms-offset", getCdpClientDetails(getHfCdpStockLog, "female_condoms_offset"));
                flag_count_male+=Integer.parseInt(getCdpClientDetails(getHfCdpStockLog, "male_condoms_offset"));
                flag_count_female+=Integer.parseInt(getCdpClientDetails(getHfCdpStockLog, "female_condoms_offset"));
            }

            else if (!getCdpClientDetails(getHfCdpStockLog, "point_of_service").equals("other")){
                reportJsonObject.put("point-of-service", getCdpClientDetails(getHfCdpStockLog, "point_of_service"));
                reportJsonObject.put("male-condoms-offset", getCdpClientDetails(getHfCdpStockLog, "male_condoms_offset"));
                reportJsonObject.put("female-condoms-offset", getCdpClientDetails(getHfCdpStockLog, "female_condoms_offset"));
                flag_count_male+=Integer.parseInt(getCdpClientDetails(getHfCdpStockLog, "male_condoms_offset"));
                flag_count_female+=Integer.parseInt(getCdpClientDetails(getHfCdpStockLog, "female_condoms_offset"));
            }


           // ==> oustside
            if (getCdpClientDetails(getHfCdpStockLog, "condom_type").equals("male_condom")){
                reportJsonObject.put("point-of-service", getCdpClientDetails(getHfCdpStockLog, "requester"));
                reportJsonObject.put("male-condoms-offset", getCdpClientDetails(getHfCdpStockLog, "quantity_response"));
                reportJsonObject.put("female-condoms-offset", getCdpClientDetails(getHfCdpStockLog, "0"));
                flag_count_male+=Integer.parseInt(getCdpClientDetails(getHfCdpStockLog, "quantity_response"));
            }

            if(getCdpClientDetails(getHfCdpStockLog, "condom_type").equals("female_condom")){
                reportJsonObject.put("point-of-service", getCdpClientDetails(getHfCdpStockLog, "requester"));
                reportJsonObject.put("male-condoms-offset", getCdpClientDetails(getHfCdpStockLog, "0"));
                reportJsonObject.put("female-condoms-offset", getCdpClientDetails(getHfCdpStockLog, "quantity_response"));
                flag_count_female+=Integer.parseInt(getCdpClientDetails(getHfCdpStockLog, "quantity_response"));
            }

           dataArray.put(reportJsonObject);
        }

        //finally go display total of all
        if (flag_count_male > 0 || flag_count_female > 0 ){
            JSONObject reportJsonObject = new JSONObject();
            reportJsonObject.put("total-id",i+1);
            reportJsonObject.put("total","TOTAL NUMBER OF CONDOMS RECEIVED");
            reportJsonObject.put("total-male-condoms",flag_count_male);
            reportJsonObject.put("total-female-condoms",flag_count_female);
            dataArray.put(reportJsonObject);
        }

        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put("reportData", dataArray);

        return resultJsonObject;
    }

    private String getCdpClientDetails(Map<String, String> chwRegistrationFollowupClient, String key) {
        String details = chwRegistrationFollowupClient.get(key);
        assert details != null;
        if (!details.isEmpty()) {
            return details;
        }else {
            if (key.equals("0") || key.equals("male_condoms_offset") || key.equals("female_condoms_offset")){
                return "0";
            }else
                return "-";
        }

    }


}
