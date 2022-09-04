package org.smartregister.chw.hf.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.holder.ReferralCardViewHolder;
import org.smartregister.chw.hf.listener.CommunityIndexFollowupFeedbackRecyclerClickListener;
import org.smartregister.chw.hf.model.HivIndexFollowupFeedbackDetailsModel;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.util.ArrayList;
import java.util.List;

public class HivIndexFollowupCardViewAdapter extends RecyclerView.Adapter<ReferralCardViewHolder>{
    private List<HivIndexFollowupFeedbackDetailsModel> tasksAndFollowupFeedbackModel;
    private CommonPersonObjectClient personObjectClient;
    private Activity context;
    private String startingActivity;

    public HivIndexFollowupCardViewAdapter(List<HivIndexFollowupFeedbackDetailsModel> tasksAndFollowupFeedbackModel, Activity activity, CommonPersonObjectClient personObjectClient, String startingActivity) {
        this.tasksAndFollowupFeedbackModel = new ArrayList<>(tasksAndFollowupFeedbackModel);
        this.context = activity;
        this.personObjectClient = personObjectClient;
        this.startingActivity = startingActivity;
    }


    @NonNull
    @Override
    public ReferralCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View referralLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_referral_card_row, parent, false);
        return new ReferralCardViewHolder(referralLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ReferralCardViewHolder holder, int position) {
        HivIndexFollowupFeedbackDetailsModel tasksAndFollowupFeedbackList = tasksAndFollowupFeedbackModel.get(position);

        CommunityIndexFollowupFeedbackRecyclerClickListener listener = new CommunityIndexFollowupFeedbackRecyclerClickListener();
        listener.setCommonPersonObjectClient(personObjectClient);
        listener.setFollowupFeedbackDetailsModel(tasksAndFollowupFeedbackList);
        listener.setActivity(context);
        listener.setStartingActivity(startingActivity);
        holder.textViewReferralHeader.setText(context.getApplicationContext().getResources().getString(R.string.followup_feedback));
        holder.textViewReferralInfo.setText(context.getApplicationContext().getResources().getString(R.string.view_followup_feedback_info));
        holder.referralRow.setOnClickListener(listener);
        holder.textViewReferralHeader.setTextColor(context.getResources().getColor(R.color.accent));
        holder.referralRowImage.setImageResource(R.drawable.ic_feedback);
    }

    @Override
    public int getItemCount() {
        return tasksAndFollowupFeedbackModel.size();
    }
}
