package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.core.utils.Utils.passToolbarTitle;
import static org.smartregister.chw.core.utils.Utils.updateToolbarTitle;
import static org.smartregister.chw.hf.utils.Constants.Events.ANC_FIRST_FACILITY_VISIT;
import static org.smartregister.chw.hf.utils.Constants.Events.ANC_RECURRING_FACILITY_VISIT;
import static org.smartregister.chw.hf.utils.Constants.PartnerRegistrationConstants.INTENT_BASE_ENTITY_ID;
import static org.smartregister.chw.hf.utils.JsonFormUtils.SYNC_LOCATION_ID;
import static org.smartregister.chw.hf.utils.JsonFormUtils.getAutoPopulatedJsonEditFormString;
import static org.smartregister.family.util.DBConstants.KEY.ENTITY_TYPE;
import static org.smartregister.util.JsonFormUtils.STEP1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rey.material.widget.Button;

import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.api.Rules;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.activity.CoreAncMemberProfileActivity;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.model.CoreAllClientsMemberModel;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.HomeVisitUtil;
import org.smartregister.chw.core.utils.UpdateDetailsUtil;
import org.smartregister.chw.core.utils.VisitSummary;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.ReferralCardViewAdapter;
import org.smartregister.chw.hf.dao.FamilyDao;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.hf.interactor.AncMemberProfileInteractor;
import org.smartregister.chw.hf.model.FamilyDetailsModel;
import org.smartregister.chw.hf.model.FamilyProfileModel;
import org.smartregister.chw.hf.presenter.AncMemberProfilePresenter;
import org.smartregister.chw.hf.utils.VisitUtils;
import org.smartregister.chw.hiv.dao.HivDao;
import org.smartregister.chw.hivst.dao.HivstDao;
import org.smartregister.chw.ld.dao.LDDao;
import org.smartregister.chw.pmtct.dao.PmtctDao;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.AlertStatus;
import org.smartregister.domain.Task;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.interactor.FamilyProfileInteractor;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import timber.log.Timber;

public class AncMemberProfileActivity extends CoreAncMemberProfileActivity {
    private CommonPersonObjectClient commonPersonObjectClient;
    private boolean hivPositive;
    private boolean isKnownOnArt;
    private String ctcNumber;
    private String partnerBaseEntityId;
    private RelativeLayout processVisitLayout;

