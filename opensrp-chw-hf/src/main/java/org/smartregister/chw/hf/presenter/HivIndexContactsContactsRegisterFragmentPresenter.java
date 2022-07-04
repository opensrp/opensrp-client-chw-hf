package org.smartregister.chw.hf.presenter;

import org.jetbrains.annotations.NotNull;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hiv.contract.BaseHivRegisterFragmentContract;
import org.smartregister.chw.hiv.presenter.BaseHivIndexContactsRegisterFragmentPresenter;
import org.smartregister.chw.hiv.util.Constants.Tables;
import org.smartregister.chw.hiv.util.DBConstants;

public class HivIndexContactsContactsRegisterFragmentPresenter extends BaseHivIndexContactsRegisterFragmentPresenter {

    public HivIndexContactsContactsRegisterFragmentPresenter(BaseHivRegisterFragmentContract.View view, BaseHivRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    @NotNull
    public String getMainCondition() {
        return " " +
                Tables.HIV_INDEX_HF + "." + DBConstants.Key.CTC_NUMBER + " IS NULL AND (" +
                Tables.HIV_INDEX_HF + "." + DBConstants.Key.TEST_RESULTS + " IS NULL OR " +
                Tables.HIV_INDEX_HF + "." + DBConstants.Key.TEST_RESULTS + " <> 'Negative' COLLATE NOCASE)  AND " +
                Tables.HIV_INDEX_HF + "." + DBConstants.Key.HOW_TO_NOTIFY_CONTACT_CLIENT + " <> 'na' COLLATE NOCASE";

    }

    @Override
    @NotNull
    public String getDueFilterCondition() {
        return " " + Tables.HIV_INDEX_HF + ".base_entity_id IN (SELECT base_entity_id FROM ec_hiv_index_chw_followup WHERE followed_by_chw = 'true' )" ;
    }

    @Override
    public void processViewConfigurations() {
        super.processViewConfigurations();
        if (getConfig().getSearchBarText() != null && getView() != null) {
            getView().updateSearchBarHint(getView().getContext().getString(R.string.search_name_or_id));
        }


        if (getConfig().getFilterFields() != null && getView() != null) {
            getView().updateSearchBarHint(getView().getContext().getString(R.string.search_name_or_id));
        }
    }

    @Override
    public String getMainTable() {
        return Tables.HIV_INDEX_HF;
    }
}
