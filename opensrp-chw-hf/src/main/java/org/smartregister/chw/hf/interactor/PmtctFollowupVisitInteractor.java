package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.core.interactor.CorePmtctHomeVisitInteractor;
import org.smartregister.chw.pmtct.util.Constants;

public class PmtctFollowupVisitInteractor extends CorePmtctHomeVisitInteractor {
    public PmtctFollowupVisitInteractor() {
        setFlavor(new PmtctFollowupVisitInteractorFlv());
    }


    @Override
    protected String getEncounterType() {
        return Constants.EVENT_TYPE.PMTCT_FOLLOWUP;
    }

    @Override
    protected String getTableName() {
        return Constants.TABLES.PMTCT_FOLLOW_UP;
    }


}
