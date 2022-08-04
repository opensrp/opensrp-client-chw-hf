package org.smartregister.chw.hf.presenter;

import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryCervixDilationMonitoring;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryModeOfDelivery;
import static org.smartregister.family.util.Constants.JSON_FORM_KEY.OPTIONS;

import android.os.Build;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.dao.LDDao;
import org.smartregister.chw.ld.contract.LDRegisterContract;
import org.smartregister.chw.ld.presenter.BaseLDRegisterPresenter;
import org.smartregister.chw.referral.util.JsonFormConstants;
import org.smartregister.util.JsonFormUtils;

public class LDRegisterPresenter extends BaseLDRegisterPresenter {
    public LDRegisterPresenter(LDRegisterContract.View view, LDRegisterContract.Model model, LDRegisterContract.Interactor interactor) {
        super(view, model, interactor);
    }


    @Override
    public void startForm(String formName, String entityId, String metadata, String currentLocationId) throws Exception {
        if (StringUtils.isBlank(entityId)) {
            return;
        }
        JSONObject form = model.getFormAsJson(formName, entityId, currentLocationId);


        if (form != null && formName.contains(getLabourAndDeliveryCervixDilationMonitoring())) {
            try {
                JSONArray fields = form.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                populateCervixDilationMonitoringForm(fields, entityId);

                if (LDDao.getVaginalExaminationDate(entityId) != null) {
                    form.getJSONObject("global").put("last_vaginal_exam_date", LDDao.getVaginalExaminationDate(entityId));
                }

                if (LDDao.getVaginalExaminationTime(entityId) != null) {
                    form.getJSONObject("global").put("last_vaginal_exam_time", LDDao.getVaginalExaminationTime(entityId));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (form != null && formName.contains(getLabourAndDeliveryModeOfDelivery()) && (org.smartregister.chw.ld.dao.LDDao.getReasonsForAdmission(entityId) != null && org.smartregister.chw.ld.dao.LDDao.getReasonsForAdmission(entityId).equalsIgnoreCase("elective_cesarean_section"))) {
            try {
                JSONArray fields = form.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                JSONObject modeOfDelivery = JsonFormUtils.getFieldJSONObject(fields, "mode_of_delivery");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && modeOfDelivery != null) {
                    modeOfDelivery.getJSONArray(OPTIONS).remove(3);
                    modeOfDelivery.getJSONArray(OPTIONS).remove(2);
                    modeOfDelivery.getJSONArray(OPTIONS).remove(0);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        viewReference.get().startFormActivity(form);
    }

    private void populateCervixDilationMonitoringForm(JSONArray fields, String baseEntityId) throws JSONException {
        JSONObject vaginalExamDate = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "vaginal_exam_date");
        JSONObject cervixDilation = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "cervix_dilation");

        if (LDDao.getVaginalExaminationDate(baseEntityId) != null) {
            vaginalExamDate.put("min_date", LDDao.getVaginalExaminationDate(baseEntityId));
        }

        if (LDDao.getCervixDilation(baseEntityId) != null) {
            cervixDilation.put("start_number", LDDao.getCervixDilation(baseEntityId));
        }
    }
}
