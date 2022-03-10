package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.pmtct.contract.HvlResultsFragmentContract;
import org.smartregister.chw.pmtct.presenter.BaseHvlResultsFragmentPresenter;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.chw.pmtct.util.DBConstants;

public class Cd4ResultsFragmentPresenter extends BaseHvlResultsFragmentPresenter {
    public Cd4ResultsFragmentPresenter(HvlResultsFragmentContract.View view, HvlResultsFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getMainCondition() {
        return " " + Constants.TABLES.PMTCT_FOLLOW_UP + "." + DBConstants.KEY.CD4_SAMPLE_ID + " IS NOT NULL ";
    }
}
