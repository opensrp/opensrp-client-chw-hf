package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.core.fragment.CoreOrdersRegisterFragment;
import org.smartregister.chw.hf.presenter.OrdersRegisterFragmentPresenter;

public class OrdersRegisterFragment extends CoreOrdersRegisterFragment {

    @Override
    protected void initializePresenter() {
        presenter = new OrdersRegisterFragmentPresenter(this, model());
    }
}
