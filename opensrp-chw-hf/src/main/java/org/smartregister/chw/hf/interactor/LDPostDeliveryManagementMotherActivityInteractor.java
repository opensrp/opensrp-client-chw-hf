package org.smartregister.chw.hf.interactor;

import android.content.ContentValues;
import android.content.Context;
import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.ld.contract.BaseLDVisitContract;
import org.smartregister.chw.ld.dao.LDDao;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.interactor.BaseLDVisitInteractor;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.domain.Client;
import org.smartregister.domain.db.EventClient;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private MemberObject memberObject;
    public static final String EVENT_TYPE = "Post Delivery Mother Management";

    @Override
    public MemberObject getMemberClient(String memberID) {

        return LDDao.getMember(memberID);
    }

    @Override
    public void calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) {
        context = view.getContext();
        this.memberObject = memberObject;

        final Runnable runnable = () -> {
            // update the local database incase of manual date adjustment
            try {
                VisitUtils.processVisits(memberObject.getBaseEntityId());
            } catch (Exception e) {
                Timber.e(e);
            }

            try {

                evaluateMotherStatus();
                evaluatePostDeliveryObservation();
                evaluateMaternalComplicationLabour();
                evaluateNewBornStatus();

            } catch (BaseLDVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateMotherStatus() throws BaseLDVisitAction.ValidationException {

        String title = context.getString(R.string.ld_mother_status_action_title);
        MotherStatusActionHelper actionHelper = new MotherStatusActionHelper();
        BaseLDVisitAction action = getBuilder(title)
                .withOptional(false)
                .withHelper(actionHelper)
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
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withFormName(Constants.JsonForm.LDPostDeliveryMotherManagement.getLdPostDeliveryMaternalComplications())
                .build();

        actionList.put(title, action);
    }

    private void evaluateNewBornStatus() throws BaseLDVisitAction.ValidationException {
        String title = context.getString(R.string.ld_new_born_status_action_title);
        NewBornActionHelper actionHelper = new NewBornActionHelper();
        BaseLDVisitAction action = getBuilder(title)
                .withOptional(false)
                .withHelper(actionHelper)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withFormName(Constants.JsonForm.LDPostDeliveryMotherManagement.getLdNewBornStatus())
                .build();

        actionList.put(title, action);
    }

    private static class MotherStatusActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

        private String status;
        private String delivery_place;
        String delivery_date;
        String labour_information;
        private Context context;

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            status = JsonFormUtils.getFieldValue(jsonPayload, "status");
            delivery_place = JsonFormUtils.getFieldValue(jsonPayload, "delivery_place");
            delivery_date = JsonFormUtils.getFieldValue(jsonPayload, "delivery_date");
            labour_information = JsonFormUtils.getFieldValue(jsonPayload, "labour_information");
        }

        @Override
        public BaseLDVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String jsonPayload) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return null;
        }

        @Override
        public BaseLDVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isNotBlank(status) &&
                    StringUtils.isNotBlank(delivery_place) &&
                    StringUtils.isNotBlank(delivery_date) &&
                    StringUtils.isNotBlank(labour_information)) {
                return BaseLDVisitAction.Status.COMPLETED;
            } else {
                return BaseLDVisitAction.Status.PENDING;
            }
        }

        @Override
        public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {
            //Todo: Implement here
        }
    }

    private static class PostDeliveryObservationActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

        private String vagina_observation;
        private String perineum_observation;
        private String systolic;
        private String diastolic;
        private Context context;

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            vagina_observation = JsonFormUtils.getFieldValue(jsonPayload, "vagina_observation");
            perineum_observation = JsonFormUtils.getFieldValue(jsonPayload, "perineum_observation");
            systolic = JsonFormUtils.getFieldValue(jsonPayload, "systolic");
            diastolic = JsonFormUtils.getFieldValue(jsonPayload, "diastolic");
        }

        @Override
        public BaseLDVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String jsonPayload) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return null;
        }

        @Override
        public BaseLDVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isNotBlank(vagina_observation) &&
                    StringUtils.isNotBlank(perineum_observation) &&
                    StringUtils.isNotBlank(systolic) &&
                    StringUtils.isNotBlank(diastolic)) {
                return BaseLDVisitAction.Status.COMPLETED;
            } else {
                return BaseLDVisitAction.Status.PENDING;
            }
        }

        @Override
        public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {
            //Todo: Implement here
        }
    }

    private static class MaternalComplicationLabourActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

        private String maternal_complications_during_labour;
        private Context context;

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            maternal_complications_during_labour = JsonFormUtils.getFieldValue(jsonPayload, "maternal_complications_during_labour");
        }

        @Override
        public BaseLDVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String jsonPayload) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return null;
        }

        @Override
        public BaseLDVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isNotBlank(maternal_complications_during_labour)) {
                return BaseLDVisitAction.Status.COMPLETED;
            } else {
                return BaseLDVisitAction.Status.PENDING;
            }
        }

        @Override
        public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {
            //Todo: Implement here
        }
    }

    private BaseLDVisitAction.Builder getBuilder(String title) {
        return new BaseLDVisitAction.Builder(context, title);
    }

    private static class NewBornActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

        private String newbornStatus;
        private Context context;

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            newbornStatus = JsonFormUtils.getFieldValue(jsonPayload, "newborn_status");
        }

        @Override
        public BaseLDVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String jsonPayload) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return null;
        }

        @Override
        public BaseLDVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isNotBlank(newbornStatus)) {
                return BaseLDVisitAction.Status.COMPLETED;
            } else {
                return BaseLDVisitAction.Status.PENDING;
            }
        }

        @Override
        public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {
            //implement
        }
    }

    @Override
    protected String getEncounterType() {
        return EVENT_TYPE;
    }

    @Override
    protected void processExternalVisits(Visit visit, Map<String, BaseLDVisitAction> externalVisits, String memberID) throws Exception {
        super.processExternalVisits(visit, externalVisits, memberID);
        try {

            JSONObject visitJson = new JSONObject(visit.getJson());
            JSONArray obs = visitJson.getJSONArray("obs");

            JSONObject removeFamilyMemberForm = new JSONObject();
            if (isDeceased(obs)) {
                 removeFamilyMemberForm = getFormAsJson(
                        CoreConstants.JSON_FORM.getFamilyDetailsRemoveMember(), memberID, getLocationID()
                );
                if (removeFamilyMemberForm != null) {
                    JSONObject stepOne = removeFamilyMemberForm.getJSONObject(org.smartregister.chw.anc.util.JsonFormUtils.STEP1);
                    JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.chw.anc.util.JsonFormUtils.FIELDS);

                    org.smartregister.chw.anc.util.JsonFormUtils.updateFormField(jsonArray, "remove_reason", "Death");

                    // Need to get the date of delivery from the mother status format dd-MM-YYYY
                    org.smartregister.chw.anc.util.JsonFormUtils.updateFormField(jsonArray, "date_died", getDeliveryDateString(obs));
                    org.smartregister.chw.anc.util.JsonFormUtils.updateFormField(jsonArray, "age_at_death", memberObject.getAge());

                    removeUser(null, removeFamilyMemberForm, getProviderID());
                }
            }

        } catch (Exception e) {
            Timber.e(e);
        }

        closeLDForClient(memberID);

    }

    private void closeLDForClient(String memberID) {

        AllCommonsRepository commonsRepository = HealthFacilityApplication.getInstance().getAllCommonsRepository(org.smartregister.chw.ld.util.Constants.TABLES.LD_CONFIRMATION);
        if (commonsRepository != null) {

            ContentValues values = new ContentValues();
            values.put("is_closed", 1);

            HealthFacilityApplication.getInstance().getRepository().getWritableDatabase().update(
                    org.smartregister.chw.ld.util.Constants.TABLES.LD_CONFIRMATION, values, DBConstants.KEY.BASE_ENTITY_ID + " = ?  ", new String[]{memberID});
        }

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
}
