package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.MenuRes;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.smartregister.chw.core.activity.CoreFpRegisterActivity;
import org.smartregister.chw.fp.model.BaseFpRegisterModel;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.fragment.FpEcpRegisterFragment;
import org.smartregister.chw.hf.fragment.FpRegisterFragment;
import org.smartregister.chw.hf.interactor.HFFamilyPlanningRegisterInteractor;
import org.smartregister.chw.hf.listener.FpBottomNavigationListener;
import org.smartregister.chw.hf.presenter.FpRegisterPresenter;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.listener.BottomNavigationListener;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class FpRegisterActivity extends CoreFpRegisterActivity {

    private static String baseEntityId;
    private static String fpFormName;

    public static void startFpRegistrationActivity(Activity activity, String baseEntityID, String formName) {
        Intent intent = new Intent(activity, FpRegisterActivity.class);
        intent.putExtra(FamilyPlanningConstants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(FamilyPlanningConstants.ACTIVITY_PAYLOAD.FP_FORM_NAME, formName);
        intent.putExtra(FamilyPlanningConstants.ACTIVITY_PAYLOAD.ACTION, FamilyPlanningConstants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        baseEntityId = baseEntityID;
        fpFormName = formName;
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FORM_NAME != null && FORM_NAME.equalsIgnoreCase(org.smartregister.chw.hf.utils.Constants.JsonForm.getFPEcpScreening())) {
            switchToFragment(1);
        }
    }

    @Override
    protected void initializePresenter() {
        presenter = new FpRegisterPresenter(this, new BaseFpRegisterModel(), new HFFamilyPlanningRegisterInteractor());
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new FpRegisterFragment();
    }

    protected Fragment[] getOtherFragments() {
        Fragment[] fragments = new Fragment[1];
        fragments[0] = new FpEcpRegisterFragment();
        return fragments;
    }

    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_clients);
            bottomNavigationView.getMenu().removeItem(org.smartregister.chw.tb.R.id.action_register);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_search);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_library);
            bottomNavigationView.getMenu().removeItem(R.id.action_scan_qr);
            BottomNavigationListener fpBottomNavigationListener = getBottomNavigation(this);
            bottomNavigationView.setOnNavigationItemSelectedListener(fpBottomNavigationListener);

        }
    }

    public BottomNavigationListener getBottomNavigation(Activity activity) {
        return new FpBottomNavigationListener(activity);
    }

    @MenuRes
    public int getMenuResource() {
        return R.menu.bottom_nav_fp_menu;
    }

}
