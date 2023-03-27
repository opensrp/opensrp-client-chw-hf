package org.smartregister.chw.hf.activity;

import android.os.Bundle;
import android.os.Parcel;

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

    //Implementation that handles passing of large data between activities that sometimes caused TransactionTooLarge exceptions
    public final int MAX_BUNDLE_SIZE = 300;
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        long bundleSize = getBundleSize(outState);
        if (bundleSize > MAX_BUNDLE_SIZE * 1024) {
            outState.clear();
        }
    }

    private long getBundleSize(Bundle bundle) {
        long dataSize;
        Parcel obtain = Parcel.obtain();
        try {
            obtain.writeBundle(bundle);
            dataSize = obtain.dataSize();
        } finally {
            obtain.recycle();
        }
        return dataSize;
    }
}
