package org.smartregister.chw.hf.interactor;

import static org.smartregister.chw.anc.util.Constants.TABLES.EC_CHILD;
import static org.smartregister.chw.anc.util.DBConstants.KEY.DELIVERY_DATE;
import static org.smartregister.chw.anc.util.DBConstants.KEY.DOB;
import static org.smartregister.chw.anc.util.DBConstants.KEY.LAST_NAME;
import static org.smartregister.chw.anc.util.DBConstants.KEY.MOTHER_ENTITY_ID;
import static org.smartregister.chw.anc.util.DBConstants.KEY.RELATIONAL_ID;
import static org.smartregister.chw.anc.util.DBConstants.KEY.UNIQUE_ID;
import static org.smartregister.chw.anc.util.JsonFormUtils.updateFormField;
import static org.smartregister.chw.hf.utils.Constants.Events.HEI_REGISTRATION;
import static org.smartregister.chw.hf.utils.Constants.HIV_STATUS.POSITIVE;
import static org.smartregister.chw.hf.utils.Constants.JSON_FORM_EXTRA.HIV_STATUS;
import static org.smartregister.chw.hf.utils.Constants.JSON_FORM_EXTRA.RISK_CATEGORY;
import static org.smartregister.chw.hf.utils.Constants.TableName.HEI;
import static org.smartregister.chw.hf.utils.JsonFormUtils.ENCOUNTER_TYPE;
import static org.smartregister.util.JsonFormUtils.getFieldJSONObject;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.contract.BaseAncRegisterContract;
import org.smartregister.chw.anc.interactor.BaseAncRegisterInteractor;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.dao.ChwNotificationDao;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.repository.AllSharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class AncRegisterInteractor extends BaseAncRegisterInteractor {
    private String locationID;

    public static JSONObject populatePNCForm(JSONObject form, JSONArray fields, String familyBaseEntityId, String motherBaseId, String childRiskCategory, String uniqueChildID, String dob, String lastName) {
        try {
            if (form != null) {
                form.put(RELATIONAL_ID, familyBaseEntityId);
                form.put(MOTHER_ENTITY_ID, motherBaseId);
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);


                JSONObject preLoadObject;
                JSONObject jsonObject;
                updateFormField(jsonArray, MOTHER_ENTITY_ID, motherBaseId);
                updateFormField(jsonArray, RISK_CATEGORY, childRiskCategory);
                updateFormField(jsonArray, UNIQUE_ID, uniqueChildID);
                updateFormField(jsonArray, DOB, dob);
                updateFormField(jsonArray, DELIVERY_DATE, dob);
                updateFormField(jsonArray, LAST_NAME, lastName);
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    preLoadObject = getFieldJSONObject(fields, jsonObject.optString(JsonFormUtils.KEY));
                    if (preLoadObject != null) {
                        jsonObject.put(JsonFormUtils.VALUE, preLoadObject.opt(JsonFormUtils.VALUE));

                        String type = preLoadObject.getString(JsonFormConstants.TYPE);
                        if (type.equals(JsonFormConstants.CHECK_BOX)) {
                            // replace the options
                            jsonObject.put(JsonFormConstants.OPTIONS_FIELD_NAME, preLoadObject.opt(JsonFormConstants.OPTIONS_FIELD_NAME));
                        }
                    }
                }

                return form;
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }

    @Override
    protected String getLocationID() {
        if (locationID != null) {
            return locationID;
        } else {
            return super.getLocationID();
        }

    }

    @Override
    public void saveRegistration(final String jsonString, final boolean isEditMode, final BaseAncRegisterContract.InteractorCallBack callBack, final String table) {
        Runnable runnable = () -> {
            // save it
            String encounterType = "";
            boolean hasChildren = false;

            try {
                JSONObject form = new JSONObject(jsonString);
                encounterType = form.optString(Constants.JSON_FORM_EXTRA.ENCOUNTER_TYPE);
                String motherBaseId = form.optString(Constants.JSON_FORM_EXTRA.ENTITY_TYPE);

                if (encounterType.equalsIgnoreCase(Constants.EVENT_TYPE.PREGNANCY_OUTCOME) || encounterType.equalsIgnoreCase(org.smartregister.chw.hf.utils.Constants.Events.PMTCT_POST_PNC_REGISTRATION)) {
                    String tableName = CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME;
                    saveRegistration(form.toString(), tableName, motherBaseId);

                    JSONArray fields = org.smartregister.util.JsonFormUtils.fields(form);

                    JSONObject deliveryDate = getFieldJSONObject(fields, DELIVERY_DATE);
                    JSONObject famNameObject = getFieldJSONObject(fields, DBConstants.KEY.FAM_NAME);
                    JSONObject riskCategoryObject = getFieldJSONObject(fields, RISK_CATEGORY);
                    JSONObject hivStatusObject = getFieldJSONObject(fields, HIV_STATUS);

                    String familyName = famNameObject != null ? famNameObject.optString(JsonFormUtils.VALUE) : "";

                    String hivStatus = hivStatusObject != null ? hivStatusObject.optString(JsonFormUtils.VALUE) : "";
                    String riskCategory = riskCategoryObject != null ? riskCategoryObject.optString(JsonFormUtils.VALUE) : "";

                    String dob = deliveryDate.optString(JsonFormUtils.VALUE);
                    hasChildren = StringUtils.isNotBlank(deliveryDate.optString(JsonFormUtils.VALUE));

                    JSONObject familyIdObject = getFieldJSONObject(fields, DBConstants.KEY.RELATIONAL_ID);
                    String familyBaseEntityId = familyIdObject.getString(JsonFormUtils.VALUE);

                    Map<String, List<JSONObject>> jsonObjectMap = getChildFieldMaps(fields);

                    generateAndSaveFormsForEachChild(jsonObjectMap, motherBaseId, hivStatus, riskCategory, familyBaseEntityId, dob, familyName);

                } else if (encounterType.equalsIgnoreCase(Constants.EVENT_TYPE.ANC_REGISTRATION)) {

                    JSONArray fields = org.smartregister.util.JsonFormUtils.fields(form);
                    JSONObject lmp = getFieldJSONObject(fields, DBConstants.KEY.LAST_MENSTRUAL_PERIOD);
                    boolean hasLmp = StringUtils.isNotBlank(lmp.optString(JsonFormUtils.VALUE));

                    if (!hasLmp) {
                        JSONObject eddJson = getFieldJSONObject(fields, DBConstants.KEY.EDD);
                        DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("dd-MM-yyyy");

                        LocalDate lmpDate = dateTimeFormat.parseLocalDate(eddJson.optString(JsonFormUtils.VALUE)).plusDays(-280);
                        lmp.put(JsonFormUtils.VALUE, dateTimeFormat.print(lmpDate));
                    }

                    saveRegistration(form.toString(), table, motherBaseId);
                } else {
                    saveRegistration(jsonString, table, motherBaseId);
                }
            } catch (Exception e) {
                Timber.e(e);
            }

            String finalEncounterType = encounterType;
            boolean finalHasChildren = hasChildren;
            appExecutors.mainThread().execute(() -> {
                try {
                    callBack.onRegistrationSaved(finalEncounterType, isEditMode, finalHasChildren);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        };
        appExecutors.diskIO().execute(runnable);
    }

    private Map<String, List<JSONObject>> getChildFieldMaps(JSONArray fields) {
        Map<String, List<JSONObject>> jsonObjectMap = new HashMap();

        for (int i = 0; i < fields.length(); i++) {
            try {
                JSONObject jsonObject = fields.getJSONObject(i);
                String key = jsonObject.getString(JsonFormUtils.KEY);
                String keySplit = key.substring(key.lastIndexOf("_"));
                if (keySplit.matches(".*\\d.*")) {

                    String formattedKey = keySplit.replaceAll("[^\\d.]", "");
                    if (formattedKey.length() < 10)
                        continue;
                    List<JSONObject> jsonObjectList = jsonObjectMap.get(formattedKey);

                    if (jsonObjectList == null)
                        jsonObjectList = new ArrayList<>();

                    jsonObjectList.add(jsonObject);
                    jsonObjectMap.put(formattedKey, jsonObjectList);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return jsonObjectMap;
    }

    private void saveRegistration(final String jsonString, String table, String motherBaseId) throws Exception {
        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
        Event baseEvent = JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, table);
        JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
        String syncLocationId = ChwNotificationDao.getSyncLocationId(motherBaseId);
        if (syncLocationId != null) {
            // Allows setting the ID for sync purposes
            baseEvent.setLocationId(syncLocationId);
            locationID = syncLocationId;
        }
        NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseEvent)));
    }

    protected void generateAndSaveFormsForEachChild(Map<String, List<JSONObject>> jsonObjectMap, String motherBaseId, String motherHivStatus, String childRiskCategory, String familyBaseEntityId, String dob, String familyName) {

        AllSharedPreferences allSharedPreferences = ImmunizationLibrary.getInstance().context().allSharedPreferences();

        JSONArray childFields;
        for (Map.Entry<String, List<JSONObject>> entry : jsonObjectMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                childFields = new JSONArray();
                for (JSONObject jsonObject : entry.getValue()) {
                    try {
                        String replaceString = jsonObject.getString(JsonFormUtils.KEY);

                        JSONObject childField = new JSONObject(jsonObject.toString().replaceAll(replaceString, replaceString.substring(0, replaceString.lastIndexOf("_"))));

                        childFields.put(childField);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                saveChild(childFields, motherBaseId, motherHivStatus, childRiskCategory, allSharedPreferences, familyBaseEntityId, dob, familyName);
            }
        }
    }

    private void saveChild(JSONArray childFields, String motherBaseId, String motherHivStatus, String childRiskCategory, AllSharedPreferences
            allSharedPreferences, String familyBaseEntityId, String dob, String familyName) {
        String uniqueChildID = AncLibrary.getInstance().getUniqueIdRepository().getNextUniqueId().getOpenmrsId();

        if (StringUtils.isNotBlank(uniqueChildID)) {
            String childBaseEntityId = JsonFormUtils.generateRandomUUIDString();
            try {

                JSONObject surNameObject = getFieldJSONObject(childFields, DBConstants.KEY.SUR_NAME);
                String surName = surNameObject != null ? surNameObject.optString(JsonFormUtils.VALUE) : null;

                String lastName = sameASFamilyNameCheck(childFields) ? familyName : surName;
                JSONObject pncForm = getModel().getFormAsJson(
                        AncLibrary.getInstance().context().applicationContext(),
                        Constants.FORMS.PNC_CHILD_REGISTRATION,
                        childBaseEntityId,
                        getLocationID()
                );
                pncForm = populatePNCForm(pncForm, childFields, familyBaseEntityId, motherBaseId, childRiskCategory, uniqueChildID, dob, lastName);
                processPncChild(childFields, allSharedPreferences, childBaseEntityId, familyBaseEntityId, motherBaseId, uniqueChildID, lastName, dob);
                if (pncForm != null) {
                    saveRegistration(pncForm.toString(), EC_CHILD, motherBaseId);
                    saveVaccineEvents(childFields, childBaseEntityId, dob);
                }
                if (motherHivStatus.equals(POSITIVE) && pncForm != null) {
                    pncForm.put(ENCOUNTER_TYPE, HEI_REGISTRATION);
                    saveRegistration(pncForm.toString(), HEI, motherBaseId);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean sameASFamilyNameCheck(JSONArray childFields) {
        if (childFields.length() > 0) {
            JSONObject sameAsFamNameCheck = getFieldJSONObject(childFields, DBConstants.KEY.SAME_AS_FAM_NAME_CHK);
            sameAsFamNameCheck = sameAsFamNameCheck != null ? sameAsFamNameCheck : getFieldJSONObject(childFields, DBConstants.KEY.SAME_AS_FAM_NAME);
            JSONObject sameAsFamNameObject = sameAsFamNameCheck.optJSONArray(DBConstants.KEY.OPTIONS).optJSONObject(0);
            if (sameAsFamNameCheck != null) {
                return sameAsFamNameObject.optBoolean(JsonFormUtils.VALUE);
            }
        }
        return false;
    }
}
