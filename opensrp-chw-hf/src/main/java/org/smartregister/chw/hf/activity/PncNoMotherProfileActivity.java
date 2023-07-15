package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.core.utils.Utils.getDuration;
import static org.smartregister.chw.core.utils.Utils.passToolbarTitle;
import static org.smartregister.chw.hf.utils.Constants.Events.HEI_REGISTRATION;
import static org.smartregister.util.JsonFormUtils.FIELDS;
import static org.smartregister.util.JsonFormUtils.STEP1;
import static org.smartregister.util.JsonFormUtils.VALUE;
import static org.smartregister.util.Utils.getName;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.domain.Child;
import org.smartregister.chw.core.interactor.CoreChildProfileInteractor;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.custom_view.PncNoMotherFloatingMenu;
import org.smartregister.chw.hf.dao.HfChildDao;
import org.smartregister.chw.hf.dao.HfPncDao;
import org.smartregister.chw.hf.utils.HfChildUtils;
import org.smartregister.chw.hf.utils.PncVisitUtils;
import org.smartregister.chw.pnc.PncLibrary;
import org.smartregister.chw.pnc.util.PncUtil;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.Utils;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import timber.log.Timber;

public class PncNoMotherProfileActivity extends PncMemberProfileActivity {
    private static String caregiverName;
    private static String caregiverPhoneNumber;
    private static String dayPnc;
    private static char gender;
    private static String dob;
    protected TextView childCaregiver;


