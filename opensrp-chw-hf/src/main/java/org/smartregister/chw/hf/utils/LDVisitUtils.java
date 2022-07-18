package org.smartregister.chw.hf.utils;

import static org.smartregister.chw.hf.utils.Constants.Events.LD_POST_DELIVERY_MOTHER_MANAGEMENT;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.utils.Constants.Events;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.dao.LDDao;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.repository.VisitDetailsRepository;
import org.smartregister.chw.ld.repository.VisitRepository;
import org.smartregister.chw.ld.util.Constants;
import org.smartregister.chw.ld.util.VisitUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * Created by Kassim Sheghembe on 2022-05-10
 */
public class LDVisitUtils extends VisitUtils {

    public static void processVisits(String baseEntityId, boolean isPartograph) throws Exception {
        processVisits(LDLibrary.getInstance().visitRepository(), LDLibrary.getInstance().visitDetailsRepository(), baseEntityId, isPartograph);
    }

    public static void processVisits(VisitRepository visitRepository, VisitDetailsRepository visitDetailsRepository, String baseEntityId, boolean isPartograph) throws Exception {
        Calendar calendar = Calendar.getInstance();

        List<Visit> visits = StringUtils.isNotBlank(baseEntityId) ?
                visitRepository.getAllUnSynced(calendar.getTime().getTime(), baseEntityId) :
                visitRepository.getAllUnSynced(calendar.getTime().getTime());

        List<Visit> ldVisits = new ArrayList<>();

        for (Visit visit : visits) {
            if (visit.getVisitType().equalsIgnoreCase(Constants.EVENT_TYPE.LD_GENERAL_EXAMINATION)) {
                JSONObject visitJson = new JSONObject(visit.getJson());
                JSONArray obs = visitJson.getJSONArray("obs");

                boolean isGeneralConditionDone = computeCompletionStatus(obs, "general_condition");
                boolean isPulseRateDone = computeCompletionStatus(obs, "pulse_rate");
                boolean isRespiratoryRateDone = computeCompletionStatus(obs, "respiratory_rate");
                boolean isTemperatureDone = computeCompletionStatus(obs, "temperature");
                boolean isSystolicDone = computeCompletionStatus(obs, "systolic");
                boolean isDiastolicDone = computeCompletionStatus(obs, "diastolic");
                boolean isUrineProteinDone = computeCompletionStatus(obs, "urine_protein");
                boolean isUrineAcetoneDone = computeCompletionStatus(obs, "urine_acetone");
                boolean isFundalHeightDone = computeCompletionStatus(obs, "fundal_height");
                boolean isPresentationDone = getFieldValue(obs, "lie").equalsIgnoreCase("transverse") || computeCompletionStatus(obs, "presentation");
                boolean isContractionInTenMinutesDone = computeCompletionStatus(obs, "contraction_in_ten_minutes");
                boolean isFetalHeartRateDone = computeCompletionStatus(obs, "fetal_heart_rate");
                boolean isVaginalExamDateDone = computeCompletionStatus(obs, "vaginal_exam_date");
                boolean isVaginalExamTimeDone = computeCompletionStatus(obs, "vaginal_exam_time");
                boolean isCervixStateDone = computeCompletionStatus(obs, "cervix_state");
                boolean isCervixDilationDone = computeCompletionStatus(obs, "cervix_dilation");
                boolean isPresentingPartDone = computeCompletionStatus(obs, "presenting_part");
                boolean isMouldingDone = computeCompletionStatus(obs, "moulding");
                boolean isStationDone = computeCompletionStatus(obs, "station");
                boolean isDecisionDone = computeCompletionStatus(obs, "decision");

                boolean hivActionDone = false;

                if (LDDao.getHivStatus(baseEntityId) == null || !Objects.equals(LDDao.getHivStatus(baseEntityId), org.smartregister.chw.hf.utils.Constants.HIV_STATUS.POSITIVE)) {
                    String hivStatus = getFieldValue(obs, "hiv");
                    String hivTestConducted = getFieldValue(obs, "hiv_test_conducted");
                    if (hivTestConducted != null && hivTestConducted.equalsIgnoreCase("no")) {
                        hivActionDone = true;
                    } else {
                        if (StringUtils.isNotBlank(hivStatus) && hivTestConducted.equalsIgnoreCase("yes")) {
                            hivActionDone = true;
                        }
                    }
                } else {
                    hivActionDone = true;
                }

                if (isGeneralConditionDone &&
                        isPulseRateDone &&
                        isRespiratoryRateDone &&
                        isTemperatureDone &&
                        isSystolicDone &&
                        isDiastolicDone &&
                        isUrineProteinDone &&
                        isUrineAcetoneDone &&
                        isFundalHeightDone &&
                        isPresentationDone &&
                        isContractionInTenMinutesDone &&
                        isFetalHeartRateDone &&
                        isVaginalExamDateDone &&
                        isVaginalExamTimeDone &&
                        isCervixStateDone &&
                        isCervixDilationDone &&
                        isPresentingPartDone &&
                        isMouldingDone &&
                        isStationDone &&
                        isDecisionDone &&
                        hivActionDone) {
                    ldVisits.add(visit);
                }
            } else if (visit.getVisitType().equalsIgnoreCase(Events.LD_PARTOGRAPHY)) {
                if (isPartograph && shouldProcessPartographVisit(visit)) {
                    ldVisits.add(visit);
                }
            } else if (visit.getVisitType().equalsIgnoreCase(Events.LD_ACTIVE_MANAGEMENT_OF_3RD_STAGE_OF_LABOUR)) {
                JSONObject visitJson = new JSONObject(visit.getJson());
                JSONArray obs = visitJson.getJSONArray("obs");

                boolean hasPlacentaAndMembraneExpelled = computeCompletionStatus(obs, "placenta_and_membrane_expulsion");
                boolean isUterotonicDone = computeCompletionStatus(obs, "uterotonic");
                boolean isMassageOfUterusAfterDeliveryDone = computeCompletionStatus(obs, "uterus_massage_after_delivery");

                if (hasPlacentaAndMembraneExpelled && isUterotonicDone && isMassageOfUterusAfterDeliveryDone) {
                    ldVisits.add(visit);
                }
            } else if (visit.getVisitType().equalsIgnoreCase(LD_POST_DELIVERY_MOTHER_MANAGEMENT)) {
                JSONObject visitJson = new JSONObject(visit.getJson());
                JSONArray obs = visitJson.getJSONArray("obs");
                String motherStatusCompletionStatus = getFieldValue(obs, "mother_status_module_status");
                String motherObservationModuleStatus = getFieldValue(obs, "mother_observation_module_status");
                String maternalComplicationsModuleStatus = getFieldValue(obs, "maternal_complications_module_status");
                String familyPlanningModuleStatus = getFieldValue(obs, "family_planning_module_status");
                boolean childVisitsCompletionStatus = true;
                for (Visit vis : visits) {

                    if (!vis.getVisitType().equalsIgnoreCase(LD_POST_DELIVERY_MOTHER_MANAGEMENT)) {
                        JSONObject childVisitJson = new JSONObject(vis.getJson());
                        JSONArray childVisitObs = childVisitJson.getJSONArray("obs");
                        String newbornStageFourModuleStatus = getFieldValue(childVisitObs, "newborn_stage_four_module_status");
                        assert newbornStageFourModuleStatus != null;
                        if (newbornStageFourModuleStatus.equalsIgnoreCase("Partially Completed")) {
                            childVisitsCompletionStatus = false;
                        }
                    }

                }

                if (motherObservationModuleStatus != null && motherStatusCompletionStatus != null && maternalComplicationsModuleStatus != null) {
                    if (motherStatusCompletionStatus.equalsIgnoreCase("Fully Completed") &&
                            motherObservationModuleStatus.equalsIgnoreCase("Fully Completed") &&
                            maternalComplicationsModuleStatus.equalsIgnoreCase("Fully Completed") &&
                            familyPlanningModuleStatus.equalsIgnoreCase("Fully Completed") && childVisitsCompletionStatus) {
                        ldVisits.add(visit);
                    }
                }

            } else if (visit.getVisitType().contains("LND") &&
                    visit.getVisitType().contains("Newborn") &&
                    (visit.getVisitType().contains("1st") || visit.getVisitType().contains("2nd") || visit.getVisitType().contains("3rd") || visit.getVisitType().contains("th"))) {

                JSONObject visitJson = new JSONObject(visit.getJson());
                JSONArray obs = visitJson.getJSONArray("obs");
                String newbornStageFourModuleStatus = getFieldValue(obs, "newborn_stage_four_module_status");

                if (newbornStageFourModuleStatus != null) {
                    if (newbornStageFourModuleStatus.equalsIgnoreCase("Fully Completed")) {
                        ldVisits.add(visit);
                    }
                }


            } else {
                ldVisits.add(visit);
            }
        }

        if (ldVisits.size() > 0) {
            processVisits(ldVisits, visitRepository, visitDetailsRepository);
        }
    }

