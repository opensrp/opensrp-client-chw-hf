package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hf.fragment.HeiHivResultsFragment;
import org.smartregister.chw.hiv.util.DBConstants;
import org.smartregister.chw.pmtct.activity.BaseHvlResultsViewActivity;
import org.smartregister.chw.pmtct.fragment.BaseHvlResultsFragment;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.chw.pmtct.util.JsonFormUtils;
import org.smartregister.chw.pmtct.util.NCUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;

import java.util.ArrayList;

import timber.log.Timber;

public class HeiHivResultsViewActivity extends BaseHvlResultsViewActivity implements View.OnClickListener {

    private String baseEntityId;
    private String parentFormSubmissionId;

    public static void startResultsForm(Context context, String jsonString, String baseEntityId, String parentFormSubmissionId) {
        Intent intent = new Intent(context, HeiHivResultsViewActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.PMTCT_FORM, jsonString);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.PARENT_FORM_ENTITY_ID, parentFormSubmissionId);
        context.startActivity(intent);
    }

    @Override
    public BaseHvlResultsFragment getBaseFragment() {
        return HeiHivResultsFragment.newInstance(baseEntityId);
    }

    @Override
    protected void onCreation() {
        String jsonString = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.PMTCT_FORM);
        String baseEntityId = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
        String parentFormSubmissionId = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.PARENT_FORM_ENTITY_ID);

        this.baseEntityId = baseEntityId;
        this.parentFormSubmissionId = parentFormSubmissionId;

        if (StringUtils.isBlank(jsonString)) {
            super.onCreation();
            ImageView backImageView = findViewById(R.id.back);
            backImageView.setOnClickListener(this);

            TextView titleView = findViewById(R.id.textview_title);
            titleView.setText(R.string.hiv_results_title);
        } else {
            try {
                JSONObject form = new JSONObject(jsonString);
                startFormActivity(form);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back) {
            finish();
        }
    }

    @Override
    public void startFormActivity(JSONObject jsonObject) {
        Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonObject.toString());

        Form form = new Form();
        form.setName(getString(R.string.hiv_test_results_title));
        form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
        form.setNavigationBackground(org.smartregister.chw.core.R.color.family_navigation);
        form.setHomeAsUpIndicator(org.smartregister.chw.core.R.mipmap.ic_cross_white);
        form.setPreviousLabel(getResources().getString(org.smartregister.chw.core.R.string.back));
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

        startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_GET_JSON && resultCode == Activity.RESULT_CANCELED) {
            //handle form close
            finish();
        }

        if (requestCode == Constants.REQUEST_CODE_GET_JSON && resultCode == Activity.RESULT_OK) {
            //handle form saving
            String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
            try {

                String testAtAge = HeiDao.getTestAtAgeForFollowupVisit(parentFormSubmissionId);

                AllSharedPreferences allSharedPreferences = org.smartregister.util.Utils.getAllSharedPreferences();
                Event baseEvent = JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, org.smartregister.chw.hf.utils.Constants.TableName.HEI_HIV_RESULTS);
                JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
                baseEvent.setBaseEntityId(baseEntityId);
                baseEvent.addObs(
                        (new Obs())
                                .withFormSubmissionField(org.smartregister.chw.hf.utils.Constants.DBConstants.HEI_FOLLOWUP_FORM_SUBMISSION_ID)
                                .withValue(parentFormSubmissionId)
                                .withFieldCode(org.smartregister.chw.hf.utils.Constants.DBConstants.HEI_FOLLOWUP_FORM_SUBMISSION_ID)
                                .withFieldType("formsubmissionField")
                                .withFieldDataType("text")
                                .withParentCode("")
                                .withHumanReadableValues(new ArrayList<>()));
                NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.pmtct.util.JsonFormUtils.gson.toJson(baseEvent)));

                JSONObject jsonForm = new JSONObject(jsonString);
                JSONArray fields = jsonForm.getJSONObject(org.smartregister.chw.hf.utils.Constants.JsonFormConstants.STEP1).getJSONArray(org.smartregister.chw.referral.util.JsonFormConstants.FIELDS);

                JSONObject hivTestResultObj = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "hiv_test_result");
                JSONObject confirmatoryTestResultObj = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "confirmation_hiv_test_result");
                String hivTestResult = hivTestResultObj.optString(JsonFormUtils.VALUE);
                String confirmatoryTestResult = confirmatoryTestResultObj.optString(JsonFormUtils.VALUE);
                Event closePmtctEvent = JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, Constants.TABLES.PMTCT_REGISTRATION);
                JsonFormUtils.tagEvent(allSharedPreferences, closePmtctEvent);
                closePmtctEvent.setEventType(org.smartregister.chw.hf.utils.Constants.Events.PMTCT_CLOSE_VISITS);
                closePmtctEvent.setBaseEntityId(HeiDao.getMotherBaseEntityId(baseEntityId));

                if (hivTestResult.equalsIgnoreCase("positive") && confirmatoryTestResult.equalsIgnoreCase("yes")) {
                    Event closeHeiEvent = getCloseEventForPositive(allSharedPreferences, jsonString);
                    //process the events
                    NCUtils.processEvent(closePmtctEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.pmtct.util.JsonFormUtils.gson.toJson(closePmtctEvent)));
                    NCUtils.processEvent(closeHeiEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.pmtct.util.JsonFormUtils.gson.toJson(closeHeiEvent)));
                }
                //processes closing negative client if test results are for month 18
                if (hivTestResult.equalsIgnoreCase("negative") && (testAtAge.equalsIgnoreCase(org.smartregister.chw.hf.utils.Constants.HeiHIVTestAtAge.AT_18_MONTHS))) {
                    Event closeHeiEvent = getCloseEventForNegative(allSharedPreferences, jsonString);
                    //process the events
                    NCUtils.processEvent(closePmtctEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.pmtct.util.JsonFormUtils.gson.toJson(closePmtctEvent)));
                    NCUtils.processEvent(closeHeiEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.pmtct.util.JsonFormUtils.gson.toJson(closeHeiEvent)));
                }
            } catch (JSONException e) {
                Timber.e(e);
            } catch (Exception e) {
                Timber.e(e);
            }
            //handles going back to activity after save
            finish();
        }

    }

    protected Event getCloseEventForPositive(AllSharedPreferences allSharedPreferences, String jsonString) {
        Event closeHeiEvent = JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, org.smartregister.chw.hf.utils.Constants.TableName.HEI_HIV_RESULTS);
        JsonFormUtils.tagEvent(allSharedPreferences, closeHeiEvent);
        closeHeiEvent.setEventType(org.smartregister.chw.hf.utils.Constants.Events.HEI_POSITIVE_INFANT);
        closeHeiEvent.setBaseEntityId(baseEntityId);
        closeHeiEvent.addObs(
                (new Obs())
                        .withFormSubmissionField(org.smartregister.chw.hf.utils.Constants.DBConstants.HIV_REGISTRATION_DATE)
                        .withValue(System.currentTimeMillis())
                        .withFieldCode(org.smartregister.chw.hf.utils.Constants.DBConstants.HIV_REGISTRATION_DATE)
                        .withFieldType("formsubmissionField")
                        .withFieldDataType("text")
                        .withParentCode("")
                        .withHumanReadableValues(new ArrayList<>()));
        closeHeiEvent.addObs(
                (new Obs())
                        .withFormSubmissionField(DBConstants.Key.CLIENT_HIV_STATUS_DURING_REGISTRATION)
                        .withValue("positive")
                        .withFieldCode(DBConstants.Key.CLIENT_HIV_STATUS_DURING_REGISTRATION)
                        .withFieldType("formsubmissionField")
                        .withFieldDataType("text")
                        .withParentCode("")
                        .withHumanReadableValues(new ArrayList<>()));
        closeHeiEvent.addObs(
                (new Obs())
                        .withFieldCode(DBConstants.Key.CLIENT_HIV_STATUS_AFTER_TESTING)
                        .withFormSubmissionField(DBConstants.Key.TEST_RESULTS)
                        .withValue("positive")
                        .withFieldType("formsubmissionField")
                        .withFieldDataType("text")
                        .withParentCode("")
                        .withHumanReadableValues(new ArrayList<>()));

        return closeHeiEvent;
    }

    protected Event getCloseEventForNegative(AllSharedPreferences allSharedPreferences, String jsonString) {
        Event closeHeiEvent = JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, org.smartregister.chw.hf.utils.Constants.TableName.HEI_HIV_RESULTS);
        JsonFormUtils.tagEvent(allSharedPreferences, closeHeiEvent);
        closeHeiEvent.setEventType(org.smartregister.chw.hf.utils.Constants.Events.HEI_NEGATIVE_INFANT);
        closeHeiEvent.setBaseEntityId(baseEntityId);

        return closeHeiEvent;
    }

}
