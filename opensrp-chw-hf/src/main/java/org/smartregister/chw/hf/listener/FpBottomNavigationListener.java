package org.smartregister.chw.hf.listener;

import android.app.Activity;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import org.smartregister.chw.anc.listener.BaseAncBottomNavigationListener;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.FpRegisterActivity;

public class FpBottomNavigationListener extends BaseAncBottomNavigationListener {
    private final FpRegisterActivity baseRegisterActivity;

    public FpBottomNavigationListener(Activity context) {
        super(context);
        this.baseRegisterActivity = (FpRegisterActivity) context;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_fp) {
            baseRegisterActivity.switchToFragment(0);
            return true;
        } else if (item.getItemId() == R.id.action_view_ecp_clients) {
            baseRegisterActivity.switchToFragment(1);
            return true;
        } else
            return super.onNavigationItemSelected(item);
    }
}
