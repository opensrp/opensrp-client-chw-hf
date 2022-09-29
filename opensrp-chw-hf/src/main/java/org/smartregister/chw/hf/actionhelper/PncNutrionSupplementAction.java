package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.HfPncDao;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PncNutrionSupplementAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
    protected MemberObject memberObject;
    private String jsonPayload;

    private String iron_and_folic_acid;
    private BaseAncHomeVisitAction.ScheduleStatus scheduleStatus;
    private String subTitle;
    private Context context;

    public PncNutrionSupplementAction(MemberObject memberObject) {
        this.memberObject = memberObject;
    }

    @Override
    public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
        this.jsonPayload = jsonPayload;
        this.context = context;
    }

    @Override
    public String getPreProcessed() {
        JSONObject nutritionSupplementForm = null;
        JSONObject global = null;
        try {
            nutritionSupplementForm = new JSONObject(jsonPayload);
            global = nutritionSupplementForm.getJSONObject("global");
            global.put("is_eligible_for_vitamin_a", HfPncDao.isMotherEligibleForVitaminA(memberObject.getBaseEntityId()));
            return nutritionSupplementForm.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            iron_and_folic_acid = CoreJsonFormUtils.getValue(jsonObject, "iron_and_folic_acid");
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
    public String postProcess(String s) {
        return s;
    }

    @Override
    public String evaluateSubTitle() {
        if (StringUtils.isBlank(iron_and_folic_acid))
            return null;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getString(R.string.nutritional_supplements_complete));

        return stringBuilder.toString();
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(iron_and_folic_acid))
            return BaseAncHomeVisitAction.Status.PENDING;
        else {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        }
    }

    @Override
    public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
        Timber.d("onPayloadReceived");
    }
}
