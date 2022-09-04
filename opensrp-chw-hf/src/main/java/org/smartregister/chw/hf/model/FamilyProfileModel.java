package org.smartregister.chw.hf.model;

import static org.smartregister.AllConstants.LocationConstants.SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS;
import static org.smartregister.chw.hf.utils.JsonFormUtils.SYNC_LOCATION_ID;
import static org.smartregister.util.JsonFormUtils.STEP1;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.dao.ChwNotificationDao;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.dao.LocationsDao;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.model.BaseFamilyProfileModel;

import java.util.Collections;

import timber.log.Timber;

public class FamilyProfileModel extends BaseFamilyProfileModel {

    public FamilyProfileModel(String familyName) {
        super(familyName);
    }

    @Override
    public void updateWra(FamilyEventClient familyEventClient) {
        FormUtils.updateWraForBA(familyEventClient);
    }

    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject form = super.getFormAsJson(formName, entityId, currentLocationId);
        JSONObject syncLocationField = CoreJsonFormUtils.getJsonField(form, STEP1, SYNC_LOCATION_ID);
        try {
            CoreJsonFormUtils.addLocationsToDropdownField(LocationsDao.getLocationsByTags(
                    Collections.singleton(SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS)), syncLocationField);
        }catch (JSONException e){
            Timber.e(e);
        }
        return form;
    }

    @Override
    public FamilyEventClient processFamilyRegistrationForm(String jsonString, String familyBaseEntityId) {
        FamilyEventClient familyEventClient = super.processFamilyRegistrationForm(jsonString, familyBaseEntityId);
        setChwLocationId(jsonString, familyEventClient);
        return familyEventClient;
    }

    @Override
    public FamilyEventClient processMemberRegistration(String jsonString, String familyBaseEntityId) {
        FamilyEventClient familyEventClient = super.processMemberRegistration(jsonString, familyBaseEntityId);
        setFamilyHeadSyncLocationId(familyBaseEntityId, familyEventClient);
        return familyEventClient;
    }

    @Override
    public FamilyEventClient processUpdateMemberRegistration(String jsonString, String familyBaseEntityId) {
        FamilyEventClient familyEventClient = super.processUpdateMemberRegistration(jsonString, familyBaseEntityId);
        setFamilyHeadSyncLocationId(familyBaseEntityId, familyEventClient);
        return familyEventClient;
    }


    private void setChwLocationId(String jsonString, FamilyEventClient familyEventClient) {
        try {
            JSONObject syncLocationField = CoreJsonFormUtils.getJsonField(new JSONObject(jsonString), STEP1, SYNC_LOCATION_ID);
            familyEventClient.getEvent().setLocationId(CoreJsonFormUtils.getSyncLocationUUIDFromDropdown(syncLocationField));
        } catch (JSONException e) {
            Timber.e(e, "Error retrieving Sync location Field");
        }
    }

    private void setFamilyHeadSyncLocationId(String familyBaseEntityId, FamilyEventClient familyEventClient) {
        familyEventClient.getEvent().setLocationId(ChwNotificationDao.getFamilyHeadSyncLocationId(familyBaseEntityId));
    }
}
