package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.core.activity.CoreFpRegisterActivity;
import org.smartregister.chw.fp.model.BaseFpRegisterModel;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.hf.fragment.FpRegisterFragment;
import org.smartregister.chw.hf.interactor.HFFamilyPlanningRegisterInteractor;
import org.smartregister.chw.hf.presenter.FpRegisterPresenter;
import org.smartregister.helper.BottomNavigationHelper;
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
    protected void initializePresenter() {
        presenter = new FpRegisterPresenter(this, new BaseFpRegisterModel(), new HFFamilyPlanningRegisterInteractor());
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new FpRegisterFragment();
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);
        FamilyRegisterActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
    }
}
