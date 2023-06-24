package org.smartregister.chw.hf.activity;

import static org.smartregister.AllConstants.LocationConstants.SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS;
import static org.smartregister.chw.core.utils.CoreConstants.EventType.PMTCT_COMMUNITY_FOLLOWUP;
import static org.smartregister.chw.hf.activity.HivProfileActivity.startUpdateCtcNumber;
import static org.smartregister.chw.hf.utils.Constants.JsonFormConstants.STEP1;
import static org.smartregister.chw.hf.utils.JsonFormUtils.SYNC_LOCATION_ID;
import static org.smartregister.chw.hf.utils.JsonFormUtils.getAutoPopulatedJsonEditFormString;
import static org.smartregister.util.JsonFormUtils.VALUE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import org.smartregister.chw.core.model.CoreAllClientsMemberModel;
import org.smartregister.chw.core.rule.PmtctFollowUpRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FpUtil;
import org.smartregister.chw.core.utils.UpdateDetailsUtil;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.PmtctReferralCardViewAdapter;
import org.smartregister.chw.hf.custom_view.PmtctFloatingMenu;
import org.smartregister.chw.hf.dao.HfPmtctDao;
import org.smartregister.chw.hf.interactor.PmtctProfileInteractor;
import org.smartregister.chw.hf.model.FamilyProfileModel;
import org.smartregister.chw.hf.model.PmtctFollowupFeedbackModel;
import org.smartregister.chw.hf.presenter.FamilyOtherMemberActivityPresenter;
import org.smartregister.chw.hf.presenter.PmtctProfilePresenter;
import org.smartregister.chw.hf.utils.HfHomeVisitUtil;
import org.smartregister.chw.hf.utils.LFTUFormUtils;
import org.smartregister.chw.hf.utils.PmtctVisitUtils;
import org.smartregister.chw.hf.utils.TimeUtils;
import org.smartregister.chw.hiv.dao.HivDao;
import org.smartregister.chw.hiv.domain.HivMemberObject;
import org.smartregister.chw.hivst.dao.HivstDao;
import org.smartregister.chw.pmtct.PmtctLibrary;
import org.smartregister.chw.pmtct.dao.PmtctDao;
import org.smartregister.chw.pmtct.domain.Visit;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.chw.referral.util.JsonFormConstants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.dao.LocationsDao;
import org.smartregister.domain.AlertStatus;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.interactor.FamilyProfileInteractor;
import org.smartregister.family.model.BaseFamilyOtherMemberProfileActivityModel;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.repository.AllSharedPreferences;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
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
        refreshMedicalHistory(true);
        setupViews();
        org.smartregister.util.Utils.startAsyncTask(new UpdateVisitDueTask(), null);
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
        menu.findItem(R.id.action_remove_member).setVisible(true);
        if (HealthFacilityApplication.getApplicationFlavor().hasHivst()) {
            int age = memberObject.getAge();
            menu.findItem(R.id.action_hivst_registration).setVisible(HivstDao.isRegisteredForHivst(baseEntityId) && age >= 15);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        try {
            if (itemId == R.id.action_remove_member) {
                removeMember();
                return true;
            } else if (itemId == R.id.action_issue_pmtct_followup_referral) {
                JSONObject formJsonObject = (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, CoreConstants.JSON_FORM.getPmtctcCommunityFollowupReferral());
                //adds the chw locations under the current facility
                JSONObject motherChampionLocationField = CoreJsonFormUtils.getJsonField(formJsonObject, org.smartregister.util.JsonFormUtils.STEP1, "mother_champion_location");
                CoreJsonFormUtils.addLocationsToDropdownField(LocationsDao.getLocationsByTags(
                        Collections.singleton(SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS)), motherChampionLocationField);

                Date lastVisitDate;
                if (followUpVisitDate != null) {
                    lastVisitDate = followUpVisitDate;
                } else {
                    lastVisitDate = pmtctRegisterDate;
                }
                CoreJsonFormUtils.getJsonField(formJsonObject, STEP1, "last_client_visit_date").put(VALUE, sdf.format(lastVisitDate));

                startFormActivity(formJsonObject);
                return true;
            } else if (itemId == org.smartregister.chw.core.R.id.action_location_info) {
                JSONObject preFilledForm = getAutoPopulatedJsonEditFormString(
                        CoreConstants.JSON_FORM.getFamilyDetailsRegister(), this,
                        UpdateDetailsUtil.getFamilyRegistrationDetails(memberObject.getFamilyBaseEntityId()), Utils.metadata().familyRegister.updateEventType);
                if (preFilledForm != null)
                    UpdateDetailsUtil.startUpdateClientDetailsActivity(preFilledForm, this);
                return true;
            } else if (itemId == org.smartregister.chw.core.R.id.action_hivst_registration) {
                startHivstRegistration();
                return true;
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return super.onOptionsItemSelected(item);
    }

    private void startHivstRegistration() {
        CommonPersonObjectClient commonPersonObjectClient = getClientDetailsByBaseEntityID(baseEntityId);
        String gender = org.smartregister.chw.core.utils.Utils.getValue(commonPersonObjectClient.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.GENDER, false);
        HivstRegisterActivity.startHivstRegistrationActivity(this, baseEntityId, gender);
    }


    @SuppressLint("MissingSuperCall")
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
                    Toast.makeText(this, "EAC Visit saved", Toast.LENGTH_SHORT).show();
                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(org.smartregister.chw.core.utils.Utils.metadata().familyRegister.updateEventType)) {
                    FamilyEventClient familyEventClient = new CoreAllClientsMemberModel().processJsonForm(jsonString, memberObject.getFamilyBaseEntityId());
                    JSONObject syncLocationField = CoreJsonFormUtils.getJsonField(new JSONObject(jsonString), org.smartregister.util.JsonFormUtils.STEP1, SYNC_LOCATION_ID);
                    familyEventClient.getEvent().setLocationId(CoreJsonFormUtils.getSyncLocationUUIDFromDropdown(syncLocationField));
                    familyEventClient.getEvent().setEntityType(CoreConstants.TABLE_NAME.INDEPENDENT_CLIENT);
                    new FamilyProfileInteractor().saveRegistration(familyEventClient, jsonString, true, (FamilyProfileContract.InteractorCallBack) presenter());
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
                case R.id.hiv_fab:
                    ((CorePmtctFloatingMenu) basePmtctFloatingMenu).animateFAB();
                    break;
                case R.id.call_layout:
                    ((CorePmtctFloatingMenu) basePmtctFloatingMenu).launchCallWidget();
                    ((CorePmtctFloatingMenu) basePmtctFloatingMenu).animateFAB();
                    break;
                case R.id.refer_to_facility_layout:
                    LFTUFormUtils.startLTFUReferral(this, memberObject.getBaseEntityId());
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
            Date lastEac = HfPmtctDao.getDateEACRecorded(baseEntityId);
            RelativeLayout eacVisitDoneBar = findViewById(R.id.eac_visit_done_bar);
            TextView eacVisitDoneText = findViewById(R.id.textview_eac_visit_done);
            if (lastEac != null) {
                int days = TimeUtils.getElapsedDays(lastEac);
                if (days < 1) {
                    textViewRecordEac.setVisibility(View.GONE);
                    eacVisitDoneBar.setVisibility(View.VISIBLE);
                    eacVisitDoneText.setText(getString(R.string.eac_visit_done, HfPmtctDao.getEacSessionNumber(baseEntityId) - 1));
                } else {
                    eacVisitDoneBar.setVisibility(View.GONE);
                    textViewRecordEac.setVisibility(View.VISIBLE);
                }
            } else {
                eacVisitDoneBar.setVisibility(View.GONE);
                textViewRecordEac.setVisibility(View.VISIBLE);
            }
            textViewRecordEac.setOnClickListener(this);
            textViewRecordEac.setText(getString(R.string.record_eac_first_visit, HfPmtctDao.getEacSessionNumber(baseEntityId)));
        } else {
            textViewRecordEac.setVisibility(View.GONE);
        }
        Visit lastFollowupVisit = getVisit(Constants.EVENT_TYPE.PMTCT_FOLLOWUP);
        if (lastFollowupVisit != null && !lastFollowupVisit.getProcessed()) {
            if (PmtctVisitUtils.isVisitComplete(lastFollowupVisit)) {
                manualProcessVisit.setVisibility(View.VISIBLE);
                manualProcessVisit.setOnClickListener(view -> {
                    try {
                        PmtctVisitUtils.manualProcessVisit(lastFollowupVisit);
                        onResume();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                manualProcessVisit.setVisibility(View.GONE);
            }
            showVisitInProgress(org.smartregister.chw.hf.utils.Constants.Visits.PMTCT_VISIT);
            setUpEditButton();
        } else {
            manualProcessVisit.setVisibility(View.GONE);
            textViewVisitDoneEdit.setVisibility(View.GONE);
            visitDone.setVisibility(View.GONE);

            textViewRecordPmtct.setVisibility(View.VISIBLE);
            recordVisits.setVisibility(View.VISIBLE);
        }

        if (HfPmtctDao.hasTheClientTransferedOut(baseEntityId)) {
            showStatusLabel(R.string.transfer_out, org.smartregister.pmtct.R.drawable.medium_risk_label, org.smartregister.pmtct.R.color.medium_risk_text_orange);
        } else if (HfPmtctDao.isTheClientLostToFollowup(baseEntityId)) {
            showStatusLabel(R.string.lost_to_followup, org.smartregister.pmtct.R.drawable.high_risk_label, org.smartregister.pmtct.R.color.high_risk_text_red);
        }

        setClientCtcNumber(textViewClientRegNumber);
    }

    private void setClientCtcNumber(TextView tv) {
        HivMemberObject hivMemberObject = HivDao.getMember(memberObject.getBaseEntityId());
        if (hivMemberObject != null && StringUtils.isNotBlank(hivMemberObject.getCtcNumber())) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(hivMemberObject.getCtcNumber());
        }
    }

    @Override
    public void setProfileViewWithData() {
        super.setProfileViewWithData();
        TextView linkedMotherChampion = (TextView) findViewById(R.id.linked_to_mother_champion);
        if (HfPmtctDao.hasBeenReferredForMotherChampionServices(baseEntityId)) {
            //Checking if CTC Number is visible before showing an additional separator
            if (textViewClientRegNumber.getVisibility() == View.VISIBLE)
                findViewById(R.id.family_head_separator).setVisibility(View.VISIBLE);

            linkedMotherChampion.setVisibility(View.VISIBLE);
            linkedMotherChampion.setText(MessageFormat.format(getString(R.string.linked_to_mother_champion), HfPmtctDao.getLinkedMotherChampionLocation(baseEntityId)));
        } else {
            findViewById(R.id.family_head_separator).setVisibility(View.GONE);
            linkedMotherChampion.setVisibility(View.GONE);
        }
    }

    private void showStatusLabel(int stringResource, int backgroundResource, int textColorResource) {
        if (riskLabel != null) {
            riskLabel.setVisibility(View.VISIBLE);
            riskLabel.setTextSize(14);
            riskLabel.setText(stringResource);
            riskLabel.setBackgroundResource(backgroundResource);
            riskLabel.setTextColor(context().getColorResource(textColorResource));
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
        CommonPersonObjectClient commonPersonObjectClient = getClientDetailsByBaseEntityID(memberObject.getBaseEntityId());
        if (commonPersonObjectClient.getColumnmaps().get("entity_type").toString().equals(CoreConstants.TABLE_NAME.INDEPENDENT_CLIENT)) {
            commonPersonObjectClient.getColumnmaps().put(OpdDbConstants.KEY.REGISTER_TYPE, CoreConstants.REGISTER_TYPE.INDEPENDENT);
        }

        IndividualProfileRemoveActivity.startIndividualProfileActivity(this,
                commonPersonObjectClient,
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
            TextView textView = findViewById(R.id.textview_record_pmtct);
            if (textView.getText().equals(getResources().getString(R.string.record_ctc_number))) {
                try {
                    startUpdateCtcNumber(PmtctProfileActivity.this, baseEntityId);
                } catch (Exception e) {
                    Timber.e(e);
                }
            } else {
                PmtctFollowupVisitActivity.startPmtctFollowUpActivity(this, baseEntityId, false);
            }
        } else if (id == R.id.textview_record_eac) {
            JSONObject formJsonObject;
            try {
                formJsonObject = (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, org.smartregister.chw.hf.utils.Constants.JsonForm.getEacVisitsForm());
                if (formJsonObject != null) {
                    JSONObject global = formJsonObject.getJSONObject("global");
                    JSONArray fields = formJsonObject.getJSONObject(STEP1).getJSONArray(JsonFormConstants.FIELDS);
                    JSONObject eac_visit_session = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "eac_visit_session");

                    assert eac_visit_session != null;
                    eac_visit_session.put(VALUE, HfPmtctDao.getEacSessionNumber(baseEntityId));
                    global.put("eac_session_number", HfPmtctDao.getEacSessionNumber(baseEntityId));

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

    @Override
    public void refreshMedicalHistory(boolean hasHistory) {
        Visit lastFollowupVisit = getVisit(Constants.EVENT_TYPE.PMTCT_FOLLOWUP);
        if (lastFollowupVisit != null) {
            rlLastVisit.setVisibility(View.VISIBLE);
            findViewById(R.id.view_notification_and_referral_row).setVisibility(View.VISIBLE);
        } else {
            rlLastVisit.setVisibility(View.GONE);
        }
    }

    @Override
    public void openMedicalHistory() {
        PmtctMedicalHistoryActivity.startMe(this, memberObject);
    }

    private class UpdateVisitDueTask extends AsyncTask<Void, Void, Void> {
        private PmtctFollowUpRule pmtctFollowUpRule;

        @Override
        protected Void doInBackground(Void... voids) {
            pmtctRegisterDate = HfPmtctDao.getPmtctRegisterDate(memberObject.getBaseEntityId());
            followUpVisitDate = HfPmtctDao.getNextFacilityVisitDate(memberObject.getBaseEntityId());
            pmtctFollowUpRule = HfHomeVisitUtil.getPmtctVisitStatus(pmtctRegisterDate, followUpVisitDate, baseEntityId);
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            String visitStatus = pmtctFollowUpRule.getButtonStatus();
            PmtctProfileActivity.visitStatus = visitStatus;

            if (pmtctFollowUpRule.getButtonStatus().equals(CoreConstants.VISIT_STATE.NOT_DUE_YET))
                visitStatus = CoreConstants.VISIT_STATE.DUE;

            profilePresenter.recordPmtctButton(visitStatus);

            HivMemberObject hivMemberObject = HivDao.getMember(baseEntityId);
            String ctcNumber = null;
            String statusAfterTesting = null;
            if (hivMemberObject != null) {
                ctcNumber = hivMemberObject.getCtcNumber();
                statusAfterTesting = hivMemberObject.getClientHivStatusAfterTesting();
            }
            if (ctcNumber == null || ctcNumber.equals("") && statusAfterTesting != null && statusAfterTesting.equalsIgnoreCase("positive")) {
                textViewRecordPmtct.setText(R.string.record_ctc_number);
            } else if (HfPmtctDao.isNewClient(baseEntityId)) {
                textViewRecordPmtct.setText(R.string.record_pmtct_visit);
            } else {
                textViewRecordPmtct.setText(R.string.record_pmtct);
            }

            Visit lastFolllowUpVisit = getVisit(Constants.EVENT_TYPE.PMTCT_FOLLOWUP);

            if (lastFolllowUpVisit != null && lastFolllowUpVisit.getProcessed()) {
                profilePresenter.visitRow(visitStatus);
            }

            try {
                profilePresenter.nextRow(visitStatus, FpUtil.sdf.format(pmtctFollowUpRule.getDueDate()));
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }


}
