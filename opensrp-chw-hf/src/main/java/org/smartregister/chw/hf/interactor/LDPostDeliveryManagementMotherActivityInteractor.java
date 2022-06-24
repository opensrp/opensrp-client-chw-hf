package org.smartregister.chw.hf.interactor;

import static org.smartregister.chw.anc.util.Constants.TABLES.EC_CHILD;
import static org.smartregister.chw.anc.util.DBConstants.KEY.RELATIONAL_ID;
import static org.smartregister.chw.anc.util.JsonFormUtils.updateFormField;
import static org.smartregister.chw.hf.interactor.AncRegisterInteractor.populatePNCForm;
import static org.smartregister.chw.hf.utils.Constants.Events.HEI_REGISTRATION;
import static org.smartregister.chw.hf.utils.Constants.HIV_STATUS.POSITIVE;
import static org.smartregister.chw.hf.utils.Constants.HeiHIVTestAtAge.AT_BIRTH;
import static org.smartregister.chw.hf.utils.Constants.TableName.HEI;
import static org.smartregister.chw.hf.utils.Constants.TableName.HEI_FOLLOWUP;
import static org.smartregister.chw.hf.utils.JsonFormUtils.ENCOUNTER_TYPE;
import static org.smartregister.util.JsonFormUtils.FIELDS;
import static org.smartregister.util.JsonFormUtils.KEY;
import static org.smartregister.util.JsonFormUtils.STEP1;
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
import org.smartregister.chw.anc.util.AppExecutors;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.LDDao;
import org.smartregister.chw.hf.utils.LDVisitUtils;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.contract.BaseLDVisitContract;
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

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
    Map<String, List<VisitDetail>> details = null;

    @Override
    public MemberObject getMemberClient(String memberID) {

        return LDDao.getMember(memberID);
    }

    @Override
    public void calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) {
        context = view.getContext();
        this.memberObject = memberObject;

        if (view.getEditMode()) {
            Visit lastVisit = LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), EVENT_TYPE);

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

            } catch (BaseLDVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateMotherStatus(BaseLDVisitContract.InteractorCallBack callBack) throws BaseLDVisitAction.ValidationException {

        String title = context.getString(R.string.ld_mother_status_action_title);
        MotherStatusActionHelper actionHelper = new MotherStatusActionHelper(context, memberObject.getBaseEntityId(), actionList, callBack);
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

    private static class MotherStatusActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

        private String status;
        private String cause_of_death;
        private String time_of_death;
        private String delivery_place;
        private String delivered_by_occupation;
        private String name_of_delivery_person;
        private String delivery_date;
        private String completionStatus;
        private int numberOfChildrenBorn = 0;
        private String delivery_time;
        private Context context;
        private String baseEntityId;
        private LinkedHashMap<String, BaseLDVisitAction> actionList;
        private final BaseLDVisitContract.InteractorCallBack callBack;
        private Map<String, List<VisitDetail>> details;

        public MotherStatusActionHelper(Context context, String baseEntityId, LinkedHashMap<String, BaseLDVisitAction> actionList, BaseLDVisitContract.InteractorCallBack callBack) {
            this.context = context;
            this.baseEntityId = baseEntityId;
            this.actionList = actionList;
            this.callBack = callBack;
        }

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
            this.context = context;
            this.details = details;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            status = JsonFormUtils.getFieldValue(jsonPayload, "status");
            cause_of_death = JsonFormUtils.getFieldValue(jsonPayload, "cause_of_death");
            time_of_death = JsonFormUtils.getFieldValue(jsonPayload, "time_of_death");
            delivery_place = JsonFormUtils.getFieldValue(jsonPayload, "delivery_place");
            delivered_by_occupation = JsonFormUtils.getFieldValue(jsonPayload, "delivered_by_occupation");
            name_of_delivery_person = JsonFormUtils.getFieldValue(jsonPayload, "name_of_delivery_person");
            delivery_date = JsonFormUtils.getFieldValue(jsonPayload, "delivery_date");
            delivery_time = JsonFormUtils.getFieldValue(jsonPayload, "delivery_time");
            try {
                String number_children_string = JsonFormUtils.getFieldValue(jsonPayload, "number_of_children_born");
                if (StringUtils.isNotBlank(number_children_string)) {
                    numberOfChildrenBorn = Integer.parseInt(number_children_string);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
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

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(jsonPayload);
                JSONArray fields = JsonFormUtils.fields(jsonObject);

                JSONObject mother_status_module_status = JsonFormUtils.getFieldJSONObject(fields, "mother_status_module_status");
                assert mother_status_module_status != null;
                mother_status_module_status.remove(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE);
                mother_status_module_status.put(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE, completionStatus);

                String deliveryDate = JsonFormUtils.getFieldValue(jsonPayload, "delivery_date");
                String deliveryTime = JsonFormUtils.getFieldValue(jsonPayload, "delivery_time");
                String labourOnsetDate;
                String labourOnsetTime;
                if (LDDao.getLabourOnsetDate(baseEntityId) != null && LDDao.getLabourOnsetTime(baseEntityId) != null) {
                    labourOnsetDate = LDDao.getLabourOnsetDate(baseEntityId);
                    labourOnsetTime = LDDao.getLabourOnsetTime(baseEntityId);
                } else {
                    labourOnsetDate = LDDao.getAdmissionDate(baseEntityId);
                    labourOnsetTime = LDDao.getAdmissionTime(baseEntityId);
                }

                // Add a check here if delivery date is not yet filled
                if (StringUtils.isNotBlank(deliveryDate) && StringUtils.isNotBlank(deliveryTime)) {

                    String labourOnsetDateTime = labourOnsetDate + " " + labourOnsetTime;
                    String deliveryDateTime = deliveryDate + " " + deliveryTime;

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

                    Date date1 = simpleDateFormat.parse(labourOnsetDateTime);
                    Date date2 = simpleDateFormat.parse(deliveryDateTime);

                    long msDiff = date2.getTime() - date1.getTime();

                    for (int i = 0; i < fields.length(); i++) {
                        if (fields.getJSONObject(i).getString(KEY).equalsIgnoreCase("labour_duration")) {
                            fields.getJSONObject(i).put(VALUE, msDiff);
                        }
                    }
                }

            } catch (Exception e) {
                Timber.e(e);
            }

            for (Map.Entry<String, BaseLDVisitAction> entry : actionList.entrySet()) {
                if (entry.getKey().contains(MessageFormat.format(context.getString(R.string.ld_new_born_status_action_title), "")))
                    actionList.remove(entry.getKey());
            }

            if (numberOfChildrenBorn > 0) {
                for (int i = 0; i < numberOfChildrenBorn; i++) {
                    String title;
                    if (numberOfChildrenBorn == 1) {
                        title = MessageFormat.format(context.getString(R.string.ld_new_born_status_action_title), "");
                    } else {
                        title = MessageFormat.format(context.getString(R.string.ld_new_born_status_action_title), "of " + ordinal(i + 1) + " baby");
                    }
                    NewBornActionHelper actionHelper = new NewBornActionHelper(baseEntityId, delivery_date, delivery_time, numberOfChildrenBorn, status);
                    BaseLDVisitAction action = null;
                    try {
                        action = new BaseLDVisitAction.Builder(context, title)
                                .withOptional(false)
                                .withHelper(actionHelper)
                                .withBaseEntityID(baseEntityId)
                                .withProcessingMode(BaseLDVisitAction.ProcessingMode.SEPARATE)
                                .withFormName(Constants.JsonForm.LDPostDeliveryMotherManagement.getLdNewBornStatus())
                                .build();

                        actionList.put(title, action);
                    } catch (BaseLDVisitAction.ValidationException e) {
                        Timber.e(e);
                    }
                }

                //Calling the callback method to preload the actions in the actionns list.
                new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            }
            return jsonObject.toString();

        }

        @Override
        public String evaluateSubTitle() {
            if (isFullyCompleted()) {
                completionStatus = context.getString(R.string.lb_fully_completed_action);
            } else if (isPartiallyCompleted()) {
                completionStatus = context.getString(R.string.lb_partially_completed_action);
            }
            return completionStatus;
        }

        @Override
        public BaseLDVisitAction.Status evaluateStatusOnPayload() {
            if (isFullyCompleted()) {
                return BaseLDVisitAction.Status.COMPLETED;
            } else if (isPartiallyCompleted()) {
                return BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
            } else {
                return BaseLDVisitAction.Status.PENDING;
            }
        }

        @Override
        public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {
            //Todo: Implement here
        }

        private boolean isFullyCompleted() {
            boolean completed = false;
            if (StringUtils.isNotBlank(status)) {
                if (status.equalsIgnoreCase("alive")) {
                    if ((StringUtils.isNotBlank(delivery_place) && !delivery_place.equalsIgnoreCase("Place of delivery")) &&
                            StringUtils.isNotBlank(delivery_date)) {
                        completed = !delivery_place.equalsIgnoreCase("At a health facility") || (StringUtils.isNotBlank(delivered_by_occupation) &&
                                StringUtils.isNotBlank(name_of_delivery_person));
                    }
                } else {
                    if ((StringUtils.isNotBlank(delivery_place) && !delivery_place.equalsIgnoreCase("Place of delivery")) &&
                            StringUtils.isNotBlank(cause_of_death) && StringUtils.isNotBlank(time_of_death) && StringUtils.isNotBlank(delivery_date)) {
                        completed = !delivery_place.equalsIgnoreCase("At a health facility") || (StringUtils.isNotBlank(delivered_by_occupation) &&
                                StringUtils.isNotBlank(name_of_delivery_person));
                    }
                }
            }
            return completed;
        }

        private boolean isPartiallyCompleted() {
            boolean partialCompletion = false;
            if (StringUtils.isNotBlank(status)) {
                if (status.equalsIgnoreCase("alive")) {
                    // Because of spinner delivery place is never blank it is the value of the hint
                    if (delivery_place.equalsIgnoreCase("Place of delivery") || StringUtils.isBlank(delivery_date)) {
                        partialCompletion = true;
                    } else if (delivery_place.equalsIgnoreCase("At a health facility")) {
                        partialCompletion = StringUtils.isBlank(delivered_by_occupation) || StringUtils.isBlank(name_of_delivery_person);
                    }
                } else {
                    if (delivery_place.equalsIgnoreCase("Place of delivery") || StringUtils.isBlank(delivery_date) || StringUtils.isBlank(cause_of_death) ||
                            StringUtils.isBlank(time_of_death)) {
                        partialCompletion = true;
                    } else if (delivery_place.equalsIgnoreCase("At a health facility")) {
                        partialCompletion = StringUtils.isBlank(delivered_by_occupation) || StringUtils.isBlank(name_of_delivery_person);
                    }
                }
            }
            return partialCompletion;
        }
    }

    private static class PostDeliveryObservationActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

        private String vagina_observation;
        private String vaginal_bleeding_observation;
        private String perineum_observation;
        private String degree_of_perineum_tear;
        private String perineum_repair_occupation;
        private String perineum_repair_person_name;
        private String cervix_observation;
        private String systolic;
        private String diastolic;
        private String pulse_rate;
        private String temperature;
        private String uterus_contraction;
        private String urination;
        private String observation_date;
        private String observation_time;
        private String completionStatus;
        private Context context;
        private Map<String, List<VisitDetail>> details;
        private String jsonString;

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
            vaginal_bleeding_observation = JsonFormUtils.getFieldValue(jsonPayload, "vaginal_bleeding_observation");
            perineum_observation = JsonFormUtils.getFieldValue(jsonPayload, "perineum_observation");
            degree_of_perineum_tear = JsonFormUtils.getFieldValue(jsonPayload, "degree_of_perineum_tear");
            perineum_repair_occupation = JsonFormUtils.getFieldValue(jsonPayload, "perineum_repair_occupation");
            perineum_repair_person_name = JsonFormUtils.getFieldValue(jsonPayload, "perineum_repair_person_name");
            cervix_observation = JsonFormUtils.getFieldValue(jsonPayload, "cervix_observation");
            systolic = JsonFormUtils.getFieldValue(jsonPayload, "systolic");
            diastolic = JsonFormUtils.getFieldValue(jsonPayload, "diastolic");
            pulse_rate = JsonFormUtils.getFieldValue(jsonPayload, "pulse_rate");
            temperature = JsonFormUtils.getFieldValue(jsonPayload, "temperature");
            uterus_contraction = JsonFormUtils.getFieldValue(jsonPayload, "uterus_contraction");
            urination = JsonFormUtils.getFieldValue(jsonPayload, "urination");
            observation_date = JsonFormUtils.getFieldValue(jsonPayload, "observation_date");
            observation_time = JsonFormUtils.getFieldValue(jsonPayload, "observation_time");
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
            try {

                JSONObject jsonObject = new JSONObject(jsonPayload);
                JSONArray fields = JsonFormUtils.fields(jsonObject);

                JSONObject mother_observation_module_status = JsonFormUtils.getFieldJSONObject(fields, "mother_observation_module_status");
                assert mother_observation_module_status != null;
                mother_observation_module_status.remove(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE);
                mother_observation_module_status.put(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE, completionStatus);

                return jsonObject.toString();

            } catch (Exception e) {
                Timber.e(e);
            }
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            if (isFullyCompleted()) {
                completionStatus = context.getString(R.string.lb_fully_completed_action);
            } else if (isPartiallyCompleted()) {
                completionStatus = context.getString(R.string.lb_partially_completed_action);
            }
            return completionStatus;
        }

        @Override
        public BaseLDVisitAction.Status evaluateStatusOnPayload() {
            if (isFullyCompleted()) {
                return BaseLDVisitAction.Status.COMPLETED;
            } else if (isPartiallyCompleted()) {
                return BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
            } else {
                return BaseLDVisitAction.Status.PENDING;
            }
        }

        @Override
        public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {
            //Todo: Implement here
        }

        private boolean isFullyCompleted() {
            boolean complete = false;
            if (StringUtils.isNotBlank(vagina_observation) && StringUtils.isNotBlank(perineum_observation) &&
                    StringUtils.isNotBlank(cervix_observation) && StringUtils.isNotBlank(systolic) &&
                    StringUtils.isNotBlank(diastolic) && StringUtils.isNotBlank(pulse_rate) &&
                    StringUtils.isNotBlank(temperature) && StringUtils.isNotBlank(uterus_contraction) &&
                    StringUtils.isNotBlank(urination) && StringUtils.isNotBlank(observation_date) && StringUtils.isNotBlank(observation_time)) {
                complete = true;
                if (vagina_observation.contains("chk_bleeding") && perineum_observation.contains("tear")) {
                    complete = StringUtils.isNotBlank(vaginal_bleeding_observation) && StringUtils.isNotBlank(degree_of_perineum_tear) &&
                            (StringUtils.isNotBlank(perineum_repair_occupation) && !perineum_repair_occupation.contains("Perineum repaired by")) && StringUtils.isNotBlank(perineum_repair_person_name);
                } else if (vagina_observation.contains("chk_bleeding")) {
                    complete = StringUtils.isNotBlank(vaginal_bleeding_observation);
                } else if (perineum_observation.contains("tear")) {
                    complete = StringUtils.isNotBlank(degree_of_perineum_tear) && StringUtils.isNotBlank(perineum_repair_occupation) &&
                            StringUtils.isNotBlank(perineum_repair_person_name);
                }
            }

            return complete;
        }

        private boolean isPartiallyCompleted() {
            boolean partialCompletion = false;
            if (StringUtils.isNotBlank(vagina_observation)) {
                if (StringUtils.isBlank(perineum_observation) ||
                        StringUtils.isBlank(cervix_observation) || StringUtils.isBlank(systolic) ||
                        StringUtils.isBlank(diastolic) || StringUtils.isBlank(pulse_rate) ||
                        StringUtils.isBlank(temperature) || StringUtils.isBlank(uterus_contraction) ||
                        StringUtils.isBlank(urination) || StringUtils.isBlank(observation_date) || StringUtils.isBlank(observation_time)) {
                    partialCompletion = true;
                } else {
                    if (vagina_observation.contains("chk_bleeding") && perineum_observation.contains("tear")) {
                        partialCompletion = StringUtils.isBlank(vaginal_bleeding_observation) || StringUtils.isBlank(degree_of_perineum_tear) ||
                                (StringUtils.isNotBlank(perineum_repair_occupation) && perineum_repair_occupation.contains("Perineum repaired by")) ||
                                StringUtils.isBlank(perineum_repair_person_name);
                    } else if (vagina_observation.contains("chk_bleeding")) {
                        partialCompletion = StringUtils.isBlank(vaginal_bleeding_observation);
                    } else if (perineum_observation.contains("tear")) {
                        partialCompletion = StringUtils.isBlank(degree_of_perineum_tear) || (StringUtils.isNotBlank(perineum_repair_occupation) &&
                                perineum_repair_occupation.contains("Perineum repaired by")) || StringUtils.isBlank(perineum_repair_person_name);
                    }
                }
            }
            return partialCompletion;
        }
    }

    private static class MaternalComplicationLabourActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

        private JSONArray maternal_complication_values;
        private String completionStatus;
        private String jsonString;
        private Context context;

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
            this.context = context;
            this.jsonString = jsonString;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            String maternal_complications_during_labour = JsonFormUtils.getFieldValue(jsonPayload, "maternal_complications_during_labour");
            try {
                maternal_complication_values = new JSONArray(maternal_complications_during_labour);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
            try {

                JSONObject jsonObject = new JSONObject(jsonPayload);
                JSONArray fields = JsonFormUtils.fields(jsonObject);

                JSONObject maternal_complications_module_status = JsonFormUtils.getFieldJSONObject(fields, "maternal_complications_module_status");
                assert maternal_complications_module_status != null;
                maternal_complications_module_status.remove(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE);
                maternal_complications_module_status.put(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE, completionStatus);

                return jsonObject.toString();

            } catch (Exception e) {
                Timber.e(e);
            }
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            if (maternal_complication_values.length() > 0) {
                completionStatus = context.getString(R.string.lb_fully_completed_action);
            } else {
                completionStatus = context.getString(R.string.lb_partially_completed_action);
            }
            return completionStatus;
        }

        @Override
        public BaseLDVisitAction.Status evaluateStatusOnPayload() {
            if (maternal_complication_values.length() > 0) {
                return BaseLDVisitAction.Status.COMPLETED;
            } else {
                return BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
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
        private String baseEntityId;
        private Context context;
        private String deliveryDate;
        private String deliveryTime;
        private int numberOfChildrenBorn;
        private String status;

        public NewBornActionHelper(String baseEntityId, String deliveryDate, String deliveryTime, int numberOfChildrenBorn, String status) {
            this.baseEntityId = baseEntityId;
            this.deliveryDate = deliveryDate;
            this.deliveryTime = deliveryTime;
            this.numberOfChildrenBorn = numberOfChildrenBorn;
            this.status = status;
        }

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
            this.context = context;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public String getPreProcessed() {
            JSONObject newBornForm = org.smartregister.chw.core.utils.FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LDPostDeliveryMotherManagement.getLdNewBornStatus());
            String hivStatus = LDDao.getHivStatus(baseEntityId);

            try {
                newBornForm.getJSONObject("global").put("delivery_date", deliveryDate);
                newBornForm.getJSONObject("global").put("delivery_time", deliveryTime);
                newBornForm.getJSONObject("global").put("number_of_children_born", numberOfChildrenBorn);
            } catch (Exception e) {
                Timber.e(e);
            }

            JSONArray fields = null;

            if (fields != null && hivStatus != null && !hivStatus.equalsIgnoreCase(POSITIVE)) {
                try {
                    for (int x = 0; x < fields.length(); x++) {
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("risk_category"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("prompt_for_high_risk"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("prompt_for_low_risk"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("provided_azt_nvp_syrup"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("provided_other_combinations"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("specify_the_combinations"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("number_of_azt_nvp_days_dispensed"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reason_for_not_providing_other_combination"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("other_reason_for_not_providing_other_combination"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("collect_dbs"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reason_not_collecting_dbs"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("sample_id"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("sample_collection_date"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("dna_pcr_collection_time"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("provided_nvp_syrup"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("number_of_nvp_days_dispensed"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reason_for_not_providing_nvp_syrup"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("other_reason_for_not_providing_nvp_syrup"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("prophylaxis_arv_for_high_risk_given"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("prophylaxis_arv_for_high_and_low_risk_given"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("visit_number"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("next_facility_visit_date"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("followup_visit_date"))
                            fields.remove(x);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reason_for_not_breast_feeding_within_one_hour") && status.equals("died")) {
                            fields.getJSONObject(x).getJSONArray("options").remove(0);
                        }
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reasons_for_not_keeping_the_baby_warm_skin_to_skin_for_normal_apgar_score") && status.equals("died")) {
                            fields.getJSONObject(x).getJSONArray("options").remove(0);
                        }
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reasons_for_not_keeping_the_baby_warm_skin_to_skin_for_low_apgar_score") && status.equals("died")) {
                            fields.getJSONObject(x).getJSONArray("options").remove(0);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (fields != null && hivStatus != null && hivStatus.equalsIgnoreCase(POSITIVE)) {
                try {
                    for (int x = 0; x < fields.length(); x++) {
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("sample_collection_date"))
                            fields.getJSONObject(x).put("min_date", deliveryDate);
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reason_for_not_breast_feeding_within_one_hour") && status.equals("died")) {
                            fields.getJSONObject(x).getJSONArray("options").remove(0);
                        }
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reasons_for_not_keeping_the_baby_warm_skin_to_skin_for_normal_apgar_score") && status.equals("died")) {
                            fields.getJSONObject(x).getJSONArray("options").remove(0);
                        }
                        if (fields.getJSONObject(x).getString(KEY).equalsIgnoreCase("reasons_for_not_keeping_the_baby_warm_skin_to_skin_for_low_apgar_score") && status.equals("died")) {
                            fields.getJSONObject(x).getJSONArray("options").remove(0);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return newBornForm.toString();
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

            AllSharedPreferences allSharedPreferences = ImmunizationLibrary.getInstance().context().allSharedPreferences();

            JSONObject visitJson = new JSONObject(visit.getJson());
            JSONArray obs = visitJson.getJSONArray("obs");
            String deliveryDate = getDeliveryDateString(obs);

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
            if (isChildAlive(obs)) {
                saveChild(memberID, LDDao.getHivStatus(memberID), getRiskStatus(obs), allSharedPreferences, memberObject.getFamilyBaseEntityId(), getDeliveryDateString(obs), obs);
            }

            boolean visitCompleted = true;
            for (Map.Entry<String, BaseLDVisitAction> entry : actionList.entrySet()) {
                String actionStatus = entry.getValue().getActionStatus().toString();
                if (actionStatus.equalsIgnoreCase("PARTIALLY_COMPLETED")) {
                    visitCompleted = false;

                }
            }

            if (visitCompleted) {
                LDVisitUtils.processVisits(memberID, false);
            }
        } catch (Exception e) {
            Timber.e(e);
        }

    }

    private void saveChild(String motherBaseId, String motherHivStatus, String childRiskCategory, AllSharedPreferences
            allSharedPreferences, String familyBaseEntityId, String dob, JSONArray obs) {
        String uniqueChildID = AncLibrary.getInstance().getUniqueIdRepository().getNextUniqueId().getOpenmrsId();

        if (StringUtils.isNotBlank(uniqueChildID)) {
            String childBaseEntityId = org.smartregister.chw.anc.util.JsonFormUtils.generateRandomUUIDString();
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
