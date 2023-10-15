package org.smartregister.chw.hf.presenter;

import android.content.Context;

import org.smartregister.chw.core.presenter.CoreVmmcRegisterFragmentPresenter;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.vmmc.contract.VmmcRegisterFragmentContract;
import org.smartregister.chw.vmmc.util.Constants;

import java.text.MessageFormat;

public class VmmcRegisterFragmentPresenter extends CoreVmmcRegisterFragmentPresenter {
    public VmmcRegisterFragmentPresenter(VmmcRegisterFragmentContract.View view, VmmcRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getMainTable() {
        return Constants.TABLES.VMMC_ENROLLMENT;
    }

    public String getDueFilterCondition(String appointmentDate, boolean isReferred, Context context) {
        StringBuilder customFilter = new StringBuilder();

        if (appointmentDate != null && !appointmentDate.equalsIgnoreCase(context.getString(R.string.none))) {
            customFilter.append(MessageFormat.format(" and {0} like ''%{1}%'' ", "next_followup_date", appointmentDate));
        }

        if (isReferred) {
            customFilter.append(MessageFormat.format(" and {0}.{1} IN (SELECT for FROM task WHERE business_status = ''Referred'') ", getMainTable(), "base_entity_id"));
        }
        return customFilter.toString();
    }
}
