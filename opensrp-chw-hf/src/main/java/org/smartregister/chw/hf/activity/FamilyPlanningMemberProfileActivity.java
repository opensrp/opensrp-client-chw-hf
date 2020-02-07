package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.core.activity.CoreFamilyPlanningMemberProfileActivity;
import org.smartregister.chw.core.activity.CoreFpUpcomingServicesActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.FpUtil;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.hf.interactor.HfFamilyPlanningProfileInteractor;
import org.smartregister.chw.hf.presenter.HfFamilyPlanningMemberProfilePresenter;

import timber.log.Timber;

public class FamilyPlanningMemberProfileActivity extends CoreFamilyPlanningMemberProfileActivity {

    public static void startFpMemberProfileActivity(Activity activity, FpMemberObject memberObject) {
        Intent intent = new Intent(activity, FamilyPlanningMemberProfileActivity.class);
        intent.putExtra(FamilyPlanningConstants.FamilyPlanningMemberObject.MEMBER_OBJECT, memberObject);
        activity.startActivity(intent);
    }

    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        fpProfilePresenter = new HfFamilyPlanningMemberProfilePresenter(this, new HfFamilyPlanningProfileInteractor(this), fpMemberObject);
    }

    @Override
    public void openFamilyPlanningRegistration() {
        FpRegisterActivity.startFpRegistrationActivity(this, fpMemberObject.getBaseEntityId(), fpMemberObject.getAge(), CoreConstants.JSON_FORM.getFpRegistrationForm(), FamilyPlanningConstants.ActivityPayload.UPDATE_REGISTRATION_PAYLOAD_TYPE);
    }

    @Override
    public void openUpcomingServices() {
        CoreFpUpcomingServicesActivity.startMe(this, FpUtil.toMember(fpMemberObject));
    }

    @Override
    public void openMedicalHistory() {
        onMemberTypeLoadedListener listener = memberType -> {

            switch (memberType.getMemberType()) {
                case CoreConstants.TABLE_NAME.ANC_MEMBER:
                    AncMedicalHistoryActivity.startMe(FamilyPlanningMemberProfileActivity.this, memberType.getMemberObject());
                    break;
                case CoreConstants.TABLE_NAME.PNC_MEMBER:
                    PncMedicalHistoryActivity.startMe(FamilyPlanningMemberProfileActivity.this, memberType.getMemberObject());
                    break;
                case CoreConstants.TABLE_NAME.CHILD:
                    ChildMedicalHistoryActivity.startMe(FamilyPlanningMemberProfileActivity.this, memberType.getMemberObject());
                    break;
                default:
                    Timber.v("Member info undefined");
                    break;
            }
        };
        executeOnLoaded(listener);
    }

    @Override
    protected void removeMember() {
        // Not required for HF (as seen in other profile activities)?
    }

    @Override
    protected void startFamilyPlanningRegistrationActivity() {
        FpRegisterActivity.startFpRegistrationActivity(this, fpMemberObject.getBaseEntityId(), fpMemberObject.getAge(), CoreConstants.JSON_FORM.getFpChengeMethodForm(), FamilyPlanningConstants.ActivityPayload.CHANGE_METHOD_PAYLOAD_TYPE);
    }

    @Override
    public void verifyHasPhone() {
        // TODO -> Implement for HF
    }

    @Override
    public void notifyHasPhone(boolean b) {
        // TODO -> Implement for HF
    }
}
