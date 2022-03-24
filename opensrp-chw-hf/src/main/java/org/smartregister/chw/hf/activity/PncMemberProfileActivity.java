package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vijay.jsonwizard.utils.FormUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CorePncMemberProfileActivity;
import org.smartregister.chw.core.activity.CorePncRegisterActivity;
import org.smartregister.chw.core.dao.ChwNotificationDao;
import org.smartregister.chw.core.interactor.CorePncMemberProfileInteractor;
import org.smartregister.chw.core.model.ChildModel;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.fp.dao.FpDao;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.ChildFollowupViewAdapter;
import org.smartregister.chw.hf.adapter.ReferralCardViewAdapter;
import org.smartregister.chw.hf.contract.PncMemberProfileContract;
import org.smartregister.chw.hf.dao.HfPncDao;
import org.smartregister.chw.hf.interactor.PncMemberProfileInteractor;
import org.smartregister.chw.hf.model.FamilyProfileModel;
import org.smartregister.chw.hf.presenter.PncMemberProfilePresenter;
import org.smartregister.chw.hf.utils.PncVisitUtils;
import org.smartregister.chw.malaria.dao.MalariaDao;
import org.smartregister.chw.pmtct.dao.PmtctDao;
import org.smartregister.chw.pmtct.util.NCUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.AlertStatus;
import org.smartregister.domain.Task;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.interactor.FamilyProfileInteractor;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;

import java.util.Date;
import java.util.List;
import java.util.Set;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import static org.smartregister.chw.core.utils.Utils.passToolbarTitle;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.HIV_REGISTRATION;
import static org.smartregister.util.Utils.getAllSharedPreferences;

public class PncMemberProfileActivity extends CorePncMemberProfileActivity implements PncMemberProfileContract.View {

    private CommonPersonObjectClient commonPersonObjectClient;
    private PncMemberProfilePresenter pncMemberProfilePresenter;
    private RecyclerView childFollowupRecyclerView;

