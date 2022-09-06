package org.smartregister.chw.hf.activity;

import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.smartregister.chw.cdp.listener.BaseCdpBottomNavigationListener;
import org.smartregister.chw.core.activity.CoreCdpRegisterActivity;
import org.smartregister.chw.core.fragment.CoreOrdersRegisterFragment;
import org.smartregister.chw.hf.fragment.RequestOrdersRegisterFragment;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.listener.BottomNavigationListener;
import org.smartregister.view.fragment.BaseRegisterFragment;

import androidx.fragment.app.Fragment;

public class CdpRegisterActivity extends CoreCdpRegisterActivity {

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new RequestOrdersRegisterFragment();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        return new Fragment[]{
                new CoreOrdersRegisterFragment()
        };
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_clients);
            bottomNavigationView.getMenu().removeItem(org.smartregister.cdp.R.id.action_register);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_search);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_library);
            bottomNavigationView.inflateMenu(getMenuResource());
            bottomNavigationHelper.disableShiftMode(bottomNavigationView);
            bottomNavigationView.getMenu().removeItem(org.smartregister.cdp.R.id.action_add_outlet);
            BottomNavigationListener familyBottomNavigationListener = new BaseCdpBottomNavigationListener(this);
            bottomNavigationView.setOnNavigationItemSelectedListener(familyBottomNavigationListener);
        }
    }
}
