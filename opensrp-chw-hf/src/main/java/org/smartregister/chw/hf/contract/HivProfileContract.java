package org.smartregister.chw.hf.contract;

import org.smartregister.chw.core.contract.CoreHivProfileContract;
import org.smartregister.chw.hf.model.HivTbReferralTasksAndFollowupFeedbackModel;

import java.util.List;

public interface HivProfileContract extends CoreHivProfileContract {
    interface View extends CoreHivProfileContract.View {
        void setReferralTasksAndFollowupFeedback(List<HivTbReferralTasksAndFollowupFeedbackModel> tasksAndFollowupFeedbackModels);
    }

    interface Presenter extends CoreHivProfileContract.Presenter {
        void fetchReferralTasks();
    }

    interface Interactor {
        void getReferralTasks(String planId, String baseEntityId, HivProfileContract.InteractorCallback callback);
    }

    interface InteractorCallback {
        void updateReferralTasksAndFollowupFeedback(List<HivTbReferralTasksAndFollowupFeedbackModel> tasksAndFollowupFeedbackModels);
    }
}
