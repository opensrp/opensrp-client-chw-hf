package org.smartregister.chw.hf.model;

import android.util.Pair;

import org.smartregister.chw.core.model.CoreChildRegisterModel;
import org.smartregister.chw.hf.utils.JsonFormUtils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.family.util.Utils;

public class ChildRegisterModel extends CoreChildRegisterModel {

    @Override
    public Pair<Client, Event> processRegistration(String jsonString) {
        return JsonFormUtils.processChildRegistrationForm(Utils.context().allSharedPreferences(), jsonString);
    }
}
