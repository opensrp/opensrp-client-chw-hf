package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.actionhelper.PmtctVisitAction;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.contract.BasePmtctHomeVisitContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PmtctFollowupVisitInteractorFlv implements PmtctFollowupVisitInteractor.Flavor {
    public String hvlTestValue;

    @Override
    public LinkedHashMap<String, BasePmtctHomeVisitAction> calculateActions(BasePmtctHomeVisitContract.View view, MemberObject memberObject, BasePmtctHomeVisitContract.InteractorCallBack interactorCallBack) throws BasePmtctHomeVisitAction.ValidationException {
        LinkedHashMap<String, BasePmtctHomeVisitAction> actionList = new LinkedHashMap<>();

        Context context = view.getContext();

        evaluatePmtctActions(actionList, memberObject, context);

        return actionList;
    }


    private void evaluatePmtctActions(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, final MemberObject memberObject, Context context) throws BasePmtctHomeVisitAction.ValidationException {

        BasePmtctHomeVisitAction HVLFollowup = new BasePmtctHomeVisitAction.Builder(context, "HIV Viral Load (HVL)")
                .withOptional(false)
                .withFormName(Constants.JSON_FORM.getHvlSuppressionForm())
                .build();
        actionList.put("HIV Viral Load (HVL)", HVLFollowup);

        BasePmtctHomeVisitAction EAC = new BasePmtctHomeVisitAction.Builder(context, "Enhanced Adherence Counselling (EAC)")
                .withOptional(false)
                .withFormName(Constants.JSON_FORM.getPmtctEacFirst())
                .build();
        actionList.put("Enhanced Adherence Counselling (EAC)", EAC);
    }

}


