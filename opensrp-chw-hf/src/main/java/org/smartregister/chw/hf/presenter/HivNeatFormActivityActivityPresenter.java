package org.smartregister.chw.hf.presenter;

import com.nerdstone.neatformcore.domain.model.NFormViewData;
import com.vijay.jsonwizard.utils.FormUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.activity.HivNeatFormsActivity;
import org.smartregister.chw.hiv.contract.BaseNeatFormsContract;
import org.smartregister.chw.hiv.presenter.BaseNeatFormActivityPresenter;
import org.smartregister.chw.hiv.util.Constants;
import org.smartregister.chw.hiv.util.DBConstants;
import org.smartregister.chw.hiv.util.JsonFormConstants;

import java.util.Calendar;
import java.util.HashMap;

public class HivNeatFormActivityActivityPresenter extends BaseNeatFormActivityPresenter {

    public HivNeatFormActivityActivityPresenter(@NotNull String baseEntityID, @NotNull BaseNeatFormsContract.View view, @NotNull BaseNeatFormsContract.Interactor interactor) {
        super(baseEntityID, view, interactor);
    }

    @Override
    public void saveForm(@NotNull HashMap<String, NFormViewData> valuesHashMap, @NotNull JSONObject jsonObject) {
        super.saveForm(valuesHashMap, jsonObject);
        try {
            if (jsonObject.getString(JsonFormConstants.ENCOUNTER_TYPE).equals(Constants.EventType.HIV_INDEX_CONTACT_TESTING_FOLLOWUP)) {
                if (valuesHashMap.containsKey(DBConstants.Key.TEST_RESULTS) && String.valueOf(valuesHashMap.get(DBConstants.Key.TEST_RESULTS).getValue()).contains("Positive")) {
                    //The following implementation is used to generate an HIV Registration event since Index Clients who have tested positive
                    // need to be added to the HIV Register so that they can also elicit their contacts for Index testing
                    generateAndSaveAnHivRegistrationEvent(valuesHashMap);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void generateAndSaveAnHivRegistrationEvent(@NotNull HashMap<String, NFormViewData> valuesHashMap) {
        HashMap<String, NFormViewData> hivRegistrationValuesHashMap = new HashMap<>();
        hivRegistrationValuesHashMap.put(DBConstants.Key.CTC_NUMBER, valuesHashMap.get(DBConstants.Key.CTC_NUMBER));
        hivRegistrationValuesHashMap.put(DBConstants.Key.PLACE_WHERE_TEST_WAS_CONDUCTED, valuesHashMap.get(DBConstants.Key.PLACE_WHERE_TEST_WAS_CONDUCTED));
        hivRegistrationValuesHashMap.put(DBConstants.Key.TEST_RESULTS, valuesHashMap.get(DBConstants.Key.TEST_RESULTS));
        hivRegistrationValuesHashMap.put(DBConstants.Key.CLIENT_HIV_STATUS_DURING_REGISTRATION, new NFormViewData("Calculation", "positive", null, true));
        hivRegistrationValuesHashMap.put(DBConstants.Key.HIV_REGISTRATION_DATE, new NFormViewData("Calculation", Calendar.getInstance().getTimeInMillis(), null, true));

        try {
            JSONObject hivRegistrationFormJsonObject = (new FormUtils()).getFormJsonFromRepositoryOrAssets(((HivNeatFormsActivity) getView()), CoreConstants.JSON_FORM.getHivRegistration());
            if (hivRegistrationFormJsonObject != null) {
                super.saveForm(hivRegistrationValuesHashMap, hivRegistrationFormJsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRegistrationSaved(boolean saveSuccessful, @NotNull String encounterType) {
        super.onRegistrationSaved(saveSuccessful, encounterType);

        //Calling client processor to start processing events in the background
        CoreChildUtils.processClientProcessInBackground();
    }
}