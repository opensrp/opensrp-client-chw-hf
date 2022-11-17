package org.smartregister.chw.hf.fragment;

import static org.smartregister.chw.core.utils.FormUtils.getStartFormActivity;

import android.content.Intent;

import org.json.JSONObject;
import org.smartregister.chw.cdp.util.Constants;
import org.smartregister.chw.core.fragment.CoreOrdersRegisterFragment;
import org.smartregister.chw.hf.activity.OrderDetailsActivity;
import org.smartregister.chw.hf.presenter.OrdersRegisterFragmentPresenter;
import org.smartregister.chw.hf.utils.JsonFormUtils;
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

    @Override
    public void startOrderForm() {
        try {
            JSONObject form = model().getOrderFormAsJson(Constants.FORMS.CDP_CONDOM_ORDER_FACILITY);
            Intent startFormIntent = getStartFormActivity(form, null, requireActivity());
            requireActivity().startActivityForResult(startFormIntent, JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
