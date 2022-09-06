package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.cdp.contract.BaseOrdersRegisterFragmentContract;
import org.smartregister.chw.cdp.presenter.BaseOrdersRegisterFragmentPresenter;
import org.smartregister.chw.cdp.util.Constants;

public class RequestOrdersRegisterFragmentPresenter extends BaseOrdersRegisterFragmentPresenter {
    public RequestOrdersRegisterFragmentPresenter(BaseOrdersRegisterFragmentContract.View view, BaseOrdersRegisterFragmentContract.Model model) {
        super(view, model);
    }

    @Override
    public String getMainTable() {
        return Constants.TABLES.CDP_ORDERS_RECEIVE;
    }
}
