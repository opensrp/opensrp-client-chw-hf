package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.pmtct.contract.PmtctRegisterFragmentContract;
import org.smartregister.chw.pmtct.presenter.BasePmtctRegisterFragmentPresenter;

public class PmtctRegisterFragmentPresenter extends BasePmtctRegisterFragmentPresenter {
    public PmtctRegisterFragmentPresenter(PmtctRegisterFragmentContract.View view, PmtctRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }
    @Override
    public String getMainCondition() {
        return " ec_family_member.date_removed is null";
    }
}
