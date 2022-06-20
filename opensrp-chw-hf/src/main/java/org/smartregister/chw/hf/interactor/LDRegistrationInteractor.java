package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.VisitUtils;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.contract.BaseLDVisitContract;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.interactor.BaseLDVisitInteractor;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.chw.ld.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * @author ilakozejumanne@gmail.com
 * 06/05/2022
 */
public class LDRegistrationInteractor extends BaseLDVisitInteractor {
    private Flavor flavor;
    private String encounterType = Constants.Events.LD_REGISTRATION;
    public LDRegistrationInteractor(String baseEntityId) {
        setFlavor(new LDRegistrationInteractorFlv(baseEntityId));
        flavor = new LDRegistrationInteractorFlv(baseEntityId);
    }

    public void setFalseLabourEncounterType() {
        encounterType = "False Labour Report";
    }

    @Override
    protected String getEncounterType() {
        return encounterType;
    }

    @Override
    protected String getTableName() {
        return org.smartregister.chw.ld.util.Constants.TABLES.LD_CONFIRMATION;
    }

    @Override
    public void calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) {
        // update the local database incase of manual date adjustment
        try {
            VisitUtils.processVisits();
        } catch (Exception e) {
            Timber.e(e);
        }

        final Runnable runnable = () -> {
            final LinkedHashMap<String, BaseLDVisitAction> actionList = new LinkedHashMap<>();

            try {

                for (Map.Entry<String, BaseLDVisitAction> entry : flavor.calculateActions(view, memberObject, callBack).entrySet()) {
                    actionList.put(entry.getKey(), entry.getValue());
                }
            } catch (BaseLDVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    public void setFlavor(LDRegistrationInteractor.Flavor flavor) {
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

    @Override
    protected void processExternalVisits(Visit visit, Map<String, BaseLDVisitAction> externalVisits, String memberID) throws Exception {
        super.processExternalVisits(visit, externalVisits, memberID);

        List<Visit> visits = new ArrayList<>(1);
        visits.add(visit);
        org.smartregister.chw.ld.util.VisitUtils.processVisits(visits, LDLibrary.getInstance().visitRepository(), LDLibrary.getInstance().visitDetailsRepository());
    }

    @Override
    public void submitVisit(boolean editMode, String memberID, Map<String, BaseLDVisitAction> map, BaseLDVisitContract.InteractorCallBack callBack) {
        if (map != null) {
            BaseLDVisitAction labourConfirmationAction = map.get("True Labour Confirmation");
            if (labourConfirmationAction != null) {
                boolean isTrueLabour = Boolean.parseBoolean(
                        JsonFormUtils.getFieldValue(labourConfirmationAction.getJsonPayload(),
                                "labour_confirmation"));

                if (!isTrueLabour) {
                    setFalseLabourEncounterType();
                }
            }
        }
        super.submitVisit(editMode, memberID, map, callBack);
    }
}
