package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.core.utils.Utils.passToolbarTitle;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.chw.core.activity.CoreFamilyPlanningMemberProfileActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.fp.dao.FpDao;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.domain.Visit;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.ReferralCardViewAdapter;
import org.smartregister.chw.hf.contract.FamilyPlanningMemberProfileContract;
import org.smartregister.chw.hf.interactor.HfFamilyPlanningProfileInteractor;
import org.smartregister.chw.hf.presenter.HfFamilyPlanningMemberProfilePresenter;
import org.smartregister.chw.malaria.dao.MalariaDao;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;

import java.util.Set;

public class FamilyPlanningMemberProfileActivity extends CoreFamilyPlanningMemberProfileActivity implements FamilyPlanningMemberProfileContract.View {

    private CommonPersonObjectClient commonPersonObjectClient;

    public static void startFpMemberProfileActivity(Activity activity, FpMemberObject memberObject) {
        Intent intent = new Intent(activity, FamilyPlanningMemberProfileActivity.class);
        passToolbarTitle(activity, intent);
        intent.putExtra(FamilyPlanningConstants.ACTIVITY_PAYLOAD.MEMBER_OBJECT, memberObject);
        activity.startActivity(intent);
    }

    public void setReferralTasks(Set<Task> taskList) {
        if (notificationAndReferralRecyclerView != null && taskList.size() > 0) {
            RecyclerView.Adapter mAdapter = new ReferralCardViewAdapter(taskList, this, getCommonPersonObjectClient(), CoreConstants.REGISTERED_ACTIVITIES.FP_REGISTER_ACTIVITY);
            notificationAndReferralRecyclerView.setAdapter(mAdapter);
            notificationAndReferralLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.view_notification_and_referral_row).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        setCommonPersonObjectClient(getClientDetailsByBaseEntityID(fpMemberObject.getBaseEntityId()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(org.smartregister.chw.core.R.menu.family_planning_member_profile_menu, menu);
        if (MalariaDao.isRegisteredForMalaria(fpMemberObject.getBaseEntityId())) {
            menu.findItem(R.id.action_malaria_followup_visit).setVisible(true);
            menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);
        } else {
            menu.findItem(R.id.action_malaria_diagnosis).setVisible(true);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fpMemberObject == null && commonPersonObjectClient != null) {
            fpMemberObject = FpDao.getMember(commonPersonObjectClient.getCaseId());
        }
        ((FamilyPlanningMemberProfileContract.Presenter) fpProfilePresenter).fetchReferralTasks();
        if (notificationAndReferralRecyclerView != null && notificationAndReferralRecyclerView.getAdapter() != null) {
            notificationAndReferralRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    public CommonPersonObjectClient getCommonPersonObjectClient() {
        return commonPersonObjectClient;
    }

    public void setCommonPersonObjectClient(CommonPersonObjectClient commonPersonObjectClient) {
        this.commonPersonObjectClient = commonPersonObjectClient;
    }

    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        fpProfilePresenter = new HfFamilyPlanningMemberProfilePresenter(this, new HfFamilyPlanningProfileInteractor(), fpMemberObject);
    }

    @Override
    public void openMedicalHistory() {

    }

    @Override
    public Visit getLastVisit() {
        return null;
    }

    @Override
    public boolean getIsClientUsingFpMethod() {
        return false;
    }

    @Override
    public boolean isFirstVisit() {
        return false;
    }

    @Override
    public void startPointOfServiceDeliveryForm() {

    }

    @Override
    public void startFpCounselingForm() {

    }

    @Override
    public void startFpScreeningForm() {

    }

    @Override
    public void startProvideFpMethod() {

    }

    @Override
    public void startProvideOtherServices() {

    }

    @Override
    public void startFpFollowupVisit() {

    }

    @Override
    public void showFollowUpVisitButton() {

    }

    @Override
    protected void removeMember() {
        // Not required for HF (as seen in other profile activities)?
    }

    @Override
    protected void startFamilyPlanningRegistrationActivity() {
        FpRegisterActivity.startFpRegistrationActivity(this, fpMemberObject.getBaseEntityId(), CoreConstants.JSON_FORM.getFpChangeMethodForm(fpMemberObject.getGender()));
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
