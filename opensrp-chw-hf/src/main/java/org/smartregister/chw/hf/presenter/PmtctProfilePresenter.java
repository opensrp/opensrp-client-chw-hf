package org.smartregister.chw.hf.presenter;

import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.chw.core.contract.CorePmtctProfileContract;
import org.smartregister.chw.core.presenter.CorePmtctMemberProfilePresenter;
import org.smartregister.chw.hf.activity.PmtctProfileActivity;
import org.smartregister.chw.hf.dao.HfFollowupFeedbackDao;
import org.smartregister.chw.hf.interactor.PmtctProfileInteractor;
import org.smartregister.chw.hf.model.ChwFollowupFeedbackDetailsModel;
import org.smartregister.chw.hf.model.PmtctFollowupFeedbackModel;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.repository.AllSharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class PmtctProfilePresenter extends CorePmtctMemberProfilePresenter implements FamilyProfileContract.InteractorCallBack {
    private PmtctProfileInteractor createPartnerFollowupReferralEvent;
    public PmtctProfilePresenter(CorePmtctProfileContract.View view, CorePmtctProfileContract.Interactor interactor, MemberObject memberObject) {
        super(view, interactor, memberObject);
        this.createPartnerFollowupReferralEvent = (PmtctProfileInteractor) interactor;
    }

    public void updateFollowupFeedback(String baseEntityId) {
        List<ChwFollowupFeedbackDetailsModel> followupFeedbackList = HfFollowupFeedbackDao.getPmtctFollowupFeedback(baseEntityId);

        List<PmtctFollowupFeedbackModel> pmtctFollowupFeedback = new ArrayList<>();
        if (followupFeedbackList != null) {
            for (ChwFollowupFeedbackDetailsModel followupFeedbackDetailsModel : followupFeedbackList) {
                PmtctFollowupFeedbackModel followupFeedbackModel = new PmtctFollowupFeedbackModel();
                followupFeedbackModel.setFollowupFeedbackDetailsModel(followupFeedbackDetailsModel);
                followupFeedbackModel.setType("FOLLOWUP_FEEDBACK");
                pmtctFollowupFeedback.add(followupFeedbackModel);
            }

            ((PmtctProfileActivity) getView()).setFollowupFeedback(pmtctFollowupFeedback);
        }
    }

    @Override
    public void startFormForEdit(CommonPersonObjectClient commonPersonObjectClient) {
        //implement
    }

    @Override
    public void refreshProfileTopSection(CommonPersonObjectClient commonPersonObjectClient) {
        //implement
    }

    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String s) {
        //implement
    }

    @Override
    public void onNoUniqueId() {
        //implement
    }

    @Override
    public void onRegistrationSaved(boolean b, boolean b1, FamilyEventClient familyEventClient) {
        //implement
    }

    public void createPmtctCommunityFollowupReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) throws Exception {
        createPartnerFollowupReferralEvent.createPmtctCommunityFollowupReferralEvent(allSharedPreferences, jsonString, entityID);
    }
}
