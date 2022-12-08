package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.anc.util.AppExecutors;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.LDPartographFetalWellBeingActionHelper;
import org.smartregister.chw.hf.actionhelper.LDPartographLabourProgressActionHelper;
import org.smartregister.chw.hf.actionhelper.LDPartographMotherWellBeingActionHelper;
import org.smartregister.chw.hf.actionhelper.LDPartographTimeActionHelper;
import org.smartregister.chw.hf.actionhelper.LDPartographTreatmentDuringLabourActionHelper;
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
 * @author issyzac 5/7/22
 */
public class LDPartographInteractorFlv implements LDPartographInteractor.Flavor {

    LinkedHashMap<String, BaseLDVisitAction> actionList = new LinkedHashMap<>();
    private boolean editMode;

    @Override
    public LinkedHashMap<String, BaseLDVisitAction> calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) throws BaseLDVisitAction.ValidationException {

        Context context = view.getContext();
        Map<String, List<VisitDetail>> details = null;

        editMode = view.getEditMode();

        //get preloaded data
        if (editMode) {
            Visit lastVisit = LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.LD_PARTOGRAPHY);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(LDLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        evaluatePartographTime(actionList, details, memberObject, context, callBack);

        return actionList;
    }


    private void evaluatePartographTime(LinkedHashMap<String, BaseLDVisitAction> actionList,
                                        Map<String, List<VisitDetail>> details,
                                        final MemberObject memberObject,
                                        final Context context, BaseLDVisitContract.InteractorCallBack callBack) throws BaseLDVisitAction.ValidationException {

        BaseLDVisitAction partographTime = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_partograph_time))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LabourAndDeliveryPartograph.getPartographTimeForm())
                .withHelper(new PartographProcessActionHelper(memberObject, editMode, actionList, context, details, callBack))
                .build();

        actionList.put(context.getString(R.string.ld_partograph_time), partographTime);
    }

    public static class PartographProcessActionHelper extends LDPartographTimeActionHelper {

        private final LinkedHashMap<String, BaseLDVisitAction> actionList;
        private final Context context;
        private final Map<String, List<VisitDetail>> details;
        private final BaseLDVisitContract.InteractorCallBack callBack;
        private final MemberObject memberObject;

        public PartographProcessActionHelper(MemberObject memberObject, boolean editMode, LinkedHashMap<String, BaseLDVisitAction> actionList, Context context,
                                             Map<String, List<VisitDetail>> details, BaseLDVisitContract.InteractorCallBack callBack) {
            super(memberObject, editMode);
            this.actionList = actionList;
            this.context = context;
            this.details = details;
            this.callBack = callBack;
            this.memberObject = memberObject;
        }

        @Override
        public String postProcess(String s) {
            if (StringUtils.isNotBlank(time) && StringUtils.isNotBlank(date)) {
                try {
                    BaseLDVisitAction fetalWellBeingAction = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_partograph_fetal_well_being))
                            .withOptional(true)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryPartograph.getFetalWellBingForm())
                            .withHelper(new LDPartographFetalWellBeingActionHelper(memberObject, memberObject.getBaseEntityId()))
                            .build();

                    actionList.put(context.getString(R.string.ld_partograph_fetal_well_being), fetalWellBeingAction);

                    BaseLDVisitAction motherWellBeingAction = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_partograph_mother_well_being))
                            .withOptional(true)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryPartograph.getMotherWellBeingForm())
                            .withHelper(new LDPartographMotherWellBeingActionHelper(memberObject))
                            .build();

                    actionList.put(context.getString(R.string.ld_partograph_mother_well_being), motherWellBeingAction);

                    BaseLDVisitAction progressOfLaborAction = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_partograph_labor_progress))
                            .withOptional(true)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryPartograph.getProgressOfLabourForm())
                            .withHelper(new LDPartographLabourProgressActionHelper(memberObject, date, time))
                            .build();

                    actionList.put(context.getString(R.string.ld_partograph_labor_progress), progressOfLaborAction);

                    BaseLDVisitAction treatmentDuringLabour = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_partograph_treatment_during_labor))
                            .withOptional(true)
                            .withDetails(details)
                            .withFormName(Constants.JsonForm.LabourAndDeliveryPartograph.getTreatmentDuringLabourForm())
                            .withHelper(new LDPartographTreatmentDuringLabourActionHelper(memberObject))
                            .build();

                    actionList.put(context.getString(R.string.ld_partograph_treatment_during_labor), treatmentDuringLabour);

                } catch (Exception e) {
                    Timber.e(e);
                }
            } else {
                if (actionList.containsKey(context.getString(R.string.ld_partograph_fetal_well_being)))
                    actionList.remove(context.getString(R.string.ld_partograph_fetal_well_being));

                if (actionList.containsKey(context.getString(R.string.ld_partograph_mother_well_being)))
                    actionList.remove(context.getString(R.string.ld_partograph_mother_well_being));

                if (actionList.containsKey(context.getString(R.string.ld_partograph_labor_progress)))
                    actionList.remove(context.getString(R.string.ld_partograph_labor_progress));

                if (actionList.containsKey(context.getString(R.string.ld_partograph_treatment_during_labor)))
                    actionList.remove(context.getString(R.string.ld_partograph_treatment_during_labor));
            }
            //Calling the callback method to preload the actions in the actionns list.
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            return super.postProcess(s);
        }
    }

}
