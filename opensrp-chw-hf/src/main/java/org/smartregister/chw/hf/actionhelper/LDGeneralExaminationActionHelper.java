package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-05-09
 */
public class LDGeneralExaminationActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private String general_condition;
    private String pulse_rate;
    private String respiratory_rate;
    private String temperature;
    private String systolic;
    private String diastolic;
    private String urine;
    private String fundal_height;
    private String presentation;
    private String contraction_in_ten_minutes;
    private String fetal_heart_rate;
    private final Context context;

    public LDGeneralExaminationActionHelper(Context context) {
        this.context = context;
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
    }

    @Override
    public String getPreProcessed() {
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        general_condition = JsonFormUtils.getFieldValue(jsonPayload, "general_condition");
        pulse_rate = JsonFormUtils.getFieldValue(jsonPayload, "pulse_rate");
        respiratory_rate =JsonFormUtils.getFieldValue(jsonPayload, "respiratory_rate");
        temperature=JsonFormUtils.getFieldValue(jsonPayload, "temperature");
        systolic=JsonFormUtils.getFieldValue(jsonPayload, "systolic");
        diastolic=JsonFormUtils.getFieldValue(jsonPayload, "diastolic");
        urine=JsonFormUtils.getFieldValue(jsonPayload, "urine");
        fundal_height=JsonFormUtils.getFieldValue(jsonPayload, "fundal_height");
        presentation=JsonFormUtils.getFieldValue(jsonPayload, "presentation");
        contraction_in_ten_minutes=JsonFormUtils.getFieldValue(jsonPayload, "contraction_in_ten_minutes");
        fetal_heart_rate=JsonFormUtils.getFieldValue(jsonPayload, "fetal_heart_rate");
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
        if (isFullyCompleted()) {
            return context.getString(R.string.lb_fully_completed_action);
        } else if (isPartiallyCompleted()) {
            return context.getString(R.string.lb_partially_completed_action);
        }
        return null;
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (isFullyCompleted()) {
            return BaseLDVisitAction.Status.COMPLETED;
        } else if (isPartiallyCompleted()) {
            return BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
        } else {
            return BaseLDVisitAction.Status.COMPLETED;
        }
    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {

    }

    private boolean isFullyCompleted() {
        return (StringUtils.isNotBlank(general_condition) &&
                StringUtils.isNotBlank(pulse_rate) &&
                StringUtils.isNotBlank(respiratory_rate) &&
                StringUtils.isNotBlank(temperature) &&
                StringUtils.isNotBlank(systolic) &&
                StringUtils.isNotBlank(diastolic) &&
                StringUtils.isNotBlank(urine) &&
                StringUtils.isNotBlank(fundal_height) &&
                StringUtils.isNotBlank(presentation) &&
                StringUtils.isNotBlank(contraction_in_ten_minutes) &&
                StringUtils.isNotBlank(fetal_heart_rate)
                );
    }

    private boolean isPartiallyCompleted() {
        return (StringUtils.isBlank(general_condition) ||
                StringUtils.isBlank(pulse_rate) ||
                StringUtils.isBlank(respiratory_rate) ||
                StringUtils.isBlank(temperature) ||
                StringUtils.isBlank(systolic) ||
                StringUtils.isBlank(diastolic) ||
                StringUtils.isBlank(urine) ||
                StringUtils.isBlank(fundal_height) ||
                StringUtils.isBlank(presentation) ||
                StringUtils.isBlank(contraction_in_ten_minutes) ||
                StringUtils.isBlank(fetal_heart_rate)
                );
    }
}
