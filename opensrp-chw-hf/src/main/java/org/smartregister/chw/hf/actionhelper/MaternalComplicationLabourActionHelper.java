package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.HfAncJsonFormUtils;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-07-06
 */
public class MaternalComplicationLabourActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private String maternal_complications_before_delivery;
    private String maternal_complications_during_and_after_delivery;
    private String completionStatus;
    private Context context;

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        this.context = context;
    }

    @Override
    public String getPreProcessed() {
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            maternal_complications_before_delivery = HfAncJsonFormUtils.getCheckBoxValue(jsonObject, "maternal_complications_before_delivery");
            maternal_complications_during_and_after_delivery = HfAncJsonFormUtils.getCheckBoxValue(jsonObject, "maternal_complications_during_and_after_delivery");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BaseLDVisitAction.ScheduleStatus getPreProcessedStatus() {
        return null;
    }

    @Override
    public String getPreProcessedSubTitle() {
        return null;
    }

    @Override
    public String postProcess(String jsonPayload) {
        try {

            JSONObject jsonObject = new JSONObject(jsonPayload);
            JSONArray fields = JsonFormUtils.fields(jsonObject);

            JSONObject maternal_complications_module_status = JsonFormUtils.getFieldJSONObject(fields, "maternal_complications_module_status");
            assert maternal_complications_module_status != null;
            maternal_complications_module_status.remove(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE);
            maternal_complications_module_status.put(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE, completionStatus);

            return jsonObject.toString();

        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

    @Override
    public String evaluateSubTitle() {
        if (StringUtils.isNotBlank(maternal_complications_before_delivery) && StringUtils.isNotBlank(maternal_complications_during_and_after_delivery)) {
            completionStatus = context.getString(R.string.lb_fully_completed_action);
        } else if (StringUtils.isNotBlank(maternal_complications_before_delivery) || StringUtils.isNotBlank(maternal_complications_during_and_after_delivery)) {
            completionStatus = context.getString(R.string.lb_partially_completed_action);
        }
        return completionStatus;
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isNotBlank(maternal_complications_before_delivery) && StringUtils.isNotBlank(maternal_complications_during_and_after_delivery))
            return BaseLDVisitAction.Status.COMPLETED;
        else if (StringUtils.isNotBlank(maternal_complications_before_delivery) || StringUtils.isNotBlank(maternal_complications_during_and_after_delivery))
            return BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
        else
            return BaseLDVisitAction.Status.PENDING;
    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {
        //Todo: Implement here
    }
}
