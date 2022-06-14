package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.model.ChildModel;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.PncChildGeneralExamination;
import org.smartregister.chw.hf.actionhelper.PncFamilyPlanningServicesAction;
import org.smartregister.chw.hf.actionhelper.PncHivTestingAction;
import org.smartregister.chw.hf.actionhelper.PncImmunizationAction;
import org.smartregister.chw.hf.actionhelper.PncMotherGeneralExaminationAction;
import org.smartregister.chw.hf.actionhelper.PncNutrionSupplementAction;
import org.smartregister.chw.hf.dao.HfPncDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.referral.util.JsonFormConstants;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PncFacilityVisitInteractorFlv implements AncFirstFacilityVisitInteractor.Flavor {
    LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();
    private List<ChildModel> children;

    private static JSONObject setMinChildHeadCircumference(JSONObject form, String baseEntityId, Context context) {
        double currentMinHeadCircumference = HfPncDao.getChildMinHeadCircumference(baseEntityId);
        try {
            JSONArray fields = form.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
            //update head_circumference minimum
            JSONObject head_circumference = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "head_circumference");

            if (head_circumference != null) {
                JSONObject v_min = head_circumference.getJSONObject("v_min");
                v_min.put("value", currentMinHeadCircumference);
                v_min.put("err", context.getString(R.string.head_circumference_min_err, String.valueOf(currentMinHeadCircumference)) );
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return form;
    }

    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {

        Context context = view.getContext();
        Boolean editMode = view.getEditMode();
        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (editMode) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.PNC_VISIT);
            if (lastVisit != null) {
                details = Collections.unmodifiableMap(VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId())));
            }
        }
        this.children = HfPncDao.childrenForPncWoman(memberObject.getBaseEntityId());

        evaluatePncActions(memberObject, details, context, editMode);
        return actionList;
    }

    private void evaluatePncActions(MemberObject memberObject, Map<String, List<VisitDetail>> details, Context context, Boolean editMode
    ) throws BaseAncHomeVisitAction.ValidationException {

        JSONObject motherGeneralExaminationForm = null;
        try {
            motherGeneralExaminationForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getPncMotherGeneralExamination());

            JSONArray fields = motherGeneralExaminationForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
            //update visit number
            JSONObject visitNumber = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "visit_number");
            visitNumber.put(org.smartregister.chw.pmtct.util.JsonFormUtils.VALUE, HfPncDao.getVisitNumber(memberObject.getBaseEntityId()));

            //loads details to the form
            if (details != null && !details.isEmpty()) {
                org.smartregister.chw.anc.util.JsonFormUtils.populateForm(motherGeneralExaminationForm, details);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        BaseAncHomeVisitAction motherGeneralExamination = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.mother_general_examination))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getPncMotherGeneralExamination())
                .withJsonPayload(motherGeneralExaminationForm.toString())
                .withHelper(new PncMotherGeneralExaminationAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.mother_general_examination), motherGeneralExamination);
        int index = 1;
        for (ChildModel child : children) {
            JSONObject childGeneralExamForm = setMinChildHeadCircumference(FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getPncChildGeneralExamination()), child.getBaseEntityId(), context);
            try {
                childGeneralExamForm.getJSONObject("global").put("baseEntityId", child.getBaseEntityId());
                childGeneralExamForm.getJSONObject("global").put("is_eligible_for_bcg", HfPncDao.isChildEligibleForBcg(child.getBaseEntityId()));
                childGeneralExamForm.getJSONObject("global").put("is_eligible_for_opv0", HfPncDao.isChildEligibleForOpv0(child.getBaseEntityId()));
                childGeneralExamForm.getJSONObject("global").put("is_eligible_for_kangaroo", HfPncDao.isChildEligibleForKangaroo(child.getBaseEntityId(), memberObject.getBaseEntityId()));
            } catch (JSONException e) {
                Timber.e(e);
            }
            Map<String, List<VisitDetail>> childDetails = null;
            if (editMode) {
                Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(child.getBaseEntityId(), Constants.Events.PNC_CHILD_FOLLOWUP);
                if (lastVisit != null) {
                    childDetails = VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
                }
                if (childDetails != null && !childDetails.isEmpty()) {
                    JsonFormUtils.populateForm(childGeneralExamForm, childDetails);
                }
            }
            if(children.size() == 1){
                BaseAncHomeVisitAction childGeneralExamination = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.child_general_examination, child.getFirstName()))
                        .withOptional(false)
                        .withDetails(childDetails)
                        .withBaseEntityID(child.getBaseEntityId())
                        .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                        .withJsonPayload(childGeneralExamForm.toString())
                        .withFormName(Constants.JsonForm.getPncChildGeneralExamination())
                        .withHelper(new PncChildGeneralExamination(memberObject))
                        .build();
                actionList.put(context.getString(R.string.child_general_examination, child.getFirstName()), childGeneralExamination);
            }
            else {
                BaseAncHomeVisitAction childGeneralExamination = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.children_general_examination, child.getFirstName(), index))
                        .withOptional(false)
                        .withDetails(childDetails)
                        .withBaseEntityID(child.getBaseEntityId())
                        .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                        .withJsonPayload(childGeneralExamForm.toString())
                        .withFormName(Constants.JsonForm.getPncChildGeneralExamination())
                        .withHelper(new PncChildGeneralExamination(memberObject))
                        .build();
                actionList.put(context.getString(R.string.children_general_examination, child.getFirstName(), index), childGeneralExamination);
            }
            index++;
        }

        BaseAncHomeVisitAction familyPlanningServices = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.family_planning_services_title))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getPncFamilyPlanningServices())
                .withHelper(new PncFamilyPlanningServicesAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.family_planning_services_title), familyPlanningServices);

        if (HfPncDao.isMotherEligibleForTetanus(memberObject.getBaseEntityId()) || HfPncDao.isMotherEligibleForHepB(memberObject.getBaseEntityId())) {
            JSONObject motherPncImmunization = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.getPncImmunization());
            JSONObject global = null;
            try {
                global = motherPncImmunization.getJSONObject("global");
                global.put("is_eligible_for_tetanus", HfPncDao.isMotherEligibleForTetanus(memberObject.getBaseEntityId()));
                global.put("is_eligible_for_hepatitis_b", HfPncDao.isMotherEligibleForHepB(memberObject.getBaseEntityId()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            BaseAncHomeVisitAction immunization = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.immunization_title))
                    .withOptional(true)
                    .withDetails(details)
                    .withFormName(Constants.JsonForm.getPncImmunization())
                    .withJsonPayload(motherPncImmunization.toString())
                    .withHelper(new PncImmunizationAction(memberObject))
                    .build();
            actionList.put(context.getString(R.string.immunization_title), immunization);
        }

        if (HfPncDao.isMotherEligibleForHivTest(memberObject.getBaseEntityId())) {
            BaseAncHomeVisitAction hivTesting = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_hiv_testing))
                    .withOptional(true)
                    .withDetails(details)
                    .withFormName(Constants.JsonForm.getPncHivTestResults())
                    .withHelper(new PncHivTestingAction(memberObject))
                    .build();
            actionList.put(context.getString(R.string.pnc_hiv_testing), hivTesting);
        }

        BaseAncHomeVisitAction nutritionSupplements = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.nutritional_supplements_title))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getPncNutritionalSupplement())
                .withHelper(new PncNutrionSupplementAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.nutritional_supplements_title), nutritionSupplements);
    }


}

