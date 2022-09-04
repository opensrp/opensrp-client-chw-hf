package org.smartregister.chw.hf.interactor;

import static org.smartregister.client.utils.constants.JsonFormConstants.STEP1;
import static org.smartregister.client.utils.constants.JsonFormConstants.VALUE;

import org.json.JSONObject;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.interactor.CorePmtctProfileInteractor;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.CoreReferralUtils;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.repository.AllSharedPreferences;

import timber.log.Timber;

public class PmtctProfileInteractor extends CorePmtctProfileInteractor {
    public void createPmtctCommunityFollowupReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) {
        Event baseEvent = JsonFormUtils.processJsonForm(allSharedPreferences, CoreReferralUtils.setEntityId(jsonString, entityID), CoreConstants.TABLE_NAME.PMTCT_COMMUNITY_FOLLOWUP);
        JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);

        try {
            JSONObject reasonsForIssuingCommunityReferral = CoreJsonFormUtils.getJsonField(new JSONObject(jsonString), STEP1, "reasons_for_issuing_community_referral");
            JSONObject motherChampionLocation = CoreJsonFormUtils.getJsonField(new JSONObject(jsonString), STEP1, "mother_champion_location");
            if (reasonsForIssuingCommunityReferral.getString(VALUE).equals("mother_champion_services")) {
                //Updating the event type to Mother Champion Referral
                baseEvent.setEventType(Constants.Events.MOTHER_CHAMPION_COMMUNITY_SERVICES_REFERRAL);
            }
            //update sync location to send the referral to the correct targeted chw,
            // this is needed for the case of global search client who have moved or clients that may have moved village
            if(motherChampionLocation != null){
                String locationId = CoreJsonFormUtils.getSyncLocationUUIDFromDropdown(motherChampionLocation);
                baseEvent.setLocationId(locationId);
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        try {
            NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(JsonFormUtils.gson.toJson(baseEvent)));
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
