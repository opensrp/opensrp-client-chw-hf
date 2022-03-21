package org.smartregister.chw.hf.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.smartregister.chw.core.model.ChildModel;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.PncMemberProfileActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class ChildFollowupViewAdapter extends RecyclerView.Adapter<ChildFollowupViewAdapter.ChildFollowupCardholder> {
    private final List<ChildModel> childModels;

    private final Activity context;


    public ChildFollowupViewAdapter(List<ChildModel> childModels, Activity activity) {
        this.childModels = new ArrayList<>(childModels);
        this.context = activity;
    }

    @NonNull
    @Override
    public ChildFollowupCardholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View followupLayout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.child_followup_card_view, viewGroup, false);
        return new ChildFollowupCardholder(followupLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildFollowupCardholder holder, int position) {
        ChildModel childModel = childModels.get(position);
        holder.childFollowupBtn.setTag(childModel.getBaseEntityId());
        holder.childFollowupTitle.setText(context.getString(R.string.child_followup, childModel.getFirstName()));
        holder.childFollowupBtn.setOnClickListener(v -> {
            PncMemberProfileActivity.startChildForm(context, childModel.getBaseEntityId());
        });
    }


    @Override
    public int getItemCount() {
        return childModels.size();
    }

    public static class ChildFollowupCardholder extends RecyclerView.ViewHolder {
        public RelativeLayout childRow;
        public ImageView childRowImage;
        public Button childFollowupBtn;
        public CustomFontTextView childFollowupTitle;
        public CustomFontTextView childFollowupDetails;


        public ChildFollowupCardholder(@NonNull View itemView) {
            super(itemView);
            childRow = itemView.findViewById(R.id.rlChildFollowup);
            childRowImage = itemView.findViewById(R.id.child_followup_image);
            childFollowupBtn = itemView.findViewById(R.id.record_followup_btn);
            childFollowupTitle = itemView.findViewById(R.id.child_followup_title);
            childFollowupDetails = itemView.findViewById(R.id.child_followup_details);

        }
    }
}
