package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by Kassim Sheghembe on 2022-05-09
 */
public class LDVaginalExaminationActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private String vaginal_exam_date;
    private String vaginal_exam_time;
    private String cervix_state;
    private String cervix_dilation;
    private String presenting_part;
    private String occiput_position;
    private String moulding;
    private String moulding_options;
    private String station;
    private String amniotic_fluid;
    private String decision;

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {

    }

    @Override
    public String getPreProcessed() {
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        vaginal_exam_date = JsonFormUtils.getFieldValue(jsonPayload, "vaginal_exam_date");
        vaginal_exam_time = JsonFormUtils.getFieldValue(jsonPayload, "vaginal_exam_time");
        cervix_state = JsonFormUtils.getFieldValue(jsonPayload, "cervix_state");
        cervix_dilation = JsonFormUtils.getFieldValue(jsonPayload, "cervix_dilation");
        presenting_part = JsonFormUtils.getFieldValue(jsonPayload, "presenting_part");
        occiput_position = JsonFormUtils.getFieldValue(jsonPayload, "occiput_position");
        moulding = JsonFormUtils.getFieldValue(jsonPayload, "moulding");
        moulding_options = JsonFormUtils.getFieldValue(jsonPayload, "moulding_options");
        station = JsonFormUtils.getFieldValue(jsonPayload, "station");
        amniotic_fluid = JsonFormUtils.getFieldValue(jsonPayload, "amniotic_fluid");
        decision = JsonFormUtils.getFieldValue(jsonPayload, "decision");
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
    public String postProcess(String jsonPayload) {
        return null;
    }

    @Override
    public String evaluateSubTitle() {
        return null;
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (isFullyCompleted()) {
            return BaseLDVisitAction.Status.COMPLETED;
        } else if (isPartiallyCompleted()) {
            return BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
        }
        return null;
    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {

    }

    private boolean isFullyCompleted() {
        return (StringUtils.isNotBlank(vaginal_exam_date) &&
                StringUtils.isNotBlank(vaginal_exam_time) &&
                StringUtils.isNotBlank(cervix_state) &&
                StringUtils.isNotBlank(cervix_dilation) &&
                StringUtils.isNotBlank(presenting_part) &&
                StringUtils.isNotBlank(occiput_position) &&
                StringUtils.isNotBlank(moulding) &&
                StringUtils.isNotBlank(moulding_options) &&
                StringUtils.isNotBlank(station) &&
                StringUtils.isNotBlank(amniotic_fluid) &&
                StringUtils.isNotBlank(decision)
                );
    }

    private boolean isPartiallyCompleted() {
        return (StringUtils.isBlank(vaginal_exam_date) ||
                StringUtils.isBlank(vaginal_exam_time) ||
                StringUtils.isBlank(cervix_state) ||
                StringUtils.isBlank(cervix_dilation) ||
                StringUtils.isBlank(presenting_part) ||
                StringUtils.isBlank(occiput_position) ||
                StringUtils.isBlank(moulding) ||
                StringUtils.isBlank(moulding_options) ||
                StringUtils.isBlank(station) ||
                StringUtils.isBlank(amniotic_fluid) ||
                StringUtils.isBlank(decision)
                );
    }
}
