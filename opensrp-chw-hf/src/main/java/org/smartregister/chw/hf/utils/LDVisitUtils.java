package org.smartregister.chw.hf.utils;

import static org.smartregister.chw.hf.interactor.LDPostDeliveryManagementMotherActivityInteractor.ordinal;
import static org.smartregister.chw.hf.interactor.LDVisitInteractor.hbTestMoreThanTwoWeeksAgo;
import static org.smartregister.chw.hf.interactor.LDVisitInteractor.malariaTestConductedDuringRegistration;
import static org.smartregister.chw.hf.interactor.LDVisitInteractor.syphilisTestConductedDuringRegistration;
import static org.smartregister.chw.hf.utils.Constants.Events.LD_POST_DELIVERY_MOTHER_MANAGEMENT;
import static org.smartregister.chw.hf.utils.Constants.HIV_STATUS.POSITIVE;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.actionhelper.LDGeneralExaminationActionHelper;
import org.smartregister.chw.hf.interactor.LDVisitInteractor;
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

                boolean isFundalHeightDone = true;
                if (!LDGeneralExaminationActionHelper.fundalHeightCaptured(baseEntityId))
                    isFundalHeightDone = computeCompletionStatus(obs, "fundal_height");

                boolean isPresentationDone = true;
                if (!LDGeneralExaminationActionHelper.featalLieCaptured(baseEntityId))
                    isPresentationDone = getFieldValue(obs, "lie").equalsIgnoreCase("transverse") || computeCompletionStatus(obs, "presentation");

                boolean isContractionInTenMinutesDone = computeCompletionStatus(obs, "contraction_in_ten_minutes");
                boolean isFetalHeartRateDone = computeCompletionStatus(obs, "fetal_heart_rate");
                boolean isVaginalExamDateDone = computeCompletionStatus(obs, "vaginal_exam_date");
                boolean isVaginalExamTimeDone = computeCompletionStatus(obs, "vaginal_exam_time");
                boolean isCervixStateDone = computeCompletionStatus(obs, "cervix_state");
                boolean isCervixDilationDone = computeCompletionStatus(obs, "cervix_dilation");
                boolean isPresentingPartDone = true;
                if (LDDao.getPresentingPart(baseEntityId) == null)
                    isPresentingPartDone = computeCompletionStatus(obs, "presenting_part");

                boolean isMouldingDone;
                String presentingPart = getFieldValue(obs, "presenting_part");
                if (presentingPart != null && !presentingPart.equalsIgnoreCase("breech") && !presentingPart.equalsIgnoreCase("shoulder")) {
                    isMouldingDone = computeCompletionStatus(obs, "moulding");
                } else {
                    isMouldingDone = true;
                }
                boolean isStationDone = computeCompletionStatus(obs, "station");
                boolean isDecisionDone = computeCompletionStatus(obs, "decision");

                boolean hivActionDone = false;

                if (LDDao.getHivStatus(baseEntityId) == null || (!Objects.equals(LDDao.getHivStatus(baseEntityId), POSITIVE) && LDVisitInteractor.testDateIsThreeMonthsAgo(baseEntityId))) {
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

                boolean malariaActionDone = true;
                if (!malariaTestConductedDuringRegistration(baseEntityId)) {
                    String malariaTest = getFieldValue(obs, "malaria");
                    if (malariaTest == null || malariaTest.isEmpty())
                        malariaActionDone = false;
                }

                boolean syphilisActionDone = true;
                if (!syphilisTestConductedDuringRegistration(baseEntityId)) {
                    String syphilisTest = getFieldValue(obs, "syphilis");
                    if (syphilisTest == null || syphilisTest.isEmpty())
                        syphilisActionDone = false;
                }

                boolean hbActionDone = true;
                if (hbTestMoreThanTwoWeeksAgo(baseEntityId)) {
                    String hbTest = getFieldValue(obs, "hb_test_conducted");
                    if (hbTest == null || hbTest.isEmpty())
                        hbActionDone = false;
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
                        syphilisActionDone &&
                        malariaActionDone &&
                        hbActionDone &&
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
                boolean isEclampsiaManagementDone = computeCompletionStatus(obs, "has_signs_of_eclampsia");

                if (hasPlacentaAndMembraneExpelled && isUterotonicDone && isMassageOfUterusAfterDeliveryDone && isEclampsiaManagementDone) {
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

                String number_children_string = getFieldValue(obs, "number_of_children_born");
                int numberOfChildrenBorn = 0;

                if (number_children_string != null) {
                    numberOfChildrenBorn = Integer.parseInt(number_children_string);
                }
                for (int i = 0; i < numberOfChildrenBorn; i++) {
                    // Get visit details for each individual child
                    Visit immediateNewBornCareVisit = LDLibrary.getInstance().visitRepository().getVisitsByParentVisitId(visit.getVisitId(), "LND " + ordinal(i + 1) + " Newborn").get(0);

                    if (immediateNewBornCareVisit.getVisitType().contains("LND") &&
                            immediateNewBornCareVisit.getVisitType().contains("Newborn") &&
                            (immediateNewBornCareVisit.getVisitType().contains("1st") || immediateNewBornCareVisit.getVisitType().contains("2nd") || immediateNewBornCareVisit.getVisitType().contains("3rd") || immediateNewBornCareVisit.getVisitType().contains("th"))) {

                        JSONObject immediateNewBornCareVisitJson = new JSONObject(immediateNewBornCareVisit.getJson());
                        JSONArray immediateNewBornCareObs = immediateNewBornCareVisitJson.getJSONArray("obs");
                        String newbornStageFourModuleStatus = getFieldValue(immediateNewBornCareObs, "newborn_stage_four_module_status");

                        if (newbornStageFourModuleStatus != null) {
                            if (newbornStageFourModuleStatus.equalsIgnoreCase("Fully Completed")) {
                                ldVisits.add(immediateNewBornCareVisit);
                            } else {
                                childVisitsCompletionStatus = false;
                            }
                        }
                    }

                }

                if (isDeceased(obs) &&
                        motherStatusCompletionStatus != null &&
                        maternalComplicationsModuleStatus != null &&
                        motherStatusCompletionStatus.equalsIgnoreCase("Fully Completed") &&
                        maternalComplicationsModuleStatus.equalsIgnoreCase("Fully Completed") &&
                        childVisitsCompletionStatus) {
                    ldVisits.add(visit);
                } else if (motherStatusCompletionStatus != null &&
                        maternalComplicationsModuleStatus != null &&
                        motherObservationModuleStatus != null &&
                        familyPlanningModuleStatus != null &&
                        motherStatusCompletionStatus.equalsIgnoreCase("Fully Completed") &&
                        motherObservationModuleStatus.equalsIgnoreCase("Fully Completed") &&
                        maternalComplicationsModuleStatus.equalsIgnoreCase("Fully Completed") &&
                        familyPlanningModuleStatus.equalsIgnoreCase("Fully Completed") && childVisitsCompletionStatus) {
                    ldVisits.add(visit);
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

    public static boolean isDeceased(JSONArray obs) throws JSONException {
        int size = obs.length();
        for (int i = 0; i < size; i++) {
            JSONObject checkObj = obs.getJSONObject(i);
            if (checkObj.getString("fieldCode").equalsIgnoreCase("status")) {
                JSONArray values = checkObj.getJSONArray("values");
                return values.get(0).equals("deceased") || values.get(0).equals("died");
            }
        }
        return false;
    }

}
