package org.smartregister.chw.hf.contract;

import org.smartregister.domain.Task;

import java.util.Set;

public interface MalariaProfileContract {
    interface InteractorCallback {
        void updateReferralTasks(Set<Task> taskList);
    }
}
