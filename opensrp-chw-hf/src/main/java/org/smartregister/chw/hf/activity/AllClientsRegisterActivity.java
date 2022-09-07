package org.smartregister.chw.hf.activity;

import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.smartregister.AllConstants;
import org.smartregister.chw.core.activity.CoreAllClientsRegisterActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.fragment.AdvancedSearchFragment;
import org.smartregister.chw.hf.fragment.AllClientsRegisterFragment;
import org.smartregister.chw.hf.model.HfAllClientsRegisterModel;
import org.smartregister.chw.hf.presenter.HfAllClientRegisterPresenter;
import org.smartregister.family.util.Utils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.opd.contract.OpdRegisterActivityContract;
import org.smartregister.opd.presenter.BaseOpdRegisterActivityPresenter;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class AllClientsRegisterActivity extends CoreAllClientsRegisterActivity {

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new AllClientsRegisterFragment();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        Fragment[] fragments = new Fragment[1];
        fragments[0] = new AdvancedSearchFragment(false);
        return fragments;
    }

    @Override
    public List<String> getViewIdentifiers() {
        return Arrays.asList(Utils.metadata().familyRegister.config);
    }

    @Override
    public void startRegistration() {
        startFormActivity(CoreConstants.JSON_FORM.getAllClientRegistrationForm(), null, null);
    }


    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        try {
            String locationId = org.smartregister.family.util.Utils.context().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
            ((HfAllClientRegisterPresenter) presenter()).startForm(formName, entityId, metaData, locationId);

        } catch (Exception e) {
            Timber.e(e);
            displayToast(org.smartregister.family.R.string.error_unable_to_start_form);
        }
    }


    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_all_clients_menu);
        bottomNavigationHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_job_aids);
        bottomNavigationView.getMenu().findItem(R.id.action_register).setTitle(R.string.add_client).setIcon(R.drawable.ic_input_add);
    }

    @Override
    protected BaseOpdRegisterActivityPresenter createPresenter(
            @NonNull OpdRegisterActivityContract.View view, @NonNull OpdRegisterActivityContract.Model model) {
        return new HfAllClientRegisterPresenter(view, model);
    }

    @Override
    public void switchToBaseFragment() {
        Intent intent = new Intent(this, AllClientsRegisterActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_search:
                switchToFragment(1);
                return true;
            case R.id.action_family:
                switchToBaseFragment();
                break;
            case R.id.action_register:
                startRegistration();
                break;
            default:
                return true;
        }
        return true;
    }

    public OpdRegisterActivityContract.Model createActivityModel() {
        return new HfAllClientsRegisterModel(this);
    }

}
