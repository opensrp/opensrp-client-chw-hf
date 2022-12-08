package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.hf.utils.Constants.HIV_STATUS.POSITIVE;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.getPmtctRegistration;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.getPmtctRegistrationForClientsKnownOnArtForm;
import static org.smartregister.chw.pmtct.util.Constants.EVENT_TYPE.PMTCT_REGISTRATION;
import static org.smartregister.util.JsonFormUtils.FIELDS;
import static org.smartregister.util.JsonFormUtils.VALUE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.activity.CorePmtctRegisterActivity;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.custom_view.FacilityMenu;
import org.smartregister.chw.hf.fragment.PmtctRegisterFragment;
import org.smartregister.chw.hf.listener.HfFamilyBottomNavListener;
import org.smartregister.chw.hf.presenter.PmtctRegisterPresenter;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.PncVisitUtils;
import org.smartregister.chw.pmtct.interactor.BasePmtctRegisterInteractor;
import org.smartregister.chw.pmtct.model.BasePmtctRegisterModel;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;

import timber.log.Timber;

public class PmtctRegisterActivity extends CorePmtctRegisterActivity {
    private static final String CTC_NUMBER = "ctc_number";
    private String ctcNumber;

    public static void startPmtctRegistrationActivity(Activity activity, String baseEntityID, String ctcNumber, boolean isKnownOnArt) {
        Intent intent = new Intent(activity, PmtctRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.pmtct.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        if (isKnownOnArt) {
            intent.putExtra(org.smartregister.chw.pmtct.util.Constants.ACTIVITY_PAYLOAD.PMTCT_FORM_NAME, getPmtctRegistrationForClientsKnownOnArtForm());
            intent.putExtra(CTC_NUMBER, ctcNumber);
        } else {
            intent.putExtra(org.smartregister.chw.pmtct.util.Constants.ACTIVITY_PAYLOAD.PMTCT_FORM_NAME, getPmtctRegistration());
        }
        intent.putExtra(org.smartregister.chw.pmtct.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        activity.startActivity(intent);
    }

    @Override
    protected void initializePresenter() {
        presenter = new PmtctRegisterPresenter(this, new BasePmtctRegisterModel(), new BasePmtctRegisterInteractor());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ctcNumber = getIntent().getStringExtra(CTC_NUMBER);
        super.onCreate(savedInstanceState);
        FacilityMenu.getInstance(this, null, null);
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new PmtctRegisterFragment();
    }


    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_family_menu);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_clients);
            bottomNavigationView.getMenu().removeItem(org.smartregister.chw.tb.R.id.action_register);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_search);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_library);
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_job_aids);
            bottomNavigationView.setOnNavigationItemSelectedListener(new HfFamilyBottomNavListener(this, bottomNavigationView));
        }
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        if (ACTION.equalsIgnoreCase(Constants.ActionList.FOLLOWUP)) {
            startActivityForResult(FormUtils.getStartFormActivity(jsonForm, getString(R.string.pmtct_followup_form_title), this), JsonFormUtils.REQUEST_CODE_GET_JSON);
        } else {
            startActivityForResult(FormUtils.getStartFormActivity(jsonForm, this.getString(org.smartregister.chw.core.R.string.pmtct_registration), this), JsonFormUtils.REQUEST_CODE_GET_JSON);
        }
    }

    public String getCtcNumber() {
        return ctcNumber;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON) {
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
            try {
                JSONObject form = new JSONObject(jsonString);
                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(PMTCT_REGISTRATION)) {
                    JSONArray fields = form.getJSONObject("step2").getJSONArray(FIELDS);
                    JSONObject testResultsJsonObject = JsonFormUtils.getFieldJSONObject(fields, "test_results");
                    String testResult = POSITIVE;
                    if (testResultsJsonObject != null) {
                        testResult = testResultsJsonObject.getString(VALUE);
                    }

                    try {
                        if (testResult.equalsIgnoreCase(POSITIVE))
                            PncVisitUtils.createHeiRegistrationEvent(form.getString("entity_id"));
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
            } catch (JSONException jsonException) {
                Timber.e(jsonException);
            }
        }
    }
}
