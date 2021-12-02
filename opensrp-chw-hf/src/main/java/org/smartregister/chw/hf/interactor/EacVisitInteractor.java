package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.core.interactor.CorePmtctHomeVisitInteractor;
import org.smartregister.chw.hf.utils.Constants;

public class EacVisitInteractor extends CorePmtctHomeVisitInteractor {
    public EacVisitInteractor(){
        setFlavor(new EacVisitInteractorFlv());
    }


    @Override
    protected String getEncounterType() {
        return Constants.Events.PMTCT_EAC_VISIT;
    }

    @Override
    protected String getTableName() {
        return Constants.TableName.PMTCT_EAC_VISIT;
    }
}
