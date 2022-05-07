package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;

import java.util.List;
import java.util.Map;

/**
 * @author issyzac 5/7/22
 */
public class LDPartographFetalWellBeingActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    protected MemberObject memberObject;

    public LDPartographFetalWellBeingActionHelper(MemberObject memberObject){
        this.memberObject = memberObject;
    }

    @Override
    public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {

    }

    @Override
    public String getPreProcessed() {
        return null;
    }

    @Override
    public void onPayloadReceived(String s) {

    }

    @Override
    public BaseLDVisitAction.ScheduleStatus getPreProcessedStatus() {
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
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        return null;
    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction baseLDVisitAction) {

    }
}
