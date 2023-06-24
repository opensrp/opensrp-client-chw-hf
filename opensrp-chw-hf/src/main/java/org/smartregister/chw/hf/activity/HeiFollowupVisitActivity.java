package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.core.utils.Utils.getCommonPersonObjectClient;
import static org.smartregister.chw.core.utils.Utils.getDuration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.task.RunnableTask;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.domain.JSONObjectHolder;
import org.smartregister.chw.hf.interactor.HeiFollowupVisitInteractor;
import org.smartregister.chw.hf.schedulers.HfScheduleTaskExecutor;
import org.smartregister.chw.pmtct.activity.BasePmtctHomeVisitActivity;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;
import org.smartregister.chw.pmtct.presenter.BasePmtctHomeVisitPresenter;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.util.LangUtils;

import java.text.MessageFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;

public class HeiFollowupVisitActivity extends BasePmtctHomeVisitActivity {
    private long mLastExecutionTime = 0;

    private static final long MINIMUM_INTERVAL_MS = 3000;

    public static void startHeiFollowUpActivity(Activity activity, String baseEntityID, Boolean editMode) {
        Intent intent = new Intent(activity, HeiFollowupVisitActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, editMode);
        activity.startActivity(intent);
    }

    @Override
    protected void registerPresenter() {
        presenter = new BasePmtctHomeVisitPresenter(memberObject, this, new HeiFollowupVisitInteractor());
    }

    @Override
    public void submittedAndClose() {
        try {
            Runnable runnable = () -> HfScheduleTaskExecutor.getInstance().execute(memberObject.getBaseEntityId(), org.smartregister.chw.hf.utils.Constants.Events.HEI_FOLLOWUP, new Date());
            Utils.startAsyncTask(new RunnableTask(runnable), null);
        } catch (Exception e) {
            Timber.e(e);
        }
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


        Intent intent;
        try {
            if (jsonForm.getString("encounter_type").equals("HEI Baseline Investigation")) {
                // Set the large JSONObject in JSONObjectHolder
                JSONObjectHolder.getInstance().setLargeJSONObject(jsonForm);
                intent = new Intent(this, HfJsonWizardFormActivity.class);
            } else {
                intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
                intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
        }

        intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, false);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);


        //Necessary evil to disable multiple sequential clicks of actions that do sometimes cause app crushes
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastExecutionTime < MINIMUM_INTERVAL_MS) {
            // too soon to execute the function again, ignore this call
            return;
        }

        // record the current time as the last execution time
        mLastExecutionTime = currentTime;
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
        CommonPersonObjectClient client = getCommonPersonObjectClient(memberObject.getBaseEntityId());
        String age = Utils.getTranslatedDate(getDuration(Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false)), this);

        tvTitle.setText(MessageFormat.format("{0}, {1} \u00B7 {2}", memberObject.getFullName(), age, getString(R.string.hei_visit)));
    }

    @Override
    public void initializeActions(LinkedHashMap<String, BasePmtctHomeVisitAction> map) {
        actionList.clear();
        super.initializeActions(map);
        redrawVisitUI();
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
    public void redrawVisitUI() {
        boolean valid = actionList.size() > 0;
        for (Map.Entry<String, BasePmtctHomeVisitAction> entry : actionList.entrySet()) {
            BasePmtctHomeVisitAction action = entry.getValue();
            if (
                    //Updated the condition to only allow submission if the action is not completed in the L&D Registration
                    (!action.isOptional() && (action.getActionStatus() != BasePmtctHomeVisitAction.Status.COMPLETED && action.isValid()))
                            || !action.isEnabled()
            ) {
                valid = false;
                break;
            }
        }

        int res_color = valid ? org.smartregister.ld.R.color.white : org.smartregister.ld.R.color.light_grey;
        tvSubmit.setTextColor(getResources().getColor(res_color));
        tvSubmit.setOnClickListener(valid ? this : null);

        mAdapter.notifyDataSetChanged();
    }
}
