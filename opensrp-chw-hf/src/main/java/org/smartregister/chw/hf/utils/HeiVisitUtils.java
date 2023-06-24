package org.smartregister.chw.hf.utils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hiv.util.DBConstants;
import org.smartregister.chw.pmtct.PmtctLibrary;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.domain.Visit;
import org.smartregister.chw.pmtct.repository.VisitDetailsRepository;
import org.smartregister.chw.pmtct.repository.VisitRepository;
import org.smartregister.chw.pmtct.util.VisitUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.repository.AllSharedPreferences;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class HeiVisitUtils extends VisitUtils {
    public static void processVisits() throws Exception {
        processVisits(PmtctLibrary.getInstance().visitRepository(), PmtctLibrary.getInstance().visitDetailsRepository());
    }

    public static void processVisits(VisitRepository visitRepository, VisitDetailsRepository visitDetailsRepository) throws Exception {

        List<Visit> visits = visitRepository.getAllUnSynced();
        List<Visit> heiFollowupVisits = new ArrayList<>();


        for (Visit v : visits) {
            Date visitDate = new Date(v.getDate().getTime());
            int daysDiff = TimeUtils.getElapsedDays(visitDate);
            if (daysDiff >= 1 && v.getVisitType().equalsIgnoreCase(Constants.Events.HEI_FOLLOWUP)) {
                try {
                    heiFollowupVisits.add(v);
                } catch (Exception e) {
                    Timber.e(e);
                }
            }

        }

        if (heiFollowupVisits.size() > 0) {
            processVisits(heiFollowupVisits, visitRepository, visitDetailsRepository);
            for (Visit v : heiFollowupVisits) {
                boolean confirmedNegative = isClientConfirmedNegative(v);
                boolean confirmedPositive = isClientConfirmedPositive(v);
                if (confirmedPositive || confirmedNegative) {
                    createCancelledEvent(v, confirmedPositive);
                }
            }
        }
    }

    public static void manualProcessVisit(Visit visit) throws Exception {
        List<Visit> manualProcessedVisits = new ArrayList<>();
        VisitDetailsRepository visitDetailsRepository = PmtctLibrary.getInstance().visitDetailsRepository();
        VisitRepository visitRepository = PmtctLibrary.getInstance().visitRepository();
        manualProcessedVisits.add(visit);

        processVisits(manualProcessedVisits, visitRepository, visitDetailsRepository);
        for (Visit v : manualProcessedVisits) {
            boolean confirmedNegative = isClientConfirmedNegative(v);
            boolean confirmedPositive = isClientConfirmedPositive(v);
            if (confirmedPositive || confirmedNegative) {
                createCancelledEvent(v, confirmedPositive);
            }
        }
    }

    public static boolean isClientConfirmedPositive(Visit visit) {
        try {
            JSONObject jsonObject = new JSONObject(visit.getJson());
            JSONArray obs = jsonObject.getJSONArray("obs");
            int size = obs.length();
            boolean testPositive = false;
            boolean confirmed = false;
            int breakLoop = 0;
            for (int i = 0; i < size; i++) {
                JSONObject checkObj = obs.getJSONObject(i);
                if (checkObj.getString("fieldCode").equalsIgnoreCase("hiv_status")) {
                    breakLoop++;
                    JSONArray values = checkObj.getJSONArray("values");
                    if ((values.getString(0).equalsIgnoreCase("positive"))) {
                        testPositive = true;
                    }
                }
                if (checkObj.getString("fieldCode").equalsIgnoreCase("confirmation_hiv_test_result")) {
                    breakLoop++;
                    JSONArray values = checkObj.getJSONArray("values");
                    if ((values.getString(0).equalsIgnoreCase("yes"))) {
                        confirmed = true;
                    }
                }
                if (breakLoop == 2) {
                    break;
                }
            }
            if (testPositive && confirmed) {
                return true;
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return false;
    }

    public static boolean isClientConfirmedNegative(Visit visit) {
        if (!HeiDao.getNextHivTestAge(visit.getBaseEntityId()).equalsIgnoreCase(Constants.HeiHIVTestAtAge.AT_18_MONTHS)) {
            return false;
        }
        try {
            JSONObject jsonObject = new JSONObject(visit.getJson());
            JSONArray obs = jsonObject.getJSONArray("obs");
            int size = obs.length();
            for (int i = 0; i < size; i++) {
                JSONObject checkObj = obs.getJSONObject(i);
                if (checkObj.getString("fieldCode").equalsIgnoreCase("hiv_status")) {
                    JSONArray values = checkObj.getJSONArray("values");
                    if ((values.getString(0).equalsIgnoreCase("negative"))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return false;
    }

    private static void createCancelledEvent(Visit v, boolean isPositiveConfirmed) throws Exception {
        String json = v.getJson();
        String baseEntityId = v.getBaseEntityId();
        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
        Event closePmtctEvent = getClosePmtctEvent(json, baseEntityId);

        Event closeHEIEvent;
        if (isPositiveConfirmed) {
            //close if positive HEI
            closeHEIEvent = getCloseEventForPositive(json, baseEntityId);
        } else {
            //close if negative HEI
            closeHEIEvent = getCloseEventForNegative(json, baseEntityId);
        }
        NCUtils.addEvent(allSharedPreferences, closeHEIEvent);
        NCUtils.startClientProcessing();

        //process close PMTCT event regardless of hiv status
        NCUtils.addEvent(allSharedPreferences, closePmtctEvent);
        NCUtils.startClientProcessing();
    }

    protected static Event getCloseEventForPositive(String jsonString, String baseEntityId) {
        Event closeHeiEvent = new Gson().fromJson(jsonString, Event.class);

        closeHeiEvent.setEntityType(Constants.TableName.HEI_HIV_RESULTS);
        closeHeiEvent.setEventType(Constants.Events.HEI_POSITIVE_INFANT);
        closeHeiEvent.setBaseEntityId(baseEntityId);
        closeHeiEvent.addObs(
                (new Obs())
                        .withFormSubmissionField(Constants.DBConstants.HIV_REGISTRATION_DATE)
                        .withValue(System.currentTimeMillis())
                        .withFieldCode(Constants.DBConstants.HIV_REGISTRATION_DATE)
                        .withFieldType("formsubmissionField")
                        .withFieldDataType("text")
                        .withParentCode("")
                        .withHumanReadableValues(new ArrayList<>()));
        closeHeiEvent.addObs(
                (new Obs())
                        .withFormSubmissionField(DBConstants.Key.CLIENT_HIV_STATUS_DURING_REGISTRATION)
                        .withValue("positive")
                        .withFieldCode(DBConstants.Key.CLIENT_HIV_STATUS_DURING_REGISTRATION)
                        .withFieldType("formsubmissionField")
                        .withFieldDataType("text")
                        .withParentCode("")
                        .withHumanReadableValues(new ArrayList<>()));
        closeHeiEvent.addObs(
                (new Obs())
                        .withFieldCode(DBConstants.Key.CLIENT_HIV_STATUS_AFTER_TESTING)
                        .withFormSubmissionField(DBConstants.Key.TEST_RESULTS)
                        .withValue("positive")
                        .withFieldType("formsubmissionField")
                        .withFieldDataType("text")
                        .withParentCode("")
                        .withHumanReadableValues(new ArrayList<>()));

        return closeHeiEvent;
    }

    protected static Event getCloseEventForNegative(String jsonString, String baseEntityId) {
        Event closeHeiEvent = new Gson().fromJson(jsonString, Event.class);

        closeHeiEvent.setEntityType(Constants.TableName.HEI_HIV_RESULTS);
        closeHeiEvent.setEventType(Constants.Events.HEI_NEGATIVE_INFANT);
        closeHeiEvent.setBaseEntityId(baseEntityId);

        return closeHeiEvent;
    }

    protected static Event getClosePmtctEvent(String jsonString, String baseEntityId) {
        Event closePmtctEvent = new Gson().fromJson(jsonString, Event.class);

        closePmtctEvent.setEntityType(org.smartregister.chw.pmtct.util.Constants.TABLES.PMTCT_REGISTRATION);
        closePmtctEvent.setEventType(Constants.Events.PMTCT_CLOSE_VISITS);
        closePmtctEvent.setBaseEntityId(HeiDao.getMotherBaseEntityId(baseEntityId));
        closePmtctEvent.setFormSubmissionId(JsonFormUtils.generateRandomUUIDString());
        closePmtctEvent.setEventDate(new Date());
        return closePmtctEvent;
    }


    public static void closePmtctForDeceasedHei(String heiBaseEntityId) {
        String motherBaseEntityId = HeiDao.getMotherBaseEntityId(heiBaseEntityId);
        List<MemberObject> heiChildren = HeiDao.getMembersByMotherBaseEntityId(motherBaseEntityId);
        boolean hasOtherHeiChildren = false;
        for (MemberObject memberObject : heiChildren) {
            if (!heiBaseEntityId.equals(memberObject.getBaseEntityId())) {
                hasOtherHeiChildren = true;
            }
        }
        if (!hasOtherHeiChildren) {
            AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
            Event closePmtctEvent = getClosePmtctEvent(new JSONObject().toString(), heiBaseEntityId);
            try {
                NCUtils.addEvent(allSharedPreferences, closePmtctEvent);
                NCUtils.startClientProcessing();
            } catch (Exception e) {
                Timber.e(e);
            }
        }

    }
}
