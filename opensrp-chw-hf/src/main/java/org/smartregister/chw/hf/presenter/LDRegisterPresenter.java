package org.smartregister.chw.hf.presenter;

import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryCervixDilationMonitoring;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.LDDao;
import org.smartregister.chw.ld.contract.LDRegisterContract;
import org.smartregister.chw.ld.presenter.BaseLDRegisterPresenter;
import org.smartregister.chw.referral.util.JsonFormConstants;

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
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        viewReference.get().startFormActivity(form);
    }

    private void populateCervixDilationMonitoringForm(JSONArray fields, String baseEntityId) throws JSONException {
        JSONObject vaginalExamDate = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "vaginal_exam_date");
        JSONObject cervixDilation = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "cervix_dilation");

        if (LDDao.getLabourOnsetDate(baseEntityId) != null) {
            vaginalExamDate.put("min_date", LDDao.getLabourOnsetDate(baseEntityId));
        }

        if (LDDao.getCervixDilation(baseEntityId) != null) {
            cervixDilation.put("start_number", LDDao.getCervixDilation(baseEntityId));
        }
    }
}
