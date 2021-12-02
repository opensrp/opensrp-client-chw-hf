package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.interactor.CoreAncHomeVisitInteractor;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.VisitUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;

public class AncRecurringFacilityVisitInteractor extends CoreAncHomeVisitInteractor {
    private Flavor flavor;
    public AncRecurringFacilityVisitInteractor(String baseEntityId) {
        setFlavor(new AncRecurringFacilityVisitInteractorFlv(baseEntityId));
        flavor = new AncRecurringFacilityVisitInteractorFlv(baseEntityId);
    }

    @Override
    protected String getEncounterType() {
        return Constants.Events.ANC_RECURRING_FACILITY_VISIT;
    }

    @Override
    protected String getTableName() {
        return Constants.TableName.ANC_RECURRING_FACILITY_VISIT;
    }

    @Override
    public void calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) {
        // update the local database incase of manual date adjustment
        try {
            VisitUtils.processVisits();
        } catch (Exception e) {
            Timber.e(e);
        }

        final Runnable runnable = () -> {
            final LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();

            try {

                for (Map.Entry<String, BaseAncHomeVisitAction> entry : flavor.calculateActions(view, memberObject, callBack).entrySet()) {
                    actionList.put(entry.getKey(), entry.getValue());
                }
            } catch (BaseAncHomeVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }
}
