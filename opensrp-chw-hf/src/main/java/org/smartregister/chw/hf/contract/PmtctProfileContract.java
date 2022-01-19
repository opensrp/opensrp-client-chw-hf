package org.smartregister.chw.hf.contract;

import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.chw.core.contract.CorePmtctProfileContract;
import org.smartregister.chw.hf.model.HivTbReferralTasksAndFollowupFeedbackModel;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.opd.contract.OpdRegisterActivityContract;
import org.smartregister.opd.pojo.OpdEventClient;
import org.smartregister.opd.pojo.RegisterParams;

import java.util.List;

public interface PmtctProfileContract {
    interface View extends CorePmtctProfileContract.View {
        void setReferralTasksAndFollowupFeedback(List<HivTbReferralTasksAndFollowupFeedbackModel> tasksAndFollowupFeedbackModels);
    }
    interface Presenter extends CorePmtctProfileContract.Presenter {
        void fetchReferralTasks();
    }

    interface Interactor {
        void getReferralTasks(String planId, String baseEntityId, PmtctProfileContract.InteractorCallback callback);

        void getNextUniqueId(Triple<String, String, String> triple, OpdRegisterActivityContract.InteractorCallBack callBack);

        void saveRegistration(List<OpdEventClient> opdEventClientList, String jsonString, RegisterParams registerParams, MemberObject hivMemberObject, OpdRegisterActivityContract.InteractorCallBack callBack);
    }

    interface InteractorCallback {
        void updateReferralTasksAndFollowupFeedback(List<HivTbReferralTasksAndFollowupFeedbackModel> tasksAndFollowupFeedbackModels);
    }
}
