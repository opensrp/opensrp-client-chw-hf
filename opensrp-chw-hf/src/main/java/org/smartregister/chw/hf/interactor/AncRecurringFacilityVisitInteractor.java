package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.core.interactor.CoreAncHomeVisitInteractor;
import org.smartregister.chw.hf.utils.Constants;

public class AncRecurringFacilityVisitInteractor extends CoreAncHomeVisitInteractor {
    public AncRecurringFacilityVisitInteractor() {
        setFlavor(new AncRecurringFacilityVisitInteractorFlv());
    }

    @Override
    protected String getEncounterType() {
        return Constants.Events.ANC_RECURRING_FACILITY_VISIT;
    }

    @Override
    protected String getTableName() {
        return Constants.Tables.ANC_RECURRING_FACILITY_VISIT;
    }
}
