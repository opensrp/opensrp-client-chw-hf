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
import org.smartregister.chw.anc.util.AppExecutors;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.AncBirthReviewAction;
import org.smartregister.chw.hf.actionhelper.AncConsultationAction;
import org.smartregister.chw.hf.actionhelper.AncCounsellingAction;
import org.smartregister.chw.hf.actionhelper.AncLabTestAction;
import org.smartregister.chw.hf.actionhelper.AncMalariaInvestigationAction;
import org.smartregister.chw.hf.actionhelper.AncPharmacyAction;
import org.smartregister.chw.hf.actionhelper.AncTriageAction;
import org.smartregister.chw.hf.actionhelper.AncTtVaccinationAction;
import org.smartregister.chw.hf.dao.HfAncBirthEmergencyPlanDao;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.hf.repository.HfLocationRepository;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.ContactUtil;
import org.smartregister.chw.hf.utils.HfAncJsonFormUtils;
import org.smartregister.chw.referral.util.JsonFormConstants;
import org.smartregister.domain.Location;
import org.smartregister.domain.LocationTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class AncRecurringFacilityVisitInteractorFlv implements AncFirstFacilityVisitInteractor.Flavor {
    String baseEntityId;
    LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();

    public AncRecurringFacilityVisitInteractorFlv(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    private static String getPregnancyStatusString(String pregnancyStatus, Context context) {
        if (pregnancyStatus.equals("intrauterine_fetal_death")) {
            return context.getString(R.string.anc_pregnacy_status_fetal_death);
        } else if (pregnancyStatus.equals("spontaneous_abortion")) {
            return context.getString(R.string.anc_pregnacy_status_spontaneous_abortion);
        } else if (pregnancyStatus.equals("viable")) {
            return context.getString(R.string.anc_pregnacy_status_viable);
        }
        return "";
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
                JSONArray tree = referralHealthFacilities.getJSONArray("tree");
                String parentTagName = "Region";
                for (Location location : locations) {
                    Set<LocationTag> locationTags = location.getLocationTags();
                    if (locationTags.iterator().next().getName().equalsIgnoreCase(parentTagName)) {
                        JSONObject treeNode = new JSONObject();
                        treeNode.put("name", StringUtils.capitalize(location.getProperties().getName()));
                        treeNode.put("key", StringUtils.capitalize(location.getProperties().getName()));

                        JSONArray childNodes = setChildNodes(locations, location.getId(), parentTagName);
                        if (childNodes != null)
                            treeNode.put("nodes", childNodes);

                        tree.put(treeNode);
                    }
                }
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
        return form;
    }

    private static JSONArray setChildNodes(List<Location> locations, String parentLocationId, String parentTagName) {
        JSONArray nodes = new JSONArray();
        ArrayList<String> locationHierarchyTags = new ArrayList<>(Arrays.asList(BuildConfig.LOCATION_HIERACHY));

        for (Location location : locations) {
            Set<LocationTag> locationTags = location.getLocationTags();
            String childTagName = locationHierarchyTags.get(locationHierarchyTags.indexOf(parentTagName) + 1);
            if (locationTags.iterator().next().getName().equalsIgnoreCase(childTagName) && location.getProperties().getParentId().equals(parentLocationId)) {
                JSONObject childNode = new JSONObject();
                try {
                    childNode.put("name", StringUtils.capitalize(location.getProperties().getName()));
                    childNode.put("key", StringUtils.capitalize(location.getProperties().getName()));

                    JSONArray childNodes = setChildNodes(locations, location.getId(), childTagName);
                    if (childNodes != null)
                        childNode.put("nodes", childNodes);

                    nodes.put(childNode);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if (nodes.length() > 0) {
            return nodes;
        } else return null;

    }

    private static JSONObject setMinFundalHeight(JSONObject form, String baseEntityId) {
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
            v_min.put("err", "Fundal height must be equal or greater than " + fundalHeight + "CM");


        } catch (JSONException e) {
            Timber.e(e);
        }
        return form;
    }

    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.ANC_RECURRING_FACILITY_VISIT);
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

        evaluateMedicalAndSurgicalHistory(view, memberObject, callBack, details);

        return actionList;
    }

    private void evaluateMedicalAndSurgicalHistory(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack, Map<String, List<VisitDetail>> details
    ) throws BaseAncHomeVisitAction.ValidationException {

        Context context = view.getContext();
        JSONObject triageForm = null;
        try {
            triageForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.AncRecurringVisit.TRIAGE);
            triageForm.getJSONObject("global").put("last_menstrual_period", memberObject.getLastMenstrualPeriod());
            triageForm.getJSONObject("global").put("current_visit_number", HfAncDao.getVisitNumber(baseEntityId));
            JSONArray fields = triageForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
            JSONObject gest_age = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "gest_age");
            if (gest_age != null) {
                gest_age.put("value", memberObject.getGestationAge());
            }
            if (details != null && !details.isEmpty()) {
                HfAncJsonFormUtils.populateForm(triageForm, details);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BaseAncHomeVisitAction triage = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_recuring_visit_triage))
                .withOptional(false)
                .withDetails(details)
                .withJsonPayload(triageForm.toString())
                .withFormName(Constants.JsonForm.AncRecurringVisit.getTriage())
                .withHelper(new AncTriageAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_recuring_visit_triage), triage);

        BaseAncHomeVisitAction pregnancyStatus = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_recuring_visit_pregnancy_status))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.AncRecurringVisit.getPregnancyStatus())
                .withHelper(new AncPregnancyStatusAction(view, memberObject, callBack, details))
                .build();
        actionList.put(context.getString(R.string.anc_recuring_visit_pregnancy_status), pregnancyStatus);


    }

    private class AncPregnancyStatusAction extends org.smartregister.chw.hf.actionhelper.AncPregnancyStatusAction {
        Map<String, List<VisitDetail>> details;
        BaseAncHomeVisitContract.View view;
        MemberObject memberObject;
        BaseAncHomeVisitContract.InteractorCallBack callBack;


        public AncPregnancyStatusAction(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack, Map<String, List<VisitDetail>> details) {
            super(memberObject);
            this.details = details;
            this.view = view;
            this.memberObject = memberObject;
            this.callBack = callBack;
        }

        @Override
        public String evaluateSubTitle() {
            if (StringUtils.isBlank(pregnancy_status))
                return null;

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(view.getContext().getString(R.string.anc_pregnacy_status));
            stringBuilder.append(" ");
            stringBuilder.append(getPregnancyStatusString(pregnancy_status, view.getContext()));

            return stringBuilder.toString();
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(pregnancy_status))
                return BaseAncHomeVisitAction.Status.PENDING;
            else if (pregnancy_status.equals("viable")) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            }
        }

        @Override
        public String postProcess(String s) {
            Context context = view.getContext();
            if (pregnancy_status.equals("viable")) {
                JSONObject consultationForm = null;
                try {
                    consultationForm = setMinFundalHeight(FormUtils.getFormUtils().getFormJson(Constants.JsonForm.AncRecurringVisit.CONSULTATION), memberObject.getBaseEntityId());
                    consultationForm.getJSONObject("global").put("last_menstrual_period", memberObject.getLastMenstrualPeriod());
                    consultationForm.getJSONObject("global").put("client_age", memberObject.getAge());
                    String height = HfAncDao.getClientHeight(memberObject.getBaseEntityId());
                    consultationForm.getJSONObject("global").put("client_height", height);
                    if (details != null && !details.isEmpty()) {
                        HfAncJsonFormUtils.populateForm(consultationForm, details);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject pharmacyForm = null;
                try {
                    pharmacyForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.AncRecurringVisit.getPharmacy());
                    pharmacyForm.getJSONObject("global").put("deworming_given", HfAncDao.isDewormingGiven(memberObject.getBaseEntityId()));
                    pharmacyForm.getJSONObject("global").put("gestational_age", memberObject.getGestationAge());
                    pharmacyForm.getJSONObject("global").put("malaria_preventive_therapy_ipt1", HfAncDao.malariaDosageIpt1(baseEntityId));
                    pharmacyForm.getJSONObject("global").put("malaria_preventive_therapy_ipt2", HfAncDao.malariaDosageIpt2(baseEntityId));
                    pharmacyForm.getJSONObject("global").put("malaria_preventive_therapy_ipt3", HfAncDao.malariaDosageIpt3(baseEntityId));
                    pharmacyForm.getJSONObject("global").put("malaria_preventive_therapy_ipt4", HfAncDao.malariaDosageIpt4(baseEntityId));
                    if (details != null && !details.isEmpty()) {
                        HfAncJsonFormUtils.populateForm(pharmacyForm, details);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject malariaInvestigationForm = null;
                try {
                    malariaInvestigationForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.AncRecurringVisit.getMalariaInvestigation());
                    malariaInvestigationForm.getJSONObject("global").put("gestational_age", memberObject.getGestationAge());
                    malariaInvestigationForm.getJSONObject("global").put("llin_provided", HfAncDao.isLLINProvided(baseEntityId));
                    malariaInvestigationForm.getJSONObject("global").put("malaria_preventive_therapy_ipt1", HfAncDao.malariaDosageIpt1(memberObject.getBaseEntityId()));
                    malariaInvestigationForm.getJSONObject("global").put("malaria_preventive_therapy_ipt2", HfAncDao.malariaDosageIpt2(memberObject.getBaseEntityId()));
                    malariaInvestigationForm.getJSONObject("global").put("malaria_preventive_therapy_ipt3", HfAncDao.malariaDosageIpt3(memberObject.getBaseEntityId()));
                    malariaInvestigationForm.getJSONObject("global").put("malaria_preventive_therapy_ipt4", HfAncDao.malariaDosageIpt4(memberObject.getBaseEntityId()));
                    if (details != null && !details.isEmpty()) {
                        HfAncJsonFormUtils.populateForm(malariaInvestigationForm, details);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject labTestForm = null;
                try {
                    labTestForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.AncRecurringVisit.LAB_TESTS);
                    labTestForm.getJSONObject("global").put("gestational_age", memberObject.getGestationAge());
                    labTestForm.getJSONObject("global").put("hepatitis_test_complete", HfAncDao.isTestConducted(Constants.DBConstants.ANC_HEPATITIS, baseEntityId));
                    labTestForm.getJSONObject("global").put("malaria_test_complete", HfAncDao.isTestConducted(Constants.DBConstants.ANC_MRDT_FOR_MALARIA, baseEntityId));
                    labTestForm.getJSONObject("global").put("syphilis_test_complete", HfAncDao.isTestConducted(Constants.DBConstants.ANC_SYPHILIS, baseEntityId));
                    labTestForm.getJSONObject("global").put("hiv_test_complete", HfAncDao.isTestConducted(Constants.DBConstants.ANC_HIV, baseEntityId));
                    labTestForm.getJSONObject("global").put("hiv_test_at_32_complete", HfAncDao.isHivTestConductedAtWk32(baseEntityId));
                    labTestForm.getJSONObject("global").put("blood_group_complete", HfAncDao.isBloodGroupTestConducted(baseEntityId));
                    labTestForm.getJSONObject("global").put("hiv_status", HfAncDao.getHivStatus(baseEntityId));
                    JSONArray fields = labTestForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                    JSONObject hivTestNumberField = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "hiv_test_number");
                    hivTestNumberField.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getNextHivTestNumber(memberObject.getBaseEntityId()));
                    if (HfAncDao.getNextHivTestNumber(memberObject.getBaseEntityId()) == 2) {
                        JSONObject renameSecondHivAt32 = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "hiv");
                        renameSecondHivAt32.put("label", context.getString(R.string.second_hiv_test_results_woman));
                    }
                    if (details != null && !details.isEmpty()) {
                        HfAncJsonFormUtils.populateForm(labTestForm, details);
                    }
                } catch (JSONException e) {
                    Timber.e(e);
                }

                JSONObject counsellingForm = null;
                try {
                    counsellingForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getCounselling());

                    if (details != null && !details.isEmpty()) {
                        HfAncJsonFormUtils.populateForm(counsellingForm, details);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }

                JSONObject ttVaccinationForm = null;
                try {
                    ttVaccinationForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.AncFirstVisit.TT_VACCINATION);
                    if (details != null && !details.isEmpty()) {
                        HfAncJsonFormUtils.populateForm(ttVaccinationForm, details);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }

                if (pregnancy_status != null) {
                    try {
                        BaseAncHomeVisitAction consultation = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_recuring_visit_cunsultation))
                                .withOptional(true)
                                .withDetails(details)
                                .withJsonPayload(consultationForm.toString())
                                .withFormName(Constants.JsonForm.AncRecurringVisit.getConsultation())
                                .withHelper(new AncConsultationAction(memberObject))
                                .build();
                        actionList.put(context.getString(R.string.anc_recuring_visit_cunsultation), consultation);
                    } catch (BaseAncHomeVisitAction.ValidationException e) {
                        e.printStackTrace();
                    }
                    try {
                        BaseAncHomeVisitAction malariaInvestigation = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_visit_malaria_investigation))
                                .withOptional(true)
                                .withDetails(details)
                                .withJsonPayload(malariaInvestigationForm.toString())
                                .withFormName(Constants.JsonForm.AncRecurringVisit.getMalariaInvestigation())
                                .withHelper(new AncMalariaInvestigationAction(memberObject))
                                .build();
                        actionList.put(context.getString(R.string.anc_visit_malaria_investigation), malariaInvestigation);
                    } catch (BaseAncHomeVisitAction.ValidationException e) {
                        e.printStackTrace();
                    }
                    try {
                        BaseAncHomeVisitAction labTests = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_recuring_visit_lab_tests))
                                .withOptional(true)
                                .withDetails(details)
                                .withJsonPayload(labTestForm.toString())
                                .withFormName(Constants.JsonForm.AncRecurringVisit.getLabTests())
                                .withHelper(new AncLabTestAction(memberObject))
                                .build();
                        actionList.put(context.getString(R.string.anc_recuring_visit_lab_tests), labTests);
                    } catch (BaseAncHomeVisitAction.ValidationException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (HfAncDao.isEligibleForTtVaccination(memberObject.getBaseEntityId())) {
                            BaseAncHomeVisitAction vaccinationAction = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_first_visit_tt_vaccination))
                                    .withOptional(true)
                                    .withDetails(details)
                                    .withFormName(Constants.JsonForm.AncFirstVisit.getTtVaccination())
                                    .withJsonPayload(ttVaccinationForm.toString())
                                    .withHelper(new AncTtVaccinationAction(memberObject))
                                    .build();
                            actionList.put(context.getString(R.string.anc_first_visit_tt_vaccination), vaccinationAction);
                        }
                    } catch (BaseAncHomeVisitAction.ValidationException e) {
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

                    try {
                        BaseAncHomeVisitAction counselling = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_first_and_recurring_visit_counselling))
                                .withOptional(true)
                                .withDetails(details)
                                .withFormName(Constants.JsonForm.getCounselling())
                                .withJsonPayload(counsellingForm.toString())
                                .withHelper(new AncCounsellingAction(memberObject))
                                .build();
                        actionList.put(context.getString(R.string.anc_first_and_recurring_visit_counselling), counselling);
                    } catch (BaseAncHomeVisitAction.ValidationException e) {
                        e.printStackTrace();
                    }

                    if (!HfAncBirthEmergencyPlanDao.isAllFilled(baseEntityId)) {
                        JSONObject birthReviewForm = initializeHealthFacilitiesList(FormUtils.getFormUtils().getFormJson(Constants.JsonForm.AncRecurringVisit.BIRTH_REVIEW_AND_EMERGENCY_PLAN));
                        try {
                            birthReviewForm.getJSONObject("global").put("delivery_place_identified", HfAncBirthEmergencyPlanDao.isDeliveryPlaceIdentified(baseEntityId));
                            birthReviewForm.getJSONObject("global").put("transport_identified", HfAncBirthEmergencyPlanDao.isTransportMethodIdentified(baseEntityId));
                            birthReviewForm.getJSONObject("global").put("birth_companion_identified", HfAncBirthEmergencyPlanDao.isBirthCompanionIdentified(baseEntityId));
                            birthReviewForm.getJSONObject("global").put("emergency_funds_identified", HfAncBirthEmergencyPlanDao.areEmergencyFundsPrepared(baseEntityId));
                            birthReviewForm.getJSONObject("global").put("household_support_identified", HfAncBirthEmergencyPlanDao.isHouseholdSupportIdentified(baseEntityId));
                            birthReviewForm.getJSONObject("global").put("blood_donor_identified", HfAncBirthEmergencyPlanDao.isBloodDonorIdentified(baseEntityId));
                        } catch (JSONException e) {
                           Timber.e(e);
                        }
                        try {
                            BaseAncHomeVisitAction birthReview = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_recuring_visit_review_birth_and_emergency_plan))
                                    .withOptional(true)
                                    .withDetails(details)
                                    .withJsonPayload(birthReviewForm.toString())
                                    .withFormName(Constants.JsonForm.AncRecurringVisit.getBirthReviewAndEmergencyPlan())
                                    .withHelper(new AncBirthReviewAction(memberObject))
                                    .build();
                            actionList.put(context.getString(R.string.anc_recuring_visit_review_birth_and_emergency_plan), birthReview);
                        } catch (BaseAncHomeVisitAction.ValidationException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                actionList.remove(context.getString(R.string.anc_recuring_visit_cunsultation));
                actionList.remove(context.getString(R.string.anc_recuring_visit_lab_tests));
                actionList.remove(context.getString(R.string.anc_recuring_visit_pharmacy));
                actionList.remove(context.getString(R.string.anc_first_and_recurring_visit_counselling));
                actionList.remove(context.getString(R.string.anc_visit_malaria_investigation));
                actionList.remove(context.getString(R.string.anc_recuring_visit_review_birth_and_emergency_plan));
                actionList.remove(context.getString(R.string.anc_first_visit_tt_vaccination));
            }
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            return super.postProcess(s);
        }
    }


}

