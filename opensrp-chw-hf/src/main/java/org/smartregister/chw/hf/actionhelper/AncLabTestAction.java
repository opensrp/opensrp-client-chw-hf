package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class AncLabTestAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
    protected MemberObject memberObject;
    private String jsonPayload;

    private String lab_tests;
    private BaseAncHomeVisitAction.ScheduleStatus scheduleStatus;
    private String subTitle;

    public AncLabTestAction(MemberObject memberObject) {
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
            lab_tests = CoreJsonFormUtils.getCheckBoxValue(jsonObject, "lab_tests");
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
        if (StringUtils.isBlank(lab_tests))
            return null;

        //TODO ilakoze extract to string resources
        return MessageFormat.format("Lab Tests Conducted : {0}", lab_tests);
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(lab_tests))
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
