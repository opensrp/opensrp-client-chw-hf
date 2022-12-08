package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.core.activity.DefaultAncMedicalHistoryActivityFlv;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeiMedicalHistoryActivityFlv extends DefaultAncMedicalHistoryActivityFlv {

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


                String[] hf_params = {"pmtct_visit_date", "actual_age", "followup_status", "weight", "number_of_ctx_days_dispensed", "number_of_nvp_days_dispensed", "number_of_azt_nvp_days_dispensed", "infant_feeding_practice", "sample_id", "next_facility_visit_date"};
                extractHFVisit(visits, hf_params, hf_visits, x, context);

                x++;
            }

            processLastVisit(days, context);
            processFacilityVisit(hf_visits, visits, context);
        }
    }

    private void extractHFVisit(List<Visit> sourceVisits, String[] hf_params, List<Map<String, String>> hf_visits, int iteration, Context context) {
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


    protected void processFacilityVisit(List<Map<String, String>> hf_visits, List<Visit> visits, Context context) {
        if (hf_visits != null && hf_visits.size() > 0) {
            linearLayoutHealthFacilityVisit.setVisibility(View.VISIBLE);

            int x = 0;
            for (Map<String, String> vals : hf_visits) {
                View view = inflater.inflate(R.layout.medical_history_hei_visit, null);
                TextView tvTitle = view.findViewById(R.id.title);
                TextView tvTypeOfVisit = view.findViewById(R.id.type_of_visit);
                TextView tvWeight = view.findViewById(R.id.weight);
                TextView tvCtx = view.findViewById(R.id.ctx);
                TextView tvNvp = view.findViewById(R.id.nvp);
                TextView tvFeedingPractice = view.findViewById(R.id.feeding_practice);
                TextView tvDnaPcr = view.findViewById(R.id.dna_pcr);
                TextView tvEdit = view.findViewById(R.id.textview_edit);

                // Updating visibility of EDIT button if the visit is the last visit
                if (x == visits.size() - 1)
                    tvEdit.setVisibility(View.VISIBLE);
                else
                    tvEdit.setVisibility(View.GONE);

                tvEdit.setOnClickListener(view1 -> {
                    ((Activity) context).finish();
                    Visit visit = visits.get(0);
                    if (visit != null && visit.getVisitType().equalsIgnoreCase(Constants.Events.HEI_FOLLOWUP) && visit.getBaseEntityId() != null)
                        HeiFollowupVisitActivity.startHeiFollowUpActivity((Activity) context, visit.getBaseEntityId(), true);
                });


                evaluateTitle(context, x, vals, tvTitle);
                evaluateFollowupStatus(context, vals, tvTypeOfVisit);
                evaluateWeight(context, vals, tvWeight);
                evaluateCtxDaysDispensed(context, vals, tvCtx);
                evaluateNvpNumberOfDays(context, vals, tvNvp);
                evaluateFeedingPractice(context, vals, tvFeedingPractice);
                evaluateDnaPcr(context, vals, tvDnaPcr);
                evaluateNextVisitDate(context, vals, view);
                linearLayoutHealthFacilityVisitDetails.addView(view, 0);

                x++;
            }
        }
    }

    private void evaluateNextVisitDate(Context context, Map<String, String> vals, View view) {
        if (StringUtils.isBlank(getMapValue(vals, "next_facility_visit_date"))) {
            view.findViewById(R.id.next_facility_visit_date).setVisibility(View.GONE);
        } else {
            ((TextView) view.findViewById(R.id.next_facility_visit_date)).setText(MessageFormat.format(context.getString(R.string.next_facility_visit_date), getMapValue(vals, "next_facility_visit_date")));
        }
    }

    private void evaluateTitle(Context context, int x, Map<String, String> vals, TextView tvTitle) {
        if (StringUtils.isBlank(vals.get("pmtct_visit_date"))) {
            tvTitle.setVisibility(View.GONE);
        } else {
            tvTitle.setText(MessageFormat.format(context.getString(R.string.hei_visit_title), x + 1, getMapValue(vals, "pmtct_visit_date"), getMapValue(vals, "actual_age")));
        }
    }

    private void evaluateFollowupStatus(Context context, Map<String, String> vals, TextView tvTypeOfVisit) {
        if (StringUtils.isNotBlank(getMapValue(vals, "followup_status"))) {
            String followupStatus = getMapValue(vals, "followup_status");
            switch (followupStatus) {
                case "infant_and_mother":
                    tvTypeOfVisit.setText(MessageFormat.format(context.getString(R.string.hei_type_of_visit), "IM"));
                    break;
                case "infant_with_other_caregiver":
                    tvTypeOfVisit.setText(MessageFormat.format(context.getString(R.string.hei_type_of_visit), "IC"));
                    break;
                case "transfer_out":
                    tvTypeOfVisit.setText(MessageFormat.format(context.getString(R.string.hei_type_of_visit), "TO"));
                    break;
                default:
                    tvTypeOfVisit.setText(MessageFormat.format(context.getString(R.string.hei_type_of_visit), followupStatus));
                    break;
            }
        }
    }

    private void evaluateWeight(Context context, Map<String, String> vals, TextView tvWeight) {
        if (StringUtils.isNotBlank(getMapValue(vals, "weight"))) {
            tvWeight.setText(MessageFormat.format(context.getString(R.string.weight_in_kgs), getMapValue(vals, "weight")));
        } else {
            tvWeight.setVisibility(View.GONE);
        }
    }

    private void evaluateCtxDaysDispensed(Context context, Map<String, String> vals, TextView tvCtx) {
        if (StringUtils.isBlank(getMapValue(vals, "number_of_ctx_days_dispensed"))) {
            tvCtx.setVisibility(View.GONE);
        } else {
            tvCtx.setText(MessageFormat.format(context.getString(R.string.ctx_days_dispensed), getMapValue(vals, "number_of_ctx_days_dispensed")));
        }
    }

    private void evaluateNvpNumberOfDays(Context context, Map<String, String> vals, TextView tvNvp) {
        if (StringUtils.isBlank(getMapValue(vals, "number_of_nvp_days_dispensed")) && StringUtils.isBlank(getMapValue(vals, "number_of_azt_nvp_days_dispensed"))) {
            tvNvp.setVisibility(View.GONE);
        } else {
            String message = StringUtils.isBlank(getMapValue(vals, "number_of_azt_nvp_days_dispensed")) ?
                    MessageFormat.format(context.getString(R.string.nvp_days_dispensed), getMapValue(vals, "number_of_nvp_days_dispensed")) : MessageFormat.format(context.getString(R.string.nvp_days_dispensed), getMapValue(vals, "number_of_azt_nvp_days_dispensed"));
            tvNvp.setText(message);
        }
    }

    private void evaluateDnaPcr(Context context, Map<String, String> vals, TextView tvDnaPcr) {
        if (StringUtils.isBlank(getMapValue(vals, "sample_id"))) {
            tvDnaPcr.setVisibility(View.GONE);
        } else {
            tvDnaPcr.setText(MessageFormat.format(context.getString(R.string.dna_pcr_sample), getMapValue(vals, "sample_id")));
        }
    }

    private void evaluateFeedingPractice(Context context, Map<String, String> vals, TextView tvFeedingPractice) {
        if (StringUtils.isBlank(getMapValue(vals, "infant_feeding_practice"))) {
            tvFeedingPractice.setVisibility(View.GONE);
        } else {
            String feedingPractice = getMapValue(vals, "infant_feeding_practice");
            switch (feedingPractice) {
                case "ebf":
                    tvFeedingPractice.setText(MessageFormat.format(context.getString(R.string.infant_feeding_practice), context.getString(R.string.feeding_practice_ebf)));
                    break;
                case "rf":
                    tvFeedingPractice.setText(MessageFormat.format(context.getString(R.string.infant_feeding_practice), context.getString(R.string.feeding_practice_rf)));
                    break;
                case "mf":
                    tvFeedingPractice.setText(MessageFormat.format(context.getString(R.string.infant_feeding_practice), context.getString(R.string.feeding_practice_mf)));
                    break;
                case "bf+":
                    tvFeedingPractice.setText(MessageFormat.format(context.getString(R.string.infant_feeding_practice), context.getString(R.string.feeding_practice_bf_plus)));
                    break;
                case "rf+":
                    tvFeedingPractice.setText(MessageFormat.format(context.getString(R.string.infant_feeding_practice), context.getString(R.string.feeding_practice_rf_plus)));
                    break;
                case "sbf":
                    tvFeedingPractice.setText(MessageFormat.format(context.getString(R.string.infant_feeding_practice), context.getString(R.string.feeding_practice_sbf)));
                    break;
                default:
                    tvFeedingPractice.setText(MessageFormat.format(context.getString(R.string.infant_feeding_practice), feedingPractice));
                    break;
            }
        }
    }

    private String getMapValue(Map<String, String> map, String key) {
        if (map.containsKey(key)) {
            if (map.get(key) != null && map.get(key).length() > 1) {
                return map.get(key).split(",")[0];
            }
            return map.get(key);
        }
        return "";
    }
}
