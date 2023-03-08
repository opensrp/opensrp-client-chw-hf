package org.smartregister.chw.hf.actionhelper;

import static org.smartregister.chw.hf.interactor.LDPostDeliveryManagementMotherActivityInteractor.ordinal;
import static org.smartregister.util.JsonFormUtils.KEY;
import static org.smartregister.util.JsonFormUtils.VALUE;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.AppExecutors;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.LDDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.contract.BaseLDVisitContract;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.chw.referral.util.JsonFormConstants;
import org.smartregister.util.JsonFormUtils;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-07-04
 */
public class MotherStatusActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    protected String status;
    private String cause_of_death;
    private String time_of_death;
    private String delivery_place;
    private String designation_of_delivery_personnel;
    private String name_of_delivery_person;
    private String delivery_date;
    private String completionStatus;
    private int numberOfChildrenBorn = 0;
    private String delivery_time;
    private Context context;
    private final String baseEntityId;
    private final LinkedHashMap<String, BaseLDVisitAction> actionList;
    private final BaseLDVisitContract.InteractorCallBack callBack;
    private Map<String, List<VisitDetail>> details;
    private final boolean isEdit;

    public MotherStatusActionHelper(Context context, String baseEntityId, LinkedHashMap<String, BaseLDVisitAction> actionList, BaseLDVisitContract.InteractorCallBack callBack, boolean isEdit) {
        this.context = context;
        this.baseEntityId = baseEntityId;
        this.actionList = actionList;
        this.callBack = callBack;
        this.isEdit = isEdit;
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        this.context = context;
        this.details = details;
    }

    @Override
    public String getPreProcessed() {
        JSONObject motherStatusForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LDPostDeliveryMotherManagement.getLdPostDeliveryManagementMotherStatus());
        try {
            JSONArray fields = motherStatusForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);

            String modeOfDelivery = LDDao.getModeOfDelivery(baseEntityId);

            if (modeOfDelivery != null && !modeOfDelivery.isEmpty()) {
                JSONObject modeOfDeliveryQuestion = JsonFormUtils.getFieldJSONObject(fields, "mode_of_delivery");
                modeOfDeliveryQuestion.put("type", "hidden");

            }

            if (modeOfDelivery != null && (modeOfDelivery.equalsIgnoreCase("cesarean") || modeOfDelivery.equalsIgnoreCase("vacuum_extraction"))) {
                JSONObject placeOfDelivery = JsonFormUtils.getFieldJSONObject(fields, "delivery_place");
                placeOfDelivery.getJSONArray("values").remove(3);
                placeOfDelivery.getJSONArray("values").remove(2);
                placeOfDelivery.getJSONArray("values").remove(1);
                placeOfDelivery.getJSONArray("keys").remove(3);
                placeOfDelivery.getJSONArray("keys").remove(2);
                placeOfDelivery.getJSONArray("keys").remove(1);
                placeOfDelivery.getJSONArray("openmrs_choice_ids").remove(3);
                placeOfDelivery.getJSONArray("openmrs_choice_ids").remove(2);
                placeOfDelivery.getJSONArray("openmrs_choice_ids").remove(1);
            }

            JSONObject hivJsonObject = JsonFormUtils.getFieldJSONObject(fields, "hiv");
            String hivStatus = LDDao.getHivStatus(baseEntityId);
            if (hivJsonObject != null && hivStatus != null) {
                hivJsonObject.put(VALUE, hivStatus);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }

        return motherStatusForm.toString();
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        status = JsonFormUtils.getFieldValue(jsonPayload, "status");
        cause_of_death = JsonFormUtils.getFieldValue(jsonPayload, "cause_of_death");
        time_of_death = JsonFormUtils.getFieldValue(jsonPayload, "time_of_death");
        delivery_place = JsonFormUtils.getFieldValue(jsonPayload, "delivery_place");
        designation_of_delivery_personnel = JsonFormUtils.getFieldValue(jsonPayload, "designation_of_delivery_personnel");
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
            updateDeliveryDuration(jsonPayload, fields);

            Iterator<Map.Entry<String, BaseLDVisitAction>> itr = actionList.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, BaseLDVisitAction> entry = itr.next();
                if (entry.getKey().contains(MessageFormat.format(context.getString(R.string.ld_new_born_status_action_title), ""))) {
                    itr.remove();
                }
            }

        } catch (Exception e) {
            Timber.e(e);
        }

        if (numberOfChildrenBorn > 0) {
            startActionForChild();
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
                    completed = !delivery_place.equalsIgnoreCase("at_a_health_facility") || (StringUtils.isNotBlank(designation_of_delivery_personnel) &&
                            StringUtils.isNotBlank(name_of_delivery_person));
                }
            } else {
                if ((StringUtils.isNotBlank(delivery_place) && !delivery_place.equalsIgnoreCase("Place of delivery")) &&
                        StringUtils.isNotBlank(cause_of_death) && StringUtils.isNotBlank(time_of_death) && StringUtils.isNotBlank(delivery_date)) {
                    completed = !delivery_place.equalsIgnoreCase("at_a_health_facility") || (StringUtils.isNotBlank(designation_of_delivery_personnel) &&
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
                } else if (delivery_place.equalsIgnoreCase("at_a_health_facility")) {
                    partialCompletion = StringUtils.isBlank(designation_of_delivery_personnel) || StringUtils.isBlank(name_of_delivery_person);
                }
            } else {
                if (delivery_place.equalsIgnoreCase("Place of delivery") || StringUtils.isBlank(delivery_date) || StringUtils.isBlank(cause_of_death) ||
                        StringUtils.isBlank(time_of_death)) {
                    partialCompletion = true;
                } else if (delivery_place.equalsIgnoreCase("at_a_health_facility")) {
                    partialCompletion = StringUtils.isBlank(designation_of_delivery_personnel) || StringUtils.isBlank(name_of_delivery_person);
                }
            }
        }
        return partialCompletion;
    }

    private void updateDeliveryDuration(String jsonPayload, JSONArray fields) throws ParseException, JSONException {
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
    }

    private void startActionForChild() {
        for (int i = 0; i < numberOfChildrenBorn; i++) {
            // Get visit details for each individual child
            try {
                if (isEdit) {
                    Visit lastVisit = LDLibrary.getInstance().visitRepository().getLatestVisit(baseEntityId, "Post Delivery Mother Management");
                    if (lastVisit != null) {
                        Visit lastImmediateNewBornCareVisit = LDLibrary.getInstance().visitRepository().getVisitsByParentVisitId(lastVisit.getVisitId(), "LND " + ordinal(i + 1) + " Newborn").get(0);

                        if (lastImmediateNewBornCareVisit != null) {
                            details = org.smartregister.chw.ld.util.VisitUtils.getVisitGroups(LDLibrary.getInstance().visitDetailsRepository().getVisits(lastImmediateNewBornCareVisit.getVisitId()));
                        }
                    }
                }
            } catch (Exception e){
                Timber.e(e);
            }

            String title;
            if (numberOfChildrenBorn == 1) {
                title = MessageFormat.format(context.getString(R.string.ld_new_born_status_action_title), "");
            } else {
                title = MessageFormat.format(context.getString(R.string.ld_new_born_status_action_title), "of " + ordinal(i + 1) + " baby");
            }
            NewBornActionHelper actionHelper = new NewBornActionHelper(baseEntityId, delivery_date, delivery_time, numberOfChildrenBorn, status, ordinal(i + 1));
            BaseLDVisitAction action = null;
            try {
                action = new BaseLDVisitAction.Builder(context, title)
                        .withOptional(false)
                        .withHelper(actionHelper)
                        .withDetails(details)
                        .withBaseEntityID(org.smartregister.chw.anc.util.JsonFormUtils.generateRandomUUIDString())
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
}
