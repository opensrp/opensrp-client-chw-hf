package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.core.interactor.CoreAncHomeVisitInteractor;
import org.smartregister.chw.hf.utils.Constants;

public class AncFirstFacilityVisitInteractor extends CoreAncHomeVisitInteractor {
    public AncFirstFacilityVisitInteractor() {
        setFlavor(new AncFirstFacilityVisitInteractorFlv());
    }

    @Override
    protected String getEncounterType() {
        return Constants.Events.ANC_FIRST_FACILITY_VISIT;
    }

    @Override
    protected String getTableName() {
        return Constants.Tables.ANC_FIRST_FACILITY_VISIT;
    }
}
