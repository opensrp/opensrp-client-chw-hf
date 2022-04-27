package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
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
import org.smartregister.chw.hf.actionhelper.NextFollowupVisitAction;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hf.repository.HfLocationRepository;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.chw.core.utils.Utils.getCommonPersonObjectClient;
import static org.smartregister.chw.core.utils.Utils.getDuration;

public class HeiFollowupVisitInteractorFlv implements PmtctFollowupVisitInteractor.Flavor {
    LinkedHashMap<String, BasePmtctHomeVisitAction> actionList = new LinkedHashMap<>();
    Map<String, List<VisitDetail>> details = null;
    BasePmtctHomeVisitContract.InteractorCallBack callBack;

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

        evaluateBaselineInvestigationAction(actionList, details, memberObject, context, baselineInvestigationForm);
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

    private void evaluateAntibodyTest(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, MemberObject memberObject, Context context, JSONObject antibodyTestForm) throws BasePmtctHomeVisitAction.ValidationException {
        BasePmtctHomeVisitAction AntibodyTest = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.antibody_test_sample_collection))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getHeiAntibodyTestSampleCollection())
                .withJsonPayload(antibodyTestForm.toString())
                .withHelper(new HeiAntibodyTestAction(memberObject))
                .build();
        if (HeiDao.isEligibleForAntiBodiesHivTest(memberObject.getBaseEntityId()))
            actionList.put(context.getString(R.string.antibody_test_sample_collection), AntibodyTest);
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

    private void evaluateNextVisitAction(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, Context context) throws BasePmtctHomeVisitAction.ValidationException {
        BasePmtctHomeVisitAction NextFollowupVisitDate = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.next_visit))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getNextFacilityVisitForm())
                .withHelper(new NextFollowupVisitAction())
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
                return jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
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
            if (!visit_type.equalsIgnoreCase("transfer_out")) {
                JSONObject dnaPcrForm = null;
                try {
                    dnaPcrForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getHeiDnaPcrSampleCollection());

                    JSONArray fields = dnaPcrForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                    //update fields
                    JSONObject testAtAge = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "test_at_age");
                    testAtAge.put(JsonFormUtils.VALUE, HeiDao.getNextHivTestAge(memberObject.getBaseEntityId()));

                    JSONObject actualAge = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "actual_age");
                    CommonPersonObjectClient client = getCommonPersonObjectClient(memberObject.getBaseEntityId());
                    actualAge.put(JsonFormUtils.VALUE, getDuration(org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.DOB, false)));

                    //loads details to the form
                    if (details != null && !details.isEmpty()) {
                        JsonFormUtils.populateForm(dnaPcrForm, details);
                    }
                } catch (JSONException e) {
                    Timber.e(e);
                }

                JSONObject antibodyTestForm = null;
                try {
                    antibodyTestForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getHeiAntibodyTestSampleCollection());

                    JSONArray fields = antibodyTestForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                    //update fields
                    JSONObject testAtAge = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "test_at_age");
                    testAtAge.put(JsonFormUtils.VALUE, HeiDao.getNextHivTestAge(memberObject.getBaseEntityId()));

                    JSONObject actualAge = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "actual_age");
                    CommonPersonObjectClient client = getCommonPersonObjectClient(memberObject.getBaseEntityId());
                    actualAge.put(JsonFormUtils.VALUE, getDuration(org.smartregister.family.util.Utils.getValue(client.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.DOB, false)));

                    //loads details to the form
                    if (details != null && !details.isEmpty()) {
                        JsonFormUtils.populateForm(antibodyTestForm, details);
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
                try {
                    evaluateDnaPcrAction(actionList, details, memberObject, context, dnaPcrForm);
                    evaluateAntibodyTest(actionList, details, memberObject, context, antibodyTestForm);
                    evaluateCtxPrescription(actionList, details, memberObject, context, ctxPrescriptionForm);
                    evaluateArvPrescriptionHighRisk(actionList, details, memberObject, context, arvPrescriptionForHighRiskForm);
                    evaluateArvPrescription(actionList, details, memberObject, context, arvPrescriptionForHighAndLowRiskForm);
                    evaluateNextVisitAction(actionList, details, context);
                } catch (Exception e) {
                    Timber.e(e);
                }
            } else {
                actionList.remove(context.getString(R.string.dna_pcr_sample_collection));
                actionList.remove(context.getString(R.string.antibody_test_sample_collection));
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


