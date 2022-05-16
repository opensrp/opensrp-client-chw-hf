package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryCervixDilationMonitoring;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryLabourStage;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryModeOfDelivery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.LDVisitUtils;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.activity.BaseLDProfileActivity;
import org.smartregister.chw.ld.dao.LDDao;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.util.Constants;

public class LDProfileActivity extends BaseLDProfileActivity {
    public static final String LD_PROFILE_ACTION = "LD_PROFILE_ACTION";

    public static void startProfileActivity(Activity activity, String baseEntityId) {
        Intent intent = new Intent(activity, LDProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        setTextViewRecordLDText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupViews();
        setTextViewRecordLDText();
    }

    protected void setupViews() {
        super.setupViews();

        try {
            LDVisitUtils.processVisits(memberObject.getBaseEntityId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Visit lastLDVisit = getLastVisit();
        if (lastLDVisit != null && !lastLDVisit.getProcessed()) {
            showVisitInProgress();
            setUpEditButton();
        } else {
            textViewRecordLD.setVisibility(View.VISIBLE);
            textViewVisitDoneEdit.setVisibility(View.GONE);
            visitDone.setVisibility(View.GONE);
        }

    }

    private void showVisitInProgress() {
        textViewRecordLD.setVisibility(View.GONE);
        textViewVisitDoneEdit.setVisibility(View.VISIBLE);
        visitDone.setVisibility(View.VISIBLE);
        textViewVisitDone.setText(this.getString(R.string.visit_in_progress, org.smartregister.chw.hf.utils.Constants.Visits.LD_GENERAL_VISIT));
        textViewVisitDone.setTextColor(getResources().getColor(R.color.black_text_color));
        imageViewCross.setImageResource(R.drawable.activityrow_visit_in_progress);
    }

    private Visit getLastVisit() {
        return LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EVENT_TYPE.LD_GENERAL_EXAMINATION);
    }

    private void setUpEditButton() {
        textViewVisitDoneEdit.setOnClickListener(v -> {
            LDVisitActivity.startLDVisitActivity(this, memberObject.getBaseEntityId(), true);
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
                openPostDeliveryManagementMother();
            }
        } else {
            super.onClick(view);
        }
    }

    private String getName(MemberObject memberObject) {
        return getName(getName(memberObject.getFirstName(), memberObject.getMiddleName()), memberObject.getLastName());
    }

    private String getName(String nameOne, String nameTwo) {
        return nameOne + " " + nameTwo;
    }

    private void setTextViewRecordLDText() {
        if (LDDao.getLabourStage(memberObject.getBaseEntityId()) == null)
            textViewRecordLD.setText(R.string.labour_and_delivery_labour_stage_title);
        else if (LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("1")) {
            if (LDDao.getCervixDilation(memberObject.getBaseEntityId()) == null) {
                textViewRecordLD.setText(R.string.labour_and_delivery_examination_and_consultation_button_tittle);
            } else if (Integer.parseInt(LDDao.getCervixDilation(memberObject.getBaseEntityId())) < 3) {
                textViewRecordLD.setText(R.string.labour_and_delivery_cervix_dilation_monitoring_button_tittle);
            } else {
                textViewRecordLD.setText(R.string.labour_and_delivery_partograph_button_title);
            }
        } else if (LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("2")) {
            textViewRecordLD.setText(R.string.lb_mode_of_delivery);
        } else if (LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("3")) {
            textViewRecordLD.setText(R.string.ld_active_management_3rd_stage);
        } else if (LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("4")) {
            textViewRecordLD.setText(R.string.ld_mother_post_delivery_management);
        }
     }

    public static void startLDForm(Activity activity, String baseEntityID, String formName) {
        Intent intent = new Intent(activity, LDRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.ld.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.LD_FORM_NAME, formName);
        intent.putExtra(org.smartregister.chw.ld.util.Constants.ACTIVITY_PAYLOAD.ACTION, LD_PROFILE_ACTION);
        activity.startActivity(intent);
    }

    private void openExaminationConsultation() {
        String baseEntityId = null;
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            baseEntityId = extras.getString(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
        }

        LDVisitActivity.startLDVisitActivity(this, baseEntityId, false);
    }


    private void openActiveManagementStage() {
        LDActiveManagementStageActivity.startActiveManagementActivity(this, memberObject.getBaseEntityId(), false);
    }


    private void openPostDeliveryManagementMother() {
        LDPostDeliveryManagementMotherActivity.startPostDeliveryMotherManagementActivity(this, memberObject.getBaseEntityId(), false);
    }

}
