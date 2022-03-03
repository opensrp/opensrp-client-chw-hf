package org.smartregister.chw.hf.listener;

import android.app.Activity;
import android.view.View;

import org.jetbrains.annotations.Contract;
import org.smartregister.chw.hf.activity.CommunityFollowupFeedbackViewActivity;
import org.smartregister.chw.hf.model.ChwFollowupFeedbackDetailsModel;
import org.smartregister.commonregistry.CommonPersonObjectClient;

/**
 * Created by cozej4 on 6/21/20.
 *
 * @author cozej4 https://github.com/cozej4
 */

public class CommunityFollowupFeedbackRecyclerClickListener implements View.OnClickListener {
    private ChwFollowupFeedbackDetailsModel followupFeedbackDetailsModel;
    private Activity activity;
    private String startingActivity;
    private CommonPersonObjectClient commonPersonObjectClient;

    @Override
    public void onClick(View view) {
        CommunityFollowupFeedbackViewActivity.startCommunityFollowupFeedbackViewActivity(getActivity(), getCommonPersonObjectClient(), getFollowupFeedbackDetailsModel(), getStartingActivity());
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public ChwFollowupFeedbackDetailsModel getFollowupFeedbackDetailsModel() {
        return followupFeedbackDetailsModel;
    }

    public void setFollowupFeedbackDetailsModel(ChwFollowupFeedbackDetailsModel followupFeedbackDetailsModel) {
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
