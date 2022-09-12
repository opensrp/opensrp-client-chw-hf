package org.smartregister.chw.hf.contract;

import org.smartregister.chw.ld.contract.LDProfileContract;
import org.smartregister.domain.Task;

import java.util.Set;

public interface HfLDProfileContract extends LDProfileContract {
    interface View extends LDProfileContract.View {
        void setClientTasks(Set<Task> taskList);
    }

    interface Presenter extends LDProfileContract.Presenter {
        void fetchTasks();

        void setEntityId(String entityId);

    }

    interface Interactor extends LDProfileContract.Interactor {
        void getClientTasks(String planId, String baseEntityId, HfLDProfileContract.InteractorCallBack callback);
    }


    interface InteractorCallBack {
        void setClientTasks(Set<Task> taskList);
    }
}