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

public class LDPartographDetailsActivityFlv extends DefaultAncMedicalHistoryActivityFlv {

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
            List<Map<String, String>> hf_visits = new ArrayList<>();

            int x = 0;
            while (x < visits.size()) {

                // the first object in this list is the days difference
                if (x == 0) {
                    days = Days.daysBetween(new DateTime(visits.get(visits.size() - 1).getDate()), new DateTime()).getDays();
                }


                String[] hf_params = {
                        "partograph_date",
                        "partograph_time",
                        "fetal_heart_rate",
                        "moulding",
                        "moulding_options",
                        "amniotic_fluid",
                        "pulse_rate",
                        "respiratory_rate",
                        "temperature",
                        "systolic",
                        "diastolic",
                        "urine",
                        "cervix_dilation",
                        "descent_presenting_part",
                        "contraction_every_half_hour_frequency",
                        "contraction_every_half_hour_time"
                };

                extractHFVisit(visits, hf_params, hf_visits, x, context);

                x++;
            }

            processLastVisit();
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


    private void processLastVisit() {
        linearLayoutLastVisit.setVisibility(View.GONE);
    }


    protected void processFacilityVisit(List<Map<String, String>> hf_visits, Context context) {
        if (hf_visits != null && hf_visits.size() > 0) {
            linearLayoutHealthFacilityVisit.setVisibility(View.VISIBLE);

            for (Map<String, String> vals : hf_visits) {
                View view = inflater.inflate(R.layout.ld_patograph_details, null);
                TextView tvPartographDateTime = view.findViewById(R.id.partograph_date_time);
                TextView tvFetalHeartRate = view.findViewById(R.id.fetal_heart_rate);
                TextView tvFetalMoulding = view.findViewById(R.id.fetal_moulding);
                TextView tvAmnioticFluid = view.findViewById(R.id.amniotic_fluid);
                TextView tvPulseRate = view.findViewById(R.id.pulse_rate);
                TextView tvRespiratoryRate = view.findViewById(R.id.respiratory_rate);
                TextView tvTemperature = view.findViewById(R.id.temperature);
                TextView tvBloodPressure = view.findViewById(R.id.blood_pressure);
                TextView tvProteinAcetone = view.findViewById(R.id.protein_acetone);
                TextView tvCervixDilation = view.findViewById(R.id.cervix_dilation);
                TextView tvDescentPresentingPart = view.findViewById(R.id.descent_presenting_part);
                TextView tvContraction = view.findViewById(R.id.contraction);

                evaluatePartographDateTime(context, vals, tvPartographDateTime);
                evaluateFetalHeartRate(context, vals, tvFetalHeartRate);
                evaluateFetalMoulding(context, vals, tvFetalMoulding);
                evaluateAmnioticFluid(context, vals, tvAmnioticFluid);
                evaluatePulseRate(context, vals, tvPulseRate);
                evaluateRespiratoryRate(context, vals, tvRespiratoryRate);
                evaluateTemperature(context, vals, tvTemperature);
                evaluateBloodPressure(context, vals, tvBloodPressure);
                evaluateProteinAcetone(context, vals, tvProteinAcetone);
                evaluateCervixDilation(context, vals, tvCervixDilation);
                evaluateDescentPresentingPart(context, vals, tvDescentPresentingPart);
                evaluateContraction(context, vals, tvContraction);


                linearLayoutHealthFacilityVisitDetails.addView(view, 0);

            }
        }
    }


    private void evaluatePartographDateTime(Context context, Map<String, String> vals, TextView tvPartographDateTime) {
        if (StringUtils.isBlank(vals.get("partograph_date"))) {
            tvPartographDateTime.setVisibility(View.GONE);
        } else {
            tvPartographDateTime.setText(MessageFormat.format(context.getString(R.string.partograph_date_time), getMapValue(vals, "partograph_date"), getMapValue(vals, "partograph_time")));
        }
    }


    private void evaluateFetalHeartRate(Context context, Map<String, String> vals, TextView tvFetalHeartRate) {
        if (StringUtils.isBlank(vals.get("fetal_heart_rate"))) {
            tvFetalHeartRate.setVisibility(View.GONE);
        } else {
            tvFetalHeartRate.setText(MessageFormat.format(context.getString(R.string.fetal_heart_rate), getMapValue(vals, "fetal_heart_rate")));
        }
    }

