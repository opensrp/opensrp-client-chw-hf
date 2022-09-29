package org.smartregister.chw.hf.interactor;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.kvp.KvpHealthEducationHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpIecSbccActionHelper;
import org.smartregister.chw.kvp.contract.BaseKvpVisitContract;
import org.smartregister.chw.kvp.domain.VisitDetail;
import org.smartregister.chw.kvp.interactor.BaseKvpVisitInteractor;
import org.smartregister.chw.kvp.model.BaseKvpVisitAction;
import org.smartregister.chw.kvp.util.Constants;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class KvpBehavioralServiceInteractor extends BaseKvpVisitInteractor {

    String visitType;

    public KvpBehavioralServiceInteractor(String visitType) {
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
                evaluateIecSbcc(details);
                evaluateHealthEducation(details);
            } catch (BaseKvpVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateIecSbcc(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        KvpIecSbccActionHelper actionHelper = new KvpIecSbccActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_iec_sbcc))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BEHAVIORAL_SERVICE_FORMS.KVP_IEC_SBCC)
                .build();

        actionList.put(context.getString(R.string.kvp_iec_sbcc), action);
    }

    private void evaluateHealthEducation(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        KvpHealthEducationHelper actionHelper = new KvpHealthEducationHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_health_education))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BEHAVIORAL_SERVICE_FORMS.KVP_HEALTH_EDUCATION)
                .build();

        actionList.put(context.getString(R.string.kvp_health_education), action);
    }

    @Override
    protected String getEncounterType() {
        return Constants.EVENT_TYPE.KVP_BEHAVIORAL_SERVICE_VISIT;
    }

    @Override
    protected String getTableName() {
        return Constants.TABLES.KVP_FOLLOW_UP;
    }

}
