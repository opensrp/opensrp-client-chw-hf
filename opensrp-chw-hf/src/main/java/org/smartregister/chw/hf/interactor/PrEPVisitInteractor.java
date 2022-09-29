package org.smartregister.chw.hf.interactor;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.prep.PrEPInitiationActionHelper;
import org.smartregister.chw.hf.actionhelper.prep.PrEPOtherServicesActionHelper;
import org.smartregister.chw.hf.actionhelper.prep.PrEPScreeningActionHelper;
import org.smartregister.chw.hf.actionhelper.prep.PrEPVisitTypeActionHelper;
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
        final Runnable runnable = () -> {
            try {
                evaluateVisitType(details);
                evaluatePrEPScreening(details);
                evaluatePrEPInitiation(details);
                evaluateOtherServices(details);

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

    private void evaluatePrEPScreening(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        PrEPScreeningActionHelper actionHelper = new PrEPScreeningActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.prep_screening))
                .withOptional(false)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.PrEP_FOLLOWUP_FORMS.SCREENING)
                .build();

        actionList.put(context.getString(R.string.prep_screening), action);
    }

    private void evaluatePrEPInitiation(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        PrEPInitiationActionHelper actionHelper = new PrEPInitiationActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.prep_initiation))
                .withOptional(false)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.PrEP_FOLLOWUP_FORMS.INITIATION)
                .build();

        actionList.put(context.getString(R.string.prep_initiation), action);
    }

    private void evaluateOtherServices(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        PrEPOtherServicesActionHelper actionHelper = new PrEPOtherServicesActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.other_services))
                .withOptional(false)
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
}
