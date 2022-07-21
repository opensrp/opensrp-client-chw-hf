package org.smartregister.chw.hf.interactor;

import static org.smartregister.chw.anc.util.Constants.TABLES.EC_CHILD;
import static org.smartregister.chw.anc.util.DBConstants.KEY.RELATIONAL_ID;
import static org.smartregister.chw.anc.util.JsonFormUtils.updateFormField;
import static org.smartregister.chw.hf.interactor.AncRegisterInteractor.populatePNCForm;
import static org.smartregister.chw.hf.utils.Constants.Events.HEI_REGISTRATION;
import static org.smartregister.chw.hf.utils.Constants.Events.LD_POST_DELIVERY_MOTHER_MANAGEMENT;
import static org.smartregister.chw.hf.utils.Constants.HIV_STATUS.POSITIVE;
import static org.smartregister.chw.hf.utils.Constants.HeiHIVTestAtAge.AT_BIRTH;
import static org.smartregister.chw.hf.utils.Constants.TableName.HEI;
import static org.smartregister.chw.hf.utils.Constants.TableName.HEI_FOLLOWUP;
import static org.smartregister.chw.hf.utils.JsonFormUtils.ENCOUNTER_TYPE;
import static org.smartregister.util.JsonFormUtils.FIELDS;
import static org.smartregister.util.JsonFormUtils.KEY;
import static org.smartregister.util.JsonFormUtils.VALUE;

import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.util.Pair;

import androidx.annotation.RequiresApi;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.MaternalComplicationLabourActionHelper;
import org.smartregister.chw.hf.actionhelper.MotherStatusActionHelper;
import org.smartregister.chw.hf.actionhelper.PostDeliveryFamilyPlanningActionHelper;
import org.smartregister.chw.hf.actionhelper.PostDeliveryObservationActionHelper;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.LDVisitUtils;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.contract.BaseLDVisitContract;
import org.smartregister.chw.ld.dao.LDDao;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.interactor.BaseLDVisitInteractor;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.chw.referral.util.JsonFormConstants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.domain.Client;
import org.smartregister.domain.db.EventClient;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-05-16
 */
public class LDPostDeliveryManagementMotherActivityInteractor extends BaseLDVisitInteractor {

    protected Context context;
    final LinkedHashMap<String, BaseLDVisitAction> actionList = new LinkedHashMap<>();
    private static MemberObject memberObject;
    private static boolean isEdit = false;
    Map<String, List<VisitDetail>> details = null;

    @Override
    public MemberObject getMemberClient(String memberID) {

        return LDDao.getMember(memberID);
    }

