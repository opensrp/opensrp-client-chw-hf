package org.smartregister.chw.hf.sync.intent;

import android.app.IntentService;
import android.content.Intent;

import org.json.JSONObject;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.dao.ChwNotificationDao;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hf.dao.HfPmtctDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.util.JsonFormUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by cozej4 on 2022-03-24.
 *
 * @author cozej4 https://github.com/cozej4
 */
public class MarkPmtctAndHeiLtfIntentService extends IntentService {

    private static final String TAG = MarkPmtctAndHeiLtfIntentService.class.getSimpleName();
    private AllSharedPreferences sharedPreferences;
    private ECSyncHelper syncHelper;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());


    public MarkPmtctAndHeiLtfIntentService() {
        super(TAG);
        sharedPreferences = Utils.getAllSharedPreferences();
        syncHelper = FamilyLibrary.getInstance().getEcSyncHelper();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        List<MemberObject> pmtctMembers = HfPmtctDao.getMembers();
        if (pmtctMembers != null) {
            for (MemberObject pmtctMember : pmtctMembers) {
                if (!HfPmtctDao.hasTheClientTransferedOut(pmtctMember.getBaseEntityId()) && !HfPmtctDao.isTheClientLostToFollowup(pmtctMember.getBaseEntityId()) && pmtctMember.getDod() == null) {
                    Date lastVisitDate = HfPmtctDao.getPmtctFollowUpVisitDate(pmtctMember.getBaseEntityId());
                    Calendar ltfCalendar = Calendar.getInstance();
                    if (lastVisitDate == null)
                        lastVisitDate = HfPmtctDao.getPmtctRegisterDate(pmtctMember.getBaseEntityId());

                    if (lastVisitDate != null) {
                        ltfCalendar.setTimeInMillis(lastVisitDate.getTime());
                        ltfCalendar.add(Calendar.DAY_OF_YEAR, 56);
                        checkIfLtf(ltfCalendar, pmtctMember.getBaseEntityId(), ChwNotificationDao.getSyncLocationId(pmtctMember.getBaseEntityId()), HfPmtctDao.getVisitNumber(pmtctMember.getBaseEntityId()), true);
                    }
                }
            }
        }


        List<MemberObject> heiMembers = HeiDao.getMember();
        if (heiMembers != null) {
            for (MemberObject heiMember : heiMembers) {
                if (!HeiDao.hasTheChildTransferedOut(heiMember.getBaseEntityId()) && heiMember.getDod() == null) {
                    int visitNumber = HeiDao.getVisitNumber(heiMember.getBaseEntityId());
                    Date lastVisitDate = HeiDao.getHeiFollowUpVisitDate(heiMember.getBaseEntityId());
                    Calendar ltfCalendar = Calendar.getInstance();

                    if (lastVisitDate == null)
                        lastVisitDate = HeiDao.getHeiRegisterDate(heiMember.getBaseEntityId());

                    if (lastVisitDate != null) {
                        ltfCalendar.setTimeInMillis(lastVisitDate.getTime());

                        if (visitNumber == 1)
                            ltfCalendar.add(Calendar.DAY_OF_YEAR, 56);
                        else
                            ltfCalendar.add(Calendar.DAY_OF_YEAR, 70);

                        checkIfLtf(ltfCalendar, heiMember.getBaseEntityId(), ChwNotificationDao.getSyncLocationId(heiMember.getBaseEntityId()), visitNumber, false);
                    }
                }
            }
        }
    }

    public void checkIfLtf(Calendar ltfCalendar, String baseEntityId, String syncLocationId, int visitNumber, boolean isPmtct) {
        if (Calendar.getInstance().getTime().after(ltfCalendar.getTime())) {
            saveLtfEvent(
                    baseEntityId,
                    syncLocationId,
                    visitNumber,
                    isPmtct
            );
        }
    }


    private void saveLtfEvent(String baseEntityId, String userLocationId, int visitNumber, boolean isPmtct) {
        try {

            Event baseEvent = generateEvent(baseEntityId, userLocationId);
            if (isPmtct) {
                baseEvent.setEventType(Constants.Events.MARK_PMTCT_CLIENT_AS_LTF);
                baseEvent.setEntityType((Constants.TableName.PMTCT_FOLLOWUP));
            } else {
                baseEvent.setEventType(Constants.Events.MARK_HEI_CLIENT_AS_LTF);
                baseEvent.setEntityType((Constants.TableName.HEI_FOLLOWUP));
            }

            baseEvent.addObs((new Obs())
                    .withFormSubmissionField(Constants.FormConstants.FormSubmissionFields.VISIT_NUMBER)
                    .withValue(visitNumber)
                    .withFieldCode(Constants.FormConstants.FormSubmissionFields.VISIT_NUMBER)
                    .withFieldType(CoreConstants.FORMSUBMISSION_FIELD).withFieldDataType(CoreConstants.TEXT).withParentCode("")
                    .withHumanReadableValues(new ArrayList<>()));

            baseEvent.addObs((new Obs())
                    .withFormSubmissionField(Constants.FormConstants.FormSubmissionFields.FOLLOWUP_VISIT_DATE)
                    .withValue(sdf.format(Calendar.getInstance().getTime()))
                    .withFieldCode(Constants.FormConstants.FormSubmissionFields.FOLLOWUP_VISIT_DATE)
                    .withFieldType(CoreConstants.FORMSUBMISSION_FIELD).withFieldDataType(CoreConstants.TEXT).withParentCode("")
                    .withHumanReadableValues(new ArrayList<>()));

            baseEvent.addObs((new Obs())
                    .withFormSubmissionField(Constants.FormConstants.FormSubmissionFields.FOLLOWUP_STATUS)
                    .withValue(Constants.LOST_TO_FOLLOWUP)
                    .withFieldCode(Constants.FormConstants.FormSubmissionFields.FOLLOWUP_STATUS)
                    .withFieldType(CoreConstants.FORMSUBMISSION_FIELD).withFieldDataType(CoreConstants.TEXT).withParentCode("")
                    .withHumanReadableValues(new ArrayList<>()));


            CoreJsonFormUtils.tagSyncMetadata(sharedPreferences, baseEvent);
            syncEvents(baseEvent, baseEntityId);
        } catch (Exception e) {
            Timber.e(e, "MarkPmtctAndHeiLtfIntentService --> savePmtctLtfEvent");
        }
    }

    private Event generateEvent(String baseEntityId, String userLocationId) {
        Event baseEvent = null;
        try {
            AllSharedPreferences sharedPreferences = org.smartregister.family.util.Utils.getAllSharedPreferences();
            baseEvent = (Event) new Event()
                    .withBaseEntityId(baseEntityId)
                    .withEventDate(new Date())
                    .withFormSubmissionId(JsonFormUtils.generateRandomUUIDString())
                    .withProviderId(sharedPreferences.fetchRegisteredANM())
                    .withLocationId(userLocationId)
                    .withTeamId(sharedPreferences.fetchDefaultTeamId(sharedPreferences.fetchRegisteredANM()))
                    .withTeam(sharedPreferences.fetchDefaultTeam(sharedPreferences.fetchRegisteredANM()))
                    .withDateCreated(new Date());

            CoreJsonFormUtils.tagSyncMetadata(sharedPreferences, baseEvent);
        } catch (Exception e) {
            Timber.e(e, "MarkPmtctAndHeiLtfIntentService --> generateEvent");
        }
        return baseEvent;
    }

    private void syncEvents(Event baseEvent, String baseEntityId) {
        try {
            JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
            syncHelper.addEvent(baseEntityId, eventJson);
            long lastSyncTimeStamp = sharedPreferences.fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
            List<String> formSubmissionIds = new ArrayList<>();
            formSubmissionIds.add(baseEvent.getFormSubmissionId());
            CoreChwApplication.getInstance().getClientProcessorForJava().processClient(syncHelper.getEvents(formSubmissionIds));
            sharedPreferences.saveLastUpdatedAtDate(lastSyncDate.getTime());
        } catch (Exception e) {
            Timber.e(e, "MarkPmtctAndHeiLtfIntentService --> syncEvents");
        }
    }


}
