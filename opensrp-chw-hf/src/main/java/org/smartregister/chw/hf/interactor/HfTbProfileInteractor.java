package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.interactor.CoreTbProfileInteractor;
import org.smartregister.chw.core.repository.ChwTaskRepository;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.contract.TbProfileContract;
import org.smartregister.chw.hf.dao.HfFollowupFeedbackDao;
import org.smartregister.chw.hf.model.ChwFollowupFeedbackDetailsModel;
import org.smartregister.chw.hf.model.HivTbReferralTasksAndFollowupFeedbackModel;
import org.smartregister.chw.tb.contract.BaseTbProfileContract;
import org.smartregister.chw.tb.domain.TbMemberObject;
import org.smartregister.domain.Task;
import org.smartregister.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HfTbProfileInteractor extends CoreTbProfileInteractor implements TbProfileContract.Interactor {

    public HfTbProfileInteractor(Context context) {
        super(context);
    }

    @Override
    public void getReferralTasks(String planId, String baseEntityId, TbProfileContract.InteractorCallback callback) {
        TaskRepository taskRepository = CoreChwApplication.getInstance().getTaskRepository();
        Set<Task> taskList = ((ChwTaskRepository) taskRepository).getReferralTasksForClientByStatus(planId, baseEntityId, CoreConstants.BUSINESS_STATUS.REFERRED);

        List<HivTbReferralTasksAndFollowupFeedbackModel> tasksAndFollowupFeedbackModels = new ArrayList<>();
        for (Task task : taskList) {
            HivTbReferralTasksAndFollowupFeedbackModel tasksAndFollowupFeedbackModel = new HivTbReferralTasksAndFollowupFeedbackModel();
            tasksAndFollowupFeedbackModel.setTask(task);
            tasksAndFollowupFeedbackModel.setType("TASK");
            tasksAndFollowupFeedbackModels.add(tasksAndFollowupFeedbackModel);
        }

        List<ChwFollowupFeedbackDetailsModel> followupFeedbackList = HfFollowupFeedbackDao.getTbFollowupFeedback(baseEntityId);

        for (ChwFollowupFeedbackDetailsModel followupFeedbackDetailsModel : followupFeedbackList) {
            HivTbReferralTasksAndFollowupFeedbackModel tasksAndFollowupFeedbackModel = new HivTbReferralTasksAndFollowupFeedbackModel();
            tasksAndFollowupFeedbackModel.setFollowupFeedbackDetailsModel(followupFeedbackDetailsModel);
            tasksAndFollowupFeedbackModel.setType("FOLLOWUP_FEEDBACK");
            tasksAndFollowupFeedbackModels.add(tasksAndFollowupFeedbackModel);
        }

        callback.updateReferralTasksAndFollowupFeedback(tasksAndFollowupFeedbackModels);
    }

    @Override
    public void updateProfileTbStatusInfo(TbMemberObject memberObject, BaseTbProfileContract.InteractorCallback callback) {
        //overriding updateProfileHivStatusInfo
    }
}
