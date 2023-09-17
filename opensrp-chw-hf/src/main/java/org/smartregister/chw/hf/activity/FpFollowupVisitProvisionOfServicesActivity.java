package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.fp.activity.BaseFpFollowupVisitProvisionOfServicesActivity;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;

public class FpFollowupVisitProvisionOfServicesActivity extends BaseFpFollowupVisitProvisionOfServicesActivity {
    public static void startMe(Activity activity, String baseEntityID, Boolean isEditMode) {
        Intent intent = new Intent(activity, FpFollowupVisitProvisionOfServicesActivity.class);
        intent.putExtra(FamilyPlanningConstants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(FamilyPlanningConstants.ACTIVITY_PAYLOAD.EDIT_MODE, isEditMode);
        activity.startActivityForResult(intent, FamilyPlanningConstants.REQUEST_CODE_GET_JSON);
    }

}