    private void evaluateFetalMoulding(Context context, Map<String, String> vals, TextView tvFetalMoulding) {
        if (StringUtils.isBlank(vals.get("moulding"))) {
            tvFetalMoulding.setVisibility(View.GONE);
        } else {
            tvFetalMoulding.setText(MessageFormat.format(context.getString(R.string.fetal_moulding), getMapValue(vals, "moulding")));
        }
    }

    private void evaluateAmnioticFluid(Context context, Map<String, String> vals, TextView tvAmnioticFluid) {
        if (StringUtils.isBlank(vals.get("amniotic_fluid"))) {
            tvAmnioticFluid.setVisibility(View.GONE);
        } else {
            tvAmnioticFluid.setText(MessageFormat.format(context.getString(R.string.amniotic_fluid), getMapValue(vals, "amniotic_fluid")));
        }
    }

    private void evaluatePulseRate(Context context, Map<String, String> vals, TextView tvPulseRate) {
        if (StringUtils.isBlank(vals.get("pulse_rate"))) {
            tvPulseRate.setVisibility(View.GONE);
        } else {
            tvPulseRate.setText(MessageFormat.format(context.getString(R.string.pulse_rate), getMapValue(vals, "pulse_rate")));
        }
    }

    private void evaluateRespiratoryRate(Context context, Map<String, String> vals, TextView tvRespiratoryRate) {
        if (StringUtils.isBlank(vals.get("respiratory_rate"))) {
            tvRespiratoryRate.setVisibility(View.GONE);
        } else {
            tvRespiratoryRate.setText(MessageFormat.format(context.getString(R.string.respiratory_rate), getMapValue(vals, "respiratory_rate")));
        }
    }

    private void evaluateTemperature(Context context, Map<String, String> vals, TextView tvTemperature) {
        if (StringUtils.isBlank(vals.get("temperature"))) {
            tvTemperature.setVisibility(View.GONE);
        } else {
            tvTemperature.setText(MessageFormat.format(context.getString(R.string.temperature), getMapValue(vals, "temperature")));
        }
    }

    private void evaluateBloodPressure(Context context, Map<String, String> vals, TextView tvBloodPressure) {
        if (StringUtils.isBlank(vals.get("systolic"))) {
            tvBloodPressure.setVisibility(View.GONE);
        } else {
            tvBloodPressure.setText(MessageFormat.format(context.getString(R.string.blood_pressure), getMapValue(vals, "systolic"), getMapValue(vals, "diastolic")));
        }
    }

    private void evaluateProteinAcetone(Context context, Map<String, String> vals, TextView tvProteinAcetone) {
        if (StringUtils.isBlank(vals.get("urine"))) {
            tvProteinAcetone.setVisibility(View.GONE);
        } else {
            tvProteinAcetone.setText(MessageFormat.format(context.getString(R.string.protein_acetone), getMapValue(vals, "urine")));
        }
    }

    private void evaluateCervixDilation(Context context, Map<String, String> vals, TextView tvCervixDilation) {
        if (StringUtils.isBlank(vals.get("cervix_dilation"))) {
            tvCervixDilation.setVisibility(View.GONE);
        } else {
            tvCervixDilation.setText(MessageFormat.format(context.getString(R.string.cervix_dilation), getMapValue(vals, "cervix_dilation")));
        }
    }

    private void evaluateDescentPresentingPart(Context context, Map<String, String> vals, TextView tvDescentPresentingPart) {
        if (StringUtils.isBlank(vals.get("descent_presenting_part"))) {
            tvDescentPresentingPart.setVisibility(View.GONE);
        } else {
            tvDescentPresentingPart.setText(MessageFormat.format(context.getString(R.string.descent_presenting_part), getMapValue(vals, "descent_presenting_part")));
        }
    }

    private void evaluateContraction(Context context, Map<String, String> vals, TextView tvContraction) {
        if (StringUtils.isBlank(vals.get("contraction_every_half_hour_frequency"))) {
            tvContraction.setVisibility(View.GONE);
        } else {
            tvContraction.setText(MessageFormat.format(context.getString(R.string.contraction), getMapValue(vals, "contraction_every_half_hour_frequency"), getMapValue(vals, "contraction_every_half_hour_time")));
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
