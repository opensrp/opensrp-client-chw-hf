package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.core.utils.Utils.passToolbarTitle;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.chw.core.activity.CoreFamilyPlanningMemberProfileActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.fp.dao.FpDao;
import org.smartregister.chw.fp.domain.Visit;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.fp.util.VisitUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.ReferralCardViewAdapter;
import org.smartregister.chw.hf.contract.FamilyPlanningMemberProfileContract;
import org.smartregister.chw.hf.interactor.HfFamilyPlanningProfileInteractor;
import org.smartregister.chw.hf.presenter.HfFamilyPlanningMemberProfilePresenter;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.family.util.Utils;

import java.util.Set;

import timber.log.Timber;

public class FpMemberProfileActivity extends CoreFamilyPlanningMemberProfileActivity implements FamilyPlanningMemberProfileContract.View {

    private CommonPersonObjectClient commonPersonObjectClient;

    public static void startFpMemberProfileActivity(Activity activity, String baseEntityId) {
        Intent intent = new Intent(activity, FpMemberProfileActivity.class);
        passToolbarTitle(activity, intent);
        intent.putExtra(FamilyPlanningConstants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
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
        delayRefreshSetupViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(org.smartregister.chw.core.R.menu.family_planning_member_profile_menu, menu);
        menu.findItem(R.id.action_malaria_followup_visit).setVisible(false);
        menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);
        menu.findItem(R.id.action_fp_change).setVisible(false);
        menu.findItem(R.id.action_fp_ecp_provision).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_fp_ecp_provision) {
            FpRegisterActivity.startFpRegistrationActivity(this, fpMemberObject.getBaseEntityId(), Constants.JsonForm.getFPEcpScreening());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            VisitUtils.processVisits(fpMemberObject.getBaseEntityId());
        } catch (Exception e) {
            Timber.e(e);
        }
        ((FamilyPlanningMemberProfileContract.Presenter) fpProfilePresenter).fetchReferralTasks();
        if (notificationAndReferralRecyclerView != null && notificationAndReferralRecyclerView.getAdapter() != null) {
            notificationAndReferralRecyclerView.getAdapter().notifyDataSetChanged();
        }
        delayRefreshSetupViews();
    }

    private void delayRefreshSetupViews() {
        try {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                fpMemberObject = FpDao.getMember(commonPersonObjectClient.getCaseId());
                getLastVisit();
                setupViews();
                Visit lastVisit = FpDao.getLatestVisit(fpMemberObject.getBaseEntityId(), FamilyPlanningConstants.EVENT_TYPE.FP_POINT_OF_SERVICE_DELIVERY);
                if (lastVisit != null)
                    refreshMedicalHistory(true);
            }, 300);
        } catch (Exception e) {
            Timber.e(e);
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
        fpProfilePresenter.refreshProfileBottom();
    }

    @Override
    public void openMedicalHistory() {
        FpMedicalHistoryActivity.startMe(FpMemberProfileActivity.this, fpMemberObject);
    }

    @Override
    public Visit getLastVisit() {
        try {
            Visit lastVisit = FpDao.getLatestVisit(fpMemberObject.getBaseEntityId());
            if (lastVisit.getParentVisitID() != null) {
                Visit parentVisit = FpDao.getLatestVisitById(lastVisit.getParentVisitID());
                if (parentVisit != null) return parentVisit;
            }
            return lastVisit;
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }

    @Override
    public boolean isFirstVisit() {
        return FpDao.getLatestVisit(fpMemberObject.getBaseEntityId(), FamilyPlanningConstants.EVENT_TYPE.FP_OTHER_SERVICES) == null;
    }

    @Override
    public void startPointOfServiceDeliveryForm() {
        startFormActivity(FamilyPlanningConstants.FORMS.FP_POINT_OF_SERVICE_DELIVERY, fpMemberObject.getBaseEntityId(), null);
    }

    @Override
    public void startFpCounselingForm() {
        startFormActivity(FamilyPlanningConstants.FORMS.FP_COUNSELING, fpMemberObject.getBaseEntityId(), null);
    }

    @Override
    public void startFpScreeningForm() {
        FpScreeningActivity.startMe(this, fpMemberObject.getBaseEntityId(), false);
    }

    @Override
    public void startProvideFpMethod() {
        startFormActivity(FamilyPlanningConstants.FORMS.FP_PROVISION_OF_FP_METHOD, fpMemberObject.getBaseEntityId(), null);
    }

    @Override
    public void startProvideOtherServices() {
        FpOtherServicesActivity.startMe(this, fpMemberObject.getBaseEntityId(), false);
    }

    @Override
    public void startFpFollowupVisit() {
        FpFollowupVisitProvisionOfServicesActivity.startMe(this, fpMemberObject.getBaseEntityId(), false);
    }

    @Override
    public void showFollowUpVisitButton() {
        //Not Required
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

    @Override
    public Class getFormActivity() {
        return Utils.metadata().familyMemberFormActivity;
    }

    @Override
    public void setFollowUpButtonOverdue() {
        showFollowUpVisitButton();
        textViewRecordFp.setBackground(getResources().getDrawable(org.smartregister.chw.fp.R.drawable.record_btn_selector));
    }
}
