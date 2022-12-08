package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.hf.utils.Constants.REQUEST_FILTERS;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.MenuRes;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.model.BaseAncRegisterModel;
import org.smartregister.chw.anc.presenter.BaseAncRegisterPresenter;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.activity.CoreAncRegisterActivity;
import org.smartregister.chw.core.activity.CorePncRegisterActivity;
import org.smartregister.chw.core.dao.ChwNotificationDao;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.hf.fragment.AncReferralListRegisterFragment;
import org.smartregister.chw.hf.fragment.AncRegisterFragment;
import org.smartregister.chw.hf.interactor.AncRegisterInteractor;
import org.smartregister.chw.hf.listener.AncBottomNavigationListener;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.listener.BottomNavigationListener;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import timber.log.Timber;

public class AncRegisterActivity extends CoreAncRegisterActivity {

    private static String taskId = null;
    private static String baseEntityId;

    public static void startAncRegistrationActivity(Activity activity, String memberBaseEntityID, String phoneNumber, String formName,
                                                    String uniqueId, String familyBaseID, String family_name) {
        Intent intent = new Intent(activity, AncRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, memberBaseEntityID);
        phone_number = phoneNumber;
        familyBaseEntityId = familyBaseID;
        form_name = formName;
        familyName = family_name;
        unique_id = uniqueId;
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.TABLE_NAME, getFormTable());
        activity.startActivity(intent);
    }

    public static void startAncRegistrationActivity(Activity activity, String memberBaseEntityID, String phoneNumber, String formName,
                                                    String uniqueId, String familyBaseID, String family_name, String taskID) {
        Intent intent = new Intent(activity, AncRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, memberBaseEntityID);
        phone_number = phoneNumber;
        familyBaseEntityId = familyBaseID;
        form_name = formName;
        familyName = family_name;
        unique_id = uniqueId;
        baseEntityId = memberBaseEntityID;
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.TABLE_NAME, getFormTable());
        taskId = taskID;
        activity.startActivity(intent);
    }


    @Override
    public void startFormActivity(JSONObject jsonForm) {
        try {
            JSONObject stepOne = jsonForm.getJSONObject(JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);

            Map<String, String> values = new HashMap<>();

            values.put(DBConstants.KEY.TEMP_UNIQUE_ID, unique_id);
            values.put(CoreConstants.JsonAssets.FAM_NAME, familyName);
            values.put(CoreConstants.JsonAssets.FAMILY_MEMBER.PHONE_NUMBER, phone_number);
            values.put(org.smartregister.family.util.DBConstants.KEY.RELATIONAL_ID, familyBaseEntityId);
            values.put(DBConstants.KEY.LAST_MENSTRUAL_PERIOD, lastMenstrualPeriod);
            if (taskId != null) {
                List<String> existingTaskIds = HfAncDao.getPresentTaskIds(baseEntityId);
                if (!existingTaskIds.isEmpty()) {
                    ArrayList<String> list = new ArrayList<>(Collections.singleton(existingTaskIds.toString()));
                    list.add(taskId);
                    String taskIdsString = list.toString();
                    values.put(org.smartregister.chw.hf.utils.Constants.DBConstants.TASK_ID, taskIdsString);
                } else {
                    values.put(org.smartregister.chw.hf.utils.Constants.DBConstants.TASK_ID, taskId);
                }
            }
            try {
                JSONObject min_date = CoreJsonFormUtils.getFieldJSONObject(jsonArray, "delivery_date");
                min_date.put("min_date", lastMenstrualPeriod);
            } catch (Exception e) {
                Timber.e(e);
            }

            FormUtils.updateFormField(jsonArray, values);

            Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

            Form form = new Form();
            form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
            form.setWizard(false);
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

            if (jsonForm.getString("encounter_type").equals("ANC Partner Community Followup Referral")) {
                form.setWizard(true);
                form.setNavigationBackground(org.smartregister.chw.core.R.color.family_navigation);
                form.setName(this.getString(R.string.anc_partner_followup_referral));
                form.setNextLabel(this.getResources().getString(org.smartregister.chw.core.R.string.next));
                form.setPreviousLabel(this.getResources().getString(org.smartregister.chw.core.R.string.back));
                form.setSaveLabel(this.getResources().getString(org.smartregister.chw.core.R.string.save));
            } else {
                form.setWizard(true);
                form.setNavigationBackground(org.smartregister.chw.core.R.color.family_navigation);
                if (jsonForm.getString("encounter_type").equals("Pregnancy Confirmation")) {
                    form.setName(this.getString(R.string.pregnancy_confirmation));
                } else if (jsonForm.getString("encounter_type").equals("ANC Followup Client Registration")) {
                    form.setName(this.getString(R.string.anc_followup_client_registration));
                }
                form.setNextLabel(this.getResources().getString(org.smartregister.chw.core.R.string.next));
                form.setPreviousLabel(this.getResources().getString(org.smartregister.chw.core.R.string.back));
                form.setSaveLabel(this.getResources().getString(org.smartregister.chw.core.R.string.save));
            }

            startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    protected Fragment[] getOtherFragments() {
        return new AncReferralListRegisterFragment[]{
                new AncReferralListRegisterFragment()};
    }

    public static String getFormTable() {
        if (form_name != null && (form_name.equals(CoreConstants.JSON_FORM.getAncRegistration()) || form_name.equals(CoreConstants.JSON_FORM.ANC_PREGNANCY_CONFIRMATION))) {
            return CoreConstants.TABLE_NAME.ANC_MEMBER;
        }
        return CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME;
    }

    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_clients);
            bottomNavigationView.getMenu().removeItem(org.smartregister.chw.tb.R.id.action_register);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_search);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_library);
            bottomNavigationView.getMenu().removeItem(R.id.action_scan_qr);
