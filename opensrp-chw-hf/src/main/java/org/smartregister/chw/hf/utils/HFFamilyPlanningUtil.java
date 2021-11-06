package org.smartregister.chw.hf.utils;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.dao.ChwNotificationDao;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreReferralUtils;
import org.smartregister.chw.fp.FpLibrary;
import org.smartregister.chw.fp.util.FpJsonFormUtils;
import org.smartregister.chw.fp.util.FpUtil;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.Utils;

import timber.log.Timber;

public class HFFamilyPlanningUtil extends FpUtil {

    public static void saveFormEvent(final String jsonString) throws Exception {
        AllSharedPreferences allSharedPreferences = FpLibrary.getInstance().context().allSharedPreferences();
        Event baseEvent = FpJsonFormUtils.processJsonForm(allSharedPreferences, jsonString);
        processEvent(allSharedPreferences, baseEvent);
        createFamilyPlanningUpdateEvent(jsonString, baseEvent.getBaseEntityId());
    }

    public static void createFamilyPlanningUpdateEvent(String jsonString, String baseEntityId) {
        try {
            // Set the FP update event type and update time for fp_reg
            JSONObject formObject = new JSONObject(jsonString);
            String currentEncounter = formObject.getString(JsonFormUtils.ENCOUNTER_TYPE);
            formObject.put(JsonFormUtils.ENCOUNTER_TYPE, CoreConstants.EventType.FAMILY_PLANNING_UPDATE);
            populateStartDateTime(formObject, currentEncounter);
            AllSharedPreferences allSharedPreferences = Utils.getAllSharedPreferences();
            Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, CoreReferralUtils.setEntityId(formObject.toString(), baseEntityId), CoreConstants.TABLE_NAME.FAMILY_PLANNING_UPDATE);
            org.smartregister.chw.anc.util.JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
            baseEvent.setLocationId(ChwNotificationDao.getSyncLocationId(baseEntityId));
            NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(JsonFormUtils.gson.toJson(baseEvent)));
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    public static void populateStartDateTime(@NotNull JSONObject jsonObject, String encounter) throws JSONException {
        String dateTimeString = new LocalDateTime().toString("yyyy-MM-dd HH:mm:ss");
        JSONObject jsonStepObject = jsonObject.getJSONObject("step1");
        JSONArray fieldsArray = jsonStepObject.getJSONArray(JsonFormConstants.FIELDS);
        String key = "fp_reg_date";
        if (("Family Planning Change Method").equals(encounter)) {
            key = "fp_change_or_stop_date";
        }
        JSONObject startDateObject = JsonFormUtils.getFieldJSONObject(fieldsArray, key);
        if (startDateObject != null)
            startDateObject.put(JsonFormUtils.VALUE, dateTimeString);
    }
}
