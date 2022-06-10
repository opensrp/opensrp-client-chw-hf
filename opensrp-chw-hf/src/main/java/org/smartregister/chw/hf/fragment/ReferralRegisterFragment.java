package org.smartregister.chw.hf.fragment;

import android.os.Handler;
import android.view.View;

import org.smartregister.chw.core.fragment.BaseReferralRegisterFragment;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.ReferralTaskViewActivity;
import org.smartregister.chw.hf.model.ReferralModel;
import org.smartregister.chw.hf.presenter.ReferralFragmentPresenter;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.family.util.DBConstants;
import org.smartregister.repository.AllSharedPreferences;

public class ReferralRegisterFragment extends BaseReferralRegisterFragment {

    public Handler handler = new Handler();
    private ReferralFragmentPresenter referralFragmentPresenter;
    private CommonPersonObjectClient commonPersonObjectClient;

    @Override
    public void setClient(CommonPersonObjectClient commonPersonObjectClient) {
        setCommonPersonObjectClient(commonPersonObjectClient);
    }

    @Override
    protected String getMainCondition() {
        AllSharedPreferences allSharedPreferences = Utils.getAllSharedPreferences();
        String anm = allSharedPreferences.fetchRegisteredANM();
        String currentLoaction = allSharedPreferences.fetchUserLocalityId(anm);
        return "task.business_status = '" + CoreConstants.BUSINESS_STATUS.REFERRED + "' and  ec_family_member_search.date_removed is null and task.location <> '" + currentLoaction + "' AND task.focus <> 'LTFU' ";
    }

    @Override
    protected int getToolBarTitle() {
        return R.string.received_referrals;
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
        referralFragmentPresenter = new ReferralFragmentPresenter(this, new ReferralModel());
        presenter = referralFragmentPresenter;

    }

    @Override
    protected void onViewClicked(View view) {
        CommonPersonObjectClient client = (CommonPersonObjectClient) view.getTag();
        referralFragmentPresenter.setBaseEntityId(Utils.getValue(client.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false));
        referralFragmentPresenter.fetchClient();

        Task task = getTask(Utils.getValue(client.getColumnmaps(), "_id", false));
        referralFragmentPresenter.setTasksFocus(task.getFocus());
        goToReferralsDetails(client);

    }

    private Task getTask(String taskId) {
        return HealthFacilityApplication.getInstance().getTaskRepository().getTaskByIdentifier(taskId);
    }

    private void goToReferralsDetails(CommonPersonObjectClient client) {
        handler.postDelayed(() -> ReferralTaskViewActivity.startReferralTaskViewActivity(getActivity(), getCommonPersonObjectClient(), getTask(Utils.getValue(client.getColumnmaps(), "_id", false)), CoreConstants.REGISTERED_ACTIVITIES.REFERRALS_REGISTER_ACTIVITY), 100);
    }

}