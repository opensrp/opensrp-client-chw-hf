package org.smartregister.chw.hf.model;

import org.smartregister.chw.core.model.CoreFamilyRemoveMemberModel;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;

import java.util.Date;

public class FamilyRemoveMemberModel extends CoreFamilyRemoveMemberModel {
    @Override
    public String getForm(CommonPersonObjectClient client) {
        Date dob = Utils.dobStringToDate(Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false));
        return ((dob != null && getDiffYears(dob, new Date()) >= 5) ? Constants.JSON_FORM.getFamilyDetailsRemoveMember() : Constants.JSON_FORM.getFamilyDetailsRemoveChild());
    }
}
