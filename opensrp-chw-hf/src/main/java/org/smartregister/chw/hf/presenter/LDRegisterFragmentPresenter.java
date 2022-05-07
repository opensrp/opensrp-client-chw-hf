package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.ld.contract.LDRegisterFragmentContract;
import org.smartregister.chw.ld.presenter.BaseLDRegisterFragmentPresenter;
import org.smartregister.chw.ld.util.Constants;

public class LDRegisterFragmentPresenter extends BaseLDRegisterFragmentPresenter {
    public LDRegisterFragmentPresenter(LDRegisterFragmentContract.View view, LDRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getMainCondition() {
        return "" + Constants.TABLES.LD_CONFIRMATION + "." + "labour_confirmation = 'true' AND "
                + Constants.TABLES.LD_CONFIRMATION + "." + "is_closed is 0";
    }
}
