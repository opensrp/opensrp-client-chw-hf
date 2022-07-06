package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * @author ilakozejumanne@gmail.com
 * 06/05/2022
 */
public class LDRegistrationCurrentLabourAction implements BaseLDVisitAction.LDVisitActionHelper {
    protected MemberObject memberObject;
    private String labourOnsetDate;
    private String labourOnsetTime;
    private String rupturedMembrane;
    private String fetalMovement;
    private Context context;
    private String reasonsForAdmission;

    public void setReasonsForAdmission(String reasonsForAdmission) {
        this.reasonsForAdmission = reasonsForAdmission;
    }

    public LDRegistrationCurrentLabourAction(MemberObject memberObject) {
        this.memberObject = memberObject;
    }

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
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);

            if (reasonsForAdmission != null && (reasonsForAdmission.contains("elective_cesarean_section") || reasonsForAdmission.contains("induction"))) {
                labourOnsetDate = "none";
                labourOnsetTime = "none";
            } else {
                labourOnsetDate = CoreJsonFormUtils.getValue(jsonObject, "labour_onset_date");
                labourOnsetTime = CoreJsonFormUtils.getValue(jsonObject, "labour_onset_time");
            }
            rupturedMembrane = CoreJsonFormUtils.getValue(jsonObject, "membrane");
            fetalMovement = CoreJsonFormUtils.getValue(jsonObject, "fetal_movement");
        } catch (JSONException e) {
            Timber.e(e);
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
    public String postProcess(String s) {
        return null;
    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction baseAncHomeVisitAction) {
        Timber.v("onPayloadReceived");
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (isAllFieldsCompleted())
            return BaseLDVisitAction.Status.COMPLETED;
        else if (isAnyFieldCompleted())
            return BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
        else
            return BaseLDVisitAction.Status.PENDING;
    }

    @Override
    public String evaluateSubTitle() {
        if (isAllFieldsCompleted())
            return context.getString(R.string.ld_registration_current_labour_complete);
        else if (isAnyFieldCompleted())
            return context.getString(R.string.ld_registration_current_labour_pending);
        return "";
    }

    /**
     * evaluate if all fields are completed
     **/
    private boolean isAllFieldsCompleted() {
        return !StringUtils.isBlank(labourOnsetDate) && !StringUtils.isBlank(labourOnsetTime) && !StringUtils.isBlank(rupturedMembrane) && !StringUtils.isBlank(fetalMovement);
    }

    /**
     * evaluate if any field has been completed
     **/
    private boolean isAnyFieldCompleted() {
        return !StringUtils.isBlank(labourOnsetDate) || !StringUtils.isBlank(labourOnsetTime) || !StringUtils.isBlank(rupturedMembrane) || !StringUtils.isBlank(fetalMovement);

    }

}
