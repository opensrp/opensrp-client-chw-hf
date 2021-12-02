package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.pmtct.domain.Visit;
import org.smartregister.chw.pmtct.util.VisitUtils;
import org.smartregister.chw.core.interactor.CorePmtctHomeVisitInteractor;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.actionhelper.PmtctVisitAction;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.PmtctLibrary;
import org.smartregister.chw.pmtct.contract.BasePmtctHomeVisitContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.domain.VisitDetail;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class EacVisitInteractorFlv implements CorePmtctHomeVisitInteractor.Flavor {
    @Override
    public LinkedHashMap<String, BasePmtctHomeVisitAction> calculateActions(BasePmtctHomeVisitContract.View view, MemberObject memberObject, BasePmtctHomeVisitContract.InteractorCallBack interactorCallBack) throws BasePmtctHomeVisitAction.ValidationException {
        LinkedHashMap<String, BasePmtctHomeVisitAction> actionList = new LinkedHashMap<>();

        Context context = view.getContext();

        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = PmtctLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.PMTCT_EAC_VISIT);
        if (lastVisit != null) {
            details = VisitUtils.getVisitGroups(PmtctLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
        }
    }

        evaluateEacActions(actionList, details, memberObject, context);

        return actionList;
    }

    private void evaluateEacActions(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, MemberObject memberObject, Context context) throws BasePmtctHomeVisitAction.ValidationException {
        BasePmtctHomeVisitAction EAC = new BasePmtctHomeVisitAction.Builder(context, "Enhanced Adherence Counselling (EAC)")
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.getPmtctEacFirst())
                .withHelper(new EACFirstVisitAction(memberObject))
                .build();
        actionList.put("Enhanced Adherence Counselling (EAC)", EAC);
    }

    private class EACFirstVisitAction extends PmtctVisitAction {
        protected MemberObject memberObject;
        private Context context;
        private String jsonPayload;

        private String first_visit;
        private BasePmtctHomeVisitAction.ScheduleStatus scheduleStatus;
        private String subTitle;

        public EACFirstVisitAction(MemberObject memberObject) {
            this.memberObject = memberObject;
        }

        @Override
        public  void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
            this.context = context;
            this.jsonPayload = jsonPayload;
        }

        @Override
        public String getPreProcessed() {
            try{
                JSONObject jsonObject = new JSONObject(jsonPayload);
                return jsonObject.toString();
            }catch (Exception e){
                Timber.e(e);
            }
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try{
                JSONObject jsonObject = new JSONObject(jsonPayload);
                first_visit = CoreJsonFormUtils.getValue(jsonObject, "eac_day_1");
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
            if (StringUtils.isBlank(first_visit))
                return null;

            return MessageFormat.format("Visit Date: {0}", first_visit);
        }

        @Override
        public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(first_visit)) {
                return BasePmtctHomeVisitAction.Status.PENDING;
            } else {
                return BasePmtctHomeVisitAction.Status.COMPLETED;
            }
        }

        @Override
        public void onPayloadReceived(BasePmtctHomeVisitAction basePmtctHomeVisitAction){
            Timber.d("onPayloadReceived");
        }
    }

}
