package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * @author ilakozejumanne@gmail.com
 * 06/05/2022
 */
public class LDRegistrationObstetricHistoryAction implements BaseLDVisitAction.LDVisitActionHelper {
    protected MemberObject memberObject;
    private String gravida;
    protected String para;
    private String childrenAlive;
    private String numberOfAbortion;
    private String lastMenstrualPeriod;
    private Context context;

    public LDRegistrationObstetricHistoryAction(MemberObject memberObject) {
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
            gravida = CoreJsonFormUtils.getValue(jsonObject, "gravida");
            para = CoreJsonFormUtils.getValue(jsonObject, "para");
            childrenAlive = CoreJsonFormUtils.getValue(jsonObject, "children_alive");
            numberOfAbortion = CoreJsonFormUtils.getValue(jsonObject, "number_of_abortion");
            lastMenstrualPeriod = CoreJsonFormUtils.getValue(jsonObject, "last_menstrual_period");
        } catch (JSONException e) {
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
    public void onPayloadReceived(BaseLDVisitAction baseAncHomeVisitAction) {
        Timber.v("onPayloadReceived");
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (isAllFieldsCompleted())
            return BaseLDVisitAction.Status.COMPLETED;
        else if (isAnyFieldCompleted())
            return BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
        else
            return BaseLDVisitAction.Status.PENDING;
    }

    @Override
    public String evaluateSubTitle() {
        if (isAllFieldsCompleted())
            return context.getString(R.string.ld_registration_obstetric_history_complete);
        else if (isAnyFieldCompleted())
            return context.getString(R.string.ld_registration_obstetric_history_pending);
        return "";
    }

    /**
     * evaluate if all fields are completed
     **/
    private boolean isAllFieldsCompleted() {
        return !StringUtils.isBlank(gravida) &&
                !StringUtils.isBlank(para) &&
                !StringUtils.isBlank(childrenAlive) &&
                !StringUtils.isBlank(numberOfAbortion) &&
                !StringUtils.isBlank(lastMenstrualPeriod);
    }

    /**
     * evaluate if any field has been completed
     **/
    private boolean isAnyFieldCompleted() {
        return !StringUtils.isBlank(gravida) ||
                !StringUtils.isBlank(para) ||
                !StringUtils.isBlank(childrenAlive) ||
                !StringUtils.isBlank(numberOfAbortion) ||
                !StringUtils.isBlank(lastMenstrualPeriod);
    }

}
