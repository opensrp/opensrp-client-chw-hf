package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.hf.utils.Constants.JsonForm.getPmtctRegistration;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.getPmtctRegistrationForClientsKnownOnArtForm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.json.JSONObject;
import org.smartregister.chw.core.activity.CorePmtctRegisterActivity;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.fragment.PmtctRegisterFragment;
import org.smartregister.chw.hf.listener.HfFamilyBottomNavListener;
import org.smartregister.chw.hf.presenter.PmtctRegisterPresenter;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.interactor.BasePmtctRegisterInteractor;
import org.smartregister.chw.pmtct.model.BasePmtctRegisterModel;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;

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
        NavigationMenu.getInstance(this, null, null);
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
}
