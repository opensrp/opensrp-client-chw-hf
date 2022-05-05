package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.hf.utils.Constants.JsonForm.getLdRegistration;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.core.activity.CoreLDRegisterActivity;
import org.smartregister.chw.hf.fragment.LDRegisterFragment;
import org.smartregister.chw.ld.util.Constants;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class LDRegisterActivity extends CoreLDRegisterActivity {
    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new LDRegisterFragment();
    }

    public static void startLDRegistrationActivity(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, LDRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.ld.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.LD_FORM_NAME, getLdRegistration());
        intent.putExtra(org.smartregister.chw.ld.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.ld.util.Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        activity.startActivity(intent);
    }
}
