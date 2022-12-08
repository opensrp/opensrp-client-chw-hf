package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by Ilakoze Jumanne on 2022-10-25
 */
public class LDBloodGroupTestActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private final Context context;
    private String blood_group;

    public LDBloodGroupTestActionHelper(Context context) {
        this.context = context;
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        //Todo: Implement here
    }

    @Override
    public String getPreProcessed() {
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        blood_group = JsonFormUtils.getFieldValue(jsonPayload, "blood_group");
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
        return null;
    }

    @Override
    public String evaluateSubTitle() {
        if (isCompleted()) {
            return context.getString(R.string.lb_fully_completed_action);
        } else if (isPartiallyCompleted()) {
            return context.getString(R.string.lb_partially_completed_action);
        }
        return "";
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (isCompleted()) {
            return BaseLDVisitAction.Status.COMPLETED;
        } else {
            return isPartiallyCompleted() ? BaseLDVisitAction.Status.PARTIALLY_COMPLETED : BaseLDVisitAction.Status.PENDING;
        }

    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {
        //Todo: Implement
    }

    private boolean isCompleted() {
        boolean actionCompleted;
        actionCompleted = StringUtils.isNotBlank(blood_group);
        return actionCompleted;
    }

    private boolean isPartiallyCompleted() {
        return (StringUtils.isNotBlank(blood_group));
    }
}
