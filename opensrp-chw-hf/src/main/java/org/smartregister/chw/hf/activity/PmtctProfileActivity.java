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
import android.widget.Toast;

import com.vijay.jsonwizard.utils.FormUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CorePmtctProfileActivity;
import org.smartregister.chw.core.custom_views.CorePmtctFloatingMenu;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.rule.PmtctFollowUpRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.FpUtil;
import org.smartregister.chw.core.utils.HomeVisitUtil;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.PmtctReferralCardViewAdapter;
import org.smartregister.chw.hf.custom_view.PmtctFloatingMenu;
import org.smartregister.chw.hf.dao.HfPmtctDao;
import org.smartregister.chw.hf.interactor.PmtctProfileInteractor;
import org.smartregister.chw.hf.model.FamilyProfileModel;
import org.smartregister.chw.hf.model.PmtctFollowupFeedbackModel;
import org.smartregister.chw.hf.presenter.FamilyOtherMemberActivityPresenter;
import org.smartregister.chw.hf.presenter.PmtctProfilePresenter;
import org.smartregister.chw.hf.utils.PmtctVisitUtils;
import org.smartregister.chw.pmtct.PmtctLibrary;
import org.smartregister.chw.pmtct.dao.PmtctDao;
import org.smartregister.chw.pmtct.domain.Visit;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.domain.AlertStatus;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.interactor.FamilyProfileInteractor;
import org.smartregister.family.model.BaseFamilyOtherMemberProfileActivityModel;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import static org.smartregister.chw.core.utils.CoreConstants.EventType.PMTCT_COMMUNITY_FOLLOWUP;
import static org.smartregister.chw.hf.utils.Constants.Events.PMTCT_FIRST_EAC_VISIT;
import static org.smartregister.chw.hf.utils.Constants.Events.PMTCT_SECOND_EAC_VISIT;

public class PmtctProfileActivity extends CorePmtctProfileActivity {
    private static String baseEntityId;

