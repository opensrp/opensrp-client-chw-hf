package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONObject;
import org.smartregister.chw.fp.activity.BaseFpOtherServicesActivity;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.family.util.Utils;

public class FpOtherServicesActivity extends BaseFpOtherServicesActivity {
    public static void startMe(Activity activity, String baseEntityID, Boolean isEditMode) {
        Intent intent = new Intent(activity, FpOtherServicesActivity.class);
        intent.putExtra(FamilyPlanningConstants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(FamilyPlanningConstants.ACTIVITY_PAYLOAD.EDIT_MODE, isEditMode);
        activity.startActivityForResult(intent, FamilyPlanningConstants.REQUEST_CODE_GET_JSON);
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
        intent.putExtra(FamilyPlanningConstants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        if (getFormConfig() != null) {
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, getFormConfig());
        }

        startActivityForResult(intent, FamilyPlanningConstants.REQUEST_CODE_GET_JSON);
    }
}
