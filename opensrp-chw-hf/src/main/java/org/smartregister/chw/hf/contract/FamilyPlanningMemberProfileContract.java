package org.smartregister.chw.hf.contract;

import org.smartregister.chw.core.contract.CoreFamilyPlanningMemberProfileContract;
import org.smartregister.domain.Task;

import java.util.Set;

public interface FamilyPlanningMemberProfileContract {

    interface View extends CoreFamilyPlanningMemberProfileContract.View {
        void setReferralTasks(Set<Task> taskList);
    }

    interface Presenter extends CoreFamilyPlanningMemberProfileContract.Presenter {
        void fetchReferralTasks();
    }

    interface Interactor {
        void getReferralTasks(String planId, String baseEntityId, FamilyPlanningMemberProfileContract.InteractorCallback callback);
    }

    interface InteractorCallback {
        void updateReferralTasks(Set<Task> taskList);
    }
}