    public static void startPmtctActivity(Activity activity, String baseEntityId) {
        PmtctProfileActivity.baseEntityId = baseEntityId;
        Intent intent = new Intent(activity, PmtctProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        activity.startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        ((PmtctProfilePresenter) profilePresenter).updateFollowupFeedback(baseEntityId);
        if (notificationAndReferralRecyclerView != null && notificationAndReferralRecyclerView.getAdapter() != null) {
            notificationAndReferralRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        String baseEntityId = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
        memberObject = PmtctDao.getMember(baseEntityId);
        profilePresenter = new PmtctProfilePresenter(this, new PmtctProfileInteractor(), memberObject);
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
        int itemId = item.getItemId();
        try {
            if (itemId == R.id.action_issue_pmtct_followup_referral) {
                JSONObject formJsonObject = (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, CoreConstants.JSON_FORM.getPmtctcCommunityFollowupReferral());
                startFormActivity(formJsonObject);
                return true;
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);
                String encounterType = form.getString(JsonFormUtils.ENCOUNTER_TYPE);
                if (encounterType.equals(Utils.metadata().familyMemberRegister.updateEventType)) {
                    FamilyEventClient familyEventClient =
                            new FamilyProfileModel(memberObject.getFamilyName()).processUpdateMemberRegistration(jsonString, memberObject.getBaseEntityId());
                    new FamilyProfileInteractor().saveRegistration(familyEventClient, jsonString, true, (FamilyProfileContract.InteractorCallBack) profilePresenter);
                } else if (encounterType.equals(PMTCT_COMMUNITY_FOLLOWUP)) {
                    AllSharedPreferences allSharedPreferences = org.smartregister.util.Utils.getAllSharedPreferences();
                    ((PmtctProfilePresenter) profilePresenter).createPmtctCommunityFollowupReferralEvent(allSharedPreferences, data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON), baseEntityId);
                } else {
                    profilePresenter.saveForm(data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
                    finish();
                }
            } catch (Exception e) {
                Timber.e(e, "PmtctProfileActivity -- > onActivityResult");
            }
        }
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

    @Override
    protected void setupViews() {
        super.setupViews();
        try {
            PmtctVisitUtils.processVisits();
        } catch (Exception e) {
            Timber.e(e);
        }
        view_hvl_results_row.setVisibility(View.VISIBLE);
        rlHvlResults.setVisibility(View.VISIBLE);
        boolean isEligibleForFirst = HfPmtctDao.isEligibleForEac(baseEntityId);
        boolean isEligibleForSecond = HfPmtctDao.isEligibleForSecondEac(baseEntityId);
        if (isEligibleForFirst) {
            Visit lastEacVisit = getVisit(PMTCT_FIRST_EAC_VISIT);
            if (lastEacVisit != null && !lastEacVisit.getProcessed()) {
                showVisitInProgress(org.smartregister.chw.hf.utils.Constants.Visits.FIRST_EAC);
                setUpEditButton(org.smartregister.chw.hf.utils.Constants.Visits.FIRST_EAC);
            }
        }
        if (isEligibleForSecond) {
            Visit lastEacVisit = getVisit(PMTCT_SECOND_EAC_VISIT);
            if (lastEacVisit != null && !lastEacVisit.getProcessed()) {
                showVisitInProgress(org.smartregister.chw.hf.utils.Constants.Visits.SECOND_EAC);
                setUpEditButton(org.smartregister.chw.hf.utils.Constants.Visits.SECOND_EAC);
            }
        }
        Visit lastFollowupVisit = getVisit(Constants.EVENT_TYPE.PMTCT_FOLLOWUP);
        if (lastFollowupVisit != null && !lastFollowupVisit.getProcessed()) {
            showVisitInProgress(org.smartregister.chw.hf.utils.Constants.Visits.PMTCT_VISIT);
            setUpEditButton(org.smartregister.chw.hf.utils.Constants.Visits.PMTCT_VISIT);
        }

    }

    @Override
    public void refreshFamilyStatus(AlertStatus status) {
        super.refreshFamilyStatus(status);
        rlFamilyServicesDue.setVisibility(View.GONE);
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
        boolean isEligibleForFirst = HfPmtctDao.isEligibleForEac(baseEntityId);
        boolean isEligibleForSecond = HfPmtctDao.isEligibleForSecondEac(baseEntityId);
        boolean isEacFirstDone = HfPmtctDao.isEacFirstDone(baseEntityId);
        boolean isEacSecondDone = HfPmtctDao.isSecondEacDone(baseEntityId);
        if (id == R.id.textview_record_pmtct) {
            PmtctFollowupVisitActivity.startPmtctFollowUpActivity(this, baseEntityId, false);
        } else if (id == R.id.textview_record_anc) {
            if (isEligibleForFirst && !isEacFirstDone) {
                PmtctEacFirstVisitActivity.startEacActivity(this, memberObject.getBaseEntityId(), false);
            } else if (isEligibleForSecond && !isEacSecondDone) {
                PmtctEacSecondVisitActivity.startSecondEacActivity(this, memberObject.getBaseEntityId(), false);
            }
        } else if (id == R.id.textview_edit) {
            Toast.makeText(this, "Action Not Defined", Toast.LENGTH_SHORT).show();
        }

    }

    private void showVisitInProgress(String typeOfVisit) {
        recordVisits.setVisibility(View.GONE);
        if (typeOfVisit.equalsIgnoreCase(org.smartregister.chw.hf.utils.Constants.Visits.PMTCT_VISIT)) {
            textViewRecordPmtct.setVisibility(View.GONE);
        }
        textViewVisitDoneEdit.setVisibility(View.VISIBLE);
        visitDone.setVisibility(View.VISIBLE);
        textViewVisitDone.setText(getContext().getString(R.string.visit_in_progress, typeOfVisit));
        textViewVisitDone.setTextColor(getResources().getColor(R.color.black_text_color));
        imageViewCross.setImageResource(org.smartregister.chw.core.R.drawable.activityrow_notvisited);
    }

    private void setUpEditButton(String typeOfVisit) {
        textViewVisitDoneEdit.setOnClickListener(v -> {
            switch (typeOfVisit) {
                case org.smartregister.chw.hf.utils.Constants.Visits.FIRST_EAC:
                    PmtctEacFirstVisitActivity.startEacActivity(PmtctProfileActivity.this, memberObject.getBaseEntityId(), true);
                    break;
                case org.smartregister.chw.hf.utils.Constants.Visits.SECOND_EAC:
                    PmtctEacSecondVisitActivity.startSecondEacActivity(PmtctProfileActivity.this, memberObject.getBaseEntityId(), true);
                    break;
                case org.smartregister.chw.hf.utils.Constants.Visits.PMTCT_VISIT:
                    PmtctFollowupVisitActivity.startPmtctFollowUpActivity(PmtctProfileActivity.this, baseEntityId, true);
                    break;
                default:
                    break;
            }
        });
    }

    public @Nullable
    Visit getVisit(String eventType) {
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

    public void setFollowupFeedback(List<PmtctFollowupFeedbackModel> followupFeedbacks) {
        if (notificationAndReferralRecyclerView != null && followupFeedbacks.size() > 0) {
            RecyclerView.Adapter mAdapter = new PmtctReferralCardViewAdapter(followupFeedbacks, this, org.smartregister.chw.core.utils.Utils.getCommonPersonObjectClient(memberObject.getBaseEntityId()), CoreConstants.REGISTERED_ACTIVITIES.PMTCT_REGISTER_ACTIVITY);
            notificationAndReferralRecyclerView.setAdapter(mAdapter);
            notificationAndReferralLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.view_notification_and_referral_row).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void openHvlResultsHistory() {
        //implement
        Intent intent = new Intent(this, HvlResultsViewActivity.class);
        startActivity(intent);
    }

    private class UpdateVisitDueTask extends AsyncTask<Void, Void, Void> {
        private PmtctFollowUpRule pmtctFollowUpRule;

        @Override
        protected Void doInBackground(Void... voids) {
            Date pmtctRegisterDate = PmtctDao.getPmtctRegisterDate(memberObject.getBaseEntityId());
            Date followUpVisitDate = PmtctDao.getPmtctFollowUpVisitDate(memberObject.getBaseEntityId());
            pmtctFollowUpRule = HomeVisitUtil.getPmtctVisitStatus(pmtctRegisterDate, followUpVisitDate, baseEntityId);
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            profilePresenter.recordPmtctButton(pmtctFollowUpRule.getButtonStatus());
            if (pmtctFollowUpRule.isFirstVisit())
                textViewRecordPmtct.setText(R.string.record_first_pmtct);

            boolean showEac = !pmtctFollowUpRule.getButtonStatus().equalsIgnoreCase("DUE")
                    && !pmtctFollowUpRule.getButtonStatus().equalsIgnoreCase("OVERDUE")
                    && !pmtctFollowUpRule.getButtonStatus().equalsIgnoreCase("EXPIRY");
            boolean isEligibleForFirstEac = HfPmtctDao.isEligibleForEac(baseEntityId);
            boolean isEligibleForSecondEac = HfPmtctDao.isEligibleForSecondEac(baseEntityId);
            boolean isEacFirstDone = HfPmtctDao.isEacFirstDone(baseEntityId);
            boolean isEacSecondDone = HfPmtctDao.isSecondEacDone(baseEntityId);
            Visit lastFolllowUpVisit = getVisit(Constants.EVENT_TYPE.PMTCT_FOLLOWUP);

            if (showEac && isEligibleForFirstEac && !isEacFirstDone) {
                recordVisits.setWeightSum(1);
                textViewRecordAnc.setVisibility(View.VISIBLE);
                textViewRecordAnc.setText(R.string.record_eac_first_visit);
            } else if (showEac && isEligibleForSecondEac && !isEacSecondDone) {
                recordVisits.setWeightSum(1);
                textViewRecordAnc.setVisibility(View.VISIBLE);
                textViewRecordAnc.setText(R.string.record_eac_second_visit);
            } else {
                if (lastFolllowUpVisit != null && lastFolllowUpVisit.getProcessed()) {
                    profilePresenter.visitRow(pmtctFollowUpRule.getButtonStatus());
                }
            }
            profilePresenter.nextRow(pmtctFollowUpRule.getButtonStatus(), FpUtil.sdf.format(pmtctFollowUpRule.getDueDate()));
        }
    }


}
