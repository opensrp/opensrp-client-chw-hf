package org.smartregister.chw.hf.utils;

import static org.smartregister.util.Utils.getAllSharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.model.ChildModel;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hf.dao.HfPmtctDao;
import org.smartregister.chw.hf.dao.HfPncDao;
import org.smartregister.chw.pmtct.PmtctLibrary;
import org.smartregister.chw.pmtct.domain.Visit;
import org.smartregister.chw.pmtct.repository.VisitDetailsRepository;
import org.smartregister.chw.pmtct.repository.VisitRepository;
import org.smartregister.chw.pmtct.util.VisitUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.repository.AllSharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import timber.log.Timber;

public class PmtctVisitUtils extends VisitUtils {
    public static void processVisits() throws Exception {
        processVisits(PmtctLibrary.getInstance().visitRepository(), PmtctLibrary.getInstance().visitDetailsRepository());
    }

    public static void processVisits(VisitRepository visitRepository, VisitDetailsRepository visitDetailsRepository) throws Exception {

        List<Visit> visits = visitRepository.getAllUnSynced();
        List<Visit> pmtctFollowupVisits = new ArrayList<>();


        for (Visit v : visits) {
            Date updatedAtDate = new Date(v.getUpdatedAt().getTime());
            int daysDiff = TimeUtils.getElapsedDays(updatedAtDate);
            if (daysDiff > 1 && v.getVisitType().equalsIgnoreCase(org.smartregister.chw.pmtct.util.Constants.EVENT_TYPE.PMTCT_FOLLOWUP)) {
                try {
                    JSONObject jsonObject = new JSONObject(v.getJson());
                    String baseEntityId = jsonObject.getString("baseEntityId");
                    JSONArray obs = jsonObject.getJSONArray("obs");
                    List<Boolean> checks = new ArrayList<Boolean>();

                    boolean isFollowupStatusDone = computeCompletionStatus(obs, "followup_status");

                    boolean isCounsellingDone = computeCompletionStatus(obs, "is_client_counselled");
                    boolean isClinicalStagingDone = computeCompletionStatus(obs, "clinical_staging_disease");
                    boolean isTbScreeningDone = computeCompletionStatus(obs, "on_tb_treatment");
                    boolean isArvPrescriptionDone = computeCompletionStatus(obs, "prescribed_regimes");
                    boolean isNextVisitSet = computeCompletionStatus(obs, "next_facility_visit_date");

                    boolean isBaselineInvestigationComplete = computeCompletionStatus(obs, "liver_function_test_conducted") && computeCompletionStatus(obs, "renal_function_test_conducted");
                    boolean isHvlSampleCollectionComplete = computeCompletionStatus(obs, "hvl_sample_id");
                    boolean isCd4SampleCollectionComplete = computeCompletionStatus(obs, "cd4_sample_id");


                    checks.add(isFollowupStatusDone);

                    if (isContinuingWithServices(v)) {
                        checks.add(isCounsellingDone);
                        checks.add(isClinicalStagingDone);
                        checks.add(isTbScreeningDone);
                        checks.add(isArvPrescriptionDone);
                        checks.add(isNextVisitSet);

//                        if (HfPmtctDao.isEligibleForHlvTest(baseEntityId)) {
//                            checks.add(isHvlSampleCollectionComplete);
//                        }

                        if (HfPmtctDao.isEligibleForBaselineInvestigation(baseEntityId) || HfPmtctDao.isEligibleForBaselineInvestigationOnFollowupVisit(baseEntityId)) {
                            checks.add(isBaselineInvestigationComplete);
                        }

//                        if (HfPmtctDao.isEligibleForCD4Retest(baseEntityId) || HfPmtctDao.isEligibleForCD4Test(baseEntityId)) {
//                            checks.add(isCd4SampleCollectionComplete);
//                        }
                    }


                    if (!checks.contains(false)) {
                        pmtctFollowupVisits.add(v);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }

            }
        }

        if (pmtctFollowupVisits.size() > 0) {
            processVisits(pmtctFollowupVisits, visitRepository, visitDetailsRepository);
            for (Visit v : pmtctFollowupVisits) {
                if (isWomanTransferOut(v)) {
                    createHeiTransferOutUpdate(v.getJson());
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

    public static boolean isContinuingWithServices(Visit visit) {
        boolean isContinuingWithServices = false;
        try {
            JSONObject jsonObject = new JSONObject(visit.getJson());
            JSONArray obs = jsonObject.getJSONArray("obs");
            int size = obs.length();
            for (int i = 0; i < size; i++) {
                JSONObject checkObj = obs.getJSONObject(i);
                if (checkObj.getString("fieldCode").equalsIgnoreCase("followup_status")) {
                    JSONArray values = checkObj.getJSONArray("values");
                    if ((values.getString(0).equalsIgnoreCase("continuing_with_services"))) {
                        isContinuingWithServices = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return isContinuingWithServices;
    }

    public static boolean isWomanTransferOut(Visit visit) {
        boolean isWomanTransferOut = false;
        try {
            JSONObject jsonObject = new JSONObject(visit.getJson());
            JSONArray obs = jsonObject.getJSONArray("obs");
            int size = obs.length();
            for (int i = 0; i < size; i++) {
                JSONObject checkObj = obs.getJSONObject(i);
                if (checkObj.getString("fieldCode").equalsIgnoreCase("followup_status")) {
                    JSONArray values = checkObj.getJSONArray("values");
                    if (values.getString(0).equalsIgnoreCase("transfer_out")) {
                        isWomanTransferOut = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return isWomanTransferOut;
    }

    private static void createHeiTransferOutUpdate(String json) throws Exception {
        JSONObject jsonObject = new JSONObject(json);
        String motherBaseEntityId = jsonObject.getString("baseEntityId");
        List<ChildModel> childModels = HfPncDao.childrenForPncWoman(motherBaseEntityId);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        //process if the mother has children
        if (childModels.size() > 0) {
            for (ChildModel childModel : childModels) {
                String childBaseEntityId = childModel.getBaseEntityId();
                AllSharedPreferences sharedPreferences = getAllSharedPreferences();
                Event baseEvent = (Event) new Event()
                        .withBaseEntityId(childBaseEntityId)
                        .withEventDate(new Date())
                        .withEventType(Constants.Events.HEI_FOLLOWUP)
                        .withEntityType(Constants.TableName.HEI_FOLLOWUP)
                        .withFormSubmissionId(UUID.randomUUID().toString())
                        .withDateCreated(new Date());

                baseEvent.addObs(
                        (new Obs())
                                .withFormSubmissionField("followup_status")
                                .withValue("transfer_out")
                                .withFieldCode("followup_status")
                                .withFieldType("transfer_out")
                                .withFieldDataType("text")
                                .withParentCode("")
                                .withHumanReadableValues(new ArrayList<>()));
                baseEvent.addObs(
                        (new Obs())
                                .withFormSubmissionField("visit_number")
                                .withFieldCode("visit_number")
                                .withValue(String.valueOf(HeiDao.getVisitNumber(childBaseEntityId)))
                                .withFieldType("transfer_out")
                                .withFieldDataType("text")
                                .withParentCode("")
                                .withHumanReadableValues(new ArrayList<>()));
                baseEvent.addObs(
                        (new Obs())
                                .withFormSubmissionField("followup_visit_date")
                                .withFieldCode("followup_visit_date")
                                .withValue(sdf.format(new Date()))
                                .withFieldType("followup_visit_date")
                                .withFieldDataType("text")
                                .withParentCode("")
                                .withHumanReadableValues(new ArrayList<>()));
                // tag docs
                org.smartregister.chw.hf.utils.JsonFormUtils.tagSyncMetadata(sharedPreferences, baseEvent);
                NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseEvent)));
            }
        }

    }

}
