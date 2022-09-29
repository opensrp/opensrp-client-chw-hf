package org.smartregister.chw.hf.interactor;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.kvp.KvpGbvAnalysisActionHelper;
import org.smartregister.chw.kvp.contract.BaseKvpVisitContract;
import org.smartregister.chw.kvp.domain.VisitDetail;
import org.smartregister.chw.kvp.interactor.BaseKvpVisitInteractor;
import org.smartregister.chw.kvp.model.BaseKvpVisitAction;
import org.smartregister.chw.kvp.util.Constants;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class KvpStructuralServiceInteractor extends BaseKvpVisitInteractor {

    String visitType;

    public KvpStructuralServiceInteractor(String visitType) {
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
                evaluateGbvAnalysis(details);
            } catch (BaseKvpVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateGbvAnalysis(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        KvpGbvAnalysisActionHelper actionHelper = new KvpGbvAnalysisActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_gbv_analysis))
                .withOptional(false)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_STRUCTURAL_AND_OTHER_SERVICES_FORMS.KVP_GBV_ANALYSIS)
                .build();

        actionList.put(context.getString(R.string.kvp_gbv_analysis), action);
    }

    @Override
    protected String getEncounterType() {
        return Constants.EVENT_TYPE.KVP_STRUCTURAL_SERVICE_VISIT;
    }

    @Override
    protected String getTableName() {
        return Constants.TABLES.KVP_FOLLOW_UP;
    }
}
