package org.smartregister.chw.hf.actionhelper;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.LDDao;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.chw.referral.util.JsonFormConstants;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * @author issyzac 5/7/22
 */
public class LDPartographFetalWellBeingActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    protected MemberObject memberObject;
    private String fetalHeartRate;
    private String amnioticFluid;
    private String caput;
    private String mouldingOptions;
    private Context context;
    final private String baseEntityId;

    public LDPartographFetalWellBeingActionHelper(MemberObject memberObject, String baseEntityId) {
        this.memberObject = memberObject;
        this.baseEntityId = baseEntityId;
    }

    @Override
    public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public String getPreProcessed() {
        JSONObject fetalWellBeingForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LabourAndDeliveryPartograph.getFetalWellBingForm());
        if (fetalWellBeingForm != null) {
            try {
                fetalWellBeingForm.getJSONObject("global").put("moulding", LDDao.getMoulding(baseEntityId) == null ? "" : LDDao.getMoulding(baseEntityId));
                JSONArray fields = fetalWellBeingForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);

                JSONObject mouldingOptions = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "moulding_options");

                if (LDDao.getMoulding(baseEntityId) != null && LDDao.getMoulding(baseEntityId).equalsIgnoreCase("yes")) {
                    mouldingOptions.getJSONArray("options").remove(0);
                }

                JSONObject amnioticFluid = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "amniotic_fluid");

                if (LDDao.getAmnioticFluidState(baseEntityId) != null && !LDDao.getAmnioticFluidState(baseEntityId).equalsIgnoreCase("membrane_intact")) {
                    amnioticFluid.getJSONArray("options").remove(0);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return fetalWellBeingForm.toString();
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            fetalHeartRate = CoreJsonFormUtils.getValue(jsonObject, "fetal_heart_rate");
            caput = CoreJsonFormUtils.getValue(jsonObject, "caput");
            mouldingOptions = CoreJsonFormUtils.getValue(jsonObject, "moulding_options");
            amnioticFluid = CoreJsonFormUtils.getValue(jsonObject, "amniotic_fluid");
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

    private boolean allFieldsCompleted() {
        return StringUtils.isNotBlank(fetalHeartRate) &&
                StringUtils.isNotBlank(amnioticFluid);
    }

    private boolean anyFieldCompleted() {
        return StringUtils.isNotBlank(fetalHeartRate) ||
                StringUtils.isNotBlank(caput) ||
                StringUtils.isNotBlank(mouldingOptions) ||
                StringUtils.isNotBlank(amnioticFluid);
    }
}
