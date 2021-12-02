package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.model.BaseAncRegisterModel;
import org.smartregister.chw.anc.presenter.BaseAncRegisterPresenter;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.activity.CoreAncRegisterActivity;
import org.smartregister.chw.core.activity.CorePncRegisterActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.fragment.AncRegisterFragment;
import org.smartregister.chw.hf.interactor.AncRegisterInteractor;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.chw.core.utils.CoreConstants.JSON_FORM.isMultiPartForm;

public class AncRegisterActivity extends CoreAncRegisterActivity {

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
            try {
                JSONObject min_date = CoreJsonFormUtils.getFieldJSONObject(jsonArray, "delivery_date");
                min_date.put("min_date", lastMenstrualPeriod);
            }catch (Exception e){
                Timber.e(e);
            }

            FormUtils.updateFormField(jsonArray, values);

            Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

            Form form = new Form();
            form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
            form.setWizard(false);
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

            if (isMultiPartForm(jsonForm)) {
                form.setWizard(true);
                form.setNavigationBackground(org.smartregister.chw.core.R.color.family_navigation);
                form.setName(this.getString(R.string.pregnancy_confirmation));
                form.setNextLabel(this.getResources().getString(org.smartregister.chw.core.R.string.next));
                form.setPreviousLabel(this.getResources().getString(org.smartregister.chw.core.R.string.back));
            }

            startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    public static String getFormTable() {
        if (form_name != null && (form_name.equals(CoreConstants.JSON_FORM.getAncRegistration()) || form_name.equals(org.smartregister.chw.hf.utils.Constants.JsonForm.getAncPregnancyConfirmationForm()))) {
            return CoreConstants.TABLE_NAME.ANC_MEMBER;
        }
        return CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME;
    }

    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        FamilyRegisterActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
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
        return org.smartregister.chw.hf.utils.Constants.Events.ANC_PREGNANCY_CONFIRMATION;
    }

    @Override
    protected void initializePresenter() {
        this.presenter = new BaseAncRegisterPresenter(this, new BaseAncRegisterModel(), new AncRegisterInteractor());
    }


    @Override
    public Class getRegisterActivity(String register) {
        if (register.equals(org.smartregister.chw.hf.utils.Constants.PregnancyConfirmationGroups.PREGNANCY_CONFIRMATION) || register.equals(CoreConstants.SERVICE_GROUPS.ANC))
            return AncRegisterActivity.class;
        else
            return CorePncRegisterActivity.class;
    }
}
