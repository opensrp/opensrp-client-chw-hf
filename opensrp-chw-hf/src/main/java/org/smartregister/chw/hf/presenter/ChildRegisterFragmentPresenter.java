package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.core.contract.CoreChildRegisterFragmentContract;
import org.smartregister.chw.core.presenter.CoreChildRegisterFragmentPresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.utils.HfReferralUtils;

public class ChildRegisterFragmentPresenter extends CoreChildRegisterFragmentPresenter {

    public ChildRegisterFragmentPresenter(CoreChildRegisterFragmentContract.View view, CoreChildRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getDueCondition() {
        return " AND " + CoreConstants.TABLE_NAME.CHILD + ".base_entity_id in ("
                + HfReferralUtils.getReferralDueFilter(CoreConstants.TABLE_NAME.CHILD, CoreConstants.TASKS_FOCUS.SICK_CHILD)
                + ")";
    }
}
