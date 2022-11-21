package org.smartregister.chw.hf.activity;

import android.content.Intent;

import com.nerdstone.neatformcore.domain.model.NFormViewData;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.interactor.HivFormsInteractor;
import org.smartregister.chw.hf.presenter.HivFormActivityActivityPresenter;
import org.smartregister.chw.hiv.activity.BaseHivFormsActivity;
import org.smartregister.chw.hiv.presenter.BaseHivFormsActivityPresenter;
import org.smartregister.chw.hiv.util.Constants;
import org.smartregister.chw.hiv.util.DBConstants;
import org.smartregister.chw.hiv.util.JsonFormConstants;

import java.util.HashMap;

public class HivFormsActivity extends BaseHivFormsActivity {
    @NotNull
    @Override
    public BaseHivFormsActivityPresenter presenter() {
        return new HivFormActivityActivityPresenter(getBaseEntityId(), this, new HivFormsInteractor());
    }

    @Override
    public void setDataToBePassedBackToCallingActivityAsResults(@NotNull Intent intent, @NotNull JSONObject jsonForm, @NotNull HashMap<String, NFormViewData> formData) {
        try {
            if (jsonForm.getString(JsonFormConstants.ENCOUNTER_TYPE).equals(Constants.EventType.HIV_INDEX_CONTACT_TESTING_FOLLOWUP)) {
                boolean registered_to_hiv_registry = formData.containsKey(DBConstants.Key.TEST_RESULTS)
                        && StringUtils.containsIgnoreCase(String.valueOf(formData.get(DBConstants.Key.TEST_RESULTS).getValue()), "positive")
                        && StringUtils.containsIgnoreCase(String.valueOf(formData.get(DBConstants.Key.ENROLLED_TO_CLINIC).getValue()), "yes");
                intent.putExtra(HivIndexContactProfileActivity.REGISTERED_TO_HIV_REGISTRY, registered_to_hiv_registry);
            }
            if (jsonForm.getString(JsonFormConstants.ENCOUNTER_TYPE).equals(org.smartregister.chw.hf.utils.Constants.Events.UPDATE_HIV_INDEX_TESTING_FOLLOWUP)) {
                intent.putExtra(HivIndexContactProfileActivity.REGISTERED_TO_HIV_REGISTRY, true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

