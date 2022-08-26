package org.smartregister.chw.hf.actionhelper;

import static org.smartregister.chw.hf.utils.Constants.HIV_STATUS.POSITIVE;
import static org.smartregister.chw.hf.utils.JsonFormUtils.ENCOUNTER_TYPE;
import static org.smartregister.util.JsonFormUtils.FIELDS;
import static org.smartregister.util.JsonFormUtils.KEY;
import static org.smartregister.util.JsonFormUtils.STEP1;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.LDDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-07-06
 */
public class NewBornActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private final String baseEntityId;
    private Context context;
    private String deliveryDate;
    private String deliveryTime;
    private int numberOfChildrenBorn;
    private String status;
    private String childNumber;

    private String newbornStatus;
    private String still_birth_choice;
    private String sex;
    private String apgar_activity_score_at_1_minute;
    private String apgar_pulse_score_at_1_minute;
    private String apgar_grimace_on_stimulation_score_at_1_minute;
    private String apgar_appearance_score_at_1_minute;
    private String apgar_respiration_score_at_1_minute;
    private String apgar_activity_score_at_5_minutes;
    private String apgar_pulse_score_at_5_minutes;
    private String apgar_grimace_on_stimulation_score_at_5_minutes;
    private String apgar_appearance_score_at_5_minutes;
    private String apgar_respiration_score_at_5_minutes;
    private String resuscitation;
    private String temperature;
    private String weight;
    private String heart_rate;
    private String keep_warm;
    private String respiratory_rate;
    private String cord_bleeding;
    private String early_bf_1hr;
    private String reason_for_not_breast_feeding_within_one_hour;
    private String eye_care;
    private String child_bcg_vaccination;
    private String child_opv0_vaccination;
    private String risk_category;
    private String provided_azt_nvp_syrup;
    private String provided_other_combinations;
    private String specify_the_combinations;
    private String number_of_azt_nvp_days_dispensed;
    private String reason_for_not_providing_other_combination;
    private String collect_dbs;
    private String reason_not_collecting_dbs;
    private String sample_collection_date;
    private String dna_pcr_collection_time;
    private String sample_id;
    private String provided_nvp_syrup;
    private String number_of_nvp_days_dispensed;
    private String reason_for_not_providing_nvp_syrup;
    private String other_reason_for_not_providing_nvp_syrup;

    private String moduleCompletionStatus;

    public NewBornActionHelper(String baseEntityId, String deliveryDate, String deliveryTime, int numberOfChildrenBorn, String status, String childNumber) {
        this.baseEntityId = baseEntityId;
        this.deliveryDate = deliveryDate;
        this.deliveryTime = deliveryTime;
        this.numberOfChildrenBorn = numberOfChildrenBorn;
        this.status = status;
        this.childNumber = childNumber;
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public String getPreProcessed() {
        JSONObject newBornForm = org.smartregister.chw.core.utils.FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LDPostDeliveryMotherManagement.getLdNewBornStatus());
        String hivStatus = LDDao.getHivStatus(baseEntityId);

        try {
            newBornForm.getJSONObject("global").put("delivery_date", deliveryDate);
            newBornForm.getJSONObject("global").put("delivery_time", deliveryTime);
            newBornForm.getJSONObject("global").put("number_of_children_born", numberOfChildrenBorn);
            newBornForm.getJSONObject("global").put("child_number", childNumber);
        } catch (Exception e) {
            Timber.e(e);
        }

        JSONArray fields = null;
        try {
            fields = newBornForm.getJSONObject(STEP1).getJSONArray(FIELDS);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (fields != null && hivStatus != null && !hivStatus.equalsIgnoreCase(POSITIVE)) {
            try {
                for (int x = 0; x < fields.length(); x++) {
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("risk_category"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("prompt_for_high_risk"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("prompt_for_low_risk"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("provided_azt_nvp_syrup"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("provided_other_combinations"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("specify_the_combinations"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("number_of_azt_nvp_days_dispensed"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reason_for_not_providing_other_combination"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("other_reason_for_not_providing_other_combination"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("collect_dbs"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reason_not_collecting_dbs"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("sample_id"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("sample_collection_date"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("dna_pcr_collection_time"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("provided_nvp_syrup"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("number_of_nvp_days_dispensed"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reason_for_not_providing_nvp_syrup"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("other_reason_for_not_providing_nvp_syrup"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("prophylaxis_arv_for_high_risk_given"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("prophylaxis_arv_for_high_and_low_risk_given"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("visit_number"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("next_facility_visit_date"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("followup_visit_date"))
                        fields.remove(x);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reason_for_not_breast_feeding_within_one_hour") && status.equals("died")) {
                        fields.getJSONObject(x).getJSONArray("options").remove(0);
                    }
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reasons_for_not_keeping_the_baby_warm_skin_to_skin_for_normal_apgar_score") && status.equals("died")) {
                        fields.getJSONObject(x).getJSONArray("options").remove(0);
                    }
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reasons_for_not_keeping_the_baby_warm_skin_to_skin_for_low_apgar_score") && status.equals("died")) {
                        fields.getJSONObject(x).getJSONArray("options").remove(0);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (fields != null && hivStatus != null && hivStatus.equalsIgnoreCase(POSITIVE)) {
            try {
                for (int x = 0; x < fields.length(); x++) {
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("sample_collection_date"))
                        fields.getJSONObject(x).put("min_date", deliveryDate);
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reason_for_not_breast_feeding_within_one_hour") && status.equals("died")) {
                        fields.getJSONObject(x).getJSONArray("options").remove(0);
                    }
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reasons_for_not_keeping_the_baby_warm_skin_to_skin_for_normal_apgar_score") && status.equals("died")) {
                        fields.getJSONObject(x).getJSONArray("options").remove(0);
                    }
                    if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reasons_for_not_keeping_the_baby_warm_skin_to_skin_for_low_apgar_score") && status.equals("died")) {
                        fields.getJSONObject(x).getJSONArray("options").remove(0);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newBornForm.toString();
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        newbornStatus = JsonFormUtils.getFieldValue(jsonPayload, "newborn_status");
        still_birth_choice = JsonFormUtils.getFieldValue(jsonPayload, "still_birth_choice");
        sex = JsonFormUtils.getFieldValue(jsonPayload, "sex");
        apgar_activity_score_at_1_minute = JsonFormUtils.getFieldValue(jsonPayload, "apgar_activity_score_at_1_minute");
        apgar_pulse_score_at_1_minute = JsonFormUtils.getFieldValue(jsonPayload, "apgar_pulse_score_at_1_minute");
        apgar_grimace_on_stimulation_score_at_1_minute = JsonFormUtils.getFieldValue(jsonPayload, "apgar_grimace_on_stimulation_score_at_1_minute");
        apgar_appearance_score_at_1_minute = JsonFormUtils.getFieldValue(jsonPayload, "apgar_appearance_score_at_1_minute");
        apgar_respiration_score_at_1_minute = JsonFormUtils.getFieldValue(jsonPayload, "apgar_respiration_score_at_1_minute");
        apgar_activity_score_at_5_minutes = JsonFormUtils.getFieldValue(jsonPayload, "apgar_activity_score_at_5_minutes");
        apgar_pulse_score_at_5_minutes = JsonFormUtils.getFieldValue(jsonPayload, "apgar_pulse_score_at_5_minutes");
        apgar_grimace_on_stimulation_score_at_5_minutes = JsonFormUtils.getFieldValue(jsonPayload, "apgar_grimace_on_stimulation_score_at_5_minutes");
        apgar_appearance_score_at_5_minutes = JsonFormUtils.getFieldValue(jsonPayload, "apgar_appearance_score_at_5_minutes");
        apgar_respiration_score_at_5_minutes = JsonFormUtils.getFieldValue(jsonPayload, "apgar_respiration_score_at_5_minutes");
        resuscitation = JsonFormUtils.getFieldValue(jsonPayload, "resuscitation");
        temperature = JsonFormUtils.getFieldValue(jsonPayload, "temperature");
        weight = JsonFormUtils.getFieldValue(jsonPayload, "weight");
        heart_rate = JsonFormUtils.getFieldValue(jsonPayload, "heart_rate");
        keep_warm = JsonFormUtils.getFieldValue(jsonPayload, "keep_warm");
        respiratory_rate = JsonFormUtils.getFieldValue(jsonPayload, "respiratory_rate");
        cord_bleeding = JsonFormUtils.getFieldValue(jsonPayload, "cord_bleeding");
        early_bf_1hr = JsonFormUtils.getFieldValue(jsonPayload, "early_bf_1hr");
        reason_for_not_breast_feeding_within_one_hour = JsonFormUtils.getFieldValue(jsonPayload, "reason_for_not_breast_feeding_within_one_hour");
        eye_care = JsonFormUtils.getFieldValue(jsonPayload, "eye_care");
        child_bcg_vaccination = JsonFormUtils.getFieldValue(jsonPayload, "child_bcg_vaccination");
        child_opv0_vaccination = JsonFormUtils.getFieldValue(jsonPayload, "child_opv0_vaccination");
        risk_category = JsonFormUtils.getFieldValue(jsonPayload, "risk_category");
        provided_azt_nvp_syrup = JsonFormUtils.getFieldValue(jsonPayload, "provided_azt_nvp_syrup");
        provided_other_combinations = JsonFormUtils.getFieldValue(jsonPayload, "provided_other_combinations");
        specify_the_combinations = JsonFormUtils.getFieldValue(jsonPayload, "specify_the_combinations");
        number_of_azt_nvp_days_dispensed = JsonFormUtils.getFieldValue(jsonPayload, "number_of_azt_nvp_days_dispensed");
        reason_for_not_providing_other_combination = JsonFormUtils.getFieldValue(jsonPayload, "reason_for_not_providing_other_combination");
        collect_dbs = JsonFormUtils.getFieldValue(jsonPayload, "collect_dbs");
        reason_not_collecting_dbs = JsonFormUtils.getFieldValue(jsonPayload, "reason_not_collecting_dbs");
        sample_collection_date = JsonFormUtils.getFieldValue(jsonPayload, "sample_collection_date");
        dna_pcr_collection_time = JsonFormUtils.getFieldValue(jsonPayload, "dna_pcr_collection_time");
        sample_id = JsonFormUtils.getFieldValue(jsonPayload, "sample_id");
        provided_nvp_syrup = JsonFormUtils.getFieldValue(jsonPayload, "provided_nvp_syrup");
        number_of_nvp_days_dispensed = JsonFormUtils.getFieldValue(jsonPayload, "number_of_nvp_days_dispensed");
        reason_for_not_providing_nvp_syrup = JsonFormUtils.getFieldValue(jsonPayload, "reason_for_not_providing_nvp_syrup");
        other_reason_for_not_providing_nvp_syrup = JsonFormUtils.getFieldValue(jsonPayload, "other_reason_for_not_providing_nvp_syrup");
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
            jsonObject.remove(ENCOUNTER_TYPE);
            jsonObject.putOpt(ENCOUNTER_TYPE, "LND " + childNumber + " Newborn");

            JSONArray fields = JsonFormUtils.fields(jsonObject);

            JSONObject newborn_stage_four_module_status = JsonFormUtils.getFieldJSONObject(fields, "newborn_stage_four_module_status");
            assert newborn_stage_four_module_status != null;
            newborn_stage_four_module_status.remove(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE);
            newborn_stage_four_module_status.put(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE, moduleCompletionStatus);

            return jsonObject.toString();
        } catch (JSONException e) {
            Timber.e(e);
        }
        return null;
    }

    @Override
    public String evaluateSubTitle() {
        if (StringUtils.isNotBlank(newbornStatus)) {
            if (isFullyCompleted()) {
                moduleCompletionStatus = context.getString(R.string.lb_fully_completed_action);
            } else if (isPartiallyCompleted()) {
                moduleCompletionStatus = context.getString(R.string.lb_partially_completed_action);
            }
            return moduleCompletionStatus;
        }
        return null;
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        BaseLDVisitAction.Status status = BaseLDVisitAction.Status.PENDING;
        if (StringUtils.isNotBlank(newbornStatus)) {
            if (isFullyCompleted()) {
                status = BaseLDVisitAction.Status.COMPLETED;
            } else if (isPartiallyCompleted()) {
                status = BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
            }
        }

        return status;
    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {
        //implement
    }

    private boolean isFullyCompleted() {
        if (newbornStatus.equalsIgnoreCase("alive")) {
            return allFirstLevelFieldsCompleted() && breastFeedingWithin1HourCompleted() && isRiskCategoryCompleted();
        } else {
            return StringUtils.isNotBlank(still_birth_choice);
        }
    }

    private boolean isPartiallyCompleted() {
        if (newbornStatus.equalsIgnoreCase("alive")) {
            return !allFirstLevelFieldsCompleted() || !breastFeedingWithin1HourCompleted() || !isRiskCategoryCompleted();
        } else {
            return StringUtils.isBlank(still_birth_choice);
        }
    }

    private boolean allFirstLevelFieldsCompleted() {

        return (StringUtils.isNotBlank(sex) && StringUtils.isNotBlank(apgar_activity_score_at_1_minute) &&
                StringUtils.isNotBlank(apgar_pulse_score_at_1_minute) && StringUtils.isNotBlank(apgar_grimace_on_stimulation_score_at_1_minute) &&
                StringUtils.isNotBlank(apgar_appearance_score_at_1_minute) && StringUtils.isNotBlank(apgar_respiration_score_at_1_minute) &&
                StringUtils.isNotBlank(apgar_activity_score_at_5_minutes) && StringUtils.isNotBlank(apgar_pulse_score_at_5_minutes) &&
                StringUtils.isNotBlank(apgar_grimace_on_stimulation_score_at_5_minutes) && StringUtils.isNotBlank(apgar_appearance_score_at_5_minutes) &&
                StringUtils.isNotBlank(apgar_respiration_score_at_5_minutes) && StringUtils.isNotBlank(resuscitation) &&
                StringUtils.isNotBlank(temperature) && StringUtils.isNotBlank(weight) &&
                StringUtils.isNotBlank(heart_rate) && StringUtils.isNotBlank(keep_warm) &&
                StringUtils.isNotBlank(respiratory_rate) && StringUtils.isNotBlank(cord_bleeding) &&
                StringUtils.isNotBlank(early_bf_1hr) && StringUtils.isNotBlank(eye_care) &&
                StringUtils.isNotBlank(child_bcg_vaccination) && StringUtils.isNotBlank(child_opv0_vaccination));

    }

    private boolean breastFeedingWithin1HourCompleted() {
        if (StringUtils.isNotBlank(early_bf_1hr)) {
            if (early_bf_1hr.equalsIgnoreCase("no")) {
                return StringUtils.isNotBlank(reason_for_not_breast_feeding_within_one_hour);
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean isRiskCategoryCompleted() {
        boolean riskCategoryCompletionStatus = false;

        String hivStatus = LDDao.getHivStatus(baseEntityId);
        if(!hivStatus.equalsIgnoreCase(POSITIVE))
            return true;

        if (StringUtils.isNotBlank(risk_category)) {
            if (risk_category.equalsIgnoreCase("high")) {
                boolean providedAztNvpSyrupCompletion = false;
                boolean collectDBSCompletion = false;
                if (StringUtils.isNotBlank(provided_azt_nvp_syrup)) {
                    if (provided_azt_nvp_syrup.equalsIgnoreCase("yes")) {
                        providedAztNvpSyrupCompletion = StringUtils.isNotBlank(number_of_azt_nvp_days_dispensed);
                    } else {
                        if (StringUtils.isNotBlank(provided_other_combinations)) {
                            if (provided_other_combinations.equalsIgnoreCase("yes")) {
                                providedAztNvpSyrupCompletion = StringUtils.isNotBlank(specify_the_combinations) && StringUtils.isNotBlank(number_of_azt_nvp_days_dispensed);
                            } else {
                                providedAztNvpSyrupCompletion = StringUtils.isNotBlank(reason_for_not_providing_other_combination);
                            }
                        }
                    }
                }

                if (StringUtils.isNotBlank(collect_dbs)) {
                    if (collect_dbs.equalsIgnoreCase("yes")) {
                        collectDBSCompletion = StringUtils.isNotBlank(dna_pcr_collection_time) && StringUtils.isNotBlank(sample_collection_date) && StringUtils.isNotBlank(sample_id);
                    } else {
                        collectDBSCompletion = StringUtils.isNotBlank(reason_not_collecting_dbs);
                    }
                }
                riskCategoryCompletionStatus = providedAztNvpSyrupCompletion && collectDBSCompletion;
            } else {
                if (StringUtils.isNotBlank(provided_nvp_syrup)) {
                    if (provided_nvp_syrup.equalsIgnoreCase("yes")) {
                        riskCategoryCompletionStatus = StringUtils.isNotBlank(number_of_nvp_days_dispensed);
                    } else {
                        riskCategoryCompletionStatus = (StringUtils.isNotBlank(reason_for_not_providing_nvp_syrup) &&
                                !reason_for_not_providing_nvp_syrup.equalsIgnoreCase("other")) ||
                                (StringUtils.isNotBlank(reason_for_not_providing_nvp_syrup) &&
                                        reason_for_not_providing_nvp_syrup.equalsIgnoreCase("other") &&
                                        StringUtils.isNotBlank(other_reason_for_not_providing_nvp_syrup));
                    }
                }
            }
        }
        return riskCategoryCompletionStatus;
    }

}
