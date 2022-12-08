package org.smartregister.chw.hf.utils;

import static org.smartregister.chw.anc.util.NCUtils.getSyncHelper;
import static org.smartregister.chw.hf.utils.LDVisitUtils.getFieldValue;
import static org.smartregister.chw.hf.utils.PncVisitUtils.computeCompletionStatus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.repository.VisitDetailsRepository;
import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.dao.ChwNotificationDao;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.pmtct.dao.PmtctDao;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.immunization.service.intent.RecurringIntentService;
import org.smartregister.immunization.service.intent.VaccineIntentService;
import org.smartregister.repository.AllSharedPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import timber.log.Timber;

public class VisitUtils extends org.smartregister.chw.anc.util.VisitUtils {
    public static String Complete = "complete";
    public static String Pending = "pending";
    public static String Ongoing = "ongoing";

    public static void processVisits() throws Exception {
        processVisits(AncLibrary.getInstance().visitRepository(), AncLibrary.getInstance().visitDetailsRepository());
    }

    public static void processVisits(VisitRepository visitRepository, VisitDetailsRepository visitDetailsRepository) throws Exception {
        Calendar calendar = Calendar.getInstance();

        List<Visit> visits = visitRepository.getAllUnSynced(calendar.getTime().getTime());

        List<Visit> ancFirstVisitsCompleted = new ArrayList<>();
        List<Visit> ancFollowupVisitsCompleted = new ArrayList<>();


        for (Visit v : visits) {
            Date updatedAtDate = new Date(v.getDate().getTime());
            int daysDiff = TimeUtils.getElapsedDays(updatedAtDate);
            if (daysDiff >= 1) {
                if (v.getVisitType().equalsIgnoreCase(Constants.Events.ANC_FIRST_FACILITY_VISIT)) {
                    if (isAncVisitComplete(v)) {
                        ancFirstVisitsCompleted.add(v);
                    }
                } else if (v.getVisitType().equalsIgnoreCase(Constants.Events.ANC_RECURRING_FACILITY_VISIT)) {
                    if (isAncVisitComplete(v)) {
                        ancFollowupVisitsCompleted.add(v);
                    }
                }
            }
        }
        if (ancFirstVisitsCompleted.size() > 0) {
            processVisits(ancFirstVisitsCompleted, visitRepository, visitDetailsRepository);
        }

        if (ancFollowupVisitsCompleted.size() > 0) {
            processVisits(ancFollowupVisitsCompleted, visitRepository, visitDetailsRepository);
            for (Visit v : ancFollowupVisitsCompleted) {
                if (isNextVisitsCancelled(v)) {
                    createCancelledEvent(v.getJson());
                    createEventToMoveAncClientsWithStillBirthToPnc(v.getJson());
                    if (PmtctDao.isRegisteredForPmtct(v.getBaseEntityId())) {
                        createClosePmtctEvent(v.getJson());
                    }
                }
            }
        }
    }

    private static void createCancelledEvent(String json) throws Exception {
        Event baseEvent = new Gson().fromJson(json, Event.class);
        baseEvent.setFormSubmissionId(UUID.randomUUID().toString());
        baseEvent.setEventType("ANC Close Followup Visits");
        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
        NCUtils.addEvent(allSharedPreferences, baseEvent);
        NCUtils.startClientProcessing();
    }

    private static void createEventToMoveAncClientsWithStillBirthToPnc(String json) throws Exception {
        JSONObject visitJson = new JSONObject(json);
        JSONArray obs = visitJson.getJSONArray("obs");
        String pregnancyStatus = getFieldValue(obs, "pregnancy_status");
        if (pregnancyStatus.equalsIgnoreCase("intrauterine_fetal_death")) {
            Event baseEvent = new Gson().fromJson(json, Event.class);
            baseEvent.setFormSubmissionId(UUID.randomUUID().toString());
            baseEvent.setEventType("Transfer to PNC");
            AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
            NCUtils.addEvent(allSharedPreferences, baseEvent);
            NCUtils.startClientProcessing();
        }
    }

    protected static void createClosePmtctEvent(String jsonString) {
        Event closePmtctEvent = new Gson().fromJson(jsonString, Event.class);
        closePmtctEvent.setEntityType(org.smartregister.chw.pmtct.util.Constants.TABLES.PMTCT_REGISTRATION);
        closePmtctEvent.setEventType(Constants.Events.PMTCT_CLOSE_VISITS);
        closePmtctEvent.setFormSubmissionId(UUID.randomUUID().toString());
        closePmtctEvent.setEventDate(new Date());
        try {
            AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
            NCUtils.addEvent(allSharedPreferences, closePmtctEvent);
            NCUtils.startClientProcessing();
        } catch (Exception e) {
            Timber.e(e);
        }

    }

