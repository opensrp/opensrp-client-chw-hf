package org.smartregister.chw.hf.actionhelper;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.ld.dao.LDDao;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.chw.referral.util.JsonFormConstants;
import org.smartregister.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by Kassim Sheghembe on 2022-05-09
 */
public class LDVaginalExaminationActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private final Context context;

    private String vaginal_exam_date;
    private String vaginal_exam_time;
    private String cervix_state;
    private String cervix_dilation;
    private String presenting_part;
    private String moulding;
    private String station;
    private String decision;
    private final String baseEntityId;

    public LDVaginalExaminationActionHelper(Context context, String baseEntityId) {
        this.context = context;
        this.baseEntityId = baseEntityId;
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public String getPreProcessed() {
        JSONObject vaginalExaminationForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LDVisit.getLdVaginalExamination());
        if (vaginalExaminationForm != null) {
            try {
                JSONArray fields = vaginalExaminationForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                populateVaginalExaminationForm(fields, baseEntityId);

                if (LDDao.getLabourOnsetDate(baseEntityId) != null) {
                    vaginalExaminationForm.getJSONObject("global").put("labour_onset_date", LDDao.getLabourOnsetDate(baseEntityId));
                }

                if (LDDao.getLabourOnsetTime(baseEntityId) != null) {
                    vaginalExaminationForm.getJSONObject("global").put("labour_onset_time", LDDao.getLabourOnsetTime(baseEntityId));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            vaginalExaminationForm.getJSONObject("global").put("moulding", LDDao.getMoulding(baseEntityId) == null ? "" : LDDao.getMoulding(baseEntityId));
            JSONArray fields = vaginalExaminationForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);

            JSONObject amnioticFluid = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "amniotic_fluid");

            if (org.smartregister.chw.hf.utils.LDDao.getMembraneState(baseEntityId) != null && org.smartregister.chw.hf.utils.LDDao.getMembraneState(baseEntityId).equalsIgnoreCase("ruptured")) {
                amnioticFluid.getJSONArray("options").remove(0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return vaginalExaminationForm.toString();
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        vaginal_exam_date = JsonFormUtils.getFieldValue(jsonPayload, "vaginal_exam_date");
        vaginal_exam_time = JsonFormUtils.getFieldValue(jsonPayload, "vaginal_exam_time");
        cervix_state = JsonFormUtils.getFieldValue(jsonPayload, "cervix_state");
        cervix_dilation = JsonFormUtils.getFieldValue(jsonPayload, "cervix_dilation");
        presenting_part = JsonFormUtils.getFieldValue(jsonPayload, "presenting_part");

        if (LDDao.getMoulding(baseEntityId) != null && !LDDao.getMoulding(baseEntityId).equalsIgnoreCase("yes"))
            moulding = JsonFormUtils.getFieldValue(jsonPayload, "moulding");
        else
            moulding = "no";
        station = JsonFormUtils.getFieldValue(jsonPayload, "station");
        decision = JsonFormUtils.getFieldValue(jsonPayload, "decision");
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
    public String postProcess(String jsonPayload) {
        return null;
    }

    @Override
    public String evaluateSubTitle() {
        if (isFullyCompleted()) {
            return context.getString(R.string.lb_fully_completed_action);
        } else if (isPartiallyCompleted()) {
            return context.getString(R.string.lb_partially_completed_action);
        } else {
            return "";
        }
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (isFullyCompleted()) {
            return BaseLDVisitAction.Status.COMPLETED;
        } else if (isPartiallyCompleted()) {
            return BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
        } else {
            return BaseLDVisitAction.Status.PENDING;
        }
    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {

    }

    private boolean isFullyCompleted() {
        return (StringUtils.isNotBlank(vaginal_exam_date) &&
                StringUtils.isNotBlank(vaginal_exam_time) &&
                StringUtils.isNotBlank(cervix_state) &&
                StringUtils.isNotBlank(cervix_dilation) &&
                (StringUtils.isNotBlank(presenting_part) && !presenting_part.equalsIgnoreCase("Presenting part")) &&
                (StringUtils.isNotBlank(moulding)) &&
                StringUtils.isNotBlank(station) &&
                StringUtils.isNotBlank(decision)
        );
    }

    private boolean isPartiallyCompleted() {
        return (StringUtils.isNotBlank(vaginal_exam_date) ||
                StringUtils.isNotBlank(vaginal_exam_time) ||
                StringUtils.isNotBlank(cervix_state) ||
                StringUtils.isNotBlank(cervix_dilation) ||
                (StringUtils.isNotBlank(presenting_part) && !presenting_part.equalsIgnoreCase("Presenting part")) ||
                StringUtils.isNotBlank(moulding) ||
                StringUtils.isNotBlank(station) ||
                StringUtils.isNotBlank(decision)
        );
    }

    private void populateVaginalExaminationForm(JSONArray fields, String baseEntityId) throws JSONException {
        JSONObject vaginalExamDate = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "vaginal_exam_date");

        if (LDDao.getVaginalExaminationDate(baseEntityId) != null) {
            vaginalExamDate.put("min_date", LDDao.getVaginalExaminationDate(baseEntityId));
        } else if (LDDao.getLabourOnsetDate(baseEntityId) != null) {
            vaginalExamDate.put("min_date", LDDao.getLabourOnsetDate(baseEntityId));
        }
    }
}
