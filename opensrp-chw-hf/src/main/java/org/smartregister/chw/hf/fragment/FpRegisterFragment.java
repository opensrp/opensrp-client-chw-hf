package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.core.fragment.CoreFpRegisterFragment;
import org.smartregister.chw.core.provider.CoreFpProvider;
import org.smartregister.chw.hf.model.FpRegisterFragmentModel;
import org.smartregister.chw.hf.presenter.FpRegisterFragmentPresenter;
import org.smartregister.chw.hf.provider.HfFpRegisterProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;

import java.util.Set;

public class FpRegisterFragment extends CoreFpRegisterFragment {

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        CoreFpProvider fpRegisterProvider = new HfFpRegisterProvider(getActivity(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, fpRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new FpRegisterFragmentPresenter(this, new FpRegisterFragmentModel());
    }

    @Override
    protected void openProfile(CommonPersonObjectClient client) {
        // TODO -> FamilyPlanningMemberProfileActivity.startFpMemberProfileActivity(getActivity(), FpDao.getMember(client.getCaseId()));
    }
}
