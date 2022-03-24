package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.core.fragment.CorePmtctRegisterFragment;
import org.smartregister.chw.hf.activity.PmtctProfileActivity;
import org.smartregister.chw.hf.activity.PmtctRegisterActivity;
import org.smartregister.chw.hf.model.PmtctRegisterFragmentModel;
import org.smartregister.chw.hf.presenter.PmtctRegisterFragmentPresenter;
import org.smartregister.chw.hf.provider.HfPmtctRegisterProvider;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;

import java.util.Set;

import timber.log.Timber;

public class PmtctRegisterFragment extends CorePmtctRegisterFragment {

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        HfPmtctRegisterProvider pmtctRegisterProvider = new HfPmtctRegisterProvider(getActivity(), paginationViewHandler, registerActionHandler, visibleColumns);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, pmtctRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        String viewConfigurationIdentifier = null;
        try {
            viewConfigurationIdentifier = ((PmtctRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        } catch (Exception e) {
            Timber.e(e);
        }

        presenter = new PmtctRegisterFragmentPresenter(this, new PmtctRegisterFragmentModel(), viewConfigurationIdentifier);

    }

    @Override
    protected void openProfile(String baseEntityId) {
        PmtctProfileActivity.startPmtctActivity(getActivity(), baseEntityId);
    }

    @Override
    protected void openFollowUpVisit(String baseEntityId) {
        //  PmtctFollowUpVisitActivity.startPmtctFollowUpActivity(getActivity(),baseEntityId);
    }
}
