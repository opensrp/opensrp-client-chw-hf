package org.smartregister.chw.hf.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.hf.utils.Constants;

import timber.log.Timber;

public class AncJsonFormFragment extends JsonWizardFormFragment {

    public static AncJsonFormFragment getFormFragment(String stepName) {
        AncJsonFormFragment jsonFormFragment = new AncJsonFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString("stepName", stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }

    @Override
    public void finishWithResult(Intent returnIntent) {
        try {
            JSONObject jsonObject = new JSONObject(returnIntent.getStringExtra(JsonFormConstants.JSON_FORM_KEY.JSON));
            JSONArray fields = jsonObject.getJSONObject(Constants.JsonFormConstants.STEP1)
                    .getJSONArray(org.smartregister.chw.referral.util.JsonFormConstants.FIELDS);

            JSONObject referralHealthFacilities = org.smartregister.family.util.JsonFormUtils.getFieldJSONObject(fields, Constants.JsonFormConstants.NAME_OF_HF);

            referralHealthFacilities.put("options", new JSONArray());

            Intent newReturnIntent = new Intent();
            newReturnIntent.putExtra("json", jsonObject.toString());

            newReturnIntent.putExtra(JsonFormConstants.SKIP_VALIDATION,
                    "true");


            getActivity().setResult(Activity.RESULT_OK, newReturnIntent);
            getActivity().finish();
        } catch (Exception e) {
            Timber.e(e);
            getActivity().setResult(Activity.RESULT_OK, returnIntent);
            getActivity().finish();
        }
    }
}