    @Override
    public void calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) {
        context = view.getContext();
        LDPostDeliveryManagementMotherActivityInteractor.memberObject = memberObject;

        if (view.getEditMode()) {
            isEdit = view.getEditMode();
            Visit lastVisit = LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), LD_POST_DELIVERY_MOTHER_MANAGEMENT);

            if (lastVisit != null) {
                details = org.smartregister.chw.ld.util.VisitUtils.getVisitGroups(LDLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        final Runnable runnable = () -> {
            // update the local database incase of manual date adjustment
            try {
                VisitUtils.processVisits(memberObject.getBaseEntityId());
            } catch (Exception e) {
                Timber.e(e);
            }

            try {

                evaluateMotherStatus(callBack);
                evaluatePostDeliveryObservation();
                evaluateMaternalComplicationLabour();
                evaluateFamilyPlanning();

            } catch (BaseLDVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateMotherStatus(BaseLDVisitContract.InteractorCallBack callBack) throws BaseLDVisitAction.ValidationException {

        String title = context.getString(R.string.ld_mother_status_action_title);
        MotherStatusActionHelper actionHelper = new MotherStatusActionHelper(context, memberObject.getBaseEntityId(), actionList, callBack, isEdit);
        BaseLDVisitAction action = getBuilder(title)
                .withOptional(false)
                .withHelper(actionHelper)
                .withDetails(details)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withFormName(Constants.JsonForm.LDPostDeliveryMotherManagement.getLdPostDeliveryManagementMotherStatus())
                .build();

        actionList.put(title, action);
    }

    private void evaluatePostDeliveryObservation() throws BaseLDVisitAction.ValidationException {
        String title = context.getString(R.string.ld_post_delivery_observation_action_title);
        PostDeliveryObservationActionHelper actionHelper = new PostDeliveryObservationActionHelper();
        BaseLDVisitAction action = getBuilder(title)
                .withOptional(false)
                .withHelper(actionHelper)
                .withDetails(details)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withFormName(Constants.JsonForm.LDPostDeliveryMotherManagement.getLdPostDeliveryMotherObservation())
                .build();

        actionList.put(title, action);
    }

    private void evaluateMaternalComplicationLabour() throws BaseLDVisitAction.ValidationException {

        String title = context.getString(R.string.ld_maternal_complication_action_title);
        MaternalComplicationLabourActionHelper actionHelper = new MaternalComplicationLabourActionHelper();
        BaseLDVisitAction action = getBuilder(title)
                .withOptional(false)
                .withHelper(actionHelper)
                .withDetails(details)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withFormName(Constants.JsonForm.LDPostDeliveryMotherManagement.getLdPostDeliveryMaternalComplications())
                .build();

        actionList.put(title, action);
    }

    private void evaluateFamilyPlanning() throws BaseLDVisitAction.ValidationException {
        String title = context.getString(R.string.ld_post_delivery_family_planning);

        PostDeliveryFamilyPlanningActionHelper actionHelper = new PostDeliveryFamilyPlanningActionHelper();

        BaseLDVisitAction action = getBuilder(title)
                .withOptional(false)
                .withHelper(actionHelper)
                .withDetails(details)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withFormName(Constants.JsonForm.LDPostDeliveryMotherManagement.getLdPostDeliveryFamilyPlanning())
                .build();

        actionList.put(title, action);
    }

    private BaseLDVisitAction.Builder getBuilder(String title) {
        return new BaseLDVisitAction.Builder(context, title);
    }

    @Override
    protected String getEncounterType() {
        return LD_POST_DELIVERY_MOTHER_MANAGEMENT;
    }

    @Override
    protected void processExternalVisits(Visit visit, Map<String, BaseLDVisitAction> externalVisits, String memberID) throws Exception {
        //super.processExternalVisits(visit, externalVisits, memberID);
        if (visit != null && !externalVisits.isEmpty()) {
            for (Map.Entry<String, BaseLDVisitAction> entry : externalVisits.entrySet()) {
                Map<String, BaseLDVisitAction> subEvent = new HashMap<>();
                subEvent.put(entry.getKey(), entry.getValue());

                String subMemberID = entry.getValue().getBaseEntityID();
                if (StringUtils.isBlank(subMemberID))
                    subMemberID = memberID;

                submitVisit(false, subMemberID, subEvent, visit.getVisitType());
            }
        }
        try {

            boolean visitCompleted = true;
            for (Map.Entry<String, BaseLDVisitAction> entry : actionList.entrySet()) {
                String actionStatus = entry.getValue().getActionStatus().toString();
                if (actionStatus.equalsIgnoreCase("PARTIALLY_COMPLETED")) {
                    visitCompleted = false;

                }
            }

            if (visitCompleted) {

                AllSharedPreferences allSharedPreferences = ImmunizationLibrary.getInstance().context().allSharedPreferences();

                JSONObject visitJson = new JSONObject(visit.getJson());
                JSONArray obs = visitJson.getJSONArray("obs");
                String deliveryDate = getDeliveryDateString(obs);

                String completionStatus = getObValue(obs, "newborn_stage_four_module_status");

                JSONObject removeFamilyMemberForm = new JSONObject();
                if (isDeceased(obs)) {
                    removeFamilyMemberForm = getFormAsJson(
                            CoreConstants.JSON_FORM.getFamilyDetailsRemoveMember(), memberID, getLocationID()
                    );
                    if (removeFamilyMemberForm != null) {
                        JSONObject stepOne = removeFamilyMemberForm.getJSONObject(org.smartregister.chw.anc.util.JsonFormUtils.STEP1);
                        JSONArray jsonArray = stepOne.getJSONArray(FIELDS);

                        org.smartregister.chw.anc.util.JsonFormUtils.updateFormField(jsonArray, "remove_reason", "Death");

                        // Need to get the date of delivery from the mother status format dd-MM-YYYY
                        org.smartregister.chw.anc.util.JsonFormUtils.updateFormField(jsonArray, "date_died", deliveryDate);
                        org.smartregister.chw.anc.util.JsonFormUtils.updateFormField(jsonArray, "age_at_death", memberObject.getAge());

                        removeUser(null, removeFamilyMemberForm, getProviderID());
                    }
                }
                if (isChildAlive(obs) && (StringUtils.isNotBlank(completionStatus) && completionStatus.equalsIgnoreCase("Fully Completed"))) {
                    saveChild(memberID,memberObject.getBaseEntityId(), LDDao.getHivStatus(memberObject.getBaseEntityId()), getRiskStatus(obs), allSharedPreferences, memberObject.getFamilyBaseEntityId(), getDeliveryDateString(obs), obs);
                }

                LDVisitUtils.processVisits(memberID, false);
            }
        } catch (Exception e) {
            Timber.e(e);
        }

    }

    private void saveChild(String childBaseEntityId, String motherBaseId, String motherHivStatus, String childRiskCategory, AllSharedPreferences
            allSharedPreferences, String familyBaseEntityId, String dob, JSONArray obs) {
        String uniqueChildID = AncLibrary.getInstance().getUniqueIdRepository().getNextUniqueId().getOpenmrsId();

        if (StringUtils.isNotBlank(uniqueChildID)) {
            try {
                String lastName = memberObject.getLastName();
                JSONObject pncForm = getFormAsJson(
                        Constants.JsonForm.getLdChildRegistration(),
                        childBaseEntityId,
                        getLocationID()
                );
                pncForm = populatePNCForm(pncForm, obs, familyBaseEntityId, motherBaseId, childRiskCategory, uniqueChildID, dob, lastName);
                pncForm = populateChildRegistrationForm(pncForm, obs, motherBaseId, familyBaseEntityId);
                processChild(pncForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS), allSharedPreferences, childBaseEntityId, familyBaseEntityId, motherBaseId, uniqueChildID, lastName, dob);
                if (pncForm != null) {
                    saveChildRegistration(pncForm.toString(), EC_CHILD);
                }
                if (motherHivStatus.equals(POSITIVE) && pncForm != null) {
                    pncForm.put(ENCOUNTER_TYPE, HEI_REGISTRATION);
                    saveChildRegistration(pncForm.toString(), HEI);

                    JSONObject heiFollowupForm = getFormAsJson(
                            Constants.JsonForm.getLdHeiFirstVisit(),
                            childBaseEntityId,
                            getLocationID()
                    );

                    heiFollowupForm = populateHeiFollowupForm(heiFollowupForm, obs, familyBaseEntityId);
                    saveChildRegistration(heiFollowupForm.toString(), HEI_FOLLOWUP);


                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static JSONObject populateHeiFollowupForm(JSONObject form, JSONArray fields, String familyBaseEntityId) {
        try {
            if (form != null) {
                form.put(RELATIONAL_ID, familyBaseEntityId);

                JSONObject stepOne = form.getJSONObject(org.smartregister.chw.anc.util.JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(FIELDS);

                if (getRiskStatus(fields).equalsIgnoreCase("high")) {
                    updateFormField(jsonArray, "test_at_age", AT_BIRTH);
                    updateFormField(jsonArray, "actual_age", "0d");
                    updateFormField(jsonArray, "type_of_hiv_test", "DNA PCR");
                }

                JSONObject jsonObject;
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    String value = getObValue(fields, jsonObject.optString(KEY));
                    if (value != null) {
                        jsonObject.put(VALUE, value);
                    }
                }

                return form;
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }

    private JSONObject populateChildRegistrationForm(JSONObject form, JSONArray obs, String motherId, String familyId) {
        try {
            form.put(DBConstants.KEY.RELATIONAL_ID, familyId);
            form.put("mother_entity_id", motherId);
            JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
            JSONArray fields = stepOne.getJSONArray(FIELDS);

            String babyFirstName = context.getString(R.string.ld_baby_of_text) + " " + memberObject.getFirstName();
            String dob = getDeliveryDateString(obs);
            String gender = getChildGender(obs);

            org.smartregister.chw.anc.util.JsonFormUtils.updateFormField(fields, "mother_entity_id", motherId);
            org.smartregister.chw.anc.util.JsonFormUtils.updateFormField(fields, "first_name", babyFirstName);
            org.smartregister.chw.anc.util.JsonFormUtils.updateFormField(fields, "last_name", memberObject.getLastName());
            org.smartregister.chw.anc.util.JsonFormUtils.updateFormField(fields, "middle_name", memberObject.getMiddleName());
            org.smartregister.chw.anc.util.JsonFormUtils.updateFormField(fields, "dob", dob);
            org.smartregister.chw.anc.util.JsonFormUtils.updateFormField(fields, "gender", gender);
            org.smartregister.chw.anc.util.JsonFormUtils.updateFormField(fields, "surname", memberObject.getLastName());

            return form;
        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }

    private void processChild(JSONArray fields, AllSharedPreferences allSharedPreferences, String entityId, String familyBaseEntityId, String motherBaseId, String uniqueChildID, String lastName, String dob) {

        try {
            org.smartregister.clientandeventmodel.Client pncChild = JsonFormUtils.createBaseClient(fields, org.smartregister.chw.anc.util.JsonFormUtils.formTag(allSharedPreferences), entityId);
            Map<String, String> identifiers = new HashMap<>();
            identifiers.put(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.OPENSPR_ID, uniqueChildID.replace("-", ""));
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            Date date = formatter.parse(dob);
            pncChild.setLastName(lastName);
            pncChild.setBirthdate(date);
            pncChild.setIdentifiers(identifiers);
            pncChild.addRelationship(org.smartregister.chw.anc.util.Constants.RELATIONSHIP.FAMILY, familyBaseEntityId);
            pncChild.addRelationship(org.smartregister.chw.anc.util.Constants.RELATIONSHIP.MOTHER, motherBaseId);

            JSONObject eventJson = new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(pncChild));
            AncLibrary.getInstance().getUniqueIdRepository().close(pncChild.getIdentifier(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.OPENSPR_ID));

            NCUtils.getSyncHelper().addClient(pncChild.getBaseEntityId(), eventJson);

        } catch (Exception e) {
            Timber.e(e);
        }

    }

    private void saveChildRegistration(final String jsonString, String table) throws Exception {
        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
        Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, table);

        NCUtils.addEvent(allSharedPreferences, baseEvent);
        NCUtils.startClientProcessing();
    }

    private String removeUser(String familyID, JSONObject closeFormJsonString, String providerId) throws Exception {
        String res = null;
        Triple<Pair<Date, String>, String, List<Event>> triple = CoreJsonFormUtils.processRemoveMemberEvent(familyID, Utils.getAllSharedPreferences(), closeFormJsonString, providerId);
        if (triple != null && triple.getLeft() != null) {
            processEvents(triple.getRight());

            if (triple.getLeft().second.equalsIgnoreCase(CoreConstants.EventType.REMOVE_CHILD)) {
                updateRepo(triple, Utils.metadata().familyMemberRegister.tableName);
                updateRepo(triple, CoreConstants.TABLE_NAME.CHILD);
            } else if (triple.getLeft().second.equalsIgnoreCase(CoreConstants.EventType.REMOVE_FAMILY)) {
                updateRepo(triple, Utils.metadata().familyRegister.tableName);
            } else {
                updateRepo(triple, Utils.metadata().familyMemberRegister.tableName);
            }
            res = triple.getLeft().second;
        }

        long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
        Date lastSyncDate = new Date(lastSyncTimeStamp);
        getClientProcessorForJava().processClient(getSyncHelper().getEvents(lastSyncDate, BaseRepository.TYPE_Unprocessed));
        getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
        return res;
    }

    private void processEvents(List<Event> events) throws Exception {
        ECSyncHelper syncHelper = HealthFacilityApplication.getInstance().getEcSyncHelper();
        List<EventClient> clients = new ArrayList<>();
        for (Event e : events) {
            JSONObject json = new JSONObject(CoreJsonFormUtils.gson.toJson(e));
            syncHelper.addEvent(e.getBaseEntityId(), json);

            org.smartregister.domain.Event event = CoreJsonFormUtils.gson.fromJson(json.toString(), org.smartregister.domain.Event.class);
            clients.add(new EventClient(event, new Client(e.getBaseEntityId())));
        }
        getClientProcessorForJava().processClient(clients);
    }

    public ClientProcessorForJava getClientProcessorForJava() {
        return FamilyLibrary.getInstance().getClientProcessorForJava();
    }

    public ECSyncHelper getSyncHelper() {
        return FamilyLibrary.getInstance().getEcSyncHelper();
    }

    public AllSharedPreferences getAllSharedPreferences() {
        return Utils.context().allSharedPreferences();
    }

    private void updateRepo(Triple<Pair<Date, String>, String, List<Event>> triple, String tableName) {
        AllCommonsRepository commonsRepository = HealthFacilityApplication.getInstance().getAllCommonsRepository(tableName);

        Date date_removed = new Date();
        Date dod = null;
        if (triple.getLeft() != null && triple.getLeft().first != null) {
            dod = triple.getLeft().first;
        }

        if (commonsRepository != null && dod == null) {
            ContentValues values = new ContentValues();
            values.put(DBConstants.KEY.DATE_REMOVED, getDBFormatedDate(date_removed));
            commonsRepository.update(tableName, values, triple.getMiddle());
            commonsRepository.updateSearch(triple.getMiddle());
            commonsRepository.close(triple.getMiddle());
        }

        // enter the date of death
        if (dod != null && commonsRepository != null) {
            ContentValues values = new ContentValues();
            values.put(DBConstants.KEY.DOD, getDBFormatedDate(dod));
            commonsRepository.update(tableName, values, triple.getMiddle());
            commonsRepository.updateSearch(triple.getMiddle());
        }
    }

    private String getDBFormatedDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject jsonObject = getJsonForm(formName);
        org.smartregister.chw.anc.util.JsonFormUtils.getRegistrationForm(jsonObject, entityId, currentLocationId);

        return jsonObject;
    }

    private JSONObject getJsonForm(String formName) throws Exception {
        return FormUtils.getInstance(HealthFacilityApplication.getInstance().getApplicationContext().getApplicationContext()).getFormJson(formName);
    }

    protected String getLocationID() {
        return org.smartregister.Context.getInstance().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
    }

    protected String getProviderID() {
        return org.smartregister.Context.getInstance().allSharedPreferences().fetchRegisteredANM();
    }

    private boolean isDeceased(JSONArray obs) throws JSONException {
        int size = obs.length();
        for (int i = 0; i < size; i++) {
            JSONObject checkObj = obs.getJSONObject(i);
            if (checkObj.getString("fieldCode").equalsIgnoreCase("status")) {
                JSONArray values = checkObj.getJSONArray("values");
                if (values != null) {
                    return values.get(0).equals("deceased");
                }
            }
        }
        return false;
    }

    private static String getRiskStatus(JSONArray obs) throws JSONException {
        int size = obs.length();
        for (int i = 0; i < size; i++) {
            JSONObject checkObj = obs.getJSONObject(i);
            if (checkObj.getString("fieldCode").equalsIgnoreCase("risk_category")) {
                JSONArray values = checkObj.getJSONArray("values");
                if (values != null) {
                    return values.getString(0);
                }
            }
        }
        return null;
    }

    private String getDeliveryDateString(JSONArray obs) throws JSONException {
        String deliveryDateString = null;
        if (obs.length() > 0) {
            for (int i = 0; i < obs.length(); i++) {
                JSONObject jsonObject = obs.getJSONObject(i);
                if (jsonObject.getString("fieldCode").equalsIgnoreCase("delivery_date")) {
                    JSONArray values = jsonObject.getJSONArray("values");
                    if (values != null) {
                        deliveryDateString = values.getString(0);
                    }
                }
            }
        }
        return deliveryDateString;
    }

    private static String getObValue(JSONArray obs, String key) throws JSONException {
        String valueString = null;
        if (obs.length() > 0) {
            for (int i = 0; i < obs.length(); i++) {
                JSONObject jsonObject = obs.getJSONObject(i);
                if (jsonObject.getString("fieldCode").equalsIgnoreCase(key)) {
                    JSONArray values = jsonObject.getJSONArray("values");
                    if (values != null) {
                        valueString = values.getString(0);
                    }
                }
            }
        }
        return valueString;
    }

    private boolean isChildAlive(JSONArray obs) throws JSONException {
        int size = obs.length();
        for (int i = 0; i < size; i++) {
            JSONObject checkObj = obs.getJSONObject(i);
            if (checkObj.getString("fieldCode").equalsIgnoreCase("newborn_status")) {
                JSONArray values = checkObj.getJSONArray("values");
                if (values != null) {
                    return values.get(0).equals("alive");
                }
            }
        }
        return false;
    }

    private String getChildGender(JSONArray obs) throws JSONException {
        String gender = null;
        if (obs.length() > 0) {

            for (int i = 0; i < obs.length(); i++) {

                JSONObject jsonObject = obs.getJSONObject(i);

                if (jsonObject.getString("fieldCode").equalsIgnoreCase("sex")) {
                    JSONArray values = jsonObject.getJSONArray("values");
                    if (values != null) {
                        if (!values.getString(0).equalsIgnoreCase("null")) {
                            gender = values.getString(0);
                        } else {
                            gender = jsonObject.getJSONArray("humanReadableValues").getString(0);
                        }
                    }
                }

            }

        }

        return gender;
    }

    public static String ordinal(int i) {
        String[] suffixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];

        }
    }
}
