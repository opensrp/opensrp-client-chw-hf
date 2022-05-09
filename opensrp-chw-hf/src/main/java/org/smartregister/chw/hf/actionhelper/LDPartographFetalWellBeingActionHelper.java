package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * @author issyzac 5/7/22
 */
public class LDPartographFetalWellBeingActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    protected MemberObject memberObject;
    private String fetalHeartRate;
    private String membrane;
    private String moulding;
    private Context context;

    public LDPartographFetalWellBeingActionHelper(MemberObject memberObject){
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
            fetalHeartRate = CoreJsonFormUtils.getValue(jsonObject, "fetal_heart_rate");
            moulding = CoreJsonFormUtils.getValue(jsonObject, "moulding");
            membrane = CoreJsonFormUtils.getValue(jsonObject, "moulding");
        }catch (JSONException e){
            Timber.e(e);
        }
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
    public String postProcess(String s) {
        return null;
    }

    @Override
    public String evaluateSubTitle() {
        if (allFieldsCompleted())
            return context.getString(R.string.ld_partograph_fetal_wellbeing_completed);
        else if (anyFieldCompleted())
            return context.getString(R.string.ld_partograph_fetal_wellbeing_pending);
        return "";
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (allFieldsCompleted())
            return BaseLDVisitAction.Status.COMPLETED;
        else if (anyFieldCompleted())
            return BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
        else
            return BaseLDVisitAction.Status.PENDING;
    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction baseLDVisitAction) {
        Timber.v("onPayloadReceived");
    }

    private boolean allFieldsCompleted(){
        return StringUtils.isNotBlank(fetalHeartRate) &&
                StringUtils.isNotBlank(moulding) &&
                StringUtils.isNotBlank(membrane);
    }

    private boolean anyFieldCompleted(){
        return StringUtils.isNotBlank(fetalHeartRate) ||
                StringUtils.isNotBlank(moulding) ||
                StringUtils.isNotBlank(membrane);
    }
}
