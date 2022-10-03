package org.smartregister.chw.hf.activity;

import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.smartregister.chw.core.activity.CoreCdpRegisterActivity;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.fragment.CdpReceiveMsdRegisterFragment;
import org.smartregister.chw.hf.fragment.OrdersRegisterFragment;
import org.smartregister.chw.hf.fragment.RequestOrdersRegisterFragment;
import org.smartregister.chw.hf.listener.CdpBottomNavigationListener;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.listener.BottomNavigationListener;
import org.smartregister.view.fragment.BaseRegisterFragment;

import androidx.fragment.app.Fragment;

import static org.smartregister.util.Utils.getAllSharedPreferences;

public class CdpRegisterActivity extends CoreCdpRegisterActivity {
    private final String userLocationTag = getAllSharedPreferences().fetchUserLocationTag();

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new RequestOrdersRegisterFragment();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        return new Fragment[]{
                new OrdersRegisterFragment(),
                new CdpReceiveMsdRegisterFragment()
        };
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.CDP_HF);
        }
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
            BottomNavigationListener familyBottomNavigationListener = new CdpBottomNavigationListener(this);
            bottomNavigationView.setOnNavigationItemSelectedListener(familyBottomNavigationListener);
        }
    }

    @Override
    public int getMenuResource() {
        if (userLocationTag.contains("msd_code")) {
            return R.menu.bottom_nav_cdp_msd_facility;
        }
        return super.getMenuResource();
    }
}
