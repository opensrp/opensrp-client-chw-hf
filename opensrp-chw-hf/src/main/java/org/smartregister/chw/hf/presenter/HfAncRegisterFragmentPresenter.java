package org.smartregister.chw.hf.presenter;

import android.content.Context;

import org.smartregister.chw.anc.contract.BaseAncRegisterFragmentContract;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.presenter.AncRegisterFragmentPresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;

import java.text.MessageFormat;

public class HfAncRegisterFragmentPresenter extends AncRegisterFragmentPresenter {
    public HfAncRegisterFragmentPresenter(BaseAncRegisterFragmentContract.View view, BaseAncRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getMainCondition() {
        return " " + CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.DATE_REMOVED + " is null " +
                "AND " + CoreConstants.TABLE_NAME.ANC_MEMBER + "." + DBConstants.KEY.IS_CLOSED + " is 0 AND confirmation_status = 'Confirmed' ";
    }

    public String getDueFilterCondition(String hivStatus, String appointmentDate, boolean isReferred, Context context) {
        StringBuilder customFilter = new StringBuilder();
        if (hivStatus != null && !hivStatus.equalsIgnoreCase("all")) {
            customFilter.append(MessageFormat.format(" and {0} like ''%{1}%'' ", "hiv", hivStatus));
        }
        if (appointmentDate != null && !appointmentDate.equalsIgnoreCase(context.getString(R.string.none))) {
            customFilter.append(MessageFormat.format(" and {0} like ''%{1}%'' ", "next_facility_visit_date", appointmentDate));
        }

        if (isReferred) {
            customFilter.append(MessageFormat.format(" and {0}.{1} IN (SELECT for FROM task WHERE business_status = ''Referred'') ", getMainTable(), "base_entity_id"));
        }
        return customFilter.toString();
    }

}
