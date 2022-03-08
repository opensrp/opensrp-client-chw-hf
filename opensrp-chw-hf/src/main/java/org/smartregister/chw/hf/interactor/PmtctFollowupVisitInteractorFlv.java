package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.actionhelper.PmtctVisitAction;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.PmtctLibrary;
import org.smartregister.chw.pmtct.contract.BasePmtctHomeVisitContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.domain.Visit;
import org.smartregister.chw.pmtct.domain.VisitDetail;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;
import org.smartregister.chw.pmtct.util.VisitUtils;

import java.text.MessageFormat;
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
        BasePmtctHomeVisitAction ClinicianDetails = new BasePmtctHomeVisitAction.Builder(context, "Clinician Details")
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getClinicianDetailsForm())
                .withHelper(new ClinicianDetailsAction(memberObject))
                .build();
        actionList.put("Clinician Details", ClinicianDetails);
    }

    private static class ClinicianDetailsAction extends PmtctVisitAction {
        protected MemberObject memberObject;
        private String jsonPayload;

        private String clinician_name;
        private BasePmtctHomeVisitAction.ScheduleStatus scheduleStatus;
        private String subTitle;

        public ClinicianDetailsAction(MemberObject memberObject) {
            super(memberObject);
            this.memberObject = memberObject;
        }

        @Override
        public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
            this.jsonPayload = jsonPayload;
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
            if (clinician_name != null)
                return MessageFormat.format("Attended by: {0}", clinician_name);
            return null;
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


