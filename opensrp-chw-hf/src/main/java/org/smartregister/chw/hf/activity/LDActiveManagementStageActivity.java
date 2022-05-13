package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.chw.hf.interactor.LDActiveManagementStageActivityInteractor;
import org.smartregister.chw.ld.activity.BaseLDVisitActivity;
import org.smartregister.chw.ld.presenter.BaseLDVisitPresenter;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;

/**
 * Created by Kassim Sheghembe on 2022-05-13
 */
public class LDActiveManagementStageActivity extends BaseLDVisitActivity {

    public static void startActiveManagementActivity(Activity activity, String baseEntityID, Boolean editMode) {

        Intent intent = new Intent(activity, LDActiveManagementStageActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, editMode);

        activity.startActivityForResult(intent, org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT);

    }

    @Override
    protected void registerPresenter() {
        presenter = new BaseLDVisitPresenter(memberObject, this, new LDActiveManagementStageActivityInteractor());
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Form form = new Form();
        form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
        form.setWizard(false);

        Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
        intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
        intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, false);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }
}
