package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.pmtct.contract.HvlResultsFragmentContract;
import org.smartregister.chw.pmtct.presenter.BaseHvlResultsFragmentPresenter;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.chw.pmtct.util.DBConstants;

public class Cd4ResultsFragmentPresenter extends BaseHvlResultsFragmentPresenter {
    private String baseEntityId;
    public Cd4ResultsFragmentPresenter(String baseEntityId, HvlResultsFragmentContract.View view, HvlResultsFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
        this.baseEntityId = baseEntityId;
    }

    @Override
    public String getMainCondition() {
        return " " + Constants.TABLES.PMTCT_FOLLOW_UP + "." + DBConstants.KEY.CD4_SAMPLE_ID + " IS NOT NULL " +
                " AND " + Constants.TABLES.PMTCT_FOLLOW_UP + "." + DBConstants.KEY.ENTITY_ID + " = '"+ baseEntityId+ "'" ;
    }
}
