package org.smartregister.chw.hf.fragment;

import static org.smartregister.chw.core.utils.CoreConstants.JSON_FORM.isMultiPartForm;

import android.content.Intent;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.chw.cdp.util.Constants;
import org.smartregister.chw.core.fragment.CoreOrdersRegisterFragment;
import org.smartregister.chw.hf.activity.HfJsonWizardFormActivity;
import org.smartregister.chw.hf.activity.OrderDetailsActivity;
import org.smartregister.chw.hf.domain.JSONObjectHolder;
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
            JSONObject jsonForm = model().getOrderFormAsJson(Constants.FORMS.CDP_CONDOM_ORDER_FACILITY);

            // Set the large JSONObject in JSONObjectHolder
            JSONObjectHolder.getInstance().setLargeJSONObject(jsonForm);

            Intent startFormIntent = new Intent(requireActivity(), HfJsonWizardFormActivity.class);
            startFormIntent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, getOrderForm(jsonForm));

            getActivity().startActivityForResult(startFormIntent, JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Form getOrderForm(JSONObject jsonForm) {
        Form form = new Form();
        form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
        form.setWizard(false);
        form.setHomeAsUpIndicator(org.smartregister.chw.core.R.mipmap.ic_cross_white);
        form.setSaveLabel(getResources().getString(org.smartregister.chw.core.R.string.submit));

        if (isMultiPartForm(jsonForm)) {
            form.setWizard(true);
            form.setNavigationBackground(org.smartregister.chw.core.R.color.family_navigation);
            form.setNextLabel(getResources().getString(org.smartregister.chw.core.R.string.next));
            form.setPreviousLabel(getResources().getString(org.smartregister.chw.core.R.string.back));
        }

        return form;
    }

}
