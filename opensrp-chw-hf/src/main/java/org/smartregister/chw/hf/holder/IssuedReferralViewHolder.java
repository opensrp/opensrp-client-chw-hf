package org.smartregister.chw.hf.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.smartregister.chw.core.holders.ReferralViewHolder;
import org.smartregister.chw.hf.R;

public class IssuedReferralViewHolder extends ReferralViewHolder {
    public TextView patientName;
    public TextView textViewVillage;
    public TextView textViewGender;
    public TextView textReferralStatus;
    public TextView textViewService;
    public TextView textViewReferralClinic;
    public View patientColumn;
    public View registerColumns;
    public View dueWrapper;

    public IssuedReferralViewHolder(@NonNull View itemView) {
        super(itemView);
        patientName = itemView.findViewById(R.id.patient_name_age);
        textViewVillage = itemView.findViewById(R.id.text_view_village);
        textViewGender = itemView.findViewById(R.id.text_view_gender);
        textReferralStatus = itemView.findViewById(R.id.text_view_referral_status);
        patientColumn = itemView.findViewById(R.id.patient_column);
        textViewService = itemView.findViewById(R.id.text_view_service);
        textViewReferralClinic = itemView.findViewById(R.id.text_view_referral_clinic);
        registerColumns = itemView.findViewById(R.id.register_columns);
        dueWrapper = itemView.findViewById(R.id.due_button_wrapper);
    }
}
