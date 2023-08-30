package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.LTFUFeedbackDao;
import org.smartregister.chw.referral.activity.ReferralDetailsViewActivity;
import org.smartregister.chw.referral.domain.MemberObject;
import org.smartregister.chw.referral.util.Constants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Location;
import org.smartregister.domain.Task;
import org.smartregister.repository.LocationRepository;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

public class ReferralsDetailsViewActivity extends ReferralDetailsViewActivity {
    private static CommonPersonObjectClient client;
    private static Task task;
    private static boolean isSuccessfulReferral = false;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private CustomFontTextView clientName;
    private CustomFontTextView careGiverName;
    private CustomFontTextView careGiverPhone;
    private CustomFontTextView clientReferralProblem;
    private CustomFontTextView referralDate;
    private CustomFontTextView referralFacility;
    private LinearLayout feedBackReasonsLayout;
    private CustomFontTextView feedBackReasons;
    private LinearLayout dateOfDeathLayout;
    private CustomFontTextView dateOfDeath;
    private LinearLayout returnDateLayout;
    private CustomFontTextView returnDate;

    private CustomFontTextView returnDateLabel;

    private CustomFontTextView feedBackDate;
    private CustomFontTextView feedBackFollowupStatus;
    private CustomFontTextView referralType;

    public static void startReferralsDetailsViewActivity(Activity activity, MemberObject memberObject, CommonPersonObjectClient client) {
        Intent intent = new Intent(activity, ReferralsDetailsViewActivity.class);
        intent.putExtra(Constants.ReferralMemberObject.MEMBER_OBJECT, memberObject);
        ReferralsDetailsViewActivity.client = client;
        task = null;
        isSuccessfulReferral = false;
        activity.startActivity(intent);
    }

