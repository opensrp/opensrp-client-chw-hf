package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.interactor.CoreAncHomeVisitInteractor;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.actionhelper.AncTtVaccinationAction;
import org.smartregister.chw.pmtct.contract.BasePmtctHomeVisitContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;

import java.util.LinkedHashMap;

public class PmtctFollowupVisitInteractorFlv implements PmtctFollowupVisitInteractor.Flavor {
    @Override
    public LinkedHashMap<String, BasePmtctHomeVisitAction> calculateActions(BasePmtctHomeVisitContract.View view, MemberObject memberObject, BasePmtctHomeVisitContract.InteractorCallBack interactorCallBack) throws BasePmtctHomeVisitAction.ValidationException {
        LinkedHashMap<String,BasePmtctHomeVisitAction> actionList = new LinkedHashMap<>();

        Context context = view.getContext();

        //evaluatePmtctActions(actionList,memberObject,context);
        BasePmtctHomeVisitAction hvlTestAction = new BasePmtctHomeVisitAction.Builder(context,"HVL TEST")
                .withOptional(true)
                .withFormName(CoreConstants.JSON_FORM.getPmtctForm())
                .build();
        actionList.put("HVL TEST",hvlTestAction);
        return actionList;
    }

//
//    private void evaluatePmtctActions(LinkedHashMap<String,BaseAncHomeVisitAction> actionList, final MemberObject memberObject, Context context) throws BaseAncHomeVisitAction.ValidationException {
//
//    }
}


