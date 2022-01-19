package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.domain.VisitDetail;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PmtctVisitAction implements BasePmtctHomeVisitAction.PmtctHomeVisitActionHelper {
    protected MemberObject memberObject;
    private String jsonPayload;

    private String visit;
    private BasePmtctHomeVisitAction.ScheduleStatus scheduleStatus;
    private String subTitle;

    public PmtctVisitAction(MemberObject memberObject) {
        this.memberObject = memberObject;
    }

    @Override
    public  void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
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
            visit = CoreJsonFormUtils.getValue(jsonObject, "");
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
        if (StringUtils.isBlank(visit))
            return null;

        return MessageFormat.format("Visit Date: {0}", visit);
    }

    @Override
    public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(visit)) {
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
