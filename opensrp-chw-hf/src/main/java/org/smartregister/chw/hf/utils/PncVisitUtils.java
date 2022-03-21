package org.smartregister.chw.hf.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.repository.VisitDetailsRepository;
import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.hf.dao.HfPncDao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

public class PncVisitUtils extends org.smartregister.chw.anc.util.VisitUtils {
    public static void processVisits() throws Exception {
        processVisits(AncLibrary.getInstance().visitRepository(), AncLibrary.getInstance().visitDetailsRepository());
    }

    public static void processVisits(VisitRepository visitRepository, VisitDetailsRepository visitDetailsRepository) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);

        List<Visit> visits = visitRepository.getAllUnSynced(calendar.getTime().getTime());

        List<Visit> pncVisitsCompleted = new ArrayList<>();


        for (Visit v : visits) {
            if (v.getVisitType().equalsIgnoreCase(Constants.Events.PNC_VISIT)) {
                try {
                    JSONObject jsonObject = new JSONObject(v.getJson());
                    JSONArray obs = jsonObject.getJSONArray("obs");
                    String baseEntityId = jsonObject.getString("baseEntityId");

                    List<Boolean> checks = new ArrayList<Boolean>();

                    boolean isMotherGeneralExaminationDone = computeCompletionStatus(obs, "systolic");
                    boolean isFamilyPlanningServicesDone = computeCompletionStatus(obs, "education_counselling_given");
                    boolean isImmunizationDone = computeCompletionStatus(obs, "tetanus_vaccination") ||computeCompletionStatus(obs, "hepatitis_b_vaccination") ;
                    boolean isHivTestingDone = computeCompletionStatus(obs, "hiv_test_result");
                    boolean isNutritionSupplementsDone = computeCompletionStatus(obs, "iron_and_folic_acid");

                    if(HfPncDao.isMotherEligibleForHivTest(baseEntityId)){
                        checks.add(isHivTestingDone);
                    }
                    if(HfPncDao.isMotherEligibleForTetanus(baseEntityId) || HfPncDao.isMotherEligibleForHepB(baseEntityId)){
                        checks.add(isImmunizationDone);
                    }

                    checks.add(isMotherGeneralExaminationDone);
                    checks.add(isFamilyPlanningServicesDone);
                    checks.add(isNutritionSupplementsDone);
                    if (!checks.contains(false)) {
                        pncVisitsCompleted.add(v);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
            }
        }
        if (pncVisitsCompleted.size() > 0) {
            processVisits(pncVisitsCompleted, visitRepository, visitDetailsRepository);
        }
    }

    public static boolean computeCompletionStatus(JSONArray obs, String checkString) throws JSONException {
        int size = obs.length();
        for (int i = 0; i < size; i++) {
            JSONObject checkObj = obs.getJSONObject(i);
            if (checkObj.getString("fieldCode").equalsIgnoreCase(checkString)) {
                return true;
            }
        }
        return false;
    }


}
