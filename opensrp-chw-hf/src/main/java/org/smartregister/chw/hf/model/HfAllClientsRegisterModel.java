package org.smartregister.chw.hf.model;

import static com.vijay.jsonwizard.utils.FormUtils.fields;
import static com.vijay.jsonwizard.utils.FormUtils.getFieldJSONObject;
import static org.smartregister.AllConstants.LocationConstants.SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS;
import static org.smartregister.chw.hf.utils.JsonFormUtils.METADATA;
import static org.smartregister.chw.hf.utils.JsonFormUtils.SYNC_LOCATION_ID;
import static org.smartregister.family.util.JsonFormUtils.STEP2;
import static org.smartregister.util.JsonFormUtils.ENCOUNTER_LOCATION;
import static org.smartregister.util.JsonFormUtils.STEP1;

import android.content.Context;

import androidx.annotation.Nullable;

import com.vijay.jsonwizard.utils.FormUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.utils.AllClientsUtils;
import org.smartregister.dao.LocationsDao;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.opd.model.OpdRegisterActivityModel;
import org.smartregister.opd.pojo.OpdEventClient;
import org.smartregister.opd.utils.OpdUtils;

import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class HfAllClientsRegisterModel extends OpdRegisterActivityModel {
    private Context context;

    public HfAllClientsRegisterModel(Context context) {
        this.context = context;
    }


    @Nullable
    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) {
        try {
            JSONObject form;
            if (context != null) {
                form = (new FormUtils()).getFormJsonFromRepositoryOrAssets(context, formName);
            } else
                form = OpdUtils.getJsonFormToJsonObject(formName);

            if (form == null) {
                return null;
            }
            JSONObject syncLocationField = CoreJsonFormUtils.getJsonField(form, STEP1, SYNC_LOCATION_ID);
            CoreJsonFormUtils.addLocationsToDropdownField(LocationsDao.getLocationsByTags(
                    Collections.singleton(SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS)), syncLocationField);
            form.getJSONObject(METADATA).put(ENCOUNTER_LOCATION, currentLocationId);

            String newEntityId = entityId;
            if (StringUtils.isNotBlank(entityId)) {
                newEntityId = entityId.replace("-", "");
            }

            JSONObject stepOneUniqueId = getFieldJSONObject(fields(form, STEP1), Constants.JSON_FORM_KEY.UNIQUE_ID);

            if (stepOneUniqueId != null) {
                stepOneUniqueId.remove(JsonFormUtils.VALUE);
                stepOneUniqueId.put(JsonFormUtils.VALUE, newEntityId + "_Family");
            }

            JSONObject stepTwoUniqueId = getFieldJSONObject(fields(form, STEP2), Constants.JSON_FORM_KEY.UNIQUE_ID);
            if (stepTwoUniqueId != null) {
                stepTwoUniqueId.remove(JsonFormUtils.VALUE);
                stepTwoUniqueId.put(JsonFormUtils.VALUE, newEntityId);
            }

            JsonFormUtils.addLocHierarchyQuestions(form);
            return form;

        } catch (Exception e) {
            Timber.e(e, "Error loading All Client registration form");
        }
        return null;
    }

    @Nullable
    @Override
    public List<OpdEventClient> processRegistration(String jsonString, FormTag formTag) {

        List<OpdEventClient> opdEventClients = AllClientsUtils.getOpdEventClients(jsonString);
        try {
            JSONObject syncLocationField = CoreJsonFormUtils.getJsonField(new JSONObject(jsonString), STEP1, SYNC_LOCATION_ID);
            for (OpdEventClient opdEventClient : opdEventClients) {
                opdEventClient.getEvent().setLocationId(CoreJsonFormUtils.getSyncLocationUUIDFromDropdown(syncLocationField));
            }
        } catch (JSONException e) {
            Timber.e(e, "Error retrieving Sync location Field");
        }
        return opdEventClients;
    }
}
