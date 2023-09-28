package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.presenter.BaseAncMedicalHistoryPresenter;
import org.smartregister.chw.core.activity.CoreAncMedicalHistoryActivity;
import org.smartregister.chw.core.activity.DefaultAncMedicalHistoryActivityFlv;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.FpMedicalHistoryInteractor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class FpMedicalHistoryActivity extends CoreAncMedicalHistoryActivity {
    private static FpMemberObject fpMemberObject;
    private final Flavor flavor = new FpMedicalHistoryActivityFlv();
    private ProgressBar progressBar;

    public static void startMe(Activity activity, FpMemberObject memberObject) {
        Intent intent = new Intent(activity, FpMedicalHistoryActivity.class);
        fpMemberObject = memberObject;
        activity.startActivity(intent);
    }

    @Override
    public void initializePresenter() {
        presenter = new BaseAncMedicalHistoryPresenter(new FpMedicalHistoryInteractor(), this, fpMemberObject.getBaseEntityId());
    }

    @Override
    public void setUpView() {
        linearLayout = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.linearLayoutMedicalHistory);
        progressBar = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.progressBarMedicalHistory);

        TextView tvTitle = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.tvTitle);
        tvTitle.setText(getString(org.smartregister.chw.opensrp_chw_anc.R.string.back_to, fpMemberObject.getFullName()));

        ((TextView) findViewById(R.id.medical_history)).setText(getString(R.string.visits_history));
    }

    @Override
    public View renderView(List<Visit> visits) {
        super.renderView(visits);
        View view = flavor.bindViews(this);
        displayLoadingState(true);
        flavor.processViewData(visits, this);
        displayLoadingState(false);
        TextView agywVisitTitle = view.findViewById(org.smartregister.chw.core.R.id.customFontTextViewHealthFacilityVisitTitle);
        agywVisitTitle.setText(R.string.visits_history);
        return view;
    }

    @Override
    public void displayLoadingState(boolean state) {
        progressBar.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    private static class FpMedicalHistoryActivityFlv extends DefaultAncMedicalHistoryActivityFlv {
        private final StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);

        @Override
        protected void processAncCard(String has_card, Context context) {
            // super.processAncCard(has_card, context);
            linearLayoutAncCard.setVisibility(View.GONE);
        }

        @Override
        protected void processHealthFacilityVisit(List<Map<String, String>> hf_visits, Context context) {
            //super.processHealthFacilityVisit(hf_visits, context);
        }

        @Override
        public void processViewData(List<Visit> visits, Context context) {

            if (visits.size() > 0) {
                int days = 0;
                List<LinkedHashMap<String, String>> hf_visits = new ArrayList<>();

                int x = 0;
                while (x < visits.size()) {
                    LinkedHashMap<String, String> visitDetails = new LinkedHashMap<>();

                    // the first object in this list is the days difference
                    if (x == 0) {
                        days = Days.daysBetween(new DateTime(visits.get(visits.size() - 1).getDate()), new DateTime()).getDays();
                    }

                    String[] params = {
                            "point_of_service_delivery",
                            "service_delivery_point_facility",
                            "service_delivery_point_outreach",
                            "family_planning_education_provided",
                            "client_counseled_with_her_partner",
                            "client_agreed_on_fp_choice",
                            "selected_fp_method_after_counseling",
                            "client_medical_history",
                            "ctc_number",
                            "number_of_pregnancies",
                            "number_of_miscarriages",
                            "number_still_births",
                            "number_live_births",
                            "number_children_alive",
                            "date_last_delivery",
                            "is_client_breastfeeding",
                            "weight",
                            "systolic",
                            "diastolic",
                            "anaemia",
                            "jaundice",
                            "thyroid_enlarged",
                            "chest_movement",
                            "breast_condition",
                            "specify_other_condition",
                            "cervix",
                            "discharge",
                            "growth",
                            "uterine_size",
                            "uterine_position",
                            "adnexa",
                            "menarche",
                            "lnmp",
                            "mp_duration",
                            "blood_loss",
                            "cycle_length",
                            "dysmenorrhoea",
                            "client_category_after_screening",
                            "pop",
                            "coc",
                            "ecp",
                            "injection_administered",
                            "jadelle_inserted",
                            "implanon_inserted",
                            "iucd_inserted",
                            "cycle_beads_provided",
                            "client_counseled_on_lam",
                            "vasectomy",
                            "btl",
                            "post_instruction_fp_method_provided",
                            "reasons_for_not_providing_method",
                            "client_provided_condom",
                            "type_of_condom_collected",
                            "number_male_condoms_collected",
                            "number_female_condoms_collected",
                            "next_appointment_date",
                            "other_services_offered",
                            "client_hiv_test_results",
                            "client_referred_to_ctc",
                            "partner_tested_for_hiv",
                            "partner_hiv_test_results",
                            "partner_referred_to_ctc",
                            "counseling_cervical_cancer_provided",
                            "client_eligible_for_via",
                            "via_results",
                            "client_satisfied_with_fp_method",
                            "reason_for_dissatisfaction",
                            "side_effects",
                            "complication",
                            "specify_other_reasons_for_dissatisfaction",
                            "client_want_to_switch_stop",
                            "jadelle_removed",
                            "implanon_removed",
                            "iud_removed",
                            "client_have_any_complain"
                    };
                    extractVisitDetails(visits, params, visitDetails, x, context);


                    hf_visits.add(visitDetails);

                    x++;
                }

                processLastVisit(days, context);
                processVisit(hf_visits, context, visits);
            }
        }

        private void extractVisitDetails(List<Visit> sourceVisits, String[] hf_params, LinkedHashMap<String, String> visitDetailsMap, int iteration, Context context) {
            // get the hf details
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            for (String param : hf_params) {
                try {
                    List<VisitDetail> details = sourceVisits.get(iteration).getVisitDetails().get(param);
                    map.put(param, getTexts(context, details));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            visitDetailsMap.putAll(map);
        }


        private void processLastVisit(int days, Context context) {
            linearLayoutLastVisit.setVisibility(View.VISIBLE);
            if (days < 1) {
                customFontTextViewLastVisit.setText(org.smartregister.chw.core.R.string.less_than_twenty_four);
            } else {
                customFontTextViewLastVisit.setText(StringUtils.capitalize(MessageFormat.format(context.getString(org.smartregister.chw.core.R.string.days_ago), String.valueOf(days))));
            }
        }


        protected void processVisit(List<LinkedHashMap<String, String>> community_visits, Context context, List<Visit> visits) {
            if (community_visits != null && community_visits.size() > 0) {
                linearLayoutHealthFacilityVisit.setVisibility(View.VISIBLE);

                int x = 0;
                for (LinkedHashMap<String, String> vals : community_visits) {
                    View view = inflater.inflate(R.layout.medical_history_visit, null);
                    TextView tvTitle = view.findViewById(R.id.title);
                    View edit = view.findViewById(R.id.textview_edit);
                    LinearLayout visitDetailsLayout = view.findViewById(R.id.visit_details_layout);

                    tvTitle.setText(visits.get(x).getVisitType() + " " + visits.get(x).getDate());

                    if (x == visits.size() - 1) {
                        edit.setVisibility(View.VISIBLE);
                        int position = x;
//                        edit.setOnClickListener(view1 -> {
//                            if (visits.get(position).getVisitType().equalsIgnoreCase("KVP Bio Medical Service Visit")) {
//                                KvpBioMedicalServiceActivity.startKvpBioMedicalServiceActivity((Activity) context, visits.get(position).getBaseEntityId(), true);
//                            } else if (visits.get(position).getVisitType().equalsIgnoreCase("KVP Structural Service Visit")) {
//                                KvpStructuralServiceActivity.startKvpStructuralServiceActivity((Activity) context, visits.get(position).getBaseEntityId(), true);
//                            } else if (visits.get(position).getVisitType().equalsIgnoreCase("KVP Other Service Visit")) {
//                                KvpOtherServiceActivity.startKvpOtherServiceActivity((Activity) context, visits.get(position).getBaseEntityId(), true);
//                            } else if (visits.get(position).getVisitType().equalsIgnoreCase("KVP Behavioral Service Visit")) {
//                                KvpBehavioralServiceActivity.startKvpBehavioralServiceActivity((Activity) context, visits.get(position).getBaseEntityId(), true);
//                            }
//                        });
                    }


                    for (LinkedHashMap.Entry<String, String> entry : vals.entrySet()) {
                        TextView visitDetailTv = new TextView(context);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                                ((int) LinearLayout.LayoutParams.MATCH_PARENT, (int) LinearLayout.LayoutParams.WRAP_CONTENT);

                        visitDetailTv.setLayoutParams(params);
                        float scale = context.getResources().getDisplayMetrics().density;
                        int dpAsPixels = (int) (10 * scale + 0.5f);
                        visitDetailTv.setPadding(dpAsPixels, 0, 0, 0);
                        visitDetailsLayout.addView(visitDetailTv);


                        try {
                            int resource = context.getResources().getIdentifier("fp_" + entry.getKey(), "string", context.getPackageName());
                            evaluateView(context, vals, visitDetailTv, entry.getKey(), resource, "fp_");
                        } catch (Exception e) {
                            Timber.e(e);
                        }
                    }
                    linearLayoutHealthFacilityVisitDetails.addView(view, 0);

                    x++;
                }
            }
        }

        private void evaluateView(Context context, Map<String, String> vals, TextView tv, String valueKey, int viewTitleStringResource, String valuePrefixInStringResources) {
            if (StringUtils.isNotBlank(getMapValue(vals, valueKey))) {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                spannableStringBuilder.append(context.getString(viewTitleStringResource), boldSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE).append("\n");

                String stringValue = getMapValue(vals, valueKey);
                String[] stringValueArray;
                if (stringValue.contains(",")) {
                    stringValueArray = stringValue.split(",");
                    for (String value : stringValueArray) {
                        spannableStringBuilder.append(getStringResource(context, valuePrefixInStringResources, value.trim()) + "\n", new BulletSpan(10), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    spannableStringBuilder.append(getStringResource(context, valuePrefixInStringResources, stringValue)).append("\n");
                }
                tv.setText(spannableStringBuilder);
            } else {
                tv.setVisibility(View.GONE);
            }
        }


        private String getMapValue(Map<String, String> map, String key) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
            return "";
        }

        private String getStringResource(Context context, String prefix, String resourceName) {
            int resourceId = context.getResources().
                    getIdentifier(prefix + resourceName.trim(), "string", context.getPackageName());
            try {
                return context.getString(resourceId);
            } catch (Exception e) {
                Timber.e(e);
                return prefix + resourceName;
            }
        }
    }
}