//
//            bottomNavigationView.inflateMenu(getMenuResource());
//            bottomNavigationHelper.disableShiftMode(bottomNavigationView);

            BottomNavigationListener ancBottomNavigationListener = getBottomNavigation(this);
            bottomNavigationView.setOnNavigationItemSelectedListener(ancBottomNavigationListener);

        }
    }


    @Override
    public BottomNavigationListener getBottomNavigation(Activity activity) {
        return new AncBottomNavigationListener(activity);
    }

    @MenuRes
    public int getMenuResource() {
        return R.menu.bottom_nav_anc_menu;
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new AncRegisterFragment();
    }

    @Override
    public void switchToBaseFragment() {
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        startActivity(intent);
        finish();
    }

    protected void startRegisterActivity(Class registerClass) {
        Intent intent = new Intent(this, registerClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
        this.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        this.finish();
    }

    @Override
    public String getRegisterEventType() {
        return CoreConstants.EventType.ANC_PREGNANCY_CONFIRMATION;
    }

    @Override
    protected void initializePresenter() {
        this.presenter = new BaseAncRegisterPresenter(this, new BaseAncRegisterModel(), new AncRegisterInteractor());
    }

    @Override
    protected void onActivityResultExtended(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_CODE_GET_JSON) {
//            process the form
            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                Timber.d("JSONResult %s", jsonString);

                JSONObject form = new JSONObject(jsonString);
                String encounter_type = form.optString(Constants.JSON_FORM_EXTRA.ENCOUNTER_TYPE);
                String pregnancyConfirmationStatus = CoreJsonFormUtils.getValue(form, "pregnancy_confirmation_status");
                String table = data.getStringExtra(Constants.ACTIVITY_PAYLOAD.TABLE_NAME);
                boolean isPregnancyConfirmed;
                if (encounter_type.equals("ANC Followup Client Registration")) {
                    isPregnancyConfirmed = true;
                } else {
                    isPregnancyConfirmed = pregnancyConfirmationStatus.equalsIgnoreCase("confirmed");
                }

                if (encounter_type.equalsIgnoreCase(getRegisterEventType()) || encounter_type.equals("ANC Followup Client Registration")) {
                    saveFormForPregnancyConfirmation(jsonString, table);
                    if (!isPregnancyConfirmed) {
                        closeForUnconfirmed(jsonString, table);
                    }
                } else if (encounter_type.equalsIgnoreCase(Constants.EVENT_TYPE.PREGNANCY_OUTCOME)) {

                    presenter().saveForm(jsonString, false, table);

                } else if (encounter_type.equalsIgnoreCase(CoreConstants.EventType.ANC_FOLLOWUP_CLIENT_REGISTRATION)) {

                    presenter().saveForm(jsonString, false, table);

                } else if (encounter_type.startsWith(Constants.EVENT_TYPE.UPDATE_EVENT_CONDITION)) {

                    presenter().saveForm(form.toString(), true, TABLE);

                }
            } catch (Exception e) {
                Timber.e(e);
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_FILTERS) {
            ((AncRegisterFragment) mBaseFragment).onFiltersUpdated(requestCode, data);
        }
    }

    public static void saveFormForPregnancyConfirmation(String jsonString, String table) {
        try {
            JSONObject form = new JSONObject(jsonString);
            JSONArray fields = org.smartregister.util.JsonFormUtils.fields(form);
            JSONObject lmp = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, DBConstants.KEY.LAST_MENSTRUAL_PERIOD);
            boolean hasLmp = StringUtils.isNotBlank(lmp.optString(JsonFormUtils.VALUE));

            if (!hasLmp) {
                JSONObject eddJson = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, DBConstants.KEY.EDD);
                DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("dd-MM-yyyy");

                LocalDate lmpDate = dateTimeFormat.parseLocalDate(eddJson.optString(JsonFormUtils.VALUE)).plusDays(-280);
                lmp.put(JsonFormUtils.VALUE, dateTimeFormat.print(lmpDate));
            }
            processPregnancyConfirmation(form.toString(), table);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public static void processPregnancyConfirmation(String jsonString, String table) throws Exception {
        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
        Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, table);
        org.smartregister.chw.anc.util.JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
        String syncLocationId = ChwNotificationDao.getSyncLocationId(baseEvent.getBaseEntityId());
        if (syncLocationId != null) {
            // Allows setting the ID for sync purposes
            baseEvent.setLocationId(syncLocationId);
        }
        NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseEvent)));

    }

    private static void closeForUnconfirmed(String json, String table) throws Exception {
        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
        Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, json, table);
        org.smartregister.chw.anc.util.JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
        String syncLocationId = ChwNotificationDao.getSyncLocationId(baseEvent.getBaseEntityId());
        if (syncLocationId != null) {
            // Allows setting the ID for sync purposes
            baseEvent.setLocationId(syncLocationId);
        }
        baseEvent.setFormSubmissionId(UUID.randomUUID().toString());
        baseEvent.setEventType("Pregnancy Unconfirmed");
        NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseEvent)));
    }


    @Override
    public Class getRegisterActivity(String register) {
        if (register.equals(org.smartregister.chw.hf.utils.Constants.PregnancyConfirmationGroups.PREGNANCY_CONFIRMATION) || register.equals(CoreConstants.SERVICE_GROUPS.ANC))
            return AncRegisterActivity.class;
        else
            return CorePncRegisterActivity.class;
    }
}
