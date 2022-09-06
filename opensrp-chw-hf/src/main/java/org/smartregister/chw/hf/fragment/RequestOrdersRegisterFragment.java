package org.smartregister.chw.hf.fragment;

import android.view.View;

import com.google.android.material.tabs.TabLayout;

import org.smartregister.chw.core.fragment.CoreOrdersRegisterFragment;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.OrderDetailsActivity;
import org.smartregister.chw.hf.presenter.RequestOrdersRegisterFragmentPresenter;
import org.smartregister.commonregistry.CommonPersonObjectClient;

public class RequestOrdersRegisterFragment extends CoreOrdersRegisterFragment {

    @Override
    protected int getFragmentTitle() {
        return R.string.menu_cdp;
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
            syncProgressBar.setVisibility(android.view.View.GONE);
        }
        if (syncButton != null) {
            syncButton.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    protected void initializePresenter() {
        presenter = new RequestOrdersRegisterFragmentPresenter(this, model());
    }

    @Override
    public void showDetails(CommonPersonObjectClient cp) {
        OrderDetailsActivity.startMe(requireActivity(), cp);
    }
}
