package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.LDRegistrationTriageAction;
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
 * @author ilakozejumanne@gmail.com
 * 06/05/2022
 */
public class LDRegistrationInteractorFlv implements LDRegistrationInteractor.Flavor {
    private String baseEntityId;
    LinkedHashMap<String, BaseLDVisitAction> actionList = new LinkedHashMap<>();

    public LDRegistrationInteractorFlv(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    @Override
    public LinkedHashMap<String, BaseLDVisitAction> calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) throws BaseLDVisitAction.ValidationException {

        Context context = view.getContext();

        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.LD_REGISTRATION);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(LDLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        evaluateLDRegistration(actionList, details, memberObject, context, callBack);

        return actionList;
    }

    private void evaluateLDRegistration(LinkedHashMap<String, BaseLDVisitAction> actionList,
                                        Map<String, List<VisitDetail>> details,
                                        final MemberObject memberObject,
                                        final Context context,
                                        BaseLDVisitContract.InteractorCallBack callBack
    ) throws BaseLDVisitAction.ValidationException {
        BaseLDVisitAction ldRegistrationTriage = new BaseLDVisitAction.Builder(context, context.getString(R.string.ld_registration_triage_title))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LabourAndDeliveryRegistration.getLabourAndDeliveryRegistrationTriage())
                .withHelper(new LDRegistrationTriageAction(memberObject))
                .build();
        actionList.put(context.getString(R.string.ld_registration_triage_title), ldRegistrationTriage);

    }


}

