package org.smartregister.chw.hf.activity;

import android.content.Intent;

import com.nerdstone.neatformcore.domain.model.NFormViewData;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.presenter.HivNeatFormActivityActivityPresenter;
import org.smartregister.chw.hiv.activity.BaseHivNeatFormsActivity;
import org.smartregister.chw.hiv.interactor.BaseNeatFormsInteractor;
import org.smartregister.chw.hiv.presenter.BaseNeatFormActivityPresenter;
import org.smartregister.chw.hiv.util.Constants;
import org.smartregister.chw.hiv.util.DBConstants;
import org.smartregister.chw.hiv.util.JsonFormConstants;

import java.util.HashMap;

public class HivNeatFormsActivity extends BaseHivNeatFormsActivity {
    @NotNull
    @Override
    public BaseNeatFormActivityPresenter presenter() {
        return new HivNeatFormActivityActivityPresenter(getBaseEntityId(), this, new BaseNeatFormsInteractor());
    }

    @Override
    public void setDataToBePassedBackToCallingActivityAsResults(@NotNull Intent intent, @NotNull JSONObject jsonForm, @NotNull HashMap<String, NFormViewData> formData) {
        try {
            if (jsonForm.getString(JsonFormConstants.ENCOUNTER_TYPE).equals(Constants.EventType.HIV_INDEX_CONTACT_TESTING_FOLLOWUP)) {
                intent.putExtra(HivIndexContactProfileActivity.REGISTERED_TO_HIV_REGISTRY, formData.containsKey(DBConstants.Key.TEST_RESULTS) && String.valueOf(formData.get(DBConstants.Key.TEST_RESULTS).getValue()).contains("Positive"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

