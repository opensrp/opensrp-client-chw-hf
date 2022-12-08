package org.smartregister.chw.hf.activity;

import static com.vijay.jsonwizard.constants.JsonFormConstants.COUNT;
import static org.smartregister.AllConstants.DEFAULT_LOCALITY_NAME;
import static org.smartregister.opd.utils.OpdConstants.JSON_FORM_KEY.VISIT_ID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkemon.XMLtoPDF.PdfGenerator;
import com.gkemon.XMLtoPDF.PdfGeneratorListener;
import com.gkemon.XMLtoPDF.model.FailureResponse;
import com.gkemon.XMLtoPDF.model.SuccessResponse;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.presenter.BaseAncMedicalHistoryPresenter;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.activity.CoreAncMedicalHistoryActivity;
import org.smartregister.chw.core.activity.DefaultAncMedicalHistoryActivityFlv;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.CoreReferralUtils;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.AncRegistrationDetailsInteractor;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.PmtctVisitUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.repository.AllSharedPreferences;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class AncRegistrationDetailsActivity extends CoreAncMedicalHistoryActivity {
    private static MemberObject ancMemberObject;
    private Flavor flavor = new AncRegistrationDetailsActivityFlv();
    private ProgressBar progressBar;
    private RelativeLayout headerLayout;
    private final StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);

    public static void startMe(Activity activity, MemberObject memberObject) {
        Intent intent = new Intent(activity, AncRegistrationDetailsActivity.class);
        ancMemberObject = memberObject;
        activity.startActivity(intent);
    }

    @Override
    public void initializePresenter() {
        presenter = new BaseAncMedicalHistoryPresenter(new AncRegistrationDetailsInteractor(), this, ancMemberObject.getBaseEntityId());
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
        tvTitle.setText(getString(org.smartregister.chw.opensrp_chw_anc.R.string.back_to, ancMemberObject.getFullName()));
        tvTitle.setTextColor(getResources().getColor(org.smartregister.ld.R.color.white));

        TextView medicalHistory = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.medical_history);
        medicalHistory.setVisibility(View.GONE);

        TextView facilityName = findViewById(org.smartregister.ld.R.id.facility_name);
        TextView clientName = findViewById(org.smartregister.ld.R.id.client_name);

        String facilityNameString = AncLibrary.getInstance().context().allSharedPreferences().getPreference(DEFAULT_LOCALITY_NAME);

        if (StringUtils.isNotBlank(facilityNameString)) {
            facilityName.setText(facilityNameString);
        } else {
            facilityName.setVisibility(View.GONE);
        }

        clientName.setText(MessageFormat.format(getString(org.smartregister.ld.R.string.partograph_client_name), ancMemberObject.getFirstName(), ancMemberObject.getMiddleName(), ancMemberObject.getLastName()));
    }

    @Override
    public View renderView(List<Visit> visits) {
        super.renderView(visits);
        View view = flavor.bindViews(AncRegistrationDetailsActivity.this);
        displayLoadingState(true);
        flavor.processViewData(visits, AncRegistrationDetailsActivity.this);
        displayLoadingState(false);
        TextView title = view.findViewById(org.smartregister.chw.core.R.id.customFontTextViewHealthFacilityVisitTitle);
        TextView edit = view.findViewById(org.smartregister.chw.core.R.id.textview_edit);
        title.setText(getString(R.string.ld_registration_details_title));

        edit.setVisibility(View.GONE);
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
            downloadHistory();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void downloadHistory() {
        headerLayout = findViewById(R.id.header_layout);
        headerLayout.setVisibility(View.VISIBLE);


        int age = 0;
        try {
            age = ancMemberObject.getAge();
        } catch (Exception e) {
            Timber.e(e);
        }
        View mView = findViewById(R.id.main_layout);
        PdfGenerator.getBuilder()
                .setContext(AncRegistrationDetailsActivity.this)
                .fromViewSource()
                .fromView(mView)
                .setFileName(String.format(Locale.getDefault(), "%s %s %s, %d",
                        ancMemberObject.getFirstName(),
                        ancMemberObject.getMiddleName(),
                        ancMemberObject.getLastName(),
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

    public void startFormForEdit(Integer title_resource, String formName, MemberObject memberObject, String deletedVisitId) {
        try {
            JSONObject form = CoreJsonFormUtils.getAncPncForm(title_resource, formName, memberObject, AncRegistrationDetailsActivity.this);
            form.put(VISIT_ID, deletedVisitId);
            startActivityForResult(getStartEditFormIntent(form, getString(title_resource)), JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public Intent getStartEditFormIntent(JSONObject jsonForm, String title) {
        Intent intent = FormUtils.getStartFormActivity(jsonForm, null, this);
        intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        Form form = new Form();
        form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
        form.setName(title);
        form.setNavigationBackground(org.smartregister.chw.core.R.color.family_navigation);

        try {
            form.setWizard(jsonForm.getInt(COUNT) > 1);
        } catch (JSONException e) {
            Timber.e(e);
            form.setWizard(false);
        }
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            AllSharedPreferences allSharedPreferences = org.smartregister.util.Utils.getAllSharedPreferences();
            try {
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);
                String encounterType = form.getString(JsonFormUtils.ENCOUNTER_TYPE);
                if (encounterType.equals(CoreConstants.EventType.ANC_PREGNANCY_CONFIRMATION) || encounterType.equals(CoreConstants.EventType.ANC_FOLLOWUP_CLIENT_REGISTRATION)) {
                    if (form.has(VISIT_ID)) {
                        String deletedVisitId = form.getString(VISIT_ID);
                        form.remove(VISIT_ID);
                        PmtctVisitUtils.deleteProcessedVisit(deletedVisitId, ancMemberObject.getBaseEntityId());
                    }

                    Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, CoreReferralUtils.setEntityId(jsonString, ancMemberObject.getBaseEntityId()), CoreConstants.TABLE_NAME.ANC_MEMBER);
                    org.smartregister.chw.anc.util.JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
                    NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseEvent)));
                    finish();
                }
            } catch (Exception e) {
                Timber.e(e, "AncRegistrationDetailsActivity -- > onActivityResult");
            }
        }
    }

    class AncRegistrationDetailsActivityFlv extends DefaultAncMedicalHistoryActivityFlv {

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

                    // the first object in this list is the days difference


                    String[] hf_params = {
                            "danger_signs",
                            "type_of_pregnancy_test",
                            "fundal_palpation",
                            "upt",
                            "uss",
                            "known_last_menstrual_period",
                            "last_menstrual_period_unknown",
                            "fundal_height",
                            "estimated_last_menstrual_period",
                            "gest_age_note",
                            "is_transfer_in",
                            "medical_surgical_history",
                            "other_medical_surgical_history",
                            "ctc_number",
                            "gravida",
                            "parity",
                            "no_surv_children",
                            "height",
                            "has_the_client_received_ipt_doses_from_previous_facility",
                            "malaria_preventive_therapy",
                            "has_the_client_received_deworming_from_previous_facility",
                            "deworming",
                            "hiv_status",
                            "is_test_at_32",
                            "ctc_number",
                            "abdominal_scars",
                            "abdominal_movement_with_respiration",
                            "abnormal_vaginal_discharge",
                            "vaginal_sores",
                            "vaginal_swelling"
                    };

                    extractHFVisit(visits, hf_params, hf_visits, x, context);

                    x++;
                }

//                processLastVisit();
                processFacilityVisit(hf_visits, visits, context);
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

        protected void processFacilityVisit(List<LinkedHashMap<String, String>> hfVisitsDetails, List<Visit> visits, Context context) {
            if (hfVisitsDetails != null && hfVisitsDetails.size() > 0) {
                linearLayoutHealthFacilityVisit.setVisibility(View.VISIBLE);

                int x = 0;
                for (Map<String, String> vals : hfVisitsDetails) {
                    View view = inflater.inflate(R.layout.medical_history_anc_visit, null);

                    TextView tvTitle = view.findViewById(R.id.title);
                    TextView tvEdit = view.findViewById(R.id.textview_edit);
                    tvEdit.setTag(visits.get(x).getVisitId());
                    LinearLayout visitDetailsLayout = view.findViewById(R.id.visit_details_layout);


                    // Updating visibility of EDIT button if the visit is the last visit
                    if (x == visits.size() - 1)
                        tvEdit.setVisibility(View.VISIBLE);
                    else
                        tvEdit.setVisibility(View.GONE);

                    String titleString = "";
                    if (visits.get(x).getVisitType().equalsIgnoreCase(CoreConstants.EventType.ANC_PREGNANCY_CONFIRMATION))
                        titleString = context.getString(R.string.anc_pregnancy_confirmation_date);
                    else
                        titleString = context.getString(R.string.anc_registration_date);

                    tvTitle.setText(MessageFormat.format(titleString, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(visits.get(x).getDate())));

                    final int position = x;
                    tvEdit.setOnClickListener(view1 -> {
                        Visit visit = visits.get(position);
                        if (visit != null && visit.getVisitType().equalsIgnoreCase(CoreConstants.EventType.ANC_PREGNANCY_CONFIRMATION) && visit.getBaseEntityId() != null)
                            startFormForEdit(R.string.edit_pregnancy_confirmation, CoreConstants.JSON_FORM.ANC_PREGNANCY_CONFIRMATION, ancMemberObject, view1.getTag().toString());
                        else if (visit != null && visit.getVisitType().equalsIgnoreCase(CoreConstants.EventType.ANC_FOLLOWUP_CLIENT_REGISTRATION) && visit.getBaseEntityId() != null)
                            startFormForEdit(R.string.edit_anc_registration, Constants.JSON_FORM.ANC_TRANSFER_IN_REGISTRATION, ancMemberObject, view1.getTag().toString());
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
}
