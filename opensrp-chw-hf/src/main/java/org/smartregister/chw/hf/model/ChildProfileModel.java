package org.smartregister.chw.hf.model;

import org.json.JSONObject;
import org.smartregister.chw.core.model.CoreChildProfileModel;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.dao.LocationsDao;

import java.util.Collections;

import static org.smartregister.AllConstants.LocationConstants.SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS;
import static org.smartregister.chw.hf.utils.JsonFormUtils.SYNC_LOCATION_ID;
import static org.smartregister.util.JsonFormUtils.STEP1;

public class ChildProfileModel extends CoreChildProfileModel {

    public ChildProfileModel(String familyName) {
        super(familyName);
    }

    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId, String familyID) throws Exception {
        JSONObject form = super.getFormAsJson(formName, entityId, currentLocationId, familyID);
        JSONObject syncLocationField = CoreJsonFormUtils.getJsonField(form, STEP1, SYNC_LOCATION_ID);
        CoreJsonFormUtils.addLocationsToDropdownField(LocationsDao.getLocationsByTags(
                Collections.singleton(SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS)), syncLocationField);
        return form;
    }
}
