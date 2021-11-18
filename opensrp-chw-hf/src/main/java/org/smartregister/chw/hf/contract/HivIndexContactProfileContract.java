package org.smartregister.chw.hf.contract;

import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.chw.core.contract.CoreIndexContactProfileContract;
import org.smartregister.chw.hf.model.HivIndexFollowupFeedbackDetailsModel;
import org.smartregister.chw.hiv.domain.HivIndexContactObject;
import org.smartregister.opd.contract.OpdRegisterActivityContract;
import org.smartregister.opd.pojo.OpdEventClient;
import org.smartregister.opd.pojo.RegisterParams;

import java.util.List;

public interface HivIndexContactProfileContract extends CoreIndexContactProfileContract {
    interface View extends CoreIndexContactProfileContract.View {
        void setReferralAndFollowupFeedback(List<HivIndexFollowupFeedbackDetailsModel> followupFeedbackDetailsModel);
    }

    interface Presenter extends CoreIndexContactProfileContract.Presenter {
        void fetchReferralTasks();
    }

    interface Interactor extends CoreIndexContactProfileContract.Interactor {
        void getReferralTasks(String planId, String baseEntityId, HivIndexContactProfileContract.InteractorCallback callback);

        void getNextUniqueId(Triple<String, String, String> triple, OpdRegisterActivityContract.InteractorCallBack callBack);

        void saveRegistration(List<OpdEventClient> opdEventClientList, String jsonString, RegisterParams registerParams, HivIndexContactObject hivIndexContactObject, OpdRegisterActivityContract.InteractorCallBack callBack);
    }

    interface InteractorCallback {
        void getReferralAndFollowupFeedback(List<HivIndexFollowupFeedbackDetailsModel> followupFeedbackDetailsModel);
    }
}
