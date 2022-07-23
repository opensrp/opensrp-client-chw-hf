package org.smartregister.chw.hf.utils;

import com.google.gson.Gson;

import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.repository.VisitDetailsRepository;
import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.clientandeventmodel.Event;
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
            Date truncatedUpdatedDate = DateUtils.truncate(v.getUpdatedAt(), Calendar.DATE);
            Date today = DateUtils.truncate(new Date(), Calendar.DATE);
            if (truncatedUpdatedDate.before(today)) {
                if (v.getVisitType().equalsIgnoreCase(Constants.Events.ANC_FIRST_FACILITY_VISIT)) {
                    if (istAncVisitComplete(v)) {
                        ancFirstVisitsCompleted.add(v);
                    }
                } else if (v.getVisitType().equalsIgnoreCase(Constants.Events.ANC_RECURRING_FACILITY_VISIT)) {
                    try {
//                        JSONObject jsonObject = new JSONObject(v.getJson());
//                        JSONArray obs = jsonObject.getJSONArray("obs");
//
//                        boolean isTriageDone = computeCompletionStatus(obs, "rapid_examination");
//                        boolean isPregnancyStatusDone = computeCompletionStatus(obs, "pregnancy_status");
//
//                        String ttCheckString = "tt_vaccination";
//
//                        if (isTriageDone && isPregnancyStatusDone) {
//                            if (checkIfStatusIsViable(obs)) {
//                                boolean isConsultationDone = computeCompletionStatus(obs, "examination_findings");
//                                boolean isLabTestsDone = computeCompletionStatus(obs, "hb_level_test");
//                                boolean isPharmacyDone = computeCompletionStatus(obs, "iron_folate_supplements");
//                                boolean isCounsellingDone = computeCompletionStatus(obs, "given_counselling");
//                                boolean isTTVaccinationDone = computeCompletionStatus(obs, ttCheckString);
//
//                                if (HfAncDao.isEligibleForTtVaccination(v.getBaseEntityId())) {
//                                    if (isConsultationDone && isLabTestsDone && isPharmacyDone && isCounsellingDone && isTTVaccinationDone) {
//                                        ancFollowupVisitsCompleted.add(v);
//                                    }
//                                } else {
//                                    if (isConsultationDone && isLabTestsDone && isPharmacyDone && isCounsellingDone) {
//                                        ancFollowupVisitsCompleted.add(v);
//                                    }
//                                }
//
//                            } else {
//                                ancFollowupVisitsCompleted.add(v);
//                            }
//                        }
                    } catch (Exception e) {
                        Timber.e(e);
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
            int size = obs.length();
            for (int i = 0; i < size; i++) {
                JSONObject checkObj = obs.getJSONObject(i);
                if (checkObj.getString("fieldCode").equalsIgnoreCase("pregnancy_status")) {
                    JSONArray values = checkObj.getJSONArray("values");
                    if (!(values.getString(0).equalsIgnoreCase("viable"))) {
                        isCancelled = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return isCancelled;
    }

    public static boolean istAncVisitComplete(Visit visit) {
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
                if (!completionObject.containsValue(false)) {
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
                completionObject.put("isTriageDone", computeCompletionStatusForAction(obs, "triage_completion_status"));
                completionObject.put("isPregnancyStatusDone", computeCompletionStatusForAction(obs, "pregnancy_status_completion_status"));
                if (checkIfStatusIsViable(obs)) {
                    //check the other fields
                    completionObject.put("isConsultationDone", computeCompletionStatusForAction(obs, "consultation_completion_status"));
                    completionObject.put("isMalariaInvestigationComplete", computeCompletionStatusForAction(obs, "malaria_investigation_completion_status"));
                }

                if (!completionObject.containsValue(false)) {
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

}
