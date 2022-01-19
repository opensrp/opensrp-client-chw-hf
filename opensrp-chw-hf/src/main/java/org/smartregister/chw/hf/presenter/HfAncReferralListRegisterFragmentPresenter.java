package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.anc.contract.BaseAncRegisterFragmentContract;
import org.smartregister.chw.core.presenter.AncRegisterFragmentPresenter;

public class HfAncReferralListRegisterFragmentPresenter extends AncRegisterFragmentPresenter {
    public HfAncReferralListRegisterFragmentPresenter(BaseAncRegisterFragmentContract.View view, BaseAncRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getDefaultSortQuery() {
        return " task.last_modified DESC ";
    }

    @Override
    public void initializeQueries(String mainCondition) {
        String tableName = getMainTable();

        String countSelect = model.countSelect(tableName, getMainCondition());
        String mainSelect = model.mainSelect(tableName, getMainCondition());

        getView().initializeQueryParams(tableName, countSelect, mainSelect);
        getView().initializeAdapter(visibleColumns);

        getView().countExecute();
        getView().filterandSortInInitializeQueries();
    }

    @Override
    public String getMainTable() {
        return "ec_family_member";
    }

    @Override
    public String getMainCondition() {
        return " ec_family_member.base_entity_id NOT  IN (SELECT base_entity_id FROM ec_anc_register ) AND focus='Pregnancy Confirmation'";
    }


}
