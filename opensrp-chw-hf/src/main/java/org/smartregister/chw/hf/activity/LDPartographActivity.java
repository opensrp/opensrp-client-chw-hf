package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.chw.core.task.RunnableTask;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.LDPartographInteractor;
import org.smartregister.chw.hf.schedulers.HfScheduleTaskExecutor;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.ld.activity.BaseLDVisitActivity;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.chw.ld.presenter.BaseLDVisitPresenter;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.util.LangUtils;

import java.text.MessageFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * @author issyzac 5/7/22
 */
public class LDPartographActivity extends BaseLDVisitActivity {

    public static void startMe(Activity activity, String baseEntityID, Boolean isEditMode, String fullName, String age) {
        Intent intent = new Intent(activity, LDPartographActivity.class);
        intent.putExtra(org.smartregister.chw.ld.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(org.smartregister.chw.ld.util.Constants.ACTIVITY_PAYLOAD.EDIT_MODE, isEditMode);
        intent.putExtra("FULL_NAME", fullName);
        intent.putExtra("AGE", age);
        activity.startActivityForResult(intent, org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT);
    }

    @Override
    protected void registerPresenter() {
        presenter = new BaseLDVisitPresenter(memberObject, this, new LDPartographInteractor(baseEntityID));
    }

    @Override
    public void submittedAndClose() {
        Runnable runnable = () -> HfScheduleTaskExecutor.getInstance().execute(memberObject.getBaseEntityId(), Constants.Events.LD_PARTOGRAPHY, new Date());
        Utils.startAsyncTask(new RunnableTask(runnable), null);
        super.submittedAndClose();
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
    public void redrawHeader(MemberObject memberObject) {
        tvTitle.setText(MessageFormat.format("{0}, {1} \u00B7 {2}", getIntent().getStringExtra("FULL_NAME"), getIntent().getStringExtra("AGE"), getString(R.string.ld_partograph)));
    }

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void initializeActions(LinkedHashMap<String, BaseLDVisitAction> map) {

        //Clearing action list before recreating
        actionList.clear();

        if (map.containsKey(getString(R.string.ld_partograph_time))){
            BaseLDVisitAction partographTimeAction = map.get(getString(R.string.ld_partograph_time));
            assert partographTimeAction != null;
            actionList.put(getString(R.string.ld_partograph_time), partographTimeAction);
        }

        for (Map.Entry<String, BaseLDVisitAction> entry : map.entrySet()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                actionList.putIfAbsent(entry.getKey(), entry.getValue());
            } else {
                actionList.put(entry.getKey(), entry.getValue());
            }
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }

        displayProgressBar(false);

    }
}
