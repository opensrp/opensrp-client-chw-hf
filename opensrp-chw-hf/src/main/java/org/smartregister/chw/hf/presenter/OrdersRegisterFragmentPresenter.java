package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.cdp.contract.BaseOrdersRegisterFragmentContract;
import org.smartregister.chw.cdp.presenter.BaseOrdersRegisterFragmentPresenter;
import org.smartregister.chw.cdp.util.Constants;
import org.smartregister.chw.cdp.util.DBConstants;

public class OrdersRegisterFragmentPresenter extends BaseOrdersRegisterFragmentPresenter {
    public OrdersRegisterFragmentPresenter(BaseOrdersRegisterFragmentContract.View view, BaseOrdersRegisterFragmentContract.Model model) {
        super(view, model);
    }

    @Override
    public String getMainCondition() {
        String providerId = org.smartregister.Context.getInstance().allSharedPreferences().fetchRegisteredANM();
        String userLocationId = org.smartregister.Context.getInstance().allSharedPreferences().fetchUserLocalityId(providerId);

        return super.getMainCondition() + " AND " + getMainTable() + "." + DBConstants.KEY.REQUEST_TYPE + " = '" + Constants.ORDER_TYPES.FACILITY_TO_FACILITY_ORDER + "'" +
                " AND " + getMainTable() + "." + DBConstants.KEY.LOCATION_ID + " = " + "'" + userLocationId + "'";
    }

}
