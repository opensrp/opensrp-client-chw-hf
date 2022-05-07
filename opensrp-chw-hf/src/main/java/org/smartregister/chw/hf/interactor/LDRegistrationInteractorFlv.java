package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.smartregister.chw.anc.util.AppExecutors;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.LDRegistrationAdmissionAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationAncClinicFindingsAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationCurrentLabourAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationObstetricHistoryAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationTriageAction;
import org.smartregister.chw.hf.actionhelper.LDRegistrationTrueLabourConfirmationAction;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.contract.BaseLDVisitContract;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.chw.ld.util.VisitUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * @author ilakozejumanne@gmail.com
 * 06/05/2022
 */
public class LDRegistrationInteractorFlv implements LDRegistrationInteractor.Flavor {
    private String baseEntityId;
    LinkedHashMap<String, BaseLDVisitAction> actionList = new LinkedHashMap<>();

    public LDRegistrationInteractorFlv(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    @Override
    public LinkedHashMap<String, BaseLDVisitAction> calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) throws BaseLDVisitAction.ValidationException {

        Context context = view.getContext();

        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.LD_REGISTRATION);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(LDLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        evaluateLDRegistration(actionList, details, memberObject, context, callBack);

        return actionList;
    }

    private void evaluateLDRegistration(LinkedHashMap<String, BaseLDVisitAction> actionList,
                                        Map<String, List<VisitDetail>> details,
                                        final MemberObject memberObject,
                                        final Context context,
                                        BaseLDVisitContract.InteractorCallBack callBack
    ) throws BaseLDVisitAction.ValidationException {
        BaseLDVisitAction ldRegistrationTriage = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_triage_title))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryRegistrationTriage())
                .withHelper(new LDRegistrationTriageAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.ld_registration_triage_title), ldRegistrationTriage);

        BaseLDVisitAction ldRegistrationTrueLabourConfirmation = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_true_labour_title))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryRegistrationTrueLabourConfirmation())
                .withHelper(new TrueLabourConfirmationAction(memberObject, actionList, details, callBack, context))
                .build();
        actionList.put(context.getString(R.string.ld_registration_true_labour_title), ldRegistrationTrueLabourConfirmation);
    }

    private static class TrueLabourConfirmationAction extends LDRegistrationTrueLabourConfirmationAction {
        private LinkedHashMap<String, BaseLDVisitAction> actionList;
        private Context context;
        private Map<String, List<VisitDetail>> details;
        private BaseLDVisitContract.InteractorCallBack callBack;

        public TrueLabourConfirmationAction(MemberObject memberObject, LinkedHashMap<String, BaseLDVisitAction> actionList, Map<String, List<VisitDetail>> details, BaseLDVisitContract.InteractorCallBack callBack, Context context) {
            super(memberObject);
            this.actionList = actionList;
            this.context = context;
            this.details = details;
            this.callBack = callBack;
        }

        @Override
        public String postProcess(String s) {
            if (labourConfirmation.equalsIgnoreCase("true")) {
                //Adding the next actions when true labour confirmation is completed and the client is confirmed with True Labour.
                try {
                    BaseLDVisitAction ldRegistrationAdmissionInformation = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_admission_information_title))
                            .withOptional(false)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryAdmissionInformation())
                            .withHelper(new LDRegistrationAdmissionAction(memberObject))
                            .build();

                    actionList.put(context.getString(R.string.ld_registration_admission_information_title), ldRegistrationAdmissionInformation);
                } catch (Exception e) {
                    Timber.e(e);
                }

                try {
                    BaseLDVisitAction ldRegistrationObstetricHistory = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_obstetric_history_title))
                            .withOptional(false)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryObstetricHistory())
                            .withHelper(new LDRegistrationObstetricHistoryAction(memberObject))
                            .build();

                    actionList.put(context.getString(R.string.ld_registration_obstetric_history_title), ldRegistrationObstetricHistory);
                } catch (Exception e) {
                    Timber.e(e);
                }

                try {
                    BaseLDVisitAction ldRegistrationAncClinicFindings = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_anc_clinic_findings_title))
                            .withOptional(false)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryAncClinicFindings())
                            .withHelper(new LDRegistrationAncClinicFindingsAction(memberObject))
                            .build();

                    actionList.put(context.getString(R.string.ld_registration_anc_clinic_findings_title), ldRegistrationAncClinicFindings);
                } catch (Exception e) {
                    Timber.e(e);
                }

                try {
                    BaseLDVisitAction ldRegistrationCurrentLabour = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_current_labour_title))
                            .withOptional(false)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryCurrentLabour())
                            .withHelper(new LDRegistrationCurrentLabourAction(memberObject))
                            .build();
                    actionList.put(context.getString(R.string.ld_registration_current_labour_title), ldRegistrationCurrentLabour);
                } catch (BaseLDVisitAction.ValidationException e) {
                    Timber.e(e);
                }


            } else {
                //Removing the next actions  the client is confirmed with False Labour.
                if (actionList.containsKey(context.getString(R.string.ld_registration_admission_information_title))) {
                    actionList.remove(context.getString(R.string.ld_registration_admission_information_title));
                }

                if (actionList.containsKey(context.getString(R.string.ld_registration_obstetric_history_title))) {
                    actionList.remove(context.getString(R.string.ld_registration_obstetric_history_title));
                }

                if (actionList.containsKey(context.getString(R.string.ld_registration_anc_clinic_findings_title))) {
                    actionList.remove(context.getString(R.string.ld_registration_anc_clinic_findings_title));
                }

                if (actionList.containsKey(context.getString(R.string.ld_registration_current_labour_title))) {
                    actionList.remove(context.getString(R.string.ld_registration_current_labour_title));
                }

            }

            //Calling the callback method to preload the actions in the actionns list.
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            return super.postProcess(s);
        }
    }


}

