package org.smartregister.chw.hf.activity;

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
            String has_card = "No";
            List<Map<String, String>> hf_visits = new ArrayList<>();

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

                String[] hf_params = {"weight", "height", "followup_status", "health_status", "infant_feeding_practice", "anc_visit_date"};
                extractHFVisit(visits, hf_params, hf_visits, x, context);

                x++;
            }

            processLastVisit(days, context);
            processAncCard(has_card, context);
            processFacilityVisit(hf_visits, context);
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


    protected void processFacilityVisit(List<Map<String, String>> hf_visits, Context context) {
        if (hf_visits != null && hf_visits.size() > 0) {
            linearLayoutHealthFacilityVisit.setVisibility(View.VISIBLE);

            int x = 0;
            for (Map<String, String> vals : hf_visits) {
                View view = inflater.inflate(R.layout.medical_history_anc_visit, null);

                TextView tvTitle = view.findViewById(R.id.title);
                TextView tvGA = view.findViewById(R.id.gest_age);
                TextView tvFundalHeight = view.findViewById(R.id.fundal_height);
                TextView tvHeight = view.findViewById(R.id.height);
                TextView tvWeight = view.findViewById(R.id.weight);
                TextView tvBP = view.findViewById(R.id.bp);
                TextView tvHB = view.findViewById(R.id.hb);
                TextView tvMrdtMalaria = view.findViewById(R.id.mrdt_malaria);
                TextView tvHivStatus = view.findViewById(R.id.hiv_status);


                tvTitle.setText(MessageFormat.format(context.getString(org.smartregister.chw.core.R.string.anc_visit_date), x + 1, getMapValue(vals, "health_status")));

                if (StringUtils.isBlank(getMapValue(vals, "infant_feeding_practice"))) {
                    tvFundalHeight.setVisibility(View.GONE);
                } else {
                    tvFundalHeight.setText(MessageFormat.format(context.getString(R.string.fundal_height), getMapValue(vals, "infant_feeding_practice")));
                }

                tvGA.setText(MessageFormat.format(context.getString(R.string.gestation_age), getMapValue(vals, "followup_status")));
                if (StringUtils.isBlank(getMapValue(vals, "weight"))) {
                    tvWeight.setVisibility(View.GONE);
                } else {
                    tvWeight.setText(MessageFormat.format(context.getString(org.smartregister.chw.core.R.string.weight_in_kgs), getMapValue(vals, "weight")));
                }

                if (StringUtils.isBlank(getMapValue(vals, "height"))) {
                    tvHeight.setVisibility(View.GONE);
                } else {
                    tvHeight.setText(MessageFormat.format(context.getString(R.string.height_in_cm), getMapValue(vals, "height")));
                }


                if (StringUtils.isBlank(getMapValue(vals, "mRDT_for_malaria"))) {
                    tvMrdtMalaria.setVisibility(View.GONE);
                } else {
                    tvMrdtMalaria.setText(MessageFormat.format(context.getString(R.string.malaria), getMapValue(vals, "mRDT_for_malaria")));
                }

                if (StringUtils.isBlank(getMapValue(vals, "health_status"))) {
                    tvHivStatus.setVisibility(View.GONE);
                } else {
                    tvHivStatus.setText(MessageFormat.format(context.getString(R.string.hiv_status), getMapValue(vals, "health_status")));
                }


                linearLayoutHealthFacilityVisitDetails.addView(view, 0);

                x++;
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
