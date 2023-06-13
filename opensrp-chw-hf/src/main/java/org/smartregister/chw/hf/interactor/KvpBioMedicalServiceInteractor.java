package org.smartregister.chw.hf.interactor;

import static org.smartregister.util.JsonFormUtils.VALUE;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.AppExecutors;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.kvp.KvpCervicalCancerScreeningActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpCondomProvisionActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpFamilyPlanningActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpHepatitisActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpMatActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpPrepPepActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpStiScreeningActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpTbScreeningActionHelper;
import org.smartregister.chw.hf.actionhelper.kvp.KvpVmmcActionHelper;
import org.smartregister.chw.hf.dao.HfKvpDao;
import org.smartregister.chw.kvp.contract.BaseKvpVisitContract;
import org.smartregister.chw.kvp.dao.KvpDao;
import org.smartregister.chw.kvp.domain.VisitDetail;
import org.smartregister.chw.kvp.interactor.BaseKvpVisitInteractor;
import org.smartregister.chw.kvp.model.BaseKvpVisitAction;
import org.smartregister.chw.kvp.util.Constants;
import org.smartregister.chw.kvp.util.KvpJsonFormUtils;
import org.smartregister.chw.referral.util.JsonFormConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class KvpBioMedicalServiceInteractor extends BaseKvpVisitInteractor {

    protected BaseKvpVisitContract.InteractorCallBack callBack;
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
        this.callBack = callBack;
        final Runnable runnable = () -> {
            try {
                evaluateClientStatus(details);
                evaluateHts(details);
                evaluateCondomProvision(details);
                evaluateFamilyPlanning(details);
                evaluateTbScreening(details);
                evaluateStiScreening(details);
                evaluateHepatitis(details);
                if (memberObject.getGender().equalsIgnoreCase(Constants.MALE)) {
                    evaluateVmmc(details);
                } else {
                    evaluateCervicalScreening(details);
                }
                if (shouldShowMat())
                    evaluateMat(details);
            } catch (Exception e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateClientStatus(Map<String, List<VisitDetail>> details) throws Exception {

        JSONObject clientStatusForm = FormUtils.getFormUtils().getFormJson(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_CLIENT_STATUS);
        JSONArray fields = clientStatusForm.getJSONObject(org.smartregister.chw.hf.utils.Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);

        //update client status
        JSONObject client_status_object = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "client_status");
        if (StringUtils.isNotBlank(HfKvpDao.getClientStatus(memberObject.getBaseEntityId()))) {
            KvpJsonFormUtils.removeOptionFromCheckboxListWithKey(client_status_object, "new_client");
        } else {
            try {
                String enrollmentDateString = HfKvpDao.getClientEnrollmentDate(memberObject.getBaseEntityId());
                if (StringUtils.isNotBlank(enrollmentDateString)) {
                    Date enrollmentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(enrollmentDateString);

                    Date currentDate = new Date();
                    long cutoffTime = currentDate.getTime() - (28L * 24L * 60L * 60L * 1000L);
                    Date cutoffDate = new Date(cutoffTime);


                    if (enrollmentDate != null && !enrollmentDate.before(cutoffDate)) {
                        KvpJsonFormUtils.removeOptionFromCheckboxListWithKey(client_status_object, "return");
                    }
                }
            } catch (Exception e) {
                Timber.e(e);
                KvpJsonFormUtils.removeOptionFromCheckboxListWithKey(client_status_object, "return");
            }

        }

        //update other_kvp_category
        JSONObject other_kvp_category = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "other_kvp_category");
        KvpJsonFormUtils.removeOptionFromCheckboxListWithKey(other_kvp_category, KvpDao.getDominantKVPGroup(memberObject.getBaseEntityId()));
        if (memberObject.getGender().equalsIgnoreCase(Constants.MALE)) {
            //remove FSW, AGYW
            //TODO: extract keys to constant
            KvpJsonFormUtils.removeOptionFromCheckboxListWithKey(other_kvp_category, "fsw");
            KvpJsonFormUtils.removeOptionFromCheckboxListWithKey(other_kvp_category, "agyw");
        }
        if (memberObject.getGender().equalsIgnoreCase(Constants.FEMALE)) {
            //remove MSM
            //TODO: extract keys to constant
            KvpJsonFormUtils.removeOptionFromCheckboxListWithKey(other_kvp_category, "msm");

            if (memberObject.getAge() > 24) {
                //remove AGYW
                KvpJsonFormUtils.removeOptionFromCheckboxListWithKey(other_kvp_category, "agyw");
            }
        }


        KvpClientStatusActionHelper actionHelper = new KvpClientStatusActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_client_status))
                .withOptional(false)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_CLIENT_STATUS)
                .withJsonPayload(clientStatusForm.toString())
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
        JSONObject kvpPrepPepJson = FormUtils.getFormUtils().getFormJson(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_PrEP_PEP);
        try {
            JSONArray fields = kvpPrepPepJson.getJSONObject(org.smartregister.chw.hf.utils.Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
            //update enrolled_to_prep
            JSONObject enrolled_to_prep_object = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "enrolled_to_prep");
            enrolled_to_prep_object.put(VALUE, KvpDao.getPrEPMember(memberObject.getBaseEntityId()) != null);
        } catch (Exception e) {
            Timber.e(e);
        }

        KvpPrepPepActionHelper actionHelper = new KvpPrepPepActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_pep_assesment))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withJsonPayload(kvpPrepPepJson.toString())
                .withFormName(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_PrEP_PEP)
                .build();

        actionList.put(context.getString(R.string.kvp_pep_assesment), action);
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

        String formName;
        if (memberObject.getGender().equalsIgnoreCase("male"))
            formName = Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_MALE_FAMILY_PLANNING_SERVICES;
        else
            formName = Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_FEMALE_FAMILY_PLANNING_SERVICES;
        KvpFamilyPlanningActionHelper actionHelper = new KvpFamilyPlanningActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_family_planning))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(formName)
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
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.kvp_heptitis_test))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_HEPATITIS)
                .build();

        actionList.put(context.getString(R.string.kvp_heptitis_test), action);
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

    private class KvpHtsActionHelper extends org.smartregister.chw.hf.actionhelper.kvp.KvpHtsActionHelper {
        @Override
        public String postProcess(String s) {
            if (StringUtils.isBlank(hiv_status) || !(hiv_status.equalsIgnoreCase("positive") || hiv_status.equalsIgnoreCase("known_positive"))) {
                try {
                    if (KvpDao.getPrEPMember(memberObject.getBaseEntityId()) == null)
                        evaluatePrepPep(details);
                } catch (BaseKvpVisitAction.ValidationException e) {
                    e.printStackTrace();
                }
            } else {
                actionList.remove(context.getString(R.string.kvp_pep_assesment));
            }
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            return super.postProcess(s);
        }
    }

    private class KvpClientStatusActionHelper extends org.smartregister.chw.hf.actionhelper.kvp.KvpClientStatusActionHelper {
        private String other_kvp_category;

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                other_kvp_category = CoreJsonFormUtils.getValue(jsonObject, "other_kvp_category");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPayloadReceived(jsonPayload);
        }

        @Override
        public String postProcess(String s) {
            if ((other_kvp_category.contains("pwud") || other_kvp_category.contains("pwid")) && !shouldShowMat()) {
                try {
                    evaluateMat(details);
                } catch (Exception e) {
                    Timber.e(e);
                }
            } else if (!shouldShowMat()) {
                actionList.remove(context.getString(R.string.kvp_mat));
            }

            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));

            return super.postProcess(s);
        }
    }

    private boolean shouldShowMat() {
        return KvpDao.getDominantKVPGroup(memberObject.getBaseEntityId()).equalsIgnoreCase("pwud") ||
                KvpDao.getDominantKVPGroup(memberObject.getBaseEntityId()).equalsIgnoreCase("pwid");
    }
}
