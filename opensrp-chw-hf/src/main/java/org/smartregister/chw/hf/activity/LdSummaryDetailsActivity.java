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
import org.apache.commons.text.WordUtils;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.presenter.BaseAncMedicalHistoryPresenter;
import org.smartregister.chw.core.activity.CoreAncMedicalHistoryActivity;
import org.smartregister.chw.core.activity.DefaultAncMedicalHistoryActivityFlv;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.LDSummaryDetailsInteractor;
import org.smartregister.chw.ld.domain.MemberObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class LdSummaryDetailsActivity extends CoreAncMedicalHistoryActivity {
    private static MemberObject ldMemberObject;
    private final Flavor flavor = new LdExaminationDetailsActivityFlv();
    private ProgressBar progressBar;

    public static void startMe(Activity activity, MemberObject memberObject) {
        Intent intent = new Intent(activity, LdSummaryDetailsActivity.class);
        ldMemberObject = memberObject;
        activity.startActivity(intent);
    }

    @Override
    public void initializePresenter() {
        presenter = new BaseAncMedicalHistoryPresenter(new LDSummaryDetailsInteractor(), this, ldMemberObject.getBaseEntityId());
    }

    @Override
    public void setUpView() {
        linearLayout = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.linearLayoutMedicalHistory);
        progressBar = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.progressBarMedicalHistory);

        TextView tvTitle = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.tvTitle);
        tvTitle.setText(getString(org.smartregister.chw.opensrp_chw_anc.R.string.back_to, ldMemberObject.getFullName()));

        ((TextView) findViewById(R.id.medical_history)).setText(getString(R.string.ld_details_title));
    }

    @Override
    public View renderView(List<Visit> visits) {
        super.renderView(visits);
        View view = flavor.bindViews(this);
        displayLoadingState(true);
        flavor.processViewData(visits, this);
        displayLoadingState(false);
        TextView visitTitle = view.findViewById(org.smartregister.chw.core.R.id.customFontTextViewHealthFacilityVisitTitle);
        visitTitle.setText(R.string.visits_history);
        return view;
    }

    @Override
    public void displayLoadingState(boolean state) {
        progressBar.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    private static class LdExaminationDetailsActivityFlv extends DefaultAncMedicalHistoryActivityFlv {
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
                List<LinkedHashMap<String, String>> hf_visits = new ArrayList<>();

                int x = 0;
                while (x < visits.size()) {
                    LinkedHashMap<String, String> visitDetails = new LinkedHashMap<>();

                    String[] generalExamination = {"general_condition", "pulse_rate", "respiratory_rate", "temperature", "systolic", "diastolic", "urine_protein", "urine_acetone", "fundal_height", "lie", "presentation", "contraction_frequency", "contraction_in_ten_minutes", "fetal_heart_rate", "level"};
                    extractVisitDetails(visits, generalExamination, visitDetails, x, context);

                    String[] vaginalExamination = {"vaginal_exam_date", "vaginal_exam_time", "cervix_state", "cervix_dilation", "presenting_part", "occiput_position", "mento_position", "sacro_position", "dorso_position", "moulding", "moulding_options", "station", "amniotic_fluid", "decision", "forecasted_svd_time"};
                    extractVisitDetails(visits, vaginalExamination, visitDetails, x, context);

                    String[] hivTest = {"hiv_test_conducted", "reason_for_no_hiv_test", "hiv_counselling_before_testing", "hiv_counselling_after_testing"};
                    extractVisitDetails(visits, hivTest, visitDetails, x, context);

                    String[] bloodGroupTest = {"blood_group", "rh_factor", "management_provided_for_rh", "reason_for_not_conducting_blood_group_test"};
                    extractVisitDetails(visits, bloodGroupTest, visitDetails, x, context);

                    String[] hbTest = {"hb_test_conducted", "reason_for_not_conducting_hb_test", "other_reason_hb_test_not_conducted", "hb_level", "management_provided_for_hb_level"};
                    extractVisitDetails(visits, hbTest, visitDetails, x, context);

                    String[] syphilisTest = {"syphilis", "management_provided_for_syphilis"};
                    extractVisitDetails(visits, syphilisTest, visitDetails, x, context);

                    String[] malariaTest = {"malaria", "management_provided_for_malaria", "reason_for_not_conducting_malaria_test", "other_reason_for_not_conducting_malaria_test"};
                    extractVisitDetails(visits, malariaTest, visitDetails, x, context);


                    String[] uterotonic = {"uterotonic"};
                    extractVisitDetails(visits, uterotonic, visitDetails, x, context);

                    String[] placentaAndMembrane = {"method_used_to_remove_the_placenta", "placenta_and_membrane_expulsion", "type_of_incomplete_placenta", "placenta_removed_by_hand", "conducted_mva", "administered_antibiotics", "removal_date", "removal_duration", "estimated_blood_loss", "provided_blood_transfusion", "name_of_the_provider_who_removed_the_placenta"};
                    extractVisitDetails(visits, placentaAndMembrane, visitDetails, x, context);

                    String[] uterusMassage = {"uterus_massage_after_delivery", "reason_for_not_massaging_uterus_after_delivery"};
                    extractVisitDetails(visits, uterusMassage, visitDetails, x, context);

                    String[] eclampsiaManagement = {"has_signs_of_eclampsia", "administered_magnesium_sulphate", "reason_for_not_administering_magnesium_sulphate", "other_reason_for_for_not_administering_magnesium_sulphate"};
                    extractVisitDetails(visits, eclampsiaManagement, visitDetails, x, context);

                    String[] motherStatus = {"status", "cause_of_death", "time_of_death", "mode_of_delivery", "delivery_place", "designation_of_delivery_personnel", "name_of_delivery_person", "supervised_by_occupation", "name_of_supervising_person", "number_of_children_born", "delivery_date", "delivery_time"};
                    extractVisitDetails(visits, motherStatus, visitDetails, x, context);

                    String[] motherObservation = {"vagina_observation", "vaginal_bleeding_observation", "perineum_observation", "degree_of_perineum_tear", "perineum_repair_person_name", "perineum_repair_occupation", "cervix_observation", "systolic", "diastolic", "pulse_rate", "temperature", "uterus_contraction", "urination", "observation_date", "observation_time"};
                    extractVisitDetails(visits, motherObservation, visitDetails, x, context);

                    String[] maternalComplications = {"maternal_complications_before_delivery", "maternal_complications_before_delivery_other", "maternal_complications_during_and_after_delivery", "maternal_complications_during_and_after_delivery_other"};
                    extractVisitDetails(visits, maternalComplications, visitDetails, x, context);

                    String[] familyPlanning = {"family_planning_counselling_after_delivery", "family_planning_methods_selected", "other_family_planning_methods_selected"};
                    extractVisitDetails(visits, familyPlanning, visitDetails, x, context);

                    String[] immediateNewBornCare = {"newborn_status", "still_birth_choice", "child_delivery_date", "child_delivery_time", "sex"};
                    extractVisitDetails(visits, immediateNewBornCare, visitDetails, x, context);

                    String[] totalApgarScoreAt1Minutes = {"apgar_score_at_1_minute"};
                    extractVisitDetails(visits, totalApgarScoreAt1Minutes, visitDetails, x, context);

                    String[] immediateNewBornCareAgparScoreAt1Minute = {"apgar_activity_score_at_1_minute", "apgar_pulse_score_at_1_minute", "apgar_grimace_on_stimulation_score_at_1_minute", "apgar_appearance_score_at_1_minute", "apgar_respiration_score_at_1_minute", "apgar_score_at_1_minute"};
                    extractVisitDetails(visits, immediateNewBornCareAgparScoreAt1Minute, visitDetails, x, context);

                    String[] totalApgarScoreAt5Minutes = {"apgar_score_at_5_minutes"};
                    extractVisitDetails(visits, totalApgarScoreAt5Minutes, visitDetails, x, context);

                    String[] immediateNewBornCareAgparScoreAt5Minutes = {"apgar_score_activity_label_at_5_mins", "apgar_activity_score_at_5_minutes", "apgar_pulse_score_at_5_minutes", "apgar_grimace_on_stimulation_score_at_5_minutes", "apgar_appearance_score_at_5_minutes", "apgar_respiration_score_at_5_minutes", "apgar_score_at_5_minutes"};
                    extractVisitDetails(visits, immediateNewBornCareAgparScoreAt5Minutes, visitDetails, x, context);


                    String[] newBornCare = {"resuscitation_question", "keep_warm", "cord_bleeding", "early_bf_1hr", "reason_for_not_breast_feeding_within_one_hour", "other_reason_for_not_breast_feeding_within_one_hour", "eye_care", "reason_for_not_giving_eye_care", "other_reason_for_not_giving_eye_care"};
                    extractVisitDetails(visits, newBornCare, visitDetails, x, context);

                    String[] immediateNewBornCareVaccinations = {"child_bcg_vaccination", "reason_for_not_providing_bcg_vacc", "other_reason_for_not_providing_bcg_vacc", "child_opv0_vaccination", "reason_for_not_providing_opv0_vacc", "other_reason_for_not_providing_opv0_vacc", "child_hepatitis_b_vaccination", "reason_for_not_providing_hepatitis_b_vacc", "other_reason_for_not_providing_hepatitis_b_injection", "child_vitamin_k_injection", "reason_for_not_providing_vitamin_k_injection", "other_reason_for_not_providing_vitamin_k_injection"};
                    extractVisitDetails(visits, immediateNewBornCareVaccinations, visitDetails, x, context);

                    String[] immediateNewBornCareHei = {"risk_category", "provided_azt_nvp_syrup", "provided_other_combinations", "specify_the_combinations", "number_of_azt_nvp_days_dispensed", "reason_for_not_providing_other_combination", "other_reason_for_not_providing_other_combination", "collect_dbs", "reason_not_collecting_dbs", "sample_collection_date", "dna_pcr_collection_time", "sample_id", "provided_nvp_syrup", "number_of_nvp_days_dispensed", "reason_for_not_providing_nvp_syrup", "other_reason_for_not_providing_nvp_syrup"};
                    extractVisitDetails(visits, immediateNewBornCareHei, visitDetails, x, context);

                    hf_visits.add(visitDetails);

                    x++;
                }

                updateLastVisitView();
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


        private void updateLastVisitView() {
            linearLayoutLastVisit.setVisibility(View.GONE);
        }


        protected void processVisit(List<LinkedHashMap<String, String>> community_visits, Context context, List<Visit> visits) {
            if (community_visits != null && community_visits.size() > 0) {
                linearLayoutHealthFacilityVisit.setVisibility(View.VISIBLE);

                int x = 0;
                for (LinkedHashMap<String, String> vals : community_visits) {
                    View view = inflater.inflate(R.layout.medical_history_visit, null);
                    TextView tvTitle = view.findViewById(R.id.title);
                    LinearLayout visitDetailsLayout = view.findViewById(R.id.visit_details_layout);

                    tvTitle.setText(visits.get(x).getVisitType() + " " + visits.get(x).getDate());


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
                            if (entry.getKey().equalsIgnoreCase("cervix_observation")) {
                                int resource = context.getResources().getIdentifier("ld_" + entry.getKey(), "string", context.getPackageName());
                                evaluateView(context, vals, visitDetailTv, entry.getKey(), resource, "ld_cervix_observation_");
                            } else if (entry.getKey().equalsIgnoreCase("vaginal_bleeding_observation")) {
                                int resource = context.getResources().getIdentifier("ld_" + entry.getKey(), "string", context.getPackageName());
                                evaluateView(context, vals, visitDetailTv, entry.getKey(), resource, "ld_vaginal_bleeding_observation_");
                            } else {
                                int resource = context.getResources().getIdentifier("ld_" + entry.getKey(), "string", context.getPackageName());
                                evaluateView(context, vals, visitDetailTv, entry.getKey(), resource, "");
                            }
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
                if (resourceName.contains("_")) {
                    resourceName = resourceName.replace("_", " ");
                    resourceName = WordUtils.capitalize(resourceName);
                }
                return resourceName;
            }
        }
    }
}
