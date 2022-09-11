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

public class AncConsultationAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
    protected MemberObject memberObject;
    private String jsonPayload;

    private HashMap<String, Boolean> checkObject = new HashMap<>();
    private BaseAncHomeVisitAction.ScheduleStatus scheduleStatus;
    private String subTitle;
    private Context context;

    public AncConsultationAction(MemberObject memberObject) {
        this.memberObject = memberObject;
    }

    @Override
    public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
        this.jsonPayload = jsonPayload;
        this.context = context;
    }

    @Override
    public String getPreProcessed() {

        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            return jsonObject.toString();
        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            JSONObject global = jsonObject.getJSONObject("global");
            checkObject.clear();
            int clientAge = global.getInt("client_age");
            int gestAge = 12;
            try {
                gestAge = Integer.parseInt(CoreJsonFormUtils.getValue(jsonObject, "gest_age_consultation"));
            } catch (Exception e) {
                Timber.e(e);
            }

            String lie = CoreJsonFormUtils.getValue(jsonObject, "lie");
            boolean lieCheck = StringUtils.isNotBlank(lie) && !(lie.equalsIgnoreCase("Lie") || lie.equalsIgnoreCase("Mlalo wa mtoto tumboni"));

            String presentation = CoreJsonFormUtils.getValue(jsonObject, "presentation");
            boolean presentationCheck = StringUtils.isNotBlank(presentation) && !(presentation.equalsIgnoreCase("Presentation") || presentation.equalsIgnoreCase("Kitangulizi cha mtoto"));

            checkObject.put("examination_findings", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "examination_findings")));
            if (clientAge < 25) {
                checkObject.put("height", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "height")));
            }
            checkObject.put("weight", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "weight")));
            if (gestAge >= 20) {
                checkObject.put("fundal_height", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "fundal_height")));
                checkObject.put("fetal_heart_rate", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "fetal_heart_rate")));
            }
            if (gestAge >= 36) {
                checkObject.put("lie", lieCheck);
                if (lie.contains("longitudinal")) {
                    checkObject.put("presentation", presentationCheck);
                }
            }
            checkObject.put("systolic", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "systolic")));
            checkObject.put("diastolic", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "diastolic")));
            checkObject.put("pulse_rate", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "pulse_rate")));
            checkObject.put("temperature", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "temperature")));
            checkObject.put("breast", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "breast")));
            checkObject.put("lymph_node_under_arm", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "lymph_node_under_arm")));
            checkObject.put("lymph_node_cervical", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "lymph_node_cervical")));
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
            JSONObject consultationCompletionStatus = JsonFormUtils.getFieldJSONObject(fields, "consultation_completion_status");
            assert consultationCompletionStatus != null;
            consultationCompletionStatus.put(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE, VisitUtils.getActionStatus(checkObject));
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
            return context.getString(R.string.anc_consultation_done);
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
