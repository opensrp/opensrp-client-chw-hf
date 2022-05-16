package org.smartregister.chw.hf.utils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.repository.VisitDetailsRepository;
import org.smartregister.chw.ld.repository.VisitRepository;
import org.smartregister.chw.ld.util.Constants;
import org.smartregister.chw.ld.util.VisitUtils;
import org.smartregister.chw.hf.utils.Constants.Events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
                boolean isUrineDone = computeCompletionStatus(obs, "urine");
                boolean isFundalHeightDone = computeCompletionStatus(obs, "fundal_height");
                boolean isPresentationDone = computeCompletionStatus(obs, "presentation");
                boolean isContractionInTenMinutesDone = computeCompletionStatus(obs, "contraction_in_ten_minutes");
                boolean isFetalHeartRateDone = computeCompletionStatus(obs, "fetal_heart_rate");

                boolean isVaginalExamDateDone = computeCompletionStatus(obs, "vaginal_exam_date");
                boolean isVaginalExamTimeDone = computeCompletionStatus(obs, "vaginal_exam_time");
                boolean isCervixStateDone = computeCompletionStatus(obs, "cervix_state");
                boolean isCervixDilationDone = computeCompletionStatus(obs, "cervix_dilation");
                boolean isPresentingPartDone = computeCompletionStatus(obs, "presenting_part");
                boolean isOcciputPositionDone = computeCompletionStatus(obs, "occiput_position");
                boolean isMouldingDone = computeCompletionStatus(obs, "moulding");
                boolean isStationDone = computeCompletionStatus(obs, "station");
                boolean isDecisionDone = computeCompletionStatus(obs, "decision");

                if (isGeneralConditionDone &&
                        isPulseRateDone &&
                        isRespiratoryRateDone &&
                        isTemperatureDone &&
                        isSystolicDone &&
                        isDiastolicDone &&
                        isUrineDone &&
                        isFundalHeightDone &&
                        isPresentationDone &&
                        isContractionInTenMinutesDone &&
                        isFetalHeartRateDone &&
                        isVaginalExamDateDone &&
                        isVaginalExamTimeDone &&
                        isCervixStateDone &&
                        isCervixDilationDone &&
                        isPresentingPartDone &&
                        isOcciputPositionDone &&
                        isMouldingDone &&
                        isStationDone &&
                        isDecisionDone) {
                    ldVisits.add(visit);
                }
            } else if (visit.getVisitType().equalsIgnoreCase(Events.LD_PARTOGRAPHY) && isPartograph) {
                if(shouldProcessPartographVisit(visit)) {
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
        boolean hasUrine = computeCompletionStatus(obs, "urine");
        boolean hasCervixDilation = computeCompletionStatus(obs, "cervix_dilation");
        boolean hasDescentPresentingPart = computeCompletionStatus(obs, "descent_presenting_part");
        boolean hasContractionEveryHalfHourFrequency = computeCompletionStatus(obs, "contraction_every_half_hour_frequency");
        boolean hasContractionEveryHalfAnHour = computeCompletionStatus(obs, "contraction_every_half_hour_time");

        return hasPartographDate && hasPartographTime && (hasRespiratoryRate || hasPulseRate || hasAmnioticFluid || hasFetalHeartRate || hasTemperature || hasSystolic || hasDiastolic || hasUrine || hasCervixDilation || hasDescentPresentingPart || hasContractionEveryHalfHourFrequency || hasContractionEveryHalfAnHour || hasMolding);
    }

}
