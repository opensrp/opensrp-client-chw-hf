package org.smartregister.chw.hf.interactor;

import static org.smartregister.util.JsonFormUtils.FIELDS;
import static org.smartregister.util.JsonFormUtils.STEP1;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.PmtctArvLineAction;
import org.smartregister.chw.hf.actionhelper.PmtctBaselineInvestigationAction;
import org.smartregister.chw.hf.actionhelper.PmtctCd4SampleCollection;
import org.smartregister.chw.hf.actionhelper.PmtctCounsellingAction;
import org.smartregister.chw.hf.actionhelper.PmtctDiseaseStagingAction;
import org.smartregister.chw.hf.actionhelper.PmtctNextFollowupVisitAction;
import org.smartregister.chw.hf.actionhelper.PmtctTbScreeningAction;
import org.smartregister.chw.hf.actionhelper.PmtctVisitAction;
import org.smartregister.chw.hf.dao.HfPmtctDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.PmtctLibrary;
import org.smartregister.chw.pmtct.contract.BasePmtctHomeVisitContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.domain.Visit;
import org.smartregister.chw.pmtct.domain.VisitDetail;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;
import org.smartregister.chw.pmtct.util.AppExecutors;
import org.smartregister.chw.pmtct.util.JsonFormUtils;
import org.smartregister.chw.pmtct.util.VisitUtils;
import org.smartregister.chw.referral.util.JsonFormConstants;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PmtctFollowupVisitInteractorFlv implements PmtctFollowupVisitInteractor.Flavor {

    LinkedHashMap<String, BasePmtctHomeVisitAction> actionList = new LinkedHashMap<>();

    private static String getFollowupStatusString(String followup_status, Context context) {
        switch (followup_status) {
            case "continuing_with_services":
                return context.getString(R.string.continuing_with_services);
            case "transfer_out":
                return context.getString(R.string.transfer_out);
            case "lost_to_followup":
                return context.getString(R.string.lost_to_followup);
            default:
                return "";
        }
    }

    @Override
    public LinkedHashMap<String, BasePmtctHomeVisitAction> calculateActions(BasePmtctHomeVisitContract.View view, MemberObject memberObject, BasePmtctHomeVisitContract.InteractorCallBack callBack) throws BasePmtctHomeVisitAction.ValidationException {

        Context context = view.getContext();

        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = PmtctLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), org.smartregister.chw.pmtct.util.Constants.EVENT_TYPE.PMTCT_FOLLOWUP);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(PmtctLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }
        evaluatePmtctActions(view, details, callBack, memberObject, context);

        return actionList;
    }


    private void evaluatePmtctActions(BasePmtctHomeVisitContract.View view, Map<String, List<VisitDetail>> details, BasePmtctHomeVisitContract.InteractorCallBack callBack, MemberObject memberObject, Context context)
            throws BasePmtctHomeVisitAction.ValidationException {

        if (!HfPmtctDao.isNewClient(memberObject.getBaseEntityId())) {
            JSONObject followupStatusForm = null;
            try {
                followupStatusForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getPmtctFollowupStatus());
                JSONArray fields = followupStatusForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);

                //update visit number
                JSONObject visitNumber = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "visit_number");
                visitNumber.put(JsonFormUtils.VALUE, HfPmtctDao.getVisitNumber(memberObject.getBaseEntityId()));

            } catch (Exception e) {
                Timber.e(e);
            }

            BasePmtctHomeVisitAction FollowupStatus = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.pmtct_followup_status_title))
                    .withOptional(false)
                    .withDetails(details)
                    .withFormName(Constants.JsonForm.getPmtctFollowupStatus())
                    .withJsonPayload(followupStatusForm.toString())
                    .withHelper(new PmtctFollowupStatusAction(view, memberObject, callBack, details))
                    .build();

            actionList.put(context.getString(R.string.pmtct_followup_status_title), FollowupStatus);
        } else {
            addActions(details, memberObject, context);
        }
    }

    private void addActions(Map<String, List<VisitDetail>> details, MemberObject memberObject, Context context) {
        JSONObject counsellingForm = null;
        try {
            counsellingForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getPmtctCounselling());

            //loads details to the form
            if (details != null && !details.isEmpty()) {
                JsonFormUtils.populateForm(counsellingForm, details);
            }
            BasePmtctHomeVisitAction Counselling = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.pmtct_counselling_title))
                    .withOptional(true)
                    .withDetails(details)
                    .withFormName(Constants.JsonForm.getPmtctCounselling())
                    .withJsonPayload(counsellingForm.toString())
                    .withHelper(new PmtctCounsellingAction(memberObject))
                    .build();
            actionList.put(context.getString(R.string.pmtct_counselling_title), Counselling);

        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject baselineInvestigationForm = null;
        try {
            baselineInvestigationForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getPmtctBaselineInvestigation());
            JSONObject global = baselineInvestigationForm.getJSONObject("global");

            global.put("isLiverFunctionTestConducted", HfPmtctDao.isLiverFunctionTestConducted(memberObject.getBaseEntityId()));
            global.put("isLiverFunctionTestResultsFilled", HfPmtctDao.isLiverFunctionTestResultsFilled(memberObject.getBaseEntityId()));
            global.put("isRenalFunctionTestConducted", HfPmtctDao.isRenalFunctionTestConducted(memberObject.getBaseEntityId()));
            global.put("isRenalFunctionTestResultsFilled", HfPmtctDao.isRenalFunctionTestResultsFilled(memberObject.getBaseEntityId()));
            //loads details to the form
            if (details != null && !details.isEmpty()) {
                JsonFormUtils.populateForm(baselineInvestigationForm, details);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        JSONObject tbScreeningForm = null;
        try {
            tbScreeningForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getPmtctTbScreening());
            JSONObject global = tbScreeningForm.getJSONObject("global");

            String hasTheClientEverBeenProvidedWithTpTBefore = HfPmtctDao.hasTheClientBeenProvidedWithTpt(memberObject.getBaseEntityId());

            global.put("is_provided_with_tpt_before", (hasTheClientEverBeenProvidedWithTpTBefore != null && !hasTheClientEverBeenProvidedWithTpTBefore.equals("no")) || HfPmtctDao.hasTheClientBeenProvidedWithTptInPreviousSessions(memberObject.getBaseEntityId()));
            global.put("has_the_client_completed_tpt", (hasTheClientEverBeenProvidedWithTpTBefore != null && hasTheClientEverBeenProvidedWithTpTBefore.equals("yes")) || HfPmtctDao.hasTheClientCompletedTpt(memberObject.getBaseEntityId()));

            JSONArray fields = tbScreeningForm.getJSONObject(STEP1).getJSONArray(FIELDS);
            JSONObject hasBeenProvidedWithTptBefore = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "has_been_provided_with_tpt_before");
            if (HfPmtctDao.hasTheClientBeenProvidedWithTptInPreviousSessions(memberObject.getBaseEntityId())) {
                hasBeenProvidedWithTptBefore.remove("relevance");
                hasBeenProvidedWithTptBefore.put("value", "partial_complete");
                hasBeenProvidedWithTptBefore.put("type", "hidden");
            }

            //loads details to the form
            if (details != null && !details.isEmpty()) {
                JsonFormUtils.populateForm(tbScreeningForm, details);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        JSONObject nextVisitForm = null;
        try {
            nextVisitForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getNextFacilityVisitForm());

            //Using this form to store client followup status for new clients
            //Followup status action wont be shown instead the status will be saved here
            if (HfPmtctDao.isNewClient(memberObject.getBaseEntityId())) {
                JSONArray fields = nextVisitForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                JSONObject followupStatus = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "followup_status");
                followupStatus.put(JsonFormUtils.VALUE, "new_client");
            }

            //loads details to the form
            if (details != null && !details.isEmpty()) {
                JsonFormUtils.populateForm(nextVisitForm, details);
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        BasePmtctHomeVisitAction BaselineInvestigation = null;
        try {
            BaselineInvestigation = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.pmtct_baseline_investigation_title))
                    .withOptional(true)
                    .withDetails(details)
                    .withFormName(Constants.JsonForm.getPmtctBaselineInvestigation())
                    .withJsonPayload(baselineInvestigationForm.toString())
                    .withHelper(new PmtctBaselineInvestigationAction(memberObject))
                    .build();
        } catch (BasePmtctHomeVisitAction.ValidationException e) {
            e.printStackTrace();
        }

        if (HfPmtctDao.isEligibleForBaselineInvestigation(memberObject.getBaseEntityId()) || HfPmtctDao.isEligibleForBaselineInvestigationOnFollowupVisit(memberObject.getBaseEntityId()))
            actionList.put(context.getString(R.string.pmtct_baseline_investigation_title), BaselineInvestigation);

        BasePmtctHomeVisitAction HvlSampleCollection = null;
        try {
            HvlSampleCollection = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.hvl_sample_collection))
                    .withOptional(true)
                    .withDetails(details)
                    .withFormName(Constants.JsonForm.getHvlClinicianDetailsForm())
                    .withHelper(new HvlSampleCollectionAction(memberObject))
                    .build();
        } catch (BasePmtctHomeVisitAction.ValidationException e) {
            e.printStackTrace();
        }

        if (HfPmtctDao.isEligibleForHlvTest(memberObject.getBaseEntityId()))
            actionList.put(context.getString(R.string.hvl_sample_collection), HvlSampleCollection);

        BasePmtctHomeVisitAction Cd4SampleCollection = null;
        try {
            Cd4SampleCollection = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.cd4_sample_collection))
                    .withOptional(true)
                    .withDetails(details)
                    .withFormName(Constants.JsonForm.getPmtctCd4SampleCollection())
                    .withHelper(new PmtctCd4SampleCollection(memberObject))
                    .build();
        } catch (BasePmtctHomeVisitAction.ValidationException e) {
            e.printStackTrace();
        }

        if (HfPmtctDao.isEligibleForCD4Retest(memberObject.getBaseEntityId()) || HfPmtctDao.isEligibleForCD4Test(memberObject.getBaseEntityId()))
            actionList.put(context.getString(R.string.cd4_sample_collection), Cd4SampleCollection);


        try {
            BasePmtctHomeVisitAction ClinicalDiseaseStaging = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.clinical_staging_of_hiv))
                    .withOptional(true)
                    .withDetails(details)
                    .withFormName(Constants.JsonForm.getPmtctClinicalStagingOfDisease())
                    .withHelper(new PmtctDiseaseStagingAction(memberObject))
                    .build();
            actionList.put(context.getString(R.string.clinical_staging_of_hiv), ClinicalDiseaseStaging);
        } catch (BasePmtctHomeVisitAction.ValidationException e) {
            e.printStackTrace();
        }

        try {
            BasePmtctHomeVisitAction TbScreening = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.tb_screening_title))
                    .withOptional(true)
                    .withDetails(details)
                    .withFormName(Constants.JsonForm.getPmtctTbScreening())
                    .withJsonPayload(tbScreeningForm.toString())
                    .withHelper(new PmtctTbScreeningAction(memberObject))
                    .build();
            actionList.put(context.getString(R.string.tb_screening_title), TbScreening);

        } catch (BasePmtctHomeVisitAction.ValidationException e) {
            e.printStackTrace();
        }

        try {
            BasePmtctHomeVisitAction ArvPrescription = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.arv_prescription_title))
                    .withOptional(true)
                    .withDetails(details)
                    .withFormName(Constants.JsonForm.getPmtctArvLine())
                    .withHelper(new PmtctArvLineAction(memberObject))
                    .build();
            actionList.put(context.getString(R.string.arv_prescription_title), ArvPrescription);
        } catch (BasePmtctHomeVisitAction.ValidationException e) {
            e.printStackTrace();
        }

        try {
            BasePmtctHomeVisitAction NextFollowupVisitDate = new BasePmtctHomeVisitAction.Builder(context, context.getString(R.string.next_visit))
                    .withOptional(true)
                    .withDetails(details)
                    .withFormName(Constants.JsonForm.getNextFacilityVisitForm())
                    .withJsonPayload(nextVisitForm.toString())
                    .withHelper(new PmtctNextFollowupVisitAction())
                    .build();
            actionList.put(context.getString(R.string.next_visit), NextFollowupVisitDate);
        } catch (BasePmtctHomeVisitAction.ValidationException e) {
            Timber.e(e);
        }
    }

    private static class HvlSampleCollectionAction extends PmtctVisitAction {
        protected MemberObject memberObject;
        private String jsonPayload;

        private String clinician_name;
        private BasePmtctHomeVisitAction.ScheduleStatus scheduleStatus;
        private String subTitle;
        private Context context;

        public HvlSampleCollectionAction(MemberObject memberObject) {
            super(memberObject);
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
            } catch (Exception e) {
                Timber.e(e);
            }
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                clinician_name = CoreJsonFormUtils.getValue(jsonObject, "clinician_name");
            } catch (JSONException e) {
                Timber.e(e);
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
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            if (StringUtils.isBlank(clinician_name))
                return null;

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(context.getString(R.string.hvl_sample_collected));

            return stringBuilder.toString();
        }

        @Override
        public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(clinician_name)) {
                return BasePmtctHomeVisitAction.Status.PENDING;
            } else {
                return BasePmtctHomeVisitAction.Status.COMPLETED;
            }
        }

        @Override
        public void onPayloadReceived(BasePmtctHomeVisitAction basePmtctHomeVisitAction) {
            Timber.d("onPayloadReceived");
        }
    }

    private class PmtctFollowupStatusAction extends org.smartregister.chw.hf.actionhelper.PmtctFollowupStatusAction {
        Map<String, List<VisitDetail>> details;
        BasePmtctHomeVisitContract.View view;
        MemberObject memberObject;
        BasePmtctHomeVisitContract.InteractorCallBack callBack;

        public PmtctFollowupStatusAction(BasePmtctHomeVisitContract.View view, MemberObject memberObject, BasePmtctHomeVisitContract.InteractorCallBack callBack, Map<String, List<VisitDetail>> details) {
            super(memberObject);
            this.details = details;
            this.view = view;
            this.memberObject = memberObject;
            this.callBack = callBack;
        }

        @Override
        public String evaluateSubTitle() {
            if (StringUtils.isBlank(followup_status))
                return null;

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(view.getContext().getString(R.string.pmtct_followup_status));
            stringBuilder.append(" ");
            stringBuilder.append(getFollowupStatusString(followup_status, view.getContext()));

            return stringBuilder.toString();
        }

        @Override
        public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(followup_status))
                return BasePmtctHomeVisitAction.Status.PENDING;
            else if (followup_status.equals("continuing_with_services") || followup_status.equals("new_client")) {
                return BasePmtctHomeVisitAction.Status.COMPLETED;
            } else {
                return BasePmtctHomeVisitAction.Status.PARTIALLY_COMPLETED;
            }
        }

        @Override
        public String postProcess(String s) {
            Context context = view.getContext();
            if (followup_status.equals("continuing_with_services") || followup_status.equals("new_client")) {
                addActions(details, memberObject, context);

            } else {
                actionList.remove(context.getString(R.string.pmtct_counselling_title));
                actionList.remove(context.getString(R.string.pmtct_baseline_investigation_title));
                actionList.remove(context.getString(R.string.hvl_sample_collection));
                actionList.remove(context.getString(R.string.cd4_sample_collection));
                actionList.remove(context.getString(R.string.clinical_staging_of_hiv));
                actionList.remove(context.getString(R.string.tb_screening_title));
                actionList.remove(context.getString(R.string.arv_prescription_title));
                actionList.remove(context.getString(R.string.next_visit));
            }
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            return super.postProcess(s);
        }
    }

}


