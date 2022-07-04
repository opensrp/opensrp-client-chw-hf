package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-07-04
 */
public class PostDeliveryFamilyPlanningActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private String family_planning_counselling_after_delivery;
    private String family_planning_methods_selected;
    private String completionStatus;
    private Context context;

    @Override
    public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
        this.context = context;
    }

    @Override
    public String getPreProcessed() {
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        family_planning_counselling_after_delivery = JsonFormUtils.getFieldValue(jsonPayload, "family_planning_counselling_after_delivery");
        family_planning_methods_selected = JsonFormUtils.getFieldValue(jsonPayload, "family_planning_methods_selected");
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
        JSONObject jsonObject = null;

        try {

            jsonObject = new JSONObject(jsonPayload);
            JSONArray fields = JsonFormUtils.fields(jsonObject);

            JSONObject family_planning_module_status = JsonFormUtils.getFieldJSONObject(fields, "family_planning_module_status");
            if (family_planning_module_status != null) {
                family_planning_module_status.remove(JsonFormConstants.VALUE);
                family_planning_module_status.put(JsonFormConstants.VALUE, completionStatus);
            }

            return jsonObject.toString();

        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }

    @Override
    public String evaluateSubTitle() {
        if (isFullyCompleted()) {
            completionStatus = context.getString(R.string.lb_fully_completed_action);
        } else if (isPartiallyCompleted()) {
            completionStatus = context.getString(R.string.lb_partially_completed_action);
        }
        return completionStatus;
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (isFullyCompleted()) {
            return BaseLDVisitAction.Status.COMPLETED;
        } else if (isPartiallyCompleted()) {
            return BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
        } else {
            return BaseLDVisitAction.Status.PENDING;
        }
    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction baseLDVisitAction) {

    }

    private boolean isFullyCompleted() {
        return StringUtils.isNotBlank(family_planning_counselling_after_delivery) && StringUtils.isNotBlank(family_planning_methods_selected);
    }

    private boolean isPartiallyCompleted() {
        return StringUtils.isNotBlank(family_planning_counselling_after_delivery) || StringUtils.isNotBlank(family_planning_methods_selected);
    }
}
