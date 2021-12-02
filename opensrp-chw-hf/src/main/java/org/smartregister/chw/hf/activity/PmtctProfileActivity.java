package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CorePmtctProfileActivity;
import org.smartregister.chw.core.custom_views.CorePmtctFloatingMenu;
import org.smartregister.chw.core.interactor.CorePmtctProfileInteractor;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.rule.PmtctFollowUpRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.FpUtil;
import org.smartregister.chw.core.utils.HomeVisitUtil;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.custom_view.PmtctFloatingMenu;
import org.smartregister.chw.hf.dao.HfPmtctDao;
import org.smartregister.chw.hf.presenter.FamilyOtherMemberActivityPresenter;
import org.smartregister.chw.hf.presenter.PmtctProfilePresenter;
import org.smartregister.chw.pmtct.PmtctLibrary;
import org.smartregister.chw.pmtct.dao.PmtctDao;
import org.smartregister.chw.pmtct.domain.Visit;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.family.model.BaseFamilyOtherMemberProfileActivityModel;

import java.util.Date;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import timber.log.Timber;

import static org.smartregister.chw.hf.utils.Constants.Events.PMTCT_EAC_VISIT;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.getHvlSuppressionForm;
import static org.smartregister.chw.pmtct.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID;

public class PmtctProfileActivity extends CorePmtctProfileActivity {
    private static String baseEntityId;

    public static void startPmtctActivity(Activity activity, String baseEntityId) {
        PmtctProfileActivity.baseEntityId = baseEntityId;
        Intent intent = new Intent(activity, PmtctProfileActivity.class);
        intent.putExtra(BASE_ENTITY_ID, baseEntityId);
        activity.startActivity(intent);
    }

