package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.MenuRes;
import androidx.fragment.app.Fragment;

import com.vijay.jsonwizard.domain.Form;

import org.smartregister.chw.core.activity.CoreSbcRegisterActivity;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.fragment.SbcRegisterFragment;
import org.smartregister.chw.sbc.util.Constants;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class SbcRegisterActivity extends CoreSbcRegisterActivity {

    public static void startRegistration(Activity activity, String baseEntityId) {
        Intent intent = new Intent(activity, SbcRegisterActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.ACTION, Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.SBC_FORM_NAME, Constants.FORMS.SBC_ENROLLMENT);

        activity.startActivity(intent);
    }

    @Override
    public Form getFormConfig() {
        Form form = new Form();
        form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
        form.setWizard(true);
        form.setName(getString(R.string.sbc_registration));
        form.setNavigationBackground(org.smartregister.chw.core.R.color.family_navigation);
        form.setNextLabel(this.getResources().getString(org.smartregister.chw.core.R.string.next));
        form.setPreviousLabel(this.getResources().getString(org.smartregister.chw.core.R.string.back));
        form.setSaveLabel(this.getResources().getString(org.smartregister.chw.core.R.string.save));
        return form;
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new SbcRegisterFragment();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        return new Fragment[]{};
    }

    @MenuRes
    public int getMenuResource() {
        return org.smartregister.chw.sbc.R.menu.bottom_nav_sbc_menu;
    }

}
