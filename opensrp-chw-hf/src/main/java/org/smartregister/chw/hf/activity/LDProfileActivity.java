package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.activity.BaseLDProfileActivity;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.util.Constants;

public class LDProfileActivity extends BaseLDProfileActivity {
    public static void startProfileActivity(Activity activity, String baseEntityId) {
        Intent intent = new Intent(activity, LDProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        activity.startActivity(intent);
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        textViewRecordLD.setText("Examination/Consultation");

        Visit lastLDVisit = LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), "Visit Type");
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();

        if (id == R.id.textview_record_ld) {
            openExaminationConsultation();
        }

    }

    private void openExaminationConsultation() {
        String baseEntityId = null;
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            baseEntityId = extras.getString(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
        }

        LDVisitActivity.startLDVisitActivity(this, baseEntityId, false);
    }

}
