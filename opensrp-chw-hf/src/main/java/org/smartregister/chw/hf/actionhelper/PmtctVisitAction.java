package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.smartregister.chw.pmtct.domain.VisitDetail;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;

import java.util.List;
import java.util.Map;

public class PmtctVisitAction implements BasePmtctHomeVisitAction.PmtctHomeVisitActionHelper {
    @Override
    public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
        //implement
    }
    @Override
    public String getPreProcessed() {
        return null;
    }

    @Override
    public void onPayloadReceived(String s) {
        //implement
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
        return null;
    }

    @Override
    public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
        return null;
    }

    @Override
    public void onPayloadReceived(BasePmtctHomeVisitAction basePmtctHomeVisitAction) {
        //implement
    }
}
