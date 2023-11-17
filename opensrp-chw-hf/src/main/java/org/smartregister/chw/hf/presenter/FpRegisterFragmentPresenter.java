package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.fp.contract.BaseFpRegisterFragmentContract;
import org.smartregister.chw.fp.presenter.BaseFpRegisterFragmentPresenter;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;

public class FpRegisterFragmentPresenter extends BaseFpRegisterFragmentPresenter {

    public FpRegisterFragmentPresenter(BaseFpRegisterFragmentContract.View view, BaseFpRegisterFragmentContract.Model model) {
        super(view, model, FamilyPlanningConstants.CONFIGURATION.FP_REGISTRATION_CONFIGURATION);
    }
}
