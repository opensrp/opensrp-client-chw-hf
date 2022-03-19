package org.smartregister.chw.hf.activity;

import static com.vijay.jsonwizard.constants.JsonFormConstants.VALUE;
import static org.smartregister.chw.core.utils.CoreConstants.EventType.PMTCT_COMMUNITY_FOLLOWUP;
import static org.smartregister.client.utils.constants.JsonFormConstants.FIELDS;
import static org.smartregister.client.utils.constants.JsonFormConstants.STEP1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.vijay.jsonwizard.utils.FormUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CorePmtctProfileActivity;
import org.smartregister.chw.core.custom_views.CorePmtctFloatingMenu;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.rule.PmtctFollowUpRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
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
import org.smartregister.chw.referral.util.JsonFormConstants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.AlertStatus;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.interactor.FamilyProfileInteractor;
import org.smartregister.family.model.BaseFamilyOtherMemberProfileActivityModel;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import timber.log.Timber;

public class PmtctProfileActivity extends CorePmtctProfileActivity {
    private static String baseEntityId;
    private static String visitStatus;
    private static Date pmtctRegisterDate;
    private static Date followUpVisitDate;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());


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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        try {
            if (itemId == R.id.action_issue_pmtct_followup_referral) {
                JSONObject formJsonObject = (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, CoreConstants.JSON_FORM.getPmtctcCommunityFollowupReferral());

                JSONObject reasonsForIssuingCommunityReferral = CoreJsonFormUtils.getJsonField(formJsonObject, STEP1, "reasons_for_issuing_community_referral");

                Date lastVisitDate = null;
                if (followUpVisitDate != null) {
                    lastVisitDate = followUpVisitDate;
                } else {
                    lastVisitDate = pmtctRegisterDate;
                }
                formJsonObject.getJSONObject(STEP1).getJSONArray(FIELDS).getJSONObject(getJsonArrayIndex(formJsonObject.getJSONObject(STEP1).getJSONArray(FIELDS), "last_client_visit_date")).put(VALUE, sdf.format(lastVisitDate));


                if (visitStatus.equals(CoreConstants.VISIT_STATE.DUE)) {
                    reasonsForIssuingCommunityReferral.getJSONArray("options").remove(getJsonArrayIndex(reasonsForIssuingCommunityReferral.getJSONArray("options"), "lost_to_followup"));
                } else if (visitStatus.equals(CoreConstants.VISIT_STATE.OVERDUE)) {
                    reasonsForIssuingCommunityReferral.getJSONArray("options").remove(getJsonArrayIndex(reasonsForIssuingCommunityReferral.getJSONArray("options"), "missed_appointment"));
                } else {
                    reasonsForIssuingCommunityReferral.getJSONArray("options").remove(getJsonArrayIndex(reasonsForIssuingCommunityReferral.getJSONArray("options"), "missed_appointment"));
                    reasonsForIssuingCommunityReferral.getJSONArray("options").remove(getJsonArrayIndex(reasonsForIssuingCommunityReferral.getJSONArray("options"), "lost_to_followup"));
                    reasonsForIssuingCommunityReferral.getJSONArray("options").getJSONObject(getJsonArrayIndex(reasonsForIssuingCommunityReferral.getJSONArray("options"), "mother_champion_services")).put(VALUE, true);
                }
                int index = getJsonArrayIndex(formJsonObject.getJSONObject(STEP1).getJSONArray(FIELDS), "reasons_for_issuing_community_referral");
                formJsonObject.getJSONObject(STEP1).getJSONArray(FIELDS).put(index, reasonsForIssuingCommunityReferral);

                startFormActivity(formJsonObject);
                return true;
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return super.onOptionsItemSelected(item);
    }

    private int getJsonArrayIndex(JSONArray options, String key) {
        for (int i = 0; i < options.length(); ++i) {
            try {
                if (options.getJSONObject(i).getString("key").equals(key)) {
                    return i;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                } else if (encounterType.equals(org.smartregister.chw.hf.utils.Constants.Events.PMTCT_EAC_VISIT)) {
                    AllSharedPreferences allSharedPreferences = org.smartregister.util.Utils.getAllSharedPreferences();
                    Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, Constants.TABLES.PMTCT_EAC_VISITS);
                    org.smartregister.chw.anc.util.JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
                    baseEvent.setBaseEntityId(baseEntityId);
                    try {
                        NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseEvent)));
                    } catch (Exception e) {
                        Timber.e(e);
                    }
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
        if (HfPmtctDao.hasHvlResults(baseEntityId)) {
            view_hvl_results_row.setVisibility(View.VISIBLE);
            rlHvlResults.setVisibility(View.VISIBLE);
        }

        if (HfPmtctDao.hasCd4Results(baseEntityId)) {
            view_baseline_results_row.setVisibility(View.VISIBLE);
            rlBaselineResults.setVisibility(View.VISIBLE);
        }
        if (HfPmtctDao.isEligibleForEac(baseEntityId)) {
            textViewRecordEac.setVisibility(View.VISIBLE);
            textViewRecordEac.setOnClickListener(this);
            if (HfPmtctDao.getEacVisitType(baseEntityId).equalsIgnoreCase(org.smartregister.chw.hf.utils.Constants.EacVisitTypes.EAC_FIRST_VISIT)) {
                textViewRecordEac.setText(R.string.record_eac_first_visit);
            } else {
                textViewRecordEac.setText(R.string.record_eac_second_visit);
            }
        }
        Visit lastFollowupVisit = getVisit(Constants.EVENT_TYPE.PMTCT_FOLLOWUP);
        if (lastFollowupVisit != null && !lastFollowupVisit.getProcessed()) {
            showVisitInProgress(org.smartregister.chw.hf.utils.Constants.Visits.PMTCT_VISIT);
            setUpEditButton();
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

        if (id == R.id.textview_record_pmtct) {
            PmtctFollowupVisitActivity.startPmtctFollowUpActivity(this, baseEntityId, false);
        } else if (id == R.id.textview_record_eac) {
            JSONObject formJsonObject;
            try {
                formJsonObject = (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, org.smartregister.chw.hf.utils.Constants.JsonForm.getEacVisitsForm());
                if (formJsonObject != null) {
                    JSONArray fields = formJsonObject.getJSONObject(org.smartregister.chw.hf.utils.Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                    JSONObject visit_type = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "eac_visit_type");

                    assert visit_type != null;
                    visit_type.put(JsonFormUtils.VALUE, HfPmtctDao.getEacVisitType(baseEntityId));

                    startFormActivity(formJsonObject);
                }
            } catch (JSONException e) {
                Timber.e(e);
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

    private void setUpEditButton() {
        textViewVisitDoneEdit.setOnClickListener(v -> {
            PmtctFollowupVisitActivity.startPmtctFollowUpActivity(PmtctProfileActivity.this, baseEntityId, true);
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
        Intent intent = new Intent(this, HvlResultsViewActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        startActivity(intent);
    }

    @Override
    public void openBaselineInvestigationResults() {
        Intent intent = new Intent(this, Cd4ResultsViewActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        startActivity(intent);
    }

    private class UpdateVisitDueTask extends AsyncTask<Void, Void, Void> {
        private PmtctFollowUpRule pmtctFollowUpRule;

        @Override
        protected Void doInBackground(Void... voids) {
            pmtctRegisterDate = PmtctDao.getPmtctRegisterDate(memberObject.getBaseEntityId());
            followUpVisitDate = PmtctDao.getPmtctFollowUpVisitDate(memberObject.getBaseEntityId());
            pmtctFollowUpRule = HomeVisitUtil.getPmtctVisitStatus(pmtctRegisterDate, followUpVisitDate, baseEntityId);
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            String visitStatus = pmtctFollowUpRule.getButtonStatus();
            PmtctProfileActivity.visitStatus = visitStatus;

            if (pmtctFollowUpRule.getButtonStatus().equals(CoreConstants.VISIT_STATE.NOT_DUE_YET))
                visitStatus = CoreConstants.VISIT_STATE.DUE;

            profilePresenter.recordPmtctButton(visitStatus);

            if (pmtctFollowUpRule.isFirstVisit())
                textViewRecordPmtct.setText(R.string.record_first_pmtct);

            Visit lastFolllowUpVisit = getVisit(Constants.EVENT_TYPE.PMTCT_FOLLOWUP);

            if (lastFolllowUpVisit != null && lastFolllowUpVisit.getProcessed()) {
                profilePresenter.visitRow(visitStatus);
            }

            profilePresenter.nextRow(visitStatus, FpUtil.sdf.format(pmtctFollowUpRule.getDueDate()));
        }
    }


}