    public static void startMe(Activity activity, String baseEntityID, MemberObject memberObject, CommonPersonObjectClient pc) {
        Intent intent = new Intent(activity, PncNoMotherProfileActivity.class);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, memberObject);
        passToolbarTitle(activity, intent);
        caregiverName = Utils.getValue(pc.getColumnmaps(), org.smartregister.chw.hf.utils.Constants.DBConstants.CAREGIVER_NAME, false);
        caregiverPhoneNumber = Utils.getValue(pc.getColumnmaps(), org.smartregister.chw.hf.utils.Constants.DBConstants.CAREGIVER_PHONE_NUMBER, false);
        dayPnc = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DELIVERY_DATE, true);
        dob = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, true);
        gender = Utils.getValue(pc.getColumnmaps(), org.smartregister.chw.pnc.util.Constants.KEY.GENDER, false).charAt(0);

        activity.startActivity(intent);
    }

    public static void startChildForm(Activity activity, String childBaseEntityId) {
        JSONObject jsonForm = org.smartregister.chw.core.utils.FormUtils.getFormUtils().getFormJson(org.smartregister.chw.hf.utils.Constants.JsonForm.getPncChildGeneralExamination());
        try {
            Child child = HfChildDao.getNoMotherChild(childBaseEntityId);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.getDefault());
            int ageInDays = PncUtil.getDaysDifference(dateFormat.format(child.getDateOfBirth()));

            child.getDateOfBirth();
            jsonForm.getJSONObject("global").put("baseEntityId", childBaseEntityId);
            jsonForm.getJSONObject("global").put("is_eligible_for_bcg", HfPncDao.isChildEligibleForBcg(childBaseEntityId));
            jsonForm.getJSONObject("global").put("is_eligible_for_opv0", ageInDays <= 14 && HfPncDao.isChildEligibleForOpv0(childBaseEntityId));
            jsonForm.getJSONObject("global").put("anti_body_test_conducted", HfPncDao.hasHivAntibodyTestBeenConducted(childBaseEntityId));
            jsonForm.getJSONObject("global").put("is_a_child_without_mother", HfPncDao.isAChildWithoutMother(childBaseEntityId));
            activity.startActivityForResult(org.smartregister.chw.core.utils.FormUtils.getStartFormActivity(jsonForm, activity.getString(R.string.record_child_followup), activity), JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public void setupViews() {
        super.setupViews();
        try {
            PncVisitUtils.processVisits();
        } catch (Exception e) {
            Timber.e(e);
        }

        textview_record_anc_visit.setVisibility(View.VISIBLE);
        textview_record_anc_visit.setOnClickListener(this);
        textViewAncVisitNot.setText(R.string.complete_pnc_visits);
        textViewAncVisitNot.setOnClickListener(v -> confirmRemovePncMember());
        childCaregiver = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.primary_anc_caregiver);

        findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.family_anc_head).setVisibility(View.GONE);
        childCaregiver.setVisibility(View.VISIBLE);

        childCaregiver.setText(this.getString(R.string.caregiver_name, caregiverName));
    }

    @Override
    public void setProfileImage(String baseEntityId, String entityType) {
        imageRenderHelper.refreshProfileImage(baseEntityId, imageView, org.smartregister.chw.core.R.drawable.rowavatar_child);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_pnc_member_registration) {
            startFormForEdit(org.smartregister.chw.core.R.string.registration_info,
                    CoreConstants.JSON_FORM.getChildRegister());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_location_info).setVisible(false);
        return true;
    }

    @Override
    protected int getPncDay() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
        return Days.daysBetween(new DateTime(formatter.parseDateTime(dayPnc)), new DateTime()).getDays();
    }

    @Override
    public void startFormForEdit(Integer title_resource, String formName) {

        JSONObject childEnrollmentForm = null;
        CommonPersonObjectClient client = HfChildUtils.getChildClientByBaseEntityId(memberObject.getBaseEntityId());

        if (client == null) {
            return;
        }
        if (formName.equals(CoreConstants.JSON_FORM.getChildRegister())) {
            CoreChildProfileInteractor childProfileInteractor = new CoreChildProfileInteractor();
            childEnrollmentForm = childProfileInteractor.getAutoPopulatedJsonEditFormString(CoreConstants.JSON_FORM.getChildRegister(), (title_resource != null) ? getResources().getString(title_resource) : null, this, client);
            try {
                JSONObject stepOne = childEnrollmentForm.getJSONObject(JsonFormUtils.STEP1);
                JSONArray fields = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                JSONObject sameAsFamName = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "same_as_fam_name");
                sameAsFamName.put("type", "hidden");
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        try {
            assert childEnrollmentForm != null;
            startFormActivity(childEnrollmentForm);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void setMemberGA(String memberGA) {
        text_view_ga.setText(getName(getString(org.smartregister.chw.pnc.R.string.pnc_day), String.valueOf(getPncDay())));
    }

    @Override
    public void setMemberName(String memberName) {
        String age = org.smartregister.family.util.Utils.getTranslatedDate(getDuration(dob), this);
        text_view_anc_member_name.setText(String.format("%s %s %s, %s", memberObject.getFirstName(),
                memberObject.getMiddleName(), memberObject.getLastName(), age));
        if (gender == 'M') {
            imageView.setBorderColor(PncLibrary.getInstance().context().getColorResource(org.smartregister.chw.pnc.R.color.light_blue));
        } else {
            imageView.setBorderColor(PncLibrary.getInstance().context().getColorResource(org.smartregister.chw.pnc.R.color.light_pink));
        }
        imageView.setBorderWidth(10);
    }

    @Override
    public void initializeFloatingMenu() {
        baseAncFloatingMenu = new PncNoMotherFloatingMenu(this, caregiverName, caregiverPhoneNumber);
        baseAncFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        addContentView(baseAncFloatingMenu, linearLayoutParams);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.textview_record_visit) {
            startChildForm(this, baseEntityID);
        } else {
            super.onClick(v);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON) {
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
            try {
                JSONObject form = new JSONObject(jsonString);
                String encounterType = form.getString(JsonFormUtils.ENCOUNTER_TYPE);
                if (encounterType.equals(org.smartregister.chw.hf.utils.Constants.Events.PNC_CHILD_FOLLOWUP)) {
                    try {
                        JSONObject global = form.getJSONObject("global");
                        JSONArray fields = form.getJSONObject(STEP1).getJSONArray(FIELDS);
                        JSONObject hivAntibodyTest = JsonFormUtils.getFieldJSONObject(fields, "hiv_antibody_test");
                        if (hivAntibodyTest != null && hivAntibodyTest.has(VALUE) && hivAntibodyTest.getString(VALUE).equalsIgnoreCase("positive")) {
                            createHeiRegistrationEvent(global.getString("baseEntityId"));
                        }
                        Toast.makeText(this, getString(R.string.saved_child_followup), Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        Timber.e(ex);
                    }
                }
            } catch (JSONException jsonException) {
                Timber.e(jsonException);
            }
        }
    }

    private static void createHeiRegistrationEvent(String baseEntityId) throws Exception {
        Event baseEvent = new Event();
        baseEvent.setFormSubmissionId(UUID.randomUUID().toString());
        baseEvent.setEventType(HEI_REGISTRATION);
        baseEvent.addObs(
                (new Obs())
                        .withFormSubmissionField("risk_category")
                        .withValue("high")
                        .withFieldCode("risk_category")
                        .withFieldType("formsubmissionField")
                        .withFieldDataType("text")
                        .withParentCode("")
                        .withHumanReadableValues(new ArrayList<>()));


        baseEvent.setEventDate(new Date());
        baseEvent.setDateCreated(new Date());
        baseEvent.setBaseEntityId(baseEntityId);
        baseEvent.setEntityType(org.smartregister.chw.hf.utils.Constants.TableName.HEI);

        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();

        org.smartregister.chw.anc.util.JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);
        org.smartregister.chw.anc.util.NCUtils.addEvent(allSharedPreferences, baseEvent);
        org.smartregister.chw.anc.util.NCUtils.startClientProcessing();

    }

    @Override
    public void setLastVisit(Date lastVisitDate) {
        Visit lastFollowupVisit = getVisit(org.smartregister.chw.hf.utils.Constants.Events.PNC_CHILD_FOLLOWUP);

        if (lastFollowupVisit != null) {
            rlLastVisit.setVisibility(View.VISIBLE);
            view_last_visit_row.setVisibility(View.VISIBLE);
            String x = lastFollowupVisit.getDate().toString();
            tvLastVisitDate.setText(MessageFormat.format(getString(org.smartregister.chw.pnc.R.string.pnc_last_visit_text), x));
        }
    }

    @Override
    public void openMedicalHistory() {
        PncNoMotherChildMedicalHistoryActivity.startMe(this, memberObject);
    }
}
