package org.smartregister.chw.hf.activity;

import static com.vijay.jsonwizard.constants.JsonFormConstants.VALUE;
import static org.smartregister.AllConstants.LocationConstants.SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS;
import static org.smartregister.chw.core.utils.Utils.getCommonPersonObjectClient;
import static org.smartregister.chw.core.utils.Utils.getDuration;
import static org.smartregister.chw.core.utils.Utils.updateToolbarTitle;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.getEditHeiNumber;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.getHeiNumberRegistration;
import static org.smartregister.client.utils.constants.JsonFormConstants.FIELDS;
import static org.smartregister.client.utils.constants.JsonFormConstants.STEP1;
import static org.smartregister.util.Utils.getName;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.vijay.jsonwizard.utils.FormUtils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.custom_views.CorePmtctFloatingMenu;
import org.smartregister.chw.core.interactor.CoreChildProfileInteractor;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.custom_view.PmtctFloatingMenu;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hf.interactor.HeiProfileInteractor;
import org.smartregister.chw.hf.model.FamilyProfileModel;
import org.smartregister.chw.hf.presenter.HeiProfilePresenter;
import org.smartregister.chw.hf.rule.HfHeiFollowupRule;
import org.smartregister.chw.hf.utils.HeiVisitUtils;
import org.smartregister.chw.hf.utils.HfChildUtils;
import org.smartregister.chw.hiv.dao.HivDao;
import org.smartregister.chw.hiv.domain.HivMemberObject;
import org.smartregister.chw.pmtct.PmtctLibrary;
import org.smartregister.chw.pmtct.activity.BasePmtctProfileActivity;
import org.smartregister.chw.pmtct.dao.PmtctDao;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.domain.Visit;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.chw.pmtct.util.PmtctUtil;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.dao.LocationsDao;
import org.smartregister.domain.AlertStatus;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.interactor.FamilyProfileInteractor;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import timber.log.Timber;

public class HeiProfileActivity extends BasePmtctProfileActivity {

    private static String baseEntityId;
    protected TextView textViewRecordHeiNumber;
    private SimpleDateFormat dayMonthYear = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private Date registerDate = HeiDao.getHeiRegisterDate(baseEntityId);
    private Date followUpVisitDate = HeiDao.getHeiFollowUpVisitDate(baseEntityId);
    private HfHeiFollowupRule heiFollowupRule = new HfHeiFollowupRule(registerDate, followUpVisitDate, baseEntityId);

    public static void startProfile(Activity activity, String baseEntityId) {
        HeiProfileActivity.baseEntityId = baseEntityId;
        Intent intent = new Intent(activity, HeiProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        activity.startActivity(intent);
    }

    protected static CommonPersonObjectClient getClientDetailsByBaseEntityID(@NonNull String baseEntityId) {
        return getCommonPersonObjectClient(baseEntityId);
    }


    @Override
    protected void onCreation() {
        super.onCreation();
        updateToolbarTitle(this, org.smartregister.chw.core.R.id.toolbar_title, memberObject.getFamilyName());
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        setupViews();
    }

    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        String baseEntityId = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
        memberObject = HeiDao.getMember(baseEntityId);
        profilePresenter = new HeiProfilePresenter(this, new HeiProfileInteractor(), memberObject);
        fetchProfileData();
        profilePresenter.refreshProfileBottom();
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        try {
            HeiVisitUtils.processVisits();
        } catch (Exception e) {
            Timber.e(e);
        }
        int defaultImage = org.smartregister.chw.core.R.drawable.rowavatar_child;
        ImageView imageViewProfile = findViewById(org.smartregister.chw.core.R.id.imageview_profile);
        imageViewProfile.setImageDrawable(getResources().getDrawable(defaultImage));
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.hei_toolbar_title);

        textViewRecordPmtct.setText(R.string.record_followup);
        textViewRecordPmtct.setOnClickListener(this);

