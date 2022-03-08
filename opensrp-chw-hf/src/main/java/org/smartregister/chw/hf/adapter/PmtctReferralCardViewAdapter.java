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
import org.smartregister.chw.hf.model.PmtctFollowupFeedbackModel;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cozej4 on 3/3/22.
 *
 * @author cozej4 https://github.com/cozej4
 */
public class PmtctReferralCardViewAdapter extends RecyclerView.Adapter<ReferralCardViewHolder> {
    private List<PmtctFollowupFeedbackModel> pmtctFollowupFeedbackModels;
    private CommonPersonObjectClient personObjectClient;
    private Activity context;
    private String startingActivity;

    public PmtctReferralCardViewAdapter(List<PmtctFollowupFeedbackModel> pmtctFollowupFeedbackModels, Activity activity, CommonPersonObjectClient personObjectClient, String startingActivity) {
        this.pmtctFollowupFeedbackModels = new ArrayList<>(pmtctFollowupFeedbackModels);
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
        PmtctFollowupFeedbackModel pmtctFollowupFeedbackModel = pmtctFollowupFeedbackModels.get(position);

        CommunityFollowupFeedbackRecyclerClickListener listener = new CommunityFollowupFeedbackRecyclerClickListener();
        listener.setActivity(context);
        listener.setFollowupFeedbackDetailsModel(pmtctFollowupFeedbackModel.getFollowupFeedbackDetailsModel());
        listener.setStartingActivity(startingActivity);
        listener.setCommonPersonObjectClient(personObjectClient);
        referralCardViewHolder.textViewReferralHeader.setText(context.getApplicationContext().getResources().getString(R.string.followup_feedback));
        referralCardViewHolder.textViewReferralInfo.setText(context.getApplicationContext().getResources().getString(R.string.view_followup_feedback_info));
        referralCardViewHolder.referralRow.setOnClickListener(listener);
        referralCardViewHolder.textViewReferralHeader.setTextColor(context.getResources().getColor(R.color.accent));
        referralCardViewHolder.referralRowImage.setImageResource(R.drawable.ic_feedback);
    }

    @Override
    public int getItemCount() {
        return pmtctFollowupFeedbackModels.size();
    }
}
