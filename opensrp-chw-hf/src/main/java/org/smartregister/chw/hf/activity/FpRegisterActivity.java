package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.core.activity.CoreFpRegisterActivity;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.hf.fragment.FpRegisterFragment;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class FpRegisterActivity extends CoreFpRegisterActivity {

    private static String baseEntityId;

    public static void startFpRegistrationActivity(Activity activity, String baseEntityID, String dob, String formName, String payloadType) {
        Intent intent = new Intent(activity, FpRegisterActivity.class);
        intent.putExtra(FamilyPlanningConstants.ActivityPayload.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(FamilyPlanningConstants.ActivityPayload.DOB, dob);
        intent.putExtra(FamilyPlanningConstants.ActivityPayload.FP_FORM_NAME, formName);
        intent.putExtra(FamilyPlanningConstants.ActivityPayload.ACTION, payloadType);
        baseEntityId = baseEntityID;
        activity.startActivity(intent);
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new FpRegisterFragment();
    }

    @Override
    protected Activity getFpRegisterActivity() {
        return this;
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);
        FamilyRegisterActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
    }
}
