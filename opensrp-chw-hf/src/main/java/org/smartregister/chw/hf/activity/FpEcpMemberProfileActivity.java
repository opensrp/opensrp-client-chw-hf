package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.core.utils.Utils.passToolbarTitle;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.chw.core.activity.CoreFamilyPlanningMemberProfileActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.fp.dao.FpDao;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.domain.Visit;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.fp.util.VisitUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.ReferralCardViewAdapter;
import org.smartregister.chw.hf.contract.FamilyPlanningMemberProfileContract;
import org.smartregister.chw.hf.dao.HfFpDao;
import org.smartregister.chw.hf.interactor.HfFamilyPlanningProfileInteractor;
import org.smartregister.chw.hf.presenter.HfFamilyPlanningMemberProfilePresenter;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.family.util.Utils;

import java.util.Set;

import timber.log.Timber;

public class FpEcpMemberProfileActivity extends CoreFamilyPlanningMemberProfileActivity implements FamilyPlanningMemberProfileContract.View {

    private CommonPersonObjectClient commonPersonObjectClient;

    public static void startFpEcpMemberProfileActivity(Activity activity, String baseEntityId) {
        Intent intent = new Intent(activity, FpEcpMemberProfileActivity.class);
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
        return true;
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
                fpMemberObject = HfFpDao.getEcpMember(commonPersonObjectClient.getCaseId());
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

    @Override
    public void setupViews() {
        super.setupViews();
        textViewRecordFp.setText(R.string.ecp_provision);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        if (((TextView) view).getText().equals(this.getString(R.string.ecp_provision))) {
            startFpEcpProvisionForm();
        }
    }

    public void startFpEcpProvisionForm() {
        startFormActivity(Constants.JsonForm.getFpEcpProvision(), fpMemberObject.getBaseEntityId(), null);
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
        FpMedicalHistoryActivity.startMe(FpEcpMemberProfileActivity.this, fpMemberObject);
    }

    @Override
    public Visit getLastVisit() {
        return null;
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

    @Override
    protected FpMemberObject getMemberObject(String baseEntityId) {
        return HfFpDao.getEcpMember(baseEntityId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 3000);
    }
}
