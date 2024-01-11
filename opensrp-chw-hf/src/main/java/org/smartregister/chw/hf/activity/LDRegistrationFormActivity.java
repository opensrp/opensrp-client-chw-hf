package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.task.RunnableTask;
import org.smartregister.chw.hf.interactor.LDRegistrationInteractor;
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
 * @author ilakozejumanne@gmail.com
 * 06/05/2022
 */
public class LDRegistrationFormActivity extends BaseLDVisitActivity {
    public static String LABOUR_AND_DELIVERY_REGISTRATION_ADMISSION_INFORMATION;

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

    @Override
    public void submittedAndClose() {
        Runnable runnable = () -> HfScheduleTaskExecutor.getInstance().execute(memberObject.getBaseEntityId(), Constants.Events.LD_REGISTRATION, new Date());
        Utils.startAsyncTask(new RunnableTask(runnable), null);

        Intent intent = new Intent(this, LDRegisterActivity.class);
        this.startActivity(intent);
        finish();
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {

        Form form = new Form();
        form.setActionBarBackground(R.color.family_actionbar);
        form.setWizard(false);

        Intent intent;

        try {
            if (jsonForm.getString("encounter_type").equals("Labour and Delivery Registration Admission Information")) {
                LABOUR_AND_DELIVERY_REGISTRATION_ADMISSION_INFORMATION = jsonForm.toString();
                intent = new Intent(this, LDRegistrationAdmissionInformationJsonWizardFormActivity.class);
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
        tvTitle.setText(MessageFormat.format("{0}, {1} \u00B7 {2}", getIntent().getStringExtra("FULL_NAME"), getIntent().getStringExtra("AGE"), getString(org.smartregister.chw.hf.R.string.ld_registration)));
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
        //Clearing the action List before recreation
        actionList.clear();

        //Necessary evil to rearrange the actions according to a specific arrangement
        if (map.containsKey(getString(org.smartregister.chw.hf.R.string.ld_registration_triage_title))) {
            BaseLDVisitAction triageAction = map.get(getString(org.smartregister.chw.hf.R.string.ld_registration_triage_title));
            actionList.put(getString(org.smartregister.chw.hf.R.string.ld_registration_triage_title), triageAction);
        }
        if (map.containsKey(getString(org.smartregister.chw.hf.R.string.ld_registration_true_labour_title))) {
            BaseLDVisitAction ldRegistrationTrueLabourConfirmation = map.get(getString(org.smartregister.chw.hf.R.string.ld_registration_true_labour_title));
            actionList.put(getString(org.smartregister.chw.hf.R.string.ld_registration_true_labour_title), ldRegistrationTrueLabourConfirmation);
        }
        if (map.containsKey(getString(org.smartregister.chw.hf.R.string.ld_registration_true_labour_title))) {
            BaseLDVisitAction ldRegistrationTrueLabourConfirmation = map.get(getString(org.smartregister.chw.hf.R.string.ld_registration_true_labour_title));
            actionList.put(getString(org.smartregister.chw.hf.R.string.ld_registration_true_labour_title), ldRegistrationTrueLabourConfirmation);
        }
        if (map.containsKey(getString(org.smartregister.chw.hf.R.string.labour_and_delivery_labour_stage_title))) {
            BaseLDVisitAction ldRegistrationTrueLabourConfirmation = map.get(getString(org.smartregister.chw.hf.R.string.labour_and_delivery_labour_stage_title));
            actionList.put(getString(org.smartregister.chw.hf.R.string.labour_and_delivery_labour_stage_title), ldRegistrationTrueLabourConfirmation);
        }
        if (map.containsKey(getString(org.smartregister.chw.hf.R.string.ld_registration_admission_information_title))) {
            BaseLDVisitAction ldRegistrationAdmissionInformation = map.get(getString(org.smartregister.chw.hf.R.string.ld_registration_admission_information_title));
            actionList.put(getString(org.smartregister.chw.hf.R.string.ld_registration_admission_information_title), ldRegistrationAdmissionInformation);
        }
        if (map.containsKey(getString(org.smartregister.chw.hf.R.string.ld_registration_obstetric_history_title))) {
            BaseLDVisitAction ldRegistrationObstetricHistory = map.get(getString(org.smartregister.chw.hf.R.string.ld_registration_obstetric_history_title));
            actionList.put(getString(org.smartregister.chw.hf.R.string.ld_registration_obstetric_history_title), ldRegistrationObstetricHistory);
        }
        if (map.containsKey(getString(org.smartregister.chw.hf.R.string.ld_registration_past_obstetric_history_title))) {
            BaseLDVisitAction labourAndDeliveryPastObstetricHistory = map.get(getString(org.smartregister.chw.hf.R.string.ld_registration_past_obstetric_history_title));
            actionList.put(getString(org.smartregister.chw.hf.R.string.ld_registration_past_obstetric_history_title), labourAndDeliveryPastObstetricHistory);
        }
        //====================End of Necessary evil ====================================


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
        redrawVisitUI();
        displayProgressBar(false);
    }

    @Override
    public void redrawVisitUI() {
        boolean valid = actionList.size() > 0;
        for (Map.Entry<String, BaseLDVisitAction> entry : actionList.entrySet()) {
            BaseLDVisitAction action = entry.getValue();
            if (
                //Updated the condition to only allow submission if the action is not completed in the L&D Registration
                    (!action.isOptional() && (action.getActionStatus() != BaseLDVisitAction.Status.COMPLETED && action.isValid()))
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
