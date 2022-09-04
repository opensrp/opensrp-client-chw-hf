package org.smartregister.chw.hf.fragment;

import static org.smartregister.chw.hf.utils.HfReferralUtils.REGISTER_TYPE;

import android.view.View;

import androidx.annotation.NonNull;

import org.smartregister.chw.core.fragment.CoreAllClientsRegisterFragment;
import org.smartregister.chw.core.utils.CoreConstants;
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
        String registerType = commonPersonObjectClient.getDetails().get(REGISTER_TYPE);

        if(registerType != null){
            switch (registerType){
                case CoreConstants.REGISTER_TYPE.HIV:
                    AllClientsUtils.goToHivProfile(this.getActivity(),commonPersonObjectClient);
                    break;
                case CoreConstants.REGISTER_TYPE.HTS:
                    AllClientsUtils.goToHTsProfile(this.getActivity(),commonPersonObjectClient);
                    break;
                case CoreConstants.REGISTER_TYPE.TB:
                    AllClientsUtils.goToTbProfile(this.getActivity(),commonPersonObjectClient);
                    break;
                default:
                    AllClientsUtils.goToClientProfile(this.getActivity(),commonPersonObjectClient);
                    break;
            }
        }else {
            AllClientsUtils.goToClientProfile(this.getActivity(), commonPersonObjectClient);
        }
    }

    @Override
    public void initializeAdapter() {
        HfOpdRegisterProvider childRegisterProvider = new HfOpdRegisterProvider(getActivity(), registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, childRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }
}

