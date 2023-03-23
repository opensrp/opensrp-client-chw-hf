package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.core.activity.DefaultAncMedicalHistoryActivityFlv;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.LDDao;
import org.smartregister.chw.ld.domain.MemberObject;

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


                String[] hf_params = {
                        "name_of_the_health_care_provider",
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
                        "urine_protein",
                        "urine_acetone",
                        "urine_volume",
                        "cervix_dilation",
                        "descent_presenting_part",
                        "contraction_every_half_hour_frequency",
                        "contraction_every_half_hour_time"
                };

                extractHFVisit(visits, hf_params, hf_visits, x, context);

                x++;
            }

            processLastVisit();
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


    private void processLastVisit() {
        linearLayoutLastVisit.setVisibility(View.GONE);
    }


    protected void processFacilityVisit(List<Map<String, String>> hf_visits, List<Visit> visits, Context context) {
        if (hf_visits != null && hf_visits.size() > 0) {
            linearLayoutHealthFacilityVisit.setVisibility(View.VISIBLE);

            int x = 0;
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
                TextView tvUrineProtein = view.findViewById(R.id.urine_protein);
                TextView tvUrineAcetone = view.findViewById(R.id.urine_acetone);
                TextView tvUrineVolume = view.findViewById(R.id.urine_volume);
                TextView tvCervixDilation = view.findViewById(R.id.cervix_dilation);
                TextView tvDescentPresentingPart = view.findViewById(R.id.descent_presenting_part);
                TextView tvContraction = view.findViewById(R.id.contraction);
                TextView tvEdit = view.findViewById(R.id.textview_edit);

                // Updating visibility of EDIT button if the visit is the last visit
                if ((x == visits.size() - 1) && !LDDao.isClosed(visits.get(x).getBaseEntityId()))
                    tvEdit.setVisibility(View.VISIBLE);
                else
                    tvEdit.setVisibility(View.GONE);

                tvEdit.setOnClickListener(view1 -> {
                    ((Activity) context).finish();
                    Visit visit = visits.get(0);
                    MemberObject memberObject = LDDao.getLDMember(visit.getBaseEntityId());

                    if (memberObject != null && visit.getBaseEntityId() != null) {
                        ((Activity) context).finish();
                        LDPartographActivity.startMe((Activity) context, memberObject.getBaseEntityId(), true,
                                memberObject.getFirstName() + " " + memberObject.getMiddleName(), String.valueOf(new Period(new DateTime(memberObject.getAge()), new DateTime()).getYears()));
                    }
                });

                evaluatePartographDateTime(context, vals, tvPartographDateTime);
                evaluateFetalHeartRate(context, vals, tvFetalHeartRate);
                evaluateFetalMoulding(context, vals, tvFetalMoulding);
                evaluateAmnioticFluid(context, vals, tvAmnioticFluid);
                evaluatePulseRate(context, vals, tvPulseRate);
                evaluateRespiratoryRate(context, vals, tvRespiratoryRate);
                evaluateTemperature(context, vals, tvTemperature);
                evaluateBloodPressure(context, vals, tvBloodPressure);
                evaluateUrineProtein(context, vals, tvUrineProtein);
                evaluateUrineAcetone(context, vals, tvUrineAcetone);
                evaluateUrineVolume(context, vals, tvUrineVolume);
                evaluateCervixDilation(context, vals, tvCervixDilation);
                evaluateDescentPresentingPart(context, vals, tvDescentPresentingPart);
                evaluateContraction(context, vals, tvContraction);

                if (tvFetalHeartRate.getVisibility() == View.GONE &&
                        tvFetalMoulding.getVisibility() == View.GONE &&
                        tvAmnioticFluid.getVisibility() == View.GONE) {
                    view.findViewById(R.id.fetal_well_being).setVisibility(View.GONE);
                }

                if (tvPulseRate.getVisibility() == View.GONE &&
                        tvRespiratoryRate.getVisibility() == View.GONE &&
                        tvTemperature.getVisibility() == View.GONE &&
                        tvBloodPressure.getVisibility() == View.GONE &&
                        tvUrineProtein.getVisibility() == View.GONE &&
                        tvUrineAcetone.getVisibility() == View.GONE &&
                        tvUrineVolume.getVisibility() == View.GONE) {
                    view.findViewById(R.id.mother_well_being).setVisibility(View.GONE);
                }

                if (tvCervixDilation.getVisibility() == View.GONE &&
                        tvDescentPresentingPart.getVisibility() == View.GONE &&
                        tvContraction.getVisibility() == View.GONE) {
                    view.findViewById(R.id.fetal_progress_of_labour).setVisibility(View.GONE);
                }

                linearLayoutHealthFacilityVisitDetails.addView(view, 0);
                x++;

            }
        }
    }


    private void evaluatePartographDateTime(Context context, Map<String, String> vals, TextView tvPartographDateTime) {
        if (StringUtils.isBlank(vals.get("partograph_date"))) {
            tvPartographDateTime.setVisibility(View.GONE);
        } else {
            tvPartographDateTime.setText(MessageFormat.format(context.getString(R.string.partograph_date_time), getMapValue(vals, "partograph_date"), getMapValue(vals, "partograph_time"), getMapValue(vals, "name_of_the_health_care_provider")));
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

            String value = getMapValue(vals, "amniotic_fluid");
            int resourceId = context.getResources().
                    getIdentifier("amniotic_fluid_" + value, "string", context.getPackageName());

            tvAmnioticFluid.setText(MessageFormat.format(context.getString(R.string.amniotic_fluid), context.getString(resourceId)));
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

    private void evaluateUrineProtein(Context context, Map<String, String> vals, TextView tvUrineProtein) {
        if (StringUtils.isBlank(vals.get("urine_protein"))) {
            tvUrineProtein.setVisibility(View.GONE);
        } else {
            tvUrineProtein.setText(MessageFormat.format(context.getString(R.string.urine_protein), getMapValue(vals, "urine_protein")));
        }
    }

    private void evaluateUrineAcetone(Context context, Map<String, String> vals, TextView tvUrineAcetone) {
        if (StringUtils.isBlank(vals.get("urine_acetone"))) {
            tvUrineAcetone.setVisibility(View.GONE);
        } else {
            tvUrineAcetone.setText(MessageFormat.format(context.getString(R.string.urine_acetone), getMapValue(vals, "urine_acetone")));
        }
    }

    private void evaluateUrineVolume(Context context, Map<String, String> vals, TextView tvUrineVolume) {
        if (StringUtils.isBlank(vals.get("urine_volume"))) {
            tvUrineVolume.setVisibility(View.GONE);
        } else {
            tvUrineVolume.setText(MessageFormat.format(context.getString(R.string.urine_volume), getMapValue(vals, "urine_volume")));
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
        if (StringUtils.isBlank(vals.get("contraction_every_half_hour_frequency")) || StringUtils.isBlank(vals.get("contraction_every_half_hour_time"))) {
            tvContraction.setVisibility(View.GONE);
        } else {
            String contractionsTimeValue = getMapValue(vals, "contraction_every_half_hour_time");
            int resourceId = context.getResources().
                    getIdentifier("contraction_every_half_hour_time_" + contractionsTimeValue, "string", context.getPackageName());
            tvContraction.setText(MessageFormat.format(context.getString(R.string.contraction), getMapValue(vals, "contraction_every_half_hour_frequency"), context.getString(resourceId)));
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
