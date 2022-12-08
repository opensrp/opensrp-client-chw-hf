package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.hf.utils.Constants.REQUEST_FILTERS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.EnumUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.model.BaseAncRegisterModel;
import org.smartregister.chw.anc.presenter.BaseAncRegisterPresenter;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.core.activity.CoreFamilyRegisterActivity;
import org.smartregister.chw.core.activity.CorePncRegisterActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.fragment.PncNoMotherRegisterFragment;
import org.smartregister.chw.hf.fragment.PncRegisterFragment;
import org.smartregister.chw.hf.interactor.AncRegisterInteractor;
import org.smartregister.family.util.Utils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class PncRegisterActivity extends CorePncRegisterActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    protected static boolean motherHivStatus;

    public static void startPncRegistrationActivity(Activity activity, String memberBaseEntityID, String phoneNumber, String formName,
                                                    String uniqueId, String familyBaseID, String family_name, String last_menstrual_period, boolean motherHivStatus) {
        Intent intent = new Intent(activity, PncRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, memberBaseEntityID);
        phone_number = phoneNumber;
        familyBaseEntityId = familyBaseID;
        form_name = formName;
        familyName = family_name;
        PncRegisterActivity.motherHivStatus = motherHivStatus;
        unique_id = uniqueId;
        lastMenstrualPeriod = last_menstrual_period;
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.TABLE_NAME, getFormTable());
        activity.startActivity(intent);
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        try {
            JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            JSONArray fields = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
            JSONObject global = jsonForm.getJSONObject("global");

            Map<String, String> values = new HashMap<>();
            values.put(DBConstants.KEY.TEMP_UNIQUE_ID, unique_id);
            values.put(CoreConstants.JsonAssets.FAM_NAME, familyName);
            values.put(CoreConstants.JsonAssets.FAMILY_MEMBER.PHONE_NUMBER, phone_number);
            values.put(org.smartregister.family.util.DBConstants.KEY.RELATIONAL_ID, familyBaseEntityId);
            FormUtils.updateFormField(fields, values);

            global.put("hiv_status_mother", motherHivStatus);
            Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

            Form form = new Form();
            form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
            form.setWizard(false);
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

            if (jsonForm.getString("encounter_type").equals("Pregnancy Outcome")) {
                form.setWizard(true);
                form.setNavigationBackground(org.smartregister.chw.core.R.color.family_navigation);
                form.setName(getString(R.string.pregnancy_outcome_title));
                form.setNextLabel(this.getResources().getString(org.smartregister.chw.core.R.string.next));
                form.setPreviousLabel(this.getResources().getString(org.smartregister.chw.core.R.string.back));
            } else if (jsonForm.getString("encounter_type").equals("PMTCT Post PNC Registration")) {
                form.setWizard(true);
                form.setNavigationBackground(org.smartregister.chw.core.R.color.family_navigation);
                form.setName(getString(R.string.pmtct_registration));
                form.setNextLabel(this.getResources().getString(org.smartregister.chw.core.R.string.next));
                form.setPreviousLabel(this.getResources().getString(org.smartregister.chw.core.R.string.back));
            }

            startActivityForResult(intent, org.smartregister.family.util.JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @MenuRes
    public int getMenuResource() {
        return R.menu.bottom_nav_anc_menu;
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().clear();

        bottomNavigationView.inflateMenu(R.menu.bottom_nav_pnc_menu);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public void switchToBaseFragment() {
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        return new PncNoMotherRegisterFragment[]{
                new PncNoMotherRegisterFragment()};
    }

    @Override
    protected Class<? extends CoreFamilyRegisterActivity> getFamilyRegisterActivity() {
        return FamilyRegisterActivity.class;
    }

    @Override
    public void onRegistrationSaved(String encounterType, boolean isEdit, boolean hasChildren) {
        if (encounterType.equalsIgnoreCase(Constants.EVENT_TYPE.PREGNANCY_OUTCOME) || encounterType.equalsIgnoreCase(org.smartregister.chw.hf.utils.Constants.Events.PMTCT_POST_PNC_REGISTRATION)) {
            hideProgressDialog();
            Timber.d("We are home - PNC Register");
            if (encounterType.equalsIgnoreCase(org.smartregister.chw.hf.utils.Constants.Events.PMTCT_POST_PNC_REGISTRATION)) {
                finish();
                Intent intent = new Intent(this, PmtctRegisterActivity.class);
                startActivity(intent);
            }

        } else {
            super.onRegistrationSaved(encounterType, isEdit, hasChildren);
        }
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new PncRegisterFragment();
    }

    @Override
    protected void initializePresenter() {
        this.presenter = new BaseAncRegisterPresenter(this, new BaseAncRegisterModel(), new AncRegisterInteractor());
    }


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResultExtended(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_FILTERS) {
            ((PncRegisterFragment) mBaseFragment).onFiltersUpdated(requestCode, data);
        } else if (resultCode == Activity.RESULT_OK) {
            try {
                JSONObject form = new JSONObject(data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
                String encounter_type = form.optString(Constants.JSON_FORM_EXTRA.ENCOUNTER_TYPE);
                String table = data.getStringExtra(Constants.ACTIVITY_PAYLOAD.TABLE_NAME);

                if (CoreConstants.EventType.PREGNANCY_OUTCOME.equals(encounter_type)) {
                    JSONArray fields = org.smartregister.util.JsonFormUtils.fields(form);
                    String pregnancyOutcome = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, org.smartregister.chw.hf.utils.Constants.pregnancyOutcome).optString(JsonFormUtils.VALUE);
                    if (EnumUtils.isValidEnum(org.smartregister.chw.hf.utils.Constants.FamilyRegisterOptionsUtil.class, pregnancyOutcome)) {
                        startRegisterActivity(FamilyRegisterActivity.class);
                        this.finish();
                        return;
                    }
                } else if (encounter_type.equalsIgnoreCase(org.smartregister.chw.hf.utils.Constants.Events.PMTCT_POST_PNC_REGISTRATION)) {
                    presenter().saveForm(form.toString(), false, table);
                }
                SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);

            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_pnc) {
            switchToFragment(0);
            return true;
        } else if (menuItem.getItemId() == R.id.action_no_mother) {
            switchToFragment(1);
            return true;
        } else if (menuItem.getItemId() == R.id.action_register_child) {
            PncNoMotherRegisterActivity.startPncNoMotherRegistrationActivity(this, "pnc_no_mother_registration");
            return true;
        }
        return false;
    }
}
