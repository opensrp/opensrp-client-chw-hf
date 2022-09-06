package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.core.fragment.CoreOrdersRegisterFragment;
import org.smartregister.chw.hf.activity.OrderDetailsActivity;
import org.smartregister.chw.hf.presenter.OrdersRegisterFragmentPresenter;
import org.smartregister.commonregistry.CommonPersonObjectClient;

public class OrdersRegisterFragment extends CoreOrdersRegisterFragment {

    @Override
    protected void initializePresenter() {
        presenter = new OrdersRegisterFragmentPresenter(this, model());
    }

    @Override
    public void showDetails(CommonPersonObjectClient cp) {
        OrderDetailsActivity.startMe(requireActivity(), cp);
    }
}
