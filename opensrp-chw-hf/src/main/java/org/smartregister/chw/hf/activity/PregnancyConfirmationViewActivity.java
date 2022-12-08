package org.smartregister.chw.hf.activity;

import static android.view.View.GONE;
import static org.smartregister.chw.core.utils.Utils.passToolbarTitle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.AllClientsUtils;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.Map;

public class PregnancyConfirmationViewActivity extends ReferralTaskViewActivity implements View.OnClickListener {
    private static String CLIENT_PHONE_NUMBER;

    public static void startPregnancyConfirmationViewActivity(Activity activity, CommonPersonObjectClient personObjectClient, Task task, String startingActivity, Map<String, String> details) {
        PregnancyConfirmationViewActivity.personObjectClient = personObjectClient;
        CLIENT_PHONE_NUMBER = details.get("family_member_phone_number");
        Intent intent = new Intent(activity, PregnancyConfirmationViewActivity.class);
        intent.putExtra(CoreConstants.INTENT_KEY.USERS_TASKS, task);
        intent.putExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON, personObjectClient);
        intent.putExtra(CoreConstants.INTENT_KEY.STARTING_ACTIVITY, startingActivity);
        passToolbarTitle(activity, intent);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.referrals_tasks_view_layout);
        if (getIntent().getExtras() != null) {
            extraClientTask();
            extraDetails();
            setStartingActivity((String) getIntent().getSerializableExtra(CoreConstants.INTENT_KEY.STARTING_ACTIVITY));
            inflateToolbar();
            setUpViews();
        }
    }

    @Override
    protected void onCreation() {
        //overridden
    }

    @Override
    protected void onResumption() {
        //Overridden
    }

    public void setUpViews() {
        clientName = findViewById(R.id.client_name);
        careGiverName = findViewById(R.id.care_giver_name);
        childName = findViewById(R.id.child_name);
        careGiverPhone = findViewById(R.id.care_giver_phone);
        clientReferralProblem = findViewById(R.id.client_referral_problem);
        chwDetailsNames = findViewById(R.id.chw_details_names);
        referralDate = findViewById(R.id.referral_date);

        womanGaLayout = findViewById(R.id.woman_ga_layout);
        careGiverLayout = findViewById(R.id.care_giver_name_layout);
        childNameLayout = findViewById(R.id.child_name_layout);

        womanGa = findViewById(R.id.woman_ga);
        CustomFontTextView viewProfile = findViewById(R.id.view_profile);

        CustomFontTextView markAskDone = findViewById(R.id.mark_ask_done);
        markAskDone.setOnClickListener(this);

        if (getStartingActivity().equals(CoreConstants.REGISTERED_ACTIVITIES.REFERRALS_REGISTER_ACTIVITY)) {
            viewProfile.setOnClickListener(this);
        } else {
            viewProfile.setVisibility(View.INVISIBLE);
        }
        getReferralDetails();

        ((CustomFontTextView) findViewById(R.id.toolbar_title)).setText(getResources().getString(R.string.return_to_all_anc_women));
        ((CustomFontTextView) findViewById(R.id.mark_ask_done)).setText(getResources().getString(R.string.pregnancy_confirmation));
        findViewById(R.id.view_profile).setVisibility(GONE);
    }

    public void setStartingActivity(String startingActivity) {
        this.startingActivity = startingActivity;
    }

    public String getBaseEntityId() {
        return task.getForEntity();
    }

    @Override
    protected void getReferralDetails() {
        super.getReferralDetails();
        careGiverPhone.setText(StringUtils.isBlank(CLIENT_PHONE_NUMBER) ? getString(org.smartregister.chw.core.R.string.phone_not_provided) : CLIENT_PHONE_NUMBER);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.view_profile) {
            personObjectClient.getDetails().put(OpdDbConstants.KEY.REGISTER_TYPE, mapTaskFocusToRegisterType());
            AllClientsUtils.goToClientProfile(this, personObjectClient);
        } else if (view.getId() == R.id.mark_ask_done) {
            AncRegisterActivity.startAncRegistrationActivity(PregnancyConfirmationViewActivity.this, getBaseEntityId(), getFamilyMemberContacts(),
                    CoreConstants.JSON_FORM.ANC_PREGNANCY_CONFIRMATION, null, getBaseEntityId(), name, task.getIdentifier());
        }
    }

    @NonNull
    private String mapTaskFocusToRegisterType() {
        switch (task.getFocus()) {
            case CoreConstants.TASKS_FOCUS.SICK_CHILD:
                return CoreConstants.REGISTER_TYPE.CHILD;
            case CoreConstants.TASKS_FOCUS.SUSPECTED_MALARIA:
                return CoreConstants.REGISTER_TYPE.MALARIA;
            case CoreConstants.TASKS_FOCUS.ANC_DANGER_SIGNS:
                return CoreConstants.REGISTER_TYPE.ANC;
            case CoreConstants.TASKS_FOCUS.PNC_DANGER_SIGNS:
                return CoreConstants.REGISTER_TYPE.PNC;
            case CoreConstants.TASKS_FOCUS.FP_SIDE_EFFECTS:
                return CoreConstants.REGISTER_TYPE.FAMILY_PLANNING;
            default:
                return "";
        }
    }
}