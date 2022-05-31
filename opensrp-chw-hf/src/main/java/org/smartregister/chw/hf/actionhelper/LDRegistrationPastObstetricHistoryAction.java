package org.smartregister.chw.hf.actionhelper;

import static org.smartregister.util.JsonFormUtils.VALUE;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.chw.referral.util.JsonFormConstants;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * @author ilakozejumanne@gmail.com
 * 30/05/2022
 */
public class LDRegistrationPastObstetricHistoryAction implements BaseLDVisitAction.LDVisitActionHelper {
    protected MemberObject memberObject;
    private Context context;
    private int para;
    private Boolean isFormComplete;

    public LDRegistrationPastObstetricHistoryAction(MemberObject memberObject, int para) {
        this.memberObject = memberObject;
        this.para = para;
    }

    @Override
    public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
        this.context = context;
    }

    @Override
    public String getPreProcessed() {
        JSONObject labourAndDeliveryPastObstetricHistoryForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryPastObstetricHistory());
        if (labourAndDeliveryPastObstetricHistoryForm != null) {
            try {
                JSONArray fields = labourAndDeliveryPastObstetricHistoryForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                JSONObject paraOneObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "para_1_obstetric_history");

                for (int i = 1; i < para; i++) {
                    JSONObject paraObject = new JSONObject(paraOneObject.toString());
                    paraObject.put("key", "para_" + (i + 1) + "_obstetric_history");
                    paraObject.put("openmrs_entity_id", "para_" + (i + 1) + "_obstetric_history");
                    paraObject.put("text", MessageFormat.format(context.getString(R.string.para_obstretric_history), i + 1));
                    fields.put(paraObject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return labourAndDeliveryPastObstetricHistoryForm.toString();
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            JSONArray fields = jsonObject.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);

            List<Boolean> expansionPanelsFilled = new ArrayList<Boolean>();
            for (int i = 0; i < fields.length(); i++) {
                JSONObject field = fields.getJSONObject(i);
                if (field.has(VALUE) && field.getJSONArray(VALUE).length() > 0) {
                    expansionPanelsFilled.add(true);
                }
            }
            if (expansionPanelsFilled.size() == fields.length()) {
                isFormComplete = true;
            } else if (expansionPanelsFilled.size() > 0 && expansionPanelsFilled.size() < fields.length()) {
                isFormComplete = false;
            } else if (expansionPanelsFilled.size() == 0) {
                isFormComplete = null;
            }
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
    public void onPayloadReceived(BaseLDVisitAction baseAncHomeVisitAction) {
        Timber.v("onPayloadReceived");
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (isFormComplete == null)
            return BaseLDVisitAction.Status.PENDING;
        else if (isFormComplete)
            return BaseLDVisitAction.Status.COMPLETED;
        else
            return BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
    }

    @Override
    public String evaluateSubTitle() {
        if (isFormComplete == null)
            return "";
        else if (isFormComplete)
            return context.getString(R.string.ld_registration_past_obstetric_history_complete);
        else
            return context.getString(R.string.ld_registration_past_obstetric_history_pending);
    }

}
