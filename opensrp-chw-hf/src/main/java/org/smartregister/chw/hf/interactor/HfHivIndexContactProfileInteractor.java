package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.chw.anc.util.AppExecutors;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.interactor.CoreHivIndexContactProfileInteractor;
import org.smartregister.chw.core.repository.ChwTaskRepository;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.contract.HivIndexContactProfileContract;
import org.smartregister.chw.hf.dao.HivFollowupFeedbackDao;
import org.smartregister.chw.hf.model.HivTbFollowupFeedbackDetailsModel;
import org.smartregister.chw.hf.model.HivTbReferralTasksAndFollowupFeedbackModel;
import org.smartregister.chw.hiv.contract.BaseIndexContactProfileContract;
import org.smartregister.chw.hiv.domain.HivIndexContactObject;
import org.smartregister.domain.Task;
import org.smartregister.domain.UniqueId;
import org.smartregister.opd.contract.OpdRegisterActivityContract;
import org.smartregister.opd.pojo.OpdEventClient;
import org.smartregister.opd.pojo.RegisterParams;
import org.smartregister.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HfHivIndexContactProfileInteractor extends CoreHivIndexContactProfileInteractor implements HivIndexContactProfileContract.Interactor {

    private HfAllClientsRegisterInteractor hfAllClientsRegisterInteractor;

    public HfHivIndexContactProfileInteractor(Context context) {
        hfAllClientsRegisterInteractor = new HfAllClientsRegisterInteractor();
    }

    @Override
    public void getReferralTasks(String planId, String baseEntityId, HivIndexContactProfileContract.InteractorCallback callback) {
        List<HivTbReferralTasksAndFollowupFeedbackModel> tasksAndFollowupFeedbackModels = new ArrayList<>();
        TaskRepository taskRepository = CoreChwApplication.getInstance().getTaskRepository();
        Set<Task> taskList = ((ChwTaskRepository) taskRepository).getReferralTasksForClientByStatus(planId, baseEntityId, CoreConstants.BUSINESS_STATUS.REFERRED);

        for (Task task : taskList) {
            HivTbReferralTasksAndFollowupFeedbackModel tasksAndFollowupFeedbackModel = new HivTbReferralTasksAndFollowupFeedbackModel();
            tasksAndFollowupFeedbackModel.setTask(task);
            tasksAndFollowupFeedbackModel.setType("TASK");
            tasksAndFollowupFeedbackModels.add(tasksAndFollowupFeedbackModel);
        }

        List<HivTbFollowupFeedbackDetailsModel> followupFeedbackList = HivFollowupFeedbackDao.getHivFollowupFeedback(baseEntityId);

        for (HivTbFollowupFeedbackDetailsModel followupFeedbackDetailsModel : followupFeedbackList) {
            HivTbReferralTasksAndFollowupFeedbackModel tasksAndFollowupFeedbackModel = new HivTbReferralTasksAndFollowupFeedbackModel();
            tasksAndFollowupFeedbackModel.setFollowupFeedbackDetailsModel(followupFeedbackDetailsModel);
            tasksAndFollowupFeedbackModel.setType("FOLLOWUP_FEEDBACK");
            tasksAndFollowupFeedbackModels.add(tasksAndFollowupFeedbackModel);
        }


        callback.updateReferralTasksAndFollowupFeedback(tasksAndFollowupFeedbackModels);
    }

    @Override
    public void getNextUniqueId(final Triple<String, String, String> triple, final OpdRegisterActivityContract.InteractorCallBack callBack) {
        hfAllClientsRegisterInteractor.getNextUniqueId(triple, callBack);
    }

    @Override
    public void saveRegistration(final List<OpdEventClient> opdEventClientList, final String jsonString,
                                 final RegisterParams registerParams, final HivIndexContactObject hivIndexContactObject, final OpdRegisterActivityContract.InteractorCallBack callBack) {
        for (OpdEventClient opdEventClient : opdEventClientList) {
            if (!opdEventClient.getClient().getIdentifier("opensrp_id").contains("family")) {
                //TODO implement saving registrations
            }
        }
        hfAllClientsRegisterInteractor.saveRegistration(opdEventClientList, jsonString, registerParams, callBack);
    }

}
