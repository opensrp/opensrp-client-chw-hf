package org.smartregister.chw.hf.presenter;


import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.contract.CorePncMemberProfileContract;
import org.smartregister.chw.core.presenter.CorePncMemberProfilePresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.contract.PncMemberProfileContract;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;

import java.util.Set;

public class PncMemberProfilePresenter extends CorePncMemberProfilePresenter implements
        PncMemberProfileContract.Presenter, PncMemberProfileContract.InteractorCallback, FamilyProfileContract.InteractorCallBack {

    private PncMemberProfileContract.Interactor interactor;
    private String entityId;

    public PncMemberProfilePresenter(CorePncMemberProfileContract.View view, CorePncMemberProfileContract.Interactor interactor,
                                     MemberObject memberObject) {
        super(view, interactor, memberObject);
        setEntityId(memberObject.getBaseEntityId());
        this.interactor = (PncMemberProfileContract.Interactor) interactor;
    }

    @Override
    public void fetchReferralTasks() {
        interactor.getReferralTasks(CoreConstants.REFERRAL_PLAN_ID, getEntityId(), this);
    }

    @Override
    public void updateReferralTasks(Set<Task> taskList) {
        ((PncMemberProfileContract.View) getView()).setReferralTasks(taskList);
    }

    public String getEntityId() {
        return entityId;
    }

    private void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    @Override
    public void startFormForEdit(CommonPersonObjectClient commonPersonObjectClient) {
        //override
    }

    @Override
    public void refreshProfileTopSection(CommonPersonObjectClient commonPersonObjectClient) {
        //override
    }

    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String s) {
        //override
    }

    @Override
    public void onNoUniqueId() {
        //override
    }

    @Override
    public void onRegistrationSaved(boolean b, boolean b1, FamilyEventClient familyEventClient) {
        //override
    }
}


