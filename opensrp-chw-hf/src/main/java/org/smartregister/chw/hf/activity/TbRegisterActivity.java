package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.jetbrains.annotations.NotNull;
import org.smartregister.chw.core.activity.CoreTbRegisterActivity;
import org.smartregister.chw.hf.custom_view.FacilityMenu;
import org.smartregister.chw.hf.fragment.TbFollowupFragment;
import org.smartregister.chw.hf.fragment.TbRegisterFragment;
import org.smartregister.chw.tb.fragment.BaseTbCommunityFollowupRegisterFragment;
import org.smartregister.chw.tb.fragment.BaseTbRegisterFragment;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.listener.BottomNavigationListener;

public class TbRegisterActivity extends CoreTbRegisterActivity {

    public static void startTbFormActivity(Activity activity, String baseEntityID, String formName, String payloadType) {
        Intent intent = new Intent(activity, TbRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.tb.util.Constants.ActivityPayload.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(org.smartregister.chw.tb.util.Constants.ActivityPayload.ACTION, payloadType);
        intent.putExtra(org.smartregister.chw.tb.util.Constants.ActivityPayload.TB_REGISTRATION_FORM_NAME, formName);
        activity.startActivity(intent);
    }

    @NotNull
    @Override
    protected BaseTbRegisterFragment getRegisterFragment() {
        return new TbRegisterFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacilityMenu.getInstance(this, null, null);
    }

    @NotNull
    @Override
    protected BaseTbCommunityFollowupRegisterFragment[] getOtherFragments() {
        return new TbFollowupFragment[]{
                new TbFollowupFragment()};
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_clients);
            bottomNavigationView.getMenu().removeItem(org.smartregister.chw.tb.R.id.action_register);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_search);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_library);
            bottomNavigationView.getMenu().removeItem(org.smartregister.chw.tb.R.id.action_received_referrals);

            bottomNavigationView.inflateMenu(getMenuResource());
            bottomNavigationHelper.disableShiftMode(bottomNavigationView);

            BottomNavigationListener tbBottomNavigationListener = getBottomNavigation(this);
            bottomNavigationView.setOnNavigationItemSelectedListener(tbBottomNavigationListener);

        }
    }

}
 