package org.smartregister.chw.hf.contract;

import org.smartregister.chw.ld.contract.LDProfileContract;
import org.smartregister.chw.vmmc.contract.VmmcProfileContract;
import org.smartregister.domain.Task;

import java.util.Set;

public interface HfVmmcProfileContract extends VmmcProfileContract{

    interface View extends LDProfileContract.View {
        void setClientTasks(Set<Task> taskList);
    }

    interface Interactor extends VmmcProfileContract.Interactor {
        void getClientTasks(String planId, String baseEntityId, HfVmmcProfileContract.InteractorCallBack callback);
    }

    interface InteractorCallBack {
        void setClientTasks(Set<Task> taskList);
    }
}
