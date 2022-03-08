package org.smartregister.chw.hf.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.holder.ReferralCardViewHolder;
import org.smartregister.chw.hf.listener.CommunityFollowupFeedbackRecyclerClickListener;
import org.smartregister.chw.hf.listener.ReferralRecyclerClickListener;
import org.smartregister.chw.hf.model.HivTbReferralTasksAndFollowupFeedbackModel;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cozej4 on 6/21/20.
 *
 * @author cozej4 https://github.com/cozej4
 */
public class HivAndTbReferralCardViewAdapter extends RecyclerView.Adapter<ReferralCardViewHolder> {
    private List<HivTbReferralTasksAndFollowupFeedbackModel> tasksAndFollowupFeedbackModels;
    private CommonPersonObjectClient personObjectClient;
    private Activity context;
    private String startingActivity;

    public HivAndTbReferralCardViewAdapter(List<HivTbReferralTasksAndFollowupFeedbackModel> tasksAndFollowupFeedbackModels, Activity activity, CommonPersonObjectClient personObjectClient, String startingActivity) {
        this.tasksAndFollowupFeedbackModels = new ArrayList<>(tasksAndFollowupFeedbackModels);
        this.context = activity;
        this.personObjectClient = personObjectClient;
        this.startingActivity = startingActivity;
    }

    @NonNull
    @Override
    public ReferralCardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View referralLayout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.profile_referral_card_row, viewGroup, false);
        return new ReferralCardViewHolder(referralLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ReferralCardViewHolder referralCardViewHolder, int position) {
        HivTbReferralTasksAndFollowupFeedbackModel tasksAndFollowupFeedbackModel = tasksAndFollowupFeedbackModels.get(position);
        if (tasksAndFollowupFeedbackModel.getType().equals("TASK")) {
            ReferralRecyclerClickListener referralRecyclerClickListener = new ReferralRecyclerClickListener();
            referralRecyclerClickListener.setTask(tasksAndFollowupFeedbackModel.getTask());
            referralRecyclerClickListener.setCommonPersonObjectClient(personObjectClient);
            referralRecyclerClickListener.setActivity(context);
            referralRecyclerClickListener.setStartingActivity(startingActivity);
            referralCardViewHolder.textViewReferralHeader.setText(String.format(context.getApplicationContext().getResources().getString(R.string.referral_for), tasksAndFollowupFeedbackModel.getTask().getFocus()));
            referralCardViewHolder.referralRow.setOnClickListener(referralRecyclerClickListener);
        } else if (tasksAndFollowupFeedbackModel.getType().equals("FOLLOWUP_FEEDBACK")) {
            CommunityFollowupFeedbackRecyclerClickListener listener = new CommunityFollowupFeedbackRecyclerClickListener();
            listener.setActivity(context);
            listener.setFollowupFeedbackDetailsModel(tasksAndFollowupFeedbackModel.getFollowupFeedbackDetailsModel());
            listener.setStartingActivity(startingActivity);
            listener.setCommonPersonObjectClient(personObjectClient);
            referralCardViewHolder.textViewReferralHeader.setText(context.getApplicationContext().getResources().getString(R.string.followup_feedback));
            referralCardViewHolder.textViewReferralInfo.setText(context.getApplicationContext().getResources().getString(R.string.view_followup_feedback_info));
            referralCardViewHolder.referralRow.setOnClickListener(listener);
            referralCardViewHolder.textViewReferralHeader.setTextColor(context.getResources().getColor(R.color.accent));
            referralCardViewHolder.referralRowImage.setImageResource(R.drawable.ic_feedback);
        }
    }

    @Override
    public int getItemCount() {
        return tasksAndFollowupFeedbackModels.size();
    }
}
