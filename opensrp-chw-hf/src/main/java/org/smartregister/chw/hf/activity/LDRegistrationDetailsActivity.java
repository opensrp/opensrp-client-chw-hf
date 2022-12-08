package org.smartregister.chw.hf.activity;

import static org.smartregister.AllConstants.DEFAULT_LOCALITY_NAME;
import static org.smartregister.util.Utils.getName;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkemon.XMLtoPDF.PdfGenerator;
import com.gkemon.XMLtoPDF.PdfGeneratorListener;
import com.gkemon.XMLtoPDF.model.FailureResponse;
import com.gkemon.XMLtoPDF.model.SuccessResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.presenter.BaseAncMedicalHistoryPresenter;
import org.smartregister.chw.core.activity.CoreAncMedicalHistoryActivity;
import org.smartregister.chw.core.activity.DefaultAncMedicalHistoryActivityFlv;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.LDRegistrationDetailsInteractor;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.dao.LDDao;
import org.smartregister.chw.ld.domain.MemberObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class LDRegistrationDetailsActivity extends CoreAncMedicalHistoryActivity {
    private static MemberObject ldMemberObject;
    private Flavor flavor = new LDRegistrationDetailsActivityFlv();
    private ProgressBar progressBar;
    private RelativeLayout headerLayout;

    public static void startMe(Activity activity, MemberObject memberObject) {
        Intent intent = new Intent(activity, LDRegistrationDetailsActivity.class);
        ldMemberObject = memberObject;
        activity.startActivity(intent);
    }

    @Override
    public void initializePresenter() {
        presenter = new BaseAncMedicalHistoryPresenter(new LDRegistrationDetailsInteractor(), this, ldMemberObject.getBaseEntityId());
    }

    @Override
    public void setUpView() {
        findViewById(R.id.collapsing_toolbar).setBackgroundColor(getResources().getColor(R.color.primary));

        Drawable upArrow = this.getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
        upArrow.setColorFilter(this.getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        linearLayout = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.linearLayoutMedicalHistory);
        progressBar = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.progressBarMedicalHistory);


        TextView tvTitle = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.tvTitle);
        tvTitle.setText(getString(org.smartregister.chw.opensrp_chw_anc.R.string.back_to, ldMemberObject.getFullName()));
        tvTitle.setTextColor(getResources().getColor(org.smartregister.ld.R.color.white));

        TextView medicalHistory = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.medical_history);
        medicalHistory.setVisibility(View.GONE);

        TextView facilityName = findViewById(org.smartregister.ld.R.id.facility_name);
        TextView clientName = findViewById(org.smartregister.ld.R.id.client_name);
        TextView gravida = findViewById(org.smartregister.ld.R.id.gravida);
        TextView para = findViewById(org.smartregister.ld.R.id.para);
        TextView admissionDate = findViewById(org.smartregister.ld.R.id.admission_date);
        TextView admissionTime = findViewById(org.smartregister.ld.R.id.admission_time);

        String facilityNameString = LDLibrary.getInstance().context().allSharedPreferences().getPreference(DEFAULT_LOCALITY_NAME);

        if (StringUtils.isNotBlank(facilityNameString)) {
            facilityName.setText(facilityNameString);
        } else {
            facilityName.setVisibility(View.GONE);
        }

        clientName.setText(MessageFormat.format(getString(org.smartregister.ld.R.string.partograph_client_name), ldMemberObject.getFirstName(), ldMemberObject.getMiddleName(), ldMemberObject.getLastName()));
        gravida.setText(MessageFormat.format(getString(org.smartregister.ld.R.string.partograph_gravida), LDDao.getGravida(ldMemberObject.getBaseEntityId())));
        para.setText(MessageFormat.format(getString(org.smartregister.ld.R.string.partograph_para), LDDao.getPara(ldMemberObject.getBaseEntityId())));
        admissionDate.setText(MessageFormat.format(getString(org.smartregister.ld.R.string.partograph_admission_date), LDDao.getAdmissionDate(ldMemberObject.getBaseEntityId())));
        admissionTime.setText(MessageFormat.format(getString(org.smartregister.ld.R.string.partograph_admission_time), LDDao.getAdmissionTime(ldMemberObject.getBaseEntityId())));
    }

    @Override
    public View renderView(List<Visit> visits) {
        super.renderView(visits);
        View view = flavor.bindViews(this);
        displayLoadingState(true);
        flavor.processViewData(visits, this);
        displayLoadingState(false);
        TextView title = view.findViewById(org.smartregister.chw.core.R.id.customFontTextViewHealthFacilityVisitTitle);
        TextView edit = view.findViewById(org.smartregister.chw.core.R.id.textview_edit);
        title.setText(getString(R.string.ld_registration_details_title));

        edit.setVisibility(View.VISIBLE);
        edit.setOnClickListener(view1 -> LDRegistrationFormActivity.startMe(LDRegistrationDetailsActivity.this, ldMemberObject.getBaseEntityId(), true, getName(getName(ldMemberObject.getFirstName(), ldMemberObject.getMiddleName()), ldMemberObject.getLastName()), String.valueOf(ldMemberObject.getAge())));

        return view;
    }

    @Override
    public void displayLoadingState(boolean state) {
        progressBar.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(org.smartregister.ld.R.menu.partograph_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == org.smartregister.ld.R.id.action_download_partograph) {
            downloadPartograph();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void downloadPartograph() {
        headerLayout = findViewById(R.id.header_layout);
        headerLayout.setVisibility(View.VISIBLE);


        int age = 0;
        try {
            age = Integer.parseInt(ldMemberObject.getAge());
        } catch (Exception e) {
            Timber.e(e);
        }
        View mView = findViewById(R.id.main_layout);
        PdfGenerator.getBuilder()
                .setContext(LDRegistrationDetailsActivity.this)
                .fromViewSource()
                .fromView(mView)
                .setFileName(String.format(Locale.getDefault(), "%s %s %s, %d",
                        ldMemberObject.getFirstName(),
                        ldMemberObject.getMiddleName(),
                        ldMemberObject.getLastName(),
                        age))
                .setFolderNameOrPath("MyFolder/MyDemoHorizontalText/")
                .actionAfterPDFGeneration(PdfGenerator.ActionAfterPDFGeneration.OPEN)
                .build(new PdfGeneratorListener() {
                    @Override
                    public void onFailure(FailureResponse failureResponse) {
                        super.onFailure(failureResponse);
                    }

                    @Override
                    public void showLog(String log) {
                        super.showLog(log);
                    }

                    @Override
                    public void onStartPDFGeneration() {
                        /*When PDF generation begins to start*/
                    }

                    @Override
                    public void onFinishPDFGeneration() {
                        /*When PDF generation is finished*/
                        headerLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onSuccess(SuccessResponse response) {
                        super.onSuccess(response);
                    }
                });
    }

    class LDRegistrationDetailsActivityFlv extends DefaultAncMedicalHistoryActivityFlv {

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
                            "systolic",
                            "diastolic",
                            "pulse_rate",
                            "respiratory_rate",
                            "oxygen_saturation",
                            "fetal_heart_rate",
                            "temperature",
                            "weight",
                            "height",
                            "gravida",
                            "para",
                            "number_of_abortion",
                            "children_alive",
                            "gest_age",
                            "past_medical_surgical_history",
                            "admission_date",
                            "admission_time",
                            "admitting_person_name",
                            "admission_from",
                            "name_of_hf",
                            "reason_for_referral",
                            "reasons_for_admission",
                            "other_reason_for_admission",
                            "danger_signs",
                            "other_danger_signs",
                            "number_of_visits",
                            "ipt_doses",
                            "tt_doses",
                            "llin_used",
                            "hb_test",
                            "hb_level",
                            "management_provided_for_severe_anaemia",
                            "management_provided_for_mild_anaemia",
                            "hb_test_date",
                            "anc_hiv_status",
                            "hiv",
                            "pmtct_test_date",
                            "management_provided_for_pmtct",
                            "art_prescription",
                            "prompt_for_art_management",
                            "syphilis",
                            "management_provided_for_syphilis",
                            "malaria",
                            "management_provided_for_malaria",
                            "blood_group",
                            "rh_factor",
                            "true_labour",
                            "management_provided_for_rh",
                            "labour_onset_date",
                            "labour_onset_time",
                            "membrane",
                            "membrane_ruptured_date",
                            "membrane_ruptured_time",
                            "fetal_movement",
                            "movement_status"
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
                    View rootView = inflater.inflate(R.layout.fragment_labour_and_delivery_registration_summary, null);

                    TextView para = rootView.findViewById(R.id.para);
                    TextView pmtct = rootView.findViewById(R.id.pmtct);
                    TextView weight = rootView.findViewById(R.id.weight);
                    TextView height = rootView.findViewById(R.id.height);
                    TextView gravida = rootView.findViewById(R.id.gravida);
                    TextView systolic = rootView.findViewById(R.id.systolic);
                    TextView gest_age = rootView.findViewById(R.id.gest_age);
                    TextView tt_doses = rootView.findViewById(R.id.tt_doses);
                    TextView hb_level = rootView.findViewById(R.id.hb_level);
                    TextView syphilis = rootView.findViewById(R.id.syphilis);
                    TextView diastolic = rootView.findViewById(R.id.diastolic);
                    TextView ipt_doses = rootView.findViewById(R.id.ipt_doses);
                    TextView rh_factor = rootView.findViewById(R.id.rh_factor);
                    TextView pulse_rate = rootView.findViewById(R.id.pulse_rate);
                    TextView temperature = rootView.findViewById(R.id.temperature);
                    TextView true_labour = rootView.findViewById(R.id.true_labour);
                    TextView blood_group = rootView.findViewById(R.id.blood_group);
                    TextView danger_signs = rootView.findViewById(R.id.danger_signs);
                    TextView visit_number = rootView.findViewById(R.id.visit_number);
                    TextView hb_test_date = rootView.findViewById(R.id.hb_test_date);
                    TextView itn_llin_used = rootView.findViewById(R.id.itn_llin_used);
                    TextView malaria = rootView.findViewById(R.id.malaria);
                    TextView admission_date = rootView.findViewById(R.id.admission_date);
                    TextView admission_time = rootView.findViewById(R.id.admission_time);
                    TextView children_alive = rootView.findViewById(R.id.children_alive);
                    TextView fetal_movement = rootView.findViewById(R.id.fetal_movement);
                    TextView admission_place = rootView.findViewById(R.id.admission_place);
                    TextView pmtct_test_date = rootView.findViewById(R.id.pmtct_test_date);
                    TextView respiratory_rate = rootView.findViewById(R.id.respiratory_rate);
                    TextView fetal_heart_rate = rootView.findViewById(R.id.fetal_heart_rate);
                    TextView admission_reason = rootView.findViewById(R.id.admission_reason);
                    TextView art_prescription = rootView.findViewById(R.id.art_prescription);
                    TextView labour_onset_date = rootView.findViewById(R.id.labour_onset_date);
                    TextView labour_onset_time = rootView.findViewById(R.id.labour_onset_time);
                    TextView ruptured_membrane = rootView.findViewById(R.id.ruptured_membrane);
                    TextView number_of_abortion = rootView.findViewById(R.id.number_of_abortion);
                    TextView admitting_person_name = rootView.findViewById(R.id.admitting_person_name);
                    TextView membrane_ruptured_date = rootView.findViewById(R.id.membrane_ruptured_date);
                    TextView membrane_ruptured_time = rootView.findViewById(R.id.membrane_ruptured_time);
                    TextView past_medical_surgical_history = rootView.findViewById(R.id.past_medical_history);

                    evaluateView(context, vals, true_labour, "true_labour", "ld_");
                    evaluateView(context, vals, systolic, "systolic", "ld_");
                    evaluateView(context, vals, diastolic, "diastolic", "ld_");
                    evaluateView(context, vals, pulse_rate, "pulse_rate", "ld_");
                    evaluateView(context, vals, respiratory_rate, "respiratory_rate", "ld_");
                    evaluateView(context, vals, fetal_heart_rate, "fetal_heart_rate", "ld_");
                    evaluateView(context, vals, temperature, "temperature", "ld_");
                    evaluateView(context, vals, weight, "weight", "ld_");
                    evaluateView(context, vals, height, "height", "ld_");
                    evaluateView(context, vals, gravida, "gravida", "ld_");
                    evaluateView(context, vals, para, "para", "ld_");
                    evaluateView(context, vals, number_of_abortion, "number_of_abortion", "ld_");
                    evaluateView(context, vals, children_alive, "children_alive", "ld_");
                    evaluateView(context, vals, past_medical_surgical_history, "past_medical_surgical_history", "ld_");
                    evaluateView(context, vals, gest_age, "gest_age", "ld_");
                    evaluateView(context, vals, admission_date, "admission_date", "ld_");
                    evaluateView(context, vals, admission_time, "admission_time", "ld_");
                    evaluateView(context, vals, admitting_person_name, "admitting_person_name", "ld_");
                    evaluateView(context, vals, admission_place, "admission_from", "ld_");
                    evaluateView(context, vals, admission_reason, "reasons_for_admission", "ld_");
                    evaluateView(context, vals, danger_signs, "danger_signs", "ld_");
                    evaluateView(context, vals, visit_number, "number_of_visits", "ld_");
                    evaluateView(context, vals, ipt_doses, "ipt_doses", "ld_");
                    evaluateView(context, vals, tt_doses, "tt_doses", "ld_");
                    evaluateView(context, vals, itn_llin_used, "llin_used", "");
                    evaluateView(context, vals, hb_level, "hb_level", "ld_");
                    evaluateView(context, vals, hb_test_date, "hb_test_date", "ld_");
                    evaluateView(context, vals, pmtct, "anc_hiv_status", "ld_");
                    evaluateView(context, vals, pmtct_test_date, "pmtct_test_date", "ld_");
                    evaluateView(context, vals, art_prescription, "art_prescription", "ld_");
                    evaluateView(context, vals, syphilis, "syphilis", "ld_");
                    evaluateView(context, vals, malaria, "malaria", "ld_");
                    evaluateView(context, vals, blood_group, "blood_group", "ld_");
                    evaluateView(context, vals, rh_factor, "rh_factor", "");
                    evaluateView(context, vals, labour_onset_date, "labour_onset_date", "ld_");
                    evaluateView(context, vals, labour_onset_time, "labour_onset_time", "ld_");
                    evaluateView(context, vals, ruptured_membrane, "membrane", "ld_");
                    evaluateView(context, vals, membrane_ruptured_date, "membrane_ruptured_date", "ld_");
                    evaluateView(context, vals, membrane_ruptured_time, "membrane_ruptured_time", "ld_");
                    evaluateView(context, vals, fetal_movement, "fetal_movement", "ld_");


                    linearLayoutHealthFacilityVisitDetails.addView(rootView, 0);

                }
            }
        }


        private void evaluateView(Context context, Map<String, String> vals, TextView tv, String valueKey, String valuePrefixInStringResources) {
            if (StringUtils.isNotBlank(getMapValue(vals, valueKey))) {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

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
                tv.setVisibility(View.VISIBLE);
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
