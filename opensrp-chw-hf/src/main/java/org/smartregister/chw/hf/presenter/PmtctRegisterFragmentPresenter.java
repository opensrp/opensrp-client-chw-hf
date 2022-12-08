package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.pmtct.contract.PmtctRegisterFragmentContract;
import org.smartregister.chw.pmtct.presenter.BasePmtctRegisterFragmentPresenter;
import org.smartregister.chw.pmtct.util.Constants;

public class PmtctRegisterFragmentPresenter extends BasePmtctRegisterFragmentPresenter {
    public PmtctRegisterFragmentPresenter(PmtctRegisterFragmentContract.View view, PmtctRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }
    @Override
    public String getMainCondition() {
        return "" + Constants.TABLES.PMTCT_REGISTRATION + "." + "hiv_status = 'positive' "+
                " AND " + Constants.TABLES.PMTCT_REGISTRATION + "." + "is_closed is 0 AND ec_family_member.is_closed is 0";
    }
}
