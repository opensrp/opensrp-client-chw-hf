package org.smartregister.chw.hf.presenter;

import org.jetbrains.annotations.NotNull;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.tb.contract.BaseTbRegisterFragmentContract;
import org.smartregister.chw.tb.presenter.BaseTbCommunityFollowupPresenter;
import org.smartregister.chw.tb.util.Constants.Tables;
import org.smartregister.chw.tb.util.DBConstants;

public class TbFollowupFragmentPresenter extends BaseTbCommunityFollowupPresenter {

    public TbFollowupFragmentPresenter(BaseTbRegisterFragmentContract.View view, BaseTbRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    @NotNull
    public String getMainCondition() {
        return " " + CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.Key.DATE_REMOVED + " is null " +
                "AND " + Tables.TB_COMMUNITY_FOLLOWUP + "." + DBConstants.Key.IS_CLOSED + " = '0' " +
                "AND " + Tables.TB_COMMUNITY_FOLLOWUP + "." + DBConstants.Key.BASE_ENTITY_ID + " NOT IN (SELECT " + DBConstants.Key.COMMUNITY_REFERRAL_FORM_ID + " FROM " + Tables.TB_COMMUNITY_FEEDBACK + "  WHERE mark_as_done = '1' ) ";

    }

    @Override
    @NotNull
    public String getDueFilterCondition() {
        return " " + CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.Key.DATE_REMOVED + " is null " +
                "AND " + Tables.TB_COMMUNITY_FOLLOWUP + "." + DBConstants.Key.IS_CLOSED + " = '0' " +
                "AND " + Tables.TB_COMMUNITY_FOLLOWUP + "." + DBConstants.Key.BASE_ENTITY_ID + " IN (SELECT " + DBConstants.Key.COMMUNITY_REFERRAL_FORM_ID + " FROM " + Tables.TB_COMMUNITY_FEEDBACK + " ) ";

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
        return Tables.TB_COMMUNITY_FOLLOWUP;
    }
}
