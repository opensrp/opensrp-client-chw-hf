package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.core.fragment.CoreVmmcRegisterFragment;
import org.smartregister.chw.core.model.CoreVmmcRegisterFragmentModel;
import org.smartregister.chw.core.provider.ChwVmmcRegisterProvider;
import org.smartregister.chw.hf.activity.VmmcProfileActivity;
import org.smartregister.chw.hf.presenter.VmmcRegisterFragmentPresenter;
import org.smartregister.chw.hf.provider.HfVmmcRegisterProvider;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.view.activity.BaseRegisterActivity;

import java.util.Set;

public class VmmcRegisterFragment extends CoreVmmcRegisterFragment {

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        ChwVmmcRegisterProvider vmmcRegisterProvider = new HfVmmcRegisterProvider(getActivity(), paginationViewHandler, registerActionHandler, visibleColumns);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, vmmcRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new VmmcRegisterFragmentPresenter(this, new CoreVmmcRegisterFragmentModel(), viewConfigurationIdentifier);
    }

    @Override
    protected void openProfile(String baseEntityId) {
        VmmcProfileActivity.startVmmcActivity(getActivity(), baseEntityId);
    }
}