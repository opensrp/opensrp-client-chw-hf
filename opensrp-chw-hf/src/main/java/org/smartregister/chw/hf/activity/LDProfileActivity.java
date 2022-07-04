package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.hf.utils.Constants.Events.LD_ACTIVE_MANAGEMENT_OF_3RD_STAGE_OF_LABOUR;
import static org.smartregister.chw.hf.utils.Constants.Events.LD_PARTOGRAPHY;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryCervixDilationMonitoring;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryLabourStage;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryModeOfDelivery;
import static org.smartregister.chw.hf.utils.LDVisitUtils.shouldProcessPartographVisit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.LDPostDeliveryManagementMotherActivityInteractor;
import org.smartregister.chw.hf.utils.LDVisitUtils;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.activity.BaseLDProfileActivity;
import org.smartregister.chw.ld.dao.LDDao;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.util.Constants;
import org.smartregister.domain.AlertStatus;

import java.util.Date;

import timber.log.Timber;

public class LDProfileActivity extends BaseLDProfileActivity {
    public static final String LD_PROFILE_ACTION = "LD_PROFILE_ACTION";
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
            return  LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), LDPostDeliveryManagementMotherActivityInteractor.EVENT_TYPE);
        else
            return LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), LD_ACTIVE_MANAGEMENT_OF_3RD_STAGE_OF_LABOUR);
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
            }
        } else if (id == R.id.textview_process_partograph) {
            processPartographEvent();
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
        if (LDDao.getLabourStage(memberObject.getBaseEntityId()) == null && (LDDao.getReasonsForAdmission(memberObject.getBaseEntityId()) == null || !LDDao.getReasonsForAdmission(memberObject.getBaseEntityId()).equalsIgnoreCase("elective_cesarean_section"))) {
            currentVisitItemTitle = getString(R.string.labour_and_delivery_labour_stage_title);
            textViewRecordLD.setText(R.string.labour_and_delivery_labour_stage_title);
        } else if ((LDDao.getReasonsForAdmission(memberObject.getBaseEntityId()) != null && LDDao.getReasonsForAdmission(memberObject.getBaseEntityId()).equalsIgnoreCase("elective_cesarean_section"))) {
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
                labourStage = Integer.parseInt(LDDao.getLabourStage(memberObject.getBaseEntityId()));
            } catch (Exception e) {
                Timber.e(e);
            }
            menu.findItem(R.id.action_mode_of_delivery).setVisible(labourStage <= 2);
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
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return super.onOptionsItemSelected(item);
    }

}