    public static boolean computeCompletionStatusForAction(JSONArray obs, String checkString) throws JSONException {
        int size = obs.length();
        for (int i = 0; i < size; i++) {
            JSONObject checkObj = obs.getJSONObject(i);
            if (checkObj.getString("fieldCode").equalsIgnoreCase(checkString)) {
                String status = checkObj.getJSONArray("values").getString(0);
                return status.equalsIgnoreCase("complete");
            }
        }
        return false;
    }

    public static boolean checkIfStatusIsViable(JSONArray obs) throws JSONException {
        String pregnancyStatus = "";
        int size = obs.length();
        for (int i = 0; i < size; i++) {
            JSONObject checkObj = obs.getJSONObject(i);
            if (checkObj.getString("fieldCode").equalsIgnoreCase("pregnancy_status")) {
                JSONArray values = checkObj.getJSONArray("values");
                pregnancyStatus = values.getString(0);
                break;
            }
        }
        return pregnancyStatus.equalsIgnoreCase("viable");
    }

    public static boolean isNextVisitsCancelled(Visit visit) {
        boolean isCancelled = false;
        try {
            JSONObject jsonObject = new JSONObject(visit.getJson());
            JSONArray obs = jsonObject.getJSONArray("obs");
            isCancelled = !checkIfStatusIsViable(obs);
        } catch (Exception e) {
            Timber.e(e);
        }
        return isCancelled;
    }

