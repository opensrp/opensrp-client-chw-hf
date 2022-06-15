package org.smartregister.chw.hf.utils;

import android.app.Activity;

import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.activity.ReferralRegistrationActivity;

import timber.log.Timber;

public class LFTUFormUtils {
    public static void setLFTUClinic(JSONObject form, String key, String text) {
        try {
            JSONObject option = new JSONObject();
            option.put("name", key);
            option.put("text", text);
            option.put("meta_data", new JSONObject()
                    .put("openmrs_entity", "")
                    .put("openmrs_entity_id", key)
                    .put("openmrs_entity_parent", ""));
            JSONArray steps = form.getJSONArray("steps");
            JSONObject step = steps.getJSONObject(0);
            JSONArray fields = step.getJSONArray("fields");
            for (int i = 0; i < fields.length(); i++) {
                JSONObject field = fields.getJSONObject(i);
                if (field.getString("name").equals("problem")) {
                    field.getJSONArray("options").put(option);
                }
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    public static void startLTFUReferral(Activity context, String baseEntityId){
        JSONObject formJsonObject;
        try {
            formJsonObject = (new FormUtils()).getFormJsonFromRepositoryOrAssets(context, Constants.JsonForm.getLtfuReferralForm());
            if (formJsonObject != null) {
                formJsonObject.put(Constants.REFERRAL_TASK_FOCUS, Constants.FOCUS.LOST_TO_FOLLOWUP_FOCUS);
                ReferralRegistrationActivity.startGeneralReferralFormActivityForResults(context, baseEntityId, formJsonObject, false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
