package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.contract.HfLDProfileContract;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.presenter.BaseLDProfilePresenter;
import org.smartregister.domain.Task;

import java.util.Set;

public class LDProfilePresenter extends BaseLDProfilePresenter implements HfLDProfileContract.Presenter, HfLDProfileContract.InteractorCallBack {
    public LDProfilePresenter(HfLDProfileContract.View view, HfLDProfileContract.Interactor interactor, MemberObject memberObject) {
        super(view, interactor, memberObject);
    }

    @Override
    public void setClientTasks(Set<Task> taskList) {
        if (getView() != null) {
            ((HfLDProfileContract.View) getView()).setClientTasks(taskList);
        }
    }

    @Override
    public void fetchTasks() {
        ((HfLDProfileContract.Interactor) interactor).getClientTasks(CoreConstants.REFERRAL_PLAN_ID, memberObject.getBaseEntityId(), this);
    }

    @Override
    public void setEntityId(String entityId) {
        memberObject.getBaseEntityId();
    }
}
