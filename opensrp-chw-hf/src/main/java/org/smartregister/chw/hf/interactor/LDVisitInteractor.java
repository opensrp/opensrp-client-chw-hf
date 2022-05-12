package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.LDGeneralExaminationActionHelper;
import org.smartregister.chw.hf.actionhelper.LDVaginalExaminationActionHelper;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.LDVisitUtils;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.contract.BaseLDVisitContract;
import org.smartregister.chw.ld.dao.LDDao;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.interactor.BaseLDVisitInteractor;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.chw.referral.util.JsonFormConstants;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-05-06
 */
public class LDVisitInteractor extends BaseLDVisitInteractor {
    protected Context context;
    final LinkedHashMap<String, BaseLDVisitAction> actionList = new LinkedHashMap<>();
    Map<String, List<VisitDetail>> details = null;
    private MemberObject memberObject;

    @Override
    public void calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) {

        context = view.getContext();
        this.memberObject = memberObject;

        if (view.getEditMode()) {
            Visit lastVisit = LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), org.smartregister.chw.ld.util.Constants.EVENT_TYPE.LD_GENERAL_EXAMINATION);

            if (lastVisit != null) {
                details = org.smartregister.chw.ld.util.VisitUtils.getVisitGroups(LDLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        final Runnable runnable = () -> {
            // update the local database incase of manual date adjustment
            try {
                VisitUtils.processVisits(memberObject.getBaseEntityId());
            } catch (Exception e) {
                Timber.e(e);
            }

            try {

                evaluateGenExamination(details);
                evaluateVaginalExamination(details);

            } catch (BaseLDVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateVaginalExamination(Map<String, List<VisitDetail>> details) throws BaseLDVisitAction.ValidationException {
        JSONObject vaginalExaminationForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LDVisit.getLdVaginalExamination());
        if (vaginalExaminationForm != null) {
            try {
                JSONArray fields = vaginalExaminationForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                populateVaginalExaminationForm(fields, memberObject.getBaseEntityId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (LDDao.getMembraneStateDuringAdmissionToLabour(memberObject.getBaseEntityId()) != null) {
            try {
                vaginalExaminationForm.getJSONObject("global").put("membrane_status", LDDao.getMembraneStateDuringAdmissionToLabour(memberObject.getBaseEntityId()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        LDVaginalExaminationActionHelper actionHelper = new LDVaginalExaminationActionHelper(context);
        BaseLDVisitAction action = getBuilder(context.getString(R.string.lb_visit_vaginal_examination))
                .withOptional(false)
                .withDetails(details)
                .withHelper(actionHelper)
                .withJsonPayload(vaginalExaminationForm.toString())
                .withFormName(Constants.JsonForm.LDVisit.getLdVaginalExamination())
                .build();

        actionList.put(context.getString(R.string.lb_visit_vaginal_examination), action);

    }

    private void evaluateGenExamination(Map<String, List<VisitDetail>> details) throws BaseLDVisitAction.ValidationException {

        LDGeneralExaminationActionHelper actionHelper = new LDGeneralExaminationActionHelper(context);
        BaseLDVisitAction action = getBuilder(context.getString(R.string.lb_visit_general_examination))
                .withOptional(false)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.JsonForm.LDVisit.getLdGeneralExamination())
                .build();

        actionList.put(context.getString(R.string.lb_visit_general_examination), action);
    }

    @Override
    public MemberObject getMemberClient(String memberID) {

        return LDDao.getMember(memberID);
    }

    public BaseLDVisitAction.Builder getBuilder(String title) {
        return new BaseLDVisitAction.Builder(context, title);
    }

    @Override
    protected String getEncounterType() {
        return org.smartregister.chw.ld.util.Constants.EVENT_TYPE.LD_GENERAL_EXAMINATION;
    }

    @Override
    protected String getTableName() {
        return org.smartregister.chw.ld.util.Constants.TABLES.EC_LD_GENERAL_EXAMINATION;
    }

    @Override
    protected void processExternalVisits(Visit visit, Map<String, BaseLDVisitAction> externalVisits, String memberID) throws Exception {
        super.processExternalVisits(visit, externalVisits, memberID);

        /*List<Visit> visits = new ArrayList<>(1);
        visits.add(visit);
        org.smartregister.chw.ld.util.VisitUtils.processVisits(visits, LDLibrary.getInstance().visitRepository(), LDLibrary.getInstance().visitDetailsRepository());*/
        try {

            LDVisitUtils.processVisits(memberID);
        } catch (Exception e) {
            Timber.e(e);
        }

    }

    @Override
    protected void prepareEvent(Event baseEvent) {
        super.prepareEvent(baseEvent);
    }

    private void populateVaginalExaminationForm(JSONArray fields, String baseEntityId) throws JSONException {
        JSONObject vaginalExamDate = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "vaginal_exam_date");

        if (LDDao.getLabourOnsetDate(baseEntityId) != null) {
            vaginalExamDate.put("min_date", LDDao.getLabourOnsetDate(baseEntityId));
        }
    }

}
