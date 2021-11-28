package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONObject;
import org.smartregister.chw.core.activity.CorePmtctRegisterActivity;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.hf.fragment.PmtctRegisterFragment;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;

import androidx.annotation.Nullable;

import static org.smartregister.chw.core.utils.CoreConstants.JSON_FORM.getPmtctForm;
import static org.smartregister.chw.hf.utils.Constants.JSON_FORM.getPmtctRegistration;

public class PmtctRegisterActivity extends CorePmtctRegisterActivity {
    public static void startPmtctRegistrationActivity(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, PmtctRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.pmtct.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(org.smartregister.chw.pmtct.util.Constants.ACTIVITY_PAYLOAD.PMTCT_FORM_NAME, getPmtctRegistration());
        intent.putExtra(org.smartregister.chw.pmtct.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationMenu.getInstance(this, null, null);
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new PmtctRegisterFragment();
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        super.startFormActivity(jsonForm);
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);
        FamilyRegisterActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
    }

}
