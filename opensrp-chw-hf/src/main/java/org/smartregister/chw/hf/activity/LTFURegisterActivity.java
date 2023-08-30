package org.smartregister.chw.hf.activity;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.smartregister.chw.core.activity.BaseReferralRegister;
import org.smartregister.chw.core.presenter.BaseReferralPresenter;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.fragment.LTFUReferralsRegisterFragment;
import org.smartregister.chw.hf.fragment.SuccessfulReferralsRegisterFragment;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class LTFURegisterActivity extends BaseReferralRegister implements BottomNavigationView.OnNavigationItemSelectedListener {


    @Override
    protected void initializePresenter() {
        presenter = new BaseReferralPresenter();
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new LTFUReferralsRegisterFragment();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        return new SuccessfulReferralsRegisterFragment[]{new SuccessfulReferralsRegisterFragment()};
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);
        bottomNavigationView.getMenu().clear();

        bottomNavigationView.inflateMenu(R.menu.ltfu_bottom_nav_menu);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_home) {
            switchToFragment(0);
            return true;
        } else if (menuItem.getItemId() == R.id.action_completed_ltfu) {
            switchToFragment(1);
            return true;
        } else
            return false;
    }
}
