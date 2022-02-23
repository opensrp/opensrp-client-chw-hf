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

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class AncBaselineInvestigationAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
    protected MemberObject memberObject;
    private String glucose_in_urine;
    private Context context;

    public AncBaselineInvestigationAction(MemberObject memberObject) {
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
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            glucose_in_urine = CoreJsonFormUtils.getValue(jsonObject, "glucose_in_urine");
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
    public String postProcess(String s) {
        return null;
    }

    @Override
    public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
        Timber.v("onPayloadReceived");
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(glucose_in_urine))
            return BaseAncHomeVisitAction.Status.PENDING;
        else {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        }
    }

    @Override
    public String evaluateSubTitle() {
        if (!StringUtils.isBlank(glucose_in_urine))
            return context.getString(R.string.baseline_investigation_conducted);
        return "";
    }
}
