package org.smartregister.chw.hf.presenter;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hiv.contract.BaseHivRegisterFragmentContract;
import org.smartregister.chw.hiv.presenter.BaseHivRegisterFragmentPresenter;
import org.smartregister.chw.hiv.util.DBConstants;

public class HtsRegisterFragmentPresenter extends BaseHivRegisterFragmentPresenter {

    public HtsRegisterFragmentPresenter(BaseHivRegisterFragmentContract.View view, BaseHivRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    @NotNull
    public String getMainCondition() {
        return " " + CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.Key.DATE_REMOVED + " is null " +
                "AND " + CoreConstants.TABLE_NAME.HTS_MEMBERS + "." + DBConstants.Key.CTC_NUMBER + " IS NULL " +
                "AND " + CoreConstants.TABLE_NAME.HTS_MEMBERS + "." + Constants.DBConstants.CHW_REFERRAL_SERVICE + " = 'Conventional HIV Test' " +
                "AND " + CoreConstants.TABLE_NAME.HTS_MEMBERS + "." + DBConstants.Key.CLIENT_HIV_STATUS_AFTER_TESTING + " is NULL " +
                "AND " + CoreConstants.TABLE_NAME.HTS_MEMBERS + "." + DBConstants.Key.IS_CLOSED + " = '0' " +
                "AND " + CoreConstants.TABLE_NAME.HTS_MEMBERS + "." + DBConstants.Key.BASE_ENTITY_ID +
                " NOT IN (SELECT base_entity_id FROM " + org.smartregister.chw.hiv.util.Constants.Tables.HIV_INDEX_HF + ")";
    }

    @Override
    @NotNull
    public String getDueFilterCondition() {
        return CoreConstants.TABLE_NAME.HTS_MEMBERS + ".base_entity_id IN (SELECT for FROM task WHERE business_status = 'Referred')";
    }

    @NonNull
    @Override
    public String getDefaultSortQuery() {
        return CoreConstants.TABLE_NAME.HTS_MEMBERS + "." + org.smartregister.chw.hiv.util.DBConstants.Key.HIV_REGISTRATION_DATE + " DESC ";
    }

    @Override
    public void processViewConfigurations() {
        super.processViewConfigurations();
        if (getConfig().getSearchBarText() != null && getView() != null) {
            getView().updateSearchBarHint(getView().getContext().getString(R.string.search_name_or_id));
        }
    }

    @Override
    public String getMainTable() {
        return CoreConstants.TABLE_NAME.HTS_MEMBERS;
    }
}
