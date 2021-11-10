package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.contract.BasePmtctHomeVisitContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;

import java.util.LinkedHashMap;

public class PmtctFollowupVisitInteractorFlv implements PmtctFollowupVisitInteractor.Flavor {
    @Override
    public LinkedHashMap<String, BasePmtctHomeVisitAction> calculateActions(BasePmtctHomeVisitContract.View view, MemberObject memberObject, BasePmtctHomeVisitContract.InteractorCallBack interactorCallBack) throws BasePmtctHomeVisitAction.ValidationException {
        LinkedHashMap<String, BasePmtctHomeVisitAction> actionList = new LinkedHashMap<>();

        Context context = view.getContext();

        evaluatePmtctActions(actionList, memberObject, context);

        return actionList;
    }


    private void evaluatePmtctActions(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, final MemberObject memberObject, Context context) throws BasePmtctHomeVisitAction.ValidationException {
        BasePmtctHomeVisitAction hvlTestAction = new BasePmtctHomeVisitAction.Builder(context, "HVL TEST")
                .withOptional(true)
                .withFormName(Constants.JSON_FORM.getHvlTestForm())
                .build();
        actionList.put("HVL TEST", hvlTestAction);

        BasePmtctHomeVisitAction hvlSuppression = new BasePmtctHomeVisitAction.Builder(context, "HVL SUPPRESSION")
                .withOptional(true)
                .withFormName(Constants.JSON_FORM.getHvlSuppressionForm())
                .build();
        actionList.put("HVL SUPPRESSION", hvlSuppression);
    }
}


