package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.core.utils.Utils.getCommonPersonObjectClient;
import static org.smartregister.chw.hf.activity.HeiProfileActivity.getClientDetailsByBaseEntityID;
import static org.smartregister.chw.hf.dao.LDDao.isTheClientReferred;
import static org.smartregister.chw.hf.utils.Constants.Events.LD_ACTIVE_MANAGEMENT_OF_3RD_STAGE_OF_LABOUR;
import static org.smartregister.chw.hf.utils.Constants.Events.LD_PARTOGRAPHY;
import static org.smartregister.chw.hf.utils.Constants.Events.LD_POST_DELIVERY_MOTHER_MANAGEMENT;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryCervixDilationMonitoring;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryLabourStage;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryModeOfDelivery;
import static org.smartregister.chw.hf.utils.LDVisitUtils.shouldProcessPartographVisit;
import static org.smartregister.util.Utils.getAllSharedPreferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.core.dao.ChwNotificationDao;
import org.smartregister.chw.core.dataloader.CoreFamilyMemberDataLoader;
import org.smartregister.chw.core.form_data.NativeFormsDataBinder;
import org.smartregister.chw.core.model.ChildModel;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.UpdateDetailsUtil;
import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.ReferralCardViewAdapter;
import org.smartregister.chw.hf.contract.HfLDProfileContract;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.hf.dao.HfPncDao;
import org.smartregister.chw.hf.dao.LDDao;
import org.smartregister.chw.hf.interactor.LDProfileInteractor;
import org.smartregister.chw.hf.interactor.PncMemberProfileInteractor;
import org.smartregister.chw.hf.presenter.LDProfilePresenter;
import org.smartregister.chw.hf.utils.LDReferralFormUtils;
import org.smartregister.chw.hf.utils.LDVisitUtils;
import org.smartregister.chw.hiv.dao.HivDao;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.activity.BaseLDProfileActivity;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.util.Constants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.AlertStatus;
import org.smartregister.domain.Task;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.repository.AllSharedPreferences;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class LDProfileActivity extends BaseLDProfileActivity implements HfLDProfileContract.View {
    public static final String LD_PROFILE_ACTION = "LD_PROFILE_ACTION";
    protected RecyclerView notificationAndReferralRecyclerView;
    protected RelativeLayout notificationAndReferralLayout;
    protected HashMap<String, String> menuItemEditNames = new HashMap<>();
    private String partographVisitTitle;
    private String currentVisitItemTitle = "";
    private TextView processPartograph;
    private Visit lastLDVisit;

    public static void startProfileActivity(Activity activity, String baseEntityId) {
        Intent intent = new Intent(activity, LDProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        activity.startActivity(intent);
    }

    public static void startLDForm(Activity activity, String baseEntityID, String formName) {
        Intent intent = new Intent(activity, LDRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.ld.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.LD_FORM_NAME, formName);
        intent.putExtra(org.smartregister.chw.ld.util.Constants.ACTIVITY_PAYLOAD.ACTION, LD_PROFILE_ACTION);
        activity.startActivity(intent);
    }

    public static void closeLDVisit(String baseEntityId, Context context) {
        AllSharedPreferences sharedPreferences = getAllSharedPreferences();
        Event baseEvent = (Event) new Event()
                .withBaseEntityId(baseEntityId)
                .withEventDate(new Date())
                .withEventType(org.smartregister.chw.hf.utils.Constants.Events.CLOSE_LD)
                .withFormSubmissionId(org.smartregister.util.JsonFormUtils.generateRandomUUIDString())
                .withEntityType(CoreConstants.TABLE_NAME.LABOUR_AND_DELIVERY)
                .withProviderId(sharedPreferences.fetchRegisteredANM())
                .withLocationId(ChwNotificationDao.getSyncLocationId(baseEntityId))
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
        Intent intent = new Intent(context, PncRegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        ((LDProfileActivity) context).finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeNotificationReferralRecyclerView();
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        partographVisitTitle = getString(R.string.labour_and_delivery_partograph_button_title);
        setTextViewRecordLDText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupViews();
        setTextViewRecordLDText();
        refreshMedicalHistory(true);
        invalidateOptionsMenu();
        ((LDProfilePresenter) profilePresenter).fetchTasks();


        if (LDDao.isClosed(memberObject.getBaseEntityId())) {
            textViewRecordLD.setVisibility(View.GONE);
            TextView forecastSvdTitle = findViewById(R.id.forecast_svd_title);
            forecastSvdTitle.setText("Discharged Date");
            vaginalExamDate.setText(LDDao.getLastInteractedWith(memberObject.getBaseEntityId()));
            forecastSVDTime.setVisibility(View.GONE);
        }
    }

    protected void setupViews() {
        super.setupViews();
        processPartograph = findViewById(R.id.textview_process_partograph);
        processPartograph.setOnClickListener(this);

        processVisits(false);

        lastLDVisit = getLastVisit();
        if (lastLDVisit != null && !lastLDVisit.getProcessed()) {
            showVisitInProgress();
            setUpEditButton();
        } else {
            textViewRecordLD.setVisibility(View.VISIBLE);
            textViewVisitDoneEdit.setVisibility(View.GONE);
            visitDone.setVisibility(View.GONE);
            processPartograph.setVisibility(View.GONE);
        }

        showLabourProgress(LDDao.getPartographStartTime(memberObject.getBaseEntityId()) != null);
        findViewById(org.smartregister.ld.R.id.primary_ld_caregiver).setVisibility(View.GONE);
        findViewById(org.smartregister.ld.R.id.family_ld_head).setVisibility(View.GONE);

        Boolean theClientReferred = isTheClientReferred(memberObject.getBaseEntityId());
        if (theClientReferred != null && theClientReferred) {
            referredLabel.setVisibility(View.VISIBLE);
            referredLabel.setText(getString(R.string.referred_for_ld_emergency));
        } else {
            referredLabel.setVisibility(View.GONE);
        }

        if (!(LDDao.getLabourStage(memberObject.getBaseEntityId()) == null && (LDDao.getReasonsForAdmission(memberObject.getBaseEntityId()) == null || !LDDao.getReasonsForAdmission(memberObject.getBaseEntityId()).equalsIgnoreCase("elective_cesarean_section")))) {
            rlLdDetails.setVisibility(View.VISIBLE);
        } else {
            rlLdDetails.setVisibility(View.GONE);
        }

    }

    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        profilePresenter = new LDProfilePresenter(this, new LDProfileInteractor(), memberObject);
        fetchProfileData();
        profilePresenter.refreshProfileBottom();
    }

    private void processVisits(boolean partograph) {
        try {
            LDVisitUtils.processVisits(memberObject.getBaseEntityId(), partograph);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void showVisitInProgress() {
        textViewRecordLD.setVisibility(View.GONE);
        textViewVisitDoneEdit.setVisibility(View.VISIBLE);
        visitDone.setVisibility(View.VISIBLE);
        textViewVisitDone.setTextColor(getResources().getColor(R.color.black_text_color));
        imageViewCross.setImageResource(R.drawable.activityrow_visit_in_progress);
        if (currentVisitItemTitle.equalsIgnoreCase(partographVisitTitle)) {
            textViewVisitDone.setText(this.getString(R.string.visit_in_progress, org.smartregister.chw.hf.utils.Constants.Visits.LD_PARTOGRAPH_VISIT));
            try {
                if (shouldProcessPartographVisit(lastLDVisit))
                    processPartograph.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Timber.e(e);
            }
        } else if (currentVisitItemTitle.equalsIgnoreCase(getString(R.string.labour_and_delivery_examination_and_consultation_button_tittle))) {
            textViewVisitDone.setText(this.getString(R.string.visit_in_progress, org.smartregister.chw.hf.utils.Constants.Visits.LD_GENERAL_VISIT));
        } else if (currentVisitItemTitle.equalsIgnoreCase(getString(R.string.ld_mother_post_delivery_management))) {
            textViewVisitDone.setText(this.getString(R.string.visit_in_progress, org.smartregister.chw.hf.utils.Constants.Visits.LD_IMMEDIATE_POSTPARTUM_CARE));
        } else {
            textViewVisitDone.setText(this.getString(R.string.visit_in_progress, org.smartregister.chw.hf.utils.Constants.Visits.LD_MANAGEMENT_OF_3rd_STAGE_OF_LABOUR_VISIT));
        }
    }

    private Visit getLastVisit() {
        if (currentVisitItemTitle.equalsIgnoreCase(partographVisitTitle))
            return LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), LD_PARTOGRAPHY);
        else if (currentVisitItemTitle.equalsIgnoreCase(getString(R.string.labour_and_delivery_examination_and_consultation_button_tittle)))
            return LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EVENT_TYPE.LD_GENERAL_EXAMINATION);
        else if (currentVisitItemTitle.equalsIgnoreCase(getString(R.string.ld_mother_post_delivery_management)))
            return LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), LD_POST_DELIVERY_MOTHER_MANAGEMENT);
        else if (currentVisitItemTitle.equalsIgnoreCase(getString(R.string.ld_active_management_3rd_stage)))
            return LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), LD_ACTIVE_MANAGEMENT_OF_3RD_STAGE_OF_LABOUR);
        else
            return LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), LD_POST_DELIVERY_MOTHER_MANAGEMENT);
    }

    private void setUpEditButton() {
        textViewVisitDoneEdit.setOnClickListener(v -> {
            if (currentVisitItemTitle.equalsIgnoreCase(partographVisitTitle)) {
                LDPartographActivity.startMe(this, memberObject.getBaseEntityId(), true,
                        getName(memberObject), String.valueOf(new Period(new DateTime(this.memberObject.getAge()), new DateTime()).getYears()));
            } else if (currentVisitItemTitle.equalsIgnoreCase(getString(R.string.labour_and_delivery_examination_and_consultation_button_tittle))) {
                LDGeneralExaminationVisitActivity.startLDGeneralExaminationVisitActivity(this, memberObject.getBaseEntityId(), true);
            } else if (currentVisitItemTitle.equalsIgnoreCase(getString(R.string.ld_mother_post_delivery_management))) {
                openPostDeliveryManagementMother(true);
            } else {
                LDActiveManagementStageActivity.startActiveManagementActivity(this, memberObject.getBaseEntityId(), true);
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.textview_record_ld) {
            if (((TextView) view).getText().equals(getString(R.string.labour_and_delivery_labour_stage_title))) {
                startLDForm(this, memberObject.getBaseEntityId(), getLabourAndDeliveryLabourStage());
            } else if (((TextView) view).getText().equals(getString(R.string.labour_and_delivery_examination_and_consultation_button_tittle))) {
                openExaminationConsultation();
            } else if (((TextView) view).getText().equals(getString(R.string.labour_and_delivery_cervix_dilation_monitoring_button_tittle))) {
                startLDForm(this, memberObject.getBaseEntityId(), getLabourAndDeliveryCervixDilationMonitoring());
            } else if (((TextView) view).getText().equals(getString(R.string.labour_and_delivery_partograph_button_title))) {
                LDPartographActivity.startMe(this, memberObject.getBaseEntityId(), false,
                        getName(memberObject), String.valueOf(new Period(new DateTime(this.memberObject.getAge()), new DateTime()).getYears()));
            } else if (((TextView) view).getText().equals(getString(R.string.lb_mode_of_delivery))) {
                startLDForm(this, memberObject.getBaseEntityId(), getLabourAndDeliveryModeOfDelivery());
            } else if (((TextView) view).getText().equals(getString(R.string.ld_active_management_3rd_stage))) {
                openActiveManagementStage();
            } else if (((TextView) view).getText().equals(getString(R.string.ld_mother_post_delivery_management))) {
                openPostDeliveryManagementMother(false);
            } else if (((TextView) view).getText().equals(getString(R.string.ld_discharge_client))) {
                closeLDVisit(memberObject.getBaseEntityId(), LDProfileActivity.this);
            } else if (((TextView) view).getText().equals(getString(R.string.pregnancy_outcome_title))) {
                CommonPersonObjectClient client = getCommonPersonObjectClient(memberObject.getBaseEntityId());
                String familyBaseEntityId = org.smartregister.util.Utils.getValue(client.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.RELATIONAL_ID, false);
                boolean motherHivStatus = LDDao.getHivStatus(memberObject.getBaseEntityId()).equalsIgnoreCase("positive") || HivDao.isRegisteredForHiv(memberObject.getBaseEntityId()) || HfAncDao.isClientKnownOnArt(memberObject.getBaseEntityId()) || HfAncDao.getHivStatus(memberObject.getBaseEntityId()).equalsIgnoreCase("positive");
                PncRegisterActivity.startPncRegistrationActivity(LDProfileActivity.this, memberObject.getBaseEntityId(), null, CoreConstants.JSON_FORM.getPregnancyOutcome(), AncLibrary.getInstance().getUniqueIdRepository().getNextUniqueId().getOpenmrsId(), familyBaseEntityId, memberObject.getFamilyName(), null, motherHivStatus);
            }
        } else if (id == R.id.textview_process_partograph) {
            processPartographEvent();
        } else if (id == R.id.rlRegistrationDetails) {
            LDRegistrationDetailsActivity.startMe(this, memberObject);
        } else if (id == R.id.rlLdDetails) {
            LdSummaryDetailsActivity.startMe(this, memberObject);
        } else {
            super.onClick(view);
        }
    }

    private void processPartographEvent() {
        processVisits(true);
        onResume();
    }

    private String getName(MemberObject memberObject) {
        return getName(getName(memberObject.getFirstName(), memberObject.getMiddleName()), memberObject.getLastName());
    }

    private String getName(String nameOne, String nameTwo) {
        return nameOne + " " + nameTwo;
    }

    private void setTextViewRecordLDText() {
        Boolean theClientReferred = isTheClientReferred(memberObject.getBaseEntityId());
        if (theClientReferred != null && theClientReferred) {
            textViewRecordLD.setText(R.string.pregnancy_outcome_title);
        } else if (LDDao.getLabourStage(memberObject.getBaseEntityId()) == null && (LDDao.getReasonsForAdmission(memberObject.getBaseEntityId()) == null || !LDDao.getReasonsForAdmission(memberObject.getBaseEntityId()).equalsIgnoreCase("elective_cesarean_section"))) {
            currentVisitItemTitle = getString(R.string.labour_and_delivery_labour_stage_title);
            textViewRecordLD.setText(R.string.labour_and_delivery_labour_stage_title);
        } else if ((LDDao.getReasonsForAdmission(memberObject.getBaseEntityId()) != null && LDDao.getReasonsForAdmission(memberObject.getBaseEntityId()).equalsIgnoreCase("elective_cesarean_section")) && (LDDao.getLabourStage(memberObject.getBaseEntityId()) == null || (!LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("complete") && !LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("3")))) {
            textViewRecordLD.setText(R.string.lb_mode_of_delivery);
            currentVisitItemTitle = getString(R.string.lb_mode_of_delivery);
        } else if (LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("1") || LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("2")) {
            if (LDDao.getCervixDilation(memberObject.getBaseEntityId()) == null || Integer.parseInt(LDDao.getCervixDilation(memberObject.getBaseEntityId())) < 3) {
                textViewRecordLD.setText(R.string.labour_and_delivery_examination_and_consultation_button_tittle);
                currentVisitItemTitle = getString(R.string.labour_and_delivery_examination_and_consultation_button_tittle);
            } else if (Integer.parseInt(LDDao.getCervixDilation(memberObject.getBaseEntityId())) >= 3 && Integer.parseInt(LDDao.getCervixDilation(memberObject.getBaseEntityId())) < 10) {
                textViewRecordLD.setText(R.string.labour_and_delivery_partograph_button_title);
                currentVisitItemTitle = getString(R.string.labour_and_delivery_partograph_button_title);
            } else if (Integer.parseInt(LDDao.getCervixDilation(memberObject.getBaseEntityId())) == 10) {
                textViewRecordLD.setText(R.string.lb_mode_of_delivery);
                currentVisitItemTitle = getString(R.string.lb_mode_of_delivery);
            }
        } else if (LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("3") && (LDDao.getModeOfDelivery(memberObject.getBaseEntityId()) == null || (LDDao.getModeOfDelivery(memberObject.getBaseEntityId()) != null && !LDDao.getModeOfDelivery(memberObject.getBaseEntityId()).equals("cesarean")))) {
            textViewRecordLD.setText(R.string.ld_active_management_3rd_stage);
        } else if (LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("complete")) {
            textViewRecordLD.setText(R.string.ld_discharge_client);
            currentVisitItemTitle = getString(R.string.ld_discharge_client);
        } else if (LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("4") || (LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("3") && (LDDao.getModeOfDelivery(memberObject.getBaseEntityId()) != null && LDDao.getModeOfDelivery(memberObject.getBaseEntityId()).equals("cesarean")))) {
            textViewRecordLD.setText(R.string.ld_mother_post_delivery_management);
            currentVisitItemTitle = getString(R.string.ld_mother_post_delivery_management);
        }
    }

    private void openExaminationConsultation() {
        String baseEntityId = null;
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            baseEntityId = extras.getString(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
        }

        LDGeneralExaminationVisitActivity.startLDGeneralExaminationVisitActivity(this, baseEntityId, false);
    }

    private void openActiveManagementStage() {
        LDActiveManagementStageActivity.startActiveManagementActivity(this, memberObject.getBaseEntityId(), false);
    }

    private void openPostDeliveryManagementMother(boolean editMode) {
        LDPostDeliveryManagementMotherActivity.startPostDeliveryMotherManagementActivity(this, memberObject.getBaseEntityId(), editMode);
    }

    @Override
    public void refreshMedicalHistory(boolean hasHistory) {
        showProgressBar(false);
        Visit lastVisit = LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), LD_PARTOGRAPHY);

        if (lastVisit != null) {
            rlLastVisit.setVisibility(View.VISIBLE);
            TextView ivViewHistoryArrow = findViewById(R.id.ivViewHistoryArrow);
            ivViewHistoryArrow.setText(R.string.partograph_details_title);
            ivViewHistoryArrow.setTextColor(getResources().getColor(R.color.black));
        } else {
            rlLastVisit.setVisibility(View.GONE);
        }
    }

    @Override
    public void openMedicalHistory() {
        LDPartographDetailsActivity.startMe(this, memberObject);
    }

    @Override
    public void refreshUpComingServicesStatus(String service, AlertStatus status, Date date) {
        showProgressBar(false);
        rlUpcomingServices.setVisibility(View.GONE);
    }

    @Override
    public void refreshFamilyStatus(AlertStatus status) {
        showProgressBar(false);
        view_family_row.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ld_member_profile_menu, menu);
        if (LDDao.getLabourStage(memberObject.getBaseEntityId()) != null) {
            int labourStage = 1;
            try {
                if (LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("complete")) {
                    labourStage = 4;
                } else
                    labourStage = Integer.parseInt(LDDao.getLabourStage(memberObject.getBaseEntityId()));
            } catch (Exception e) {
                Timber.e(e);
            }
            menu.findItem(R.id.action_mode_of_delivery).setVisible(labourStage <= 2);
        }

        List<ChildModel> childModels = HfPncDao.childrenForPncWoman(memberObject.getBaseEntityId());
        for (int i = 0; i < childModels.size(); i++) {
            String nameOfMenuItem = getString(R.string.refer_child_for_emergency, childModels.get(i).getFirstName());
            menu.add(0, R.id.action_child_emergency_registration, 100 + i, nameOfMenuItem);
            menuItemEditNames.put(nameOfMenuItem, childModels.get(i).getBaseEntityId());
        }

        for (int i = 0; i < childModels.size(); i++) {
            String nameOfMenuItem = getString(R.string.mark_newborn_as_deceased, childModels.get(i).getFirstName());
            menu.add(0, R.id.action_mark_newborn_as_deceased, 200 + i, nameOfMenuItem);
            menuItemEditNames.put(nameOfMenuItem, childModels.get(i).getBaseEntityId());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        try {
            if (itemId == R.id.action_mode_of_delivery) {
                startLDForm(this, memberObject.getBaseEntityId(), getLabourAndDeliveryModeOfDelivery());
                return true;
            } else if (itemId == R.id.action_ld_emergency_referral) {
                LDReferralFormUtils.startLDEmergencyReferral(this, memberObject.getBaseEntityId());
                return true;
            } else if (itemId == R.id.action_child_emergency_registration) {
                getChildEmergencyReferralMenuItem(item);
            } else if (itemId == R.id.action_mark_newborn_as_deceased) {
                getDeceasedChildMenuItem(item);
            } else if (itemId == R.id.action_member_registration) {
                if (UpdateDetailsUtil.isIndependentClient(memberObject.getBaseEntityId())) {
                    startFormForEdit(org.smartregister.chw.core.R.string.registration_info,
                            CoreConstants.JSON_FORM.getAllClientUpdateRegistrationInfoForm());
                } else {
                    startFormForEdit(org.smartregister.chw.core.R.string.edit_member_form_title,
                            CoreConstants.JSON_FORM.getFamilyMemberRegister());
                }
                return true;
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean getChildEmergencyReferralMenuItem(MenuItem item) {
        if (getChildren(memberObject).size() > 0) {
            for (CommonPersonObjectClient child : getChildren(memberObject)) {
                for (Map.Entry<String, String> entry : menuItemEditNames.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(item.getTitle().toString()) && entry.getValue().equalsIgnoreCase(child.entityId())) {
                        LDReferralFormUtils.startLDChildEmergencyReferral(this, child.entityId());
                    }
                }
            }
        }
        return true;
    }

    private boolean getDeceasedChildMenuItem(MenuItem item) {
        if (getChildren(memberObject).size() > 0) {
            for (CommonPersonObjectClient child : getChildren(memberObject)) {
                for (Map.Entry<String, String> entry : menuItemEditNames.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(item.getTitle().toString()) && entry.getValue().equalsIgnoreCase(child.entityId())) {

                        CommonPersonObjectClient commonPersonObjectClient = getClientDetailsByBaseEntityID(child.entityId());
                        if (commonPersonObjectClient.getColumnmaps().get("entity_type").toString().equals(CoreConstants.TABLE_NAME.INDEPENDENT_CLIENT)) {
                            commonPersonObjectClient.getColumnmaps().put(OpdDbConstants.KEY.REGISTER_TYPE, CoreConstants.REGISTER_TYPE.INDEPENDENT);
                        }

                        IndividualProfileRemoveActivity.startIndividualProfileActivity(this,
                                commonPersonObjectClient,
                                memberObject.getFamilyBaseEntityId(), memberObject.getFamilyHead(),
                                memberObject.getPrimaryCareGiver(), LDProfileActivity.class.getCanonicalName());
                    }
                }
            }
        }
        return true;
    }

    protected List<CommonPersonObjectClient> getChildren(MemberObject memberObject) {
        return new PncMemberProfileInteractor().pncChildrenUnder29Days(memberObject.getBaseEntityId());
    }

    public void startFormForEdit(Integer title_resource, String formName) {

        JSONObject form = null;
        CommonPersonObjectClient client = org.smartregister.chw.core.utils.Utils.clientForEdit(memberObject.getBaseEntityId());

        if (formName.equals(CoreConstants.JSON_FORM.getFamilyMemberRegister())) {
            form = CoreJsonFormUtils.getAutoPopulatedJsonEditMemberFormString(
                    (title_resource != null) ? getResources().getString(title_resource) : null,
                    CoreConstants.JSON_FORM.getFamilyMemberRegister(),
                    this, client,
                    Utils.metadata().familyMemberRegister.updateEventType, memberObject.getLastName(), false);
        } else if (formName.equals(CoreConstants.JSON_FORM.getAncRegistration())) {
            form = CoreJsonFormUtils.getAutoJsonEditAncFormString(
                    memberObject.getBaseEntityId(), this, formName, CoreConstants.EventType.UPDATE_ANC_REGISTRATION, getResources().getString(title_resource));
        } else if (formName.equalsIgnoreCase(CoreConstants.JSON_FORM.getAllClientUpdateRegistrationInfoForm())) {
            String titleString = title_resource != null ? getResources().getString(title_resource) : null;
            CommonPersonObjectClient commonPersonObjectClient = UpdateDetailsUtil.getFamilyRegistrationDetails(memberObject.getFamilyBaseEntityId());
            String uniqueID = commonPersonObjectClient.getColumnmaps().get(DBConstants.KEY.UNIQUE_ID);
            boolean isPrimaryCareGiver = commonPersonObjectClient.getCaseId().equalsIgnoreCase(memberObject.getFamilyBaseEntityId());

            NativeFormsDataBinder binder = new NativeFormsDataBinder(getContext(), memberObject.getBaseEntityId());
            binder.setDataLoader(new CoreFamilyMemberDataLoader(memberObject.getFamilyName(), isPrimaryCareGiver, titleString,
                    org.smartregister.chw.core.utils.Utils.metadata().familyMemberRegister.updateEventType, uniqueID));
            JSONObject jsonObject = binder.getPrePopulatedForm(CoreConstants.JSON_FORM.getAllClientUpdateRegistrationInfoForm());

            try {
                if (jsonObject != null) {
                    UpdateDetailsUtil.startUpdateClientDetailsActivity(jsonObject, this);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
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

    public Context getContext() {
        return this;
    }

    protected void initializeNotificationReferralRecyclerView() {
        notificationAndReferralLayout = findViewById(org.smartregister.chw.core.R.id.notification_and_referral_row);
        notificationAndReferralRecyclerView = findViewById(org.smartregister.chw.core.R.id.notification_and_referral_recycler_view);
        if (notificationAndReferralRecyclerView != null)
            notificationAndReferralRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void setClientTasks(Set<Task> taskList) {
        if (notificationAndReferralRecyclerView != null && taskList.size() > 0) {
            RecyclerView.Adapter mAdapter = new ReferralCardViewAdapter(taskList, this, getCommonPersonObjectClient(memberObject.getBaseEntityId()), CoreConstants.REGISTERED_ACTIVITIES.ANC_REGISTER_ACTIVITY);
            notificationAndReferralRecyclerView.setAdapter(mAdapter);
            notificationAndReferralLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.view_notification_and_referral_row).setVisibility(View.VISIBLE);
        }
    }

}
