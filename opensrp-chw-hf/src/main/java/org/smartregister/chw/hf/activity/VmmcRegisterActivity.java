package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.core.utils.CoreConstants.JSON_FORM.getVmmcEnrollment;
import static org.smartregister.chw.hf.utils.Constants.REQUEST_FILTERS;
import static org.smartregister.chw.hf.utils.JsonFormUtils.ENCOUNTER_TYPE;
import static org.smartregister.util.JsonFormUtils.FIELDS;
import static org.smartregister.util.JsonFormUtils.STEP1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONObject;
import org.smartregister.chw.core.activity.CoreVmmcRegisterActivity;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.fragment.VmmcRegisterFragment;
import org.smartregister.chw.hf.utils.VmmcReferralFormUtils;
import org.smartregister.chw.vmmc.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.Calendar;

import timber.log.Timber;

public class VmmcRegisterActivity extends CoreVmmcRegisterActivity {
    public static void startVmmcRegistrationActivity(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, VmmcRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.VMMC_FORM_NAME, getVmmcEnrollment());

        activity.startActivity(intent);
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new VmmcRegisterFragment();
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.VMMC);
        }
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);

        try {
            if (jsonForm.getString(ENCOUNTER_TYPE).equals("Vmmc Enrollment")) {

                JSONObject vmmcClientIdJsonObject = JsonFormUtils.getFieldJSONObject(jsonForm.getJSONObject(STEP1).getJSONArray(FIELDS), "vmmc_client_id");
                if (vmmcClientIdJsonObject != null) {
                    vmmcClientIdJsonObject.put("mask", VmmcReferralFormUtils.getHfrCode() + " #### " + Calendar.getInstance().get(Calendar.YEAR));
                    vmmcClientIdJsonObject.put("hint", "VMMC Client Id e.g " + VmmcReferralFormUtils.getHfrCode() + " 0001 " + Calendar.getInstance().get(Calendar.YEAR));
                }

            }
        } catch (Exception e) {
            Timber.e(e);
        }

        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        if (getFormConfig() != null) {
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, getFormConfig());
        }
        startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_FILTERS) {
            ((VmmcRegisterFragment) mBaseFragment).onFiltersUpdated(requestCode, data);
        }
    }

}

