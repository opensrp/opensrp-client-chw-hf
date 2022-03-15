package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.contract.HvlResultsFragmentContract;
import org.smartregister.chw.pmtct.presenter.BaseHvlResultsFragmentPresenter;

public class HeiHivResultsFragmentPresenter extends BaseHvlResultsFragmentPresenter {
    private String baseEntityId;

    public HeiHivResultsFragmentPresenter(String baseEntityId, HvlResultsFragmentContract.View view, HvlResultsFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
        this.baseEntityId = baseEntityId;
    }

    @Override
    public String getMainCondition() {
        return " " + Constants.TableName.HEI_FOLLOWUP + "." + Constants.DBConstants.HEI_HIV_SAMPLE_ID + " IS NOT NULL " +
                " AND " + Constants.TableName.HEI_FOLLOWUP +".entity_id = '" + baseEntityId + "'";
    }

    @Override
    public String getMainTable() {
        return Constants.TableName.HEI_FOLLOWUP;
    }
}
