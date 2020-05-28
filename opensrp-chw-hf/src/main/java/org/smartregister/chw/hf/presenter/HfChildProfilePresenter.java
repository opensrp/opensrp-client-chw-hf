package org.smartregister.chw.hf.presenter;

import android.util.Pair;

import org.smartregister.chw.core.contract.CoreChildProfileContract;
import org.smartregister.chw.core.presenter.CoreChildProfilePresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.HfChildProfileInteractor;
import org.smartregister.chw.hf.interactor.HfFamilyProfileInteractor;
import org.smartregister.chw.hf.model.ChildRegisterModel;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.repository.AllSharedPreferences;

import timber.log.Timber;

public class HfChildProfilePresenter extends CoreChildProfilePresenter {

    public HfChildProfilePresenter(CoreChildProfileContract.View childView, CoreChildProfileContract.Model model, String childBaseEntityId) {
        super(childView, model, childBaseEntityId);
        setInteractor(new HfChildProfileInteractor());
    }

    @Override
    public void verifyHasPhone() {
        new HfFamilyProfileInteractor().verifyHasPhone(familyID, this);
    }

    @Override
    public void updateChildProfile(String jsonString) {
        getView().showProgressDialog(R.string.updating);
        Pair<Client, Event> pair = new ChildRegisterModel().processRegistration(jsonString);
        if (pair == null) {
            return;
        }

        getInteractor().saveRegistration(pair, jsonString, true, this);
    }

    @Override
    public void startSickChildForm(CommonPersonObjectClient client) {
        try {
            getView().startFormActivity(getFormUtils().getFormJson(CoreConstants.JSON_FORM.getChildSickForm()));
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    @Override
    public void createSickChildEvent(AllSharedPreferences allSharedPreferences, String jsonString) throws Exception {
        getInteractor().setChildBaseEntityId(getChildBaseEntityId());
        getInteractor().createSickChildEvent(allSharedPreferences, jsonString);
    }

    @Override
    public void createSickChildFollowUpEvent(AllSharedPreferences allSharedPreferences, String jsonString) throws Exception {
        getInteractor().setChildBaseEntityId(getChildBaseEntityId());
        getInteractor().createSickChildFollowUpEvent(allSharedPreferences, jsonString);
    }

}
