package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PmtctNextFollowupVisitAction implements BasePmtctHomeVisitAction.PmtctHomeVisitActionHelper {

    private String jsonPayload;

    private String next_facility_visit_date;
    private BasePmtctHomeVisitAction.ScheduleStatus scheduleStatus;
    private String subTitle;
    private Context context;

    public PmtctNextFollowupVisitAction() {

    }


    @Override
    public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<org.smartregister.chw.pmtct.domain.VisitDetail>> map) {
        this.jsonPayload = jsonPayload;
        this.context = context;
    }

    @Override
    public String getPreProcessed() {

        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            return jsonObject.toString();
        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            next_facility_visit_date = CoreJsonFormUtils.getValue(jsonObject, "next_facility_visit_date");
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public BasePmtctHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
        return scheduleStatus;
    }


    @Override
    public String getPreProcessedSubTitle() {
        return subTitle;
    }

    @Override
    public String postProcess(String s) {
        return s;
    }

    @Override
    public String evaluateSubTitle() {
        if (StringUtils.isBlank(next_facility_visit_date))
            return null;

        return next_facility_visit_date;

    }

    @Override
    public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(next_facility_visit_date))
            return BasePmtctHomeVisitAction.Status.PENDING;
        else {
            return BasePmtctHomeVisitAction.Status.COMPLETED;
        }
    }

    @Override
    public void onPayloadReceived(BasePmtctHomeVisitAction basePmtctHomeVisitAction) {
        Timber.d("onPayloadReceived");
    }


}
