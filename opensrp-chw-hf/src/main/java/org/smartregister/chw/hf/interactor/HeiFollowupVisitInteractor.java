package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.core.interactor.CorePmtctHomeVisitInteractor;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.HeiVisitUtils;
import org.smartregister.chw.pmtct.contract.BasePmtctHomeVisitContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;

import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;

public class HeiFollowupVisitInteractor extends CorePmtctHomeVisitInteractor {
    private final Flavor flavor;

    public HeiFollowupVisitInteractor() {
        flavor = new HeiFollowupVisitInteractorFlv();
        setFlavor(new HeiFollowupVisitInteractorFlv());
    }


    @Override
    protected String getEncounterType() {
        return Constants.Events.HEI_FOLLOWUP;
    }

    @Override
    protected String getTableName() {
        return CoreConstants.TABLE_NAME.HEI;
    }

    @Override
    public void calculateActions(BasePmtctHomeVisitContract.View view, MemberObject memberObject, BasePmtctHomeVisitContract.InteractorCallBack callBack) {
        super.calculateActions(view, memberObject, callBack);
        try {
            HeiVisitUtils.processVisits();
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

    @Override
    public MemberObject getMemberClient(String memberID) {
        return HeiDao.getMember(memberID);
    }
}
