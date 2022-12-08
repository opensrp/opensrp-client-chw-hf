package org.smartregister.chw.hf.fragment;

import android.content.Intent;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONObject;
import org.smartregister.chw.cdp.provider.BaseReceivedOrdersRegisterProvider;
import org.smartregister.chw.cdp.util.Constants;
import org.smartregister.chw.core.fragment.CoreOrdersRegisterFragment;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.OrderRequestDetailsActivity;
import org.smartregister.chw.hf.presenter.RequestOrdersRegisterFragmentPresenter;
import org.smartregister.chw.hf.utils.JsonFormUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;

import static org.smartregister.chw.core.utils.FormUtils.getStartFormActivity;

public class RequestOrdersRegisterFragment extends CoreOrdersRegisterFragment {

    @Override
    protected int getFragmentTitle() {
        return R.string.menu_issue_condoms;
    }

    @Override
    protected TabLayout getTabLayout(View view) {
        TabLayout tabs = view.findViewById(R.id.requests_tab_layout);
        tabs.setVisibility(View.VISIBLE);
        return tabs;
    }

    @Override
    protected void refreshSyncProgressSpinner() {
        if (syncProgressBar != null) {
            syncProgressBar.setVisibility(View.GONE);
        }
        if (syncButton != null) {
            syncButton.setVisibility(View.VISIBLE);
            syncButton.setPadding(0, 0, 10, 0);
            syncButton.setImageDrawable(context().getDrawable(org.smartregister.cdp.R.drawable.ic_add_white_24));
            syncButton.setOnClickListener(view -> {
                startDistributionForm();
            });
        }
    }

    private void startDistributionForm() {
        try {
            JSONObject form = model().getDistributionFormAsJson(Constants.FORMS.CDP_CONDOM_DISTRIBUTION_WITHIN);
            Intent startFormIntent = getStartFormActivity(form, null, requireActivity());
            requireActivity().startActivityForResult(startFormIntent, JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initializePresenter() {
        presenter = new RequestOrdersRegisterFragmentPresenter(this, model());
    }

    @Override
    public void showDetails(CommonPersonObjectClient cp) {
        OrderRequestDetailsActivity.startMe(requireActivity(), cp);
    }


    @Override
    public void initializeAdapter(String tableName) {
        BaseReceivedOrdersRegisterProvider registerProvider = new BaseReceivedOrdersRegisterProvider(getActivity(), registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, registerProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }
}
