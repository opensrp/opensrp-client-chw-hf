package org.smartregister.chw.hf.utils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.repository.VisitDetailsRepository;
import org.smartregister.chw.anc.repository.VisitRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

public class VisitUtils extends org.smartregister.chw.anc.util.VisitUtils {
    public static void processVisits() throws Exception {
        processVisits(AncLibrary.getInstance().visitRepository(), AncLibrary.getInstance().visitDetailsRepository());
    }

    public static void processVisits(VisitRepository visitRepository, VisitDetailsRepository visitDetailsRepository) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);

        List<Visit> visits = visitRepository.getAllUnSynced(calendar.getTime().getTime());

        List<Visit> firstVisitsCompleted = new ArrayList<>();
        List<Visit> followupVisitsCompleted = new ArrayList<>();

        for(Visit v : visits){
            if(v.getVisitType().equalsIgnoreCase(Constants.Events.ANC_FIRST_FACILITY_VISIT)){
               try {
                   JSONObject jsonObject = new JSONObject(v.getJson());
                   JSONArray obs = jsonObject.getJSONArray("obs");

                   boolean isMedicalAndSurgicalHistoryDone = computeCompletionStatus(obs, "medical_surgical_history");
                   boolean isObstetricExaminationDone = computeCompletionStatus(obs, "abdominal_scars");
                   boolean isBaselineInvestigationDone = computeCompletionStatus(obs, "glucose_in_urine");
                   boolean isTTVaccinationDone = computeCompletionStatus(obs, "tt_card");
                   if(isMedicalAndSurgicalHistoryDone &&
                      isObstetricExaminationDone &&
                      isBaselineInvestigationDone &&
                      isTTVaccinationDone  ){
                       firstVisitsCompleted.add(v);
                   }
               } catch (Exception e){
                   Timber.e(e);
               }
            }
            else if(v.getVisitType().equalsIgnoreCase(Constants.Events.ANC_RECURRING_FACILITY_VISIT)){
                try {
                    JSONObject jsonObject = new JSONObject(v.getJson());
                    JSONArray obs = jsonObject.getJSONArray("obs");

                    boolean isTriageDone = computeCompletionStatus(obs, "rapid_examination");
                    boolean isConsultationDone = computeCompletionStatus(obs, "examination_findings");
                    boolean isLabTestsDone = computeCompletionStatus(obs, "lab_tests");
                    boolean isPharmacyDone = computeCompletionStatus(obs, "iron_folate_supplements");
                    boolean isPregnancyStatusDone = computeCompletionStatus(obs,"pregnancy_status");

                    if(isTriageDone &&
                            isConsultationDone &&
                            isLabTestsDone &&
                            isPharmacyDone &&
                            isPregnancyStatusDone){
                        followupVisitsCompleted.add(v);
                    }
                } catch (Exception e){
                    Timber.e(e);
                }
            }
        }
        processVisits(firstVisitsCompleted,visitRepository,visitDetailsRepository);
        processVisits(followupVisitsCompleted,visitRepository,visitDetailsRepository);
    }
    public static boolean computeCompletionStatus(JSONArray obs, String checkString) throws JSONException {
        for(int i = 0; i < obs.length(); i++){
            JSONObject checkObj = obs.getJSONObject(i);
            if(checkObj.getString("fieldCode").equalsIgnoreCase(checkString)){
                return true;
            }
        }
        return false;
    }
}
