package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.hf.utils.Constants.Events.PNC_CHILD_FOLLOWUP;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.StyleSpan;
import android.view.View;

import org.smartregister.chw.anc.domain.GroupedVisit;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.core.activity.CorePncMedicalHistoryActivity;
import org.smartregister.chw.core.domain.Child;
import org.smartregister.chw.core.domain.MedicalHistory;
import org.smartregister.chw.core.helper.BaMedicalHistoryActivityHelper;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.HfChildDao;
import org.smartregister.chw.hf.interactor.PncMedicalHistoryActivityInteractor;
import org.smartregister.chw.pnc.contract.BasePncMedicalHistoryContract;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class PncNoMotherChildMedicalHistoryActivity extends CorePncMedicalHistoryActivity {

    private HfMedicalHistoryFlavor flavor = new HfMedicalHistoryFlavor();

    public static void startMe(Activity activity, MemberObject memberObject) {
        Intent intent = new Intent(activity, PncNoMotherChildMedicalHistoryActivity.class);
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
        private final StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);

        private final StyleSpan italicSpan = new StyleSpan(Typeface.ITALIC);

        private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        public void processViewData(List<GroupedVisit> groupedVisits, Context context, MemberObject memberObject) {
            if (groupedVisits.size() > 0) {
                for (GroupedVisit groupedVisit : groupedVisits) {
                    // Process child's details
                    processChildDetails(groupedVisit.getVisitList(), groupedVisit.getName());
                }
            }
        }

        protected void processImmunization(Map<String, String> immunization) {
            Context context = PncNoMotherChildMedicalHistoryActivity.this;
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

        @Override
        protected void processChildDetails(List<Visit> visits, String memberName) {
            this.visits = visits;
            Child child = HfChildDao.getNoMotherChild(visits.get(0).getBaseEntityId());
            addChildDetailsView(child.getFirstName() + " " + child.getMiddleName() + " " + child.getLastName());
            medicalHistories = new ArrayList<>(); // New history list for child details

            for (int i = visits.size() - 1; i >= 0; i--) {
                Visit visit = visits.get(i);
                if (!visit.getVisitType().equals(PNC_CHILD_FOLLOWUP)) {
                    continue;
                }
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

        protected void processData(Map<String, String> growth_data, int titleStringResourceId, int visitNumber, Date date) {
            Context context = PncNoMotherChildMedicalHistoryActivity.this;
            if (growth_data != null && growth_data.size() > 0) {

                List<SpannableStringBuilder> nutritionDetails = new ArrayList<>();
                for (Map.Entry<String, String> entry : growth_data.entrySet()) {
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

                    spannableStringBuilder.append(getStringResource(context, "pnc_key_", entry.getKey()), boldSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE).append(" : ");
                    if (entry.getValue().contains(",")) {
                        String[] values = entry.getValue().split(",");
                        for (String value : values) {
                            spannableStringBuilder.append("\n");
                            spannableStringBuilder.append(getStringResource(context, "", value) + "\n", new BulletSpan(10), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    } else {
                        spannableStringBuilder.append(getStringResource(context, "", entry.getValue()), italicSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    nutritionDetails.add(spannableStringBuilder);
                }
                MedicalHistory medicalHistory = new MedicalHistory();
                medicalHistory.setTitle(context.getString(titleStringResourceId) + " " + (visitNumber + 1) + " : " + simpleDateFormat.format(date));
                medicalHistory.setSpannableStringBuilders(nutritionDetails);

                if (medicalHistories == null) {
                    medicalHistories = new ArrayList<>();
                }

                medicalHistories.add(medicalHistory);
            }
        }

        @Override
        protected void extractHealthFacilityVisits(List<Visit> visits, Map<String, String> healthFacilityVisitMap, int count) {
            Visit visit = visits.get(count);
            Map<String, List<VisitDetail>> map = visit.getVisitDetails();

            Map<String, String> generalExamination = new HashMap<>();

            for (String key : map.keySet()) {
                List<VisitDetail> visitDetails = map.get(key);
                for (VisitDetail visitDetail : visitDetails) {

                    switch (visitDetail.getVisitKey()) {
                        // general examination
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
                        default:
                    }
                }
            }

            healthFacilityVisitMap.put("followup_visit_date", map.get("followup_visit_date").get(0).getDetails());
            healthFacilityVisitMap.putAll(generalExamination);
        }

        private String getStringResource(Context context, String prefix, String resourceName) {
            int resourceId = context.getResources().getIdentifier(prefix + resourceName.trim(), "string", context.getPackageName());
            try {
                return context.getString(resourceId);
            } catch (Exception e) {
                Timber.e(e);
                return prefix + resourceName;
            }
        }
    }
}
