package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.core.contract.CoreHivProfileContract;
import org.smartregister.chw.core.presenter.CoreHivProfilePresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.contract.HivProfileContract;
import org.smartregister.chw.hiv.domain.HivMemberObject;
import org.smartregister.domain.Task;

import java.util.Set;

public class HivProfilePresenter extends CoreHivProfilePresenter
        implements HivProfileContract.Presenter, HivProfileContract.InteractorCallback {

    private HivMemberObject hivMemberObject;
    private HivProfileContract.Interactor interactor;

    public HivProfilePresenter(CoreHivProfileContract.View view, CoreHivProfileContract.Interactor interactor,
                               HivMemberObject hivMemberObject) {
        super(view, interactor, hivMemberObject);
        this.hivMemberObject = hivMemberObject;
        this.interactor = (HivProfileContract.Interactor) interactor;
    }

    @Override
    public void fetchReferralTasks() {
        interactor.getReferralTasks(CoreConstants.REFERRAL_PLAN_ID, hivMemberObject.getBaseEntityId(), this);
    }

    @Override
    public void updateReferralTasks(Set<Task> taskList) {
        ((HivProfileContract.View) getView()).setReferralTasks(taskList);
    }
}