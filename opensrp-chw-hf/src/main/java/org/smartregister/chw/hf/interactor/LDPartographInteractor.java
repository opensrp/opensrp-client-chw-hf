package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.VisitUtils;
import org.smartregister.chw.ld.contract.BaseLDVisitContract;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.interactor.BaseLDVisitInteractor;
import org.smartregister.chw.ld.model.BaseLDVisitAction;

import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * @author issyzac 5/7/22
 */
public class LDPartographInteractor extends BaseLDVisitInteractor {

    private Flavor flavor;

    public LDPartographInteractor(String baseEntityId){
        setFlavor(new LDPartographInteractorFlv());
        flavor = new LDPartographInteractorFlv();
    }

    @Override
    public void calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) {

        try {
            VisitUtils.processVisits();
        }catch (Exception e){
            Timber.e(e);
        }

        final Runnable runnable = () -> {
            final LinkedHashMap<String, BaseLDVisitAction> actionList = new LinkedHashMap<>();
            try {
                for (Map.Entry<String, BaseLDVisitAction> entry : flavor.calculateActions(view, memberObject, callBack).entrySet()){
                    actionList.put(entry.getKey(), entry.getValue());
                }
            }catch (BaseLDVisitAction.ValidationException e){
                Timber.e(e);
            }
            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    protected String getEncounterType() {
        return Constants.Events.LD_PARTOGRAPHY;
    }

    @Override
    protected String getTableName() {
        return org.smartregister.chw.ld.util.Constants.TABLES.LD_CONFIRMATION;
    }

    public void setFlavor(LDPartographInteractor.Flavor flavor) {
        this.flavor = flavor;
    }

    public interface Flavor {
        LinkedHashMap<String, BaseLDVisitAction> calculateActions(final BaseLDVisitContract.View view, MemberObject memberObject, final BaseLDVisitContract.InteractorCallBack callBack) throws BaseLDVisitAction.ValidationException;
    }

    @Override
    public MemberObject getMemberClient(String memberID) {
        MemberObject memberObject = new MemberObject();
        memberObject.setBaseEntityId(memberID);
        return memberObject;
    }
}