    public void startFollowupForm(Activity activity, String baseEntityID){
        Intent intent = new Intent(activity, PmtctRegisterActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID,baseEntityID );
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.PMTCT_FORM_NAME, getHvlSuppressionForm());
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.hf.utils.Constants.ActionList.FOLLOWUP);
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        String baseEntityId = getIntent().getStringExtra(BASE_ENTITY_ID);
        memberObject = PmtctDao.getMember(baseEntityId);
        profilePresenter = new PmtctProfilePresenter(this, new CorePmtctProfileInteractor(), memberObject);
        fetchProfileData();
        profilePresenter.refreshProfileBottom();
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        org.smartregister.util.Utils.startAsyncTask(new UpdateVisitDueTask(), null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_remove_member).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void initializeFloatingMenu() {
        basePmtctFloatingMenu = new PmtctFloatingMenu(this, memberObject);
        checkPhoneNumberProvided(StringUtils.isNotBlank(memberObject.getPhoneNumber()));
        OnClickFloatingMenu onClickFloatingMenu = viewId -> {
            switch (viewId) {
                case R.id.pmtct_fab:
                    ((CorePmtctFloatingMenu) basePmtctFloatingMenu).animateFAB();
                    break;
                case R.id.call_layout:
                    ((CorePmtctFloatingMenu) basePmtctFloatingMenu).launchCallWidget();
                    ((CorePmtctFloatingMenu) basePmtctFloatingMenu).animateFAB();
                    break;
                default:
                    Timber.d("Unknown fab action");
                    break;
            }

        };

        ((CorePmtctFloatingMenu) basePmtctFloatingMenu).setFloatMenuClickListener(onClickFloatingMenu);
        basePmtctFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.END);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        addContentView(basePmtctFloatingMenu, linearLayoutParams);
    }

    private class UpdateVisitDueTask extends AsyncTask<Void, Void, Void> {
        private PmtctFollowUpRule pmtctFollowUpRule;

        @Override
        protected Void doInBackground(Void... voids) {
            Date pmtctRegisterDate = PmtctDao.getPmtctRegisterDate(memberObject.getBaseEntityId());
            Date followUpVisitDate = PmtctDao.getPmtctFollowUpVisitDate(memberObject.getBaseEntityId());
            pmtctFollowUpRule = HomeVisitUtil.getPmtctVisitStatus(pmtctRegisterDate, followUpVisitDate);
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            profilePresenter.recordPmtctButton(pmtctFollowUpRule.getButtonStatus());
            boolean showEac = !pmtctFollowUpRule.getButtonStatus().equalsIgnoreCase("DUE")
                                && !pmtctFollowUpRule.getButtonStatus().equalsIgnoreCase("OVERDUE")
                                && !pmtctFollowUpRule.getButtonStatus().equalsIgnoreCase("EXPIRY");
            boolean isEligible = HfPmtctDao.isEligibleForEac(baseEntityId);

            if(showEac && isEligible){
                recordVisits.setWeightSum(1);
                textViewRecordAnc.setVisibility(View.VISIBLE);
                textViewRecordAnc.setText(R.string.pmtct_record_eac_visit);
            }else{
                profilePresenter.visitRow(pmtctFollowUpRule.getButtonStatus());
            }
            profilePresenter.nextRow(pmtctFollowUpRule.getButtonStatus(), FpUtil.sdf.format(pmtctFollowUpRule.getDueDate()));
        }
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        Visit lastEacVisit = getVisit(PMTCT_EAC_VISIT);
        if(lastEacVisit != null && !lastEacVisit.getProcessed()){
            showVisitInProgress();
        }

    }

    @NonNull
    @Override
    public FamilyOtherMemberActivityPresenter presenter() {
        return new FamilyOtherMemberActivityPresenter(this, new BaseFamilyOtherMemberProfileActivityModel(), null, memberObject.getRelationalId(), memberObject.getBaseEntityId(), memberObject.getFamilyHead(), memberObject.getPrimaryCareGiver(), memberObject.getAddress(), memberObject.getLastName());
    }

    @Override
    protected void removeMember() {
        IndividualProfileRemoveActivity.startIndividualProfileActivity(this,
                getClientDetailsByBaseEntityID(memberObject.getBaseEntityId()),
                memberObject.getFamilyBaseEntityId(), memberObject.getFamilyHead(),
                memberObject.getPrimaryCareGiver(), FpRegisterActivity.class.getCanonicalName());
    }

    @Override
    public void setProfileImage(String s, String s1) {
        //implement
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.textview_record_pmtct) {
            startFollowupForm(this, memberObject.getBaseEntityId());
        }else if(id == R.id.textview_record_anc){
            PmtctEacVisitActivity.startEacActivity(this, memberObject.getBaseEntityId(), false);
        }else if (id == R.id.textview_edit){
            Visit lastVisit = getVisit(PMTCT_EAC_VISIT);
            if(lastVisit != null){
                PmtctEacVisitActivity.startEacActivity(this,memberObject.getBaseEntityId(),true);
            }
        }

    }


    private void showVisitInProgress() {
        recordVisits.setVisibility(View.GONE);
        textViewVisitDoneEdit.setVisibility(View.VISIBLE);
        visitDone.setVisibility(View.VISIBLE);
        textViewVisitDone.setText(R.string.visit_in_progress);
        textViewVisitDone.setTextColor(getResources().getColor(R.color.black_text_color));
        imageViewCross.setImageResource(org.smartregister.chw.core.R.drawable.activityrow_notvisited);
    }

    public @Nullable Visit getVisit(String eventType){
        return PmtctLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), eventType);
    }

    @Override
    public void setProfileDetailThree(String s) {
        //implement
    }

    @Override
    public void toggleFamilyHead(boolean b) {
        //implement
    }

    @Override
    public void togglePrimaryCaregiver(boolean b) {
        //implement
    }

    @Override
    public void refreshList() {
        //implement
    }

    @Override
    public void updateHasPhone(boolean hasPhone) {
        //implement
    }

    @Override
    public void setFamilyServiceStatus(String status) {
        //implement
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void openUpcomingService() {
        //  executeOnLoaded(memberType -> MalariaUpcomingServicesActivity.startMe(PmtctProfileActivity.this, memberType.getMemberObject()));
    }

    @Override
    public void openFamilyDueServices() {
        Intent intent = new Intent(this, FamilyProfileActivity.class);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, memberObject.getFamilyBaseEntityId());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_HEAD, memberObject.getFamilyHead());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.PRIMARY_CAREGIVER, memberObject.getPrimaryCareGiver());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_NAME, memberObject.getFamilyName());

        intent.putExtra(CoreConstants.INTENT_KEY.SERVICE_DUE, true);
        startActivity(intent);
    }
    @Override
    protected Class<? extends CoreFamilyProfileActivity> getFamilyProfileActivityClass() {
        return FamilyProfileActivity.class;
    }

    @Override
    public void verifyHasPhone() {
//        TODO implement check if has phone number
    }

    @Override
    public void notifyHasPhone(boolean b) {
//        TODO notify if it has phone number
    }

    private void checkPhoneNumberProvided(boolean hasPhoneNumber) {
        ((CorePmtctFloatingMenu) basePmtctFloatingMenu).redraw(hasPhoneNumber);
    }


}
