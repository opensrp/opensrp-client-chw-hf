package org.smartregister.chw.hf.actionhelper.prep;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.kvp.dao.KvpDao;
import org.smartregister.chw.kvp.domain.VisitDetail;
import org.smartregister.chw.kvp.model.BaseKvpVisitAction;
import org.smartregister.family.util.JsonFormUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class PrEPInitiationActionHelper implements BaseKvpVisitAction.KvpVisitActionHelper {

    private String prep_status;
    private String jsonPayload;
    private String baseEntityId;
    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    public PrEPInitiationActionHelper(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    @Override
    public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
        this.jsonPayload = jsonPayload;
    }

    @Override
    public String getPreProcessed() {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            JSONArray fields = jsonObject.getJSONObject(org.smartregister.chw.hf.utils.Constants.JsonFormConstants.STEP1).getJSONArray(org.smartregister.chw.referral.util.JsonFormConstants.FIELDS);
            JSONObject prepPillsNumber = JsonFormUtils.getFieldJSONObject(fields, "prep_pills_number");


            String enrollmentDateString = KvpDao.getPrepEnrollmentDate(baseEntityId);
            if (enrollmentDateString != null) {
                Date enrollmentDate = df.parse(enrollmentDateString);
                if (enrollmentDate != null) {
                    Date threeMonthsAgo = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90));
                    if (enrollmentDate.before(threeMonthsAgo)) {
                        prepPillsNumber.remove("v_max");
                    }
                }
            }
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
            prep_status = CoreJsonFormUtils.getValue(jsonObject, "prep_status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BaseKvpVisitAction.ScheduleStatus getPreProcessedStatus() {
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
        return null;
    }

    @Override
    public BaseKvpVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(prep_status))
            return BaseKvpVisitAction.Status.PENDING;
        else {
            return BaseKvpVisitAction.Status.COMPLETED;
        }
    }

    @Override
    public void onPayloadReceived(BaseKvpVisitAction baseKvpVisitAction) {
        //overridden
    }
}
