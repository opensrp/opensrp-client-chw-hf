package org.smartregister.chw.hf.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.json.JSONObject;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.fragment.AllClientsRegisterFragment;
import org.smartregister.chw.hf.presenter.AllClientsRegisterPresenter;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.opd.activity.BaseOpdRegisterActivity;
import org.smartregister.opd.contract.OpdRegisterActivityContract;
import org.smartregister.opd.presenter.BaseOpdRegisterActivityPresenter;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class AllClientsRegisterActivity extends BaseOpdRegisterActivity {

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new AllClientsRegisterFragment();
    }

    @Override
    public void startFormActivity(JSONObject jsonObject) {
        //Overridden from the extended abstract class - feature not required for HF app
    }

    @Override
    protected void onActivityResultExtended(int i, int i1, Intent intent) {
        //Overridden from the extended abstract class - feature not required for HF app
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationMenu.getInstance(this, null, null);
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);
        FamilyRegisterActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
    }

    @Override
    protected BaseOpdRegisterActivityPresenter createPresenter(@NonNull OpdRegisterActivityContract.View view, @NonNull OpdRegisterActivityContract.Model model) {
        return new AllClientsRegisterPresenter(view, model);
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter()
                    .setSelectedView(CoreConstants.DrawerMenu.ALL_CLIENTS);
        }
    }

    @Override
    public void switchToBaseFragment() {
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void startRegistration() {
        //Overridden from the abstract class - registration feature not required for HF app
    }
}
