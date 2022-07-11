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
 * Author: issyzac on 2022-07-6
 */

public class LDHBTestActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private final Context context;
    private String hbTestConducted;

    public LDHBTestActionHelper(Context context) {
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
        hbTestConducted = JsonFormUtils.getFieldValue(jsonPayload, "hb_test_conducted");
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
        }
        return "";
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (isCompleted()) {
            return BaseLDVisitAction.Status.COMPLETED;
        } else {
            return BaseLDVisitAction.Status.PENDING;
        }

    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {
        //Todo: Implement
    }

    private boolean isCompleted() {
        return StringUtils.isNotBlank(hbTestConducted);
    }

}
