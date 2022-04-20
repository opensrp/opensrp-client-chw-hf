package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.HfAncDao;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class AncTtVaccinationAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
    protected MemberObject memberObject;
    protected String tt1_vaccination;
    private String jsonPayload;
    private BaseAncHomeVisitAction.ScheduleStatus scheduleStatus;
    private String subTitle;
    private Context context;

    public AncTtVaccinationAction(MemberObject memberObject) {
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
        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            String checkString = getCheckString(memberObject.getBaseEntityId());

            tt1_vaccination = CoreJsonFormUtils.getValue(jsonObject, checkString);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
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
        if (StringUtils.isBlank(tt1_vaccination))
            return null;

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(context.getString(R.string.tt_vaccination_filled));

        return stringBuilder.toString();
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(tt1_vaccination))
            return BaseAncHomeVisitAction.Status.PENDING;
        else {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        }
    }

    @Override
    public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
        Timber.d("onPayloadReceived");
    }

    private String getCheckString(String baseEntityId) {
        if (HfAncDao.getTTVaccinationType(baseEntityId).equalsIgnoreCase("none")) {
            return "tt1_vaccination";
        } else if (HfAncDao.getTTVaccinationType(baseEntityId).equalsIgnoreCase("tt1")) {
            return "tt2_vaccination";
        } else if (HfAncDao.getTTVaccinationType(baseEntityId).equalsIgnoreCase("tt2")) {
            return "tt3_vaccination";
        }
        return "tt_vaccination";
    }
}
