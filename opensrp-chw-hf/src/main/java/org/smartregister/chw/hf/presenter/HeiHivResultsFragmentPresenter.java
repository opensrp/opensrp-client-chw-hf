package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.contract.HvlResultsFragmentContract;
import org.smartregister.chw.pmtct.presenter.BaseHvlResultsFragmentPresenter;

public class HeiHivResultsFragmentPresenter extends BaseHvlResultsFragmentPresenter {
    public HeiHivResultsFragmentPresenter(HvlResultsFragmentContract.View view, HvlResultsFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getMainCondition() {
        return " " + Constants.TableName.HEI_FOLLOWUP + "." + Constants.DBConstants.HEI_HIV_SAMPLE_ID + " IS NOT NULL ";
    }

    @Override
    public String getMainTable() {
        return Constants.TableName.HEI_FOLLOWUP;
    }
}
