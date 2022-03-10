package org.smartregister.chw.hf.interactor;

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
import org.smartregister.chw.pmtct.util.JsonFormUtils;
import org.smartregister.chw.pmtct.util.VisitUtils;
import org.smartregister.chw.referral.util.JsonFormConstants;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PmtctFollowupVisitInteractorFlv implements PmtctFollowupVisitInteractor.Flavor {


    @Override
    public LinkedHashMap<String, BasePmtctHomeVisitAction> calculateActions(BasePmtctHomeVisitContract.View view, MemberObject memberObject, BasePmtctHomeVisitContract.InteractorCallBack interactorCallBack) throws BasePmtctHomeVisitAction.ValidationException {
        LinkedHashMap<String, BasePmtctHomeVisitAction> actionList = new LinkedHashMap<>();

        Context context = view.getContext();

        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = PmtctLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), org.smartregister.chw.pmtct.util.Constants.EVENT_TYPE.PMTCT_FOLLOWUP);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(PmtctLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }
        evaluatePmtctActions(actionList, details, memberObject, context);

        return actionList;
    }


    private void evaluatePmtctActions(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, MemberObject memberObject, Context context) throws BasePmtctHomeVisitAction.ValidationException {
        JSONObject counsellingForm = null;
        try {
            counsellingForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getPmtctCounselling());

            JSONArray fields = counsellingForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
            //update visit number
            JSONObject visitNumber = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "visit_number");
            visitNumber.put(org.smartregister.chw.pmtct.util.JsonFormUtils.VALUE, HfPmtctDao.getVisitNumber(memberObject.getBaseEntityId()));

            //loads details to the form
            if (details != null && !details.isEmpty()) {
                JsonFormUtils.populateForm(counsellingForm, details);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        BasePmtctHomeVisitAction Counselling = new BasePmtctHomeVisitAction.Builder(context, "Counselling")
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getPmtctCounselling())
                .withJsonPayload(counsellingForm.toString())
                .withHelper(new PmtctCounsellingAction(memberObject))
                .build();
        actionList.put("Counselling", Counselling);

        BasePmtctHomeVisitAction BaselineInvestigation = new BasePmtctHomeVisitAction.Builder(context, "Baseline Investigation")
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getPmtctBaselineInvestigation())
                .withHelper(new PmtctBaselineInvestigationAction(memberObject))
                .build();

        if (HfPmtctDao.isEligibleForBaselineInvestigation(memberObject.getBaseEntityId()) || HfPmtctDao.isEligibleForBaselineInvestigationOnFollowupVisit(memberObject.getBaseEntityId()))
            actionList.put("Baseline Investigation", BaselineInvestigation);

        BasePmtctHomeVisitAction HvlSampleCollection = new BasePmtctHomeVisitAction.Builder(context, "HVL Sample Collection")
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getHvlClinicianDetailsForm())
                .withHelper(new HvlSampleCollectionAction(memberObject))
                .build();

        if (HfPmtctDao.isEligibleForHlvTest(memberObject.getBaseEntityId()))
            actionList.put("HVL Sample Collection", HvlSampleCollection);

        BasePmtctHomeVisitAction Cd4SampleCollection = new BasePmtctHomeVisitAction.Builder(context, "CD4 Sample Collection")
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getPmtctCd4SampleCollection())
                .withHelper(new PmtctCd4SampleCollection(memberObject))
                .build();

        if (HfPmtctDao.isEligibleForCD4Retest(memberObject.getBaseEntityId()) || HfPmtctDao.isEligibleForCD4Test(memberObject.getBaseEntityId()))
            actionList.put("CD4 Sample Collection", Cd4SampleCollection);

        BasePmtctHomeVisitAction ClinicalDiseaseStaging = new BasePmtctHomeVisitAction.Builder(context, "Clinical Staging of HIV")
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getPmtctClinicalStagingOfDisease())
                .withHelper(new PmtctDiseaseStagingAction(memberObject))
                .build();
        actionList.put("Clinical Staging of HIV", ClinicalDiseaseStaging);

        BasePmtctHomeVisitAction TbScreening = new BasePmtctHomeVisitAction.Builder(context, "TB Screening")
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getPmtctTbScreening())
                .withHelper(new PmtctTbScreeningAction(memberObject))
                .build();
        actionList.put("TB Screening", TbScreening);

        BasePmtctHomeVisitAction ArvPrescription = new BasePmtctHomeVisitAction.Builder(context, "ARV Prescription")
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getPmtctArvLine())
                .withHelper(new PmtctArvLineAction(memberObject))
                .build();
        actionList.put("ARV Prescription", ArvPrescription);
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

}


