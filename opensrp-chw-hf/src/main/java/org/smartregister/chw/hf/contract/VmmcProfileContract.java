package org.smartregister.chw.hf.contract;

import org.smartregister.domain.Task;

import java.util.Set;

public interface VmmcProfileContract {
    interface InteractorCallback {
        void updateReferralTasks(Set<Task> taskList);
    }
}
