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
import org.smartregister.chw.hf.interactor.KvpBioMedicalServiceInteractor;
import org.smartregister.chw.hf.schedulers.HfScheduleTaskExecutor;
import org.smartregister.chw.kvp.activity.BaseKvpVisitActivity;
import org.smartregister.chw.kvp.domain.MemberObject;
import org.smartregister.chw.kvp.model.BaseKvpVisitAction;
import org.smartregister.chw.kvp.presenter.BaseKvpVisitPresenter;
import org.smartregister.chw.kvp.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.util.LangUtils;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class KvpBioMedicalServiceActivity extends BaseKvpVisitActivity {

    public static void startKvpBioMedicalServiceActivity(Activity activity, String baseEntityId, Boolean editMode) {
        Intent intent = new Intent(activity, KvpBioMedicalServiceActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, editMode);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.PROFILE_TYPE, Constants.PROFILE_TYPES.KVP_PROFILE);
        activity.startActivity(intent);
    }

    @Override
    protected void registerPresenter() {
        presenter = new BaseKvpVisitPresenter(memberObject, this, new KvpBioMedicalServiceInteractor(Constants.EVENT_TYPE.KVP_BIO_MEDICAL_SERVICE_VISIT));
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
    public void submittedAndClose() {
        Runnable runnable = () -> HfScheduleTaskExecutor.getInstance().execute(memberObject.getBaseEntityId(), Constants.EVENT_TYPE.KVP_BIO_MEDICAL_SERVICE_VISIT, new Date());
        Utils.startAsyncTask(new RunnableTask(runnable), null);
        super.submittedAndClose();
    }

    @Override
    protected void attachBaseContext(Context base) {
        // get language from prefs
        String lang = LangUtils.getLanguage(base.getApplicationContext());
        super.attachBaseContext(LangUtils.setAppLocale(base, lang));
    }

    @Override
    public void redrawHeader(MemberObject memberObject) {
        tvTitle.setText(R.string.bio_medical_services);
    }

    @Override
    public void initializeActions(LinkedHashMap<String, BaseKvpVisitAction> map) {
        actionList.clear();

        //Necessary evil to rearrange the actions according to a specific arrangement
        //assuming the list will have client_status, then hts, then the third action is prep_pep

        //TODO: fix this if the case of client_status action would be moved from biomedical
        if (map.containsKey(getString(R.string.kvp_client_status))) {
            BaseKvpVisitAction clientStatusAction = map.get(getString(R.string.kvp_client_status));
            actionList.put(getString(R.string.kvp_client_status), clientStatusAction);
        }

        if (map.containsKey(getString(R.string.kvp_hts))) {
            BaseKvpVisitAction htsAction = map.get(getString(R.string.kvp_hts));
            actionList.put(getString(R.string.kvp_hts), htsAction);
        }

        if (map.containsKey(getString(R.string.kvp_pep_assesment))) {
            BaseKvpVisitAction PrEPPepAction = map.get(getString(R.string.kvp_pep_assesment));
            actionList.put(getString(R.string.kvp_pep_assesment), PrEPPepAction);
        }
        for (Map.Entry<String, BaseKvpVisitAction> entry : map.entrySet()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                actionList.putIfAbsent(entry.getKey(), entry.getValue());
            } else {
                actionList.put(entry.getKey(), entry.getValue());
            }
        }
        //====================End of Necessary evil ====================================

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        displayProgressBar(false);
    }
}
