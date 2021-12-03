package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.core.interactor.CorePmtctHomeVisitInteractor;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.PmtctVisitUtils;
import org.smartregister.chw.pmtct.contract.BasePmtctHomeVisitContract;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;

import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;

public class EacSecondVisitInteractor extends CorePmtctHomeVisitInteractor {
    private final Flavor flavor;
    public EacSecondVisitInteractor(){
        setFlavor(new EacSecondVisitInteractorFlv());
        flavor = new EacSecondVisitInteractorFlv();
    }


    @Override
    protected String getEncounterType() {
        return Constants.Events.PMTCT_SECOND_EAC_VISIT;
    }

    @Override
    protected String getTableName() {
        return Constants.TableName.PMTCT_EAC_VISIT;
    }

    @Override
    public void calculateActions(BasePmtctHomeVisitContract.View view, org.smartregister.chw.pmtct.domain.MemberObject memberObject, BasePmtctHomeVisitContract.InteractorCallBack callBack) {
        super.calculateActions(view, memberObject, callBack);
        try {
            PmtctVisitUtils.processVisits();
        } catch (Exception e) {
            Timber.e(e);
        }

        final Runnable runnable = () -> {
            final LinkedHashMap<String, BasePmtctHomeVisitAction> actionList = new LinkedHashMap<>();

            try {

                for (Map.Entry<String, BasePmtctHomeVisitAction> entry : flavor.calculateActions(view, memberObject, callBack).entrySet()) {
                    actionList.put(entry.getKey(), entry.getValue());
                }
            } catch (BasePmtctHomeVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }


}