    public static boolean isAncVisitComplete(Visit visit) {
        boolean isComplete = false;
        if (visit.getVisitType().equalsIgnoreCase(Constants.Events.ANC_FIRST_FACILITY_VISIT)) {
            try {
                JSONObject jsonObject = new JSONObject(visit.getJson());
                JSONArray obs = jsonObject.getJSONArray("obs");
                HashMap<String, Boolean> completionObject = new HashMap<>();
                completionObject.put("isMedicalAndSurgicalHistoryDone", computeCompletionStatusForAction(obs, "medical_surgical_history_completion_status"));
                completionObject.put("isBaselineInvestigationComplete", computeCompletionStatusForAction(obs, "baseline_investigation_completion_status"));
                completionObject.put("isObstetricExaminationComplete", computeCompletionStatusForAction(obs, "obstetric_examination_completion_status"));
                completionObject.put("isTbScreeningComplete", computeCompletionStatusForAction(obs, "tb_screening_completion_status"));
                completionObject.put("isMalariaInvestigationComplete", computeCompletionStatusForAction(obs, "malaria_investigation_completion_status"));
                completionObject.put("isPharmacyComplete", computeCompletionStatusForAction(obs, "pharmacy_completion_status"));
                completionObject.put("isTTVaccinationComplete", computeCompletionStatusForAction(obs, "tt_vaccination_completion_status"));
                completionObject.put("isCounsellingComplete", computeCompletionStatusForAction(obs, "counselling_completion_status"));
                boolean isNextVisitSet = computeCompletionStatus(obs, "next_facility_visit_date");

                if (!completionObject.containsValue(false) && isNextVisitSet) {
                    isComplete = true;
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        if (visit.getVisitType().equalsIgnoreCase(Constants.Events.ANC_RECURRING_FACILITY_VISIT)) {
            try {
                JSONObject jsonObject = new JSONObject(visit.getJson());
                JSONArray obs = jsonObject.getJSONArray("obs");
                HashMap<String, Boolean> completionObject = new HashMap<>();
                completionObject.put("isPregnancyStatusDone", computeCompletionStatusForAction(obs, "pregnancy_status_completion_status"));
                boolean isNextVisitSet = true;
                if (checkIfStatusIsViable(obs)) {
                    completionObject.put("isTriageDone", computeCompletionStatusForAction(obs, "triage_completion_status"));
                    completionObject.put("isConsultationDone", computeCompletionStatusForAction(obs, "consultation_completion_status"));
                    completionObject.put("isMalariaInvestigationComplete", computeCompletionStatusForAction(obs, "malaria_investigation_completion_status"));
                    completionObject.put("isPharmacyComplete", computeCompletionStatusForAction(obs, "pharmacy_completion_status"));
                    completionObject.put("isLabTestComplete", computeCompletionStatusForAction(obs, "lab_test_completion_status"));
                    if (HfAncDao.isEligibleForTtVaccination(visit.getBaseEntityId())) {
                        completionObject.put("isTTVaccinationComplete", computeCompletionStatusForAction(obs, "tt_vaccination_completion_status"));
                    }
                    completionObject.put("isCounsellingComplete", computeCompletionStatusForAction(obs, "counselling_completion_status"));
                    isNextVisitSet = computeCompletionStatus(obs, "next_facility_visit_date");
                }

                if (!completionObject.containsValue(false) && isNextVisitSet) {
                    isComplete = true;
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return isComplete;
    }

    public static String getActionStatus(Map<String, Boolean> checkObject) {
        for (Map.Entry<String, Boolean> entry : checkObject.entrySet()) {
            if (entry.getValue()) {
                if (checkObject.containsValue(false)) {
                    return Ongoing;
                }
                return Complete;
            }
        }
        return Pending;
    }

    public static void manualProcessVisit(Visit visit, Context context) throws Exception {
        List<Visit> manualProcessedVisits = new ArrayList<>();
        VisitDetailsRepository visitDetailsRepository = AncLibrary.getInstance().visitDetailsRepository();
        VisitRepository visitRepository = AncLibrary.getInstance().visitRepository();
        manualProcessedVisits.add(visit);
        processVisits(manualProcessedVisits, visitRepository, visitDetailsRepository);
        if (visit.getVisitType().equalsIgnoreCase(Constants.Events.ANC_RECURRING_FACILITY_VISIT) && isNextVisitsCancelled(visit)) {
            createCancelledEvent(visit.getJson());
            createEventToMoveAncClientsWithStillBirthToPnc(visit.getJson());
            if (PmtctDao.isRegisteredForPmtct(visit.getBaseEntityId())) {
                createClosePmtctEvent(visit.getJson());
            }
            ((Activity) context).finish();
        }
    }

    public static void processVisits(List<Visit> visits, VisitRepository visitRepository, VisitDetailsRepository visitDetailsRepository) throws Exception {
        String visitGroupId = UUID.randomUUID().toString();
        for (Visit v : visits) {
            if (!v.getProcessed()) {

                // persist to db
                Event baseEvent = new Gson().fromJson(v.getPreProcessedJson(), Event.class);
                if (StringUtils.isBlank(baseEvent.getFormSubmissionId()))
                    baseEvent.setFormSubmissionId(UUID.randomUUID().toString());

                String locationId = ChwNotificationDao.getSyncLocationId(baseEvent.getBaseEntityId());

                baseEvent.addDetails(org.smartregister.chw.anc.util.Constants.HOME_VISIT_GROUP, visitGroupId);


                AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
                JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
                baseEvent.setLocationId(locationId);
                JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
                getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson);

                // process details
                processVisitDetails(visitGroupId, v, visitDetailsRepository, v.getVisitId(), v.getBaseEntityId());

                visitRepository.completeProcessing(v.getVisitId());
            }
        }

        // process after all events are saved
        NCUtils.startClientProcessing();

        // process vaccines and services
        Context context = AncLibrary.getInstance().context().applicationContext();
        context.startService(new Intent(context, VaccineIntentService.class));
        context.startService(new Intent(context, RecurringIntentService.class));
    }

    private static void processVisitDetails(String visitGroupId, Visit visit, VisitDetailsRepository visitDetailsRepository, String visitID, String baseEntityID) {
        List<VisitDetail> visitDetailList = visitDetailsRepository.getVisits(visitID);
        for (VisitDetail visitDetail : visitDetailList) {
            if (!visitDetail.getProcessed()) {
                if (org.smartregister.chw.anc.util.Constants.HOME_VISIT_TASK.SERVICE.equalsIgnoreCase(visitDetail.getPreProcessedType())) {
                    saveVisitDetailsAsServiceRecord(visitGroupId, visitDetail, baseEntityID, visit.getDate());
                    visitDetailsRepository.completeProcessing(visitDetail.getVisitDetailsId());
                    continue;
                }


                if (
                        org.smartregister.chw.anc.util.Constants.HOME_VISIT_TASK.VACCINE.equalsIgnoreCase(visitDetail.getParentCode()) ||
                                org.smartregister.chw.anc.util.Constants.HOME_VISIT_TASK.VACCINE.equalsIgnoreCase(visitDetail.getPreProcessedType())
                ) {
                    saveVisitDetailsAsVaccine(visitGroupId, visitDetail, baseEntityID, visit.getDate());
                    visitDetailsRepository.completeProcessing(visitDetail.getVisitDetailsId());
                    continue;
                }

                visitDetailsRepository.completeProcessing(visitDetail.getVisitDetailsId());
            }
        }
    }

}
