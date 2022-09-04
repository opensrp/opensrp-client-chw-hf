package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.joda.time.DateTime;
import org.joda.time.Years;
import org.smartregister.chw.core.holders.ReferralViewHolder;
import org.smartregister.chw.core.provider.BaseReferralRegisterProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.holder.IssuedReferralViewHolder;
import org.smartregister.chw.referral.util.Constants;
import org.smartregister.chw.referral.util.ReferralUtil;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Location;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.LocationRepository;

import java.util.Locale;

public class ReferralsRegisterProvider extends BaseReferralRegisterProvider {
    private final Context context;

    public ReferralsRegisterProvider(Context context, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, onClickListener, paginationClickListener);
        this.context = context;
    }


    @Override
    public void populatePatientColumn(CommonPersonObjectClient pc, ReferralViewHolder viewHolder) {
        String firstName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String middleName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true);
        String lastName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
        String patientName = org.smartregister.util.Utils.getName(firstName, middleName + " " + lastName);
        String dobString = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false);
        int age = Years.yearsBetween(new DateTime(dobString), new DateTime()).getYears();


        IssuedReferralViewHolder issuedReferralViewHolder = (IssuedReferralViewHolder) viewHolder;
        issuedReferralViewHolder.patientName.setText(String.format(Locale.getDefault(), "%s, %d", patientName, age));
        issuedReferralViewHolder.textViewGender.setText(ReferralUtil.getTranslatedGenderString(context, Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.GENDER, false)));
        issuedReferralViewHolder.textViewService.setText(Utils.getValue(pc.getColumnmaps(), CoreConstants.DB_CONSTANTS.FOCUS, true));
        issuedReferralViewHolder.textViewReferralClinic.setText(getReferralClinic(Utils.getValue(pc.getColumnmaps(), org.smartregister.chw.referral.util.DBConstants.Key.PROBLEM, false)));
        issuedReferralViewHolder.textViewReferralClinic.setVisibility(View.VISIBLE);
        setReferralStatusColor(context, issuedReferralViewHolder.textReferralStatus, Utils.getValue(pc.getColumnmaps(), org.smartregister.chw.referral.util.DBConstants.Key.REFERRAL_STATUS, true));
        setVillageNameName(pc, issuedReferralViewHolder.textViewVillage);
        attachPatientOnclickListener(viewHolder.itemView, pc);
    }

    @Override
    public ReferralViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater().inflate(R.layout.issued_referral_register_list_row, parent, false);
        return new IssuedReferralViewHolder(view);
    }


    private String getReferralClinic(String key){
        switch (key.toLowerCase()){
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
    }

    private String removeSquareBrackets(String text){
        if(text.startsWith("[") && text.endsWith("]")){
            return text.substring(1, text.length() - 1).toUpperCase();
        }
        return text.toUpperCase();
    }

    private void setReferralStatusColor(Context context, TextView textViewStatus, String status) {
        switch (status) {
            case Constants.BusinessStatus.EXPIRED:
                textViewStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.alert_urgent_red)
                );
                textViewStatus.setText(context.getString(R.string.referral_status_failed));
                break;
            case Constants.BusinessStatus.COMPLETE:
                textViewStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.alert_complete_green)
                );
                textViewStatus.setText(context.getString(R.string.referral_status_successful));
                break;
            default:
                textViewStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.alert_in_progress_blue)
                );
                textViewStatus.setText(context.getString(R.string.referral_status_pending));
                break;
        }
    }

    private void setVillageNameName(CommonPersonObjectClient pc, TextView textViewVillage) {
        String locationId = Utils.getValue(pc.getColumnmaps(), org.smartregister.chw.referral.util.DBConstants.Key.REFERRAL_HF, false);
        LocationRepository locationRepository = new LocationRepository();
        Location location = locationRepository.getLocationById(locationId);

        if (location != null) {
            textViewVillage.setText(location.getProperties().getName());
        } else {
            textViewVillage.setText(locationId);
        }
    }


}