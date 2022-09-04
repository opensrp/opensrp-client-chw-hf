package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.interactor.CorePmtctHomeVisitInteractor;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.actionhelper.PmtctVisitAction;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.PmtctLibrary;
import org.smartregister.chw.pmtct.contract.BasePmtctHomeVisitContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.domain.Visit;
import org.smartregister.chw.pmtct.domain.VisitDetail;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;
import org.smartregister.chw.pmtct.util.JsonFormUtils;
import org.smartregister.chw.pmtct.util.VisitUtils;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class EacFirstVisitInteractorFlv implements CorePmtctHomeVisitInteractor.Flavor {
    @Override
    public LinkedHashMap<String, BasePmtctHomeVisitAction> calculateActions(BasePmtctHomeVisitContract.View view, MemberObject memberObject, BasePmtctHomeVisitContract.InteractorCallBack interactorCallBack) throws BasePmtctHomeVisitAction.ValidationException {
        LinkedHashMap<String, BasePmtctHomeVisitAction> actionList = new LinkedHashMap<>();

        Context context = view.getContext();

        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = PmtctLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.PMTCT_FIRST_EAC_VISIT);
        if (lastVisit != null) {
            details = VisitUtils.getVisitGroups(PmtctLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
        }
    }

        evaluateEacActions(actionList, details, memberObject, context);

        return actionList;
    }

    private void evaluateEacActions(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, MemberObject memberObject, Context context) throws BasePmtctHomeVisitAction.ValidationException {
        JSONObject firstEacVisitForm = null;
        JSONObject secondEacVisitForm = null;
        JSONObject thirdEacVisitForm = null;

        try{
            firstEacVisitForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.EacVisits.PMTCT_EAC_VISIT);
            firstEacVisitForm.getJSONObject("global").put("type_of_visit","eac_day_1");
            if (details != null && !details.isEmpty()) {
                JsonFormUtils.populateForm(firstEacVisitForm, details);
            }
        }catch (JSONException e){
            Timber.e(e);
        }
        try{
            secondEacVisitForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.EacVisits.PMTCT_EAC_VISIT);
            secondEacVisitForm.getJSONObject("global").put("type_of_visit","eac_day_2");
            if (details != null && !details.isEmpty()) {
                JsonFormUtils.populateForm(secondEacVisitForm, details);
            }
        }catch (JSONException e){
            Timber.e(e);
        }
        try{
            thirdEacVisitForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.EacVisits.PMTCT_EAC_VISIT);
            thirdEacVisitForm.getJSONObject("global").put("type_of_visit","eac_day_3");
            if (details != null && !details.isEmpty()) {
                JsonFormUtils.populateForm(thirdEacVisitForm, details);
            }
        }catch (JSONException e){
            Timber.e(e);
        }

        BasePmtctHomeVisitAction EACFirstVisit = new BasePmtctHomeVisitAction.Builder(context, "EAC Visit - Day One")
                .withOptional(false)
                .withDetails(details)
                .withJsonPayload(firstEacVisitForm.toString())
                .withFormName(Constants.JsonForm.EacVisits.getPmtctEacVisit())
                .withHelper(new EACFirstVisitAction(memberObject))
                .build();
        actionList.put("EAC Visit - Day One", EACFirstVisit);

        BasePmtctHomeVisitAction EACSecondVisit = new BasePmtctHomeVisitAction.Builder(context, "EAC Visit - Day Two")
                .withOptional(true)
                .withDetails(details)
                .withJsonPayload(secondEacVisitForm.toString())
                .withFormName(Constants.JsonForm.EacVisits.getPmtctEacVisit())
                .withHelper(new EACSecondVisitAction(memberObject))
                .build();
        actionList.put("EAC Visit - Day Two", EACSecondVisit);

        BasePmtctHomeVisitAction EACThirdVisit = new BasePmtctHomeVisitAction.Builder(context, "EAC Visit - Day Three")
                .withOptional(true)
                .withDetails(details)
                .withJsonPayload(thirdEacVisitForm.toString())
                .withFormName(Constants.JsonForm.EacVisits.getPmtctEacVisit())
                .withHelper(new EACThirdVisitAction(memberObject))
                .build();
        actionList.put("EAC Visit - Day Three", EACThirdVisit);
    }

    private class EACFirstVisitAction extends PmtctVisitAction {
        protected MemberObject memberObject;
        private String first_visit;

        public EACFirstVisitAction(MemberObject memberObject) {
            super(memberObject);
            this.memberObject = memberObject;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try{
                JSONObject jsonObject = new JSONObject(jsonPayload);
                first_visit = CoreJsonFormUtils.getValue(jsonObject, "eac_day_1");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public String evaluateSubTitle() {
            if (StringUtils.isBlank(first_visit))
                return null;

            return MessageFormat.format("Visit Date: {0}", first_visit);
        }

        @Override
        public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(first_visit)) {
                return BasePmtctHomeVisitAction.Status.PENDING;
            } else {
                return BasePmtctHomeVisitAction.Status.COMPLETED;
            }
        }

    }

    private class EACSecondVisitAction extends PmtctVisitAction {
        protected MemberObject memberObject;
        private String second_visit;

        public EACSecondVisitAction(MemberObject memberObject) {
            super(memberObject);
            this.memberObject = memberObject;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try{
                JSONObject jsonObject = new JSONObject(jsonPayload);
                second_visit = CoreJsonFormUtils.getValue(jsonObject, "eac_day_2");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public String evaluateSubTitle() {
            if (StringUtils.isBlank(second_visit))
                return null;

            return MessageFormat.format("Visit Date: {0}", second_visit);
        }

        @Override
        public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(second_visit)) {
                return BasePmtctHomeVisitAction.Status.PENDING;
            } else {
                return BasePmtctHomeVisitAction.Status.COMPLETED;
            }
        }

    }

    private class EACThirdVisitAction extends PmtctVisitAction {
        protected MemberObject memberObject;
        private String third_visit;

        public EACThirdVisitAction(MemberObject memberObject) {
            super(memberObject);
            this.memberObject = memberObject;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try{
                JSONObject jsonObject = new JSONObject(jsonPayload);
                third_visit = CoreJsonFormUtils.getValue(jsonObject, "eac_day_3");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public String evaluateSubTitle() {
            if (StringUtils.isBlank(third_visit))
                return null;

            return MessageFormat.format("Visit Date: {0}", third_visit);
        }

        @Override
        public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(third_visit)) {
                return BasePmtctHomeVisitAction.Status.PENDING;
            } else {
                return BasePmtctHomeVisitAction.Status.COMPLETED;
            }
        }
    }

}
