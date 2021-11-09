package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import static org.smartregister.chw.core.utils.CoreConstants.JSON_FORM.getPmtctFollowupForm;

public class PmtctFollowupVisitActivity extends PmtctRegisterActivity{
    public static void startPmtctFollowUpActivity(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, PmtctRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.pmtct.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(org.smartregister.chw.pmtct.util.Constants.ACTIVITY_PAYLOAD.PMTCT_FORM_NAME, getPmtctFollowupForm());
        intent.putExtra(org.smartregister.chw.pmtct.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.pmtct.util.Constants.ACTIVITY_PAYLOAD_TYPE.FOLLOW_UP_VISIT);
        activity.startActivity(intent);
    }
}
