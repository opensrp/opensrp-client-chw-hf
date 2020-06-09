package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.core.contract.CoreTbProfileContract;
import org.smartregister.chw.core.presenter.CoreTbProfilePresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.contract.TbProfileContract;
import org.smartregister.chw.tb.domain.TbMemberObject;
import org.smartregister.domain.Task;

import java.util.Set;

public class TbProfilePresenter extends CoreTbProfilePresenter
        implements TbProfileContract.Presenter, TbProfileContract.InteractorCallback {

    private TbMemberObject tbMemberObject;
    private TbProfileContract.Interactor interactor;

    public TbProfilePresenter(CoreTbProfileContract.View view, CoreTbProfileContract.Interactor interactor,
                              TbMemberObject tbMemberObject) {
        super(view, interactor, tbMemberObject);
        this.tbMemberObject = tbMemberObject;
        this.interactor = (TbProfileContract.Interactor) interactor;
    }

    @Override
    public void fetchReferralTasks() {
        interactor.getReferralTasks(CoreConstants.REFERRAL_PLAN_ID, tbMemberObject.getBaseEntityId(), this);
    }

    @Override
    public void updateReferralTasks(Set<Task> taskList) {
        ((TbProfileContract.View) getView()).setReferralTasks(taskList);
    }
}
