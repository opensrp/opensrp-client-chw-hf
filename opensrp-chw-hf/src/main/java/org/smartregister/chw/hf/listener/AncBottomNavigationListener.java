package org.smartregister.chw.hf.listener;

import android.app.Activity;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import org.smartregister.chw.anc.listener.BaseAncBottomNavigationListener;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.AncRegisterActivity;

public class AncBottomNavigationListener extends BaseAncBottomNavigationListener {
    private final AncRegisterActivity baseRegisterActivity;

    public AncBottomNavigationListener(Activity context) {
        super(context);
        this.baseRegisterActivity = (AncRegisterActivity) context;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_anc) {
            baseRegisterActivity.switchToFragment(0);
            return true;
        } else if (item.getItemId() == R.id.action_view_pregnancy_confirmation_referrals) {
            baseRegisterActivity.switchToFragment(1);
            return true;
        } else
            return super.onNavigationItemSelected(item);
    }
}
