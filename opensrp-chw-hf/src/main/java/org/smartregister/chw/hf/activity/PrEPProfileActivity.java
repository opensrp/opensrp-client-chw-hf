package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.chw.core.activity.CoreKvpProfileActivity;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.LFTUFormUtils;
import org.smartregister.chw.hivst.dao.HivstDao;
import org.smartregister.chw.kvp.KvpLibrary;
import org.smartregister.chw.kvp.domain.Visit;
import org.smartregister.chw.kvp.listener.OnClickFloatingMenu;
import org.smartregister.chw.kvp.util.Constants;
import org.smartregister.chw.kvp.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.Utils;

import timber.log.Timber;

public class PrEPProfileActivity extends CoreKvpProfileActivity {

    public static void startProfile(Activity activity, String baseEntityId) {
        Intent intent = new Intent(activity, PrEPProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.PROFILE_TYPE, Constants.PROFILE_TYPES.PrEP_PROFILE);
        activity.startActivity(intent);
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        if (evaluateProvisionOfPrepService()) {
            textViewRecordKvp.setVisibility(View.GONE);
        }
    }

    private boolean evaluateProvisionOfPrepService() {
        Visit lastVisit = getVisit(Constants.EVENT_TYPE.PrEP_FOLLOWUP_VISIT);
        if (lastVisit != null && lastVisit.getProcessed()) {
            DateTime time = new DateTime(lastVisit.getUpdatedAt());
            DateTime now = new DateTime();
            int diffDays = Days.daysBetween(time, now).getDays();
            return diffDays < 1;
        }
        return false;
    }

    @Override
    public void openFollowupVisit() {
        PrEPVisitActivity.startPrEPVisitActivity(this, memberObject.getBaseEntityId(), false);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.textview_continue) {
            PrEPVisitActivity.startPrEPVisitActivity(this, memberObject.getBaseEntityId(), true);
        } else {
            super.onClick(view);
        }
    }

    @Override
    public void refreshMedicalHistory(boolean hasHistory) {
        Visit lastVisit = getVisit(Constants.EVENT_TYPE.PrEP_FOLLOWUP_VISIT);
        if (lastVisit != null) {
            rlLastVisit.setVisibility(View.VISIBLE);
            findViewById(R.id.view_notification_and_referral_row).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.vViewHistory)).setText(R.string.visits_history);
            ((TextView) findViewById(R.id.ivViewHistoryArrow)).setText(getString(R.string.view_visits_history));
        } else {
            rlLastVisit.setVisibility(View.GONE);
        }
    }

    @Override
    public void openMedicalHistory() {
        PrEPMedicalHistoryActivity.startMe(this, memberObject);
    }

    private Visit getVisit(String eventType) {
        return KvpLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), eventType);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (HealthFacilityApplication.getApplicationFlavor().hasHivst()) {
            int age = memberObject.getAge();
            menu.findItem(R.id.action_hivst_registration).setVisible(!HivstDao.isRegisteredForHivst(memberObject.getBaseEntityId()) && age >= 15);
        }

        return true;
    }

    @Override
    public void startHivstRegistration() {
        CommonRepository commonRepository = Utils.context().commonrepository(Utils.metadata().familyMemberRegister.tableName);

        final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(memberObject.getBaseEntityId());
        final CommonPersonObjectClient client = new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
        client.setColumnmaps(commonPersonObject.getColumnmaps());
        String gender = Utils.getValue(commonPersonObject.getColumnmaps(), DBConstants.KEY.GENDER, false);
        HivstRegisterActivity.startHivstRegistrationActivity(this, memberObject.getBaseEntityId(), gender);
    }

    @Override
    public void initializeFloatingMenu() {
        super.initializeFloatingMenu();
        CommonRepository commonRepository = Utils.context().commonrepository(Utils.metadata().familyMemberRegister.tableName);
        final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(memberObject.getBaseEntityId());
        final CommonPersonObjectClient client = new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");

        baseKvpFloatingMenu.findViewById(org.smartregister.kvp.R.id.refer_to_facility_layout).setVisibility(View.VISIBLE);
        ((TextView) baseKvpFloatingMenu.findViewById(org.smartregister.kvp.R.id.refer_to_facility_text)).setText(R.string.lost_to_followup_referral);

        OnClickFloatingMenu onClickFloatingMenu = viewId -> {
            if (viewId == org.smartregister.kvp.R.id.kvp_fab) {
                baseKvpFloatingMenu.animateFAB();
            } else if (viewId == org.smartregister.kvp.R.id.call_layout) {
                baseKvpFloatingMenu.launchCallWidget();
                baseKvpFloatingMenu.animateFAB();
            } else if (viewId == org.smartregister.kvp.R.id.refer_to_facility_layout) {
                String gender = org.smartregister.chw.core.utils.Utils.getValue(commonPersonObject.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.GENDER, false);
                String dob = org.smartregister.chw.core.utils.Utils.getValue(commonPersonObject.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.DOB, false);
                LFTUFormUtils.startLTFUReferral(this, memberObject.getBaseEntityId(), gender, org.smartregister.chw.core.utils.Utils.getAgeFromDate(dob));
            } else {
                Timber.d("Unknown FAB action");
            }
        };

        baseKvpFloatingMenu.setFloatMenuClickListener(onClickFloatingMenu);
    }
}
