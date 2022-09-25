package org.smartregister.chw.hf.interactor;

import static org.smartregister.client.utils.constants.JsonFormConstants.STEP1;

import android.content.Context;

import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.dao.ChwNotificationDao;
import org.smartregister.chw.core.interactor.CoreAncMemberProfileInteractor;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.CoreReferralUtils;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.repository.AllSharedPreferences;

import java.util.Date;

import timber.log.Timber;

public class AncMemberProfileInteractor extends CoreAncMemberProfileInteractor {

    public AncMemberProfileInteractor(Context context) {
        super(context);
    }

    @Override
    protected Date getLastVisitDate(MemberObject memberObject) {
        Date lastVisitDate = null;
        Visit lastVisit;
        lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.ANC_RECURRING_FACILITY_VISIT);
        if (lastVisit != null) {
            lastVisitDate = lastVisit.getDate();
        } else {
            lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.ANC_FIRST_FACILITY_VISIT);
            if (lastVisit != null) {
                lastVisitDate = lastVisit.getDate();
            }
        }

        return lastVisitDate;
    }

    public void createPartnerFollowupReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) throws Exception {
        Event baseEvent = JsonFormUtils.processJsonForm(allSharedPreferences, CoreReferralUtils.setEntityId(jsonString, entityID), CoreConstants.TABLE_NAME.ANC_DANGER_SIGNS_OUTCOME);
        JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);

        try {
            JSONObject chwLocation = CoreJsonFormUtils.getJsonField(new JSONObject(jsonString), STEP1, "chw_location");
            //update sync location to send the referral to the correct targeted chw,
            // this is needed for the case of global search client who have moved or clients that may have moved village
            if(chwLocation != null){
                String locationId = CoreJsonFormUtils.getSyncLocationUUIDFromDropdown(chwLocation);
                baseEvent.setLocationId(locationId);
            }
        }catch (Exception e){
            Timber.e(e);
        }

        String syncLocationId = ChwNotificationDao.getSyncLocationId(baseEvent.getBaseEntityId());
        if (syncLocationId != null) {
            // Allows setting the ID for sync purposes
            baseEvent.setLocationId(syncLocationId);
        }
        NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(JsonFormUtils.gson.toJson(baseEvent)));
    }

    public void createTestingEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) throws Exception {
        Event baseEvent = JsonFormUtils.processJsonForm(allSharedPreferences, CoreReferralUtils.setEntityId(jsonString, entityID), CoreConstants.TABLE_NAME.ANC_MEMBER);
        JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
        NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(JsonFormUtils.gson.toJson(baseEvent)));
    }
}
