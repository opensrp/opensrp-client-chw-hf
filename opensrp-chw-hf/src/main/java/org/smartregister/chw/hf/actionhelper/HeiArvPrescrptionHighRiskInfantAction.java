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

public class HeiArvPrescrptionHighRiskInfantAction implements BasePmtctHomeVisitAction.PmtctHomeVisitActionHelper {
    protected MemberObject memberObject;
    private String jsonPayload;
    private String provided_azt_nvp_syrup;
    private String provided_other_combinations;
    private Context context;
    private String subTitle;
    private BasePmtctHomeVisitAction.ScheduleStatus scheduleStatus;

    public HeiArvPrescrptionHighRiskInfantAction(MemberObject memberObject) {
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
            provided_azt_nvp_syrup = CoreJsonFormUtils.getValue(jsonObject, "provided_azt_nvp_syrup");
            provided_other_combinations = CoreJsonFormUtils.getValue(jsonObject, "provided_other_combinations");
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
        if (StringUtils.isBlank(provided_azt_nvp_syrup))
            return null;

        return provided_azt_nvp_syrup.equalsIgnoreCase("yes") ? context.getString(R.string.hei_arv_prescription_given) : provided_other_combinations.equalsIgnoreCase("yes") ? context.getString(R.string.other_combination_given) : context.getString(R.string.arv_pres_other_combinations_not_given);
    }

    @Override
    public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(provided_azt_nvp_syrup))
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