    public static void startMe(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, AncMemberProfileActivity.class);
        passToolbarTitle(activity, intent);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.BASE_ENTITY_ID, baseEntityID);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        findViewById(R.id.record_visit_panel).setVisibility(View.VISIBLE);
        setCommonPersonObjectClient(getClientDetailsByBaseEntityID(baseEntityID));
    }

    @Override
    public void initializeFloatingMenu() {
        super.initializeFloatingMenu();
        if (baseAncFloatingMenu != null) {
            FloatingActionButton floatingActionButton = baseAncFloatingMenu.findViewById(R.id.anc_fab);
            if (floatingActionButton != null)
                floatingActionButton.setImageResource(R.drawable.floating_call);
        }
    }

    @Override
    protected void registerPresenter() {
        presenter = new AncMemberProfilePresenter(this, new AncMemberProfileInteractor(this), memberObject);
    }

    @Override
    public boolean usesPregnancyRiskProfileLayout() {
        return true;
    }

    @Override
    public void setUpComingServicesStatus(String service, AlertStatus status, Date date) {
        view_most_due_overdue_row.setVisibility(View.GONE);
        rlUpcomingServices.setVisibility(View.GONE);
    }

    @Override
    public void setFamilyStatus(AlertStatus status) {
        view_family_row.setVisibility(View.GONE);
        rlFamilyServicesDue.setVisibility(View.GONE);
    }

    @Override
    public void setFamilyLocation() {
        view_family_location_row.setVisibility(View.GONE);
        rlFamilyLocation.setVisibility(View.GONE);
    }

    private String getMemberGPS() {
        return memberObject.getGps();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.anc_danger_signs_outcome).setVisible(false);
        menu.findItem(R.id.action_anc_registration).setVisible(false);
        menu.findItem(R.id.action_remove_member).setVisible(true);
        menu.findItem(R.id.action_pregnancy_out_come).setVisible(!HfAncDao.isClientClosed(baseEntityID));
        menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);

        if (HealthFacilityApplication.getApplicationFlavor().hasHivst()) {
            int age = memberObject.getAge();
            menu.findItem(R.id.action_hivst_registration).setVisible(!HivstDao.isRegisteredForHivst(baseEntityID) && age >= 15);
        }

        if (HealthFacilityApplication.getApplicationFlavor().hasLD()) {
            menu.findItem(R.id.action_ld_registration).setVisible(memberObject.getGestationAge() >= 28 && !LDDao.isRegisteredForLD(baseEntityID));
        }

        if (!HfAncDao.hasReferredForPartnerCommunityFollowup(baseEntityID)) {
            partnerBaseEntityId = HfAncDao.getPartnerBaseEntityId(memberObject.getBaseEntityId());
            if (StringUtils.isBlank(partnerBaseEntityId)) {
                menu.findItem(R.id.action_anc_partner_followup_referral).setVisible(true);
            }
        } else {
            menu.findItem(R.id.action_anc_partner_followup_referral).setVisible(false);
        }
        menu.findItem(R.id.action_pmtct_register).setVisible(!PmtctDao.isRegisteredForPmtct(baseEntityID) && (hivPositive || HivDao.isRegisteredForHiv(baseEntityID) || HfAncDao.getHivStatus(baseEntityID).equalsIgnoreCase("positive")));
        return true;
    }

    @Override // to chw
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);
                String encounterType = form.getString(JsonFormUtils.ENCOUNTER_TYPE);
                if (encounterType.equals(Utils.metadata().familyMemberRegister.updateEventType)) {
                    FamilyEventClient familyEventClient =
                            new FamilyProfileModel(memberObject.getFamilyName()).processUpdateMemberRegistration(jsonString, memberObject.getBaseEntityId());
                    new FamilyProfileInteractor().saveRegistration(familyEventClient, jsonString, true, ancMemberProfilePresenter());
                } else if (encounterType.equals(CoreConstants.EventType.UPDATE_ANC_REGISTRATION)) {
                    AllSharedPreferences allSharedPreferences = org.smartregister.util.Utils.getAllSharedPreferences();
                    Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, Constants.TABLES.ANC_MEMBERS);
                    NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseEvent)));
                    AllCommonsRepository commonsRepository = CoreChwApplication.getInstance().getAllCommonsRepository(CoreConstants.TABLE_NAME.ANC_MEMBER);

                    JSONArray field = org.smartregister.util.JsonFormUtils.fields(form);
                    JSONObject phoneNumberObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(field, DBConstants.KEY.PHONE_NUMBER);
                    String phoneNumber = phoneNumberObject.getString(CoreJsonFormUtils.VALUE);
                    String baseEntityId = baseEvent.getBaseEntityId();
                    if (commonsRepository != null) {
                        ContentValues values = new ContentValues();
                        values.put(DBConstants.KEY.PHONE_NUMBER, phoneNumber);
                        CoreChwApplication.getInstance().getRepository().getWritableDatabase().update(CoreConstants.TABLE_NAME.ANC_MEMBER, values, DBConstants.KEY.BASE_ENTITY_ID + " = ?  ", new String[]{baseEntityId});
                    }
                } else if (encounterType.equals(CoreConstants.EventType.ANC_DANGER_SIGNS_OUTCOME)) {
                    ancMemberProfilePresenter().createAncDangerSignsOutcomeEvent(Utils.getAllSharedPreferences(), jsonString, baseEntityID);
                } else if (encounterType.equals(CoreConstants.EventType.ANC_PARTNER_COMMUNITY_FOLLOWUP_REFERRAL)) {
                    ((AncMemberProfilePresenter) presenter()).createPartnerFollowupReferralEvent(Utils.getAllSharedPreferences(), jsonString, baseEntityID);
                } else if (encounterType.equals(CoreConstants.EventType.ANC_PARTNER_TESTING)) {
                    ((AncMemberProfilePresenter) presenter()).savePartnerTestingEvent(Utils.getAllSharedPreferences(), jsonString, baseEntityID);
                    displayToast(R.string.recorded_partner_testing_results);
                    setupViews();
                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(org.smartregister.chw.core.utils.Utils.metadata().familyRegister.updateEventType)) {
                    FamilyEventClient familyEventClient = new CoreAllClientsMemberModel().processJsonForm(jsonString, UpdateDetailsUtil.getFamilyBaseEntityId(getCommonPersonObjectClient()));
                    JSONObject syncLocationField = CoreJsonFormUtils.getJsonField(new JSONObject(jsonString), STEP1, SYNC_LOCATION_ID);
                    familyEventClient.getEvent().setLocationId(CoreJsonFormUtils.getSyncLocationUUIDFromDropdown(syncLocationField));
                    familyEventClient.getEvent().setEntityType(CoreConstants.TABLE_NAME.INDEPENDENT_CLIENT);
                    new FamilyProfileInteractor().saveRegistration(familyEventClient, jsonString, true, (FamilyProfileContract.InteractorCallBack) ancMemberProfilePresenter());
                }
            } catch (Exception e) {
                Timber.e(e, "AncMemberProfileActivity -- > onActivityResult");
            }
        }
        invalidateOptionsMenu();
    }

    @Override
    public void startFormForEdit(Integer title_resource, String formName) {
        try {
            JSONObject form = CoreJsonFormUtils.getAncPncForm(title_resource, formName, memberObject, this);
            startActivityForResult(CoreJsonFormUtils.getAncPncStartFormIntent(form, this), JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void openMedicalHistory() {
        AncMedicalHistoryActivity.startMe(this, memberObject);
    }

    @Override
    public void openUpcomingService() {
        //// TODO: 29/08/19
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
    protected void startLDRegistration() {
        try {
            LDRegistrationFormActivity.startMe(this, baseEntityID, false, memberObject.getFullName(), String.valueOf(memberObject.getGestationAge()));
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void setupViews() {
        super.setupViews();
        updateToolbarTitle(this, org.smartregister.chw.core.R.id.toolbar_title, memberObject.getFamilyName());
        if (!HfAncDao.isClientClosed(baseEntityID)) {
            getButtonStatus();
            try {
                VisitUtils.processVisits();
            } catch (Exception e) {
                Timber.e(e);
            }
            Visit firstVisit = getVisit(ANC_FIRST_FACILITY_VISIT);
            Visit lastVisit = getVisit(ANC_RECURRING_FACILITY_VISIT);
            try {
                setHivPositive(firstVisit, lastVisit);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if ((firstVisit == null || HfAncDao.hasNoFollowups(baseEntityID)) && HfAncDao.getVisitNumber(baseEntityID) == 0) {
                textview_record_anc_visit.setText(R.string.record_anc_first_visit);
            } else {
                textview_record_anc_visit.setText(R.string.record_anc_followup_visit);
            }

            if (lastVisit == null) {
                if (firstVisit != null) {
                    checkVisitStatus(firstVisit);
                }
            } else {
                checkVisitStatus(lastVisit);
            }
        } else {
            layoutRecordView.setVisibility(View.GONE);
            pregnancyRiskLabel.setTextSize(12);
        }

        if (baseAncFloatingMenu != null) {
            FloatingActionButton floatingActionButton = baseAncFloatingMenu.findViewById(R.id.anc_fab);
            if (floatingActionButton != null)
                floatingActionButton.setImageResource(R.drawable.floating_call);
        }

        RelativeLayout partnerView = findViewById(R.id.rlPartnerView);
        RelativeLayout registrationDetails = findViewById(R.id.rlRegistrationDetails);
        RelativeLayout partnerTestingView = findViewById(R.id.rlPartnerTesting);
        RelativeLayout partnerTestingHistoryView = findViewById(R.id.rlPartnerTestingHistory);
        CustomFontTextView tvPartnerProfileView = findViewById(R.id.text_view_partner_profile);
        CustomFontTextView tvPartnerDetails = findViewById(R.id.partner_details);
        ImageView goToProfileBtn = findViewById(R.id.partner_arrow_image);
        Button registerBtn = findViewById(R.id.register_partner_btn);
        Button testingBtn = findViewById(R.id.test_partner_btn);
        View partnerTestingBottomView = findViewById(R.id.partner_testing_row);
        View partnerBottomView = findViewById(R.id.view_partner_row);

        findViewById(R.id.view_registration_details_row).setVisibility(View.VISIBLE);
        registrationDetails.setVisibility(View.VISIBLE);
        partnerView.setVisibility(View.VISIBLE);
        partnerBottomView.setVisibility(View.VISIBLE);
        partnerTestingHistoryView.setVisibility(View.VISIBLE);

        registrationDetails.setOnClickListener(this);
        partnerView.setOnClickListener(this);
        partnerTestingHistoryView.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
        testingBtn.setOnClickListener(this);
        partnerBaseEntityId = HfAncDao.getPartnerBaseEntityId(memberObject.getBaseEntityId());
        if (StringUtils.isNotBlank(partnerBaseEntityId)) {
            try {
                CommonPersonObjectClient partnerClient = getClientDetailsByBaseEntityID(partnerBaseEntityId);
                HashMap<String, String> clientDetails = (HashMap<String, String>) partnerClient.getColumnmaps();
                tvPartnerProfileView.setText(R.string.view_partner_profile);
                tvPartnerDetails.setVisibility(View.VISIBLE);
                registerBtn.setVisibility(View.GONE);
                goToProfileBtn.setVisibility(View.VISIBLE);
                tvPartnerDetails.setText(MessageFormat.format("{0} {1} {2}", clientDetails.get("first_name"), clientDetails.get("middle_name"), clientDetails.get("last_name") != null ? clientDetails.get("last_name") : ""));
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        boolean retestPartnerAt32 = ((memberObject.getGestationAge() >= 32 && HfAncDao.getPartnerHivStatus(memberObject.getBaseEntityId()).equalsIgnoreCase("negative")) && !HfAncDao.isPartnerHivTestConductedAtWk32(memberObject.getBaseEntityId()));
        boolean partnerTestedAll = (HfAncDao.isPartnerTestedForHiv(memberObject.getBaseEntityId()) && HfAncDao.isPartnerTestedForSyphilis(memberObject.getBaseEntityId()) && HfAncDao.isPartnerTestedForHepatitis(memberObject.getBaseEntityId()));
        // HfAncDao.getPartnerHivTestNumber(memberObject.getBaseEntityId()) == 0
        if (HfAncDao.isPartnerRegistered(memberObject.getBaseEntityId()) && (!partnerTestedAll || retestPartnerAt32)) {
            partnerTestingView.setVisibility(View.VISIBLE);
            partnerTestingBottomView.setVisibility(View.VISIBLE);
        } else {
            partnerTestingView.setVisibility(View.GONE);
            partnerTestingBottomView.setVisibility(View.GONE);
        }

        if (HfAncDao.isPartnerRegistered(memberObject.getBaseEntityId()) && (!HfAncDao.getPartnerOtherStdsStatus(memberObject.getBaseEntityId()).equalsIgnoreCase("null") || HfAncDao.isPartnerTestedForHiv(memberObject.getBaseEntityId()) || HfAncDao.isPartnerTestedForSyphilis(memberObject.getBaseEntityId()) || HfAncDao.isPartnerTestedForHepatitis(memberObject.getBaseEntityId()))) {
            partnerTestingHistoryView.setVisibility(View.VISIBLE);
        } else {
            partnerTestingHistoryView.setVisibility(View.GONE);
        }

        this.findViewById(R.id.family_anc_head).setVisibility(View.GONE);
        this.findViewById(R.id.primary_anc_caregiver).setVisibility(View.GONE);

    }

    private void setHivPositive(Visit firstVisit, Visit lastVisit) throws JSONException {
        hivPositive = false;
        ctcNumber = null;
        isKnownOnArt = false;
        JSONObject jsonObject = null;
        if (firstVisit != null) {
            jsonObject = new JSONObject(firstVisit.getJson());
        }
        if (lastVisit != null) {
            jsonObject = new JSONObject(lastVisit.getJson());
        }

        if (jsonObject != null) {
            JSONArray obs = jsonObject.getJSONArray("obs");
            int obsSize = obs.length();
            for (int i = 0; i < obsSize; i++) {
                JSONObject checkObj = obs.getJSONObject(i);
                if (checkObj.getString("fieldCode").equalsIgnoreCase("known_on_art") && checkObj.getString("values").contains("true")) {
                    hivPositive = true;
                    isKnownOnArt = true;
                } else if (checkObj.getString("fieldCode").equalsIgnoreCase("hiv")) {
                    JSONArray values = checkObj.getJSONArray("values");
                    if (values.getString(0).equalsIgnoreCase("positive")) {
                        hivPositive = true;
                    }
                } else if (checkObj.getString("fieldCode").equalsIgnoreCase("ctc_number")) {
                    JSONArray values = checkObj.getJSONArray("values");
                    ctcNumber = values.getString(0);
                }
            }
        }

    }

    private void checkVisitStatus(Visit visit) {
        processVisitLayout = findViewById(R.id.rlProcessVisitBtn);
        processVisitLayout.setVisibility(View.GONE);
        boolean visitDone = visit.getProcessed();
        boolean formsCompleted = VisitUtils.isAncVisitComplete(visit);
        if (!visitDone) {
            showVisitInProgress();
            textViewUndo.setVisibility(View.GONE);
            textViewAncVisitNot.setVisibility(View.GONE);
            if (formsCompleted) {
                showCompleteVisit(visit);
            }
        } else {
            getButtonStatus();
        }
    }

    private void showVisitInProgress() {
        if (layoutRecordView != null)
            layoutRecordView.setVisibility(View.GONE);

        if (tvEdit != null)
            tvEdit.setVisibility(View.VISIBLE);

        if (layoutNotRecordView != null)
            layoutNotRecordView.setVisibility(View.VISIBLE);

        if (textViewNotVisitMonth != null)
            textViewNotVisitMonth.setText(getContext().getString(R.string.visit_in_progress, "ANC"));

        if (imageViewCross != null)
            imageViewCross.setImageResource(R.drawable.activityrow_visit_in_progress);
    }

    private void showCompleteVisit(Visit visit) {
        TextView processVisitBtn = findViewById(R.id.textview_process_visit);
        processVisitBtn.setOnClickListener(v -> {
            try {
                VisitUtils.manualProcessVisit(visit, AncMemberProfileActivity.this);
                //reload views after visit is processed
                setupViews();
                presenter().refreshProfileBottom();
                if (!baseEntityID.isEmpty()) {
                    memberObject = getMemberObject(baseEntityID);
                    ((AncMemberProfilePresenter) presenter()).refreshProfileTopSection(memberObject);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        });
        processVisitLayout.setVisibility(View.VISIBLE);
    }


    @Override
    public void setClientTasks(Set<Task> taskList) {
        if (notificationAndReferralRecyclerView != null && taskList.size() > 0) {
            RecyclerView.Adapter mAdapter = new ReferralCardViewAdapter(taskList, this, getCommonPersonObjectClient(), CoreConstants.REGISTERED_ACTIVITIES.ANC_REGISTER_ACTIVITY);
            notificationAndReferralRecyclerView.setAdapter(mAdapter);
            notificationAndReferralLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.view_notification_and_referral_row).setVisibility(View.VISIBLE);
        }
    }

    public CommonPersonObjectClient getCommonPersonObjectClient() {
        return commonPersonObjectClient;
    }

    public void setCommonPersonObjectClient(CommonPersonObjectClient commonPersonObjectClient) {
        this.commonPersonObjectClient = commonPersonObjectClient;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        // implemented but not used.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeNotificationReferralRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupViews();
        if (!baseEntityID.isEmpty()) {
            memberObject = getMemberObject(baseEntityID);
            ((AncMemberProfilePresenter) presenter()).refreshProfileTopSection(memberObject);
        }
        ancMemberProfilePresenter().fetchTasks();
        if (notificationAndReferralRecyclerView != null && notificationAndReferralRecyclerView.getAdapter() != null) {
            notificationAndReferralRecyclerView.getAdapter().notifyDataSetChanged();
        }
        setMemberGA(memberObject.getGestationAge() + "");
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.textview_record_visit || id == R.id.textview_record_reccuring_visit) {
            Visit firstVisit = getVisit(ANC_FIRST_FACILITY_VISIT);
            if ((firstVisit == null || HfAncDao.hasNoFollowups(baseEntityID)) && HfAncDao.getVisitNumber(baseEntityID) == 0) {
                AncFirstFacilityVisitActivity.startMe(this, memberObject.getBaseEntityId(), false);
            } else {
                AncRecurringFacilityVisitActivity.startMe(this, memberObject.getBaseEntityId(), false);
            }
        } else if (id == R.id.textview_edit) {
            Visit lastVisit = getVisit(ANC_RECURRING_FACILITY_VISIT);
            if (lastVisit == null)
                AncFirstFacilityVisitActivity.startMe(this, memberObject.getBaseEntityId(), true);
            else
                AncRecurringFacilityVisitActivity.startMe(this, memberObject.getBaseEntityId(), true);
        } else if (id == R.id.rlPartnerView || id == R.id.register_partner_btn) {
            if (StringUtils.isNotBlank(partnerBaseEntityId)) {
                FamilyDetailsModel familyDetailsModel = FamilyDao.getFamilyDetail(partnerBaseEntityId);

                CommonPersonObjectClient commonPersonObjectClient = getClientDetailsByBaseEntityID(partnerBaseEntityId);
                commonPersonObjectClient.setDetails(commonPersonObjectClient.getColumnmaps());
                String entityType = commonPersonObjectClient.getColumnmaps().get(ENTITY_TYPE);
                Intent intent;
                if (CoreConstants.TABLE_NAME.INDEPENDENT_CLIENT.equals(entityType)) {
                    intent = new Intent(this, AllClientsMemberProfileActivity.class);
                } else {
                    intent = new Intent(this, FamilyOtherMemberProfileActivity.class);
                }
                intent.putExtras(new Bundle());
                intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID, partnerBaseEntityId);
                intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, familyDetailsModel.getBaseEntityId());
                intent.putExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON, commonPersonObjectClient);
                intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_HEAD, familyDetailsModel.getFamilyHead());
                intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.PRIMARY_CAREGIVER, familyDetailsModel.getPrimaryCareGiver());
                intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_NAME, familyDetailsModel.getFamilyName());
                intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.VILLAGE_TOWN, familyDetailsModel.getVillageTown());
                intent.putExtra(CoreConstants.INTENT_KEY.TOOLBAR_TITLE, String.format(getString(R.string.return_to_anc_profile), memberObject.getFirstName()));
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, PartnerRegistrationActivity.class);
                intent.putExtra(INTENT_BASE_ENTITY_ID, memberObject.getBaseEntityId());
                startActivity(intent);
                setupViews();
            }
        } else if (id == R.id.rlPartnerTestingHistory) {
            AncPartnerTestingHistoryActivity.startMe(this, memberObject);
        } else if (id == R.id.test_partner_btn) {
            ((AncMemberProfilePresenter) presenter()).startPartnerTestingForm(memberObject);
        } else if (id == R.id.rlRegistrationDetails) {
            AncRegistrationDetailsActivity.startMe(this, memberObject);
        }
    }

    private void getButtonStatus() {
        openVisitMonthView();
        textViewUndo.setVisibility(View.GONE);
        boolean pmtctPendingRegistration = HfAncDao.getHivStatus(baseEntityID).equalsIgnoreCase("positive") && !PmtctDao.isRegisteredForPmtct(baseEntityID);

        Rules rules = CoreChwApplication.getInstance().getRulesEngineHelper().rules(CoreConstants.RULE_FILE.ANC_HOME_VISIT);
        Visit lastNotDoneVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(baseEntityID, org.smartregister.chw.anc.util.Constants.EVENT_TYPE.ANC_HOME_VISIT_NOT_DONE);
        if (lastNotDoneVisit != null) {
            Visit lastNotDoneVisitUndo = AncLibrary.getInstance().visitRepository().getLatestVisit(baseEntityID, org.smartregister.chw.anc.util.Constants.EVENT_TYPE.ANC_HOME_VISIT_NOT_DONE_UNDO);
            if (lastNotDoneVisitUndo != null
                    && lastNotDoneVisitUndo.getDate().after(lastNotDoneVisit.getDate())) {
                lastNotDoneVisit = null;
            }
        }
        Visit latestVisit = getVisit(ANC_RECURRING_FACILITY_VISIT);
        if (latestVisit == null)
            latestVisit = getVisit(ANC_FIRST_FACILITY_VISIT);
        String visitDate = latestVisit != null ? new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(latestVisit.getDate()) : null;
        String lastVisitNotDone = lastNotDoneVisit != null ? new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(lastNotDoneVisit.getDate()) : null;

        VisitSummary visitSummary = HomeVisitUtil.getAncVisitStatus(this, rules, visitDate, lastVisitNotDone, getDateCreated());

        String visitStatus = visitSummary.getVisitStatus();
        String monthString = null;
        if (latestVisit != null) {
            Calendar cal = Calendar.getInstance();
            int offset = cal.getTimeZone().getOffset(cal.getTimeInMillis());

            Long longDate = latestVisit.getDate().getTime();
            Date date = new Date(longDate - (long) offset);
            monthString = (String) DateFormat.format("MMMM", date);
        }


        if (visitStatus.equalsIgnoreCase(CoreConstants.VISIT_STATE.OVERDUE) && !pmtctPendingRegistration) {
            textview_record_anc_visit.setBackgroundResource(org.smartregister.chw.core.R.drawable.record_btn_selector_overdue);
            getLayoutVisibility();
        } else if ((visitStatus.equalsIgnoreCase(CoreConstants.VISIT_STATE.DUE) || visitStatus.equalsIgnoreCase("VISIT_THIS_MONTH")) && !pmtctPendingRegistration) {
            textview_record_anc_visit.setBackgroundResource(org.smartregister.chw.core.R.drawable.record_btn_anc_selector);
            getLayoutVisibility();
        } else if (visitStatus.equalsIgnoreCase(CoreConstants.VISIT_STATE.NOT_VISIT_THIS_MONTH) && !pmtctPendingRegistration) {
            textViewUndo.setVisibility(View.VISIBLE);
            textViewUndo.setText(getString(org.smartregister.chw.opensrp_chw_anc.R.string.undo));
            record_reccuringvisit_done_bar.setVisibility(View.GONE);
            openVisitMonthView();
        } else if (visitStatus.equalsIgnoreCase("LESS_TWENTY_FOUR") && !pmtctPendingRegistration) {
            layoutNotRecordView.setVisibility(View.VISIBLE);
            textViewNotVisitMonth.setText(getContext().getString(org.smartregister.chw.core.R.string.anc_visit_done, monthString));
            tvEdit.setVisibility(View.GONE);
            imageViewCross.setImageResource(org.smartregister.chw.core.R.drawable.activityrow_visited);
        } else if (pmtctPendingRegistration) {
            layoutNotRecordView.setVisibility(View.VISIBLE);
            textViewNotVisitMonth.setText(getContext().getString(R.string.pmtct_pending_registration));
            tvEdit.setText(getContext().getString(R.string.register_button_text));
            tvEdit.setVisibility(View.VISIBLE);
            tvEdit.setOnClickListener(v -> startPmtctRegistration());
            imageViewCross.setImageResource(org.smartregister.chw.core.R.drawable.activityrow_notvisited);
        }
    }

    @Override
    public void setPregnancyRiskLabel(String pregnancyRiskLevel) {
        if (pregnancyRiskLabel != null && StringUtils.isNotBlank(pregnancyRiskLevel)) {
            int labelTextColor;
            int background;
            String labelText;
            switch (pregnancyRiskLevel) {
                case Constants.HOME_VISIT.PREGNANCY_RISK_LOW:
                    labelTextColor = context().getColorResource(org.smartregister.chw.opensrp_chw_anc.R.color.low_risk_text_green);
                    background = org.smartregister.chw.opensrp_chw_anc.R.drawable.low_risk_label;
                    labelText = getContext().getString(org.smartregister.chw.opensrp_chw_anc.R.string.low_pregnancy_risk);
                    break;
                case Constants.HOME_VISIT.PREGNANCY_RISK_MEDIUM:
                    labelTextColor = context().getColorResource(org.smartregister.chw.opensrp_chw_anc.R.color.medium_risk_text_orange);
                    background = org.smartregister.chw.opensrp_chw_anc.R.drawable.medium_risk_label;
                    labelText = getContext().getString(org.smartregister.chw.opensrp_chw_anc.R.string.medium_pregnancy_risk);
                    break;
                case Constants.HOME_VISIT.PREGNANCY_RISK_HIGH:
                    labelTextColor = context().getColorResource(org.smartregister.chw.opensrp_chw_anc.R.color.high_risk_text_red);
                    background = org.smartregister.chw.opensrp_chw_anc.R.drawable.high_risk_label;
                    labelText = getContext().getString(org.smartregister.chw.opensrp_chw_anc.R.string.high_pregnancy_risk);
                    break;
                case org.smartregister.chw.hf.utils.Constants.Visits.TERMINATED:
                    labelTextColor = context().getColorResource(org.smartregister.chw.opensrp_chw_anc.R.color.high_risk_text_red);
                    background = org.smartregister.chw.opensrp_chw_anc.R.drawable.high_risk_label;
                    labelText = "Services Ended";
                    break;
                default:
                    labelTextColor = context().getColorResource(org.smartregister.chw.opensrp_chw_anc.R.color.default_risk_text_black);
                    background = org.smartregister.chw.opensrp_chw_anc.R.drawable.risk_label;
                    labelText = getContext().getString(org.smartregister.chw.opensrp_chw_anc.R.string.low_pregnancy_risk);
                    break;
            }
            pregnancyRiskLabel.setVisibility(View.GONE);
            pregnancyRiskLabel.setText(labelText);
            pregnancyRiskLabel.setTextColor(labelTextColor);
            pregnancyRiskLabel.setBackgroundResource(background);
        }
    }

    @Override
    protected void displayView() {
        Visit lastAncHomeVisitNotDoneEvent = getVisit(Constants.EVENT_TYPE.ANC_HOME_VISIT_NOT_DONE);
        Visit lastAncHomeVisitNotDoneUndoEvent = getVisit(Constants.EVENT_TYPE.ANC_HOME_VISIT_NOT_DONE_UNDO);

        if (lastAncHomeVisitNotDoneEvent != null && lastAncHomeVisitNotDoneUndoEvent != null &&
                lastAncHomeVisitNotDoneUndoEvent.getDate().before(lastAncHomeVisitNotDoneEvent.getDate())
                && ancHomeVisitNotDoneEvent(lastAncHomeVisitNotDoneEvent)) {
            setVisitViews();
        } else if (lastAncHomeVisitNotDoneUndoEvent == null && lastAncHomeVisitNotDoneEvent != null && ancHomeVisitNotDoneEvent(lastAncHomeVisitNotDoneEvent)) {
            setVisitViews();
        }
        Visit firstVisit = getVisit(ANC_FIRST_FACILITY_VISIT);
        Visit lastVisit = getVisit(ANC_RECURRING_FACILITY_VISIT);
        try {
            setHivPositive(firstVisit, lastVisit);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (lastVisit == null) {
            if (firstVisit != null) {
                checkVisitStatus(firstVisit);
            }
        } else {
            checkVisitStatus(lastVisit);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == org.smartregister.chw.core.R.id.action_pmtct_register) {
            startPmtctRegistration();
            return true;
        } else if (itemId == R.id.action_pregnancy_out_come) {
            CommonPersonObjectClient client = getCommonPersonObjectClient();
            String familyBaseEntityId = org.smartregister.util.Utils.getValue(client.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.RELATIONAL_ID, false);
            boolean motherHivStatus = hivPositive || HivDao.isRegisteredForHiv(baseEntityID) || isKnownOnArt || HfAncDao.isClientKnownOnArt(baseEntityID) || HfAncDao.getHivStatus(baseEntityID).equalsIgnoreCase("positive");

            PncRegisterActivity.startPncRegistrationActivity(AncMemberProfileActivity.this, memberObject.getBaseEntityId(), null, CoreConstants.JSON_FORM.getPregnancyOutcome(), AncLibrary.getInstance().getUniqueIdRepository().getNextUniqueId().getOpenmrsId(), familyBaseEntityId, memberObject.getFamilyName(), memberObject.getLastMenstrualPeriod(), motherHivStatus);
            return true;
        } else if (itemId == R.id.action_anc_partner_followup_referral) {
            ((AncMemberProfilePresenter) presenter()).startPartnerFollowupReferralForm(memberObject);
            return true;
        } else if (itemId == R.id.action_ld_registration) {
            startLDRegistration();
            return true;
        } else if (itemId == org.smartregister.chw.core.R.id.action_location_info) {

            JSONObject preFilledForm = getAutoPopulatedJsonEditFormString(
                    CoreConstants.JSON_FORM.getFamilyDetailsRegister(), this,
                    UpdateDetailsUtil.getFamilyRegistrationDetails(UpdateDetailsUtil.getFamilyBaseEntityId(getCommonPersonObjectClient())), Utils.metadata().familyRegister.updateEventType);
            if (preFilledForm != null)
                UpdateDetailsUtil.startUpdateClientDetailsActivity(preFilledForm, this);
            return true;
        } else if (itemId == R.id.action_hivst_registration) {
            startHivstRegistration();
            return true;
        } else if (itemId == org.smartregister.chw.core.R.id.action_remove_member) {
            removeMember();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void startPmtctRegistration() {
        try {
            if (HivDao.isRegisteredForHiv(baseEntityID) || (HfAncDao.getHivStatus(baseEntityID).equalsIgnoreCase("positive") && !HfAncDao.getClientCtcNumber(baseEntityID).equals("null"))) {
                String ctcNumber = HfAncDao.getClientCtcNumber(baseEntityID);
                if (ctcNumber.equals("null"))
                    ctcNumber = HivDao.getMember(baseEntityID).getCtcNumber();
                PmtctRegisterActivity.startPmtctRegistrationActivity(this, baseEntityID, ctcNumber, true);
            } else {
                PmtctRegisterActivity.startPmtctRegistrationActivity(this, baseEntityID, ctcNumber, isKnownOnArt || HfAncDao.isClientKnownOnArt(baseEntityID));
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void startHivstRegistration() {
        CommonRepository commonRepository = Utils.context().commonrepository(Utils.metadata().familyMemberRegister.tableName);

        final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(memberObject.getBaseEntityId());
        final CommonPersonObjectClient client =
                new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
        client.setColumnmaps(commonPersonObject.getColumnmaps());
        String gender = Utils.getValue(commonPersonObject.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.GENDER, false);
        HivstRegisterActivity.startHivstRegistrationActivity(this, baseEntityID, gender);
    }

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


}
