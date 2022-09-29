package org.smartregister.chw.hf.interactor;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.kvp.KvpCervicalCancerScreeningActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpClientStatusActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpCondomProvisionActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpFamilyPlanningActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpHepatitisActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpHtsActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpMatActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpPrepPepActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpStiScreeningActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpTbScreeningActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpVmmcActionHelper;
import org.smartregister.chw.kvp.contract.BaseKvpVisitContract;
import org.smartregister.chw.kvp.domain.VisitDetail;
import org.smartregister.chw.kvp.interactor.BaseKvpVisitInteractor;
import org.smartregister.chw.kvp.model.BaseKvpVisitAction;
import org.smartregister.chw.kvp.util.Constants;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class KvpBioMedicalServiceInteractor extends BaseKvpVisitInteractor {

    private String visitType;

    public KvpBioMedicalServiceInteractor(String visitType) {
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
                evaluateClientStatus(details);
                evaluateHts(details);
                evaluatePrepPep(details);
                evaluateCondomProvision(details);
                evaluateFamilyPlanning(details);
                evaluateTbScreening(details);
                evaluateStiScreening(details);
                evaluateHepatitis(details);
                evaluateVmmc(details);
                evaluateCervicalScreening(details);
                evaluateMat(details);
            } catch (BaseKvpVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateClientStatus(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        KvpClientStatusActionHelper actionHelper = new KvpClientStatusActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_client_status))
                .withOptional(false)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_CLIENT_STATUS)
                .build();

        actionList.put(context.getString(R.string.kvp_client_status), action);
    }

    private void evaluateHts(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        KvpHtsActionHelper actionHelper = new KvpHtsActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_hts))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_HTS)
                .build();

        actionList.put(context.getString(R.string.kvp_hts), action);
    }

    private void evaluatePrepPep(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        KvpPrepPepActionHelper actionHelper = new KvpPrepPepActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_prep_and_pep))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_PrEP_PEP)
                .build();

        actionList.put(context.getString(R.string.kvp_prep_and_pep), action);
    }

    private void evaluateCondomProvision(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        KvpCondomProvisionActionHelper actionHelper = new KvpCondomProvisionActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_condom_provision))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_CONDOM_PROVISION)
                .build();

        actionList.put(context.getString(R.string.kvp_condom_provision), action);
    }

    private void evaluateFamilyPlanning(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        KvpFamilyPlanningActionHelper actionHelper = new KvpFamilyPlanningActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_family_planning))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_FAMILY_PLANNING_SERVICES)
                .build();

        actionList.put(context.getString(R.string.kvp_family_planning), action);
    }

    private void evaluateTbScreening(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        KvpTbScreeningActionHelper actionHelper = new KvpTbScreeningActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_tb_screening))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_TB_INVESTIGATION)
                .build();

        actionList.put(context.getString(R.string.kvp_tb_screening), action);
    }

    private void evaluateStiScreening(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        KvpStiScreeningActionHelper actionHelper = new KvpStiScreeningActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_sti_screening))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_STI_SCREENING)
                .build();

        actionList.put(context.getString(R.string.kvp_sti_screening), action);
    }

    private void evaluateHepatitis(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        KvpHepatitisActionHelper actionHelper = new KvpHepatitisActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_heptitis_screening))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_HEPATITIS)
                .build();

        actionList.put(context.getString(R.string.kvp_heptitis_screening), action);
    }

    private void evaluateVmmc(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        KvpVmmcActionHelper actionHelper = new KvpVmmcActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_vmmc))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_VMMC_SERVICES)
                .build();

        actionList.put(context.getString(R.string.kvp_vmmc), action);
    }

    private void evaluateCervicalScreening(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        KvpCervicalCancerScreeningActionHelper actionHelper = new KvpCervicalCancerScreeningActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_cervical_screening))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_CERVICAL_CANCER_SCREENING)
                .build();

        actionList.put(context.getString(R.string.kvp_cervical_screening), action);
    }

    private void evaluateMat(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        KvpMatActionHelper actionHelper = new KvpMatActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_mat))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_MAT)
                .build();

        actionList.put(context.getString(R.string.kvp_mat), action);
    }

    @Override
    protected String getEncounterType() {
        return Constants.EVENT_TYPE.KVP_BIO_MEDICAL_SERVICE_VISIT;
    }

    @Override
    protected String getTableName() {
        return Constants.TABLES.KVP_FOLLOW_UP;
    }
}