        view_hvl_results_row.setVisibility(View.VISIBLE);
        if (HeiDao.hasHivResults(baseEntityId))
            rlHvlResults.setVisibility(View.VISIBLE);

        TextView tvHeiResultsTitle = findViewById(R.id.textview_hvl_results);
        TextView tvHeiResultsSubTitle = findViewById(R.id.tv_view_hvl_results);

        tvHeiResultsTitle.setText(R.string.hiv_test_results);
        tvHeiResultsSubTitle.setText(R.string.view_child_hiv_results);

        rlHvlResults.setOnClickListener(this);
        if (!HeiDao.hasTheChildTransferedOut(baseEntityId)) {
            showRiskLabel(HeiDao.getRiskLevel(baseEntityId));
        } else if (HeiDao.hasTheChildTransferedOut(baseEntityId)) {
            showStatusLabel(R.string.transfer_out, org.smartregister.pmtct.R.drawable.medium_risk_label, org.smartregister.pmtct.R.color.medium_risk_text_orange);
        } else if (HeiDao.isTheChildLostToFollowup(baseEntityId)) {
            showStatusLabel(R.string.lost_to_followup, org.smartregister.pmtct.R.drawable.high_risk_label, org.smartregister.pmtct.R.color.high_risk_text_red);
        }

