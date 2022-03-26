package org.smartregister.chw.hf.activity;

import static com.vijay.jsonwizard.constants.JsonFormConstants.VALUE;
import static org.smartregister.chw.core.utils.Utils.getCommonPersonObjectClient;
import static org.smartregister.chw.core.utils.Utils.getDuration;
import static org.smartregister.chw.core.utils.Utils.updateToolbarTitle;
import static org.smartregister.client.utils.constants.JsonFormConstants.FIELDS;
import static org.smartregister.client.utils.constants.JsonFormConstants.STEP1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.custom_views.CorePmtctFloatingMenu;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.custom_view.PmtctFloatingMenu;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hf.interactor.HeiProfileInteractor;
import org.smartregister.chw.hf.presenter.HeiProfilePresenter;
import org.smartregister.chw.hf.utils.HeiVisitUtils;
import org.smartregister.chw.pmtct.activity.BasePmtctProfileActivity;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.chw.pmtct.util.PmtctUtil;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.AlertStatus;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class HeiProfileActivity extends BasePmtctProfileActivity {

    private static String baseEntityId;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    public static void startProfile(Activity activity, String baseEntityId) {
        HeiProfileActivity.baseEntityId = baseEntityId;
        Intent intent = new Intent(activity, HeiProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        activity.startActivity(intent);
    }

    protected static CommonPersonObjectClient getClientDetailsByBaseEntityID(@NonNull String baseEntityId) {
        return getCommonPersonObjectClient(baseEntityId);
    }

    @SuppressLint("LogNotTimber")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateToolbarTitle(this, org.smartregister.chw.core.R.id.toolbar_title, memberObject.getFamilyName());
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
        rlHvlResults.setVisibility(View.VISIBLE);

        TextView tvHeiResultsTitle = findViewById(R.id.textview_hvl_results);
        TextView tvHeiResultsSubTitle = findViewById(R.id.tv_view_hvl_results);

        tvHeiResultsTitle.setText(R.string.hiv_test_results);
        tvHeiResultsSubTitle.setText(R.string.view_child_hiv_results);

        rlHvlResults.setOnClickListener(this);
        if (!HeiDao.hasTheChildTransferedOut(baseEntityId)) {
            showRiskLabel(HeiDao.getRiskLevel(baseEntityId));
        } else {
            showTransferOutLabel();
        }

        if (shouldShowRecordFollowupVisitButton()) {
            textViewRecordPmtct.setVisibility(View.VISIBLE);
            visitDone.setVisibility(View.GONE);
        } else {
            textViewRecordPmtct.setVisibility(View.GONE);
            visitDone.setVisibility(View.VISIBLE);
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
    }

    private boolean shouldShowRecordFollowupVisitButton() {
        return HeiDao.isEligibleForDnaCprHivTest(memberObject.getBaseEntityId()) ||
                HeiDao.isEligibleForAntiBodiesHivTest(memberObject.getBaseEntityId()) ||
                HeiDao.isEligibleForCtx(memberObject.getBaseEntityId()) ||
                HeiDao.isEligibleForArvPrescriptionForHighRisk(memberObject.getBaseEntityId()) ||
                HeiDao.isEligibleForArvPrescriptionForHighAndLowRisk(memberObject.getBaseEntityId());
    }

    private void showTransferOutLabel() {
        if (riskLabel != null) {
            riskLabel.setVisibility(View.VISIBLE);
            riskLabel.setTextSize(14);
            riskLabel.setText(R.string.transfer_out);
            riskLabel.setTextColor(context().getColorResource(org.smartregister.pmtct.R.color.medium_risk_text_orange));
            riskLabel.setBackgroundResource(org.smartregister.pmtct.R.drawable.medium_risk_label);
        }
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

        if (StringUtils.isNotBlank(memberObject.getFamilyHead()) && memberObject.getFamilyHead().equals(memberObject.getBaseEntityId())) {
            findViewById(org.smartregister.pmtct.R.id.family_malaria_head).setVisibility(View.VISIBLE);
        }
        if (StringUtils.isNotBlank(memberObject.getPrimaryCareGiver()) && memberObject.getPrimaryCareGiver().equals(memberObject.getBaseEntityId())) {
            findViewById(org.smartregister.pmtct.R.id.primary_malaria_caregiver).setVisibility(View.VISIBLE);
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
                Date followUpVisitDate = HeiDao.getHeiFollowUpVisitDate(baseEntityId);
                Date registerDate = HeiDao.getHeiRegisterDate(baseEntityId);

                Date lastVisitDate;
                if (followUpVisitDate != null) {
                    lastVisitDate = followUpVisitDate;
                } else {
                    lastVisitDate = registerDate;
                }
                form.getJSONObject(STEP1).getJSONArray(FIELDS).getJSONObject(getJsonArrayIndex(form.getJSONObject(STEP1).getJSONArray(FIELDS), "last_client_visit_date")).put(VALUE, sdf.format(lastVisitDate));
                form.put(org.smartregister.util.JsonFormUtils.ENTITY_ID, HeiDao.getMotherBaseEntityId(baseEntityId));
                form.getJSONObject(STEP1).getJSONArray(FIELDS).getJSONObject(getJsonArrayIndex(form.getJSONObject(STEP1).getJSONArray(FIELDS), "child_name")).put(VALUE, memberObject.getFirstName() + " " + memberObject.getMiddleName() + " " + memberObject.getLastName());

                startFormActivity(form);

            } catch (JSONException e) {
                Timber.e(e);
            }
            return true;
        } else if (itemId == R.id.action_mark_as_deceased) {
            removeMember();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(org.smartregister.chw.core.R.menu.hei_profile_menu, menu);
        menu.findItem(R.id.action_issue_pmtct_followup_referral).setVisible(true);
        menu.findItem(R.id.action_issue_pmtct_followup_referral).setTitle(R.string.issue_hei_community_referal);
        menu.findItem(R.id.action_remove_member).setVisible(false);
        return true;
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

        JSONObject form = null;
        CommonPersonObjectClient client = org.smartregister.chw.core.utils.Utils.clientForEdit(memberObject.getBaseEntityId());

        if (formName.equals(CoreConstants.JSON_FORM.getChildRegister())) {
            form = CoreJsonFormUtils.getAutoPopulatedJsonEditMemberFormString(
                    (title_resource != null) ? getResources().getString(title_resource) : null,
                    CoreConstants.JSON_FORM.getChildRegister(),
                    this, client,
                    CoreConstants.EventType.UPDATE_CHILD_REGISTRATION, memberObject.getLastName(), false);
        }
        try {
            assert form != null;
            startFormActivity(form);
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
        super.refreshMedicalHistory(hasHistory);
        rlLastVisit.setVisibility(View.GONE);
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
