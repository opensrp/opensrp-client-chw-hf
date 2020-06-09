package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.interactor.CoreHivProfileInteractor;
import org.smartregister.chw.core.repository.ChwTaskRepository;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.contract.HivProfileContract;
import org.smartregister.chw.hiv.contract.BaseHivProfileContract;
import org.smartregister.chw.hiv.domain.HivMemberObject;
import org.smartregister.domain.Task;
import org.smartregister.repository.TaskRepository;

import java.util.Set;

public class HfHivProfileInteractor extends CoreHivProfileInteractor implements HivProfileContract.Interactor {
    private Context context;

    public HfHivProfileInteractor(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void getReferralTasks(String planId, String baseEntityId, HivProfileContract.InteractorCallback callback) {
        TaskRepository taskRepository = CoreChwApplication.getInstance().getTaskRepository();
        Set<Task> taskList = ((ChwTaskRepository) taskRepository).getReferralTasksForClientByStatus(planId, baseEntityId, CoreConstants.BUSINESS_STATUS.REFERRED);
        callback.updateReferralTasks(taskList);
    }

    @Override
    public void updateProfileHivStatusInfo(HivMemberObject memberObject, BaseHivProfileContract.InteractorCallback callback) {
        //overriding updateProfileHivStatusInfo
    }
}
