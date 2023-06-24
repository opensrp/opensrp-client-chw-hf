package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.chw.core.task.RunnableTask;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.PmtctFollowupVisitInteractor;
import org.smartregister.chw.hf.schedulers.HfScheduleTaskExecutor;
import org.smartregister.chw.pmtct.activity.BasePmtctHomeVisitActivity;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;
import org.smartregister.chw.pmtct.presenter.BasePmtctHomeVisitPresenter;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.util.LangUtils;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

public class PmtctFollowupVisitActivity extends BasePmtctHomeVisitActivity {
    public static void startPmtctFollowUpActivity(Activity activity, String baseEntityID, Boolean editMode) {
        Intent intent = new Intent(activity, PmtctFollowupVisitActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, editMode);
        activity.startActivity(intent);
    }

    @Override
    protected void registerPresenter() {
        presenter = new BasePmtctHomeVisitPresenter(memberObject, this, new PmtctFollowupVisitInteractor());
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

    @Override
    public void initializeActions(LinkedHashMap<String, BasePmtctHomeVisitAction> map) {
        actionList.clear();

        ConcurrentHashMap<String, BasePmtctHomeVisitAction> concurrentHashMap = new ConcurrentHashMap<>(map);

        //Necessary evil to rearrange the actions according to a specific arrangement
        if (map.containsKey(getString(R.string.pmtct_followup_status_title))) {
            BasePmtctHomeVisitAction followupStatusVisitAction = map.get(getString(R.string.pmtct_followup_status_title));
            actionList.put(getString(R.string.pmtct_followup_status_title), followupStatusVisitAction);
        }
        //====================End of Necessary evil ====================================

        for (Map.Entry<String, BasePmtctHomeVisitAction> entry : concurrentHashMap.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(getString(R.string.next_visit))) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    actionList.putIfAbsent(entry.getKey(), entry.getValue());
                } else {
                    actionList.put(entry.getKey(), entry.getValue());
                }
            }
        }

        if (map.containsKey(getString(R.string.next_visit))) {
            actionList.put(getString(R.string.next_visit), map.get(getString(R.string.next_visit)));
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        displayProgressBar(false);
    }
}
