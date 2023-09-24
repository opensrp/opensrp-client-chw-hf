package org.smartregister.chw.hf.interactor;

import static com.vijay.jsonwizard.widgets.TimePickerFactory.KEY.KEY;
import static com.vijay.jsonwizard.widgets.TimePickerFactory.KEY.VALUE;
import static org.smartregister.chw.hf.interactor.AncFirstFacilityVisitInteractorFlv.initializeHealthFacilitiesList;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.dao.AncDao;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.LDRegistrationAdmissionAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationAncClinicFindingsAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationCurrentLabourAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationLabourStageAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationObstetricHistoryAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationPastObstetricHistoryAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationTriageAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationTrueLabourConfirmationAction;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.hf.dao.HfPmtctDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.contract.BaseLDVisitContract;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.chw.ld.util.AppExecutors;
import org.smartregister.chw.ld.util.JsonFormUtils;
import org.smartregister.chw.ld.util.VisitUtils;
import org.smartregister.chw.referral.util.JsonFormConstants;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private static JSONObject admissionInformationForm;

    public LDRegistrationInteractorFlv(String baseEntityId) {
    }

    private void populateObstetricForm(JSONArray fields, org.smartregister.chw.anc.domain.MemberObject memberObject) throws JSONException {
        JSONObject gravida = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "gravida");
        JSONObject childrenAlive = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "children_alive");
        JSONObject parity = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "para");
        JSONObject lastMenstrualPeriod = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "last_menstrual_period");
        JSONObject pastMedicalSurgicalHistory = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "past_medical_surgical_history");
        JSONObject otherPastMedicalSurgicalHistory = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "other_past_medical_surgical_history");

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
        String otherPastMedicalAndSurgicalHistory = HfAncDao.getOtherMedicalAndSurgicalHistory(memberObject.getBaseEntityId());
        otherPastMedicalSurgicalHistory.put(org.smartregister.family.util.JsonFormUtils.VALUE, otherPastMedicalAndSurgicalHistory);
    }

    public static void setCheckBoxValues(JSONArray options, String value) {
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
        JSONObject managementProvidedForSyphilis = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "management_provided_for_syphilis");
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
        hbTestConducted.put(org.smartregister.family.util.JsonFormUtils.VALUE, lastMeasuredHb.equals("") ? "no" : "yes");
        if (!lastMeasuredHB.equals("")) {
            lastMeasuredHB.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getLastMeasuredHB(memberObject.getBaseEntityId()));
            lastMeasuredHBDate.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getLastMeasuredHBDate(memberObject.getBaseEntityId()));
        }
        syphilis.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getSyphilisTestResult(memberObject.getBaseEntityId()));
        managementProvidedForSyphilis.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getSyphilisTreatment(memberObject.getBaseEntityId()) ? "yes" : "no");

        bloodGroup.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getBloodGroup(memberObject.getBaseEntityId()));
        rhFactor.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getRhFactor(memberObject.getBaseEntityId()));
        if (HfAncDao.isClientKnownOnArt(memberObject.getBaseEntityId())) {
            pmtct.put(org.smartregister.family.util.JsonFormUtils.VALUE, "known_on_art_before_this_pregnancy");
        } else if (HfAncDao.getHivStatus(memberObject.getBaseEntityId()) != null && !HfAncDao.getHivStatus(memberObject.getBaseEntityId()).equalsIgnoreCase("null")) {
            pmtct.put(org.smartregister.family.util.JsonFormUtils.VALUE, HfAncDao.getHivStatus(memberObject.getBaseEntityId()).equalsIgnoreCase("positive") ? "positive" : "negative");
        }
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
        admissionInformationForm = initializeHealthFacilitiesList(FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryAdmissionInformation()));


        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.LD_REGISTRATION);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(LDLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));

                if (!details.isEmpty()) {
                    JsonFormUtils.populateForm(obstetricForm, details);
                    JsonFormUtils.populateForm(ancClinicForm, details);
                    JsonFormUtils.populateForm(ancTriageForm, details);
                    JsonFormUtils.populateForm(admissionInformationForm, details);
                }
            }
        } else {
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
                    BaseLDVisitAction ldStage = new BaseLDVisitAction.Builder(context, context.getString(R.string.labour_and_delivery_labour_stage_title))
                            .withOptional(false)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryLabourStage())
                            .withHelper(new LabourStageAction(memberObject, actionList, details, callBack, context))
                            .build();

                    actionList.put(context.getString(R.string.labour_and_delivery_labour_stage_title), ldStage);
                    ldStage.evaluateStatus();
                } catch (Exception e) {
                    Timber.e(e);
                }
            } else {
                //Removing the next actions  the client is confirmed with False Labour.
                actionList.remove(context.getString(R.string.labour_and_delivery_labour_stage_title));
            }

            //Calling the callback method to preload the actions in the actions list.
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            return super.postProcess(s);
        }
    }



    private static class LabourStageAction extends LDRegistrationLabourStageAction {
        private final LinkedHashMap<String, BaseLDVisitAction> actionList;
        private final Context context;
        private final Map<String, List<VisitDetail>> details;
        private final BaseLDVisitContract.InteractorCallBack callBack;

        public LabourStageAction(MemberObject memberObject, LinkedHashMap<String, BaseLDVisitAction> actionList, Map<String, List<VisitDetail>> details, BaseLDVisitContract.InteractorCallBack callBack, Context context) {
            super(memberObject);
            this.actionList = actionList;
            this.details = details;
            this.callBack = callBack;
            this.context = context;
        }

        @Override
        public String postProcess(String s) {

            if (labourStage.equalsIgnoreCase("1") || labourStage.equalsIgnoreCase("2")) {
                //Adding the next actions if the client is in stage 1 or stage 2 of labour.
                try {
                    BaseLDVisitAction ldRegistrationAdmissionInformation = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_admission_information_title))
                            .withOptional(false)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryAdmissionInformation())
                            .withJsonPayload(admissionInformationForm.toString())
                            .withHelper(new RegistrationAdmissionAction(memberObject, actionList, context))
                            .build();

                    actionList.put(context.getString(R.string.ld_registration_admission_information_title), ldRegistrationAdmissionInformation);
                    ldRegistrationAdmissionInformation.evaluateStatus();
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

