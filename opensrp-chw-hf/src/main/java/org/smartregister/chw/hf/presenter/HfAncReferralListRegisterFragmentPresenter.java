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
        return " (" +
                " ec_anc_register.task_id IS NOT NULL AND "+
                " ec_anc_register.task_id  NOT LIKE '%' || task._id || '%' " +
                " AND ec_anc_register.confirmation_status <> 'Confirmed' " +
                " AND focus='Pregnancy Confirmation' " +
                ") "
                + " OR (focus='Pregnancy Confirmation' AND ec_anc_register.base_entity_id IS NULL) ";
    }


}
