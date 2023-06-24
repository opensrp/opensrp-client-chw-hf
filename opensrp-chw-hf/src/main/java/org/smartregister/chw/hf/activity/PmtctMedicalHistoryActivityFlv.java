package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.core.activity.DefaultAncMedicalHistoryActivityFlv;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.pmtct.util.Constants;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class PmtctMedicalHistoryActivityFlv extends DefaultAncMedicalHistoryActivityFlv {
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
            List<Map<String, String>> hf_visits = new ArrayList<>();

            int x = 0;
            while (x < visits.size()) {

                // the first object in this list is the days difference
                if (x == 0) {
                    days = Days.daysBetween(new DateTime(visits.get(visits.size() - 1).getDate()), new DateTime()).getDays();
                }


                String[] visitParams = {
                        "followup_visit_date",
                        "followup_status",
                        "counselling_topics_provided",
                        "clinical_staging_disease",
                        "stage_1_symptoms",
                        "stage_2_symptoms",
                        "stage_3_symptoms",
                        "stage_4_symptoms",
                        "tb_registration_number",
                        "tb_symptoms_screening",
                        "investigate_for_tb",
                        "reason_for_not_conducting_tb_test",
                        "other_reason_for_not_conducting_tb_test",
                        "has_been_provided_with_tpt_before",
                        "completed_tpt",
                        "pmtct_is_client_provided_with_tpt",
                        "pmtct_reason_for_not_providing_tpt",
                        "number_of_tpt_days_dispensed",
                        "prescribed_regimes",
                        "arv_line",
                        "first_line",
                        "second_line",
                        "third_line",
                        "reason_for_not_prescribing_arv",
                        "other_reason_for_not_prescribing_arv",
                        "other_reason_for_not_conducting_tb_test",
                        "next_facility_visit_date"
                };
                extractVisitDetails(visits, visitParams, hf_visits, x, context);

                x++;
            }

            processLastVisit(days, context);
            processVisit(hf_visits, visits, context);
        }
    }

    private void extractVisitDetails(List<Visit> sourceVisits, String[] hf_params, List<Map<String, String>> hf_visits, int iteration, Context context) {
        // get the hf details
        Map<String, String> map = new HashMap<>();
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


    protected void processVisit(List<Map<String, String>> hf_visits, List<Visit> visits, Context context) {
        if (hf_visits != null && hf_visits.size() > 0) {
            linearLayoutHealthFacilityVisit.setVisibility(View.VISIBLE);

            int x = 0;
            for (Map<String, String> vals : hf_visits) {
                View view = inflater.inflate(R.layout.medical_history_pmtct_visit, null);
                TextView tvTitle = view.findViewById(R.id.title);
                TextView tvFollowupStatus = view.findViewById(R.id.followup_status);
                TextView tvCounsellingTopicsProvided = view.findViewById(R.id.counselling_topics_provided);
                TextView tvClinicalStagingDisease = view.findViewById(R.id.clinical_staging_disease);
                TextView tvSymptoms = view.findViewById(R.id.symptoms);
                TextView tvTbRegistrationNumber = view.findViewById(R.id.tb_registration_number);
                TextView tvTbSymptomsScreening = view.findViewById(R.id.tb_symptoms_screening);
                TextView tvInvestigateForTb = view.findViewById(R.id.investigate_for_tb);
                TextView tvReasonForNotConductingTbTest = view.findViewById(R.id.reason_for_not_conducting_tb_test);
                TextView tvOtherReasonForNotConductingTbTest = view.findViewById(R.id.other_reason_for_not_conducting_tb_test);
                TextView tvHasBeenProvidedWithTptBefore = view.findViewById(R.id.has_been_provided_with_tpt_before);
                TextView tvCompletedTpt = view.findViewById(R.id.completed_tpt);
                TextView tvIsClientProvidedWithTpt = view.findViewById(R.id.is_client_provided_with_tpt);
                TextView tvReasonForNotProvidingTpt = view.findViewById(R.id.reason_for_not_providing_tpt);
                TextView tvNumberOfTptDaysDispensed = view.findViewById(R.id.number_of_tpt_days_dispensed);
                TextView tvPrescribedRegimes = view.findViewById(R.id.prescribed_regimes);
                TextView tvArvLine = view.findViewById(R.id.arv_line);
                TextView tvArvPrescription = view.findViewById(R.id.arv_prescription);
                TextView tvReasonsForNotPrescribingArv = view.findViewById(R.id.reason_for_not_prescribing_arv);
                TextView tvOtherReasonForNotPrescribingArv = view.findViewById(R.id.other_reason_for_not_prescribing_arv);
                TextView tvNumberOfArvRegimesDaysDispensed = view.findViewById(R.id.number_of_arv_regimes_days_dispensed);
                TextView tvNextFacilityVisitDate = view.findViewById(R.id.next_facility_visit_date);
                TextView tvEdit = view.findViewById(R.id.textview_edit);

                evaluateTitle(context, x, visits.get(x).getDate(), tvTitle);


                // Updating visibility of EDIT button if the visit is the last visit
                if (x == visits.size() - 1)
                    tvEdit.setVisibility(View.VISIBLE);
                else
                    tvEdit.setVisibility(View.GONE);

                tvEdit.setOnClickListener(view1 -> {
                    ((Activity) context).finish();
                    Visit visit = visits.get(0);
                    if (visit != null && visit.getVisitType().equalsIgnoreCase(Constants.EVENT_TYPE.PMTCT_FOLLOWUP) && visit.getBaseEntityId() != null)
                        PmtctFollowupVisitActivity.startPmtctFollowUpActivity((Activity) context, visit.getBaseEntityId(), true);
                });


                evaluateView(context, vals, tvFollowupStatus, "followup_status", R.string.pmtct_followup_status, "pmtct_followup_status_");
                evaluateView(context, vals, tvCounsellingTopicsProvided, "counselling_topics_provided", R.string.pmtct_counselling_topics_provided, "pmtct_counselling_topics_provided_");
                evaluateView(context, vals, tvClinicalStagingDisease, "clinical_staging_disease", R.string.pmtct_clinical_staging_disease, "pmtct_clinical_staging_disease_");

                String clinicalStage = getMapValue(vals, "clinical_staging_disease");
                if (StringUtils.isNotBlank(clinicalStage)) {
                    evaluateView(context, vals, tvSymptoms, clinicalStage + "_symptoms", R.string.pmtct_symptoms, "pmtct_clinical_staging_symptoms_");
                } else {
                    tvSymptoms.setVisibility(View.GONE);
                }

                evaluateView(context, vals, tvTbRegistrationNumber, "tb_registration_number", R.string.pmtct_tb_registration_number, "");
                evaluateView(context, vals, tvTbSymptomsScreening, "tb_symptoms_screening", R.string.pmtct_tb_symptoms_screening, "pmtct_tb_symptoms_screening_");
                evaluateView(context, vals, tvInvestigateForTb, "investigate_for_tb", R.string.pmtct_investigate_for_tb, "pmtct_investigate_for_tb_");
                evaluateView(context, vals, tvReasonForNotConductingTbTest, "reason_for_not_conducting_tb_test", R.string.pmtct_reason_for_not_conducting_tb_test, "");
                evaluateView(context, vals, tvOtherReasonForNotConductingTbTest, "other_reason_for_not_conducting_tb_test", R.string.pmtct_other_reason_for_not_conducting_tb_test, "");

                evaluateView(context, vals, tvHasBeenProvidedWithTptBefore, "has_been_provided_with_tpt_before", R.string.pmtct_has_been_provided_with_tpt_before, "pmtct_has_been_provided_with_tpt_before_");
                evaluateView(context, vals, tvCompletedTpt, "completed_tpt", R.string.pmtct_completed_tpt, "");
                evaluateView(context, vals, tvIsClientProvidedWithTpt, "pmtct_is_client_provided_with_tpt", R.string.pmtct_other_reason_for_not_conducting_tb_test, "");
                evaluateView(context, vals, tvReasonForNotProvidingTpt, "pmtct_reason_for_not_providing_tpt", R.string.pmtct_other_reason_for_not_conducting_tb_test, "");
                evaluateView(context, vals, tvNumberOfTptDaysDispensed, "number_of_tpt_days_dispensed", R.string.pmtct_number_of_tpt_days_dispensed, "");

                evaluateView(context, vals, tvPrescribedRegimes, "prescribed_regimes", R.string.pmtct_prescribed_regimes, "");
                evaluateView(context, vals, tvArvLine, "arv_line", R.string.pmtct_arv_line, "pmtct_arv_line_");

                String arvLine = getMapValue(vals, "arv_line");
                if (StringUtils.isNotBlank(arvLine)) {
                    evaluateView(context, vals, tvArvPrescription, arvLine, R.string.pmtct_arv_prescription, "pmtct_");
                }else{
                    tvArvPrescription.setVisibility(View.GONE);
                }

                evaluateView(context, vals, tvReasonsForNotPrescribingArv, "reason_for_not_prescribing_arv", R.string.pmtct_reason_for_not_prescribing_arv, "");
                evaluateView(context, vals, tvOtherReasonForNotPrescribingArv, "other_reason_for_not_prescribing_arv", R.string.pmtct_other_reason_for_not_prescribing_arv, "");
                evaluateView(context, vals, tvNumberOfArvRegimesDaysDispensed, "other_reason_for_not_conducting_tb_test", R.string.pmtct_number_of_arv_regimes_days_dispensed, "");


                evaluateView(context, vals, tvNextFacilityVisitDate, "next_facility_visit_date", R.string.pmtct_next_facility_visit_date, "");


                linearLayoutHealthFacilityVisitDetails.addView(view, 0);

                x++;
            }
        }
    }


    private void evaluateTitle(Context context, int x, Date visitDate, TextView tvTitle) {
        if (visitDate == null) {
            tvTitle.setVisibility(View.GONE);
        } else {
            try {
                tvTitle.setText(MessageFormat.format(context.getString(R.string.pmtct_visit_title), x + 1, new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(visitDate)));
            } catch (Exception e) {
                Timber.e(e);
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
