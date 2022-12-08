package org.smartregister.chw.hf.presenter;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.chw.hf.activity.PmtctRegisterActivity;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.contract.PmtctRegisterContract;
import org.smartregister.chw.pmtct.presenter.BasePmtctRegisterPresenter;

public class PmtctRegisterPresenter extends BasePmtctRegisterPresenter {
    public PmtctRegisterPresenter(PmtctRegisterContract.View view, PmtctRegisterContract.Model model, PmtctRegisterContract.Interactor interactor) {
        super(view, model, interactor);
    }

    @Override
    public void startForm(String formName, String entityId, String metadata, String currentLocationId) throws Exception {
        if (StringUtils.isBlank(entityId)) {
            return;
        }

        JSONObject form = model.getFormAsJson(formName, entityId, currentLocationId);

        if (formName.equals(Constants.JsonForm.getPmtctRegistrationForClientsKnownOnArtForm())) {
            String ctcNumber = ((PmtctRegisterActivity) getView()).getCtcNumber();
            if (ctcNumber == null) {
                ctcNumber = HfAncDao.getClientCtcNumber(entityId);
            }

            if (ctcNumber == null) {
                return;
            }
            form.getJSONObject("global").put("ctc_number", ctcNumber);
        }
        getView().startFormActivity(form);
    }
}