    public static void startSuccessfulReferralDetailsViewActivity(Activity activity, MemberObject memberObject, CommonPersonObjectClient client, Task passedTask) {
        Intent intent = new Intent(activity, ReferralsDetailsViewActivity.class);
        intent.putExtra(Constants.ReferralMemberObject.MEMBER_OBJECT, memberObject);
        ReferralsDetailsViewActivity.client = client;
        task = passedTask;
        isSuccessfulReferral = true;
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.referral_details_activity);
        inflateToolbar();
        setupViews();
    }

    private void inflateToolbar() {
        Toolbar toolbar = findViewById(R.id.back_referrals_toolbar);
        CustomFontTextView toolBarTextView = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            upArrow.setColorFilter(getResources().getColor(R.color.text_blue), PorterDuff.Mode.SRC_ATOP);
            actionBar.setHomeAsUpIndicator(upArrow);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolBarTextView.setText(R.string.back_to_referrals);
        toolBarTextView.setOnClickListener(v -> finish());
        appBarLayout = findViewById(R.id.app_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            appBarLayout.setOutlineProvider(null);
    }

    private void setupViews() {
        clientName = findViewById(R.id.client_name);
        careGiverName = findViewById(R.id.care_giver_name);
        careGiverPhone = findViewById(R.id.care_giver_phone);
        clientReferralProblem = findViewById(R.id.client_referral_problem);
        referralDate = findViewById(R.id.referral_date);
        referralFacility = findViewById(R.id.referral_facility);
        referralType = findViewById(R.id.referral_type);

        LinearLayout feedBackDetailsLayout = findViewById(R.id.referral_details_feedback);
        feedBackDate = findViewById(R.id.feedback_date);
        feedBackFollowupStatus = findViewById(R.id.feedback_followup_status);
        feedBackReasonsLayout = findViewById(R.id.feedback_reasons_layout);
        feedBackReasons = findViewById(R.id.feedback_reasons);
        dateOfDeathLayout = findViewById(R.id.date_of_death_layout);
        dateOfDeath = findViewById(R.id.date_of_death);
        returnDateLayout = findViewById(R.id.return_date_layout);
        returnDate = findViewById(R.id.return_date);
        returnDateLabel = findViewById(R.id.return_date_label);

        ViewGroup problemLayout = findViewById(R.id.client_referral_problem_layout);
        ViewGroup preManagementServicesServices = findViewById(R.id.client_pre_referral_management_layout);
        preManagementServicesServices.setVisibility(View.GONE);
        problemLayout.setVisibility(View.VISIBLE);
        MemberObject memberObject = new MemberObject(client);
        if (memberObject.getReferralType().equalsIgnoreCase("LTFU")) {
            CustomFontTextView problemLabel = findViewById(R.id.client_referral_problem_label);
            problemLabel.setText(R.string.referral_clinic);

            CustomFontTextView referralVillageLabel = findViewById(R.id.referral_facility_label);
            referralVillageLabel.setText(R.string.referral_village);
        }

        obtainReferralDetails();


        if (isSuccessfulReferral) {
            obtainFeedbackDetails();
            feedBackDetailsLayout.setVisibility(View.VISIBLE);
        } else {
            feedBackDetailsLayout.setVisibility(View.GONE);
        }
    }

    private void obtainFeedbackDetails() {
        //TODO: implement inflating of strings from strings.xml
        Date feedbackDate = LTFUFeedbackDao.getFeedBackDate(task.getIdentifier());
        String followupStatus = LTFUFeedbackDao.getFollowupStatus(task.getIdentifier());

        if (feedbackDate != null) {
            feedBackDate.setText(dateFormatter.format(feedbackDate));
        }

        if (followupStatus != null) {
            feedBackFollowupStatus.setText(getTranslatedFollowupStatus(followupStatus, this));
        }

        if (followupStatus != null && followupStatus.equalsIgnoreCase("client_found_ready_to_return")) {
            feedBackReasonsLayout.setVisibility(View.VISIBLE);
            String reasons = LTFUFeedbackDao.getReasonsForMissedAppointment(task.getIdentifier());
            if (reasons != null) {
                feedBackReasons.setText(getTranslatedReasonsForMissedAppointment(reasons, this));
            }
            Date dateOfReturn = LTFUFeedbackDao.getReferralAppointmentDate(task.getIdentifier());
            if (dateOfReturn != null) {
                returnDateLayout.setVisibility(View.VISIBLE);
                returnDateLabel.setText(R.string.promised_return_date);
                returnDate.setText(dateFormatter.format(dateOfReturn));
            }
        } else if (followupStatus != null && followupStatus.equalsIgnoreCase("deceased")) {
            Date deathDate = LTFUFeedbackDao.getDateOfDeath(task.getIdentifier());
            if (deathDate != null) {
                dateOfDeathLayout.setVisibility(View.VISIBLE);
                dateOfDeath.setText(dateFormatter.format(deathDate));
            }
        } else if (followupStatus != null && followupStatus.equalsIgnoreCase("client_not_found")) {
            feedBackReasonsLayout.setVisibility(View.VISIBLE);
            String reasons = LTFUFeedbackDao.getReasonClientNotFound(task.getIdentifier());
            if (reasons != null) {
                feedBackReasons.setText(getTranslatedReasonClientNotFound(reasons, this));
            }
        } else if (followupStatus != null && followupStatus.equalsIgnoreCase("continuing_with_services")) {
            Date lastVisitDate = LTFUFeedbackDao.getLastAppointmentDate(task.getIdentifier());
            if (lastVisitDate != null) {
                returnDateLayout.setVisibility(View.VISIBLE);
                returnDate.setText(dateFormatter.format(lastVisitDate));
                returnDateLabel.setText(R.string.last_visit_date);
            }
        }


    }

    private void obtainReferralDetails() {

        MemberObject memberObject = new MemberObject(client);

        clientReferralProblem.setText(getReferralClinic(memberObject.getProblem(), this));

        int clientAge = new Period(new DateTime(memberObject.getAge()), new DateTime()).getYears();
        clientName.setText(String.format(Locale.getDefault(), "%s %s %s, %d", memberObject.getFirstName(), memberObject.getMiddleName(), memberObject.getLastName(), clientAge));


        Calendar referralDateCalendar = Calendar.getInstance();
        referralDateCalendar.setTimeInMillis(new BigDecimal(memberObject.getChwReferralDate()).longValue());

        referralDate.setText(dateFormatter.format(referralDateCalendar.getTime()));

        setReferralFacility(memberObject);

        referralType.setText(memberObject.getReferralType());

        if (!StringUtils.isNotBlank(memberObject.getPrimaryCareGiver()) && clientAge < 5) {
            careGiverName.setText(String.format("CG : %s", memberObject.getPrimaryCareGiver()));
        } else {
            careGiverName.setVisibility(View.GONE);
        }

        if (!StringUtils.isNotBlank(getContacts(memberObject))) {
            careGiverPhone.setText(getString(R.string.phone_not_provided));
        } else {
            careGiverPhone.setText(getContacts(memberObject));
        }

    }

    private void setReferralFacility(MemberObject memberObject) {
        String locationId = memberObject.getChwReferralHf();
        LocationRepository locationRepository = new LocationRepository();
        Location location = locationRepository.getLocationById(locationId);
        if (location != null) {
            referralFacility.setText(location.getProperties().getName());
        } else {
            referralFacility.setText(locationId);
        }
    }

    private String getReferralClinic(String key, Context context) {
        try {
            switch (key.toLowerCase()) {
                case "ctc":
                    return context.getString(R.string.ltfu_clinic_ctc);
                case "pwid":
                    return context.getString(R.string.ltfu_clinic_pwid);
                case "prep":
                    return context.getString(R.string.ltfu_clinic_prep);
                case "pmtct":
                    return context.getString(R.string.ltfu_clinic_pmtct);
                case "tb":
                    return context.getString(R.string.ltfu_clinic_tb);
                default:
                    return removeSquareBrackets(key);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return "";
    }

    private String removeSquareBrackets(String text) {
        if (text.startsWith("[") && text.endsWith("]")) {
            return text.substring(1, text.length() - 1).toUpperCase();
        }
        return text.toUpperCase();
    }

    private String getTranslatedFollowupStatus(String key, Context context) {
        switch (key.toLowerCase()) {
            case "continuing_with_services":
                return context.getString(R.string.ltfu_followup_status_continuing_with_services);
            case "client_found_ready_to_return":
                return context.getString(R.string.ltfu_followup_status_client_found_ready_to_return);
            case "client_found_and_has_returned_to_clinic":
                return context.getString(R.string.ltfu_followup_status_client_found_and_has_returned_to_clinic);
            case "client_has_moved_to_another_facility":
                return context.getString(R.string.ltfu_followup_status_client_has_moved_to_another_facility);
            case "client_has_relocated":
                return context.getString(R.string.ltfu_followup_status_client_has_relocated);
            case "client_does_not_want_to_return":
                return context.getString(R.string.ltfu_followup_status_client_does_not_want_to_return);
            case "deceased":
                return context.getString(R.string.ltfu_followup_status_deceased);
            case "client_not_found":
                return context.getString(R.string.ltfu_followup_status_client_not_found);
            default:
                return key.toUpperCase();
        }
    }

    private String getTranslatedReasonsForMissedAppointment(String key, Context context) {
        switch (key.toLowerCase()) {
            case "client_has_forgotten":
                return context.getString(R.string.ltfu_reasons_for_missed_appointment_client_has_forgotten);
            case "client_was_ill":
                return context.getString(R.string.ltfu_reasons_for_missed_appointment_client_was_ill);
            case "client_failed_to_disclose_his_status":
                return context.getString(R.string.ltfu_reasons_for_missed_appointment_client_failed_to_disclose_his_status);
            case "client_did_not_have_fare":
                return context.getString(R.string.ltfu_reasons_for_missed_appointment_client_did_not_have_fare);
            case "client_lives_far_away_from_the_health_facility":
                return context.getString(R.string.ltfu_reasons_for_missed_appointment_client_lives_far_away_from_the_health_facility);
            case "client_feels_well":
                return context.getString(R.string.ltfu_reasons_for_missed_appointment_client_feels_well);
            case "client_was_busy_at_work":
                return context.getString(R.string.ltfu_reasons_for_missed_appointment_client_was_busy_at_work);
            case "client_traveled":
                return context.getString(R.string.ltfu_reasons_for_missed_appointment_client_traveled);
            case "client_uses_alternative_medicine":
                return context.getString(R.string.ltfu_reasons_for_missed_appointment_client_uses_alternative_medicine);
            case "poor_services_at_health_facility":
                return context.getString(R.string.ltfu_reasons_for_missed_appointment_poor_services_at_health_facility);
            default:
                return key.toUpperCase();
        }
    }

    private String getTranslatedReasonClientNotFound(String key, Context context) {
        switch (key.toLowerCase()) {
            case "address_incorrect":
                return context.getString(R.string.ltfu_reason_client_not_found_address_incorrect);
            case "client_relocated":
                return context.getString(R.string.ltfu_reason_client_not_found_client_relocated);
            case "seasonal_work":
                return context.getString(R.string.ltfu_reason_client_not_found_seasonal_work);
            default:
                return key.toUpperCase();
        }
    }

    private String getContacts(MemberObject memberObject) {
        String phoneNumber = "";
        String familyPhoneNumber = memberObject.getPhoneNumber();
        String familyOtherPhoneNumber = memberObject.getOtherPhoneNumber();

        if (StringUtils.isNotBlank(familyPhoneNumber)) {
            phoneNumber = familyPhoneNumber;
        }
        if (StringUtils.isNotBlank(familyOtherPhoneNumber)) {
            phoneNumber = familyOtherPhoneNumber;
        }
        if (StringUtils.isNotBlank(familyPhoneNumber) && StringUtils.isNotBlank(familyOtherPhoneNumber)) {
            phoneNumber = familyPhoneNumber + ", " + familyOtherPhoneNumber;
        }

        return phoneNumber;
    }
}
