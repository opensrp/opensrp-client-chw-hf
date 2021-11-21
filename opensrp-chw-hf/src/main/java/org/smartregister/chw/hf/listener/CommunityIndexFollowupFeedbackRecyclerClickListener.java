package org.smartregister.chw.hf.listener;

import android.app.Activity;
import android.view.View;

import org.jetbrains.annotations.Contract;
import org.smartregister.chw.hf.activity.CommunityIndexFollowupFeedbackViewActivity;
import org.smartregister.chw.hf.model.HivIndexFollowupFeedbackDetailsModel;
import org.smartregister.commonregistry.CommonPersonObjectClient;

public class CommunityIndexFollowupFeedbackRecyclerClickListener implements View.OnClickListener {
    private HivIndexFollowupFeedbackDetailsModel followupFeedbackDetailsModel;
    private Activity activity;
    private String startingActivity;
    private CommonPersonObjectClient commonPersonObjectClient;

    @Override
    public void onClick(View view) {
        CommunityIndexFollowupFeedbackViewActivity.startCommunityIndexFollowupFeedbackViewActivity(getActivity(), getCommonPersonObjectClient(),getStartingActivity(),getFollowupFeedbackDetailsModel());
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public HivIndexFollowupFeedbackDetailsModel getFollowupFeedbackDetailsModel() {
        return followupFeedbackDetailsModel;
    }

    public void setFollowupFeedbackDetailsModel(HivIndexFollowupFeedbackDetailsModel followupFeedbackDetailsModel) {
        this.followupFeedbackDetailsModel = followupFeedbackDetailsModel;
    }

    @Contract(pure = true)
    private String getStartingActivity() {
        return startingActivity;
    }

    public void setStartingActivity(String startingActivity) {
        this.startingActivity = startingActivity;
    }

    @Contract(pure = true)
    private CommonPersonObjectClient getCommonPersonObjectClient() {
        return commonPersonObjectClient;
    }

    public void setCommonPersonObjectClient(CommonPersonObjectClient commonPersonObjectClient) {
        this.commonPersonObjectClient = commonPersonObjectClient;
    }
}
