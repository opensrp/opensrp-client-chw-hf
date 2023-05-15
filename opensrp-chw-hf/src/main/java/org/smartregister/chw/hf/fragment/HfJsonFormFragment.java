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

public class HfJsonFormFragment extends JsonWizardFormFragment {
    private static final String RECEIVING_ORDER_FACILITY = "receiving_order_facility";

    public static HfJsonFormFragment getFormFragment(String stepName) {
        HfJsonFormFragment jsonFormFragment = new HfJsonFormFragment();
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

            //Removing not required options that might lead to unnecessarily large response json object
            JSONObject receivingOrderFacility = org.smartregister.family.util.JsonFormUtils.getFieldJSONObject(fields, RECEIVING_ORDER_FACILITY);
            if (receivingOrderFacility != null) {
                receivingOrderFacility.put("options", new JSONArray());
            }

            JSONObject referralHealthFacilities = org.smartregister.family.util.JsonFormUtils.getFieldJSONObject(fields, Constants.JsonFormConstants.NAME_OF_HF);
            if (referralHealthFacilities != null) {
                referralHealthFacilities.put("options", new JSONArray());
            }

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
