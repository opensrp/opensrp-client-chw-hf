package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.hf.utils.Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryLabourStage;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.ld.activity.BaseLDProfileActivity;
import org.smartregister.chw.ld.dao.LDDao;
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        memberObject = LDDao.getMember(memberObject.getBaseEntityId());
        if (LDDao.getLabourStage(memberObject.getBaseEntityId()) == null)
            textViewRecordLD.setText(R.string.labour_and_delivery_labour_stage_title);
        else
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.textview_record_ld && ((TextView) view).getText().equals(getString(R.string.labour_and_delivery_labour_stage_title))) {
            startLDForm(this, memberObject.getBaseEntityId(), getLabourAndDeliveryLabourStage());
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

}
