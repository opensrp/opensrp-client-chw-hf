package org.smartregister.chw.hf.activity;

import android.content.Intent;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.smartregister.chw.core.activity.BaseReferralRegister;
import org.smartregister.chw.core.presenter.BaseReferralPresenter;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.fragment.IssuedReferralsRegisterFragment;
import org.smartregister.chw.hf.fragment.ReferralRegisterFragment;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ReferralRegisterActivity extends BaseReferralRegister implements BottomNavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void initializePresenter() {
        presenter = new BaseReferralPresenter();
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new ReferralRegisterFragment();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        return new Fragment[]{
                new IssuedReferralsRegisterFragment()
        };
    }

    @Override
    public void switchToBaseFragment() {
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().clear();

        bottomNavigationView.inflateMenu(R.menu.bottom_nav_referral_menu);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_home) {
            switchToFragment(0);
            return true;
        } else if (menuItem.getItemId() == R.id.action_sent_referrals) {
            switchToFragment(1);
            return true;
        }
        return false;
    }
}
