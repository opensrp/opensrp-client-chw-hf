package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;

import java.util.LinkedHashMap;

public class PncFacilityVisitInteractorFlv implements AncFirstFacilityVisitInteractor.Flavor {
    LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();


    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {

        //TODO uncomment to get details

//        Map<String, List<VisitDetail>> details = null;
//        // get the preloaded data
//        if (view.getEditMode()) {
//            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.PNC_VISIT);
//            if (lastVisit != null) {
//                details = VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
//            }
//        }

        // TODO implement actions for PNC
        //evaluatePncActions(view, memberObject, callBack, details);

        return actionList;
    }

//    private void evaluatePncActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack, Map<String, List<VisitDetail>> details
//    ) throws BaseAncHomeVisitAction.ValidationException {
//
//    }


}

