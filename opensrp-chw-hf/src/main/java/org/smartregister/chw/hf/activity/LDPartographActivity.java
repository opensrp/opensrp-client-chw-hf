package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.hf.interactor.LDRegistrationInteractor;
import org.smartregister.chw.ld.activity.BaseLDVisitActivity;
import org.smartregister.chw.ld.presenter.BaseLDVisitPresenter;

/**
 * @author issyzac 5/7/22
 */
public class LDPartographActivity extends BaseLDVisitActivity {

    public static void startMe(Activity activity, String baseEntityID, Boolean isEditMode, String fullName, String age) {
        Intent intent = new Intent(activity, LDRegistrationFormActivity.class);
        intent.putExtra(org.smartregister.chw.ld.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(org.smartregister.chw.ld.util.Constants.ACTIVITY_PAYLOAD.EDIT_MODE, isEditMode);
        intent.putExtra("FULL_NAME", fullName);
        intent.putExtra("AGE", age);
        activity.startActivityForResult(intent, org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT);
    }

    @Override
    protected void registerPresenter() {
        presenter = new BaseLDVisitPresenter(memberObject, this, new LDRegistrationInteractor(baseEntityID));
    }

}
