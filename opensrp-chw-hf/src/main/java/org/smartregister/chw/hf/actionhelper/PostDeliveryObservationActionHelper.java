package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

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
 * Created by Kassim Sheghembe on 2022-07-06
 */
public class PostDeliveryObservationActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private String vagina_observation;
    private String vaginal_bleeding_observation;
    private String perineum_observation;
    private String degree_of_perineum_tear;
    private String perineum_repair_occupation;
    private String perineum_repair_person_name;
    private String cervix_observation;
    private String systolic;
    private String diastolic;
    private String pulse_rate;
    private String temperature;
    private String uterus_contraction;
    private String urination;
    private String observation_date;
    private String observation_time;
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
        vagina_observation = JsonFormUtils.getFieldValue(jsonPayload, "vagina_observation");
        vaginal_bleeding_observation = JsonFormUtils.getFieldValue(jsonPayload, "vaginal_bleeding_observation");
        perineum_observation = JsonFormUtils.getFieldValue(jsonPayload, "perineum_observation");
        degree_of_perineum_tear = JsonFormUtils.getFieldValue(jsonPayload, "degree_of_perineum_tear");
        perineum_repair_occupation = JsonFormUtils.getFieldValue(jsonPayload, "perineum_repair_occupation");
        perineum_repair_person_name = JsonFormUtils.getFieldValue(jsonPayload, "perineum_repair_person_name");
        cervix_observation = JsonFormUtils.getFieldValue(jsonPayload, "cervix_observation");
        systolic = JsonFormUtils.getFieldValue(jsonPayload, "systolic");
        diastolic = JsonFormUtils.getFieldValue(jsonPayload, "diastolic");
        pulse_rate = JsonFormUtils.getFieldValue(jsonPayload, "pulse_rate");
        temperature = JsonFormUtils.getFieldValue(jsonPayload, "temperature");
        uterus_contraction = JsonFormUtils.getFieldValue(jsonPayload, "uterus_contraction");
        urination = JsonFormUtils.getFieldValue(jsonPayload, "urination");
        observation_date = JsonFormUtils.getFieldValue(jsonPayload, "observation_date");
        observation_time = JsonFormUtils.getFieldValue(jsonPayload, "observation_time");
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

            JSONObject mother_observation_module_status = JsonFormUtils.getFieldJSONObject(fields, "mother_observation_module_status");
            if (mother_observation_module_status != null) {
                mother_observation_module_status.remove(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE);
                mother_observation_module_status.put(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE, completionStatus);
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
    public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {
        //Todo: Implement here
    }

    private boolean isFullyCompleted() {
        boolean complete = false;
        if (StringUtils.isNotBlank(vagina_observation) && StringUtils.isNotBlank(perineum_observation) &&
                StringUtils.isNotBlank(cervix_observation) && StringUtils.isNotBlank(systolic) &&
                StringUtils.isNotBlank(diastolic) && StringUtils.isNotBlank(pulse_rate) &&
                StringUtils.isNotBlank(temperature) && StringUtils.isNotBlank(uterus_contraction) &&
                StringUtils.isNotBlank(urination) && StringUtils.isNotBlank(observation_date) && StringUtils.isNotBlank(observation_time)) {
            complete = true;
            if (vagina_observation.contains("chk_bleeding") && perineum_observation.contains("tear")) {
                complete = StringUtils.isNotBlank(vaginal_bleeding_observation) && StringUtils.isNotBlank(degree_of_perineum_tear) &&
                        (StringUtils.isNotBlank(perineum_repair_occupation) && !perineum_repair_occupation.contains("Perineum repaired by")) && StringUtils.isNotBlank(perineum_repair_person_name);
            } else if (vagina_observation.contains("chk_bleeding")) {
                complete = StringUtils.isNotBlank(vaginal_bleeding_observation);
            } else if (perineum_observation.contains("tear")) {
                complete = StringUtils.isNotBlank(degree_of_perineum_tear) && StringUtils.isNotBlank(perineum_repair_occupation) &&
                        StringUtils.isNotBlank(perineum_repair_person_name);
            }
        }

        return complete;
    }

    private boolean isPartiallyCompleted() {
        boolean partialCompletion = false;
        if (StringUtils.isNotBlank(vagina_observation)) {
            if (StringUtils.isBlank(perineum_observation) ||
                    StringUtils.isBlank(cervix_observation) || StringUtils.isBlank(systolic) ||
                    StringUtils.isBlank(diastolic) || StringUtils.isBlank(pulse_rate) ||
                    StringUtils.isBlank(temperature) || StringUtils.isBlank(uterus_contraction) ||
                    StringUtils.isBlank(urination) || StringUtils.isBlank(observation_date) || StringUtils.isBlank(observation_time)) {
                partialCompletion = true;
            } else {
                if (vagina_observation.contains("chk_bleeding") && perineum_observation.contains("tear")) {
                    partialCompletion = StringUtils.isBlank(vaginal_bleeding_observation) || StringUtils.isBlank(degree_of_perineum_tear) ||
                            (StringUtils.isNotBlank(perineum_repair_occupation) && perineum_repair_occupation.contains("Perineum repaired by")) ||
                            StringUtils.isBlank(perineum_repair_person_name);
                } else if (vagina_observation.contains("chk_bleeding")) {
                    partialCompletion = StringUtils.isBlank(vaginal_bleeding_observation);
                } else if (perineum_observation.contains("tear")) {
                    partialCompletion = StringUtils.isBlank(degree_of_perineum_tear) || (StringUtils.isNotBlank(perineum_repair_occupation) &&
                            perineum_repair_occupation.contains("Perineum repaired by")) || StringUtils.isBlank(perineum_repair_person_name);
                }
            }
        }
        return partialCompletion;
    }
}
