package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.interactor.CoreTbProfileInteractor;
import org.smartregister.chw.core.repository.ChwTaskRepository;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.contract.TbProfileContract;
import org.smartregister.chw.tb.contract.BaseTbProfileContract;
import org.smartregister.chw.tb.domain.TbMemberObject;
import org.smartregister.domain.Task;
import org.smartregister.repository.TaskRepository;

import java.util.Set;

public class HfTbProfileInteractor extends CoreTbProfileInteractor implements TbProfileContract.Interactor {
    private Context context;

    public HfTbProfileInteractor(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void getReferralTasks(String planId, String baseEntityId, TbProfileContract.InteractorCallback callback) {
        TaskRepository taskRepository = CoreChwApplication.getInstance().getTaskRepository();
        Set<Task> taskList = ((ChwTaskRepository) taskRepository).getReferralTasksForClientByStatus(planId, baseEntityId, CoreConstants.BUSINESS_STATUS.REFERRED);
        callback.updateReferralTasks(taskList);
    }

    @Override
    public void updateProfileTbStatusInfo(TbMemberObject memberObject, BaseTbProfileContract.InteractorCallback callback) {
        //overriding updateProfileHivStatusInfo
    }
}
