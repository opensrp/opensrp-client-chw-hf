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

public class AncObstetricExaminationAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
    protected MemberObject memberObject;
    private HashMap<String, Boolean> checkObject = new HashMap<>();
    private Context context;

    public AncObstetricExaminationAction(MemberObject memberObject) {
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
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            int gestAge = Integer.parseInt(CoreJsonFormUtils.getValue(jsonObject, "gest_age"));
            String abdominalContour = CoreJsonFormUtils.getValue(jsonObject, "abdominal_contour");
            boolean abdominalContourCheck = !(abdominalContour.equalsIgnoreCase("Abdominal Contour") || abdominalContour.equalsIgnoreCase("Umbo la Tumbo"));

            String lie = CoreJsonFormUtils.getValue(jsonObject, "lie");
            boolean lieCheck = StringUtils.isNotBlank(lie) && !(lie.equalsIgnoreCase("Lie") || lie.equalsIgnoreCase("Mlalo wa mtoto tumboni"));

            String presentation = CoreJsonFormUtils.getValue(jsonObject, "presentation");
            boolean presentationCheck = StringUtils.isNotBlank(presentation) && !(presentation.equalsIgnoreCase("Presentation") || presentation.equalsIgnoreCase("Kitangulizi cha mtoto"));

            checkObject.clear();
            checkObject.put("weight", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "weight")));
            checkObject.put("height", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "height")));
            checkObject.put("systolic", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "systolic")));
            checkObject.put("diastolic", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "diastolic")));
            checkObject.put("pulse_rate", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "pulse_rate")));
            checkObject.put("temperature", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "temperature")));
            checkObject.put("abdominal_scars", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "abdominal_scars")));
            checkObject.put("abdominal_movement_with_respiration", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "abdominal_movement_with_respiration")));
            checkObject.put("abdominal_contour", abdominalContourCheck);
            checkObject.put("abnormal_vaginal_discharge", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "abnormal_vaginal_discharge")));
            checkObject.put("vaginal_sores", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "vaginal_sores")));
            checkObject.put("vaginal_swelling", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "vaginal_swelling")));

            if (gestAge >= 20) {
                checkObject.put("fundal_height", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "fundal_height")));
                checkObject.put("fetal_heart_rate", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "fetal_heart_rate")));
            }
            if (gestAge > 35) {
                checkObject.put("lie", lieCheck);
                if (lie.contains("longitudinal")) {
                    checkObject.put("presentation", presentationCheck);
                }
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
        return null;
    }

    @Override
    public String getPreProcessedSubTitle() {
        return null;
    }

    @Override
    public String postProcess(String jsonPayload) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonPayload);
            JSONArray fields = JsonFormUtils.fields(jsonObject);
            JSONObject obstetric_examination_completion_status = JsonFormUtils.getFieldJSONObject(fields, "obstetric_examination_completion_status");
            assert obstetric_examination_completion_status != null;
            obstetric_examination_completion_status.put(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE, VisitUtils.getActionStatus(checkObject));
        } catch (JSONException e) {
            Timber.e(e);
        }

        if (jsonObject != null) {
            return jsonObject.toString();
        }
        return null;
    }

    @Override
    public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
        Timber.v("onPayloadReceived");
    }

    @Override
    public String evaluateSubTitle() {
        String status = VisitUtils.getActionStatus(checkObject);
        if (status.equalsIgnoreCase(VisitUtils.Complete))
            return context.getString(R.string.obstetric_exam_complete);
        return "";
    }
}
