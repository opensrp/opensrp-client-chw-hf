package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CoreVmmcProfileActivity;
import org.smartregister.chw.core.custom_views.CoreVmmcFloatingMenu;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.presenter.CoreFamilyOtherMemberActivityPresenter;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.custom_view.VmmcFloatingMenu;
import org.smartregister.chw.hf.utils.VmmcReferralFormUtils;
import org.smartregister.chw.vmmc.VmmcLibrary;
import org.smartregister.chw.vmmc.domain.Visit;
import org.smartregister.chw.vmmc.util.Constants;

import timber.log.Timber;

public class VmmcProfileActivity extends CoreVmmcProfileActivity {

    private static String baseEntityId;

    public static void startVmmcActivity(Activity activity, String baseEntityId) {
        VmmcProfileActivity.baseEntityId = baseEntityId;
        Intent intent = new Intent(activity, VmmcProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.PROFILE_TYPE, Constants.PROFILE_TYPES.VMMC_PROFILE);
        activity.startActivity(intent);
    }

    @Override
    public void openFollowupVisit() {
        VmmcServiceActivity.startVmmcVisitActivity(this, baseEntityId, false);
    }

    @Override
    protected Class<? extends CoreFamilyProfileActivity> getFamilyProfileActivityClass() {
        return null;
    }

    @Override
    protected void removeMember() {
        //do nothing
    }

    @NonNull
    @Override
    public CoreFamilyOtherMemberActivityPresenter presenter() {
        return null;
    }

    @Override
    public void setProfileImage(String s, String s1) {
        //do nothing
    }

    @Override
    public void setProfileDetailThree(String s) {
        //do nothing
    }

    @Override
    public void toggleFamilyHead(boolean b) {
        //do nothing
    }

    @Override
    public void togglePrimaryCaregiver(boolean b) {
        //do nothing
    }

    @Override
    public void refreshMedicalHistory(boolean hasHistory) {
        Visit vmmcServices = getVisit(Constants.EVENT_TYPE.VMMC_SERVICES);
        Visit vmmcProcedure = getVisit(Constants.EVENT_TYPE.VMMC_PROCEDURE);
        Visit vmmcDischarge = getVisit(Constants.EVENT_TYPE.VMMC_DISCHARGE);
        Visit vmmcFollowUp = getVisit(Constants.EVENT_TYPE.VMMC_FOLLOW_UP_VISIT);


        if (vmmcServices != null || vmmcProcedure != null || vmmcDischarge != null || vmmcFollowUp != null) {
            rlLastVisit.setVisibility(View.VISIBLE);
            findViewById(R.id.view_notification_and_referral_row).setVisibility(View.VISIBLE);
        } else {
            rlLastVisit.setVisibility(View.GONE);
        }
    }

    @Override
    public void startServiceForm() {
        VmmcServiceActivity.startVmmcVisitActivity(this, baseEntityId, false);
    }

    @Override
    public void startNotifiableForm() {
        //VmmcNotifiableAdverseActivity.startVmmcVisitActivity(this, baseEntityId, false);
    }

    @Override
    public void startFollowUp() {
        VmmcFollowUpActivity.startVmmcVisitActivity(this, baseEntityId, false);
    }

    @Override
    public void startProcedure() {
        VmmcProcedureActivity.startVmmcVisitProcedureActivity(this, baseEntityId, false);
    }

    @Override
    public void startDischarge() {
        VmmcDischargeActivity.startVmmcVisitDischargeActivity(this, baseEntityId, false);
    }

    @Override
    public void continueService() {
        VmmcServiceActivity.startVmmcVisitActivity(this, baseEntityId, true);
    }

    @Override
    public void continueProcedure() {
        VmmcProcedureActivity.startVmmcVisitProcedureActivity(this, baseEntityId, true);
    }

    @Override
    public void continueDischarge() {
        VmmcDischargeActivity.startVmmcVisitDischargeActivity(this, baseEntityId, true);

    }

    @Override
    public void openMedicalHistory() {
        VmmcMedicalHistoryActivity.startMe(this, memberObject);
    }

    private Visit getVisit(String eventType) {
        return VmmcLibrary.getInstance().visitRepository().getLatestVisit(baseEntityId, eventType);
    }

    @Override
    public void initializeFloatingMenu() {

        baseVmmcFloatingMenu = new VmmcFloatingMenu(this, memberObject);

        OnClickFloatingMenu onClickFloatingMenu = viewId -> {
            switch (viewId) {
                case R.id.vmmc_fab:
                    ((CoreVmmcFloatingMenu) baseVmmcFloatingMenu).animateFAB();
                    break;
                case R.id.call_layout:
                    ((CoreVmmcFloatingMenu) baseVmmcFloatingMenu).launchCallWidget();
                    ((CoreVmmcFloatingMenu) baseVmmcFloatingMenu).animateFAB();
                    break;
                case R.id.refer_to_facility_layout:
                    VmmcReferralFormUtils.startVmmcReferral(this, memberObject.getBaseEntityId());
                    ((CoreVmmcFloatingMenu) baseVmmcFloatingMenu).animateFAB();
                    break;
                default:
                    Timber.d("Unknown fab action");
                    break;
            }
        };

        ((CoreVmmcFloatingMenu) baseVmmcFloatingMenu).setFloatMenuClickListener(onClickFloatingMenu);
        baseVmmcFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.END);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        addContentView(baseVmmcFloatingMenu, linearLayoutParams);
    }

    @Override
    public void refreshList() {
        //do nothing
    }

    @Override
    public void updateHasPhone(boolean b) {
        //do nothing
    }

    @Override
    public void setFamilyServiceStatus(String s) {
        //do nothing
    }

    @Override
    public void verifyHasPhone() {
        //do nothing
    }

    @Override
    public void notifyHasPhone(boolean b) {
        //do nothing
    }
}

