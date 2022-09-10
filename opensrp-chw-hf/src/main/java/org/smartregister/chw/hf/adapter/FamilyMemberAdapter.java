package org.smartregister.chw.hf.adapter;


import static org.smartregister.chw.core.utils.CoreReferralUtils.getCommonRepository;
import static org.smartregister.chw.core.utils.Utils.passToolbarTitle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.smartregister.chw.core.dao.AncDao;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.AllClientsMemberProfileActivity;
import org.smartregister.chw.hf.activity.FamilyOtherMemberProfileActivity;
import org.smartregister.chw.hf.dao.HfPncDao;
import org.smartregister.chw.hf.dao.LDDao;
import org.smartregister.chw.hf.domain.Entity;
import org.smartregister.chw.hf.utils.AllClientsUtils;
import org.smartregister.chw.hf.utils.PullEventClientRecordUtil;
import org.smartregister.chw.pmtct.util.PmtctUtil;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.event.Listener;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.helper.ImageRenderHelper;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FamilyMemberAdapter extends ArrayAdapter<Entity> {
    private boolean isLocal = false;

    public FamilyMemberAdapter(Context context, List<Entity> members, boolean isLocal) {
        super(context, 0, members);
        this.isLocal = isLocal;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Entity member = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.family_member_register_list_row, parent, false);
        }

        // Lookup view for data population
        TextView tvName = convertView.findViewById(R.id.patient_name_age);
        TextView tvGender = convertView.findViewById(R.id.gender);
        ImageView profile = convertView.findViewById(org.smartregister.family.R.id.profile);
        // Populate the data into the template view using the data object
        String fullName = String.format(Locale.getDefault(), "%s %s %s, %s", isNull(member.getFirstName()), isNull(member.getMiddleName()), isNull(member.getLastName()), getAge(member.getBirthdate()));
        tvName.setText(fullName);

        tvGender.setText(PmtctUtil.getGenderTranslated(getContext(), member.getGender()));
        new ImageRenderHelper(getContext()).refreshProfileImage("8e3738ba-c510-44ba-92d2-49e3938d2415", profile,
                Utils.getMemberProfileImageResourceIDentifier(""));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String familyId = member.getFamilyId();
                CommonPersonObject patient = null;
                if (familyId != null) {
                    Log.d("Family", familyId);
                    patient = Utils.context().commonrepository(Utils.metadata().familyRegister.tableName)
                            .findByCaseID(familyId);
                }
                if (isLocal && patient != null) {
                    // show family profile
                    Intent intent = new Intent(getContext(), Utils.metadata().profileActivity);
                    intent.putExtra("family_base_entity_id", patient.getCaseId());
                    intent.putExtra("family_head",
                            Utils.getValue(patient.getColumnmaps(), "family_head", false));
                    intent.putExtra("primary_caregiver",
                            Utils.getValue(patient.getColumnmaps(), "primary_caregiver", false));
                    intent.putExtra("village_town",
                            Utils.getValue(patient.getColumnmaps(), "village_town", false));
                    intent.putExtra("family_name",
                            Utils.getValue(patient.getColumnmaps(), "first_name", false));
                    intent.putExtra("go_to_due_page", false);
                    getContext().startActivity(intent);
                } else {
                    // check if member exists locally
                    CommonPersonObject personObject = getCommonRepository(Utils.metadata().familyMemberRegister.tableName).findByBaseEntityId(member.getBaseEntityId());

                    if (personObject == null) {
                        // pull client record from server
                        ProgressDialog progressDialog = new ProgressDialog(getContext());
                        progressDialog.setCancelable(false);
                        PullEventClientRecordUtil.pullEventClientRecord(member.getBaseEntityId(), pullEventClientRecordListener, progressDialog, "");

                    } else {
                        // show member profile
                        showProfile(personObject, v);
                    }
                }
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

    private String isNull(String string) {
        if (string == null) {
            return "";
        } else {
            return string.trim();
        }
    }

    private String getAge(Date birthDate) {
        DateTime birthDateTime = new DateTime(birthDate.getTime());
        DateTime now = new DateTime(new Date().getTime());
        int age = now.getYear() - birthDateTime.getYear();
        if (age == 0) {
            return now.getMonthOfYear() - birthDateTime.getMonthOfYear() + " M";
        }
        return String.valueOf(age);

    }

    public void goToProfileActivity(View view, Bundle fragmentArguments) {
        if (view.getTag() instanceof CommonPersonObjectClient) {
            CommonPersonObjectClient commonPersonObjectClient = (CommonPersonObjectClient) view.getTag();
            String entityType = Utils.getValue(commonPersonObjectClient.getColumnmaps(), ChildDBConstants.KEY.ENTITY_TYPE, false);
            if (CoreConstants.TABLE_NAME.CHILD.equals(entityType) || CoreConstants.TABLE_NAME.FAMILY_MEMBER.equals(entityType) || CoreConstants.TABLE_NAME.INDEPENDENT_CLIENT.equals(entityType)) {
                if (isAncMember(commonPersonObjectClient.entityId())) {
                    AllClientsUtils.goToAncProfile((Activity) getContext(), commonPersonObjectClient);
                } else if (isPncMember(commonPersonObjectClient.entityId())) {
                    AllClientsUtils.gotToPncProfile((Activity) getContext(), commonPersonObjectClient, fragmentArguments);
                } else if (isLDMember(commonPersonObjectClient.entityId())) {
                    AllClientsUtils.goToLDProfile((Activity) getContext(), commonPersonObjectClient);
                } else if (CoreConstants.TABLE_NAME.CHILD.equals(entityType)) {
                    AllClientsUtils.goToChildProfile((Activity) getContext(), commonPersonObjectClient, fragmentArguments);
                } else {
                    goToOtherMemberProfileActivity(commonPersonObjectClient, fragmentArguments);
                }
            }
            ((Activity) getContext()).finish();
        }
    }

    public void goToOtherMemberProfileActivity(CommonPersonObjectClient patient, Bundle bundle) {
        String entityType = patient.getColumnmaps().get("entity_type");

        Intent intent;
        if (CoreConstants.TABLE_NAME.INDEPENDENT_CLIENT.equals(entityType)) {
            intent = new Intent(getContext(), AllClientsMemberProfileActivity.class);
        } else {
            intent = new Intent(getContext(), FamilyOtherMemberProfileActivity.class);
        }
        intent.putExtras(bundle != null ? bundle : new Bundle());
        intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
        intent.putExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON, patient);
        intent.putExtra(Constants.INTENT_KEY.VILLAGE_TOWN, patient.getColumnmaps().get(DBConstants.KEY.VILLAGE_TOWN));
        passToolbarTitle((Activity) getContext(), intent);

        getContext().startActivity(intent);
    }

    private boolean isPncMember(String entityId) {
        return HfPncDao.isPNCMember(entityId);
    }

    private boolean isAncMember(String entityId) {
        return AncDao.isANCMember(entityId);
    }

    private boolean isLDMember(String entityId) {
        return LDDao.isRegisteredForLD(entityId);
    }

    private final Listener<String> pullEventClientRecordListener = new Listener<String>() {
        public void onEvent(final String baseEntityId) {
            if (baseEntityId != null) {
                // show profile view
                CommonPersonObject personObject = getCommonRepository(Utils.metadata().familyMemberRegister.tableName).findByBaseEntityId(baseEntityId);

                if (personObject == null) return;

                View v = new View(getContext());
                showProfile(personObject, v);

            } else {
                Utils.showShortToast(getContext(), "Error pulling record from server");
            }
        }
    };

    private void showProfile(CommonPersonObject personObject, View v) {
        final CommonPersonObjectClient client = new CommonPersonObjectClient(personObject.getCaseId(), personObject.getDetails(), "");

        String relationId = Utils.getValue(personObject.getColumnmaps(), "relational_id", false);
        CommonPersonObject family = getCommonRepository(Utils.metadata().familyRegister.tableName).findByBaseEntityId(relationId);

        if (family == null) {
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setCancelable(false);
            PullEventClientRecordUtil.pullEventClientRecord(relationId, pullEventClientRecordListener, progressDialog, personObject.getCaseId());
        } else {
            String village = Utils.getValue(family.getColumnmaps(), Constants.INTENT_KEY.VILLAGE_TOWN, false);

            Map<String, String> columnMaps = personObject.getColumnmaps();
            columnMaps.put(Constants.INTENT_KEY.VILLAGE_TOWN, village);
            client.setColumnmaps(columnMaps);

            v.setTag(client);

            Bundle s = new Bundle();
            goToProfileActivity(v, s);
        }
    }
}
