package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Base;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.LDVisitUtils;
import org.smartregister.chw.ld.contract.BaseLDVisitContract;
import org.smartregister.chw.ld.dao.LDDao;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.interactor.BaseLDVisitInteractor;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.util.JsonFormUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-05-13
 */
public class LDActiveManagementStageActivityInteractor extends BaseLDVisitInteractor {

    protected Context context;
    final LinkedHashMap<String, BaseLDVisitAction> actionList = new LinkedHashMap<>();
    private MemberObject memberObject;

    @Override
    public MemberObject getMemberClient(String memberID) {

        return LDDao.getMember(memberID);
    }

    @Override
    public void calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) {
        context = view.getContext();
        this.memberObject = memberObject;

        final Runnable runnable = () -> {
            // update the local database incase of manual date adjustment
            try {
                VisitUtils.processVisits(memberObject.getBaseEntityId());
            } catch (Exception e) {
                Timber.e(e);
            }

            try {

                evaluateUterotonic();
                evaluateExpulsionOfPlacenta();
                evaluateMassageUterusAfterDelivery();

            } catch (BaseLDVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }


    @Override
    protected String getEncounterType() {
        return "LD Active Management of Labour";
    }

    @Override
    protected void processExternalVisits(Visit visit, Map<String, BaseLDVisitAction> externalVisits, String memberID) throws Exception {
        super.processExternalVisits(visit, externalVisits, memberID);
        try {
            LDVisitUtils.processVisits(memberID);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void evaluateUterotonic() throws BaseLDVisitAction.ValidationException {

        String title = context.getString(R.string.uterotonic);
        UterotonicActionHelper actionHelper = new UterotonicActionHelper();
        BaseLDVisitAction action = getBuilder(title)
                .withOptional(false)
                .withHelper(actionHelper)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withFormName(Constants.JsonForm.LDActiveManagement.getLDActiveManagementUteronics())
                .build();
        actionList.put(title, action);
    }

    private void evaluateExpulsionOfPlacenta() throws BaseLDVisitAction.ValidationException {

        String title = context.getString(R.string.ld_expulsion_of_placenta);
        ExpulsionOfPlacentaHelper actionHelper = new ExpulsionOfPlacentaHelper();
        BaseLDVisitAction action = getBuilder(title)
                .withOptional(false)
                .withHelper(actionHelper)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withFormName(Constants.JsonForm.LDActiveManagement.getLdActiveManagementExpulsionPlacenta())
                .build();

        actionList.put(title, action);

    }

    private void evaluateMassageUterusAfterDelivery() throws BaseLDVisitAction.ValidationException {
        String title = context.getString(R.string.ld_massage_uterus_after_delivery);

        MassageUterusAfterDeliveryActionHelper actionHelper = new MassageUterusAfterDeliveryActionHelper();
        BaseLDVisitAction action = getBuilder(title)
                .withOptional(false)
                .withHelper(actionHelper)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withFormName(Constants.JsonForm.LDActiveManagement.getLdActiveManagementMassageUterus())
                .build();

        actionList.put(title, action);

    }

    public BaseLDVisitAction.Builder getBuilder(String title) {
        return new BaseLDVisitAction.Builder(context, title);
    }

    private static class ExpulsionOfPlacentaHelper implements BaseLDVisitAction.LDVisitActionHelper {

        private String placenta_and_membrane_expulsion;

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {

        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            placenta_and_membrane_expulsion = JsonFormUtils.getFieldValue(jsonPayload, "placenta_and_membrane_expulsion");
        }

        @Override
        public BaseLDVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String jsonPayload) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            if (StringUtils.isNotBlank(placenta_and_membrane_expulsion)) {
                if (placenta_and_membrane_expulsion.equalsIgnoreCase("retained_placenta")) {
                    return "Placenta Retained";
                }
            }
            return null;
        }

        @Override
        public BaseLDVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isNotBlank(placenta_and_membrane_expulsion)) {
                if (placenta_and_membrane_expulsion.equalsIgnoreCase("complete_placenta")) {
                    return BaseLDVisitAction.Status.COMPLETED;
                } else {
                    return BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
                }
            } else {
                return BaseLDVisitAction.Status.PENDING;
            }
        }

        @Override
        public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {

        }
    }

    private static class MassageUterusAfterDeliveryActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

        private String uterus_massage_after_delivery;

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {

        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            uterus_massage_after_delivery = JsonFormUtils.getFieldValue(jsonPayload, "uterus_massage_after_delivery");
        }

        @Override
        public BaseLDVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String jsonPayload) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            if (StringUtils.isNotBlank(uterus_massage_after_delivery)) {
                if (uterus_massage_after_delivery.equalsIgnoreCase("no")) {
                    return "The woman did not receive uterus massage";
                }
            }
            return null;
        }

        @Override
        public BaseLDVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isNotBlank(uterus_massage_after_delivery)) {
                if (uterus_massage_after_delivery.equalsIgnoreCase("yes")) {
                    return BaseLDVisitAction.Status.COMPLETED;
                } else {
                    return BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
                }
            } else {
                return BaseLDVisitAction.Status.PENDING;
            }
        }

        @Override
        public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {

        }
    }

    private static class UterotonicActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

        private String uterotonic;

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {

        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            uterotonic = JsonFormUtils.getFieldValue(jsonPayload, "uterotonic");
        }

        @Override
        public BaseLDVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String jsonPayload) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return null;
        }

        @Override
        public BaseLDVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isNotBlank(uterotonic)) {
                return BaseLDVisitAction.Status.COMPLETED;
            } else {
                return BaseLDVisitAction.Status.PENDING;
            }
        }

        @Override
        public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {

        }
    }
}
