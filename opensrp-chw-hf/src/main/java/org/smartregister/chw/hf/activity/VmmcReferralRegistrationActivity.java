package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.common.collect.Sets;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.hf.presenter.IssueReferralActivityPresenter;
import org.smartregister.chw.referral.activity.BaseIssueReferralActivity;
import org.smartregister.chw.referral.contract.BaseIssueReferralContract;
import org.smartregister.chw.referral.interactor.BaseIssueReferralInteractor;
import org.smartregister.chw.referral.model.BaseIssueReferralModel;
import org.smartregister.chw.referral.presenter.BaseIssueReferralPresenter;
import org.smartregister.chw.referral.util.Constants;
import org.smartregister.dao.LocationsDao;
import org.smartregister.domain.Location;
import org.smartregister.family.util.JsonFormUtils;

import java.util.List;

import timber.log.Timber;

public class VmmcReferralRegistrationActivity extends BaseIssueReferralActivity {
    private static String BASE_ENTITY_ID;

    public static void startGeneralReferralFormActivityForResults(Activity activity, String baseEntityID, JSONObject formJsonObject, boolean useCustomLayout) {
        BASE_ENTITY_ID = baseEntityID;
        Intent intent = new Intent(activity, VmmcReferralRegistrationActivity.class);
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

    //    @Override
    public void initializeHealthFacilitiesList(JSONObject form) {
        JSONArray steps;
        List<Location> locationList = LocationsDao.getLocationsByTags(Sets.newHashSet("health_facility", "Facility"));
        try {
            JSONArray options = new JSONArray();
            for (Location location : locationList) {
                JSONObject option = new JSONObject();
                option.put("name", StringUtils.capitalize(location.getProperties().getName()));
                option.put("text", StringUtils.capitalize(location.getProperties().getName()));
                JSONObject metaData = new JSONObject();
                metaData.put("openmrs_entity", "location_uuid");
                metaData.put("openmrs_entity_id", location.getProperties().getUid());
                option.put("meta_data", metaData);

                options.put(option);
            }

            steps = form.getJSONArray("steps");
            JSONObject step = steps.getJSONObject(0);
            JSONArray fields = step.getJSONArray("fields");
            int i = 0;
            int j = 0;
            int fieldCount = fields.length();
            int optionCount = options.length();
            while (i < fieldCount) {
                JSONObject field = fields.getJSONObject(i);
                if (field.getString("name").equals("chw_referral_hf")) {
                    JSONArray optionsArr = field.getJSONArray("options");
                    while (j < optionCount) {
                        optionsArr.put(options.get(j));
                        j++;
                    }
                    break;
                }
                i++;
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
