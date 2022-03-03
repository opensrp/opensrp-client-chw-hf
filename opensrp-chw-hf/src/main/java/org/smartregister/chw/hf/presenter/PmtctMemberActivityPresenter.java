package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.core.contract.FamilyOtherMemberProfileExtendedContract;
import org.smartregister.chw.hf.activity.PmtctProfileActivity;
import org.smartregister.chw.hf.dao.HfFollowupFeedbackDao;
import org.smartregister.chw.hf.model.ChwFollowupFeedbackDetailsModel;
import org.smartregister.chw.hf.model.PmtctFollowupFeedbackModel;
import org.smartregister.family.contract.FamilyOtherMemberContract;

import java.util.ArrayList;
import java.util.List;

public class PmtctMemberActivityPresenter extends FamilyOtherMemberActivityPresenter {

    public PmtctMemberActivityPresenter(FamilyOtherMemberProfileExtendedContract.View view,
                                        FamilyOtherMemberContract.Model model, String viewConfigurationIdentifier,
                                        String familyBaseEntityId, String baseEntityId, String familyHead,
                                        String primaryCaregiver, String villageTown, String familyName) {
        super(view, model, viewConfigurationIdentifier, familyBaseEntityId, baseEntityId, familyHead, primaryCaregiver, villageTown, familyName);
    }


    public void updateFollowupFeedback() {
        List<ChwFollowupFeedbackDetailsModel> followupFeedbackList = HfFollowupFeedbackDao.getPmtctFollowupFeedback(baseEntityId);

        List<PmtctFollowupFeedbackModel> pmtctFollowupFeedback = new ArrayList<>();
        for (ChwFollowupFeedbackDetailsModel followupFeedbackDetailsModel : followupFeedbackList) {
            PmtctFollowupFeedbackModel followupFeedbackModel = new PmtctFollowupFeedbackModel();
            followupFeedbackModel.setFollowupFeedbackDetailsModel(followupFeedbackDetailsModel);
            followupFeedbackModel.setType("FOLLOWUP_FEEDBACK");
            pmtctFollowupFeedback.add(followupFeedbackModel);
        }

        ((PmtctProfileActivity) getView()).setFollowupFeedback(pmtctFollowupFeedback);
    }


}
