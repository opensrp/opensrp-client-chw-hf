package org.smartregister.chw.hf.presenter;

import static org.smartregister.chw.hf.utils.Constants.JsonForm.HIV_REGISTRATION;

import com.nerdstone.neatformcore.domain.model.NFormViewData;
import com.vijay.jsonwizard.utils.FormUtils;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.hf.activity.HivFormsActivity;
import org.smartregister.chw.hiv.contract.BaseHivFormsContract;
import org.smartregister.chw.hiv.presenter.BaseHivFormsActivityPresenter;
import org.smartregister.chw.hiv.util.Constants;
import org.smartregister.chw.hiv.util.DBConstants;
import org.smartregister.chw.hiv.util.JsonFormConstants;

import java.util.Calendar;
import java.util.HashMap;

public class HivFormActivityActivityPresenter extends BaseHivFormsActivityPresenter {

    public HivFormActivityActivityPresenter(@NotNull String baseEntityID, @NotNull BaseHivFormsContract.View view, @NotNull BaseHivFormsContract.Interactor interactor) {
        super(baseEntityID, view, interactor);
    }

    @Override
    public void saveForm(@NotNull HashMap<String, NFormViewData> valuesHashMap, @NotNull JSONObject jsonObject) {
        super.saveForm(valuesHashMap, jsonObject);
        try {
            if (jsonObject.getString(JsonFormConstants.ENCOUNTER_TYPE).equals(Constants.EventType.HIV_INDEX_CONTACT_TESTING_FOLLOWUP)) {
                boolean is_to_be_registered_to_hiv_registry = valuesHashMap.containsKey(DBConstants.Key.TEST_RESULTS)
                        && StringUtils.containsIgnoreCase(String.valueOf(valuesHashMap.get(DBConstants.Key.TEST_RESULTS).getValue()), "positive")
                        && StringUtils.containsIgnoreCase(String.valueOf(valuesHashMap.get(DBConstants.Key.ENROLLED_TO_CLINIC).getValue()), "yes");

                if (is_to_be_registered_to_hiv_registry) {
                    //The following implementation is used to generate an HIV Registration event since Index Clients who have tested positive and registered for ctc
                    // need to be added to the HIV Register so that they can also elicit their contacts for Index testing
                    generateAndSaveAnHivRegistrationEvent(valuesHashMap);
                }
            }
            if(jsonObject.getString(JsonFormConstants.ENCOUNTER_TYPE).equals(org.smartregister.chw.hf.utils.Constants.Events.UPDATE_HIV_INDEX_TESTING_FOLLOWUP)){
                generateAndSaveAnHivRegistrationEvent(valuesHashMap);
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
        hivRegistrationValuesHashMap.put(DBConstants.Key.CLIENT_HIV_STATUS_DURING_REGISTRATION, new NFormViewData("Calculation", "Positive", null, true));
        hivRegistrationValuesHashMap.put(DBConstants.Key.HIV_REGISTRATION_DATE, new NFormViewData("Calculation", Calendar.getInstance().getTimeInMillis(), null, true));

        try {
            JSONObject hivRegistrationFormJsonObject = (new FormUtils()).getFormJsonFromRepositoryOrAssets(((HivFormsActivity) getView()), HIV_REGISTRATION);
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