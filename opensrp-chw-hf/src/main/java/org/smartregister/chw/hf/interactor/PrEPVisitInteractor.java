package org.smartregister.chw.hf.interactor;

import static org.smartregister.client.utils.constants.JsonFormConstants.FIELDS;
import static org.smartregister.client.utils.constants.JsonFormConstants.STEP1;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.AppExecutors;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.prep.PrEPInitiationActionHelper;
import org.smartregister.chw.hf.actionhelper.prep.PrEPOtherServicesActionHelper;
import org.smartregister.chw.hf.dao.HfKvpDao;
import org.smartregister.chw.kvp.contract.BaseKvpVisitContract;
import org.smartregister.chw.kvp.domain.VisitDetail;
import org.smartregister.chw.kvp.interactor.BaseKvpVisitInteractor;
import org.smartregister.chw.kvp.model.BaseKvpVisitAction;
import org.smartregister.chw.kvp.util.Constants;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PrEPVisitInteractor extends BaseKvpVisitInteractor {

    String visitType;
    protected BaseKvpVisitContract.InteractorCallBack callBack;

    public PrEPVisitInteractor(String visitType) {
        this.visitType = visitType;
    }

    @Override
    protected String getCurrentVisitType() {
        if (StringUtils.isNotBlank(visitType)) {
            return visitType;
        }
        return super.getCurrentVisitType();
    }

    @Override
    protected void populateActionList(BaseKvpVisitContract.InteractorCallBack callBack) {
        this.callBack = callBack;
        final Runnable runnable = () -> {
            try {
                evaluateVisitType(details);

            } catch (BaseKvpVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateVisitType(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {
        JSONObject prepVisitType = FormUtils.getFormUtils().getFormJson(Constants.PrEP_FOLLOWUP_FORMS.VISIT_TYPE);

        try {
            if (HfKvpDao.hasPrepFollowup(memberObject.getBaseEntityId())) {
                JSONArray fields = prepVisitType.getJSONObject(STEP1).getJSONArray(FIELDS);
                JSONObject visitType = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "visit_type");
                visitType.getJSONArray("options").remove(2);
                visitType.getJSONArray("options").remove(0);
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        PrEPVisitTypeActionHelper actionHelper = new PrEPVisitTypeActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.prep_visit_type))
                .withOptional(false)
                .withDetails(details)
                .withJsonPayload(prepVisitType.toString())
                .withHelper(actionHelper)
                .withFormName(Constants.PrEP_FOLLOWUP_FORMS.VISIT_TYPE)
                .build();

        actionList.put(context.getString(R.string.prep_visit_type), action);
    }

    private void evaluatePrEPScreening(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {
        PrEPScreeningActionHelper actionHelper = new PrEPScreeningActionHelper(memberObject.getBaseEntityId());
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.prep_screening))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.PrEP_FOLLOWUP_FORMS.SCREENING)
                .build();

        actionList.put(context.getString(R.string.prep_screening), action);
    }

    private void evaluatePrEPInitiation(Map<String, List<VisitDetail>> details, String prepVisitType) throws BaseKvpVisitAction.ValidationException {
        JSONObject prepInitiation = FormUtils.getFormUtils().getFormJson(Constants.PrEP_FOLLOWUP_FORMS.INITIATION);

        try {
            JSONArray fields = prepInitiation.getJSONObject(STEP1).getJSONArray(FIELDS);
            JSONObject prepStatus = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "prep_status");
            if (prepVisitType != null && prepVisitType.equalsIgnoreCase("new_client")) {
                prepStatus.getJSONArray("options").remove(4);
                prepStatus.getJSONArray("options").remove(2);
                prepStatus.getJSONArray("options").remove(1);
            } else if (HfKvpDao.isPrEPInitiated(memberObject.getBaseEntityId())) {
                prepStatus.getJSONArray("options").remove(3);
                prepStatus.getJSONArray("options").remove(0);
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        PrEPInitiationActionHelper actionHelper = new PrEPInitiationActionHelper(memberObject.getBaseEntityId());
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.prep_initiation))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withJsonPayload(prepInitiation.toString())
                .withFormName(Constants.PrEP_FOLLOWUP_FORMS.INITIATION)
                .build();

        actionList.put(context.getString(R.string.prep_initiation), action);
    }

    private void evaluateOtherServices(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        PrEPOtherServicesActionHelper actionHelper = new PrEPOtherServicesActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.other_services))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.PrEP_FOLLOWUP_FORMS.OTHER_SERVICES)
                .build();

        actionList.put(context.getString(R.string.other_services), action);
    }

    @Override
    protected String getEncounterType() {
        return Constants.EVENT_TYPE.PrEP_FOLLOWUP_VISIT;
    }

    @Override
    protected String getTableName() {
        return Constants.TABLES.PrEP_FOLLOWUP;
    }

    private class PrEPVisitTypeActionHelper extends org.smartregister.chw.hf.actionhelper.prep.PrEPVisitTypeActionHelper {
        @Override
        public String postProcess(String s) {
            if (StringUtils.isNotBlank(visit_type)) {
                try {
                    evaluatePrEPScreening(details);
                    evaluatePrEPInitiation(details, visit_type);
                } catch (BaseKvpVisitAction.ValidationException e) {
                    e.printStackTrace();
                }
            } else {
                actionList.remove(context.getString(R.string.prep_screening));
                actionList.remove(context.getString(R.string.prep_initiation));
                actionList.remove(context.getString(R.string.other_services));
            }
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            return super.postProcess(s);
        }

    }

    private class PrEPScreeningActionHelper extends org.smartregister.chw.hf.actionhelper.prep.PrEPScreeningActionHelper {
        public PrEPScreeningActionHelper(String baseEntityId) {
            super(baseEntityId);
        }

        @Override
        public String postProcess(String s) {
            if (should_initiate.equalsIgnoreCase("yes")) {
                try {
                    if (!actionList.containsKey(context.getString(R.string.prep_initiation)))
                        evaluatePrEPInitiation(details, null);
                    evaluateOtherServices(details);
                } catch (BaseKvpVisitAction.ValidationException e) {
                    e.printStackTrace();
                }
            } else {
                actionList.remove(context.getString(R.string.prep_initiation));
                actionList.remove(context.getString(R.string.other_services));
            }
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            return super.postProcess(s);
        }
    }
}
