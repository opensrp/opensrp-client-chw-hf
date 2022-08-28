package org.smartregister.chw.hf.interactor;

import static com.vijay.jsonwizard.widgets.TimePickerFactory.KEY.KEY;
import static com.vijay.jsonwizard.widgets.TimePickerFactory.KEY.VALUE;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.AppExecutors;
import org.smartregister.chw.core.dao.AncDao;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.LDRegistrationAdmissionAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationAncClinicFindingsAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationCurrentLabourAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationObstetricHistoryAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationPastObstetricHistoryAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationTriageAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationTrueLabourConfirmationAction;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.hf.dao.HfPmtctDao;
import org.smartregister.chw.hf.repository.HfLocationRepository;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.ld.contract.BaseLDVisitContract;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
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

/**
 * @author ilakozejumanne@gmail.com
 * 06/05/2022
 */
public class LDRegistrationInteractorFlv implements LDRegistrationInteractor.Flavor {
    static JSONObject obstetricForm = null;
    static JSONObject ancClinicForm = null;
    static JSONObject ancTriageForm = null;
    LinkedHashMap<String, BaseLDVisitAction> actionList = new LinkedHashMap<>();

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

    public LDRegistrationInteractorFlv(String baseEntityId) {
    }

    private void populateObstetricForm(JSONArray fields, org.smartregister.chw.anc.domain.MemberObject memberObject) throws JSONException {
        JSONObject gravida = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "gravida");
        JSONObject childrenAlive = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "children_alive");
        JSONObject parity = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "para");
        JSONObject lastMenstrualPeriod = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "last_menstrual_period");
        JSONObject pastMedicalSurgicalHistory = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "past_medical_surgical_history");

        gravida.put(org.smartregister.family.util.JsonFormUtils.VALUE, memberObject.getGravida());
        parity.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getParity(memberObject.getBaseEntityId()));
        childrenAlive.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getNumberOfSurvivingChildren(memberObject.getBaseEntityId()));
        lastMenstrualPeriod.put(org.smartregister.family.util.JsonFormUtils.VALUE, memberObject.getLastMenstrualPeriod());
        JSONArray historyValues;

        String pastMedicalAndSurgicalHistory = HfAncDao.getMedicalAndSurgicalHistory(memberObject.getBaseEntityId());
        if (pastMedicalAndSurgicalHistory.startsWith("[")) {
            try {
                historyValues = new JSONArray(pastMedicalAndSurgicalHistory);
                for (int i = 0; i < historyValues.length(); i++) {
                    String value = historyValues.getString(i);
                    setCheckBoxValues(pastMedicalSurgicalHistory.getJSONArray("options"), value);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        } else {
            setCheckBoxValues(pastMedicalSurgicalHistory.getJSONArray("options"), pastMedicalAndSurgicalHistory);
        }
    }

    private void setCheckBoxValues(JSONArray options, String value) {
        for (int j = 0; j < options.length(); j++) {
            JSONObject option = null;
            try {
                option = options.getJSONObject(j);
                if (option.getString(KEY).equals(value)) {
                    option.put(VALUE, true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void populateAncFindingsForm(JSONArray fields, org.smartregister.chw.anc.domain.MemberObject memberObject) throws JSONException {
        JSONObject numberOfVisits = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "number_of_visits");
        JSONObject iptDoses = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "ipt_doses");
        JSONObject malaria = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "malaria");
        JSONObject TTDoses = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "tt_doses");
        JSONObject LLINUsed = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "llin_used");
        JSONObject hbTestConducted = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "hb_test");
        JSONObject lastMeasuredHB = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "hb_level");
        JSONObject lastMeasuredHBDate = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "hb_test_date");
        JSONObject syphilis = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "syphilis");
        JSONObject bloodGroup = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "blood_group");
        JSONObject rhFactor = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "rh_factor");
        JSONObject pmtct = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "anc_hiv_status");
        JSONObject pmtctTestDate = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "pmtct_test_date");
        JSONObject artPrescription = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "art_prescription");
        JSONObject managementProvidedForPmtct = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "management_provided_for_pmtct");

        numberOfVisits.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getVisitNumber(memberObject.getBaseEntityId()));
        iptDoses.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getIptDoses(memberObject.getBaseEntityId()));
        malaria.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getMalariaTestResults(memberObject.getBaseEntityId()));
        TTDoses.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getTTDoses(memberObject.getBaseEntityId()));
        LLINUsed.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.isLLINProvided(memberObject.getBaseEntityId()) ? "Yes" : "No");

        String lastMeasuredHb = HfAncDao.getLastMeasuredHB(memberObject.getBaseEntityId());
        hbTestConducted.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getLastMeasuredHB(lastMeasuredHb).equals("") ? "no" : "yes");
        if (!lastMeasuredHB.equals("")) {
            lastMeasuredHB.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getLastMeasuredHB(memberObject.getBaseEntityId()));
            lastMeasuredHBDate.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getLastMeasuredHBDate(memberObject.getBaseEntityId()));
        }
        syphilis.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getSyphilisTestResult(memberObject.getBaseEntityId()));
        bloodGroup.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getBloodGroup(memberObject.getBaseEntityId()));
        rhFactor.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getRhFactor(memberObject.getBaseEntityId()));
        if (HfAncDao.getHivStatus(memberObject.getBaseEntityId()) != null && !HfAncDao.getHivStatus(memberObject.getBaseEntityId()).equalsIgnoreCase("null"))
            pmtct.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getHivStatus(memberObject.getBaseEntityId()).equalsIgnoreCase("positive") ? "positive" : "negative");
        pmtctTestDate.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getHivTestDate(memberObject.getBaseEntityId()));
        artPrescription.put(org.smartregister.family.util.JsonFormUtils.VALUE, (HfPmtctDao.isPrescribedArtRegimes(memberObject.getBaseEntityId()) || HfAncDao.isClientKnownOnArt(memberObject.getBaseEntityId())) ? "yes" : "no");
        managementProvidedForPmtct.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfPmtctDao.isRegisteredForPmtct(memberObject.getBaseEntityId()) ? "yes" : "no");
    }


    private void populateTriageForm(JSONArray fields, org.smartregister.chw.anc.domain.MemberObject memberObject) throws JSONException {
        JSONObject heightJsonObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "height");
        String height = HfAncDao.getClientHeight(memberObject.getBaseEntityId());
        if (!height.equalsIgnoreCase("null"))
            heightJsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, height);
    }

    @Override
    public LinkedHashMap<String, BaseLDVisitAction> calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) throws BaseLDVisitAction.ValidationException {

        Context context = view.getContext();
        obstetricForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryObstetricHistory());
        ancClinicForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryAncClinicFindings());
        ancTriageForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryRegistrationTriage());

        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (AncDao.isANCMember(memberObject.getBaseEntityId())) {
            if (obstetricForm != null) {
                try {
                    JSONArray fields = obstetricForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                    populateObstetricForm(fields, AncDao.getMember(memberObject.getBaseEntityId()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (ancClinicForm != null) {
                try {
                    JSONArray fields = ancClinicForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                    populateAncFindingsForm(fields, AncDao.getMember(memberObject.getBaseEntityId()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (ancTriageForm != null) {
                try {
                    JSONArray fields = ancTriageForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                    populateTriageForm(fields, AncDao.getMember(memberObject.getBaseEntityId()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        evaluateLDRegistration(actionList, details, memberObject, context, callBack);

        return actionList;
    }

    private void evaluateLDRegistration(LinkedHashMap<String, BaseLDVisitAction> actionList,
                                        Map<String, List<VisitDetail>> details,
                                        final MemberObject memberObject,
                                        final Context context,
                                        BaseLDVisitContract.InteractorCallBack callBack
    ) throws BaseLDVisitAction.ValidationException {
        BaseLDVisitAction ldRegistrationTriage = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_triage_title))
                .withOptional(false)
                .withDetails(details)
                .withJsonPayload(ancTriageForm.toString())
                .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryRegistrationTriage())
                .withHelper(new LDRegistrationTriageAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.ld_registration_triage_title), ldRegistrationTriage);

        BaseLDVisitAction ldRegistrationTrueLabourConfirmation = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_true_labour_title))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryRegistrationTrueLabourConfirmation())
                .withHelper(new TrueLabourConfirmationAction(memberObject, actionList, details, callBack, context))
                .build();
        actionList.put(context.getString(R.string.ld_registration_true_labour_title), ldRegistrationTrueLabourConfirmation);
    }

    private static class TrueLabourConfirmationAction extends LDRegistrationTrueLabourConfirmationAction {
        private final LinkedHashMap<String, BaseLDVisitAction> actionList;
        private final Context context;
        private final Map<String, List<VisitDetail>> details;
        private final BaseLDVisitContract.InteractorCallBack callBack;

        public TrueLabourConfirmationAction(MemberObject memberObject, LinkedHashMap<String, BaseLDVisitAction> actionList, Map<String, List<VisitDetail>> details, BaseLDVisitContract.InteractorCallBack callBack, Context context) {
            super(memberObject);
            this.actionList = actionList;
            this.context = context;
            this.details = details;
            this.callBack = callBack;
        }

        @Override
        public String postProcess(String s) {
            if (labourConfirmation.equalsIgnoreCase("true")) {
                //Adding the next actions when true labour confirmation is completed and the client is confirmed with True Labour.
                try {
                    JSONObject admissionInformationForm = initializeHealthFacilitiesList(FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryAdmissionInformation()));
                    BaseLDVisitAction ldRegistrationAdmissionInformation = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_admission_information_title))
                            .withOptional(false)
                            .withDetails(details)
                            .withJsonPayload(admissionInformationForm.toString())
                            .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryAdmissionInformation())
                            .withHelper(new RegistrationAdmissionAction(memberObject, actionList, context))
                            .build();

                    actionList.put(context.getString(R.string.ld_registration_admission_information_title), ldRegistrationAdmissionInformation);
                } catch (Exception e) {
                    Timber.e(e);
                }

                try {
                    BaseLDVisitAction ldRegistrationObstetricHistory = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_obstetric_history_title))
                            .withOptional(false)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryObstetricHistory())
                            .withJsonPayload(obstetricForm.toString())
                            .withHelper(new ObstetricHistoryAction(memberObject, actionList, details, callBack, context))
                            .build();

                    actionList.put(context.getString(R.string.ld_registration_obstetric_history_title), ldRegistrationObstetricHistory);
                } catch (Exception e) {
                    Timber.e(e);
                }

                try {
                    BaseLDVisitAction ldRegistrationAncClinicFindings = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_anc_clinic_findings_title))
                            .withOptional(false)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryAncClinicFindings())
                            .withJsonPayload(ancClinicForm.toString())
                            .withHelper(new LDRegistrationAncClinicFindingsAction(memberObject))
                            .build();

                    actionList.put(context.getString(R.string.ld_registration_anc_clinic_findings_title), ldRegistrationAncClinicFindings);
                } catch (Exception e) {
                    Timber.e(e);
                }

                try {
                    BaseLDVisitAction ldRegistrationCurrentLabour = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_current_labour_title))
                            .withOptional(false)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryCurrentLabour())
                            .withHelper(new LDRegistrationCurrentLabourAction(memberObject))
                            .build();
                    actionList.put(context.getString(R.string.ld_registration_current_labour_title), ldRegistrationCurrentLabour);
                } catch (BaseLDVisitAction.ValidationException e) {
                    Timber.e(e);
                }


            } else {
                //Removing the next actions  the client is confirmed with False Labour.
                if (actionList.containsKey(context.getString(R.string.ld_registration_admission_information_title))) {
                    actionList.remove(context.getString(R.string.ld_registration_admission_information_title));
                }

                if (actionList.containsKey(context.getString(R.string.ld_registration_obstetric_history_title))) {
                    actionList.remove(context.getString(R.string.ld_registration_obstetric_history_title));
                }

                if (actionList.containsKey(context.getString(R.string.ld_registration_anc_clinic_findings_title))) {
                    actionList.remove(context.getString(R.string.ld_registration_anc_clinic_findings_title));
                }

                if (actionList.containsKey(context.getString(R.string.ld_registration_current_labour_title))) {
                    actionList.remove(context.getString(R.string.ld_registration_current_labour_title));
                }

            }

            //Calling the callback method to preload the actions in the actionns list.
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            return super.postProcess(s);
        }
    }

    private static class ObstetricHistoryAction extends LDRegistrationObstetricHistoryAction {
        private final LinkedHashMap<String, BaseLDVisitAction> actionList;
        private final Context context;
        private final Map<String, List<VisitDetail>> details;
        private final BaseLDVisitContract.InteractorCallBack callBack;

        public ObstetricHistoryAction(MemberObject memberObject, LinkedHashMap<String, BaseLDVisitAction> actionList, Map<String, List<VisitDetail>> details, BaseLDVisitContract.InteractorCallBack callBack, Context context) {
            super(memberObject);
            this.actionList = actionList;
            this.details = details;
            this.callBack = callBack;
            this.context = context;
        }

        @Override
        public String postProcess(String s) {
            if (!StringUtils.isBlank(para) && Integer.parseInt(para) > 0) {
                //Adding the actions for capturing previous para obstetric  history when para is greater than 0 .
                try {
                    BaseLDVisitAction labourAndDeliveryPastObstetricHistory = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_past_obstetric_history_title))
                            .withOptional(false)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryPastObstetricHistory())
                            .withHelper(new LDRegistrationPastObstetricHistoryAction(memberObject, Integer.parseInt(para)))
                            .build();

                    actionList.put(context.getString(R.string.ld_registration_past_obstetric_history_title), labourAndDeliveryPastObstetricHistory);
                } catch (Exception e) {
                    Timber.e(e);
                }
            } else if (actionList.containsKey(context.getString(R.string.ld_registration_past_obstetric_history_title))) {
                actionList.remove(context.getString(R.string.ld_registration_past_obstetric_history_title));
            }


            //Calling the callback method to preload the actions in the actionns list.
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));

            return super.postProcess(s);
        }
    }

    private static class RegistrationAdmissionAction extends LDRegistrationAdmissionAction {
        private final LinkedHashMap<String, BaseLDVisitAction> actionList;
        private Context context;

        public RegistrationAdmissionAction(MemberObject memberObject, LinkedHashMap<String, BaseLDVisitAction> actionList, Context context) {
            super(memberObject);
            this.actionList = actionList;
            this.context = context;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            super.onPayloadReceived(jsonPayload);
            String reasonForAdmission = null;
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                reasonForAdmission = CoreJsonFormUtils.getValue(jsonObject, "reasons_for_admission");
            } catch (Exception e) {
                Timber.e(e);
            }
            BaseLDVisitAction ldRegistrationCurrentLabour = actionList.get(context.getString(R.string.ld_registration_current_labour_title));
            ((LDRegistrationCurrentLabourAction) ldRegistrationCurrentLabour.getLDVisitActionHelper()).setReasonsForAdmission(reasonForAdmission);
        }
    }
}

