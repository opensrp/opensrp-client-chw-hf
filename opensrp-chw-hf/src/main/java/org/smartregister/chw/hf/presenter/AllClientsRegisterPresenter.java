package org.smartregister.chw.hf.presenter;

import org.smartregister.opd.contract.OpdRegisterActivityContract;
import org.smartregister.opd.presenter.BaseOpdRegisterActivityPresenter;

public class AllClientsRegisterPresenter extends BaseOpdRegisterActivityPresenter {

    public AllClientsRegisterPresenter(OpdRegisterActivityContract.View view, OpdRegisterActivityContract.Model model) {
        super(view, model);
    }

    @Override
    public void saveForm(String jsonString, boolean isEditMode) {

    }
}
