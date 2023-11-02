package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.vmmc.contract.VmmcRegisterContract;
import org.smartregister.chw.vmmc.presenter.BaseVmmcRegisterPresenter;

public class VmmcRegisterPresenter extends BaseVmmcRegisterPresenter {
    public VmmcRegisterPresenter(VmmcRegisterContract.View view, VmmcRegisterContract.Model model, VmmcRegisterContract.Interactor interactor) {
        super(view, model, interactor);
    }

    @Override
    public void startForm(String formName, String entityId, String metadata, String currentLocationId) throws Exception {
        // implement vmmc function here
    }
}
