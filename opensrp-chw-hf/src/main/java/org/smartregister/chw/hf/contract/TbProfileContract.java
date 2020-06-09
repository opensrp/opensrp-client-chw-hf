package org.smartregister.chw.hf.contract;

import org.smartregister.chw.core.contract.CoreTbProfileContract;
import org.smartregister.domain.Task;

import java.util.Set;

public interface TbProfileContract extends CoreTbProfileContract {
    interface View extends CoreTbProfileContract.View {
        void setReferralTasks(Set<Task> taskList);
    }

    interface Presenter extends CoreTbProfileContract.Presenter {
        void fetchReferralTasks();
    }

    interface Interactor {
        void getReferralTasks(String planId, String baseEntityId, TbProfileContract.InteractorCallback callback);
    }

    interface InteractorCallback {
        void updateReferralTasks(Set<Task> taskList);
    }
}
