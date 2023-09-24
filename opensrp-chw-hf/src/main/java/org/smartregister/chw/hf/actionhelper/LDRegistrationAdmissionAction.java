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
public class LDRegistrationAdmissionAction implements BaseLDVisitAction.LDVisitActionHelper {
    protected MemberObject memberObject;
    private String admissionDate;
    private String admissionTime;
    private String admittingPersonName;
    private String admissionFrom;
    private Context context;

    public LDRegistrationAdmissionAction(MemberObject memberObject) {
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
            admissionDate = CoreJsonFormUtils.getValue(jsonObject, "admission_date");
            admissionTime = CoreJsonFormUtils.getValue(jsonObject, "admission_time");
            admittingPersonName = CoreJsonFormUtils.getValue(jsonObject, "admitting_person_name");
            admissionFrom = CoreJsonFormUtils.getValue(jsonObject, "admission_from");
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
            return context.getString(R.string.ld_registration_admission_information_complete);
        else if (isAnyFieldCompleted())
            return context.getString(R.string.ld_registration_admission_information_pending);
        else
            return "";
    }

    /**
     * evaluate if all fields are completed
     **/
    protected boolean isAllFieldsCompleted() {
        return !StringUtils.isBlank(admissionDate) &&
                !StringUtils.isBlank(admissionTime) &&
                !StringUtils.isBlank(admittingPersonName) &&
                !StringUtils.isBlank(admissionFrom);
    }

    /**
     * evaluate if any field has been completed
     **/
    protected boolean isAnyFieldCompleted() {
        return !StringUtils.isBlank(admissionDate) ||
                !StringUtils.isBlank(admissionTime) ||
                !StringUtils.isBlank(admittingPersonName) ||
                !StringUtils.isBlank(admissionFrom);
    }

}