    public static void startMe(Activity activity, String baseEntityID, MemberObject memberObject) {
        Intent intent = new Intent(activity, PncMemberProfileActivity.class);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, memberObject);
        passToolbarTitle(activity, intent);
        activity.startActivity(intent);
    }
    public static void startMe(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, PncMemberProfileActivity.class);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.BASE_ENTITY_ID, baseEntityID);
        passToolbarTitle(activity, intent);
        activity.startActivity(intent);
    }

    public static void startChildForm(Activity activity, String childBaseEntityId) {
        JSONObject jsonForm = org.smartregister.chw.core.utils.FormUtils.getFormUtils().getFormJson(org.smartregister.chw.hf.utils.Constants.JsonForm.getPncChildGeneralExamination());
        try {
            jsonForm.getJSONObject("global").put("baseEntityId", childBaseEntityId);
            jsonForm.getJSONObject("global").put("is_eligible_for_bcg", HfPncDao.isChildEligibleForBcg(childBaseEntityId));
            jsonForm.getJSONObject("global").put("is_eligible_for_opv0", HfPncDao.isChildEligibleForOpv0(childBaseEntityId));
            activity.startActivityForResult(org.smartregister.chw.core.utils.FormUtils.getStartFormActivity(jsonForm, activity.getString(R.string.record_child_followup), activity), JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    protected void startMalariaRegister() {
        MalariaRegisterActivity.startMalariaRegistrationActivity(this, memberObject.getBaseEntityId());
    }

    @Override
    protected void startFpRegister() {
        FpRegisterActivity.startFpRegistrationActivity(this, memberObject.getBaseEntityId(), memberObject.getDob(), CoreConstants.JSON_FORM.getFpRegistrationForm("female"), FamilyPlanningConstants.ActivityPayload.REGISTRATION_PAYLOAD_TYPE);
    }

    @Override
    protected void startFpChangeMethod() {
        FpRegisterActivity.startFpRegistrationActivity(this, memberObject.getBaseEntityId(), memberObject.getDob(), CoreConstants.JSON_FORM.getFpChangeMethodForm("female"), FamilyPlanningConstants.ActivityPayload.CHANGE_METHOD_PAYLOAD_TYPE);
    }

    @Override
    protected void startMalariaFollowUpVisit() {
        // TODO -> Implement for HF
    }

    @Override
    protected void startHivRegister() {
        try {
            HivRegisterActivity.startHIVFormActivity(this, memberObject.getBaseEntityId(), HIV_REGISTRATION, (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, HIV_REGISTRATION).toString());
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    protected void startTbRegister() {
        try {
            TbRegisterActivity.startTbFormActivity(this, memberObject.getBaseEntityId(), CoreConstants.JSON_FORM.getTbRegistration(), (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, CoreConstants.JSON_FORM.getTbRegistration()).toString());
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    protected void startHfMalariaFollowupForm() {
        MalariaFollowUpVisitActivityHelper.startMalariaFollowUpActivity(this, memberObject.getBaseEntityId());
    }

    @Override
    protected void getRemoveBabyMenuItem(MenuItem menuItem) {
        // TODO -> Implement for HF
    }

    @Override
    public void openMedicalHistory() {
        PncMedicalHistoryActivity.startMe(this, memberObject);
    }

    public void setReferralTasks(Set<Task> taskList) {
        if (notificationAndReferralRecyclerView != null && taskList.size() > 0) {
            RecyclerView.Adapter mAdapter = new ReferralCardViewAdapter(taskList, this, getCommonPersonObjectClient(), CoreConstants.REGISTERED_ACTIVITIES.PNC_REGISTER_ACTIVITY);
            notificationAndReferralRecyclerView.setAdapter(mAdapter);
            notificationAndReferralLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.view_notification_and_referral_row).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        findViewById(R.id.record_visit_panel).setVisibility(View.VISIBLE);
        setCommonPersonObjectClient(getClientDetailsByBaseEntityID(baseEntityID));
    }

    @Override
    public void registerPresenter() {
        presenter = new PncMemberProfilePresenter(this, new PncMemberProfileInteractor(), memberObject);
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
    public void setUpComingServicesStatus(String service, AlertStatus status, Date date) {
        view_most_due_overdue_row.setVisibility(View.GONE);
        rlUpcomingServices.setVisibility(View.GONE);
    }

    public CommonPersonObjectClient getCommonPersonObjectClient() {
        return commonPersonObjectClient;
    }

    public void setCommonPersonObjectClient(CommonPersonObjectClient commonPersonObjectClient) {
        this.commonPersonObjectClient = commonPersonObjectClient;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupViews();
        ((PncMemberProfileContract.Presenter) presenter()).fetchReferralTasks();
        if (notificationAndReferralRecyclerView != null && notificationAndReferralRecyclerView.getAdapter() != null) {
            notificationAndReferralRecyclerView.getAdapter().notifyDataSetChanged();
        }
        if (childFollowupRecyclerView != null && childFollowupRecyclerView.getAdapter() != null) {
            childFollowupRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pnc_member_profile_menu, menu);
        List<ChildModel> childModels = HfPncDao.childrenForPncWoman(memberObject.getBaseEntityId());
        for (int i = 0; i < childModels.size(); i++) {
            menu.add(0, R.id.action_pnc_registration, 100 + i, getString(R.string.edit_child_form_title, childModels.get(i).getFirstName()));
            menuItemEditNames.put(getString(R.string.edit_child_form_title, childModels.get(i).getFirstName()), childModels.get(i).getBaseEntityId());
        }
        menu.findItem(R.id.action__pnc_remove_member).setVisible(false);
        menu.findItem(R.id.action__pnc_danger_sign_outcome).setVisible(false);

        if (MalariaDao.isRegisteredForMalaria(baseEntityID)) {
            menu.findItem(R.id.action_malaria_followup_visit).setVisible(true);
            menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);
        } else {
            menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);
        }
        if (FpDao.isRegisteredForFp(baseEntityID)) {
            menu.findItem(R.id.action_fp_change).setVisible(true);
        } else {
            menu.findItem(R.id.action_fp_initiation_pnc).setVisible(false);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON) {
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
            try {
                JSONObject form = new JSONObject(jsonString);
                String encounterType = form.getString(JsonFormUtils.ENCOUNTER_TYPE);
                if (encounterType.equals(Utils.metadata().familyMemberRegister.updateEventType)) {
                    FamilyEventClient familyEventClient =
                            new FamilyProfileModel(memberObject.getFamilyName()).processUpdateMemberRegistration(jsonString, memberObject.getBaseEntityId());
                    new FamilyProfileInteractor().saveRegistration(familyEventClient, jsonString, true, (FamilyProfileContract.InteractorCallBack) pncMemberProfilePresenter());
                } else if (encounterType.equals(CoreConstants.EventType.PNC_DANGER_SIGNS_OUTCOME)) {
                    try {
                        getPncMemberProfilePresenter().createPncDangerSignsOutcomeEvent(Utils.getAllSharedPreferences(), jsonString, memberObject.getBaseEntityId());
                    } catch (Exception ex) {
                        Timber.e(ex);
                    }
                } else if (encounterType.equals(org.smartregister.chw.hf.utils.Constants.Events.PNC_CHILD_FOLLOWUP)) {
                    try {
                        AllSharedPreferences allSharedPreferences = org.smartregister.util.Utils.getAllSharedPreferences();
                        Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, org.smartregister.chw.hf.utils.Constants.TableName.PNC_FOLLOWUP);
                        org.smartregister.chw.pmtct.util.JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
                        JSONObject global = form.getJSONObject("global");
                        baseEvent.setBaseEntityId(global.getString("baseEntityId"));

                        NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.pmtct.util.JsonFormUtils.gson.toJson(baseEvent)));
                        Toast.makeText(this, getString(R.string.saved_child_followup), Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        Timber.e(ex);
                    }
                }
            } catch (JSONException jsonException) {
                Timber.e(jsonException);
            }
        }
    }

    @Override
    public void setupViews() {
        super.setupViews();
        try {
            PncVisitUtils.processVisits();
        } catch (Exception e) {
            Timber.e(e);
        }
        textview_record_anc_visit.setVisibility(View.VISIBLE);
        textview_record_anc_visit.setOnClickListener(this);

        int pncDay = Integer.parseInt(getPncMemberProfileInteractor().getPncDay(memberObject.getBaseEntityId()));
        if (pncDay >= 42) {
            textViewAncVisitNot.setVisibility(View.VISIBLE);
        }

        textViewAncVisitNot.setText(R.string.complete_pnc_visits);
        textViewAncVisitNot.setOnClickListener(v -> confirmRemovePncMember());


        if (HfPncDao.isMotherEligibleForPmtctRegistration(baseEntityID) && !PmtctDao.isRegisteredForPmtct(baseEntityID)) {
            textview_record_anc_visit.setVisibility(View.GONE);
            layoutNotRecordView.setVisibility(View.VISIBLE);
            textViewUndo.setVisibility(View.GONE);
            textViewNotVisitMonth.setText(getContext().getString(R.string.pmtct_pending_registration));
            tvEdit.setText(getContext().getString(R.string.register_button_text));
            tvEdit.setVisibility(View.VISIBLE);
            tvEdit.setOnClickListener(v -> startPmtctRegistration());
            imageViewCross.setImageResource(org.smartregister.chw.core.R.drawable.activityrow_notvisited);
        }

        Visit latestVisit = getVisit(org.smartregister.chw.hf.utils.Constants.Events.PNC_VISIT);
        if (latestVisit != null && !latestVisit.getProcessed()) {
            showVisitInProgress();
        }

        showChildFollowupViews();
    }

    @Override
    public void setMemberName(String memberName) {
        getPncMemberProfileInteractor().getPncMotherNameDetails(memberObject, text_view_anc_member_name, imageView);
    }

    @Override
    public void setProfileImage(String baseEntityId, String entityType) {
        String pncDay = getPncMemberProfileInteractor().getPncDay(memberObject.getBaseEntityId());
        if (StringUtils.isNotBlank(pncDay) && Integer.parseInt(pncDay) >= 49) {
            imageRenderHelper.refreshProfileImage(baseEntityId, imageView, org.smartregister.chw.anc.util.NCUtils.getMemberProfileImageResourceIDentifier("pnc"));
        } else {
            imageRenderHelper.refreshProfileImage(baseEntityId, imageView, org.smartregister.chw.pnc.R.drawable.pnc_less_twenty_nine_days);
        }
    }

    private void showVisitInProgress() {
        textview_record_anc_visit.setVisibility(View.GONE);
        layoutNotRecordView.setVisibility(View.VISIBLE);
        textViewUndo.setVisibility(View.GONE);
        textViewNotVisitMonth.setText(R.string.pnc_visit_in_progress);
        tvEdit.setVisibility(View.VISIBLE);
        tvEdit.setOnClickListener(v -> {
            PncFacilityVisitActivity.startMe(this, baseEntityID, true);
        });
        imageViewCross.setImageResource(org.smartregister.chw.core.R.drawable.activityrow_notvisited);
    }

    @Override
    public void setFamilyStatus(AlertStatus status) {
        view_family_row.setVisibility(View.GONE);
        rlFamilyServicesDue.setVisibility(View.GONE);
    }

    @Override
    protected Class<? extends CoreFamilyProfileActivity> getFamilyProfileActivityClass() {
        return FamilyProfileActivity.class;
    }

    @Override
    protected CorePncMemberProfileInteractor getPncMemberProfileInteractor() {
        return new PncMemberProfileInteractor();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.textview_record_visit) {
            PncFacilityVisitActivity.startMe(this, baseEntityID, false);
        }
    }

    private void confirmRemovePncMember() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.complete_pnc_visits));
        builder.setMessage(getString(R.string.complete_pnc_message));
        builder.setCancelable(true);

        builder.setPositiveButton(this.getString(R.string.yes), (dialog, id) -> {
            try {
                removePncMember();
                finish();
            } catch (Exception e) {
                Timber.e(e, "PncMemberProfileActivity --> closePncVisitsDialog");
            }
        });
        builder.setNegativeButton(this.getString(R.string.cancel), ((dialog, id) -> dialog.cancel()));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void removePncMember() {
        //creates an event to close pnc visits and removes member from pnc register
        AllSharedPreferences sharedPreferences = getAllSharedPreferences();
        Event baseEvent = (Event) new Event()
                .withBaseEntityId(baseEntityID)
                .withEventDate(new Date())
                .withEventType(org.smartregister.chw.hf.utils.Constants.Events.CLOSE_PNC_VISITS)
                .withFormSubmissionId(org.smartregister.util.JsonFormUtils.generateRandomUUIDString())
                .withEntityType(CoreConstants.TABLE_NAME.PNC_MEMBER)
                .withProviderId(sharedPreferences.fetchRegisteredANM())
                .withLocationId(ChwNotificationDao.getSyncLocationId(baseEntityID))
                .withTeamId(sharedPreferences.fetchDefaultTeamId(sharedPreferences.fetchRegisteredANM()))
                .withTeam(sharedPreferences.fetchDefaultTeam(sharedPreferences.fetchRegisteredANM()))
                .withClientDatabaseVersion(BuildConfig.DATABASE_VERSION)
                .withClientApplicationVersion(BuildConfig.VERSION_CODE)
                .withDateCreated(new Date());
        org.smartregister.chw.hf.utils.JsonFormUtils.tagSyncMetadata(Utils.context().allSharedPreferences(), baseEvent);
        try {
            org.smartregister.chw.anc.util.NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseEvent)));
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected Class<? extends CorePncRegisterActivity> getPncRegisterActivityClass() {
        return PncRegisterActivity.class;
    }

    public PncMemberProfileContract.Presenter pncMemberProfilePresenter() {
        if (pncMemberProfilePresenter == null) {
            pncMemberProfilePresenter = new PncMemberProfilePresenter(this, new PncMemberProfileInteractor(), memberObject);
        }
        return pncMemberProfilePresenter;
    }

    private void showChildFollowupViews() {
        RelativeLayout rlChildFollowup = findViewById(R.id.child_followup_row);
        childFollowupRecyclerView = findViewById(R.id.child_followup_recycler_view);
        childFollowupRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ChildModel> childModels = HfPncDao.childrenForPncWoman(memberObject.getBaseEntityId());
        if (childFollowupRecyclerView != null && childModels.size() > 0) {
            RecyclerView.Adapter mAdapter = new ChildFollowupViewAdapter(childModels, this);
            childFollowupRecyclerView.setAdapter(mAdapter);
            rlChildFollowup.setVisibility(View.VISIBLE);
        }
    }

    protected void startPmtctRegistration() {
        try {
            PmtctRegisterActivity.startPmtctRegistrationActivity(this, baseEntityID, "", false);
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
