package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.view.View;

import org.apache.commons.text.WordUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.configuration.OpdRegisterProviderMetadata;
import org.smartregister.opd.holders.OpdRegisterViewHolder;
import org.smartregister.opd.provider.OpdRegisterProvider;
import org.smartregister.opd.utils.ConfigurationInstancesHelper;

import java.util.Map;

import androidx.annotation.NonNull;

public class HfMaleClientRegisterProvider extends OpdRegisterProvider {
    private final Context context;

    private OpdRegisterProviderMetadata opdRegisterProviderMetadata;

    public HfMaleClientRegisterProvider(@NonNull Context context, @NonNull View.OnClickListener onClickListener, @NonNull View.OnClickListener paginationClickListener) {
        super(context, onClickListener, paginationClickListener);
        this.context = context;
        this.opdRegisterProviderMetadata = ConfigurationInstancesHelper
                .newInstance(OpdLibrary.getInstance()
                        .getOpdConfiguration()
                        .getOpdRegisterProviderMetadata());
    }

    @Override
    public void populatePatientColumn(CommonPersonObjectClient commonPersonObjectClient, OpdRegisterViewHolder viewHolder) {
        int age_val = new Period(new DateTime(opdRegisterProviderMetadata.getDob(commonPersonObjectClient.getColumnmaps())), new DateTime()).getYears();

        Map<String, String> patientColumnMaps = commonPersonObjectClient.getColumnmaps();

        viewHolder.hideRegisterType();
        viewHolder.removeCareGiverName();
        String firstName = opdRegisterProviderMetadata.getClientFirstName(patientColumnMaps);
        String middleName = opdRegisterProviderMetadata.getClientMiddleName(patientColumnMaps);
        String lastName = opdRegisterProviderMetadata.getClientLastName(patientColumnMaps);
        String fullName = org.smartregister.util.Utils.getName(firstName, middleName + " " + lastName);


        String age = String.valueOf(age_val);

        fillValue(viewHolder.textViewChildName, WordUtils.capitalize(fullName) + ", " +
                WordUtils.capitalize(age));
        setAddressAndGender(commonPersonObjectClient, viewHolder);
        addButtonClickListeners(commonPersonObjectClient, viewHolder);

    }


    @Override
    public void setAddressAndGender(CommonPersonObjectClient pc, OpdRegisterViewHolder viewHolder) {
        super.setAddressAndGender(pc, viewHolder);
        String gender_key = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.GENDER, true);
        String gender = "";
        if (gender_key.equalsIgnoreCase("Male")) {
            gender = context.getString(org.smartregister.chw.core.R.string.male);
        }
        fillValue(viewHolder.textViewGender, gender);
    }
}
