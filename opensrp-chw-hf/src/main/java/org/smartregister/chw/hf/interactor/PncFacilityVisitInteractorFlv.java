package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.PncFamilyPlanningServicesAction;
import org.smartregister.chw.hf.actionhelper.PncHivTestingAction;
import org.smartregister.chw.hf.actionhelper.PncImmunizationAction;
import org.smartregister.chw.hf.actionhelper.PncMotherGeneralExaminationAction;
import org.smartregister.chw.hf.actionhelper.PncNutrionSupplementAction;
import org.smartregister.chw.hf.dao.HfPncDao;
import org.smartregister.chw.hf.utils.Constants;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PncFacilityVisitInteractorFlv implements AncFirstFacilityVisitInteractor.Flavor {
    LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();

    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {

        Context context = view.getContext();

        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.PNC_VISIT);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        evaluatePncActions(memberObject, details, context);
        return actionList;
    }

    private void evaluatePncActions(MemberObject memberObject, Map<String, List<VisitDetail>> details, Context context
    ) throws BaseAncHomeVisitAction.ValidationException {

        BaseAncHomeVisitAction motherGeneralExamination = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.mother_general_examination))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getPncMotherGeneralExamination())
                .withHelper(new PncMotherGeneralExaminationAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.mother_general_examination), motherGeneralExamination);

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

