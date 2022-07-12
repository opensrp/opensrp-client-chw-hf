package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * @author ilakozejumanne@gmail.com
 * 06/05/2022
 */
public class LDRegistrationAncClinicFindingsAction implements BaseLDVisitAction.LDVisitActionHelper {
    protected MemberObject memberObject;
    private String numberOfVisits;
    private String iptDoses;
    private String ttDoses;
    private String llinUsed;
    private String hbLevel;
    private String hbTestDate;
    private String pmtct;
    private String syphilis;
    private String bloodGroup;
    private String rhFactor;
    private String hbTestConducted;
    private Context context;

    public LDRegistrationAncClinicFindingsAction(MemberObject memberObject) {
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
            numberOfVisits = CoreJsonFormUtils.getValue(jsonObject, "number_of_visits");
            iptDoses = CoreJsonFormUtils.getValue(jsonObject, "ipt_doses");
            ttDoses = CoreJsonFormUtils.getValue(jsonObject, "tt_doses");
            llinUsed = CoreJsonFormUtils.getValue(jsonObject, "llin_used");
            hbTestConducted = CoreJsonFormUtils.getValue(jsonObject, "hb_test");
            hbLevel = CoreJsonFormUtils.getValue(jsonObject, "hb_level");
            hbTestDate = CoreJsonFormUtils.getValue(jsonObject, "hb_test_date");
            pmtct = CoreJsonFormUtils.getValue(jsonObject, "anc_hiv_status");
            syphilis = CoreJsonFormUtils.getValue(jsonObject, "syphilis");
            bloodGroup = CoreJsonFormUtils.getValue(jsonObject, "blood_group");
            rhFactor = CoreJsonFormUtils.getValue(jsonObject, "rh_factor");
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
            return context.getString(R.string.ld_registration_anc_clinic_findings_complete);
        else if (isAnyFieldCompleted())
            return context.getString(R.string.ld_registration_anc_clinic_findings_pending);
        return "";
    }

    /**
     * evaluate if all fields are completed
     **/
    private boolean isAllFieldsCompleted() {
        return !StringUtils.isBlank(numberOfVisits) &&
                !StringUtils.isBlank(iptDoses) &&
                !StringUtils.isBlank(ttDoses) &&
                !StringUtils.isBlank(llinUsed) &&
                (!hbTestConducted.equals("yes") || (!StringUtils.isBlank(hbLevel) &&
                        !StringUtils.isBlank(hbTestDate))) &&
                !StringUtils.isBlank(pmtct) &&
                !StringUtils.isBlank(syphilis) &&
                !StringUtils.isBlank(bloodGroup) &&
                !StringUtils.isBlank(rhFactor);
    }

    /**
     * evaluate if any field has been completed
     **/
    private boolean isAnyFieldCompleted() {
        return !StringUtils.isBlank(numberOfVisits) ||
                !StringUtils.isBlank(iptDoses) ||
                !StringUtils.isBlank(ttDoses) ||
                !StringUtils.isBlank(llinUsed) ||
                !StringUtils.isBlank(hbLevel) ||
                !StringUtils.isBlank(hbTestDate) ||
                !StringUtils.isBlank(pmtct) ||
                !StringUtils.isBlank(syphilis) ||
                !StringUtils.isBlank(bloodGroup) ||
                !StringUtils.isBlank(rhFactor);

    }

}
