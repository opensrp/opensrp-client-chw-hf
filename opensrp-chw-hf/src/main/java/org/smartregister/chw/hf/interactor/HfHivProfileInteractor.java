package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.interactor.CoreHivProfileInteractor;
import org.smartregister.chw.core.repository.ChwTaskRepository;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.contract.HivProfileContract;
import org.smartregister.chw.hf.dao.HivFollowupFeedbackDao;
import org.smartregister.chw.hf.model.HivTbFollowupFeedbackDetailsModel;
import org.smartregister.chw.hf.model.HivTbReferralTasksAndFollowupFeedbackModel;
import org.smartregister.chw.hiv.contract.BaseHivProfileContract;
import org.smartregister.chw.hiv.domain.HivMemberObject;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.domain.Task;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.Utils;
import org.smartregister.opd.contract.OpdRegisterActivityContract;
import org.smartregister.opd.pojo.OpdEventClient;
import org.smartregister.opd.pojo.RegisterParams;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.TaskRepository;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class HfHivProfileInteractor extends CoreHivProfileInteractor implements HivProfileContract.Interactor {

    private HfAllClientsRegisterInteractor hfAllClientsRegisterInteractor;

    public HfHivProfileInteractor(Context context) {
        super(context);
        hfAllClientsRegisterInteractor = new HfAllClientsRegisterInteractor();
    }

    @Override
    public void getReferralTasks(String planId, String baseEntityId, HivProfileContract.InteractorCallback callback) {
        List<HivTbReferralTasksAndFollowupFeedbackModel> tasksAndFollowupFeedbackModels = new ArrayList<>();
        TaskRepository taskRepository = CoreChwApplication.getInstance().getTaskRepository();
        Set<Task> taskList = ((ChwTaskRepository) taskRepository).getReferralTasksForClientByStatus(planId, baseEntityId, CoreConstants.BUSINESS_STATUS.REFERRED);

        for (Task task : taskList) {
            HivTbReferralTasksAndFollowupFeedbackModel tasksAndFollowupFeedbackModel = new HivTbReferralTasksAndFollowupFeedbackModel();
            tasksAndFollowupFeedbackModel.setTask(task);
            tasksAndFollowupFeedbackModel.setType("TASK");
            tasksAndFollowupFeedbackModels.add(tasksAndFollowupFeedbackModel);
        }

        List<HivTbFollowupFeedbackDetailsModel> followupFeedbackList = HivFollowupFeedbackDao.getHivFollowupFeedback(baseEntityId);

        for (HivTbFollowupFeedbackDetailsModel followupFeedbackDetailsModel : followupFeedbackList) {
            HivTbReferralTasksAndFollowupFeedbackModel tasksAndFollowupFeedbackModel = new HivTbReferralTasksAndFollowupFeedbackModel();
            tasksAndFollowupFeedbackModel.setFollowupFeedbackDetailsModel(followupFeedbackDetailsModel);
            tasksAndFollowupFeedbackModel.setType("FOLLOWUP_FEEDBACK");
            tasksAndFollowupFeedbackModels.add(tasksAndFollowupFeedbackModel);
        }


        callback.updateReferralTasksAndFollowupFeedback(tasksAndFollowupFeedbackModels);
    }

    @Override
    public void getNextUniqueId(final Triple<String, String, String> triple, final OpdRegisterActivityContract.InteractorCallBack callBack) {
        hfAllClientsRegisterInteractor.getNextUniqueId(triple, callBack);
    }

    @Override
    public void saveRegistration(final List<OpdEventClient> opdEventClientList, final String jsonString,
                                 final RegisterParams registerParams, final HivMemberObject hivMemberObject, final OpdRegisterActivityContract.InteractorCallBack callBack) {
        for (OpdEventClient opdEventClient : opdEventClientList) {
            if (!opdEventClient.getClient().getIdentifier("opensrp_id").contains("family")) {
                saveRegisterHivIndexEvent(hivMemberObject.getBaseEntityId(), opdEventClient.getClient().getBaseEntityId(), opdEventClient.getEvent().getLocationId());
            }
        }
        hfAllClientsRegisterInteractor.saveRegistration(opdEventClientList, jsonString, registerParams, callBack);
    }


    @Override
    public void updateProfileHivStatusInfo(HivMemberObject memberObject, BaseHivProfileContract.InteractorCallback callback) {
        //overriding updateProfileHivStatusInfo
    }

    private void saveRegisterHivIndexEvent(String hivClientBaseEntityId, String indexClientBaseEntityId, String locationId) {
        try {
            AllSharedPreferences sharedPreferences = Utils.getAllSharedPreferences();
            ECSyncHelper syncHelper = FamilyLibrary.getInstance().getEcSyncHelper();
            Event baseEvent = (Event) new Event()
                    .withBaseEntityId(hivClientBaseEntityId)
                    .withEventDate(new Date())
                    .withEventType(CoreConstants.EventType.REGISTER_HIV_INDEX)
                    .withFormSubmissionId(JsonFormUtils.generateRandomUUIDString())
                    .withEntityType(CoreConstants.TABLE_NAME.HIV_INDEX)
                    .withProviderId(sharedPreferences.fetchRegisteredANM())
                    .withLocationId(locationId)
                    .withTeamId(sharedPreferences.fetchDefaultTeamId(sharedPreferences.fetchRegisteredANM()))
                    .withTeam(sharedPreferences.fetchDefaultTeam(sharedPreferences.fetchRegisteredANM()))
                    .withClientDatabaseVersion(BuildConfig.DATABASE_VERSION)
                    .withClientApplicationVersion(BuildConfig.VERSION_CODE)
                    .withDateCreated(new Date());

            baseEvent.addObs((new Obs()).withFormSubmissionField(CoreConstants.FORM_CONSTANTS.FORM_SUBMISSION_FIELD.INDEX_CLIENT_BASE_ENTITY_ID).withValue(indexClientBaseEntityId)
                    .withFieldCode(CoreConstants.FORM_CONSTANTS.FORM_SUBMISSION_FIELD.INDEX_CLIENT_BASE_ENTITY_ID).withFieldType("formsubmissionField").withFieldDataType("text").withParentCode("").withHumanReadableValues(new ArrayList<>()));


            org.smartregister.chw.hf.utils.JsonFormUtils.tagSyncMetadata(Utils.context().allSharedPreferences(), baseEvent);// tag docs

            //setting the location uuid of the referral initiator so that to allow the event to sync back to the chw app since it sync data by location.
            baseEvent.setLocationId(locationId);

            JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
            syncHelper.addEvent(hivClientBaseEntityId, eventJson);
            long lastSyncTimeStamp = HealthFacilityApplication.getInstance().getContext().allSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
            HealthFacilityApplication.getClientProcessor(HealthFacilityApplication.getInstance().getContext().applicationContext()).processClient(syncHelper.getEvents(lastSyncDate, BaseRepository.TYPE_Unprocessed));
            HealthFacilityApplication.getInstance().getContext().allSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
        } catch (Exception e) {
            Timber.e(e, "HfHivProfileInteractor --> saveRegisterIndexClientEvent");
        }

    }
}
