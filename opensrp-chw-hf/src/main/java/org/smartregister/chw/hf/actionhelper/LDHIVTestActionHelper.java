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
 * Created by Kassim Sheghembe on 2022-05-17
 */
public class LDHIVTestActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private final Context context;
    private String hiv_test_conducted;
    private String hiv_counselling_before_testing;
    private String hiv;
    private String hiv_counselling_after_testing;

    public LDHIVTestActionHelper(Context context) {
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
        hiv_test_conducted = JsonFormUtils.getFieldValue(jsonPayload, "hiv_test_conducted");
        hiv_counselling_before_testing = JsonFormUtils.getFieldValue(jsonPayload, "hiv_counselling_before_testing");
        hiv = JsonFormUtils.getFieldValue(jsonPayload, "hiv");
        hiv_counselling_after_testing = JsonFormUtils.getFieldValue(jsonPayload, "hiv_counselling_after_testing");
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
        if (StringUtils.isNotBlank(hiv_test_conducted) && hiv_test_conducted.equalsIgnoreCase("no")) {
            actionCompleted = true;
        } else {
            actionCompleted = (StringUtils.isNotBlank(hiv_test_conducted) &&
                    StringUtils.isNotBlank(hiv_counselling_before_testing) &&
                    StringUtils.isNotBlank(hiv) &&
                    StringUtils.isNotBlank(hiv_counselling_after_testing));
        }
        return actionCompleted;
    }

    private boolean isPartiallyCompleted() {
        return (StringUtils.isNotBlank(hiv_test_conducted) ||
                StringUtils.isNotBlank(hiv_counselling_before_testing) ||
                StringUtils.isNotBlank(hiv) ||
                StringUtils.isNotBlank(hiv_counselling_after_testing));
    }
}
