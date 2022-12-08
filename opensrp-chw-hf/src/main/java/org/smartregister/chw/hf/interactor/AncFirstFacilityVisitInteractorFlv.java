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
import org.smartregister.chw.hf.actionhelper.AncBaselineInvestigationAction;
import org.smartregister.chw.hf.actionhelper.AncBirthReviewAction;
import org.smartregister.chw.hf.actionhelper.AncCounsellingAction;
import org.smartregister.chw.hf.actionhelper.AncMalariaInvestigationAction;
import org.smartregister.chw.hf.actionhelper.AncNextFollowupVisitAction;
import org.smartregister.chw.hf.actionhelper.AncObstetricExaminationAction;
import org.smartregister.chw.hf.actionhelper.AncPharmacyAction;
import org.smartregister.chw.hf.actionhelper.AncTbScreeningAction;
import org.smartregister.chw.hf.actionhelper.AncTtVaccinationAction;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.hf.repository.HfLocationRepository;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.ContactUtil;
import org.smartregister.chw.hf.utils.HfAncJsonFormUtils;
import org.smartregister.chw.hiv.dao.HivDao;
import org.smartregister.chw.referral.util.JsonFormConstants;
import org.smartregister.domain.Location;
import org.smartregister.domain.LocationTag;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class AncFirstFacilityVisitInteractorFlv implements AncFirstFacilityVisitInteractor.Flavor {
    private boolean editMode;
    LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();

    public static JSONObject initializeHealthFacilitiesList(JSONObject form) {
        HfLocationRepository locationRepository = new HfLocationRepository();
        List<Location> locations = locationRepository.getAllLocationsWithTags();
        if (locations != null && form != null) {

            try {

                JSONArray fields = form.getJSONObject(Constants.JsonFormConstants.STEP1)
                        .getJSONArray(JsonFormConstants.FIELDS);

                JSONObject referralHealthFacilities = org.smartregister.family.util.JsonFormUtils.getFieldJSONObject(fields, Constants.JsonFormConstants.NAME_OF_HF);

                JSONArray options = referralHealthFacilities.getJSONArray("options");
                String healthFacilityTagName = "Facility";
                for (Location location : locations) {
                    Set<LocationTag> locationTags = location.getLocationTags();
                    if (locationTags.iterator().next().getName().equalsIgnoreCase(healthFacilityTagName)) {
                        JSONObject optionNode = new JSONObject();
                        optionNode.put("text", StringUtils.capitalize(location.getProperties().getName()));
                        optionNode.put("key", StringUtils.capitalize(location.getProperties().getName()));
                        JSONObject propertyObject = new JSONObject();
                        propertyObject.put("presumed-id", location.getProperties().getUid());
                        propertyObject.put("confirmed-id", location.getProperties().getUid());
                        optionNode.put("property", propertyObject);

                        options.put(optionNode);
                    }
                }
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
        return form;
    }

    private static JSONObject setMinFundalHeight(JSONObject form, String baseEntityId, Context context) {
        String fundalHeight = HfAncDao.getFundalHeight(baseEntityId);
        try {
            JSONArray fields = form.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
            JSONObject fundalHeightQnObj = null;
            for (int i = 0; i < fields.length(); i++) {
                if (fields.getJSONObject(i).getString(JsonFormConstants.KEY).equals("fundal_height")) {
                    fundalHeightQnObj = fields.getJSONObject(i);
                    break;
                }
            }

            assert fundalHeightQnObj != null;
            JSONObject v_min = fundalHeightQnObj.getJSONObject("v_min");
            v_min.put("value", fundalHeight);
            v_min.put("err", context.getString(R.string.anc_fundal_height_min_err) + " " + fundalHeight + " CM");


        } catch (JSONException e) {
            Timber.e(e);
        }
        return form;
    }

    private static JSONObject firstPregnancyAboveThirtyFive(JSONObject form, MemberObject memberObject, Context context) throws JSONException {

        JSONArray fields = form.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
        JSONObject medicalSurgicalHistory = null;
        for (int i = 0; i < fields.length(); i++) {
            if (fields.getJSONObject(i).getString(JsonFormConstants.KEY).equals("medical_surgical_history")) {
                medicalSurgicalHistory = fields.getJSONObject(i);
                break;
            }
        }

        JSONArray options = medicalSurgicalHistory.getJSONArray("options");

        JSONObject pregnantAtAboveThirtyFive = new JSONObject();
        pregnantAtAboveThirtyFive.put("key", "first_pregnancy_at_or_above_thirty_five");
        pregnantAtAboveThirtyFive.put("text", context.getString(R.string.first_pregnancy_option));
        pregnantAtAboveThirtyFive.put("value", false);
        pregnantAtAboveThirtyFive.put("openmrs_entity", "concept");
        pregnantAtAboveThirtyFive.put("openmrs_entity_id", "first_pregnancy_at_or_above_thirty_five");

        JSONObject none = new JSONObject();
        none.put("key", "none");
        none.put("text", context.getString(R.string.none_option_for_medical_surgical_history));
        none.put("value", false);
        none.put("openmrs_entity", "concept");
        none.put("openmrs_entity_id", "none");

        if (memberObject.getAge() >= 35) {
            options.put(pregnantAtAboveThirtyFive);
        }

        options.put(none);

        return form;
    }

    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {

        Context context = view.getContext();
        this.editMode = view.getEditMode();

        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (editMode) {
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

        evaluateMedicalAndSurgicalHistory(actionList, details, memberObject, context, callBack);

        return actionList;
    }

    private void evaluateMedicalAndSurgicalHistory(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                                   Map<String, List<VisitDetail>> details,
                                                   final MemberObject memberObject,
                                                   final Context context,
                                                   BaseAncHomeVisitContract.InteractorCallBack callBack
    ) throws BaseAncHomeVisitAction.ValidationException {
        JSONObject obstetricForm = null;
        try {
            obstetricForm = setMinFundalHeight(FormUtils.getFormUtils().getFormJson(Constants.JsonForm.AncFirstVisit.OBSTETRIC_EXAMINATION), memberObject.getBaseEntityId(), context);
            obstetricForm.getJSONObject("global").put("last_menstrual_period", memberObject.getLastMenstrualPeriod());
            if (details != null && !details.isEmpty()) {
                HfAncJsonFormUtils.populateForm(obstetricForm, details);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        JSONObject medicalSurgicalHistoryForm = null;
        try {
            medicalSurgicalHistoryForm = firstPregnancyAboveThirtyFive(FormUtils.getFormUtils().getFormJson(Constants.JsonForm.AncFirstVisit.getMedicalAndSurgicalHistory()), memberObject, context);

            if (HivDao.isRegisteredForHiv(memberObject.getBaseEntityId())) {
                JSONArray fields = medicalSurgicalHistoryForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);

                JSONObject questionField = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "medical_surgical_history");
                JSONArray options = org.smartregister.util.JsonFormUtils.getJSONArray(questionField, "options");
                JSONObject knownOnArtOption = org.smartregister.util.JsonFormUtils.getFieldJSONObject(options, "known_on_art");
                knownOnArtOption.put(JsonFormUtils.VALUE, true);

                JSONObject ctcNumber = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "ctc_number");
                ctcNumber.put(JsonFormUtils.VALUE, HivDao.getMember(memberObject.getBaseEntityId()).getCtcNumber());
                ctcNumber.put("read_only", true);
            }

            if (memberObject.getGravida() != null && !HfAncDao.getParity(memberObject.getBaseEntityId()).isEmpty()) {
                JSONArray fields = medicalSurgicalHistoryForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                JSONObject gravida = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "gravida_text");
                JSONObject parity = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "parity_text");
                gravida.put(JsonFormUtils.VALUE, memberObject.getGravida());
                parity.put(JsonFormUtils.VALUE, HfAncDao.getParity(memberObject.getBaseEntityId()));

                if (!HfAncDao.getNumberOfSurvivingChildren(memberObject.getBaseEntityId()).isEmpty()) {
                    JSONObject no_surv_children = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "no_surv_children_text");
                    no_surv_children.put(JsonFormUtils.VALUE, HfAncDao.getNumberOfSurvivingChildren(memberObject.getBaseEntityId()));
                }
            }

            if (details != null && !details.isEmpty()) {
                HfAncJsonFormUtils.populateForm(medicalSurgicalHistoryForm, details);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        BaseAncHomeVisitAction medicalAndSurgicalHistory = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_first_visit_medical_and_surgical_history))
                .withOptional(false)
                .withDetails(details)
                .withJsonPayload(medicalSurgicalHistoryForm.toString())
                .withFormName(Constants.JsonForm.AncFirstVisit.getMedicalAndSurgicalHistory())
                .withHelper(new AncMedicalAndSurgicalHistoryAction(memberObject, details, callBack))
                .build();
        actionList.put(context.getString(R.string.anc_first_visit_medical_and_surgical_history), medicalAndSurgicalHistory);

        BaseAncHomeVisitAction obstetricExaminationAction = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_first_visit_obstetric_examination))
                .withOptional(true)
                .withDetails(details)
                .withJsonPayload(obstetricForm.toString())
                .withFormName(Constants.JsonForm.AncFirstVisit.getObstetricExamination())
                .withHelper(new AncObstetricExaminationAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_first_visit_obstetric_examination), obstetricExaminationAction);


        JSONObject counsellingForm = null;
        try {
            counsellingForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getCounselling());

            if (details != null && !details.isEmpty()) {
                HfAncJsonFormUtils.populateForm(counsellingForm, details);
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        evaluateBaselineInvestigation(memberObject, context, details, false);

        try {
            BaseAncHomeVisitAction TbScreening = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.tb_screening_title))
                    .withOptional(true)
                    .withDetails(details)
                    .withFormName(Constants.JsonForm.AncFirstVisit.getTbScreening())
                    .withHelper(new AncTbScreeningAction(memberObject))
                    .build();
            actionList.put(context.getString(R.string.tb_screening_title), TbScreening);

        } catch (
                BaseAncHomeVisitAction.ValidationException e) {
            e.printStackTrace();
        }
        JSONObject malariaInvestigationForm = null;
        try {
            malariaInvestigationForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.AncFirstVisit.getMalariaInvestigation());
            malariaInvestigationForm.getJSONObject("global").put("gestational_age", memberObject.getGestationAge());

            if (editMode) {
                malariaInvestigationForm.getJSONObject("global").put("malaria_preventive_therapy_ipt1", HfAncDao.previousMalariaIptDosage("malaria_preventive_therapy_ipt1", memberObject.getBaseEntityId()));
                malariaInvestigationForm.getJSONObject("global").put("malaria_preventive_therapy_ipt2", HfAncDao.previousMalariaIptDosage("malaria_preventive_therapy_ipt2", memberObject.getBaseEntityId()));
                malariaInvestigationForm.getJSONObject("global").put("malaria_preventive_therapy_ipt3", HfAncDao.previousMalariaIptDosage("malaria_preventive_therapy_ipt3", memberObject.getBaseEntityId()));
                malariaInvestigationForm.getJSONObject("global").put("malaria_preventive_therapy_ipt4", HfAncDao.previousMalariaIptDosage("malaria_preventive_therapy_ipt4", memberObject.getBaseEntityId()));
            } else {
                malariaInvestigationForm.getJSONObject("global").put("malaria_preventive_therapy_ipt1", HfAncDao.malariaIptDosage("malaria_preventive_therapy_ipt1", memberObject.getBaseEntityId()));
                malariaInvestigationForm.getJSONObject("global").put("malaria_preventive_therapy_ipt2", HfAncDao.malariaIptDosage("malaria_preventive_therapy_ipt2", memberObject.getBaseEntityId()));
                malariaInvestigationForm.getJSONObject("global").put("malaria_preventive_therapy_ipt3", HfAncDao.malariaIptDosage("malaria_preventive_therapy_ipt3", memberObject.getBaseEntityId()));
                malariaInvestigationForm.getJSONObject("global").put("malaria_preventive_therapy_ipt4", HfAncDao.malariaIptDosage("malaria_preventive_therapy_ipt4", memberObject.getBaseEntityId()));
            }
            if (details != null && !details.isEmpty()) {
                HfAncJsonFormUtils.populateForm(malariaInvestigationForm, details);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            BaseAncHomeVisitAction malariaInvestigation = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_visit_malaria_investigation))
                    .withOptional(true)
                    .withDetails(details)
                    .withJsonPayload(malariaInvestigationForm.toString())
                    .withFormName(Constants.JsonForm.AncFirstVisit.getMalariaInvestigation())
                    .withHelper(new AncMalariaInvestigationAction(memberObject))
                    .build();
            actionList.put(context.getString(R.string.anc_visit_malaria_investigation), malariaInvestigation);
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            e.printStackTrace();
        }

        JSONObject pharmacyForm = null;
        try {
            pharmacyForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.AncRecurringVisit.getPharmacy());
            pharmacyForm.getJSONObject("global").put("gestational_age", memberObject.getGestationAge());

            if (editMode) {
                pharmacyForm.getJSONObject("global").put("deworming_given", HfAncDao.wasDewormingGivenPreviously(memberObject.getBaseEntityId()));
            } else {
                pharmacyForm.getJSONObject("global").put("deworming_given", HfAncDao.isDewormingGiven(memberObject.getBaseEntityId()));
            }
            if (details != null && !details.isEmpty()) {
                HfAncJsonFormUtils.populateForm(pharmacyForm, details);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            BaseAncHomeVisitAction pharmacy = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_recuring_visit_pharmacy))
                    .withOptional(true)
                    .withDetails(details)
                    .withJsonPayload(pharmacyForm.toString())
                    .withFormName(Constants.JsonForm.AncRecurringVisit.getPharmacy())
                    .withHelper(new AncPharmacyAction(memberObject))
                    .build();
            actionList.put(context.getString(R.string.anc_recuring_visit_pharmacy), pharmacy);
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            e.printStackTrace();
        }


        JSONObject ttVaccinationForm = null;
        try {
            ttVaccinationForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.AncFirstVisit.getTtVaccination());
            if (details != null && !details.isEmpty()) {
                HfAncJsonFormUtils.populateForm(ttVaccinationForm, details);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        if (HfAncDao.isEligibleForTtVaccination(memberObject.getBaseEntityId())) {
            BaseAncHomeVisitAction vaccinationAction = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_first_visit_tt_vaccination))
                    .withOptional(true)
                    .withDetails(details)
                    .withJsonPayload(ttVaccinationForm.toString())
                    .withFormName(Constants.JsonForm.AncFirstVisit.getTtVaccination())
                    .withHelper(new AncTtVaccinationAction(memberObject))
                    .build();
            actionList.put(context.getString(R.string.anc_first_visit_tt_vaccination), vaccinationAction);
        }

        BaseAncHomeVisitAction counsellingAction = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_first_and_recurring_visit_counselling))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getCounselling())
                .withJsonPayload(counsellingForm.toString())
                .withHelper(new AncCounsellingAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_first_and_recurring_visit_counselling), counsellingAction);


        JSONObject birthReviewForm = initializeHealthFacilitiesList(FormUtils.getFormUtils().getFormJson(Constants.JsonForm.AncRecurringVisit.BIRTH_REVIEW_AND_EMERGENCY_PLAN));
        BaseAncHomeVisitAction birthReview = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_recuring_visit_review_birth_and_emergency_plan))
                .withOptional(true)
                .withDetails(details)
                .withJsonPayload(birthReviewForm.toString())
                .withFormName(Constants.JsonForm.AncRecurringVisit.getBirthReviewAndEmergencyPlan())
                .withHelper(new AncBirthReviewAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_recuring_visit_review_birth_and_emergency_plan), birthReview);

        BaseAncHomeVisitAction nextFollowupVisitDate = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.next_visit))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getNextFacilityVisitForm())
                .withHelper(new AncNextFollowupVisitAction())
                .build();
        actionList.put(context.getString(R.string.next_visit), nextFollowupVisitDate);
    }

    private void evaluateBaselineInvestigation(MemberObject memberObject, Context context, Map<String, List<VisitDetail>> details, boolean isKnownOnART) throws BaseAncHomeVisitAction.ValidationException {
        BaseAncHomeVisitAction baselineInvestigationAction = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_first_visit_baseline_investigation))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.AncFirstVisit.getBaselineInvestigation())
                .withHelper(new AncBaselineInvestigationAction(memberObject, isKnownOnART))
                .build();
        actionList.put(context.getString(R.string.anc_first_visit_baseline_investigation), baselineInvestigationAction);

        //Refreshing the baseline Investigation form from details when the action form is recreated during update/edit
        if (details != null && details.containsKey("known_on_art")) {
            List<VisitDetail> knownOnArt = details.get("known_on_art");
            if (knownOnArt != null && knownOnArt.size() > 0 && knownOnArt.get(0).getDetails().equals("true"))
                refreshBaselineInvestigation(context, true);
        }
    }

    private class AncMedicalAndSurgicalHistoryAction extends org.smartregister.chw.hf.actionhelper.AncMedicalAndSurgicalHistoryAction {
        private final Map<String, List<VisitDetail>> details;
        BaseAncHomeVisitContract.InteractorCallBack callBack;
        private String medical_and_surgical_history_present;
        private HashMap<String, Boolean> checkObject = new HashMap<>();
        private String visitNumber;
        private Context context;

        public AncMedicalAndSurgicalHistoryAction(MemberObject memberObject, Map<String, List<VisitDetail>> details, BaseAncHomeVisitContract.InteractorCallBack callBack) {
            super(memberObject);
            this.details = details;
            this.callBack = callBack;
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
                checkObject.clear();
                medical_and_surgical_history_present = HfAncJsonFormUtils.getCheckBoxValue(jsonObject, "medical_surgical_history");
                checkObject.put("medical_surgical_history", StringUtils.isNotBlank(medical_and_surgical_history_present));
                checkObject.put("gravida", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "gravida")));
                checkObject.put("parity", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "parity")));
                checkObject.put("no_surv_children", StringUtils.isNotBlank(CoreJsonFormUtils.getValue(jsonObject, "no_surv_children")));
                visitNumber = CoreJsonFormUtils.getValue(jsonObject, "visit_number");
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
        public String postProcess(String jsonPayload) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(jsonPayload);
                JSONArray fields = org.smartregister.family.util.JsonFormUtils.fields(jsonObject);
                JSONObject medicalSurgicalHistoryCompletionStatus = org.smartregister.family.util.JsonFormUtils.getFieldJSONObject(fields, "medical_surgical_history_completion_status");
                assert medicalSurgicalHistoryCompletionStatus != null;
                medicalSurgicalHistoryCompletionStatus.put(com.vijay.jsonwizard.constants.JsonFormConstants.VALUE, org.smartregister.chw.hf.utils.VisitUtils.getActionStatus(checkObject));
            } catch (JSONException e) {
                Timber.e(e);
            }

            if (actionList.containsKey(context.getString(R.string.anc_first_visit_baseline_investigation))) {
                boolean isClientOnArt = isClientOnArt();
                refreshBaselineInvestigation(context, isClientOnArt);
            }

            if (jsonObject != null) {
                return jsonObject.toString();
            }
            return null;
        }

        private boolean isClientOnArt() {
            if (!StringUtils.isBlank(medical_and_surgical_history_present)) {
                return medical_and_surgical_history_present.contains("On ART") || medical_and_surgical_history_present.contains("Mteja yupo kwenye ART tayari");
            }
            return false;
        }

        @Override
        public String evaluateSubTitle() {
            String status = org.smartregister.chw.hf.utils.VisitUtils.getActionStatus(checkObject);
            if (status.equalsIgnoreCase(org.smartregister.chw.hf.utils.VisitUtils.Complete))
                return context.getString(R.string.medical_and_surgical_filled);
            return "";
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            String status = org.smartregister.chw.hf.utils.VisitUtils.getActionStatus(checkObject);
            if (status.equalsIgnoreCase(org.smartregister.chw.hf.utils.VisitUtils.Complete)) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
            if (status.equalsIgnoreCase(org.smartregister.chw.hf.utils.VisitUtils.Ongoing)) {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            }
            return BaseAncHomeVisitAction.Status.PENDING;
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.v("onPayloadReceived");
        }
    }

    private void refreshBaselineInvestigation(Context context, boolean isClientOnArt) {
        if (actionList.containsKey(context.getString(R.string.anc_first_visit_baseline_investigation))) {
            BaseAncHomeVisitAction baselineInvestigation = actionList.get(context.getString(R.string.anc_first_visit_baseline_investigation));
            String baselineInvestigationJsonPayload = baselineInvestigation.getJsonPayload();

            JSONObject baselineInvestigationJsonPayloadObject = null;
            try {
                baselineInvestigationJsonPayloadObject = new JSONObject(baselineInvestigationJsonPayload);
                baselineInvestigationJsonPayloadObject.getJSONObject("global").put("known_positive", isClientOnArt);
                baselineInvestigation.setJsonPayload(baselineInvestigationJsonPayloadObject.toString());
                baselineInvestigation.evaluateStatus();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}

