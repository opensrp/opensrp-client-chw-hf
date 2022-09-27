package org.smartregister.chw.hf.utils;

import com.google.gson.Gson;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.repository.VisitDetailsRepository;
import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.model.ChildModel;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hf.dao.HfPncDao;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.repository.AllSharedPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

public class PncVisitUtils extends org.smartregister.chw.anc.util.VisitUtils {
    public static void processVisits() throws Exception {
        processVisits(AncLibrary.getInstance().visitRepository(), AncLibrary.getInstance().visitDetailsRepository());
    }

    public static void processVisits(VisitRepository visitRepository, VisitDetailsRepository visitDetailsRepository) throws Exception {
        Calendar calendar = Calendar.getInstance();

        List<Visit> visits = visitRepository.getAllUnSynced(calendar.getTime().getTime());

        List<Visit> pncVisitsCompleted = new ArrayList<>();
        List<Visit> childVisitCompleted = new ArrayList<>();


        for (Visit v : visits) {
            Date updatedAtDate = new Date(v.getUpdatedAt().getTime());
            int daysDiff = TimeUtils.getElapsedDays(updatedAtDate);
            if (daysDiff > 1) {
                if (v.getVisitType().equalsIgnoreCase(Constants.Events.PNC_VISIT)) {
                    try {
                        JSONObject jsonObject = new JSONObject(v.getJson());
                        JSONArray obs = jsonObject.getJSONArray("obs");
                        String baseEntityId = jsonObject.getString("baseEntityId");

                        List<Boolean> checks = new ArrayList<Boolean>();

                        boolean isMotherGeneralExaminationDone = computeCompletionStatus(obs, "systolic");
                        boolean isFamilyPlanningServicesDone = computeCompletionStatus(obs, "education_counselling_given");
                        boolean isImmunizationDone = computeCompletionStatus(obs, "tetanus_vaccination") || computeCompletionStatus(obs, "hepatitis_b_vaccination");
                        boolean isHivTestingDone = computeCompletionStatus(obs, "hiv");
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
        }
        if (pncVisitsCompleted.size() > 0) {
            for (Visit v : pncVisitsCompleted) {
                if (childVisitCompleted.size() > 0) {
                    List<Visit> completedChildVisit = new ArrayList<>();
                    List<Visit> completedMotherVisit = new ArrayList<>();
                    for (Visit childVisit : childVisitCompleted) {
                        if (v.getVisitId().equals(childVisit.getParentVisitID())) {
                            completedChildVisit.add(childVisit);
                            completedMotherVisit.add(v);
                        }
                    }
                    processVisits(completedMotherVisit, visitRepository, visitDetailsRepository);
                    processVisits(completedChildVisit, visitRepository, visitDetailsRepository);
                } else {
                    processVisits(Collections.singletonList(v), visitRepository, visitDetailsRepository);
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

    public static void createHeiRegistrationEvent(String motherBaseEntityId) throws Exception {
        List<ChildModel> childModels = HfPncDao.childrenForPncWoman(motherBaseEntityId);
        for (ChildModel childModel : childModels) {
            if (HeiDao.getMember(childModel.getBaseEntityId()) == null) {
                JSONObject jsonObjectForChild = new JSONObject();
                jsonObjectForChild.put("baseEntityId", childModel.getBaseEntityId());
                jsonObjectForChild.put("mother_entity_id", motherBaseEntityId);
                jsonObjectForChild.put("relational_id", motherBaseEntityId);
                jsonObjectForChild.put("birthdate", childModel.getDateOfBirth());

                String childBaseEntityId = childModel.getBaseEntityId();
                Event baseEvent = new Gson().fromJson(jsonObjectForChild.toString(), Event.class);
                baseEvent.setFormSubmissionId(UUID.randomUUID().toString());
                baseEvent.setEventType(Constants.Events.HEI_REGISTRATION);
                baseEvent.setEventDate(new Date());

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

    public static int getElapsedTimeDays(Date startDate, Date endDate) {
        if (startDate == null || endDate == null)
            return 0;

        Calendar startDateCal = Calendar.getInstance();
        startDateCal.setTimeInMillis(startDate.getTime());

        Calendar endDateCal = Calendar.getInstance();
        endDateCal.setTimeInMillis(endDate.getTime());

        LocalDate startLocalDate = new LocalDate(startDateCal.get(Calendar.YEAR), startDateCal.get(Calendar.MONTH) + 1, startDateCal.get(Calendar.DAY_OF_MONTH));
        LocalDate endLocalDate = new LocalDate(endDateCal.get(Calendar.YEAR), endDateCal.get(Calendar.MONTH) + 1, endDateCal.get(Calendar.DAY_OF_MONTH));


        return Days.daysBetween(startLocalDate, endLocalDate).getDays();
    }

}
