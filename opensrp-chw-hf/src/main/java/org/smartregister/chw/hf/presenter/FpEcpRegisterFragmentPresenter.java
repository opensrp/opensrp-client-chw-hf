package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.fp.contract.BaseFpRegisterFragmentContract;
import org.smartregister.chw.fp.presenter.BaseFpRegisterFragmentPresenter;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.hf.utils.Constants;

public class FpEcpRegisterFragmentPresenter extends BaseFpRegisterFragmentPresenter {

    public FpEcpRegisterFragmentPresenter(BaseFpRegisterFragmentContract.View view, BaseFpRegisterFragmentContract.Model model) {
        super(view, model, FamilyPlanningConstants.CONFIGURATION.FP_REGISTRATION_CONFIGURATION);
    }


    public String getMainTable() {
        return Constants.TableName.FP_ECP_REGISTER;
    }

    public String getMainCondition() {
        return  getMainTable() + ".is_closed is 0 AND ecp_eligibility = 'true' ";
    }
}
