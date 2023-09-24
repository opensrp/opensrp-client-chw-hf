package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.LDPostDeliveryManagementMotherActivityInteractor;
import org.smartregister.chw.ld.activity.BaseLDVisitActivity;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.chw.ld.presenter.BaseLDVisitPresenter;
import org.smartregister.chw.ld.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Kassim Sheghembe on 2022-05-16
 */
public class LDPostDeliveryManagementMotherActivity extends BaseLDVisitActivity {

    public static void startPostDeliveryMotherManagementActivity(Activity activity, String baseEntityId, Boolean editMode) {
        Intent intent = new Intent(activity, LDPostDeliveryManagementMotherActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, editMode);

        activity.startActivityForResult(intent, org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT);
    }

    @Override
    protected void registerPresenter() {
        presenter = new BaseLDVisitPresenter(memberObject, this, new LDPostDeliveryManagementMotherActivityInteractor());
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
    public void initializeActions(LinkedHashMap<String, BaseLDVisitAction> map) {
        //Clearing the action List before recreation
        actionList.clear();

        //Rearranging the actions according to a specific arrangement
        if (map.containsKey(getString(R.string.ld_registration_admission_information_title))) {
            BaseLDVisitAction ldAdmissionAction = map.get(getString(R.string.ld_registration_admission_information_title));
            actionList.put(getString(R.string.ld_registration_admission_information_title), ldAdmissionAction);
        }
        if (map.containsKey(getString(R.string.ld_registration_obstetric_history_title))) {
            BaseLDVisitAction obstetricHistoryAction = map.get(getString(R.string.ld_registration_obstetric_history_title));
            actionList.put(getString(R.string.ld_registration_obstetric_history_title), obstetricHistoryAction);
        }
        if (map.containsKey(getString(org.smartregister.chw.hf.R.string.ld_registration_past_obstetric_history_title))) {
            BaseLDVisitAction labourAndDeliveryPastObstetricHistory = map.get(getString(org.smartregister.chw.hf.R.string.ld_registration_past_obstetric_history_title));
            actionList.put(getString(org.smartregister.chw.hf.R.string.ld_registration_past_obstetric_history_title), labourAndDeliveryPastObstetricHistory);
        }
        if (map.containsKey(getString(R.string.ld_registration_anc_clinic_findings_title))) {
            BaseLDVisitAction ancClinicFindingsAction = map.get(getString(R.string.ld_registration_anc_clinic_findings_title));
            actionList.put(getString(R.string.ld_registration_anc_clinic_findings_title), ancClinicFindingsAction);
        }
        if (map.containsKey(getString(R.string.ld_mother_status_action_title))) {
            BaseLDVisitAction mothersStatusAction = map.get(getString(R.string.ld_mother_status_action_title));
            actionList.put(getString(R.string.ld_mother_status_action_title), mothersStatusAction);
        }
        if (map.containsKey(getString(R.string.ld_post_delivery_observation_action_title))) {
            BaseLDVisitAction postDeliveryObservationsAction = map.get(getString(R.string.ld_post_delivery_observation_action_title));
            actionList.put(getString(R.string.ld_post_delivery_observation_action_title), postDeliveryObservationsAction);
        }

        if (map.containsKey(getString(R.string.ld_maternal_complication_action_title))) {
            BaseLDVisitAction martenalComplicationsAction = map.get(getString(R.string.ld_maternal_complication_action_title));
            actionList.put(getString(R.string.ld_maternal_complication_action_title), martenalComplicationsAction);
        }
        if (map.containsKey(getString(R.string.ld_post_delivery_family_planning))) {
            BaseLDVisitAction familyPlanningAction = map.get(getString(R.string.ld_post_delivery_family_planning));
            actionList.put(getString(R.string.ld_post_delivery_family_planning), familyPlanningAction);
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
        displayProgressBar(false);


        super.initializeActions(map);
    }
}
