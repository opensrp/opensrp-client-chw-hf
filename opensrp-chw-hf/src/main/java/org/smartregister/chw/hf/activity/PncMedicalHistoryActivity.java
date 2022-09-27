package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.anc.domain.GroupedVisit;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.core.activity.CorePncMedicalHistoryActivity;
import org.smartregister.chw.core.dao.PNCDao;
import org.smartregister.chw.core.domain.MedicalHistory;
import org.smartregister.chw.core.helper.BaMedicalHistoryActivityHelper;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.PncMedicalHistoryActivityInteractor;
import org.smartregister.chw.hf.utils.PncVisitUtils;
import org.smartregister.chw.pnc.contract.BasePncMedicalHistoryContract;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class PncMedicalHistoryActivity extends CorePncMedicalHistoryActivity {

    private HfMedicalHistoryFlavor flavor = new HfMedicalHistoryFlavor();

    public static void startMe(Activity activity, MemberObject memberObject) {
        Intent intent = new Intent(activity, PncMedicalHistoryActivity.class);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, memberObject);
        activity.startActivity(intent);
    }


    @Override
    public View renderMedicalHistoryView(List<GroupedVisit> groupedVisits) {
        View view = flavor.bindViews(this);
        displayLoadingState(true);
        flavor.processViewData(groupedVisits, this, memberObject);
        displayLoadingState(false);
        return view;
    }

    @Override
    protected BasePncMedicalHistoryContract.Interactor getPncMedicalHistoryInteractor() {
        return new PncMedicalHistoryActivityInteractor();
    }

    private class HfMedicalHistoryFlavor extends BaMedicalHistoryActivityHelper {
        private final StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        @Override
        protected void processChildDetails(List<Visit> visits, String memberName) {
            this.visits = visits;
            String childName = StringUtils.isNotBlank(memberName) ? memberName : "";
            addChildDetailsView(childName);
            medicalHistories = new ArrayList<>(); // New history list for child details

            for (int i = visits.size() - 1; i >= 0; i--) {
                Visit visit = visits.get(i);
                Map<String, String> childVisitDetails = new HashMap<>();

                // Note the below HashMap are only used for ordering and categorization os the keys
                Map<String, String> generalExamination = new HashMap<>();
                Map<String, String> immunization = new HashMap<>();
                Map<String, String> growthData = new HashMap<>();
                //===============================================================================

                for (Map.Entry<String, List<VisitDetail>> entry : visit.getVisitDetails().entrySet()) {
                    String val = getText(entry.getValue());

                    switch (entry.getKey()) {
                        // general examination
                        case "child_activeness":
                        case "hb_level":
                        case "hiv_antibody_test":
                        case "temperature":
                        case "septicaemia":
                        case "umbilical_cord":
                        case "jaundice":
                        case "skin_infection":
                        case "kangaroo_enrollment":
                            generalExamination.put(entry.getKey(), val);
                            break;
                        // immunization
                        case "child_opv0_vaccination":
                        case "child_bcg_vaccination":
                            immunization.put(entry.getKey(), val);
                            break;
                        // growth and nutrition
                        case "weight":
                        case "height":
                        case "head_circumference":
                        case "upper_arm_circumference":
                        case "feeding_options":
                            growthData.put(entry.getKey(), val);
                            break;
                        default:
                    }
                }

                childVisitDetails.putAll(generalExamination);
                childVisitDetails.putAll(immunization);
                childVisitDetails.putAll(growthData);

                processData(childVisitDetails, R.string.pnc_medical_history_child_general_examination_title, i, visit.getDate());
            }
            addMedicalHistoriesView();
        }


        protected void processImmunization(Map<String, String> immunization) {
            Context context = PncMedicalHistoryActivity.this;
            if (immunization != null && immunization.size() > 0) {

                List<String> immunizationDetails = new ArrayList<>();
                for (Map.Entry<String, String> entry : immunization.entrySet()) {
                    if (entry.getValue() != null) {
                        String entryValue = entry.getValue().equalsIgnoreCase("Vaccine not given") ? context.getString(R.string.pnc_vaccine_not_given) : entry.getValue();
                        if (entry.getKey().contains("bcg")) {
                            if (entryValue.equalsIgnoreCase(context.getString(R.string.no)))
                                immunizationDetails.add(MessageFormat.format(context.getString(R.string.pnc_bcg_not_done), entryValue));
                            else
                                immunizationDetails.add(MessageFormat.format(context.getString(R.string.pnc_bcg), entryValue));
                        } else if (entry.getKey().contains("opv0")) {
                            if (entryValue.equalsIgnoreCase(context.getString(R.string.no)))
                                immunizationDetails.add(MessageFormat.format(context.getString(R.string.pnc_opv0_not_done), entryValue));
                            else
                                immunizationDetails.add(MessageFormat.format(context.getString(R.string.pnc_opv0), entryValue));
                        }
                    }
                }

                if (medicalHistories == null) {
                    medicalHistories = new ArrayList<>();
                }

                MedicalHistory medicalHistory = new MedicalHistory();
                medicalHistory.setTitle(context.getString(R.string.pnc_medical_history_child_immunizations_title));
                medicalHistory.setText(immunizationDetails);
                medicalHistories.add(medicalHistory);
            }
        }

        protected void processData(Map<String, String> growth_data, int titleStringResourceId, int visitNumber, Date date) {
            Context context = PncMedicalHistoryActivity.this;
            if (growth_data != null && growth_data.size() > 0) {

                List<String> nutritionDetails = new ArrayList<>();
                for (Map.Entry<String, String> entry : growth_data.entrySet()) {
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                    spannableStringBuilder.append(getStringResource(context, "pnc_key_", entry.getKey()), boldSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE).append(" : ").append(getStringResource(context, "", entry.getValue()));

                    nutritionDetails.add(spannableStringBuilder.toString());

                }
                MedicalHistory medicalHistory = new MedicalHistory();
                medicalHistory.setTitle(context.getString(titleStringResourceId) + " " + ++visitNumber + " : " + simpleDateFormat.format(date));
                medicalHistory.setText(nutritionDetails);

                if (medicalHistories == null) {
                    medicalHistories = new ArrayList<>();
                }

                medicalHistories.add(medicalHistory);
            }
        }

        @Override
        protected void processMotherDetails(List<Visit> visits, MemberObject memberObject) {
            this.visits = visits;
            Map<Integer, String> homeVisitMap = new LinkedHashMap<>();
            List<Map<String, String>> healthFacilityVisits = new ArrayList<>();
            int x = visits.size() - 1;
            while (x >= 0) {
                Map<String, String> healthFacilityVisitMap = new LinkedHashMap<>();
                extractHealthFacilityVisits(visits, healthFacilityVisitMap, x);
                healthFacilityVisits.add(healthFacilityVisitMap);
                x--;
            }

            extractHomeVisits(visits, homeVisitMap);
            processLastVisitDate(memberObject.getBaseEntityId());
            addMotherDetailsView(memberObject.getFullName());

            medicalHistories = new ArrayList<>(); // New history list for mother's details

            processHealthFacilityVisit(healthFacilityVisits, visits.get(0).getBaseEntityId());


//            processHomeVisits(homeVisitMap);
            processFamilyPlanning(visits);
            addMedicalHistoriesView();
        }

        @Override
        protected void extractHealthFacilityVisits(List<Visit> visits, Map<String, String> healthFacilityVisitMap, int count) {
            Visit visit = visits.get(count);
            Map<String, List<VisitDetail>> map = visit.getVisitDetails();

            Map<String, String> generalExamination = new HashMap<>();
            Map<String, String> familyPlanning = new HashMap<>();
            Map<String, String> suppliments = new HashMap<>();
            Map<String, String> hivMap = new HashMap<>();

            for (String key : map.keySet()) {
                List<VisitDetail> visitDetails = map.get(key);
                for (VisitDetail visitDetail : visitDetails) {

                    switch (visitDetail.getVisitKey()) {
                        // general examination
                        case "systolic":
                        case "diastolic":
                        case "hb_level":
                        case "temperature":
                        case "weight":
                        case "breast_milk_flow":
                        case "engorgement_mastitis":
                        case "abscess":
                        case "perineum_infection":
                        case "uterus_assessment":
                        case "lochia_assessment":
                        case "vaginal_assessment":
                        case "fistula":
                        case "puerperal_psychosis":
                        case "mental_illness_symptom":
                            generalExamination.put(visitDetail.getVisitKey(), visitDetail.getDetails());
                            break;
                        // Family Planning
                        case "education_counselling_given":
                        case "iec_given":
                        case "using_family_planning_method":
                        case "method_provided":
                            familyPlanning.put(visitDetail.getVisitKey(), visitDetail.getDetails());
                            break;
                        // Suppliments
                        case "iron_and_folic_acid":
                        case "vitamin_a":
                            suppliments.put(visitDetail.getVisitKey(), visitDetail.getDetails());
                            break;
                        // HIV Test
                        case "hiv":
                        case "hiv_test_result_date":
                            hivMap.put(visitDetail.getVisitKey(), visitDetail.getDetails());
                            break;
                        default:
                    }
                }
            }

            healthFacilityVisitMap.put("followup_visit_date", map.get("followup_visit_date").get(0).getDetails());
            healthFacilityVisitMap.putAll(generalExamination);
            healthFacilityVisitMap.putAll(familyPlanning);
            healthFacilityVisitMap.putAll(suppliments);
            healthFacilityVisitMap.putAll(hivMap);

        }


        protected void processHealthFacilityVisit(List<Map<String, String>> healthFacilityVisits, String baseEntityId) {
            Context context = PncMedicalHistoryActivity.this;
            Date deliveryDate = PNCDao.getPNCDeliveryDate(baseEntityId);
            int x = healthFacilityVisits.size();
            for (Map<String, String> healthFacilityVisit : healthFacilityVisits) {
                Date visitDate;
                String visitTypeSubTitle = "";
                try {
                    String followupVisitDate = healthFacilityVisit.get("followup_visit_date");
                    if (StringUtils.isNotBlank(followupVisitDate)) {
                        visitDate = simpleDateFormat.parse(followupVisitDate);
                        visitTypeSubTitle = getVisitType(context, deliveryDate, visitDate) + " : " + healthFacilityVisit.get("followup_visit_date");
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
                MedicalHistory medicalHistory = new MedicalHistory();
                medicalHistory.setTitle(MessageFormat.format(PncMedicalHistoryActivity.this.getString(org.smartregister.chw.core.R.string.pnc_health_facility_visit_num), x + " " + visitTypeSubTitle));
                List<String> hfDetails = new ArrayList<>();

                List<String> keysNotIncluded = Arrays.asList("followup_visit_date", "visit_number", "anc_visit_date");

                for (String key : healthFacilityVisit.keySet()) {
                    if (!keysNotIncluded.contains(key)) {
                        String value = healthFacilityVisit.get(key);
                        if (StringUtils.isNotBlank(value)) {

                            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                            spannableStringBuilder.append(getStringResource(context, "pnc_key_", key), boldSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE).append(" : ").append(getStringResource(context, "", value));

                            hfDetails.add(spannableStringBuilder.toString());
                        }
                    }
                }
                medicalHistory.setText(hfDetails);
                medicalHistories.add(medicalHistory);
                x--;
            }
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


        private String getVisitType(Context context, Date deliveryDate, Date visitDate) {
            if (PncVisitUtils.getElapsedTimeDays(deliveryDate, visitDate) <= 2) {
                return context.getResources().getString(R.string.pnc_visit_less_than_48_hours);
            } else if (PncVisitUtils.getElapsedTimeDays(deliveryDate, visitDate) > 2 && PncVisitUtils.getElapsedTimeDays(deliveryDate, visitDate) <= 7) {
                return context.getResources().getString(R.string.pnc_visit_3_to_7_days);
            } else if (PncVisitUtils.getElapsedTimeDays(deliveryDate, visitDate) > 7 && PncVisitUtils.getElapsedTimeDays(deliveryDate, visitDate) <= 28) {
                return context.getResources().getString(R.string.pnc_visit_8_to_28_days);
            } else {
                return context.getResources().getString(R.string.pnc_visit_29_to_42_days);
            }
        }
    }
}
