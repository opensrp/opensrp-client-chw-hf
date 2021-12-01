package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.core.interactor.CorePmtctHomeVisitInteractor;
import org.smartregister.chw.hf.utils.Constants;

public class EacVisitInteractor extends CorePmtctHomeVisitInteractor {
    private final Flavor flavor;
    public EacVisitInteractor(){
        setFlavor(new EacVisitInteractorFlv());
        flavor = new EacVisitInteractorFlv();
    }


    @Override
    protected String getEncounterType() {
        return Constants.Events.PMTCT_EAC_VISIT;
    }

    @Override
    protected String getTableName() {
        return Constants.Tables.PMTCT_EAC_VISIT;
    }
}
