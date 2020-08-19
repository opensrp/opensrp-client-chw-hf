package org.smartregister.chw.hf.model;

import android.util.Pair;

import org.json.JSONObject;
import org.smartregister.chw.core.model.CoreChildRegisterModel;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.utils.JsonFormUtils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.dao.LocationsDao;
import org.smartregister.family.util.Utils;

import java.util.Collections;

import static org.smartregister.AllConstants.LocationConstants.SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS;
import static org.smartregister.chw.hf.utils.JsonFormUtils.SYNC_LOCATION_ID;
import static org.smartregister.util.JsonFormUtils.STEP1;

public class ChildRegisterModel extends CoreChildRegisterModel {

    @Override
    public Pair<Client, Event> processRegistration(String jsonString) {
        return JsonFormUtils.processChildRegistrationForm(Utils.context().allSharedPreferences(), jsonString);
    }

    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId, String familyID) throws Exception {
        JSONObject form = getFormUtils().getFormJson(formName);
        if (form == null) {
            return null;
        }
        JSONObject syncLocationField = CoreJsonFormUtils.getJsonField(form, STEP1, SYNC_LOCATION_ID);
        CoreJsonFormUtils.addLocationsToDropdownField(LocationsDao.getLocationsByTags(
                Collections.singleton(SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS)), syncLocationField);
        return JsonFormUtils.getFormAsJson(form, formName, entityId, currentLocationId, familyID);
    }
}
