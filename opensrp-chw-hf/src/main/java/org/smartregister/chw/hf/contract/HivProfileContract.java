package org.smartregister.chw.hf.contract;

import org.smartregister.chw.core.contract.CoreHivProfileContract;
import org.smartregister.domain.Task;

import java.util.Set;

public interface HivProfileContract extends CoreHivProfileContract {
    interface View extends CoreHivProfileContract.View {
        void setReferralTasks(Set<Task> taskList);
    }

    interface Presenter extends CoreHivProfileContract.Presenter {
        void fetchReferralTasks();
    }

    interface Interactor {
        void getReferralTasks(String planId, String baseEntityId, HivProfileContract.InteractorCallback callback);
    }

    interface InteractorCallback {
        void updateReferralTasks(Set<Task> taskList);
    }
}
