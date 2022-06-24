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
public class LDRegistrationTrueLabourConfirmationAction implements BaseLDVisitAction.LDVisitActionHelper {
    protected MemberObject memberObject;
    private String trueLabour;
    protected String labourConfirmation;
    protected String clientAdmitted;
    private Context context;

    public LDRegistrationTrueLabourConfirmationAction(MemberObject memberObject) {
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
            trueLabour = CoreJsonFormUtils.getValue(jsonObject, "true_labour");
            labourConfirmation = CoreJsonFormUtils.getValue(jsonObject, "labour_confirmation");
            clientAdmitted = CoreJsonFormUtils.getValue(jsonObject, "admit_client");
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
        else
            return BaseLDVisitAction.Status.PENDING;
    }

    @Override
    public String evaluateSubTitle() {
        if (isAllFieldsCompleted() && (labourConfirmation.equalsIgnoreCase("true") || clientAdmitted.equalsIgnoreCase("yes"))) {
            if (clientAdmitted.equalsIgnoreCase("yes")) {
                return context.getString(R.string.ld_registration_true_labour_client_admitted);
            }
            return context.getString(R.string.ld_registration_true_labour_complete);
        } else if (isAllFieldsCompleted() && labourConfirmation.equalsIgnoreCase("false")) {
            return context.getString(R.string.ld_registration_false_labour_complete);
        }
        return "";
    }

    /**
     * evaluate if all fields are completed
     **/
    public boolean isAllFieldsCompleted() {
        return !StringUtils.isBlank(trueLabour);
    }

}
