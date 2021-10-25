package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.AncBirthReviewAction;
import org.smartregister.chw.hf.actionhelper.AncConsultationAction;
import org.smartregister.chw.hf.actionhelper.AncLabTestAction;
import org.smartregister.chw.hf.actionhelper.AncPharmacyAction;
import org.smartregister.chw.hf.actionhelper.AncPregnancyStatusAction;
import org.smartregister.chw.hf.actionhelper.AncTriageAction;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.ContactUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class AncRecurringFacilityVisitInteractorFlv implements AncFirstFacilityVisitInteractor.Flavor {
    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();

        Context context = view.getContext();

        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.ANC_FIRST_FACILITY_VISIT);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        // get contact
        LocalDate lastContact = new DateTime(memberObject.getDateCreated()).toLocalDate();
        boolean isFirst = (StringUtils.isBlank(memberObject.getLastContactVisit()));
        LocalDate lastMenstrualPeriod = new LocalDate();
        try {
            lastMenstrualPeriod = DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(memberObject.getLastMenstrualPeriod());
        } catch (Exception e) {
            Timber.e(e);
        }


        if (StringUtils.isNotBlank(memberObject.getLastContactVisit())) {
            lastContact = DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(memberObject.getLastContactVisit());
        }

        Map<Integer, LocalDate> dateMap = new LinkedHashMap<>();

        // today is the due date for the very first visit
        if (isFirst) {
            dateMap.put(0, LocalDate.now());
        }

        dateMap.putAll(ContactUtil.getContactWeeks(isFirst, lastContact, lastMenstrualPeriod));

        evaluateMedicalAndSurgicalHistory(actionList, details, memberObject, context);

        return actionList;
    }

    private void evaluateMedicalAndSurgicalHistory(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                                   Map<String, List<VisitDetail>> details,
                                                   final MemberObject memberObject,
                                                   final Context context) throws BaseAncHomeVisitAction.ValidationException {
        BaseAncHomeVisitAction triage = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_recuring_visit_triage))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.ANC_RECURRING_VISIT.getTriage())
                .withHelper(new AncTriageAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_recuring_visit_triage), triage);

        BaseAncHomeVisitAction consultation = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_recuring_visit_cunsultation))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.ANC_RECURRING_VISIT.getConsultation())
                .withHelper(new AncConsultationAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_recuring_visit_cunsultation), consultation);



        BaseAncHomeVisitAction labTests = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_recuring_visit_lab_tests))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.ANC_RECURRING_VISIT.getLabTests())
                .withHelper(new AncLabTestAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_recuring_visit_lab_tests), labTests);



        BaseAncHomeVisitAction pharmacy = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_recuring_visit_pharmacy))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.ANC_RECURRING_VISIT.getPharmacy())
                .withHelper(new AncPharmacyAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_recuring_visit_pharmacy), pharmacy);



        BaseAncHomeVisitAction pregnancyStatus = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_recuring_visit_pregnancy_status))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.ANC_RECURRING_VISIT.getPregnancyStatus())
                .withHelper(new AncPregnancyStatusAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_recuring_visit_pregnancy_status), pregnancyStatus);



        BaseAncHomeVisitAction birthReview = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_recuring_visit_review_birth_and_emergency_plan))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.ANC_RECURRING_VISIT.getBirthReviewAndEmergencyPlan())
                .withHelper(new AncBirthReviewAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.anc_recuring_visit_review_birth_and_emergency_plan), birthReview);


    }


}

