package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.TransactionTooLargeException;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.activity.BaseAncHomeVisitActivity;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.presenter.BaseAncHomeVisitPresenter;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.task.RunnableTask;
import org.smartregister.chw.hf.domain.JSONObjectHolder;
import org.smartregister.chw.hf.interactor.AncFirstFacilityVisitInteractor;
import org.smartregister.chw.hf.schedulers.HfScheduleTaskExecutor;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.util.LangUtils;

import java.text.MessageFormat;
import java.util.Date;

import timber.log.Timber;

/**
 * @author ilakozejumanne@gmail.com
 * 11-10-2021
 */
public class AncFirstFacilityVisitActivity extends BaseAncHomeVisitActivity {
    private long mLastExecutionTime = 0;
    private static final long MINIMUM_INTERVAL_MS = 3000;

    public static void startMe(Activity activity, String baseEntityID, Boolean isEditMode) {
        Intent intent = new Intent(activity, AncFirstFacilityVisitActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.EDIT_MODE, isEditMode);
        activity.startActivityForResult(intent, org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT);
    }

    @Override
    protected void registerPresenter() {
        presenter = new BaseAncHomeVisitPresenter(memberObject, this, new AncFirstFacilityVisitInteractor());
    }

    @Override
    public void submittedAndClose() {
        Runnable runnable = () -> HfScheduleTaskExecutor.getInstance().execute(memberObject.getBaseEntityId(), Constants.Events.ANC_FIRST_FACILITY_VISIT, new Date());
        Utils.startAsyncTask(new RunnableTask(runnable), null);
        super.submittedAndClose();
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {

        Form form = new Form();
        form.setActionBarBackground(R.color.family_actionbar);
        form.setWizard(false);

        Intent intent;

        try {
            if (jsonForm.getString("encounter_type").equals("Emergency Plan")) {
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
        tvTitle.setText(MessageFormat.format("{0}, {1} \u00B7 {2}", memberObject.getFullName(), memberObject.getAge(), getString(org.smartregister.chw.hf.R.string.anc_first_visit)));
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
