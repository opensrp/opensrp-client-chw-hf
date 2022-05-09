package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryLabourStage;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.ld.activity.BaseLDProfileActivity;
import org.smartregister.chw.ld.dao.LDDao;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.util.Constants;

public class LDProfileActivity extends BaseLDProfileActivity {
    public static final String LABOUR_STAGE = "LABOUR_STAGE";

    public static void startProfileActivity(Activity activity, String baseEntityId) {
        Intent intent = new Intent(activity, LDProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        super.onCreation();

        if (LDDao.getLabourStage(memberObject.getBaseEntityId()) == null)
            textViewRecordLD.setText(R.string.labour_and_delivery_labour_stage_title);
        else if (LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("1")) {
            textViewRecordLD.setText(getString(R.string.record_ld_event, "Examination/Consultation"));
        } else if (LDDao.getLabourStage(memberObject.getBaseEntityId()).equals("2")) {
            textViewRecordLD.setText(getString(R.string.record_ld_event, "Partograph"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        memberObject = LDDao.getMember(memberObject.getBaseEntityId());
        if (LDDao.getLabourStage(memberObject.getBaseEntityId()) == null)
            textViewRecordLD.setText(R.string.labour_and_delivery_labour_stage_title);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.textview_record_ld) {
            if (((TextView) view).getText().equals(getString(R.string.labour_and_delivery_labour_stage_title))) {
                startLDForm(this, memberObject.getBaseEntityId(), getLabourAndDeliveryLabourStage());
            } else if (((TextView) view).getText().equals("Examination/Consultation")) {
                //TODO implement start examination form
            } else if (((TextView) view).getText().equals("Partograph")) {
                LDPartographActivity.startMe(this, memberObject.getBaseEntityId(), false,
                        getName(memberObject), String.valueOf(new Period(new DateTime(this.memberObject.getAge()), new DateTime()).getYears()));
            }
        } else {
            super.onClick(view);
        }
    }

    public static void startLDForm(Activity activity, String baseEntityID, String formName) {
        Intent intent = new Intent(activity, LDRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.ld.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.LD_FORM_NAME, formName);
        intent.putExtra(org.smartregister.chw.ld.util.Constants.ACTIVITY_PAYLOAD.ACTION, LABOUR_STAGE);
        activity.startActivity(intent);
    }

    private String getName(MemberObject memberObject) {
        return getName(getName(memberObject.getFirstName(), memberObject.getMiddleName()), memberObject.getLastName());
    }

    private String getName(String nameOne, String nameTwo) {
        return nameOne + " " + nameTwo;
    }

}
