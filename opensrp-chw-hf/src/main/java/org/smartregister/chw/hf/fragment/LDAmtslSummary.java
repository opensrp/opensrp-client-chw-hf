package org.smartregister.chw.hf.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.chw.hf.R;

public class LDAmtslSummary extends Fragment {
    private TextView uterotonic;
    private TextView method_used_to_remove_the_placenta;
    private TextView placenta_and_membrane_expulsion;
    private TextView type_of_incomplete_placenta;
    private TextView placenta_removed_by_hand;
    private TextView conducted_mva;
    private TextView administered_antibiotics;
    private TextView removal_date;
    private TextView removal_duration;
    private TextView estimated_blood_loss;
    private TextView provided_blood_transfusion;
    private TextView name_of_the_provider_who_removed_the_placenta;
    private TextView uterus_massage_after_delivery;
    private TextView reason_for_not_massaging_uterus_after_delivery;
    private TextView has_signs_of_eclampsia;
    private TextView administered_magnesium_sulphate;
    private TextView reason_for_not_administering_magnesium_sulphate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ld_amtsl_summary, container, false);
        uterotonic = rootView.findViewById(R.id.amtsl_uterotonic);
        method_used_to_remove_the_placenta = rootView.findViewById(R.id.amtsl_method_used_to_remove_the_placenta);
        placenta_and_membrane_expulsion = rootView.findViewById(R.id.amtsl_placenta_and_membrane_expulsion);
        type_of_incomplete_placenta = rootView.findViewById(R.id.amtsl_type_of_incomplete_placenta);
        placenta_removed_by_hand = rootView.findViewById(R.id.amtsl_placenta_removed_by_hand);
        conducted_mva = rootView.findViewById(R.id.amtsl_conducted_mva);
        administered_antibiotics = rootView.findViewById(R.id.amtsl_administered_antibiotics);
        removal_date = rootView.findViewById(R.id.amtsl_removal_date);
        removal_duration = rootView.findViewById(R.id.amtsl_removal_duration);
        estimated_blood_loss = rootView.findViewById(R.id.amtsl_estimated_blood_loss);
        provided_blood_transfusion = rootView.findViewById(R.id.amtsl_provided_blood_transfusion);
        name_of_the_provider_who_removed_the_placenta = rootView.findViewById(R.id.amtsl_name_of_the_provider_who_removed_the_placenta);
        uterus_massage_after_delivery = rootView.findViewById(R.id.amtsl_uterus_massage_after_delivery);
        reason_for_not_massaging_uterus_after_delivery = rootView.findViewById(R.id.amtsl_reason_for_not_massaging_uterus_after_delivery);
        has_signs_of_eclampsia = rootView.findViewById(R.id.amtsl_has_signs_of_eclampsia);
        administered_magnesium_sulphate = rootView.findViewById(R.id.amtsl_administered_magnesium_sulphate);
        reason_for_not_administering_magnesium_sulphate = rootView.findViewById(R.id.amtsl_reason_for_not_administering_magnesium_sulphate);
        return rootView;
    }
}