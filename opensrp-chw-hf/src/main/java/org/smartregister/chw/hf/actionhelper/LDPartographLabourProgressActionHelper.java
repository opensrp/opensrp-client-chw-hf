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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    final private String currentPartographDate;
    final private String currentPartographTime;

    public LDPartographLabourProgressActionHelper(MemberObject memberObject, String currentPartographDate, String currentPartographTime) {
        this.memberObject = memberObject;
        this.currentPartographDate = currentPartographDate;
        this.currentPartographTime = currentPartographTime;
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

            setPatographAlerts(progressOfLabourForm);

            JSONArray fields = progressOfLabourForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
            populateLabourProgressForm(fields, memberObject.getBaseEntityId());
        } catch (JSONException e) {
            Timber.e(e);
        }

        return progressOfLabourForm.toString();
    }

    private void setPatographAlerts(JSONObject form){

        String cervixDilationAlertLimit = "";
        String cervixDilationActionLimit = "";
        String partographDuration = "";

        String concatPartographTimeAndDate = currentPartographDate + " " + currentPartographTime;
        Long firstPartographTime = LDDao.getPartographStartTime(memberObject.getBaseEntityId());

        if (firstPartographTime == null)
            return;

        long currentPartographTimestamp;

        try {

            Date parseDate = (new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())).parse(concatPartographTimeAndDate);
            currentPartographTimestamp = parseDate.getTime();

            // Calucalte time difference
            // in milliseconds
            long timeDifference = currentPartographTimestamp - firstPartographTime;
            int hoursDifference = (int) TimeUnit.MILLISECONDS.toHours(timeDifference);

            partographDuration = String.valueOf(hoursDifference);

            int alertLimit = hoursDifference + 3; // 3 is the cervix dilation value when partograph begins
            int actionLimit = hoursDifference >= 1 ? alertLimit - 4 : 0;

            cervixDilationAlertLimit = String.valueOf(alertLimit);
            cervixDilationActionLimit = String.valueOf(actionLimit);

        }catch (ParseException e){
            Timber.e(e);
        }

        try {
            form.getJSONObject("global").put("cervix_dilation_alert_limit", cervixDilationAlertLimit);
            form.getJSONObject("global").put("cervix_dilation_action_limit", cervixDilationActionLimit);
            form.getJSONObject("global").put("partograph_duration", partographDuration);
        }catch (Exception e){
            Timber.e(e);
        }
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

            //Limit number of selectors
            int cervixDilationValue = Integer.parseInt(LDDao.getCervixDilation(baseEntityId));
            int numberOfSelectors = 10-cervixDilationValue;
            cervixDilation.put("number_of_selectors", numberOfSelectors);
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
