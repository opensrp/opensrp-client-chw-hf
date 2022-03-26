package org.smartregister.chw.hf.utils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.repository.VisitDetailsRepository;
import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.model.ChildModel;
import org.smartregister.chw.hf.dao.HfPncDao;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.repository.AllSharedPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

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
        List<Visit> childVisitCompleted = new ArrayList<>();


        for (Visit v : visits) {
            if (v.getVisitType().equalsIgnoreCase(Constants.Events.PNC_VISIT)) {
                try {
                    JSONObject jsonObject = new JSONObject(v.getJson());
                    JSONArray obs = jsonObject.getJSONArray("obs");
                    String baseEntityId = jsonObject.getString("baseEntityId");

                    List<Boolean> checks = new ArrayList<Boolean>();

                    boolean isMotherGeneralExaminationDone = computeCompletionStatus(obs, "systolic");
                    boolean isFamilyPlanningServicesDone = computeCompletionStatus(obs, "education_counselling_given");
                    boolean isImmunizationDone = computeCompletionStatus(obs, "tetanus_vaccination") || computeCompletionStatus(obs, "hepatitis_b_vaccination");
                    boolean isHivTestingDone = computeCompletionStatus(obs, "hiv_status");
                    boolean isNutritionSupplementsDone = computeCompletionStatus(obs, "iron_and_folic_acid");

                    if (HfPncDao.isMotherEligibleForHivTest(baseEntityId)) {
                        checks.add(isHivTestingDone);
                    }
                    if (HfPncDao.isMotherEligibleForTetanus(baseEntityId) || HfPncDao.isMotherEligibleForHepB(baseEntityId)) {
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
            if (v.getVisitType().equalsIgnoreCase(Constants.Events.PNC_CHILD_FOLLOWUP)) {
                try {
                    JSONObject jsonObject = new JSONObject(v.getJson());
                    JSONArray obs = jsonObject.getJSONArray("obs");

                    boolean isChildFollowupDone = computeCompletionStatus(obs, "child_activeness");
                    if (isChildFollowupDone) {
                        childVisitCompleted.add(v);
                    }
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }
        }
        if (pncVisitsCompleted.size() > 0) {
            for (Visit v : pncVisitsCompleted) {
                if (childVisitCompleted.size() > 0) {
                    List<Visit> completedChildVisit = new ArrayList<>();
                    List<Visit> completedMotherVisit = new ArrayList<>();
                    for (Visit childVisit : pncVisitsCompleted) {
                        if (v.getVisitId().equals(childVisit.getParentVisitID())) {
                            completedChildVisit.add(childVisit);
                            completedMotherVisit.add(v);
                        }
                    }
                    processVisits(completedMotherVisit, visitRepository, visitDetailsRepository);
                    processVisits(completedChildVisit, visitRepository, visitDetailsRepository);
                    if (isMotherFoundPositive(v)) {
                        createHeiRegistrationEvent(v.getJson());
                    }
                }
            }
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

    private static void createHeiRegistrationEvent(String json) throws Exception {
        JSONObject jsonObject = new JSONObject(json);
        String motherBaseEntityId = jsonObject.getString("baseEntityId");
        List<ChildModel> childModels = HfPncDao.childrenForPncWoman(motherBaseEntityId);
        for (ChildModel childModel : childModels) {
            String jsonForChild = json;
            JSONObject jsonObjectForChild = new JSONObject(jsonForChild);
            jsonObjectForChild.put("baseEntityId", childModel.getBaseEntityId());
            jsonObjectForChild.put("mother_entity_id", motherBaseEntityId);
            jsonObjectForChild.put("relational_id", motherBaseEntityId);
            jsonObjectForChild.put("birthdate", childModel.getDateOfBirth());

            String childBaseEntityId = childModel.getBaseEntityId();
            Event baseEvent = new Gson().fromJson(jsonForChild, Event.class);
            baseEvent.setFormSubmissionId(UUID.randomUUID().toString());
            baseEvent.setEventType("HEI Registration");


            baseEvent.addObs(
                    (new Obs())
                            .withFormSubmissionField("risk_category")
                            .withValue("high")
                            .withFieldCode("risk_category")
                            .withFieldType("formsubmissionField")
                            .withFieldDataType("text")
                            .withParentCode("")
                            .withHumanReadableValues(new ArrayList<>()));


            baseEvent.setBaseEntityId(childBaseEntityId);
            AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
            NCUtils.addEvent(allSharedPreferences, baseEvent);
            NCUtils.startClientProcessing();
        }

    }

    private static boolean isMotherFoundPositive(Visit visit) {
        boolean isPositive = false;
        try {
            JSONObject jsonObject = new JSONObject(visit.getJson());
            JSONArray obs = jsonObject.getJSONArray("obs");
            int size = obs.length();
            for (int i = 0; i < size; i++) {
                JSONObject checkObj = obs.getJSONObject(i);
                if (checkObj.getString("fieldCode").equalsIgnoreCase("hiv_status")) {
                    JSONArray values = checkObj.getJSONArray("values");
                    if (values.getString(0).equalsIgnoreCase("positive")) {
                        isPositive = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return isPositive;
    }

}
