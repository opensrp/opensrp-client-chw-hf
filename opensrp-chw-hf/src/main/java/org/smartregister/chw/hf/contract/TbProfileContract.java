package org.smartregister.chw.hf.contract;

import org.smartregister.chw.core.contract.CoreTbProfileContract;
import org.smartregister.chw.hf.model.HivTbReferralTasksAndFollowupFeedbackModel;

import java.util.List;

public interface TbProfileContract extends CoreTbProfileContract {
    interface View extends CoreTbProfileContract.View {
        void setReferralTasksAndFollowupFeedback(List<HivTbReferralTasksAndFollowupFeedbackModel> tasksAndFollowupFeedbackModels);
    }

    interface Presenter extends CoreTbProfileContract.Presenter {
        void fetchReferralTasks();
    }

    interface Interactor {
        void getReferralTasks(String planId, String baseEntityId, TbProfileContract.InteractorCallback callback);
    }

    interface InteractorCallback {
        void updateReferralTasksAndFollowupFeedback(List<HivTbReferralTasksAndFollowupFeedbackModel> tasksAndFollowupFeedbackModels);
    }
}
