package org.smartregister.chw.hf.interactor;

import static org.smartregister.chw.core.utils.Utils.getCommonPersonObjectClient;
import static org.smartregister.chw.core.utils.Utils.getDuration;
import static org.smartregister.chw.hf.interactor.AncFirstFacilityVisitInteractorFlv.initializeHealthFacilitiesList;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.AppExecutors;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.HeiAntibodyTestAction;
import org.smartregister.chw.hf.actionhelper.HeiArvPrescriptionHighOrLowRiskInfantAction;
import org.smartregister.chw.hf.actionhelper.HeiArvPrescrptionHighRiskInfantAction;
import org.smartregister.chw.hf.actionhelper.HeiCtxAction;
import org.smartregister.chw.hf.actionhelper.HeiDnaPcrTestAction;
import org.smartregister.chw.hf.actionhelper.PmtctNextFollowupVisitAction;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.PmtctLibrary;
import org.smartregister.chw.pmtct.contract.BasePmtctHomeVisitContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.domain.Visit;
import org.smartregister.chw.pmtct.domain.VisitDetail;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;
import org.smartregister.chw.pmtct.util.JsonFormUtils;
import org.smartregister.chw.pmtct.util.VisitUtils;
import org.smartregister.chw.referral.util.JsonFormConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Location;
import org.smartregister.domain.LocationTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class HeiFollowupVisitInteractorFlv implements PmtctFollowupVisitInteractor.Flavor {
    LinkedHashMap<String, BasePmtctHomeVisitAction> actionList = new LinkedHashMap<>();
    Map<String, List<VisitDetail>> details = null;
    BasePmtctHomeVisitContract.InteractorCallBack callBack;

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

    @Override
    public LinkedHashMap<String, BasePmtctHomeVisitAction> calculateActions(BasePmtctHomeVisitContract.View view, MemberObject memberObject, BasePmtctHomeVisitContract.InteractorCallBack interactorCallBack) throws BasePmtctHomeVisitAction.ValidationException {
        this.callBack = interactorCallBack;
        Context context = view.getContext();
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = PmtctLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.HEI_FOLLOWUP);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(PmtctLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        evaluateHEIActions(actionList, details, memberObject, context);

        return actionList;
    }

    private void evaluateHEIActions(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, MemberObject memberObject, Context context) throws BasePmtctHomeVisitAction.ValidationException {

        JSONObject baselineInvestigationForm = initializeHealthFacilitiesList(FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getHeiBaselineInvestigation()));

        JSONArray fields = null;
        try {
            fields = baselineInvestigationForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
            //update visit number
            JSONObject visitNumber = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "visit_number");
            visitNumber.put(JsonFormUtils.VALUE, HeiDao.getVisitNumber(memberObject.getBaseEntityId()));
        } catch (JSONException e) {
            Timber.e(e);
        }

        evaluateBaselineInvestigationAction(actionList, details, memberObject, context, baselineInvestigationForm);
    }

    private int getAgeInMonthsFromDate(String dateOfBirth) {
        DateTime date = DateTime.parse(dateOfBirth);
        Months ageInMonths = Months.monthsBetween(date.toLocalDate(), LocalDate.now());
        return ageInMonths.getMonths();
    }

    private void evaluateBaselineInvestigationAction(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, MemberObject memberObject, Context context, JSONObject baselineInvestigationForm) throws BasePmtctHomeVisitAction.ValidationException {
        BasePmtctHomeVisitAction BaselineInvestigation = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.anc_first_visit_baseline_investigation))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getHeiBaselineInvestigation())
                .withJsonPayload(baselineInvestigationForm.toString())
                .withHelper(new HeiBaselineInvestigationAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_first_visit_baseline_investigation), BaselineInvestigation);
    }

    private void evaluateDnaPcrAction(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, MemberObject memberObject, Context context, JSONObject dnaPcrForm) throws BasePmtctHomeVisitAction.ValidationException {
        BasePmtctHomeVisitAction DNAPCRTest = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.dna_pcr_sample_collection))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getHeiDnaPcrSampleCollection())
                .withJsonPayload(dnaPcrForm.toString())
                .withHelper(new HeiDnaPcrTestAction(memberObject))
                .build();
        if (HeiDao.isEligibleForDnaCprHivTest(memberObject.getBaseEntityId()))
            actionList.put(context.getString(R.string.dna_pcr_sample_collection), DNAPCRTest);
    }

    private void evaluateAntibodyTest(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, MemberObject memberObject, Context context) throws BasePmtctHomeVisitAction.ValidationException {
        BasePmtctHomeVisitAction AntibodyTest = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.antibody_test_result))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getHeiHivTestResults())
                .withHelper(new HeiAntibodyTestAction(memberObject))
                .build();
        if (HeiDao.isEligibleForAntiBodiesHivTest(memberObject.getBaseEntityId()))
            actionList.put(context.getString(R.string.antibody_test_result), AntibodyTest);
    }

    private void evaluateCtxPrescription(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, MemberObject memberObject, Context context, JSONObject ctxPrescriptionForm) throws BasePmtctHomeVisitAction.ValidationException {
        BasePmtctHomeVisitAction CtxPrescription = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.ctx_prescription_title))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getHeiCtxPrescription())
                .withJsonPayload(ctxPrescriptionForm.toString())
                .withHelper(new HeiCtxAction(memberObject))
                .build();
        if (HeiDao.isEligibleForCtx(memberObject.getBaseEntityId()))
            actionList.put(context.getString(R.string.ctx_prescription_title), CtxPrescription);
    }

    private void evaluateArvPrescriptionHighRisk(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, MemberObject memberObject, Context context, JSONObject arvPrescriptionForHighRiskForm) throws BasePmtctHomeVisitAction.ValidationException {
        BasePmtctHomeVisitAction ARVPrescriptionHighRisk = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.arv_prescription_azt_and_nvp))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getHeiArvPrescriptionHighRiskInfant())
                .withJsonPayload(arvPrescriptionForHighRiskForm.toString())
                .withHelper(new HeiArvPrescrptionHighRiskInfantAction(memberObject))
                .build();
        if (HeiDao.isEligibleForArvPrescriptionForHighRisk(memberObject.getBaseEntityId()))
            actionList.put(context.getString(R.string.arv_prescription_azt_and_nvp), ARVPrescriptionHighRisk);
    }

    private void evaluateArvPrescription(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, MemberObject memberObject, Context context, JSONObject arvPrescriptionForHighAndLowRiskForm) throws BasePmtctHomeVisitAction.ValidationException {
        BasePmtctHomeVisitAction ARVPrescriptionHighAndLowRisk = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.arv_prescription_nvp))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getHeiArvPrescriptionHighOrLowRiskInfant())
                .withJsonPayload(arvPrescriptionForHighAndLowRiskForm.toString())
                .withHelper(new HeiArvPrescriptionHighOrLowRiskInfantAction(memberObject))
                .build();
        if (HeiDao.isEligibleForArvPrescriptionForHighAndLowRisk(memberObject.getBaseEntityId()))
            actionList.put(context.getString(R.string.arv_prescription_nvp), ARVPrescriptionHighAndLowRisk);
    }

    private void evaluateNextVisitAction(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, Context context, JSONObject nextVisitForm) throws BasePmtctHomeVisitAction.ValidationException {
        BasePmtctHomeVisitAction NextFollowupVisitDate = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.next_visit))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getNextFacilityVisitForm())
                .withJsonPayload(nextVisitForm.toString())
                .withHelper(new PmtctNextFollowupVisitAction())
                .build();
        actionList.put(context.getString(R.string.next_visit), NextFollowupVisitDate);
    }

    private class HeiBaselineInvestigationAction implements BasePmtctHomeVisitAction.PmtctHomeVisitActionHelper {
        protected MemberObject memberObject;
        private String jsonPayload;
        private String visit_type;
        private Context context;
        private String subTitle;
        private BasePmtctHomeVisitAction.ScheduleStatus scheduleStatus;

        public HeiBaselineInvestigationAction(MemberObject memberObject) {
            this.memberObject = memberObject;
        }

        @Override
        public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
            this.jsonPayload = jsonPayload;
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                JSONArray fields = jsonObject.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);

                JSONObject actualAge = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "actual_age");
                CommonPersonObjectClient client = getCommonPersonObjectClient(memberObject.getBaseEntityId());
                actualAge.put(JsonFormUtils.VALUE, getDuration(org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.DOB, false)));

                int age = getAgeInMonthsFromDate(memberObject.getDob());
                JSONObject infantFeedingPractice = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "infant_feeding_practice");
                JSONArray values = infantFeedingPractice.getJSONArray("values");
                JSONArray keys = infantFeedingPractice.getJSONArray("keys");
                if (age < 7) {
                    values.remove(5);
                    values.remove(4);
                    values.remove(3);

                    keys.remove(5);
                    keys.remove(4);
                    keys.remove(3);

                } else {
                    values.remove(2);
                    values.remove(1);
                    values.remove(0);

                    keys.remove(2);
                    keys.remove(1);
                    keys.remove(0);
                }

                return jsonObject.toString();
            } catch (JSONException e) {
                Timber.e(e);
            }
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                visit_type = CoreJsonFormUtils.getValue(jsonObject, "followup_status");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public BasePmtctHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
            return scheduleStatus;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return subTitle;
        }

        @Override
        public String postProcess(String s) {
            if (!visit_type.equalsIgnoreCase("transfer_out") && !visit_type.equalsIgnoreCase("lost_to_followup")) {
                JSONObject dnaPcrForm = null;
                try {
                    dnaPcrForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getHeiDnaPcrSampleCollection());

                    JSONArray fields = dnaPcrForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                    //update fields
                    JSONObject testAtAge = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "test_at_age");
                    testAtAge.put(JsonFormUtils.VALUE, HeiDao.getNextHivTestAge(memberObject.getBaseEntityId()));

                    String heiNumber = HeiDao.getHeiNumber(memberObject.getBaseEntityId());
                    if (!StringUtils.isBlank(heiNumber)) {
                        JSONObject sampleId = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "sample_id");
                        sampleId.put(JsonFormUtils.VALUE, heiNumber);
                        sampleId.put("editable", false);
                        sampleId.put("read_only", true);
                    }

                    //loads details to the form
                    if (details != null && !details.isEmpty()) {
                        JsonFormUtils.populateForm(dnaPcrForm, details);
                    }
                } catch (JSONException e) {
                    Timber.e(e);
                }


                JSONObject arvPrescriptionForHighAndLowRiskForm = null;
                try {
                    arvPrescriptionForHighAndLowRiskForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getHeiArvPrescriptionHighOrLowRiskInfant());

                    JSONArray fields = arvPrescriptionForHighAndLowRiskForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                    //update visit number
                    JSONObject visitNumber = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "visit_number");
                    visitNumber.put(JsonFormUtils.VALUE, HeiDao.getVisitNumber(memberObject.getBaseEntityId()));

                    //loads details to the form
                    if (details != null && !details.isEmpty()) {
                        JsonFormUtils.populateForm(arvPrescriptionForHighAndLowRiskForm, details);
                    }
                } catch (JSONException e) {
                    Timber.e(e);
                }

                JSONObject arvPrescriptionForHighRiskForm = null;
                try {
                    arvPrescriptionForHighRiskForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getHeiArvPrescriptionHighRiskInfant());

                    JSONArray fields = arvPrescriptionForHighRiskForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                    //update visit number
                    JSONObject visitNumber = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "visit_number");
                    visitNumber.put(JsonFormUtils.VALUE, HeiDao.getVisitNumber(memberObject.getBaseEntityId()));

                    //loads details to the form
                    if (details != null && !details.isEmpty()) {
                        JsonFormUtils.populateForm(arvPrescriptionForHighRiskForm, details);
                    }
                } catch (JSONException e) {
                    Timber.e(e);
                }

                JSONObject ctxPrescriptionForm = null;
                try {
                    ctxPrescriptionForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getHeiCtxPrescription());

                    JSONArray fields = ctxPrescriptionForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                    //update visit number
                    JSONObject visitNumber = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "visit_number");
                    visitNumber.put(JsonFormUtils.VALUE, HeiDao.getVisitNumber(memberObject.getBaseEntityId()));

                    //loads details to the form
                    if (details != null && !details.isEmpty()) {
                        JsonFormUtils.populateForm(ctxPrescriptionForm, details);
                    }
                } catch (JSONException e) {
                    Timber.e(e);
                }

                JSONObject nextVisitForm = null;
                try {
                    nextVisitForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getNextFacilityVisitForm());

                    JSONArray fields = nextVisitForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                    //update visit number
                    JSONObject visitNumber = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "visit_number");
                    visitNumber.put(JsonFormUtils.VALUE, HeiDao.getVisitNumber(memberObject.getBaseEntityId()));

                    //loads details to the form
                    if (details != null && !details.isEmpty()) {
                        JsonFormUtils.populateForm(nextVisitForm, details);
                    }
                } catch (JSONException e) {
                    Timber.e(e);
                }
                try {
                    evaluateDnaPcrAction(actionList, details, memberObject, context, dnaPcrForm);
                    evaluateAntibodyTest(actionList, details, memberObject, context);
                    evaluateCtxPrescription(actionList, details, memberObject, context, ctxPrescriptionForm);
                    evaluateArvPrescriptionHighRisk(actionList, details, memberObject, context, arvPrescriptionForHighRiskForm);
                    evaluateArvPrescription(actionList, details, memberObject, context, arvPrescriptionForHighAndLowRiskForm);
                    evaluateNextVisitAction(actionList, details, context, nextVisitForm);
                } catch (Exception e) {
                    Timber.e(e);
                }
            } else {
                actionList.remove(context.getString(R.string.dna_pcr_sample_collection));
                actionList.remove(context.getString(R.string.antibody_test_result));
                actionList.remove(context.getString(R.string.ctx_prescription_title));
                actionList.remove(context.getString(R.string.arv_prescription_azt_and_nvp));
                actionList.remove(context.getString(R.string.arv_prescription_nvp));
                actionList.remove(context.getString(R.string.next_visit));
            }
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            return s;
        }

        @Override
        public String evaluateSubTitle() {
            if (StringUtils.isBlank(visit_type))
                return null;

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(context.getString(R.string.baseline_investigation_conducted));

            return stringBuilder.toString();
        }

        @Override
        public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(visit_type))
                return BasePmtctHomeVisitAction.Status.PENDING;
            else {
                return BasePmtctHomeVisitAction.Status.COMPLETED;
            }
        }

        @Override
        public void onPayloadReceived(BasePmtctHomeVisitAction basePmtctHomeVisitAction) {
            Timber.d("onPayloadReceived");
        }
    }


}


