package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.repository.ChwTaskRepository;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.contract.HfLDProfileContract;
import org.smartregister.chw.ld.interactor.BaseLDProfileInteractor;
import org.smartregister.domain.Task;
import org.smartregister.repository.TaskRepository;

import java.util.Set;

public class LDProfileInteractor extends BaseLDProfileInteractor implements HfLDProfileContract.Interactor {
    @Override
    public void getClientTasks(String planId, String baseEntityId, HfLDProfileContract.InteractorCallBack callback) {
        TaskRepository taskRepository = CoreChwApplication.getInstance().getTaskRepository();
        Set<Task> taskList = ((ChwTaskRepository) taskRepository).getReferralTasksForClientByStatus(planId, baseEntityId, CoreConstants.BUSINESS_STATUS.REFERRED);
        callback.setClientTasks(taskList);
    }
}
