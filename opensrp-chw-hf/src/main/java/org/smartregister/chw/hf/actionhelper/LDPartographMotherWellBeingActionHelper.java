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
public class LDPartographMotherWellBeingActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    protected MemberObject memberObject;
    private String pulseRate;
    private String respiratoryRate;
    private String temperature;
    private String systolic;
    private String diastolic;
    private String urineProtein;
    private String urineAcetone;
    private String urineVolume;
    private Context context;

    public LDPartographMotherWellBeingActionHelper(MemberObject memberObject) {
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
            pulseRate = CoreJsonFormUtils.getValue(jsonObject, "pulse_rate");
            respiratoryRate = CoreJsonFormUtils.getValue(jsonObject, "respiratory_rate");
            temperature = CoreJsonFormUtils.getValue(jsonObject, "temperature");
            systolic = CoreJsonFormUtils.getValue(jsonObject, "systolic");
            diastolic = CoreJsonFormUtils.getValue(jsonObject, "diastolic");
            urineProtein = CoreJsonFormUtils.getValue(jsonObject, "urine_protein");
            urineAcetone = CoreJsonFormUtils.getValue(jsonObject, "urine_acetone");
            urineVolume = CoreJsonFormUtils.getValue(jsonObject, "urine_volume");
        } catch (JSONException e) {
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
            return context.getString(R.string.ld_partograph_mother_wellbeing_completed);
        else if (anyFieldCompleted())
            return context.getString(R.string.ld_partograph_mother_wellbeing_pending);
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

    private boolean allFieldsCompleted() {
        return StringUtils.isNotBlank(pulseRate) &&
                StringUtils.isNotBlank(respiratoryRate) &&
                StringUtils.isNotBlank(temperature) &&
                StringUtils.isNotBlank(systolic) &&
                StringUtils.isNotBlank(diastolic) &&
                StringUtils.isNotBlank(urineProtein) &&
                StringUtils.isNotBlank(urineAcetone) &&
                StringUtils.isNotBlank(urineVolume);
    }

    private boolean anyFieldCompleted() {
        return StringUtils.isNotBlank(pulseRate) ||
                StringUtils.isNotBlank(respiratoryRate) ||
                StringUtils.isNotBlank(temperature) ||
                StringUtils.isNotBlank(systolic) ||
                StringUtils.isNotBlank(diastolic) ||
                StringUtils.isNotBlank(urineProtein) ||
                StringUtils.isNotBlank(urineAcetone) ||
                StringUtils.isNotBlank(urineVolume);
    }
}
