package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.referral.activity.ReferralDetailsViewActivity;
import org.smartregister.chw.referral.domain.MemberObject;
import org.smartregister.chw.referral.util.Constants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

public class IssuedReferralsDetailsViewActivity extends ReferralDetailsViewActivity {
    private static CommonPersonObjectClient client;
    private CustomFontTextView clientName;
    private CustomFontTextView careGiverName;
    private CustomFontTextView careGiverPhone;
    private CustomFontTextView clientReferralProblem;
    private CustomFontTextView referralDate;
    private CustomFontTextView referralFacility;

    private CustomFontTextView referralType;
    private ViewGroup problemLayout;
    private ViewGroup preManagementServicesServices;

    public static void startIssuedReferralsDetailsViewActivity(Activity activity, MemberObject memberObject, CommonPersonObjectClient client) {
        Intent intent = new Intent(activity, IssuedReferralsDetailsViewActivity.class);
        intent.putExtra(Constants.ReferralMemberObject.MEMBER_OBJECT, memberObject);
        IssuedReferralsDetailsViewActivity.client = client;
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
        problemLayout = findViewById(R.id.client_referral_problem_layout);
        preManagementServicesServices = findViewById(R.id.client_pre_referral_management_layout);

        preManagementServicesServices.setVisibility(View.GONE);
        problemLayout.setVisibility(View.VISIBLE);

        CustomFontTextView problemLabel = findViewById(R.id.client_referral_problem_label);
        problemLabel.setText(R.string.referral_clinic);

        CustomFontTextView referralVillageLabel = findViewById(R.id.referral_facility_label);
        referralVillageLabel.setText(R.string.referral_village);

        obtainReferralDetails();
    }

    private void obtainReferralDetails() {
        //it is memberObject
        MemberObject memberObject = new MemberObject(client);

        clientReferralProblem.setText(memberObject.getProblem());

        int clientAge = new Period(new DateTime(memberObject.getAge()), new DateTime()).getYears();
        clientName.setText(String.format(Locale.getDefault(), "%s %s %s, %d", memberObject.getFirstName(), memberObject.getMiddleName(), memberObject.getLastName(), clientAge));

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        Calendar referralDateCalendar = Calendar.getInstance();
        //referralDateCalendar.setTimeInMillis(new BigDecimal(memberObject.getChwReferralDate()).longValue());

        referralDate.setText(dateFormatter.format(referralDateCalendar.getTime()));
        referralFacility.setText(memberObject.getChwReferralHf());
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
