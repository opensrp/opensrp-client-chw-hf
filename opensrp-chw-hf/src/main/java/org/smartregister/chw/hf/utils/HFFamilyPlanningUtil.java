package org.smartregister.chw.hf.utils;

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
            // Set the FP update event type
            JSONObject formObject = new JSONObject(jsonString);
            formObject.put(JsonFormUtils.ENCOUNTER_TYPE, CoreConstants.EventType.FAMILY_PLANNING_UPDATE);
            AllSharedPreferences allSharedPreferences = Utils.getAllSharedPreferences();
            Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, CoreReferralUtils.setEntityId(formObject.toString(), baseEntityId), CoreConstants.TABLE_NAME.FAMILY_PLANNING_UPDATE);
            org.smartregister.chw.anc.util.JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
            String syncLocationId = ChwNotificationDao.getSyncLocationId(baseEvent.getBaseEntityId());
            if (syncLocationId != null) {
                // Allows setting the ID for sync purposes
                baseEvent.setLocationId(syncLocationId);
            }

            NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(JsonFormUtils.gson.toJson(baseEvent)));
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }
}