        Visit lastFollowupVisit = getVisit(org.smartregister.chw.hf.utils.Constants.Events.HEI_FOLLOWUP);
        if (lastFollowupVisit != null && !lastFollowupVisit.getProcessed()) {
            manualProcessVisit.setVisibility(View.VISIBLE);
            manualProcessVisit.setOnClickListener(view -> {
                try {
                    HeiVisitUtils.manualProcessVisit(lastFollowupVisit);
                    onResume();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            showVisitInProgress();
            setUpEditButton();
        } else {
            if (shouldShowRecordFollowupVisitButton()) {
                textViewRecordPmtct.setVisibility(View.VISIBLE);
                visitDone.setVisibility(View.GONE);
            } else {
                textViewRecordPmtct.setVisibility(View.GONE);
                visitDone.setVisibility(View.VISIBLE);
            }
            manualProcessVisit.setVisibility(View.GONE);
        }

        Map<String, String> motherDetails = getMotherDetails();
        if (motherDetails != null) {
            showMotherDetails(motherDetails);
        }
        showHeiNumberOrRegistration(baseEntityId);
    }

    private void showMotherDetails(Map<String, String> motherDetails) {
        String firstName = getName(
                Utils.getValue(motherDetails, DBConstants.KEY.FIRST_NAME, true),
                Utils.getValue(motherDetails, DBConstants.KEY.MIDDLE_NAME, true));
        String fullName = getName(firstName, Utils.getValue(motherDetails, DBConstants.KEY.LAST_NAME, true));
        String dobString = Utils.getValue(motherDetails, DBConstants.KEY.DOB, false);
        int age = new Period(new DateTime(dobString), new DateTime()).getYears();
        textViewMotherName.setVisibility(View.VISIBLE);
        textViewMotherName.setText(String.format(Locale.getDefault(), "%s, %d", fullName, age));
    }

    private Map<String, String> getMotherDetails() {
        String motherBaseEntityId = HeiDao.getMotherBaseEntityId(baseEntityId);
        if (StringUtils.isNotBlank(motherBaseEntityId)) {
            CommonPersonObjectClient motherClient = getCommonPersonObjectClient(motherBaseEntityId);
            if (motherClient.getColumnmaps() != null) {
                return motherClient.getColumnmaps();
            }
        }
        return null;
    }

    private void showHeiNumberOrRegistration(String baseEntityId) {
        textViewRecordHeiNumber = findViewById(R.id.textview_record_hei_number);
        if (!HeiDao.hasHeiNumber(baseEntityId)) {
            textViewRecordHeiNumber.setVisibility(View.VISIBLE);
            textViewRecordHeiNumber.setText(getString(R.string.record_hei_number));
            textViewRecordHeiNumber.setOnClickListener(this);
            textViewRecordPmtct.setVisibility(View.GONE);
        } else {
            String heiNumber = HeiDao.getHeiNumber(baseEntityId);
            textViewRecordHeiNumber.setVisibility(View.GONE);
            if (heiNumber != null) {
                textViewClientRegNumber.setVisibility(View.VISIBLE);
                textViewClientRegNumber.setText(this.getString(R.string.hei_number, heiNumber));
            }
        }

    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.textview_record_pmtct) {
            HeiFollowupVisitActivity.startHeiFollowUpActivity(this, baseEntityId, false);
        }
        if (id == R.id.rlHvlResults) {
            Intent intent = new Intent(this, HeiHivResultsViewActivity.class);
            intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
            startActivity(intent);
        }
        if (id == R.id.textview_record_hei_number) {
            JSONObject jsonForm = org.smartregister.chw.core.utils.FormUtils.getFormUtils().getFormJson(getHeiNumberRegistration());

            try {
                JSONArray fields = jsonForm.getJSONObject(STEP1).getJSONArray(FIELDS);
                JSONObject heiNumber = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "hei_number");
                String motherBaseEntityId = HeiDao.getMotherBaseEntityId(baseEntityId);

                HivMemberObject hivMemberObject = HivDao.getMember(motherBaseEntityId);
                if (hivMemberObject != null && StringUtils.isNotBlank(hivMemberObject.getCtcNumber())) {
                    String motherCtcNumber = hivMemberObject.getCtcNumber();
                    String heiNumberMask = motherCtcNumber + "-C##";
                    heiNumber.put("mask", heiNumberMask);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            startFormActivity(jsonForm);
        }
    }

    private void editHeiNumber() {
        JSONObject jsonForm = org.smartregister.chw.core.utils.FormUtils.getFormUtils().getFormJson(getEditHeiNumber());
        try {
            JSONArray fields = jsonForm.getJSONObject(STEP1).getJSONArray(FIELDS);
            JSONObject heiNumberJsonField = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "hei_number");

            String motherBaseEntityId = HeiDao.getMotherBaseEntityId(baseEntityId);
            HivMemberObject hivMemberObject = HivDao.getMember(motherBaseEntityId);
            if (hivMemberObject != null && StringUtils.isNotBlank(hivMemberObject.getCtcNumber()) && heiNumberJsonField != null) {
                String motherCtcNumber = hivMemberObject.getCtcNumber();
                String heiNumberMask = motherCtcNumber + "-C##";
                heiNumberJsonField.put("mask", heiNumberMask);
            }

            JSONObject previousHeiNumberJsonField = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "previous_hei_number");
            String heiNumber = HeiDao.getHeiNumber(baseEntityId);
            if (previousHeiNumberJsonField != null) {
                previousHeiNumberJsonField.put(VALUE, heiNumber);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        startFormActivity(jsonForm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMedicalHistory(true);
        setupViews();
    }

    public @Nullable
    Visit getVisit(String eventType) {
        return PmtctLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), eventType);
    }

    private boolean shouldShowRecordFollowupVisitButton() {
        return HeiDao.isEligibleForDnaCprHivTest(memberObject.getBaseEntityId()) ||
                HeiDao.isEligibleForAntiBodiesHivTest(memberObject.getBaseEntityId()) ||
                HeiDao.isEligibleForCtx(memberObject.getBaseEntityId()) ||
                HeiDao.isEligibleForArvPrescriptionForHighRisk(memberObject.getBaseEntityId()) ||
                HeiDao.isEligibleForArvPrescriptionForHighAndLowRisk(memberObject.getBaseEntityId());
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

    private void showVisitInProgress() {
        recordVisits.setVisibility(View.GONE);
        textViewRecordPmtct.setVisibility(View.GONE);
        textViewVisitDoneEdit.setVisibility(View.VISIBLE);
        visitDone.setVisibility(View.VISIBLE);
        textViewVisitDone.setText(this.getString(R.string.visit_in_progress, org.smartregister.chw.hf.utils.Constants.Visits.HEI_VISIT));
        textViewVisitDone.setTextColor(getResources().getColor(R.color.black_text_color));
        imageViewCross.setImageResource(org.smartregister.chw.core.R.drawable.activityrow_notvisited);
    }

    private void setUpEditButton() {
        textViewVisitDoneEdit.setOnClickListener(v -> {
            HeiFollowupVisitActivity.startHeiFollowUpActivity(this, baseEntityId, true);
        });
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void setProfileViewWithData() {
        CommonPersonObjectClient client = getCommonPersonObjectClient(baseEntityId);
        String age = Utils.getTranslatedDate(getDuration(Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false)), this);
        textViewName.setText(String.format("%s %s %s, %s", memberObject.getFirstName(),
                memberObject.getMiddleName(), memberObject.getLastName(), age));
        textViewGender.setText(PmtctUtil.getGenderTranslated(this, memberObject.getGender()));
        textViewLocation.setText(memberObject.getAddress());
        textViewUniqueID.setText(memberObject.getUniqueId());
    }

    @Override
    public void initializeFloatingMenu() {
        MemberObject motherMemberObject = PmtctDao.getMember(HeiDao.getMotherBaseEntityId(baseEntityId));
        if (motherMemberObject != null) {
            basePmtctFloatingMenu = new PmtctFloatingMenu(this, motherMemberObject);
            checkPhoneNumberProvided(StringUtils.isNotBlank(motherMemberObject.getPhoneNumber()));
        } else {
            basePmtctFloatingMenu = new PmtctFloatingMenu(this, memberObject);
            checkPhoneNumberProvided(StringUtils.isNotBlank(memberObject.getPhoneNumber()));
        }

        OnClickFloatingMenu onClickFloatingMenu = viewId -> {
            switch (viewId) {
                case R.id.hiv_fab:
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

    private void checkPhoneNumberProvided(boolean hasPhoneNumber) {
        ((CorePmtctFloatingMenu) basePmtctFloatingMenu).redraw(hasPhoneNumber);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == org.smartregister.chw.core.R.id.action_registration) {
            startFormForEdit(org.smartregister.chw.core.R.string.registration_info,
                    CoreConstants.JSON_FORM.getChildRegister());
            return true;
        } else if (itemId == org.smartregister.chw.core.R.id.action_remove_member) {
            removeMember();
            return true;
        } else if (itemId == R.id.action_issue_pmtct_followup_referral) {
            try {
                JSONObject form = (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, org.smartregister.chw.hf.utils.Constants.JsonForm.getHeiCommunityFollowupReferral());

                //adds the chw locations under the current facility
                JSONObject motherChampionLocationField = CoreJsonFormUtils.getJsonField(form, org.smartregister.util.JsonFormUtils.STEP1, "mother_champion_location");
                CoreJsonFormUtils.addLocationsToDropdownField(LocationsDao.getLocationsByTags(
                        Collections.singleton(SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS)), motherChampionLocationField);

                JSONObject reasonsForIssuingCommunityReferral = CoreJsonFormUtils.getJsonField(form, STEP1, "reasons_for_issuing_community_referral");

                Date lastVisitDate;
                if (followUpVisitDate != null) {
                    lastVisitDate = followUpVisitDate;
                } else {
                    lastVisitDate = registerDate;
                }
                form.getJSONObject(STEP1).getJSONArray(FIELDS).getJSONObject(getJsonArrayIndex(form.getJSONObject(STEP1).getJSONArray(FIELDS), "last_client_visit_date")).put(VALUE, dayMonthYear.format(lastVisitDate));
                form.put(org.smartregister.util.JsonFormUtils.ENTITY_ID, HeiDao.getMotherBaseEntityId(baseEntityId));
                form.getJSONObject(STEP1).getJSONArray(FIELDS).getJSONObject(getJsonArrayIndex(form.getJSONObject(STEP1).getJSONArray(FIELDS), "child_name")).put(VALUE, memberObject.getFirstName() + " " + memberObject.getMiddleName() + " " + memberObject.getLastName());

                if (heiFollowupRule.getDatesDiff() >= 3 && heiFollowupRule.getDatesDiff() < 28)
                    reasonsForIssuingCommunityReferral.getJSONArray("options").remove(getJsonArrayIndex(reasonsForIssuingCommunityReferral.getJSONArray("options"), "lost_to_followup"));
                else if (heiFollowupRule.getDatesDiff() >= 28)
                    reasonsForIssuingCommunityReferral.getJSONArray("options").remove(getJsonArrayIndex(reasonsForIssuingCommunityReferral.getJSONArray("options"), "missed_appointment"));

                startFormActivity(form);

            } catch (JSONException e) {
                Timber.e(e);
            }
            return true;
        } else if (itemId == R.id.action_edit_hei_number) {
            editHeiNumber();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(org.smartregister.chw.core.R.menu.hei_profile_menu, menu);

        if (StringUtils.isNotBlank(HeiDao.getMotherBaseEntityId(baseEntityId)) && (heiFollowupRule.getButtonStatus().equals(CoreConstants.VISIT_STATE.DUE) || heiFollowupRule.getButtonStatus().equals(CoreConstants.VISIT_STATE.OVERDUE)) && heiFollowupRule.getDatesDiff() >= 3 && !HeiDao.hasTheChildTransferedOut(baseEntityId)) {
            menu.findItem(R.id.action_issue_pmtct_followup_referral).setVisible(true);
            menu.findItem(R.id.action_issue_pmtct_followup_referral).setTitle(R.string.issue_hei_community_referal);
        }

        menu.findItem(R.id.action_edit_hei_number).setVisible(HeiDao.hasHeiNumber(baseEntityId));

        menu.findItem(R.id.action_remove_member).setVisible(true);
        return true;
    }

    @Override
    public void openMedicalHistory() {
        HeiMedicalHistoryActivity.startMe(this, memberObject);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);
                String encounterType = form.getString(JsonFormUtils.ENCOUNTER_TYPE);
                if (encounterType.equals(org.smartregister.chw.hf.utils.Constants.Events.HEI_COMMUNITY_FOLLOWUP)) {
                    AllSharedPreferences allSharedPreferences = org.smartregister.util.Utils.getAllSharedPreferences();
                    ((HeiProfilePresenter) profilePresenter).createHeiCommunityFollowupReferralEvent(allSharedPreferences, data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON), HeiDao.getMotherBaseEntityId(baseEntityId));
                }
                if (encounterType.equals(org.smartregister.chw.hf.utils.Constants.Events.HEI_NUMBER_REGISTRATION) ||
                        encounterType.equals(org.smartregister.chw.hf.utils.Constants.Events.EDIT_HEI_NUMBER)) {
                    AllSharedPreferences allSharedPreferences = org.smartregister.util.Utils.getAllSharedPreferences();
                    ((HeiProfilePresenter) profilePresenter).createHeiNumberRegistrationEvent(allSharedPreferences, data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON), baseEntityId);
                }
                if (encounterType.equalsIgnoreCase(CoreConstants.EventType.UPDATE_CHILD_REGISTRATION)) {
                    String childBaseEntityId = memberObject.getBaseEntityId();
                    FamilyEventClient familyEventClient =
                            new FamilyProfileModel(memberObject.getFamilyName()).processUpdateMemberRegistration(jsonString, childBaseEntityId);
                    familyEventClient.getEvent().setEventType(CoreConstants.EventType.UPDATE_CHILD_REGISTRATION);
                    new FamilyProfileInteractor().saveRegistration(familyEventClient, jsonString, true, (FamilyProfileContract.InteractorCallBack) profilePresenter);

                }
            } catch (Exception e) {
                Timber.e(e, "HeiProfileActivity -- > onActivityResult");
            }
        }
    }

