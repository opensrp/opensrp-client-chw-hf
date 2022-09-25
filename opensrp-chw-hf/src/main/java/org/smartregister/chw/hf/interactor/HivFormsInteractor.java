package org.smartregister.chw.hf.interactor;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.nerdstone.neatformcore.domain.model.NFormViewData;

import org.json.JSONObject;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.hiv.contract.BaseHivFormsContract;
import org.smartregister.chw.hiv.interactor.BaseHivFormsInteractor;
import org.smartregister.chw.hiv.util.Constants;
import org.smartregister.chw.hiv.util.JsonFormConstants;
import org.smartregister.chw.hiv.util.JsonFormUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.Utils;

import java.util.HashMap;

import timber.log.Timber;

public class HivFormsInteractor extends BaseHivFormsInteractor {
    @Override
    public void saveRegistration(@NonNull String baseEntityId, @NonNull HashMap<String, NFormViewData> valuesHashMap, @NonNull JSONObject jsonObject, @NonNull BaseHivFormsContract.InteractorCallBack callBack) {
        try {
            if (jsonObject.getString(JsonFormConstants.ENCOUNTER_TYPE).equals(Constants.EventType.HIV_INDEX_CONTACT_COMMUNITY_FOLLOWUP)) {
                Event event =
                        JsonFormUtils.processJsonForm(
                                getHivLibrary(), baseEntityId, valuesHashMap,
                                jsonObject, jsonObject.getString(JsonFormConstants.ENCOUNTER_TYPE)
                        );


                AllSharedPreferences allSharedPreferences = Utils.getAllSharedPreferences();
                org.smartregister.chw.anc.util.JsonFormUtils.tagEvent(allSharedPreferences, event);

                NFormViewData syncLocation = (NFormViewData) valuesHashMap.get("chw_referral_hf").getValue();
                event.setLocationId(syncLocation.getMetadata().get("openmrs_entity_id").toString());

                Timber.i("Event = %s", new Gson().toJson(event));
                NCUtils.processEvent(
                        event.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(event)));
                callBack.onRegistrationSaved(true, jsonObject.getString(JsonFormConstants.ENCOUNTER_TYPE));

            } else {
                super.saveRegistration(baseEntityId, valuesHashMap, jsonObject, callBack);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
