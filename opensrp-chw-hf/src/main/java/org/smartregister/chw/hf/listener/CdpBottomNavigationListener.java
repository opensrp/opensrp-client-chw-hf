package org.smartregister.chw.hf.listener;

import android.app.Activity;
import android.view.MenuItem;

import org.smartregister.chw.cdp.activity.BaseCdpRegisterActivity;
import org.smartregister.chw.cdp.listener.BaseCdpBottomNavigationListener;

import androidx.annotation.NonNull;

public class CdpBottomNavigationListener extends BaseCdpBottomNavigationListener {
    private final Activity context;

    public CdpBottomNavigationListener(Activity context) {
        super(context);
        this.context = context;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        super.onNavigationItemSelected(item);

        BaseCdpRegisterActivity baseRegisterActivity = (BaseCdpRegisterActivity) context;
        int itemId = item.getItemId();
        if (itemId == org.smartregister.cdp.R.id.action_family) {
            baseRegisterActivity.switchToBaseFragment();
        } else if (itemId == org.smartregister.cdp.R.id.action_order_receive) {
            baseRegisterActivity.switchToFragment(1);
        } else if (itemId == org.smartregister.cdp.R.id.action_receive_from_msd) {
            baseRegisterActivity.switchToFragment(2);
        } else if (itemId == org.smartregister.cdp.R.id.action_add_outlet) {
            baseRegisterActivity.startOutletForm();
        }

        return true;
    }
}
