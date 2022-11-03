package org.smartregister.chw.hf.interactor;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.AppExecutors;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.prep.PrEPInitiationActionHelper;
import org.smartregister.chw.hf.actionhelper.prep.PrEPOtherServicesActionHelper;
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

        PrEPVisitTypeActionHelper actionHelper = new PrEPVisitTypeActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.prep_visit_type))
                .withOptional(false)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.PrEP_FOLLOWUP_FORMS.VISIT_TYPE)
                .build();

        actionList.put(context.getString(R.string.prep_visit_type), action);
    }

    private void evaluatePrEPScreening(String visitType, Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {
        JSONObject prepScreening = FormUtils.getFormUtils().getFormJson(Constants.PrEP_FOLLOWUP_FORMS.SCREENING);
        try {
            prepScreening.put(org.smartregister.util.JsonFormUtils.ENTITY_ID, memberObject.getBaseEntityId());
            JSONObject global = prepScreening.getJSONObject("global");
            global.put("visit_type", visitType);
        } catch (JSONException e) {
            Timber.e(e);
        }

        PrEPScreeningActionHelper actionHelper = new PrEPScreeningActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.prep_screening))
                .withOptional(true)
                .withDetails(details)
                .withJsonPayload(prepScreening.toString())
                .withHelper(actionHelper)
                .withFormName(Constants.PrEP_FOLLOWUP_FORMS.SCREENING)
                .build();

        actionList.put(context.getString(R.string.prep_screening), action);
    }

    private void evaluatePrEPInitiation(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        PrEPInitiationActionHelper actionHelper = new PrEPInitiationActionHelper(memberObject.getBaseEntityId());
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.prep_initiation))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
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
                    evaluatePrEPScreening(visit_type, details);
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
        @Override
        public String postProcess(String s) {
            if (should_initiate.equalsIgnoreCase("yes")) {
                try {
                    evaluatePrEPInitiation(details);
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
