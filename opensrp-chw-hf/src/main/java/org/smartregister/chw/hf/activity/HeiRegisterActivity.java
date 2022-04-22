package org.smartregister.chw.hf.activity;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.custom_view.FacilityMenu;
import org.smartregister.chw.hf.fragment.HeiRegisterFragment;
import org.smartregister.chw.hf.listener.HfFamilyBottomNavListener;
import org.smartregister.chw.hf.presenter.HeiRegisterPresenter;
import org.smartregister.chw.pmtct.activity.BasePmtctRegisterActivity;
import org.smartregister.chw.pmtct.interactor.BasePmtctRegisterInteractor;
import org.smartregister.chw.pmtct.model.BasePmtctRegisterModel;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class HeiRegisterActivity extends BasePmtctRegisterActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacilityMenu.getInstance(this, null, null);
    }

    @Override
    protected void initializePresenter() {
        presenter = new HeiRegisterPresenter(this, new BasePmtctRegisterModel(), new BasePmtctRegisterInteractor());
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new HeiRegisterFragment();
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_family_menu);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_clients);
            bottomNavigationView.getMenu().removeItem(org.smartregister.chw.tb.R.id.action_register);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_search);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_library);
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_job_aids);
            bottomNavigationView.setOnNavigationItemSelectedListener(new HfFamilyBottomNavListener(this, bottomNavigationView));

        }

    }

    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu =  FacilityMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.HEI);
        }
    }
}