    protected void removeMember() {
        IndividualProfileRemoveActivity.startIndividualProfileActivity(this,
                getClientDetailsByBaseEntityID(memberObject.getBaseEntityId()),
                memberObject.getFamilyBaseEntityId(), memberObject.getFamilyHead(),
                memberObject.getPrimaryCareGiver(), FpRegisterActivity.class.getCanonicalName());
    }

    public void startFormForEdit(Integer title_resource, String formName) {

        JSONObject childEnrollmentForm = null;
        CommonPersonObjectClient client = HfChildUtils.getChildClientByBaseEntityId(memberObject.getBaseEntityId());

        if (client == null) {
            return;
        }
        if (formName.equals(CoreConstants.JSON_FORM.getChildRegister())) {
            CoreChildProfileInteractor childProfileInteractor = new CoreChildProfileInteractor();
            childEnrollmentForm = childProfileInteractor.getAutoPopulatedJsonEditFormString(CoreConstants.JSON_FORM.getChildRegister(), (title_resource != null) ? getResources().getString(title_resource) : null, this, client);
            String motherBaseEntityId = HeiDao.getMotherBaseEntityId(baseEntityId);
            if (StringUtils.isNotBlank(motherBaseEntityId)) {
                CommonPersonObjectClient mother = getCommonPersonObjectClient(HeiDao.getMotherBaseEntityId(baseEntityId));
                try {
                    Map<String, String> details = mother.getColumnmaps();
                    String famName = details.get(DBConstants.KEY.LAST_NAME);
                    JSONObject stepOne = childEnrollmentForm.getJSONObject(JsonFormUtils.STEP1);
                    JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);

                    Map<String, String> values = new HashMap<>();

                    assert famName != null;
                    values.put(CoreConstants.JsonAssets.FAM_NAME, famName);
                    org.smartregister.chw.core.utils.FormUtils.updateFormField(jsonArray, values);
                } catch (Exception e) {
                    Timber.e(e);
                }
            } else {
                try {
                    JSONObject stepOne = childEnrollmentForm.getJSONObject(JsonFormUtils.STEP1);
                    JSONArray fields = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                    JSONObject sameAsFamName = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "same_as_fam_name");
                    sameAsFamName.put("type", "hidden");
                } catch (Exception e) {
                    Timber.e(e);
                }
            }


        }
        try {
            assert childEnrollmentForm != null;
            startFormActivity(childEnrollmentForm);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = org.smartregister.chw.core.utils.Utils.formActivityIntent(this, jsonForm.toString());
        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    @Override
    public void refreshFamilyStatus(AlertStatus status) {
        super.refreshFamilyStatus(status);
        rlFamilyServicesDue.setVisibility(View.GONE);
    }

    @Override
    public void refreshMedicalHistory(boolean hasHistory) {
        Visit lastFollowupVisit = getVisit(org.smartregister.chw.hf.utils.Constants.Events.HEI_FOLLOWUP);
        if (lastFollowupVisit != null) {
            rlLastVisit.setVisibility(View.VISIBLE);
            TextView medicalHistoryTitle = findViewById(R.id.ivViewHistoryArrow);
            medicalHistoryTitle.setTextColor(getResources().getColor(R.color.black));
        } else {
            rlLastVisit.setVisibility(View.GONE);
        }
    }

    @Override
    public void refreshUpComingServicesStatus(String service, AlertStatus status, Date date) {
        super.refreshUpComingServicesStatus(service, status, date);
        rlUpcomingServices.setVisibility(View.GONE);
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
}
