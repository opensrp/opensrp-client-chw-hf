package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.vmmc.activity.BaseVmmcVisitActivity;
import org.smartregister.chw.vmmc.interactor.BaseVmmcServiceVisitInteractor;
import org.smartregister.chw.vmmc.model.BaseVmmcVisitAction;
import org.smartregister.chw.vmmc.presenter.BaseVmmcVisitPresenter;
import org.smartregister.chw.vmmc.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.util.LangUtils;

import java.util.LinkedHashMap;
import java.util.Map;


public class VmmcServiceActivity extends BaseVmmcVisitActivity {
    public static void startVmmcVisitActivity(Activity activity, String baseEntityId, Boolean editMode) {
        Intent intent = new Intent(activity, VmmcServiceActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, editMode);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.PROFILE_TYPE, Constants.PROFILE_TYPES.VMMC_PROFILE);
        activity.startActivity(intent);
    }

    @Override
    protected void registerPresenter() {
        presenter = new BaseVmmcVisitPresenter(memberObject, this, new BaseVmmcServiceVisitInteractor(Constants.EVENT_TYPE.VMMC_SERVICES));
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
    public void initializeActions(LinkedHashMap<String, BaseVmmcVisitAction> map) {
        actionList.clear();

        //Necessary evil to rearrange the actions according to a specific arrangement

        if (map.containsKey(getString(R.string.vmmc_medical_history))) {
            BaseVmmcVisitAction visitTypeAction = map.get(getString(R.string.vmmc_medical_history));
            actionList.put(getString(R.string.vmmc_medical_history), visitTypeAction);
        }
        if (map.containsKey(getString(R.string.vmmc_physical_examination))) {
            BaseVmmcVisitAction visitTypeAction = map.get(getString(R.string.vmmc_physical_examination));
            actionList.put(getString(R.string.vmmc_physical_examination), visitTypeAction);
        }
        if (map.containsKey(getString(R.string.vmmc_hts))) {
            BaseVmmcVisitAction visitTypeAction = map.get(getString(R.string.vmmc_hts));
            actionList.put(getString(R.string.vmmc_hts), visitTypeAction);
        }

        for (Map.Entry<String, BaseVmmcVisitAction> entry : map.entrySet()) {
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

    @Override
    protected void attachBaseContext(Context base) {
        // get language from prefs
        String lang = LangUtils.getLanguage(base.getApplicationContext());
        super.attachBaseContext(LangUtils.setAppLocale(base, lang));
    }
}
