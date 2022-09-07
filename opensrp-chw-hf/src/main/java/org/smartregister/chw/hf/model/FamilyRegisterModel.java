package org.smartregister.chw.hf.model;

import static org.smartregister.AllConstants.LocationConstants.SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS;
import static org.smartregister.chw.hf.utils.JsonFormUtils.SYNC_LOCATION_ID;
import static org.smartregister.util.JsonFormUtils.STEP1;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.dao.LocationsDao;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.model.BaseFamilyRegisterModel;

import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class FamilyRegisterModel extends BaseFamilyRegisterModel {

    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject form = super.getFormAsJson(formName, entityId, currentLocationId);
        JSONObject syncLocationField = CoreJsonFormUtils.getJsonField(form, STEP1, SYNC_LOCATION_ID);
        CoreJsonFormUtils.addLocationsToDropdownField(LocationsDao.getLocationsByTags(
                Collections.singleton(SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS)), syncLocationField);
        return form;
    }

    @Override
    public List<FamilyEventClient> processRegistration(String jsonString) {
        List<FamilyEventClient> familyEventClients = super.processRegistration(jsonString);
        try {
            JSONObject syncLocationField = CoreJsonFormUtils.getJsonField(new JSONObject(jsonString), STEP1,  SYNC_LOCATION_ID);
            for (FamilyEventClient familyEventClient: familyEventClients){
                familyEventClient.getEvent().setLocationId(CoreJsonFormUtils.getSyncLocationUUIDFromDropdown(syncLocationField));
            }
        } catch (JSONException e) {
            Timber.e(e, "Error retrieving Sync location Field");
        }

        return familyEventClients;
    }
}
