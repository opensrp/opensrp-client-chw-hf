package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.core.contract.CoreFamilyPlanningMemberProfileContract;
import org.smartregister.chw.core.presenter.CoreFamilyPlanningProfilePresenter;
import org.smartregister.chw.fp.domain.FpMemberObject;

public class HfFamilyPlanningMemberProfilePresenter extends CoreFamilyPlanningProfilePresenter {
    public HfFamilyPlanningMemberProfilePresenter(CoreFamilyPlanningMemberProfileContract.View view, CoreFamilyPlanningMemberProfileContract.Interactor interactor,
                                                  FpMemberObject fpMemberObject) {
        super(view, interactor, fpMemberObject);
    }
}
