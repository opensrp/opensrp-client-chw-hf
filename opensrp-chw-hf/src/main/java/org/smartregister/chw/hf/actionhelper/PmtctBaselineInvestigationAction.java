package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.domain.VisitDetail;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PmtctBaselineInvestigationAction implements BasePmtctHomeVisitAction.PmtctHomeVisitActionHelper {
    protected MemberObject memberObject;
    private String jsonPayload;
    private String liver_function_test_conducted;
    private String receive_liver_function_test_results;
    private String renal_function_test_conducted;
    private String receive_renal_function_test_results;
    private Context context;
    private String subTitle;
    private BasePmtctHomeVisitAction.ScheduleStatus scheduleStatus;

    public PmtctBaselineInvestigationAction(MemberObject memberObject) {
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
            liver_function_test_conducted = CoreJsonFormUtils.getValue(jsonObject, "liver_function_test_conducted");
            receive_liver_function_test_results = CoreJsonFormUtils.getValue(jsonObject, "receive_liver_function_test_results");
            renal_function_test_conducted = CoreJsonFormUtils.getValue(jsonObject, "renal_function_test_conducted");
            receive_renal_function_test_results = CoreJsonFormUtils.getValue(jsonObject, "receive_renal_function_test_results");
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
        return s;
    }

    @Override
    public String evaluateSubTitle() {
        if (StringUtils.isBlank(liver_function_test_conducted) && StringUtils.isBlank(receive_liver_function_test_results) && StringUtils.isBlank(renal_function_test_conducted) && StringUtils.isBlank(receive_renal_function_test_results))
            return null;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getString(R.string.pmtct_baseline_investigation_complete));

        return stringBuilder.toString();
    }

    @Override
    public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(liver_function_test_conducted) && StringUtils.isBlank(receive_liver_function_test_results) && StringUtils.isBlank(renal_function_test_conducted) && StringUtils.isBlank(receive_renal_function_test_results))
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
