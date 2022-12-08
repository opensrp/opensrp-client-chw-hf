package org.smartregister.chw.hf.actionhelper;

import static org.smartregister.opd.utils.OpdConstants.KEY.VALUE;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.LDDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.chw.referral.util.JsonFormConstants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * @author issyzac 5/12/22
 */
public class LDPartographTimeActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private Context context;
    private boolean editMode;
    protected String time;
    protected String date;
    private final MemberObject memberObject;
    private final DateFormat hourFormat = new SimpleDateFormat("HH:mm");
    private final DateFormat completeDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    public LDPartographTimeActionHelper(MemberObject memberObject, boolean editMode) {
        this.memberObject = memberObject;
        this.editMode = editMode;
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
                populatePartographHealthcareProviderNameForm(fields, baseEntityId);

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

                if (editMode && LDDao.getPreviousPartographTime(baseEntityId) != null) {
                    partographTime = LDDao.getPreviousPartographTime(baseEntityId);
                }else if (!editMode && LDDao.getPartographTime(baseEntityId) != null) {
                    partographTime = LDDao.getPartographTime(baseEntityId);
                } else if (LDDao.getVaginalExaminationTime(baseEntityId) != null) {
                    partographTime = LDDao.getVaginalExaminationTime(baseEntityId);
                } else if (LDDao.getLabourOnsetTime(baseEntityId) != null) {
                    partographTime = LDDao.getLabourOnsetTime(baseEntityId);
                }

                Date now = new Date();
                String partographLimit = "";

                try {

                    String completePartographDateString = partographDate + " " + partographTime;
                    Date partoDate = completeDateFormat.parse(completePartographDateString);

                    long lastPartographDiff = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - partoDate.getTime());

                    if (lastPartographDiff > 30) {
                        Date thirtyMinutesAgo = new Date(); //now
                        int thirtyMinutesLimit = -30;
                        thirtyMinutesAgo = DateUtils.addMinutes(thirtyMinutesAgo, thirtyMinutesLimit);
                        partographLimit = hourFormat.format(thirtyMinutesAgo);
                        partographTimeForm.getJSONObject("global").put("partograph_limit", partographLimit);
                    }

                } catch (Exception e) {
                    Timber.e(e);
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
        partographDate.put("value", new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime()));
    }

    private void populatePartographHealthcareProviderNameForm(JSONArray fields, String baseEntityId) throws JSONException {
        JSONObject nameOfTheHealthCareProviderJsonObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "name_of_the_health_care_provider");
        String nameOfHealthcareProvider = org.smartregister.chw.hf.dao.LDDao.getHealthcareProviderNameWhoConductedLastPartographSession(baseEntityId);

        if (nameOfHealthcareProvider != null) {
            nameOfTheHealthCareProviderJsonObject.put("editable", true);
            nameOfTheHealthCareProviderJsonObject.put("read_only", true);
            nameOfTheHealthCareProviderJsonObject.put(VALUE, nameOfHealthcareProvider);
        }
    }
}
