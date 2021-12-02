package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.actionhelper.PmtctVisitAction;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.contract.BasePmtctHomeVisitContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.domain.VisitDetail;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;

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

        evaluatePmtctActions(actionList, memberObject, context);

        return actionList;
    }


    private void evaluatePmtctActions(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, final MemberObject memberObject, Context context) throws BasePmtctHomeVisitAction.ValidationException {

        BasePmtctHomeVisitAction HVLFollowup = new BasePmtctHomeVisitAction.Builder(context, "HIV Viral Load (HVL)")
                .withOptional(false)
                .withFormName(Constants.JsonForm.getHvlSuppressionForm())
                .withHelper(new HVLAction(memberObject))
                .build();
        actionList.put("HIV Viral Load (HVL)", HVLFollowup);

        BasePmtctHomeVisitAction EAC = new BasePmtctHomeVisitAction.Builder(context, "Enhanced Adherence Counselling (EAC)")
                .withOptional(false)
                .withFormName(Constants.JsonForm.getPmtctEacFirst())
                .build();
        actionList.put("Enhanced Adherence Counselling (EAC)", EAC);
    }

    private class HVLAction extends PmtctVisitAction {
        protected MemberObject memberObject;
        private Context context;
        private String jsonPayload;

        private String hvl_suppression;
        private BasePmtctHomeVisitAction.ScheduleStatus scheduleStatus;
        private String subTitle;

        public HVLAction(MemberObject memberObject) {
            super(memberObject);
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
                hvl_suppression = CoreJsonFormUtils.getValue(jsonObject, "hvl_suppression_followup");
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
            if (StringUtils.isBlank(hvl_suppression))
                return null;

            return MessageFormat.format("HVL Suppression is: {0}", hvl_suppression);
        }

        @Override
        public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(hvl_suppression)) {
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


