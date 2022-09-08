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
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.VisitUtils;
import org.smartregister.chw.referral.util.JsonFormConstants;
import org.smartregister.family.util.JsonFormUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class AncBaselineInvestigationAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
    protected MemberObject memberObject;
    private HashMap<String, Boolean> checkObject = new HashMap<>();
    private Context context;
    private boolean isClientOnART;

    public AncBaselineInvestigationAction(MemberObject memberObject, boolean isClientOnART) {
        this.memberObject = memberObject;
        this.isClientOnART = isClientOnART;
    }

    @Override
    public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
        this.context = context;
    }

    @Override
    public String getPreProcessed() {
        JSONObject baselineInvestigationForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.AncFirstVisit.getBaselineInvestigation());
        try {
            baselineInvestigationForm.getJSONObject("global").put("gestational_age", memberObject.getGestationAge());
            baselineInvestigationForm.getJSONObject("global").put("known_positive", isClientOnART);
            JSONArray fields = baselineInvestigationForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
            JSONObject hivTestNumberField = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "hiv_test_number");
            if (hivTestNumberField != null) {
                hivTestNumberField.put(JsonFormUtils.VALUE, HfAncDao.getNextHivTestNumber(memberObject.getBaseEntityId()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return baselineInvestigationForm.toString();
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            JSONObject global = jsonObject.getJSONObject("global");
            checkObject.clear();
            boolean isKnownPositive = global.getBoolean("known_positive");

            String bloodGroup = CoreJsonFormUtils.getValue(jsonObject, "blood_group");
            boolean bloodGroupCheck = StringUtils.isNotBlank(bloodGroup) && !(bloodGroup.equalsIgnoreCase("Blood Group") || bloodGroup.equalsIgnoreCase("Kundi la damu"));

            checkObject.put("glucose_in_urine", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "glucose_in_urine")));
            checkObject.put("protein_in_urine", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "protein_in_urine")));
            checkObject.put("blood_group", bloodGroupCheck);
            if (!bloodGroup.equalsIgnoreCase("test_not_conducted"))
                checkObject.put("rh_factor", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "rh_factor")));
            checkObject.put("hb_level_test", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "hb_level_test")));
            checkObject.put("blood_for_glucose_test", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "blood_for_glucose_test")));
            checkObject.put("syphilis", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "syphilis")));
            checkObject.put("hepatitis", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "hepatitis")));
            checkObject.put("other_stds", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "other_stds")));

            if (!isKnownPositive) {
                checkObject.put("hiv_qn", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "hiv_qn")));
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
            JSONObject baselineInvestigationCompletionStatus = JsonFormUtils.getFieldJSONObject(fields, "baseline_investigation_completion_status");
            assert baselineInvestigationCompletionStatus != null;
            baselineInvestigationCompletionStatus.put(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE, VisitUtils.getActionStatus(checkObject));
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
    public String evaluateSubTitle() {
        String status = VisitUtils.getActionStatus(checkObject);
        if (status.equalsIgnoreCase(VisitUtils.Complete))
            return context.getString(R.string.baseline_investigation_conducted);
        return "";
    }

}
