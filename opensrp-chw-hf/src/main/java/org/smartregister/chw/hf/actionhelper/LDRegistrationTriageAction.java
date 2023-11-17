package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.HfAncJsonFormUtils;
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
public class LDRegistrationTriageAction implements BaseLDVisitAction.LDVisitActionHelper {
    protected MemberObject memberObject;
    private String systolic;
    private String diastolic;
    private String pulseRate;
    private String respiratoryRate;
    private String fetalHeartRate;
    private String temperature;
    private String weight;
    private String dangerSigns;
    private Context context;

    public LDRegistrationTriageAction(MemberObject memberObject) {
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
            systolic = CoreJsonFormUtils.getValue(jsonObject, "systolic");
            diastolic = CoreJsonFormUtils.getValue(jsonObject, "diastolic");
            pulseRate = CoreJsonFormUtils.getValue(jsonObject, "pulse_rate");
            respiratoryRate = CoreJsonFormUtils.getValue(jsonObject, "respiratory_rate");
            fetalHeartRate = CoreJsonFormUtils.getValue(jsonObject, "fetal_heart_rate");
            temperature = CoreJsonFormUtils.getValue(jsonObject, "temperature");
            weight = CoreJsonFormUtils.getValue(jsonObject, "weight");
            dangerSigns = HfAncJsonFormUtils.getCheckBoxValue(jsonObject, "danger_signs");
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
            return context.getString(R.string.ld_registration_triage_action_subtitle_complete);
        else if (isAnyFieldCompleted())
            return context.getString(R.string.ld_registration_triage_action_subtitle_pending);
        return "";
    }

    /**
     * evaluate if all fields are completed
     **/
    public boolean isAllFieldsCompleted() {
        return !StringUtils.isBlank(systolic) &&
                !StringUtils.isBlank(diastolic) &&
                !StringUtils.isBlank(pulseRate) &&
                !StringUtils.isBlank(respiratoryRate) &&
                !StringUtils.isBlank(temperature) &&
                !StringUtils.isBlank(weight) &&
                !StringUtils.isBlank(dangerSigns);
    }

    /**
     * evaluate if any field is completed
     **/
    public boolean isAnyFieldCompleted() {
        return !StringUtils.isBlank(systolic) ||
                !StringUtils.isBlank(diastolic) ||
                !StringUtils.isBlank(pulseRate) ||
                !StringUtils.isBlank(respiratoryRate) ||
                !StringUtils.isBlank(fetalHeartRate) ||
                !StringUtils.isBlank(temperature) ||
                !StringUtils.isBlank(weight) ||
                !StringUtils.isBlank(dangerSigns);
    }
}
