package org.smartregister.chw.hf.interactor;

import static org.smartregister.client.utils.constants.JsonFormConstants.STEP1;

import org.json.JSONObject;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.dao.ChwNotificationDao;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.CoreReferralUtils;
import org.smartregister.chw.pmtct.contract.PmtctProfileContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.interactor.BasePmtctProfileInteractor;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.repository.AllSharedPreferences;

import timber.log.Timber;

public class HeiProfileInteractor extends BasePmtctProfileInteractor {

    @Override
    public void refreshProfileInfo(MemberObject memberObject, PmtctProfileContract.InteractorCallBack callback) {
        super.refreshProfileInfo(memberObject, callback);
    }

    public void createHeiCommunityFollowupReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) {
        Event baseEvent = JsonFormUtils.processJsonForm(allSharedPreferences, CoreReferralUtils.setEntityId(jsonString, entityID), CoreConstants.TABLE_NAME.PMTCT_COMMUNITY_FOLLOWUP);
        JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
        try {
            JSONObject motherChampionLocation = CoreJsonFormUtils.getJsonField(new JSONObject(jsonString), STEP1, "mother_champion_location");
            //update sync location to send the referral to the correct targeted chw,
            // this is needed for the case of global search client who have moved or clients that may have moved village
            if(motherChampionLocation != null){
                String locationId = CoreJsonFormUtils.getSyncLocationUUIDFromDropdown(motherChampionLocation);
                baseEvent.setLocationId(locationId);
            }
        }catch (Exception e){
            Timber.e(e);
        }

        try {
            NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(JsonFormUtils.gson.toJson(baseEvent)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createHeiNumberRegistrationEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) {
        Event baseEvent = JsonFormUtils.processJsonForm(allSharedPreferences, CoreReferralUtils.setEntityId(jsonString, entityID), CoreConstants.TABLE_NAME.HEI);
        JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
        String syncLocationId = ChwNotificationDao.getSyncLocationId(baseEvent.getBaseEntityId());
        if (syncLocationId != null) {
            // Allows setting the ID for sync purposes
            baseEvent.setLocationId(syncLocationId);
        }

        try {
            NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(JsonFormUtils.gson.toJson(baseEvent)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
