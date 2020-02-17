package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.core.contract.CoreFamilyPlanningMemberProfileContract;
import org.smartregister.chw.core.presenter.CoreFamilyPlanningProfilePresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.hf.contract.FamilyPlanningMemberProfileContract;
import org.smartregister.domain.Task;

import java.util.Set;

public class HfFamilyPlanningMemberProfilePresenter extends CoreFamilyPlanningProfilePresenter implements
        FamilyPlanningMemberProfileContract.Presenter, FamilyPlanningMemberProfileContract.InteractorCallback {

    private FamilyPlanningMemberProfileContract.Interactor interactor;
    private String entityId;

    public HfFamilyPlanningMemberProfilePresenter(CoreFamilyPlanningMemberProfileContract.View view, CoreFamilyPlanningMemberProfileContract.Interactor interactor,
                                                  FpMemberObject fpMemberObject) {
        super(view, interactor, fpMemberObject);
        setEntityId(fpMemberObject.getBaseEntityId());
        this.interactor = (FamilyPlanningMemberProfileContract.Interactor) interactor;
    }

    @Override
    public void fetchReferralTasks() {
        interactor.getReferralTasks(CoreConstants.REFERRAL_PLAN_ID, getEntityId(), this);
    }

    @Override
    public void updateReferralTasks(Set<Task> taskList) {
        ((FamilyPlanningMemberProfileContract.View) getView()).setReferralTasks(taskList);
    }

    public String getEntityId() {
        return entityId;
    }

    private void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}
