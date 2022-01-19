package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONObject;
import org.smartregister.chw.core.activity.CorePmtctRegisterActivity;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.fragment.PmtctRegisterFragment;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;

import static org.smartregister.chw.hf.utils.Constants.JsonForm.getPmtctRegistration;

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
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);
        FamilyRegisterActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        if(ACTION.equalsIgnoreCase(Constants.ActionList.FOLLOWUP)){
            startActivityForResult(FormUtils.getStartFormActivity(jsonForm, getString(R.string.pmtct_followup_form_title), this), JsonFormUtils.REQUEST_CODE_GET_JSON);
        }else{
            startActivityForResult(FormUtils.getStartFormActivity(jsonForm, this.getString(org.smartregister.chw.core.R.string.pmtct_registration), this), JsonFormUtils.REQUEST_CODE_GET_JSON);
        }
    }

}
