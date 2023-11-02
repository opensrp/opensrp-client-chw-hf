package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.hf.contract.HfVmmcProfileContract;
import org.smartregister.chw.vmmc.contract.VmmcProfileContract;
import org.smartregister.chw.vmmc.domain.MemberObject;
import org.smartregister.chw.vmmc.presenter.BaseVmmcProfilePresenter;
import org.smartregister.domain.Task;

import java.util.Set;

public class VmmcProfilePresenter extends BaseVmmcProfilePresenter implements HfVmmcProfileContract.Presenter, HfVmmcProfileContract.InteractorCallBack {
    public VmmcProfilePresenter(HfVmmcProfileContract.View view, HfVmmcProfileContract.Interactor interactor, MemberObject memberObject) {
        super((VmmcProfileContract.View) view, interactor, memberObject);
    }

    @Override
    public void setClientTasks(Set<Task> taskList) {
        if (getView() != null) {
            ((HfVmmcProfileContract.View) getView()).setClientTasks(taskList);
        }
    }
}
