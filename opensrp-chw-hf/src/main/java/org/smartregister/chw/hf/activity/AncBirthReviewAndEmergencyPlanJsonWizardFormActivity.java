package org.smartregister.chw.hf.activity;

import com.vijay.jsonwizard.activities.JsonWizardFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.utils.NativeFormLangUtils;

import org.smartregister.chw.hf.fragment.AncJsonFormFragment;

public class AncBirthReviewAndEmergencyPlanJsonWizardFormActivity extends JsonWizardFormActivity {
    @Override
    protected String getJsonForm() {
        String jsonForm = AncFirstFacilityVisitActivity.ANC_BIRTH_REVIEW_AND_EMERGENCY_PLAN;

        if (translateForm) {
            jsonForm = NativeFormLangUtils.getTranslatedStringWithDBResourceBundle(this, jsonForm, null);
        }
        return jsonForm;
    }

    @Override
    public synchronized void initializeFormFragment() {
        isFormFragmentInitialized = true;
        AncJsonFormFragment formFragment = AncJsonFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);
        getSupportFragmentManager().beginTransaction()
                .add(com.vijay.jsonwizard.R.id.container, formFragment).commitAllowingStateLoss();
    }
}
