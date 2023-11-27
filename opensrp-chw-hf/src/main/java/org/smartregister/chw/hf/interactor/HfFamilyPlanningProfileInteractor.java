package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.interactor.CoreFamilyPlanningProfileInteractor;
import org.smartregister.chw.core.repository.ChwTaskRepository;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.contract.FamilyPlanningMemberProfileContract;
import org.smartregister.domain.Task;
import org.smartregister.repository.TaskRepository;

import java.util.Set;

public class HfFamilyPlanningProfileInteractor extends CoreFamilyPlanningProfileInteractor implements FamilyPlanningMemberProfileContract.Interactor {

    @Override
    public void getReferralTasks(String planId, String baseEntityId, FamilyPlanningMemberProfileContract.InteractorCallback callback) {
        TaskRepository taskRepository = CoreChwApplication.getInstance().getTaskRepository();
        Set<Task> taskList = ((ChwTaskRepository) taskRepository).getReferralTasksForClientByStatus(planId, baseEntityId, CoreConstants.BUSINESS_STATUS.REFERRED);

        callback.updateReferralTasks(taskList);
    }
}
