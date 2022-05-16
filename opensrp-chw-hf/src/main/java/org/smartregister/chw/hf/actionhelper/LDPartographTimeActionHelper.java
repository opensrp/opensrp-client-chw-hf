package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.ld.dao.LDDao;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.chw.referral.util.JsonFormConstants;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * @author issyzac 5/12/22
 */
public class LDPartographTimeActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private Context context;
    private String time;
    private String date;
    private final MemberObject memberObject;

    public LDPartographTimeActionHelper(MemberObject memberObject) {
        this.memberObject = memberObject;
    }

    @Override
    public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
        this.context = context;
    }

    @Override
    public String getPreProcessed() {
        JSONObject partographTimeForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LabourAndDeliveryPartograph.getPartographTimeForm());
        if (partographTimeForm != null) {
            try {

                String baseEntityId = memberObject.getBaseEntityId();

                JSONArray fields = partographTimeForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                populatePartograhDateTimeForm(fields, baseEntityId);


                String partographDate = null;
                String partographTime = null;
                if (LDDao.getPartographDate(baseEntityId) != null) {
                    partographDate = LDDao.getPartographDate(baseEntityId);
                } else if (LDDao.getVaginalExaminationDate(baseEntityId) != null) {
                    partographDate = LDDao.getVaginalExaminationDate(baseEntityId);
                } else if (LDDao.getLabourOnsetDate(baseEntityId) != null) {
                    partographDate = LDDao.getLabourOnsetDate(baseEntityId);
                }

                if (partographDate != null) {
                    partographTimeForm.getJSONObject("global").put("partograph_monitoring_date", partographDate);
                }

                if (LDDao.getPartographTime(baseEntityId) != null) {
                    partographTime = LDDao.getPartographTime(baseEntityId);
                } else if (LDDao.getVaginalExaminationTime(baseEntityId) != null) {
                    partographTime = LDDao.getVaginalExaminationTime(baseEntityId);
                } else if (LDDao.getLabourOnsetTime(baseEntityId) != null) {
                    partographTime = LDDao.getLabourOnsetTime(baseEntityId);
                }

                if (partographTime != null) {
                    partographTimeForm.getJSONObject("global").put("partograph_monitoring_time", partographTime);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return partographTimeForm.toString();
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            time = CoreJsonFormUtils.getValue(jsonObject, "partograph_time");
            date = CoreJsonFormUtils.getValue(jsonObject, "partograph_date");
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
        if (StringUtils.isNotBlank(time))
            return context.getString(R.string.partograph_time, time);
        else
            return "";
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(time) || StringUtils.isBlank(date))
            return BaseLDVisitAction.Status.PENDING;
        else
            return BaseLDVisitAction.Status.COMPLETED;
    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction baseLDVisitAction) {
        /*
         * TODO: Capture values after form was filled by the user
         */
    }

    private void populatePartograhDateTimeForm(JSONArray fields, String baseEntityId) throws JSONException {
        JSONObject partographDate = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "partograph_date");

        if (LDDao.getPartographDate(baseEntityId) != null) {
            partographDate.put("min_date", LDDao.getPartographDate(baseEntityId));
        } else if (LDDao.getVaginalExaminationDate(baseEntityId) != null) {
            partographDate.put("min_date", LDDao.getVaginalExaminationDate(baseEntityId));
        } else if (LDDao.getLabourOnsetDate(baseEntityId) != null) {
            partographDate.put("min_date", LDDao.getLabourOnsetDate(baseEntityId));
        }
    }
}
