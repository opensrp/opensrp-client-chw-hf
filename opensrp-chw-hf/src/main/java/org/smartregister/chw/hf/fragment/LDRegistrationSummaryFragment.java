package org.smartregister.chw.hf.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.chw.hf.R;

public class LDRegistrationSummaryFragment extends Fragment {

    private TextView bmi;
    private TextView edd;
    private TextView para;
    private TextView pmtct;
    private TextView weight;
    private TextView height;
    private TextView gravida;
    private TextView systolic;
    private TextView edd_note;
    private TextView gest_age;
    private TextView tt_doses;
    private TextView hb_level;
    private TextView syphilis;
    private TextView diastolic;
    private TextView ipt_doses;
    private TextView rh_factor;
    private TextView pulse_rate;
    private TextView temperature;
    private TextView true_labour;
    private TextView blood_group;
    private TextView danger_signs;
    private TextView visit_number;
    private TextView hb_test_date;
    private TextView gest_age_note;
    private TextView itn_llin_used;
    private TextView admission_date;
    private TextView admission_time;
    private TextView children_alive;
    private TextView fetal_movement;
    private TextView admission_place;
    private TextView pmtct_test_date;
    private TextView respiratory_rate;
    private TextView fetal_heart_rate;
    private TextView admission_reason;
    private TextView art_prescription;
    private TextView labour_onset_date;
    private TextView labour_onset_time;
    private TextView ruptured_membrane;
    private TextView number_of_abortion;
    private TextView reason_for_referral;
    private TextView admitting_person_name;
    private TextView last_menstrual_period;
    private TextView membrane_ruptured_date;
    private TextView membrane_ruptured_time;
    private TextView management_provided_for_rh;
    private TextView admission_info_danger_signs;
    private TextView management_provided_for_pmtct;
    private TextView management_provided_for_hb_level;
    private TextView management_provided_for_syphilis;

    public static LDRegistrationSummaryFragment newInstance() {
        return new LDRegistrationSummaryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_labour_and_delivery_registration_summary, container, false);

        bmi = rootView.findViewById(R.id.bmi);
        edd = rootView.findViewById(R.id.edd);
        para = rootView.findViewById(R.id.para);
        pmtct = rootView.findViewById(R.id.pmtct);
        weight = rootView.findViewById(R.id.weight);
        height = rootView.findViewById(R.id.height);
        gravida = rootView.findViewById(R.id.gravida);
        systolic = rootView.findViewById(R.id.systolic);
        edd_note = rootView.findViewById(R.id.edd_note);
        gest_age = rootView.findViewById(R.id.gest_age);
        tt_doses = rootView.findViewById(R.id.tt_doses);
        hb_level = rootView.findViewById(R.id.hb_level);
        syphilis = rootView.findViewById(R.id.syphilis);
        diastolic = rootView.findViewById(R.id.diastolic);
        ipt_doses = rootView.findViewById(R.id.ipt_doses);
        rh_factor = rootView.findViewById(R.id.rh_factor);
        pulse_rate = rootView.findViewById(R.id.pulse_rate);
        temperature = rootView.findViewById(R.id.temperature);
        true_labour = rootView.findViewById(R.id.true_labour);
        blood_group = rootView.findViewById(R.id.blood_group);
        danger_signs = rootView.findViewById(R.id.danger_signs);
        visit_number = rootView.findViewById(R.id.visit_number);
        hb_test_date = rootView.findViewById(R.id.hb_test_date);
        gest_age_note = rootView.findViewById(R.id.gest_age_note);
        itn_llin_used = rootView.findViewById(R.id.itn_llin_used);
        admission_date = rootView.findViewById(R.id.admission_date);
        admission_time = rootView.findViewById(R.id.admission_time);
        children_alive = rootView.findViewById(R.id.children_alive);
        fetal_movement = rootView.findViewById(R.id.fetal_movement);
        admission_place = rootView.findViewById(R.id.admission_place);
        pmtct_test_date = rootView.findViewById(R.id.pmtct_test_date);
        respiratory_rate = rootView.findViewById(R.id.respiratory_rate);
        fetal_heart_rate = rootView.findViewById(R.id.fetal_heart_rate);
        admission_reason = rootView.findViewById(R.id.admission_reason);
        art_prescription = rootView.findViewById(R.id.art_prescription);
        labour_onset_date = rootView.findViewById(R.id.labour_onset_date);
        labour_onset_time = rootView.findViewById(R.id.labour_onset_time);
        ruptured_membrane = rootView.findViewById(R.id.ruptured_membrane);
        number_of_abortion = rootView.findViewById(R.id.number_of_abortion);
        reason_for_referral = rootView.findViewById(R.id.reason_for_referral);
        admitting_person_name = rootView.findViewById(R.id.admitting_person_name);
        last_menstrual_period = rootView.findViewById(R.id.last_menstrual_period);
        membrane_ruptured_date = rootView.findViewById(R.id.membrane_ruptured_date);
        membrane_ruptured_time = rootView.findViewById(R.id.membrane_ruptured_time);
        management_provided_for_rh = rootView.findViewById(R.id.management_provided_for_rh);
        admission_info_danger_signs = rootView.findViewById(R.id.admission_info_danger_signs);
        management_provided_for_pmtct = rootView.findViewById(R.id.management_provided_for_pmtct);
        management_provided_for_hb_level = rootView.findViewById(R.id.management_provided_for_hb_level);
        management_provided_for_syphilis = rootView.findViewById(R.id.management_provided_for_syphilis);
        return rootView;
    }
}