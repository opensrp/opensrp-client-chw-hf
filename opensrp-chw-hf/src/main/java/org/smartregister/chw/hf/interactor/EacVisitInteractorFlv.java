package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.smartregister.chw.core.interactor.CorePmtctHomeVisitInteractor;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.contract.BasePmtctHomeVisitContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;

import java.util.LinkedHashMap;

public class EacVisitInteractorFlv implements CorePmtctHomeVisitInteractor.Flavor {
    @Override
    public LinkedHashMap<String, BasePmtctHomeVisitAction> calculateActions(BasePmtctHomeVisitContract.View view, MemberObject memberObject, BasePmtctHomeVisitContract.InteractorCallBack interactorCallBack) throws BasePmtctHomeVisitAction.ValidationException {
        LinkedHashMap<String, BasePmtctHomeVisitAction> actionList = new LinkedHashMap<>();

        Context context = view.getContext();

        evaluateEacActions(actionList, memberObject, context);

        return actionList;
    }

    private void evaluateEacActions(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, MemberObject memberObject, Context context) throws BasePmtctHomeVisitAction.ValidationException {
        BasePmtctHomeVisitAction EAC = new BasePmtctHomeVisitAction.Builder(context, "Enhanced Adherence Counselling (EAC)")
                .withOptional(false)
                .withFormName(Constants.JSON_FORM.getPmtctEacFirst())
                .build();
        actionList.put("Enhanced Adherence Counselling (EAC)", EAC);
    }
}
