package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.chw.core.task.RunnableTask;
import org.smartregister.chw.hf.interactor.PmtctFollowupVisitInteractor;
import org.smartregister.chw.hf.schedulers.HfScheduleTaskExecutor;
import org.smartregister.chw.pmtct.activity.BasePmtctHomeVisitActivity;
import org.smartregister.chw.pmtct.presenter.BasePmtctHomeVisitPresenter;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.util.LangUtils;

import java.util.Date;

import timber.log.Timber;

public class PmtctFollowupVisitActivity extends BasePmtctHomeVisitActivity {
    public static void startPmtctFollowUpActivity(Activity activity, String baseEntityID, Boolean editMode) {
        Intent intent = new Intent(activity, PmtctFollowupVisitActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID,baseEntityID );
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, editMode);
        activity.startActivity(intent);
    }

    @Override
    protected void registerPresenter() {
        presenter = new BasePmtctHomeVisitPresenter(memberObject,this,new PmtctFollowupVisitInteractor());
    }

    @Override
    public void submittedAndClose() {
        Runnable runnable = () -> HfScheduleTaskExecutor.getInstance().execute(memberObject.getBaseEntityId(), Constants.EVENT_TYPE.PMTCT_FOLLOWUP, new Date());
        Utils.startAsyncTask(new RunnableTask(runnable), null);
        super.submittedAndClose();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Override
    protected void attachBaseContext(Context base) {
        // get language from prefs
        String lang = LangUtils.getLanguage(base.getApplicationContext());
        super.attachBaseContext(LangUtils.setAppLocale(base, lang));
    }

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
