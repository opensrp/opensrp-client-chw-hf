package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.core.fragment.CorePncRegisterFragment;
import org.smartregister.chw.core.model.PncRegisterFragmentModel;
import org.smartregister.chw.core.provider.ChwPncRegisterProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.activity.PncMemberProfileActivity;
import org.smartregister.chw.hf.presenter.PncRegisterFragmentPresenter;
import org.smartregister.chw.hf.provider.HfPncRegisterProvider;
import org.smartregister.chw.hf.utils.HfReferralUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;

import java.util.Set;

public class PncRegisterFragment extends CorePncRegisterFragment {

    @Override
    public String getDueCondition() {
        return CoreConstants.TABLE_NAME.PNC_MEMBER + ".base_entity_id in ("
                + HfReferralUtils.getReferralDueFilter(CoreConstants.TABLE_NAME.PNC_MEMBER, CoreConstants.TASKS_FOCUS.PNC_DANGER_SIGNS)
                + ")";
    }

    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        ChwPncRegisterProvider provider = new HfPncRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, provider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new PncRegisterFragmentPresenter(this, new PncRegisterFragmentModel(), null);
    }

    @Override
    protected void openHomeVisit(CommonPersonObjectClient client) {
        //Overridden
    }

    @Override
    protected void openPncMemberProfile(CommonPersonObjectClient client) {
        PncMemberProfileActivity.startMe(getActivity(), client.getCaseId(), client);
    }
}
