package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.dao.ChwNotificationDao;
import org.smartregister.chw.hf.presenter.IssueReferralActivityPresenter;
import org.smartregister.chw.referral.activity.BaseIssueReferralActivity;
import org.smartregister.chw.referral.contract.BaseIssueReferralContract;
import org.smartregister.chw.referral.interactor.BaseIssueReferralInteractor;
import org.smartregister.chw.referral.model.BaseIssueReferralModel;
import org.smartregister.chw.referral.presenter.BaseIssueReferralPresenter;
import org.smartregister.chw.referral.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.location.helper.LocationHelper;

import timber.log.Timber;

public class ReferralRegistrationActivity extends BaseIssueReferralActivity {
    private static String BASE_ENTITY_ID;

    public static void startGeneralReferralFormActivityForResults(Activity activity, String baseEntityID, JSONObject formJsonObject, boolean useCustomLayout) {
        BASE_ENTITY_ID = baseEntityID;
        Intent intent = new Intent(activity, ReferralRegistrationActivity.class);
        intent.putExtra(Constants.ActivityPayload.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ActivityPayload.JSON_FORM, formJsonObject.toString());
        intent.putExtra(Constants.ActivityPayload.ACTION, Constants.ActivityPayloadType.REGISTRATION);
        intent.putExtra(Constants.ActivityPayload.USE_CUSTOM_LAYOUT, useCustomLayout);
        activity.startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationMenu.getInstance(this, null, null);
    }

    @Override
    public void initializeHealthFacilitiesList(JSONObject form) {
        //overrides and sets the chw location as the selected location
        JSONArray steps = null;
        LocationHelper locationHelper = LocationHelper.getInstance();
        String locationId = ChwNotificationDao.getSyncLocationId(BASE_ENTITY_ID);
        String locationName = locationHelper.getOpenMrsLocationName(locationId);
        //TODO: need a fix for the locations for clients out of allowed level brought by global search
        try {
            JSONObject option = new JSONObject();
            option.put("name", StringUtils.capitalize(locationName));
            option.put("text", StringUtils.capitalize(locationName));
            JSONObject metaData = new JSONObject();
            metaData.put("openmrs_entity", "location_uuid");
            metaData.put("openmrs_entity_id", locationId);
            option.put("meta_data", metaData);

            steps = form.getJSONArray("steps");
            JSONObject step = steps.getJSONObject(0);
            JSONArray fields = step.getJSONArray("fields");
            for (int i = 0; i < fields.length(); i++) {
                JSONObject field = fields.getJSONObject(i);
                if (field.getString("name").equals("chw_referral_hf")) {
                    field.getJSONArray("options").put(option);
                    field.getJSONObject("properties").put("selection", "0");
                }
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @NotNull
    @Override
    public BaseIssueReferralPresenter presenter() {
        return new IssueReferralActivityPresenter(BASE_ENTITY_ID, (BaseIssueReferralContract.View) this,
                BaseIssueReferralModel.class, (BaseIssueReferralContract.Interactor) new BaseIssueReferralInteractor());
    }
}
