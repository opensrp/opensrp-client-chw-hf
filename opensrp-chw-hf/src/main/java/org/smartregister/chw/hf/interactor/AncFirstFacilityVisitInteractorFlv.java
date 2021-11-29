package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.AncBirthReviewAction;
import org.smartregister.chw.hf.actionhelper.AncTtVaccinationAction;
import org.smartregister.chw.hf.repository.HfLocationRepository;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.ContactUtil;
import org.smartregister.chw.referral.util.JsonFormConstants;
import org.smartregister.domain.Location;
import org.smartregister.domain.LocationTag;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.LocationTagRepository;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class AncFirstFacilityVisitInteractorFlv implements AncFirstFacilityVisitInteractor.Flavor {
    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();

        Context context = view.getContext();

        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.ANC_FIRST_FACILITY_VISIT);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        // get contact
        LocalDate lastContact = new DateTime(memberObject.getDateCreated()).toLocalDate();
        boolean isFirst = (StringUtils.isBlank(memberObject.getLastContactVisit()));
        LocalDate lastMenstrualPeriod = new LocalDate();
        try {
            lastMenstrualPeriod = DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(memberObject.getLastMenstrualPeriod());
        } catch (Exception e) {
            Timber.e(e);
        }


        if (StringUtils.isNotBlank(memberObject.getLastContactVisit())) {
            lastContact = DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(memberObject.getLastContactVisit());
        }

        Map<Integer, LocalDate> dateMap = new LinkedHashMap<>();

        // today is the due date for the very first visit
        if (isFirst) {
            dateMap.put(0, LocalDate.now());
        }

        dateMap.putAll(ContactUtil.getContactWeeks(isFirst, lastContact, lastMenstrualPeriod));

        evaluateMedicalAndSurgicalHistory(actionList, details, memberObject, context);

        return actionList;
    }

    private void evaluateMedicalAndSurgicalHistory(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                                   Map<String, List<VisitDetail>> details,
                                                   final MemberObject memberObject,
                                                   final Context context) throws BaseAncHomeVisitAction.ValidationException {
        JSONObject obstetricForm = null;
        try {
            obstetricForm = FormUtils.getFormUtils().getFormJson(Constants.JSON_FORM.ANC_FIRST_VISIT.OBSTETRIC_EXAMINATION);
            obstetricForm.getJSONObject("global").put("last_menstrual_period", memberObject.getLastMenstrualPeriod());
            if (details != null && !details.isEmpty()) {
                JsonFormUtils.populateForm(obstetricForm, details);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }


        BaseAncHomeVisitAction medicalAndSurgicalHistory = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_first_visit_medical_and_surgical_history))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.ANC_FIRST_VISIT.getMedicalAndSurgicalHistory())
                .withHelper(new AncMedicalAndSurgicalHistoryAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_first_visit_medical_and_surgical_history), medicalAndSurgicalHistory);

        BaseAncHomeVisitAction obstetricExaminationAction = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_first_visit_obstetric_examination))
                .withOptional(true)
                .withDetails(details)
                .withJsonPayload(obstetricForm.toString())
                .withFormName(Constants.JSON_FORM.ANC_FIRST_VISIT.getObstetricExamination())
                .withHelper(new AncObstetricExaminationAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_first_visit_obstetric_examination), obstetricExaminationAction);

        BaseAncHomeVisitAction baselineInvestigationAction = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_first_visit_baseline_investigation))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.ANC_FIRST_VISIT.getBaselineInvestigation())
                .withHelper(new AncBaselineInvestigationAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_first_visit_baseline_investigation), baselineInvestigationAction);

        BaseAncHomeVisitAction vaccinationAction = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_first_visit_tt_vaccination))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.ANC_FIRST_VISIT.getTtVaccination())
                .withHelper(new AncTtVaccinationAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_first_visit_tt_vaccination), vaccinationAction);

        JSONObject birthReviewForm = initializeHealthFacilitiesList(FormUtils.getFormUtils().getFormJson(Constants.JSON_FORM.ANC_RECURRING_VISIT.BIRTH_REVIEW_AND_EMERGENCY_PLAN));
        BaseAncHomeVisitAction birthReview = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_recuring_visit_review_birth_and_emergency_plan))
                .withOptional(true)
                .withDetails(details)
                .withJsonPayload(birthReviewForm.toString())
                .withFormName(Constants.JSON_FORM.ANC_RECURRING_VISIT.getBirthReviewAndEmergencyPlan())
                .withHelper(new AncBirthReviewAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_recuring_visit_review_birth_and_emergency_plan), birthReview);
    }


    private class AncMedicalAndSurgicalHistoryAction extends org.smartregister.chw.hf.actionhelper.AncMedicalAndSurgicalHistoryAction {
        private String medical_and_surgical_history_present;
        private Context context;

        public AncMedicalAndSurgicalHistoryAction(MemberObject memberObject) {
            super(memberObject);
        }

        @Override
        public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                medical_and_surgical_history_present = CoreJsonFormUtils.getCheckBoxValue(jsonObject, "anc_medical_surgical_history");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String s) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return MessageFormat.format("Medical and Surgical History : {0}", medical_and_surgical_history_present);
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(medical_and_surgical_history_present)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            } else {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.v("onPayloadReceived");
        }
    }

    private class AncObstetricExaminationAction extends org.smartregister.chw.hf.actionhelper.AncObstetricExaminationAction {
        private String abdominal_scars;
        private Context context;

        public AncObstetricExaminationAction(MemberObject memberObject) {
            super(memberObject);
        }

        @Override
        public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(abdominal_scars))
                return BaseAncHomeVisitAction.Status.PENDING;
            else {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                abdominal_scars = CoreJsonFormUtils.getValue(jsonObject, "abdominal_scars");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String s) {
            return null;
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.v("onPayloadReceived");
        }

        @Override
        public String evaluateSubTitle() {
            if (!StringUtils.isBlank(abdominal_scars))
                return "Obstetric Examination Complete";
            return "";
        }
    }

    private static JSONObject initializeHealthFacilitiesList(JSONObject form) {
        HfLocationRepository locationRepository = new HfLocationRepository();
        List<Location> locations = locationRepository.getAllLocationsWithTags();
        if (locations != null && form != null) {

            Collections.sort(locations, (location1, location2) -> StringUtils.capitalize(location1.getProperties().getName()).compareTo(StringUtils.capitalize(location2.getProperties().getName())));
            try {
                JSONArray fields = form.getJSONObject(Constants.JsonFormConstants.STEP1)
                        .getJSONArray(JsonFormConstants.FIELDS);
                JSONObject referralHealthFacilities = null;
                for (int i = 0; i < fields.length(); i++) {
                    if (fields.getJSONObject(i)
                            .getString(JsonFormConstants.KEY).equals(Constants.JsonFormConstants.NAME_OF_HF)
                    ) {
                        referralHealthFacilities = fields.getJSONObject(i);
                        break;
                    }
                }

                ArrayList<String> healthFacilitiesOptions = new ArrayList<>();
                ArrayList<String> healthFacilitiesIds = new ArrayList<>();
                for (Location location : locations) {
                    Set<LocationTag> locationTags = location.getLocationTags();
                   if(locationTags.iterator().next().getName().equalsIgnoreCase("Facility") ){
                        healthFacilitiesOptions.add(StringUtils.capitalize(location.getProperties().getName()));
                        healthFacilitiesIds.add(location.getProperties().getUid());
                   }
                }
                healthFacilitiesOptions.add("Other");
                healthFacilitiesIds.add("Other");

                JSONObject openmrsChoiceIds = new JSONObject();
                int size = healthFacilitiesOptions.size();
                for (int i = 0; i < size; i++) {
                    openmrsChoiceIds.put(healthFacilitiesOptions.get(i), healthFacilitiesIds.get(i));
                }
                if (referralHealthFacilities != null) {
                    referralHealthFacilities.put("values", new JSONArray(healthFacilitiesOptions));
                    referralHealthFacilities.put("keys", new JSONArray(healthFacilitiesOptions));
                    referralHealthFacilities.put("openmrs_choice_ids", openmrsChoiceIds);
                }
            } catch (JSONException e) {
                Timber.e(e);
            }

        }
        return form;
    }

    private class AncBaselineInvestigationAction extends org.smartregister.chw.hf.actionhelper.AncBaselineInvestigationAction {
        private String glucose_in_urine;
        private Context context;

        public AncBaselineInvestigationAction(MemberObject memberObject) {
            super(memberObject);
        }

        @Override
        public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                glucose_in_urine = CoreJsonFormUtils.getValue(jsonObject, "glucose_in_urine");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String s) {
            return null;
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.v("onPayloadReceived");
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(glucose_in_urine))
                return BaseAncHomeVisitAction.Status.PENDING;
            else {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
        }

        @Override
        public String evaluateSubTitle() {
            if (!StringUtils.isBlank(glucose_in_urine))
                return "Baseline Investigation Conducted";
            return "";
        }
    }

}

