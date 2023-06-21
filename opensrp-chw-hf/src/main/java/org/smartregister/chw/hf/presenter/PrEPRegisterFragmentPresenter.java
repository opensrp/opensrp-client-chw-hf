package org.smartregister.chw.hf.presenter;

import android.content.Context;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.kvp.contract.KvpRegisterFragmentContract;
import org.smartregister.chw.kvp.presenter.BaseKvpRegisterFragmentPresenter;
import org.smartregister.chw.kvp.util.Constants;

import java.text.MessageFormat;

public class PrEPRegisterFragmentPresenter extends BaseKvpRegisterFragmentPresenter {
    public PrEPRegisterFragmentPresenter(KvpRegisterFragmentContract.View view, KvpRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getMainTable() {
        return Constants.TABLES.PrEP_REGISTER;
    }

    public String getDueFilterCondition(String appointmentDate, boolean isReferred, Context context) {
        StringBuilder customFilter = new StringBuilder();

        if (appointmentDate != null && !appointmentDate.equalsIgnoreCase(context.getString(R.string.none))) {
            customFilter.append(MessageFormat.format(" and {0} like ''%{1}%'' ", "next_visit_date", appointmentDate));
        }

        if (isReferred) {
            customFilter.append(MessageFormat.format(" and {0}.{1} IN (SELECT for FROM task WHERE business_status = ''Referred'') ", getMainTable(), "base_entity_id"));
        }
        return customFilter.toString();
    }
}
