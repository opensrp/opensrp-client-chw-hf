package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.provider.ChwPncRegisterProvider;
import org.smartregister.chw.hf.activity.PncNoMotherProfileActivity;
import org.smartregister.chw.hf.model.PncNoMotherRegisterFragmentModel;
import org.smartregister.chw.hf.presenter.PncNoMotherRegisterFragmentPresenter;
import org.smartregister.chw.hf.provider.HfPncNoMotherRegisterProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;

import java.util.Set;

public class PncNoMotherRegisterFragment extends PncRegisterFragment {

    @Override
    public String getDueCondition() {
        return "";
    }

    @Override
    protected String getMainCondition() {
        return "";
    }

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        ChwPncRegisterProvider provider = new HfPncNoMotherRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, provider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new PncNoMotherRegisterFragmentPresenter(this, new PncNoMotherRegisterFragmentModel(), null);
    }

    @Override
    protected void openPncMemberProfile(CommonPersonObjectClient client) {
        MemberObject memberObject = new MemberObject(client);
        PncNoMotherProfileActivity.startMe(getActivity(), memberObject.getBaseEntityId(), memberObject, client);
    }
}
