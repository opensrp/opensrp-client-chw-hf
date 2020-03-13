package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.core.fragment.CoreMalariaRegisterFragment;
import org.smartregister.chw.core.model.CoreMalariaRegisterFragmentModel;
import org.smartregister.chw.core.provider.ChwMalariaRegisterProvider;
import org.smartregister.chw.hf.activity.MalariaProfileActivity;
import org.smartregister.chw.hf.presenter.MalariaRegisterFragmentPresenter;
import org.smartregister.chw.hf.provider.HfMalariaRegisterProvider;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.view.activity.BaseRegisterActivity;

import java.util.Set;

public class MalariaRegisterFragment extends CoreMalariaRegisterFragment {

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        ChwMalariaRegisterProvider malariaRegisterProvider = new HfMalariaRegisterProvider(getActivity(), paginationViewHandler, registerActionHandler, visibleColumns);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, malariaRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new MalariaRegisterFragmentPresenter(this, new CoreMalariaRegisterFragmentModel(), viewConfigurationIdentifier);
    }

    @Override
    protected void openProfile(String baseEntityId) {
        MalariaProfileActivity.startMalariaActivity(getActivity(), baseEntityId);
    }
}
