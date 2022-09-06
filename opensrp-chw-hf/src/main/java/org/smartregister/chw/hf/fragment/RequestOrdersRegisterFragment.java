package org.smartregister.chw.hf.fragment;

import android.view.View;

import com.google.android.material.tabs.TabLayout;

import org.smartregister.chw.core.fragment.CoreOrdersRegisterFragment;
import org.smartregister.chw.hf.R;

public class RequestOrdersRegisterFragment extends CoreOrdersRegisterFragment {

    @Override
    protected int getFragmentTitle() {
        return R.string.menu_cdp;
    }

    @Override
    protected void setUpTabLayout(View view) {
        TabLayout tabs = view.findViewById(R.id.requests_tab_layout);
        view.findViewById(R.id.tab_layout).setVisibility(View.GONE);
        tabs.setVisibility(View.VISIBLE);
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
}
