package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.core.activity.DefaultAncMedicalHistoryActivityFlv;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class AncMedicalHistoryActivityFlv extends DefaultAncMedicalHistoryActivityFlv {
    private final StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);

    @Override
    protected void processAncCard(String has_card, Context context) {
        // super.processAncCard(has_card, context);
        linearLayoutAncCard.setVisibility(View.GONE);
    }

    @Override
    protected void processHealthFacilityVisit(List<Map<String, String>> hf_visits, Context context) {
        //super.processHealthFacilityVisit(hf_visits, context);

        if (hf_visits != null && hf_visits.size() > 0) {
            linearLayoutHealthFacilityVisit.setVisibility(View.VISIBLE);

            int x = 1;
            for (Map<String, String> vals : hf_visits) {
                View view = inflater.inflate(R.layout.medical_history_anc_visit, null);

                TextView tvTitle = view.findViewById(R.id.title);
                TextView tvTests = view.findViewById(R.id.tests);

                view.findViewById(R.id.weight).setVisibility(View.GONE);
                view.findViewById(R.id.bp).setVisibility(View.GONE);
                view.findViewById(R.id.hb).setVisibility(View.GONE);
                view.findViewById(R.id.ifa_received).setVisibility(View.GONE);


                tvTitle.setText(MessageFormat.format(context.getString(R.string.anc_visit_date), (x), vals.get("anc_hf_visit_date")));
                tvTests.setText(MessageFormat.format(context.getString(R.string.tests_done_details), vals.get("tests_done")));

                linearLayoutHealthFacilityVisitDetails.addView(view, 0);

                x++;
            }
        }
    }

    @Override
    public void processViewData(List<Visit> visits, Context context) {

        if (visits.size() > 0) {

            int days = 0;
            String has_card = "No";
            List<LinkedHashMap<String, String>> hfVisitsMaps = new ArrayList<>();

            int x = 0;
            while (x < visits.size()) {

                // the first object in this list is the days difference
                if (x == 0) {
                    days = Days.daysBetween(new DateTime(visits.get(visits.size() - 1).getDate()), new DateTime()).getDays();
                }

                // anc card
                if (has_card.equalsIgnoreCase("No")) {
                    List<VisitDetail> details = visits.get(x).getVisitDetails().get("anc_card");
                    if (details != null && StringUtils.isNotBlank(details.get(0).getHumanReadable())) {
                        has_card = details.get(0).getHumanReadable();
                    }

                }


                String[] hf_params = {"gest_age", "medical_surgical_history", "other_medical_surgical_history", "ctc_number", "gravida", "parity",
                        "glucose_in_urine", "reason_for_not_conducting_glucose_in_urine_test", "other_reason_for_not_conducting_glucose_in_urine_test", "protein_in_urine", "reason_for_not_conducting_protein_in_urine_test", "other_reason_for_not_conducting_protein_in_urine_test", "blood_group", "reason_for_not_conducting_blood_group_test", "other_reason_for_not_conducting_blood_group_test", "rh_factor", "hb_level_test", "hb_level", "reason_for_not_conducting_hb_test", "other_reason_hb_test_not_conducted", "blood_for_glucose_test", "type_of_blood_for_glucose_test", "blood_for_glucose", "hiv", "reason_for_not_conducting_hiv_test", "other_reason_for_not_conducting_hiv_test", "hiv_counselling_before_testing", "hiv_counselling_after_testing", "syphilis", "reason_for_not_conducting_syphilis_test", "other_reason_for_not_conducting_syphilis_test", "syphilis_treatment", "hepatitis", "prescribe_arv_hepb_at_above_twenty_eight", "reason_for_not_conducting_hepatitis_test", "other_reason_for_not_conducting_hepatitis_test", "other_stds", "other_stds_treatment", "reason_for_not_giving_medication_for_other_stds", "other_reason_for_not_giving_medication_for_other_stds",
                        "weight", "height", "systolic", "diastolic", "pulse_rate", "temperature", "fundal_height", "abdominal_scars", "abdominal_movement_with_respiration", "abdominal_contour", "fundal_height", "lie", "presentation", "fetal_heart_rate", "abnormal_vaginal_discharge", "vaginal_sores", "vaginal_swelling",
                        "tt_vaccination", "tt_vaccination_type",
                        "client_on_malaria_medication", "mRDT_for_malaria", "reason_for_not_conducting_malaria_test", "other_reason_for_not_conducting_malaria_test", "llin_provision", "reason_for_not_providing_llin", "other_reason_llin_not_given",
                        "delivery_place", "name_of_hf", "transport", "birth_companion", "emergency_funds", "household_support", "blood_donor",
                        "next_facility_visit_date"};
                extractHFVisit(visits, hf_params, hfVisitsMaps, x, context);

                x++;
            }

            processLastVisit(days, context);
            processAncCard(has_card, context);
            processFacilityVisit(hfVisitsMaps, visits, context);
        }
    }

    private void extractHFVisit(List<Visit> sourceVisits, String[] hf_params, List<LinkedHashMap<String, String>> hf_visits, int iteration, Context context) {
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
        hf_visits.add(map);
    }


    private void processLastVisit(int days, Context context) {
        linearLayoutLastVisit.setVisibility(View.VISIBLE);
        if (days < 1) {
            customFontTextViewLastVisit.setText(org.smartregister.chw.core.R.string.less_than_twenty_four);
        } else {
            customFontTextViewLastVisit.setText(StringUtils.capitalize(MessageFormat.format(context.getString(org.smartregister.chw.core.R.string.days_ago), String.valueOf(days))));
        }
    }


    protected void processFacilityVisit(List<LinkedHashMap<String, String>> hfVisitsDetails, List<Visit> visits, Context context) {
        if (hfVisitsDetails != null && hfVisitsDetails.size() > 0) {
            linearLayoutHealthFacilityVisit.setVisibility(View.VISIBLE);

            int x = 0;
            for (Map<String, String> vals : hfVisitsDetails) {
                View view = inflater.inflate(R.layout.medical_history_anc_visit, null);

                TextView tvTitle = view.findViewById(R.id.title);
                TextView tvEdit = view.findViewById(R.id.textview_edit);
                LinearLayout visitDetailsLayout = view.findViewById(R.id.visit_details_layout);


                // Updating visibility of EDIT button if the visit is the last visit
                if (x == visits.size() - 1)
                    tvEdit.setVisibility(View.VISIBLE);
                else
                    tvEdit.setVisibility(View.GONE);


                tvTitle.setText(MessageFormat.format(context.getString(org.smartregister.chw.core.R.string.anc_visit_date), x + 1, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(visits.get(x).getDate())));


                tvEdit.setOnClickListener(view1 -> {
                    ((Activity) context).finish();
                    Visit visit = visits.get(0);
                    if (visit != null && visit.getVisitType().equalsIgnoreCase(Constants.Events.ANC_FIRST_FACILITY_VISIT) && visit.getBaseEntityId() != null)
                        AncFirstFacilityVisitActivity.startMe((Activity) context, visit.getBaseEntityId(), true);
                    else if (visit != null && visit.getVisitType().equalsIgnoreCase(Constants.Events.ANC_RECURRING_FACILITY_VISIT) && visit.getBaseEntityId() != null)
                        AncRecurringFacilityVisitActivity.startMe((Activity) context, visit.getBaseEntityId(), true);
                });


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
                        int resource = context.getResources().getIdentifier("anc_" + entry.getKey(), "string", context.getPackageName());
                        evaluateView(context, vals, visitDetailTv, entry.getKey(), resource, "");
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }


                linearLayoutHealthFacilityVisitDetails.addView(view, 0);

                x++;
            }
        }
    }

    private String getMapValue(Map<String, String> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return "";
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

    private String getStringResource(Context context, String prefix, String resourceName) {
        int resourceId = context.getResources().
                getIdentifier(prefix + resourceName.trim(), "string", context.getPackageName());
        try {
            return context.getString(resourceId);
        } catch (Exception e) {
            Timber.e(e);
            if (resourceName.contains("_")) {
                resourceName = resourceName.replace("_", " ");
                resourceName = WordUtils.capitalize(resourceName);
            }
            return resourceName;
        }
    }

}
