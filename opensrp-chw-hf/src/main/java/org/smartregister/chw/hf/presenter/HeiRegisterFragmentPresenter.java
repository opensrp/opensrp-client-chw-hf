package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.pmtct.contract.PmtctRegisterFragmentContract;
import org.smartregister.chw.pmtct.presenter.BasePmtctRegisterFragmentPresenter;

public class HeiRegisterFragmentPresenter extends BasePmtctRegisterFragmentPresenter {
    public HeiRegisterFragmentPresenter(PmtctRegisterFragmentContract.View view, PmtctRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getMainTable() {
        return CoreConstants.TABLE_NAME.HEI;
    }

    @Override
    public String getMainCondition() {
        return CoreConstants.TABLE_NAME.HEI + "." + "is_closed is 0";
    }

}
