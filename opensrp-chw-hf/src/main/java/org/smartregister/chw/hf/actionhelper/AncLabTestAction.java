package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.VisitUtils;
import org.smartregister.family.util.JsonFormUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class AncLabTestAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
    protected MemberObject memberObject;
    private String jsonPayload;

    private HashMap<String, Boolean> checkObject = new HashMap<>();
    private BaseAncHomeVisitAction.ScheduleStatus scheduleStatus;
    private String subTitle;
    private Context context;

    public AncLabTestAction(MemberObject memberObject) {
        this.memberObject = memberObject;
    }

    @Override
    public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
        this.jsonPayload = jsonPayload;
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
            JSONObject global = jsonObject.getJSONObject("global");
            boolean bloodGroupComplete = global.getBoolean("blood_group_complete");
            boolean hivTestComplete = global.getBoolean("hiv_test_complete");

            String bloodGroup = CoreJsonFormUtils.getValue(jsonObject, "blood_group");
            boolean bloodGroupCheck = StringUtils.isNotBlank(bloodGroup) && !(bloodGroup.equalsIgnoreCase("Blood Group") || bloodGroup.equalsIgnoreCase("Kundi la damu"));

            String hivStatus = global.getString("hiv_status");
            int gestAge = global.getInt("gestational_age");
            boolean hivTestAt32Complete = global.getBoolean("hiv_test_at_32_complete");
            boolean syphilisTestComplete = global.getBoolean("syphilis_test_complete");
            boolean hepatitisTestComplete = global.getBoolean("hepatitis_test_complete");
            checkObject.clear();
            checkObject.put("hb_level_test", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "hb_level_test")));
            checkObject.put("blood_for_glucose_test", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "blood_for_glucose_test")));
            checkObject.put("glucose_in_urine", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "glucose_in_urine")));
            checkObject.put("protein_in_urine", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "protein_in_urine")));
            if(!bloodGroupComplete){
                checkObject.put("blood_group", bloodGroupCheck);
                if(!bloodGroup.contains("test_not_conducted")){
                    checkObject.put("rh_factor", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "rh_factor")));
                }
            }
            if(!hivTestComplete || ((gestAge >= 32 && hivStatus.contains("negative")) && !hivTestAt32Complete)){
                checkObject.put("hiv", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "hiv")));
            }
            if(!syphilisTestComplete){
                checkObject.put("syphilis", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "syphilis")));
            }
            if(!hepatitisTestComplete){
                checkObject.put("hepatitis", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "hepatitis")));
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
        return scheduleStatus;
    }

    @Override
    public String getPreProcessedSubTitle() {
        return subTitle;
    }

    @Override
    public String postProcess(String jsonPayload) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonPayload);
            JSONArray fields = JsonFormUtils.fields(jsonObject);
            JSONObject labTestCompletionStatus = JsonFormUtils.getFieldJSONObject(fields, "lab_test_completion_status");
            assert labTestCompletionStatus != null;
            labTestCompletionStatus.put(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE, VisitUtils.getActionStatus(checkObject));
        } catch (JSONException e) {
            Timber.e(e);
        }

        if (jsonObject != null) {
            return jsonObject.toString();
        }
        return null;
    }

    @Override
    public String evaluateSubTitle() {
        String status = VisitUtils.getActionStatus(checkObject);
        if (status.equalsIgnoreCase(VisitUtils.Complete))
            return context.getString(R.string.lab_tests_complete);
        return "";
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        String status = VisitUtils.getActionStatus(checkObject);
        if (status.equalsIgnoreCase(VisitUtils.Complete)) {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        }
        if (status.equalsIgnoreCase(VisitUtils.Ongoing)) {
            return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
        }
        return BaseAncHomeVisitAction.Status.PENDING;
    }

    @Override
    public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
        Timber.d("onPayloadReceived");
    }
}
