package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.anc.contract.BaseAncRegisterFragmentContract;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.presenter.AncRegisterFragmentPresenter;
import org.smartregister.chw.core.utils.CoreConstants;

public class HfAncRegisterFragmentPresenter extends AncRegisterFragmentPresenter {
    public HfAncRegisterFragmentPresenter(BaseAncRegisterFragmentContract.View view, BaseAncRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getMainCondition() {
        return " " + CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.DATE_REMOVED + " is null " +
                "AND " + CoreConstants.TABLE_NAME.ANC_MEMBER + "." + DBConstants.KEY.IS_CLOSED + " is 0 ";
    }

}
