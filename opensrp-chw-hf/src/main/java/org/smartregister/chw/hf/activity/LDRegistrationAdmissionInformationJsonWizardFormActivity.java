package org.smartregister.chw.hf.activity;

import com.vijay.jsonwizard.activities.JsonWizardFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.utils.NativeFormLangUtils;

import org.smartregister.chw.hf.fragment.HfJsonFormFragment;

public class LDRegistrationAdmissionInformationJsonWizardFormActivity extends JsonWizardFormActivity {
    @Override
    protected String getJsonForm() {
        String jsonForm = LDRegistrationFormActivity.LABOUR_AND_DELIVERY_REGISTRATION_ADMISSION_INFORMATION;

        if (translateForm) {
            jsonForm = NativeFormLangUtils.getTranslatedStringWithDBResourceBundle(this, jsonForm, null);
        }
        return jsonForm;
    }

    @Override
    public synchronized void initializeFormFragment() {
        isFormFragmentInitialized = true;
        HfJsonFormFragment formFragment = HfJsonFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);
        getSupportFragmentManager().beginTransaction()
                .add(com.vijay.jsonwizard.R.id.container, formFragment).commitAllowingStateLoss();
    }
}
