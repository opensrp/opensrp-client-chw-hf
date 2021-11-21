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
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;

import java.text.MessageFormat;
import java.util.LinkedHashMap;

import timber.log.Timber;

public class PmtctFollowupVisitInteractorFlv implements PmtctFollowupVisitInteractor.Flavor {
   public String hvlTestValue;
    @Override
    public LinkedHashMap<String, BasePmtctHomeVisitAction> calculateActions(BasePmtctHomeVisitContract.View view, MemberObject memberObject, BasePmtctHomeVisitContract.InteractorCallBack interactorCallBack) throws BasePmtctHomeVisitAction.ValidationException {
        LinkedHashMap<String, BasePmtctHomeVisitAction> actionList = new LinkedHashMap<>();

        Context context = view.getContext();

        evaluatePmtctActions(actionList, memberObject, context);

        return actionList;
    }


    private void evaluatePmtctActions(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, final MemberObject memberObject, Context context) throws BasePmtctHomeVisitAction.ValidationException {

        BasePmtctHomeVisitAction hvlTestAction = new BasePmtctHomeVisitAction.Builder(context, "HVL TEST")
                .withOptional(true)
                .withFormName(Constants.JSON_FORM.getHvlTestForm())
                .withHelper(new HvlTestAction())
                .build();
        actionList.put("HVL TEST", hvlTestAction);

        BasePmtctHomeVisitAction hvlSuppression = new BasePmtctHomeVisitAction.Builder(context, "HVL SUPPRESSION")
                .withOptional(true)
                .withFormName(Constants.JSON_FORM.getHvlSuppressionForm())
                .withHelper(new HvlSuppression())
                .build();
        actionList.put("HVL SUPPRESSION", hvlSuppression);
    }

    private class HvlTestAction extends PmtctVisitAction{
        private String received_hvl_test_value;
        private Context context;

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                received_hvl_test_value = CoreJsonFormUtils.getValue(jsonObject,"hvl_test");
                hvlTestValue = received_hvl_test_value;
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public BasePmtctHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String s) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return MessageFormat.format("HVL Test Value : {0}", received_hvl_test_value);
        }

        @Override
        public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(received_hvl_test_value)) {
                return BasePmtctHomeVisitAction.Status.PENDING;
            } else {
                return BasePmtctHomeVisitAction.Status.COMPLETED;
            }
        }

    }

    private class HvlSuppression extends PmtctVisitAction{
        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public BasePmtctHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String s) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return MessageFormat.format("HVL Test Value : {0}", hvlTestValue);
        }

        @Override
        public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(hvlTestValue)) {
                return BasePmtctHomeVisitAction.Status.PENDING;
            } else {
                return BasePmtctHomeVisitAction.Status.COMPLETED;
            }
        }
    }
}


