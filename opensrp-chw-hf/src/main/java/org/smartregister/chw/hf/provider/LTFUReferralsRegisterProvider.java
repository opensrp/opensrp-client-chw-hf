package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.DateTime;
import org.joda.time.Years;
import org.smartregister.chw.core.holders.ReferralViewHolder;
import org.smartregister.chw.core.provider.BaseReferralRegisterProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.holder.IssuedReferralViewHolder;
import org.smartregister.chw.referral.util.ReferralUtil;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;

import java.util.Locale;

public class LTFUReferralsRegisterProvider extends BaseReferralRegisterProvider {
    private final Context context;

    public LTFUReferralsRegisterProvider(Context context, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
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
        issuedReferralViewHolder.textViewVillage.setText(Utils.getValue(pc.getColumnmaps(), org.smartregister.chw.referral.util.DBConstants.Key.REFERRAL_HF, true));
        issuedReferralViewHolder.textViewService.setText(Utils.getValue(pc.getColumnmaps(), CoreConstants.DB_CONSTANTS.FOCUS, true));
        issuedReferralViewHolder.textViewReferralClinic.setText(Utils.getValue(pc.getColumnmaps(), org.smartregister.chw.referral.util.DBConstants.Key.PROBLEM, true));
        issuedReferralViewHolder.textViewReferralClinic.setVisibility(View.VISIBLE);
        attachPatientOnclickListener(viewHolder.itemView, pc);
    }

    @Override
    public ReferralViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater().inflate(R.layout.issued_referral_register_list_row, parent, false);
        return new IssuedReferralViewHolder(view);
    }


}