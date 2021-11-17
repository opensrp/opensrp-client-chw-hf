package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.chw.core.interactor.CoreHivIndexContactProfileInteractor;
import org.smartregister.chw.hf.contract.HivIndexContactProfileContract;
import org.smartregister.chw.hf.dao.HivIndexFollowupFeedbackDao;
import org.smartregister.chw.hf.model.HivIndexFollowupFeedbackDetailsModel;
import org.smartregister.chw.hiv.domain.HivIndexContactObject;
import org.smartregister.opd.contract.OpdRegisterActivityContract;
import org.smartregister.opd.pojo.OpdEventClient;
import org.smartregister.opd.pojo.RegisterParams;

import java.util.List;

public class HfHivIndexContactProfileInteractor extends CoreHivIndexContactProfileInteractor implements HivIndexContactProfileContract.Interactor {

    private HfAllClientsRegisterInteractor hfAllClientsRegisterInteractor;

    public HfHivIndexContactProfileInteractor(Context context) {
        hfAllClientsRegisterInteractor = new HfAllClientsRegisterInteractor();
    }

    @Override
    public void getReferralTasks(String planId, String baseEntityId, HivIndexContactProfileContract.InteractorCallback callback) {
        List<HivIndexFollowupFeedbackDetailsModel> indexFollowupFeedbackModel;
         indexFollowupFeedbackModel = HivIndexFollowupFeedbackDao.getHivIndexFollowupFeedback(baseEntityId);
        callback.getReferralAndFollowupFeedback(indexFollowupFeedbackModel);
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
