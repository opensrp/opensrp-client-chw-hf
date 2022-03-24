package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.fragment.CorePncRegisterFragment;
import org.smartregister.chw.core.provider.ChwPncRegisterProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.activity.PncMemberProfileActivity;
import org.smartregister.chw.hf.model.HfPncRegisterFragmentModel;
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
        presenter = new PncRegisterFragmentPresenter(this, new HfPncRegisterFragmentModel(), null);
    }

    @Override
    protected void openHomeVisit(CommonPersonObjectClient client) {
        //Overridden
    }

    @Override
    protected void openPncMemberProfile(CommonPersonObjectClient client) {
        MemberObject memberObject = new MemberObject(client);
        PncMemberProfileActivity.startMe(getActivity(), memberObject.getBaseEntityId(),memberObject);
    }
}
