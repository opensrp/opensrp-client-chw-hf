package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.LDPartographFetalWellBeingActionHelper;
import org.smartregister.chw.hf.actionhelper.LDPartographLabourProgressActionHelper;
import org.smartregister.chw.hf.actionhelper.LDPartographMotherWellBeingActionHelper;
import org.smartregister.chw.hf.actionhelper.LDPartographTimeActionHelper;
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

/**
 * @author issyzac 5/7/22
 */
public class LDPartographInteractorFlv implements LDPartographInteractor.Flavor {

    LinkedHashMap<String, BaseLDVisitAction> actionList = new LinkedHashMap<>();

    @Override
    public LinkedHashMap<String, BaseLDVisitAction> calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) throws BaseLDVisitAction.ValidationException {

        Context context = view.getContext();
        Map<String, List<VisitDetail>> details = null;

        //get preloaded data
        if (view.getEditMode()){
            Visit lastVisit = LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.LD_PARTOGRAPHY);
            if (lastVisit != null){
                details = VisitUtils.getVisitGroups(LDLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        evaluatePartographTime(actionList, details, memberObject, context);
        evaluateFetalWellbeing(actionList, details, memberObject, context);
        evaluateMotherWellBeing(actionList, details, memberObject, context);
        evaluateProgressOfLabour(actionList, details, memberObject, context);

        return actionList;
    }


    private void evaluatePartographTime(LinkedHashMap<String, BaseLDVisitAction> actionList,
                                        Map<String, List<VisitDetail>> details,
                                        final MemberObject memberObject,
                                        final Context context) throws BaseLDVisitAction.ValidationException {

        BaseLDVisitAction partographTime = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_partograph_time))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LabourAndDeliveryPartograph.getPartographTimeForm())
                .withHelper(new LDPartographTimeActionHelper(memberObject))
                .build();

        actionList.put(context.getString(R.string.ld_partograph_time), partographTime);
    }

    private void evaluateFetalWellbeing(LinkedHashMap<String, BaseLDVisitAction> actionList,
                                        Map<String, List<VisitDetail>> details,
                                        final MemberObject memberObject,
                                        final Context context
    ) throws BaseLDVisitAction.ValidationException {


        BaseLDVisitAction fetalWellBeingAction = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_partograph_fetal_well_being))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LabourAndDeliveryPartograph.getFetalWellBingForm())
                .withHelper(new LDPartographFetalWellBeingActionHelper(memberObject,memberObject.getBaseEntityId()))
                .build();

        actionList.put(context.getString(R.string.ld_partograph_fetal_well_being), fetalWellBeingAction);

    }

    private void evaluateMotherWellBeing(LinkedHashMap<String, BaseLDVisitAction> actionList,
                                         Map<String, List<VisitDetail>> details,
                                         final MemberObject memberObject,
                                         final Context context
    ) throws BaseLDVisitAction.ValidationException {

        BaseLDVisitAction motherWellBeingAction = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_partograph_mother_well_being))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LabourAndDeliveryPartograph.getMotherWellBeingForm())
                .withHelper(new LDPartographMotherWellBeingActionHelper(memberObject))
                .build();

        actionList.put(context.getString(R.string.ld_partograph_mother_well_being), motherWellBeingAction);

    }

    private void evaluateProgressOfLabour(LinkedHashMap<String, BaseLDVisitAction> actionList,
                                        Map<String, List<VisitDetail>> details,
                                        final MemberObject memberObject,
                                        final Context context
    ) throws BaseLDVisitAction.ValidationException {

        BaseLDVisitAction progressOfLaborAction = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_partograph_labor_progress))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LabourAndDeliveryPartograph.getProgressOfLabourForm())
                .withHelper(new LDPartographLabourProgressActionHelper(memberObject))
                .build();

        actionList.put(context.getString(R.string.ld_partograph_labor_progress), progressOfLaborAction);

    }

}
