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
public class LDPartographLabourProgressActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    protected MemberObject memberObject;
    private String cervixDilation;
    private String descentPresentingPart;
    private String contractionFrequency;
    private Context context;

    public LDPartographLabourProgressActionHelper(MemberObject memberObject) {
        this.memberObject = memberObject;
    }

    @Override
    public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public String getPreProcessed() {

        JSONObject progressOfLabourForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LabourAndDeliveryPartograph.getProgressOfLabourForm());
        try {
            JSONArray fields = progressOfLabourForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
            populateLabourProgressForm(fields, memberObject.getBaseEntityId());
        } catch (JSONException e) {
            Timber.e(e);
        }

        return progressOfLabourForm.toString();
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            cervixDilation = CoreJsonFormUtils.getValue(jsonObject, "cervix_dilation");
            descentPresentingPart = CoreJsonFormUtils.getValue(jsonObject, "descent_presenting_part");
            contractionFrequency = CoreJsonFormUtils.getValue(jsonObject, "contraction_every_half_hour_frequency");
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
            return context.getString(R.string.ld_partograph_labor_progress_completed);
        else if (anyFieldCompleted())
            return context.getString(R.string.ld_partograph_labor_progress_pending);
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
        return StringUtils.isNotBlank(cervixDilation) &&
                StringUtils.isNotBlank(descentPresentingPart) &&
                StringUtils.isNotBlank(contractionFrequency);
    }

    private boolean anyFieldCompleted() {
        return StringUtils.isNotBlank(cervixDilation) ||
                StringUtils.isNotBlank(descentPresentingPart) ||
                StringUtils.isNotBlank(contractionFrequency);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void populateLabourProgressForm(JSONArray fields, String baseEntityId) throws JSONException {
        JSONObject cervixDilation = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "cervix_dilation");
        JSONObject descentPresentingPart = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "descent_presenting_part");
        if (LDDao.getCervixDilation(baseEntityId) != null) {
            cervixDilation.put("start_number", LDDao.getCervixDilation(baseEntityId));
        }

        if (LDDao.getDescent(baseEntityId) != null && descentPresentingPart != null) {
            int descent = 5;
            try {
                descent = Integer.parseInt(LDDao.getDescent(baseEntityId));
            } catch (NumberFormatException e) {
                Timber.e(e);
            }

            if (descent == 0) {
                descentPresentingPart.getJSONArray("options").remove(4);
                descentPresentingPart.getJSONArray("options").remove(3);
                descentPresentingPart.getJSONArray("options").remove(2);
                descentPresentingPart.getJSONArray("options").remove(1);
                descentPresentingPart.getJSONArray("options").remove(0);
            }
            if (descent == 1) {
                descentPresentingPart.getJSONArray("options").remove(3);
                descentPresentingPart.getJSONArray("options").remove(2);
                descentPresentingPart.getJSONArray("options").remove(1);
                descentPresentingPart.getJSONArray("options").remove(0);
            }
            if (descent == 2) {
                descentPresentingPart.getJSONArray("options").remove(2);
                descentPresentingPart.getJSONArray("options").remove(1);
                descentPresentingPart.getJSONArray("options").remove(0);
            } else if (descent == 3) {
                descentPresentingPart.getJSONArray("options").remove(1);
                descentPresentingPart.getJSONArray("options").remove(0);
            } else if (descent == 4) {
                descentPresentingPart.getJSONArray("options").remove(0);
            }
        }
    }
}
