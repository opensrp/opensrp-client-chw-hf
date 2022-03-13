package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.pmtct.contract.PmtctRegisterContract;
import org.smartregister.chw.pmtct.presenter.BasePmtctRegisterPresenter;

public class HeiRegisterPresenter extends BasePmtctRegisterPresenter {
    public HeiRegisterPresenter(PmtctRegisterContract.View view, PmtctRegisterContract.Model model, PmtctRegisterContract.Interactor interactor) {
        super(view, model, interactor);
    }
}
