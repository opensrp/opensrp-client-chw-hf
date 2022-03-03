package org.smartregister.chw.hf.presenter;

import android.content.Context;

import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONObject;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.contract.AncMemberProfileContract;
import org.smartregister.chw.core.presenter.CoreAncMemberProfilePresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.hf.interactor.AncMemberProfileInteractor;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.repository.AllSharedPreferences;

import timber.log.Timber;

public class AncMemberProfilePresenter extends CoreAncMemberProfilePresenter {
    private AncMemberProfileInteractor ancMemberProfileInteractor;

    public AncMemberProfilePresenter(AncMemberProfileContract.View view, AncMemberProfileContract.Interactor interactor, MemberObject memberObject) {
        super(view, interactor, memberObject);
        ancMemberProfileInteractor = (AncMemberProfileInteractor) interactor;
    }

    @Override
    public void refreshProfileTopSection(MemberObject memberObject) {
        String entityType = memberObject.getBaseEntityId();
        getView().setMemberName(memberObject.getMemberName());
        getView().setMemberGA(String.valueOf(memberObject.getGestationAge()));
        getView().setMemberAddress(memberObject.getAddress());
        getView().setMemberChwMemberId(memberObject.getChwMemberId());
        getView().setProfileImage(memberObject.getBaseEntityId(), entityType);
        getView().setMemberGravida(memberObject.getGravida());
        if (HfAncDao.isClientClosed(memberObject.getBaseEntityId())) {
            getView().setPregnancyRiskLabel(Constants.Visits.TERMINATED);
        } else {
            getView().setPregnancyRiskLabel(memberObject.getPregnancyRiskLevel());
        }
    }

    @Override
    public void setPregnancyRiskTransportProfileDetails(MemberObject memberObject) {
        String entityType = memberObject.getBaseEntityId();
        getView().setMemberName(memberObject.getMemberName());
        getView().setMemberGA(String.valueOf(memberObject.getGestationAge()));
        getView().setMemberAddress(memberObject.getAddress());
        getView().setMemberChwMemberId(memberObject.getChwMemberId());
        getView().setProfileImage(memberObject.getBaseEntityId(), entityType);
        getView().setMemberGravida(memberObject.getGravida());
        if (HfAncDao.isClientClosed(memberObject.getBaseEntityId())) {
            getView().setPregnancyRiskLabel(Constants.Visits.TERMINATED);
        } else {
            getView().setPregnancyRiskLabel(memberObject.getPregnancyRiskLevel());
        }

    }

    public void startPartnerFollowupReferralForm(MemberObject memberObject) {
        try {
            JSONObject formJsonObject = (new FormUtils()).getFormJsonFromRepositoryOrAssets((Context) getView(), CoreConstants.JSON_FORM.getAncPartnerCommunityFollowupReferral());
            getView().startFormActivity(formJsonObject);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public void createPartnerFollowupReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) throws Exception {
        ancMemberProfileInteractor.createPartnerFollowupReferralEvent(allSharedPreferences, jsonString, entityID);
    }
}
