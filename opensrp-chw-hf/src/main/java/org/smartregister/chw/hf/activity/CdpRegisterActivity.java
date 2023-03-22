package org.smartregister.chw.hf.activity;

import static org.smartregister.util.Utils.getAllSharedPreferences;

import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.smartregister.chw.core.activity.CoreCdpRegisterActivity;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.fragment.CdpReceiveFromOrganizationsRegisterFragment;
import org.smartregister.chw.hf.fragment.OrdersRegisterFragment;
import org.smartregister.chw.hf.fragment.RequestOrdersRegisterFragment;
import org.smartregister.chw.hf.listener.CdpBottomNavigationListener;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.listener.BottomNavigationListener;
import org.smartregister.view.fragment.BaseRegisterFragment;

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
                new CdpReceiveFromOrganizationsRegisterFragment()
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


            BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);

            for (int i = 2; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);

                TextView smallLabel = item.findViewById(R.id.smallLabel);
                TextView largeLabel = item.findViewById(R.id.largeLabel);

                smallLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                largeLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

                smallLabel.setGravity(Gravity.CENTER);
                largeLabel.setGravity(Gravity.CENTER);

                smallLabel.setMaxLines(2);
                largeLabel.setMaxLines(2);

            }
            bottomNavigationView.invalidate();
            bottomNavigationView.requestLayout();
        }
    }
}
