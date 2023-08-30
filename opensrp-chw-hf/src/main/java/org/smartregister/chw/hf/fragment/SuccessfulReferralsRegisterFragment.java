package org.smartregister.chw.hf.fragment;

import android.os.Handler;
import android.view.View;

import org.smartregister.chw.core.fragment.BaseReferralRegisterFragment;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.ReferralsDetailsViewActivity;
import org.smartregister.chw.hf.model.LTFUSuccessfulModel;
import org.smartregister.chw.hf.presenter.ReferralFragmentPresenter;
import org.smartregister.chw.hf.provider.ReferralsRegisterProvider;
import org.smartregister.chw.referral.domain.MemberObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.domain.Task;
import org.smartregister.family.util.DBConstants;
import org.smartregister.repository.AllSharedPreferences;

import java.util.Set;

public class SuccessfulReferralsRegisterFragment extends BaseReferralRegisterFragment {

    public Handler handler = new Handler();
    private ReferralFragmentPresenter referralFragmentPresenter;
    private CommonPersonObjectClient commonPersonObjectClient;
    private String taskId;

    @Override
    public void setClient(CommonPersonObjectClient commonPersonObjectClient) {
        setCommonPersonObjectClient(commonPersonObjectClient);
    }

    @Override
    protected String getMainCondition() {
        AllSharedPreferences allSharedPreferences = Utils.getAllSharedPreferences();
        String anm = allSharedPreferences.fetchRegisteredANM();
        String currentLoaction = allSharedPreferences.fetchUserLocalityId(anm);
        return " ec_family_member_search.date_removed is null and task.location = '" + currentLoaction + "' and task.business_status = 'Complete' COLLATE NOCASE  ";
    }

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns, String tableName) {
        ReferralsRegisterProvider referralRegisterProvider = new ReferralsRegisterProvider(getActivity(), registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, referralRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected int getToolBarTitle() {
        return R.string.successful_ltfu_title;
    }

    @Override
    public CommonPersonObjectClient getCommonPersonObjectClient() {
        return commonPersonObjectClient;
    }

    @Override
    public void setCommonPersonObjectClient(CommonPersonObjectClient commonPersonObjectClient) {
        this.commonPersonObjectClient = commonPersonObjectClient;
    }

    @Override
    protected void initializePresenter() {
        referralFragmentPresenter = new ReferralFragmentPresenter(this, new LTFUSuccessfulModel());
        presenter = referralFragmentPresenter;

    }

    @Override
    protected void onViewClicked(View view) {
        CommonPersonObjectClient client = (CommonPersonObjectClient) view.getTag();
        referralFragmentPresenter.setBaseEntityId(Utils.getValue(client.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false));
        referralFragmentPresenter.fetchClient();

        Task task = getTask(Utils.getValue(client.getColumnmaps(), "_id", false));
        taskId = task.getIdentifier();
        referralFragmentPresenter.setTasksFocus(task.getFocus());
        goToReferralsDetails(client);

    }

    private Task getTask(String taskId) {
        return HealthFacilityApplication.getInstance().getTaskRepository().getTaskByIdentifier(taskId);
    }

    private void goToReferralsDetails(CommonPersonObjectClient client) {
        handler.postDelayed(() -> ReferralsDetailsViewActivity.startSuccessfulReferralDetailsViewActivity(getActivity(), new MemberObject(client), client, getTask(taskId)), 100);
    }

}