package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import org.smartregister.chw.core.activity.CoreKvpProfileActivity;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.kvp.KvpLibrary;
import org.smartregister.chw.kvp.domain.Visit;
import org.smartregister.chw.kvp.util.Constants;

public class KvpProfileActivity extends CoreKvpProfileActivity {

    public static void startProfile(Activity activity, String baseEntityId) {
        Intent intent = new Intent(activity, KvpProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.PROFILE_TYPE, Constants.PROFILE_TYPES.KVP_PROFILE);
        activity.startActivity(intent);
    }

    @Override
    public void openFollowupVisit() {
        KvpServiceActivity.startMe(this, memberObject.getBaseEntityId());
    }

    @Override
    protected void startPrEPRegistration() {
        PrEPRegisterActivity.startMe(this, memberObject.getBaseEntityId(), memberObject.getGender(), memberObject.getAge());
    }


    @Override
    public void refreshMedicalHistory(boolean hasHistory) {
        Visit kvpBehavioralServices = getVisit(org.smartregister.chw.kvp.util.Constants.EVENT_TYPE.KVP_BEHAVIORAL_SERVICE_VISIT);
        Visit kvpBioMedicalServices = getVisit(org.smartregister.chw.kvp.util.Constants.EVENT_TYPE.KVP_BIO_MEDICAL_SERVICE_VISIT);
        Visit kvpStructuralServices = getVisit(org.smartregister.chw.kvp.util.Constants.EVENT_TYPE.KVP_STRUCTURAL_SERVICE_VISIT);
        Visit kvpOtherServicesVisit = getVisit(org.smartregister.chw.kvp.util.Constants.EVENT_TYPE.KVP_OTHER_SERVICE_VISIT);


        if (kvpBehavioralServices != null || kvpBioMedicalServices != null || kvpOtherServicesVisit != null || kvpStructuralServices != null) {
            rlLastVisit.setVisibility(View.VISIBLE);
            findViewById(R.id.view_notification_and_referral_row).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.vViewHistory)).setText(R.string.visits_history);
            ((TextView) findViewById(R.id.ivViewHistoryArrow)).setText(getString(R.string.view_visits_history));
        } else {
            rlLastVisit.setVisibility(View.GONE);
        }
    }

    @Override
    public void openMedicalHistory() {
        KvpMedicalHistoryActivity.startMe(this, memberObject);
    }

    private Visit getVisit(String eventType) {
        return KvpLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), eventType);
    }
}
