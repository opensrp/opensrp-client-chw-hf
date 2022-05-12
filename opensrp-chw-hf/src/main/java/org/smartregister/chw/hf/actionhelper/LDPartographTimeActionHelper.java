package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import com.fasterxml.jackson.databind.util.JSONPObject;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.JsonFormUtils;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * @author issyzac 5/12/22
 */
public class LDPartographTimeActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private MemberObject memberObject;
    private Context context;
    private String time;

    public LDPartographTimeActionHelper(MemberObject memberObject){
        this.memberObject = memberObject;
    }

    @Override
    public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
        this.context = context;
    }

    @Override
    public String getPreProcessed() {
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            time = CoreJsonFormUtils.getValue(jsonObject, "partograph_time");
        }catch (JSONException e){
            Timber.e(e);
        }
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
        return context.getString(R.string.partograph_time, time);
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(time))
            return BaseLDVisitAction.Status.PENDING;
        else
            return BaseLDVisitAction.Status.COMPLETED;
    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction baseLDVisitAction) {

    }
}
