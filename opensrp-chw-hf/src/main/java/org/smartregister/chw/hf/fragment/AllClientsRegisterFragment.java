package org.smartregister.chw.hf.fragment;

import android.view.View;

import androidx.annotation.NonNull;

import org.smartregister.chw.core.fragment.CoreAllClientsRegisterFragment;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.provider.HfOpdRegisterProvider;
import org.smartregister.chw.hf.utils.AllClientsUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;

public class AllClientsRegisterFragment extends CoreAllClientsRegisterFragment {

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        View dueOnlyLayout = view.findViewById(R.id.due_only_layout);
        dueOnlyLayout.setVisibility(View.GONE);
    }

    @Override
    protected void goToClientDetailActivity(@NonNull CommonPersonObjectClient commonPersonObjectClient) {
        AllClientsUtils.goToClientProfile(this.getActivity(), commonPersonObjectClient);
    }

    @Override
    public void initializeAdapter() {
        HfOpdRegisterProvider childRegisterProvider = new HfOpdRegisterProvider(getActivity(), registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, childRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }
}

