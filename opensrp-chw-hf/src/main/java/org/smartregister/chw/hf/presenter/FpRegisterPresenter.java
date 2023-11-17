package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.fp.contract.BaseFpRegisterContract;
import org.smartregister.chw.fp.presenter.BaseFpRegisterPresenter;

public class FpRegisterPresenter extends BaseFpRegisterPresenter {
    private final BaseFpRegisterContract.Interactor interactor;

    public FpRegisterPresenter(BaseFpRegisterContract.View view, BaseFpRegisterContract.Model model, BaseFpRegisterContract.Interactor interactor) {
        super(view, model, interactor);
        this.interactor = interactor;
    }

    @Override
    public void saveForm(String jsonString) {
        interactor.saveRegistration(jsonString, this);
    }
}
