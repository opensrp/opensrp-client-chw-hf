package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.contract.AncMemberProfileContract;
import org.smartregister.chw.core.presenter.CoreAncMemberProfilePresenter;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.hf.utils.Constants;

public class AncMemberProfilePresenter extends CoreAncMemberProfilePresenter {


    public AncMemberProfilePresenter(AncMemberProfileContract.View view, AncMemberProfileContract.Interactor interactor, MemberObject memberObject) {
        super(view, interactor, memberObject);
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
        if(HfAncDao.isClientClosed(memberObject.getBaseEntityId())){
            getView().setPregnancyRiskLabel(Constants.Visits.TERMINATED);
        }else{
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
        if(HfAncDao.isClientClosed(memberObject.getBaseEntityId())){
            getView().setPregnancyRiskLabel(Constants.Visits.TERMINATED);
        }else{
            getView().setPregnancyRiskLabel(memberObject.getPregnancyRiskLevel());
        }

    }
}
