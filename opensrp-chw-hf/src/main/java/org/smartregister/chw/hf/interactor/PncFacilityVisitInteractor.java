package org.smartregister.chw.hf.interactor;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.dao.PNCDao;
import org.smartregister.chw.core.interactor.CoreAncHomeVisitInteractor;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.PncVisitUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;

public class PncFacilityVisitInteractor extends CoreAncHomeVisitInteractor {
    private Flavor flavor;
    private String parentVisitID;

    public PncFacilityVisitInteractor() {
        setFlavor(new PncFacilityVisitInteractorFlv());
        flavor = new PncFacilityVisitInteractorFlv();
    }

    @Override
    protected String getEncounterType() {
        return Constants.Events.PNC_VISIT;
    }

    @Override
    protected String getTableName() {
        return Constants.TableName.PNC_FOLLOWUP;
    }

    @Override
    public void calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) {
        // update the local database incase of manual date adjustment
        try {
            PncVisitUtils.processVisits();
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

    @Override
    public MemberObject getMemberClient(String memberID) {
        return PNCDao.getMember(memberID);
    }

    @Override
    protected String getParentVisitEventID(Visit visit, String parentEventType) {
        if (StringUtils.isBlank(parentEventType))
            parentVisitID = visit.getVisitId();

        return visit.getVisitId().equalsIgnoreCase(parentVisitID) ? null : parentVisitID;
    }
}
