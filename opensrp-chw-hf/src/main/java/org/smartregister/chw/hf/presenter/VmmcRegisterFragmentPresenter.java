package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.core.presenter.CoreVmmcRegisterFragmentPresenter;
import org.smartregister.chw.vmmc.contract.VmmcRegisterFragmentContract;

public class VmmcRegisterFragmentPresenter extends CoreVmmcRegisterFragmentPresenter {
    public VmmcRegisterFragmentPresenter(VmmcRegisterFragmentContract.View view,
                                         VmmcRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }
}
