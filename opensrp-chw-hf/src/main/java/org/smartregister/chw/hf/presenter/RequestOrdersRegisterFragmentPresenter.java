package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.cdp.contract.BaseOrdersRegisterFragmentContract;
import org.smartregister.chw.cdp.presenter.BaseOrdersRegisterFragmentPresenter;
import org.smartregister.chw.cdp.util.Constants;
import org.smartregister.chw.cdp.util.DBConstants;

import static org.smartregister.util.Utils.getAllSharedPreferences;

public class RequestOrdersRegisterFragmentPresenter extends BaseOrdersRegisterFragmentPresenter {
    private final String userLocationTag = getAllSharedPreferences().fetchUserLocationTag();

    public RequestOrdersRegisterFragmentPresenter(BaseOrdersRegisterFragmentContract.View view, BaseOrdersRegisterFragmentContract.Model model) {
        super(view, model);
    }

    @Override
    public String getMainCondition() {
        if (userLocationTag.contains("msd_code")) {
            return super.getMainCondition() + " AND (" + getMainTable() + "." + DBConstants.KEY.REQUEST_TYPE + " = '" + Constants.ORDER_TYPES.FACILITY_TO_FACILITY_ORDER + "'" +
                                            " OR " + getMainTable() + "." + DBConstants.KEY.REQUEST_TYPE + " = '" + Constants.ORDER_TYPES.COMMUNITY_TO_FACILITY_ORDER + "') " ;
        }
        return super.getMainCondition() + " AND " + getMainTable() + "." + DBConstants.KEY.REQUEST_TYPE + " = '" + Constants.ORDER_TYPES.COMMUNITY_TO_FACILITY_ORDER + "'";
    }

}
