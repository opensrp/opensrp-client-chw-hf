package org.smartregister.chw.hf.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.chw.core.custom_views.CorePmtctFloatingMenu;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.custom_view.PmtctFloatingMenu;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hf.interactor.HeiProfileInteractor;
import org.smartregister.chw.hf.presenter.HeiProfilePresenter;
import org.smartregister.chw.pmtct.activity.BasePmtctProfileActivity;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.chw.pmtct.util.PmtctUtil;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.AlertStatus;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;

import java.util.Date;

import androidx.annotation.NonNull;
import timber.log.Timber;

import static org.smartregister.chw.core.utils.Utils.getCommonPersonObjectClient;
import static org.smartregister.chw.core.utils.Utils.getDuration;
import static org.smartregister.chw.core.utils.Utils.updateToolbarTitle;

public class HeiProfileActivity extends BasePmtctProfileActivity {

    private static String baseEntityId;

    public static void startProfile(Activity activity, String baseEntityId) {
        HeiProfileActivity.baseEntityId = baseEntityId;
        Intent intent = new Intent(activity, HeiProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        activity.startActivity(intent);
    }

    protected static CommonPersonObjectClient getClientDetailsByBaseEntityID(@NonNull String baseEntityId) {
        return getCommonPersonObjectClient(baseEntityId);
    }

    @SuppressLint("LogNotTimber")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateToolbarTitle(this, org.smartregister.chw.core.R.id.toolbar_title, memberObject.getFamilyName());
    }

    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        String baseEntityId = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
        memberObject = HeiDao.getMember(baseEntityId);
        profilePresenter = new HeiProfilePresenter(this, new HeiProfileInteractor(), memberObject);
        fetchProfileData();
        profilePresenter.refreshProfileBottom();
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        int defaultImage = org.smartregister.chw.core.R.drawable.rowavatar_child;
        ImageView imageViewProfile = findViewById(org.smartregister.chw.core.R.id.imageview_profile);
        imageViewProfile.setImageDrawable(getResources().getDrawable(defaultImage));
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.hei_toolbar_title);

        textViewRecordPmtct.setText(R.string.record_followup);
        textViewRecordPmtct.setOnClickListener(this);

        view_hvl_results_row.setVisibility(View.VISIBLE);
        rlHvlResults.setVisibility(View.VISIBLE);

        TextView tvHeiResultsTitle = findViewById(R.id.textview_hvl_results);
        TextView tvHeiResultsSubTitle = findViewById(R.id.tv_view_hvl_results);

        tvHeiResultsTitle.setText("HIV Test Results");
        tvHeiResultsSubTitle.setText("View Child's HIV Test Results");

        rlHvlResults.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if(id == R.id.textview_record_pmtct){
            HeiFollowupVisitActivity.startHeiFollowUpActivity(this, baseEntityId,false);
        }
        if(id == R.id.rlHvlResults){
            Intent intent = new Intent(this, HeiHivResultsViewActivity.class);
            startActivity(intent);
        }
    }



    @SuppressLint("DefaultLocale")
    @Override
    public void setProfileViewWithData() {
        CommonPersonObjectClient client = getCommonPersonObjectClient(baseEntityId);
        String age = Utils.getTranslatedDate(getDuration(Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false)), this);
        textViewName.setText(String.format("%s %s %s, %s", memberObject.getFirstName(),
                memberObject.getMiddleName(), memberObject.getLastName(), age));
        textViewGender.setText(PmtctUtil.getGenderTranslated(this, memberObject.getGender()));
        textViewLocation.setText(memberObject.getAddress());
        textViewUniqueID.setText(memberObject.getUniqueId());

        if (StringUtils.isNotBlank(memberObject.getFamilyHead()) && memberObject.getFamilyHead().equals(memberObject.getBaseEntityId())) {
            findViewById(org.smartregister.pmtct.R.id.family_malaria_head).setVisibility(View.VISIBLE);
        }
        if (StringUtils.isNotBlank(memberObject.getPrimaryCareGiver()) && memberObject.getPrimaryCareGiver().equals(memberObject.getBaseEntityId())) {
            findViewById(org.smartregister.pmtct.R.id.primary_malaria_caregiver).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void initializeFloatingMenu() {
        basePmtctFloatingMenu = new PmtctFloatingMenu(this, memberObject);
        checkPhoneNumberProvided(StringUtils.isNotBlank(memberObject.getPhoneNumber()));
        OnClickFloatingMenu onClickFloatingMenu = viewId -> {
            switch (viewId) {
                case R.id.pmtct_fab:
                    ((CorePmtctFloatingMenu) basePmtctFloatingMenu).animateFAB();
                    break;
                case R.id.call_layout:
                    ((CorePmtctFloatingMenu) basePmtctFloatingMenu).launchCallWidget();
                    ((CorePmtctFloatingMenu) basePmtctFloatingMenu).animateFAB();
                    break;
                default:
                    Timber.d("Unknown fab action");
                    break;
            }

        };

        ((CorePmtctFloatingMenu) basePmtctFloatingMenu).setFloatMenuClickListener(onClickFloatingMenu);
        basePmtctFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.END);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        addContentView(basePmtctFloatingMenu, linearLayoutParams);
    }

    private void checkPhoneNumberProvided(boolean hasPhoneNumber) {
        ((CorePmtctFloatingMenu) basePmtctFloatingMenu).redraw(hasPhoneNumber);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == org.smartregister.chw.core.R.id.action_registration) {
            startFormForEdit(org.smartregister.chw.core.R.string.registration_info,
                    CoreConstants.JSON_FORM.getChildRegister());
            return true;
        } else if (itemId == org.smartregister.chw.core.R.id.action_remove_member) {
            removeMember();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(org.smartregister.chw.core.R.menu.hei_profile_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void removeMember() {
        IndividualProfileRemoveActivity.startIndividualProfileActivity(this,
                getClientDetailsByBaseEntityID(memberObject.getBaseEntityId()),
                memberObject.getFamilyBaseEntityId(), memberObject.getFamilyHead(),
                memberObject.getPrimaryCareGiver(), FpRegisterActivity.class.getCanonicalName());
    }

    public void startFormForEdit(Integer title_resource, String formName) {

        JSONObject form = null;
        CommonPersonObjectClient client = org.smartregister.chw.core.utils.Utils.clientForEdit(memberObject.getBaseEntityId());

        if (formName.equals(CoreConstants.JSON_FORM.getChildRegister())) {
            form = CoreJsonFormUtils.getAutoPopulatedJsonEditMemberFormString(
                    (title_resource != null) ? getResources().getString(title_resource) : null,
                    CoreConstants.JSON_FORM.getChildRegister(),
                    this, client,
                    CoreConstants.EventType.UPDATE_CHILD_REGISTRATION, memberObject.getLastName(), false);
        }
        try {
            assert form != null;
            startFormActivity(form);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = org.smartregister.chw.core.utils.Utils.formActivityIntent(this, jsonForm.toString());
        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }


    @Override
    public void refreshFamilyStatus(AlertStatus status) {
        super.refreshFamilyStatus(status);
        rlFamilyServicesDue.setVisibility(View.GONE);
    }

    @Override
    public void refreshMedicalHistory(boolean hasHistory) {
        super.refreshMedicalHistory(hasHistory);
        rlLastVisit.setVisibility(View.GONE);
    }

    @Override
    public void refreshUpComingServicesStatus(String service, AlertStatus status, Date date) {
        super.refreshUpComingServicesStatus(service, status, date);
        rlUpcomingServices.setVisibility(View.GONE);
    }
}
