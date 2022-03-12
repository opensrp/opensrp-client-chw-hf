package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.pmtct.contract.PmtctProfileContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.presenter.BasePmtctProfilePresenter;

public class HeiProfilePresenter extends BasePmtctProfilePresenter {
    public HeiProfilePresenter(PmtctProfileContract.View view, PmtctProfileContract.Interactor interactor, MemberObject memberObject) {
        super(view, interactor, memberObject);
    }
}
