package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class AncMedicalAndSurgicalHistoryAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
    protected MemberObject memberObject;
    private String jsonPayload;

    private String anc_medical_surgical_history;
    private BaseAncHomeVisitAction.ScheduleStatus scheduleStatus;
    private String subTitle;

    public AncMedicalAndSurgicalHistoryAction(MemberObject memberObject) {
        this.memberObject = memberObject;
    }

    @Override
    public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
        this.jsonPayload = jsonPayload;
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
            anc_medical_surgical_history = CoreJsonFormUtils.getValue(jsonObject, "anc_medical_surgical_history");
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
        if (StringUtils.isBlank(anc_medical_surgical_history))
            return null;

        StringBuilder stringBuilder = new StringBuilder();

        //TODO ilakoze extract to string recources
        stringBuilder.append("Medical And Surgical History Filled");

        return stringBuilder.toString();
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(anc_medical_surgical_history))
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