    public static boolean computeCompletionStatus(JSONArray obs, String checkString) throws JSONException {
        int size = obs.length();
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = obs.getJSONObject(i);
            if (jsonObject.getString("fieldCode").equalsIgnoreCase(checkString)) {
                return true;
            }
        }
        return false;
    }

    public static String getFieldValue(JSONArray obs, String checkString) throws JSONException {
        int size = obs.length();
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = obs.getJSONObject(i);
            if (jsonObject.getString("fieldCode").equalsIgnoreCase(checkString)) {
                JSONArray values = jsonObject.getJSONArray("values");
                return values.getString(0);
            }
        }
        return null;
    }

    public static boolean shouldProcessPartographVisit(Visit visit) throws JSONException {
        JSONObject visitJson = new JSONObject(visit.getJson());
        JSONArray obs = visitJson.getJSONArray("obs");

        boolean hasPartographDate = computeCompletionStatus(obs, "partograph_date");
        boolean hasPartographTime = computeCompletionStatus(obs, "partograph_time");

        boolean hasRespiratoryRate = computeCompletionStatus(obs, "respiratory_rate");
        boolean hasPulseRate = computeCompletionStatus(obs, "pulse_rate");
        boolean hasAmnioticFluid = computeCompletionStatus(obs, "amnioticFluid");
        boolean hasMolding = computeCompletionStatus(obs, "moulding");
        boolean hasFetalHeartRate = computeCompletionStatus(obs, "fetal_heart_rate");
        boolean hasTemperature = computeCompletionStatus(obs, "temperature");
        boolean hasSystolic = computeCompletionStatus(obs, "systolic");
        boolean hasDiastolic = computeCompletionStatus(obs, "diastolic");
        boolean hasUrineProtein = computeCompletionStatus(obs, "urine_protein");
        boolean hasUrineAcetone = computeCompletionStatus(obs, "urine_acetone");
        boolean hasUrineVolume = computeCompletionStatus(obs, "urine_volume");
        boolean hasCervixDilation = computeCompletionStatus(obs, "cervix_dilation");
        boolean hasDescentPresentingPart = computeCompletionStatus(obs, "descent_presenting_part");
        boolean hasContractionEveryHalfHourFrequency = computeCompletionStatus(obs, "contraction_every_half_hour_frequency");
        boolean hasContractionEveryHalfAnHour = computeCompletionStatus(obs, "contraction_every_half_hour_time");

        return hasPartographDate && hasPartographTime && (hasRespiratoryRate || hasPulseRate || hasAmnioticFluid || hasFetalHeartRate || hasTemperature || hasSystolic || hasDiastolic || hasUrineProtein || hasUrineAcetone || hasUrineVolume || hasCervixDilation || hasDescentPresentingPart || hasContractionEveryHalfHourFrequency || hasContractionEveryHalfAnHour || hasMolding);
    }

}
