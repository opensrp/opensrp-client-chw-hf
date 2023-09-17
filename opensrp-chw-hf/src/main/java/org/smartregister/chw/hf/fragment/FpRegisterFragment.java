package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.core.fragment.CoreFpRegisterFragment;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.activity.FpMemberProfileActivity;
import org.smartregister.chw.hf.model.FpRegisterFragmentModel;
import org.smartregister.chw.hf.presenter.FpRegisterFragmentPresenter;
import org.smartregister.chw.hf.provider.HfFpRegisterProvider;
import org.smartregister.chw.hf.utils.HfReferralUtils;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;

import java.util.Set;

public class FpRegisterFragment extends CoreFpRegisterFragment {

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        HfFpRegisterProvider fpRegisterProvider = new HfFpRegisterProvider(getActivity(), visibleColumns, registerActionHandler, paginationViewHandler);
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
    public String getDueCondition() {
        return " " + CoreConstants.TABLE_NAME.FP_MEMBER + ".base_entity_id in ("
                + HfReferralUtils.getReferralDueFilter(CoreConstants.TABLE_NAME.FP_MEMBER, CoreConstants.TASKS_FOCUS.FP_SIDE_EFFECTS)
                + ")";
    }

    @Override
    protected void openProfile(String baseEntityId) {
        FpMemberProfileActivity.startFpMemberProfileActivity(getActivity(), baseEntityId);
    }

    @Override
    protected void openFollowUpVisit(String baseEntityId) {
        super.openFollowUpVisit(baseEntityId);
    }

    @Override
    protected void onViewClicked(android.view.View view) {
        super.onViewClicked(view);

        if (view.getId() == org.smartregister.chw.core.R.id.due_only_layout) {
            toggleFilterSelection(view);
        }
    }
}
