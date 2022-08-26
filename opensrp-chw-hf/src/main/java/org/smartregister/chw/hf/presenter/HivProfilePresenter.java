package org.smartregister.chw.hf.presenter;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.chw.core.contract.CoreHivProfileContract;
import org.smartregister.chw.core.presenter.CoreHivProfilePresenter;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.HivProfileActivity;
import org.smartregister.chw.hf.contract.HivProfileContract;
import org.smartregister.chw.hf.model.HfAllClientsRegisterModel;
import org.smartregister.chw.hf.model.HivTbReferralTasksAndFollowupFeedbackModel;
import org.smartregister.chw.hiv.dao.HivIndexDao;
import org.smartregister.chw.hiv.domain.HivIndexContactObject;
import org.smartregister.chw.hiv.domain.HivMemberObject;
import org.smartregister.family.util.Utils;
import org.smartregister.opd.contract.OpdRegisterActivityContract;
import org.smartregister.opd.pojo.OpdDiagnosisAndTreatmentForm;
import org.smartregister.opd.pojo.OpdEventClient;
import org.smartregister.opd.pojo.RegisterParams;

import java.util.List;

import timber.log.Timber;

public class HivProfilePresenter extends CoreHivProfilePresenter
        implements HivProfileContract.Presenter, HivProfileContract.InteractorCallback, OpdRegisterActivityContract.InteractorCallBack {
    private HivMemberObject hivMemberObject;
    private HivProfileContract.Interactor interactor;
    private HfAllClientsRegisterModel model;

    public HivProfilePresenter(CoreHivProfileContract.View view, CoreHivProfileContract.Interactor interactor,
                               HivMemberObject hivMemberObject) {
        super(view, interactor, hivMemberObject);
        this.hivMemberObject = hivMemberObject;
        this.interactor = (HivProfileContract.Interactor) interactor;
        this.model = new HfAllClientsRegisterModel(view.getContext());
    }

    @Override
    public void fetchReferralTasks() {
        interactor.getReferralTasks(CoreConstants.REFERRAL_PLAN_ID, hivMemberObject.getBaseEntityId(), this);
    }

    @Override
    public void updateReferralTasksAndFollowupFeedback(List<HivTbReferralTasksAndFollowupFeedbackModel> tasksAndFollowupFeedbackModels) {
        ((HivProfileContract.View) getView()).setReferralTasksAndFollowupFeedback(tasksAndFollowupFeedbackModels);
    }

    public void saveForm(String jsonString, @NonNull RegisterParams registerParams) {
        try {
            List<OpdEventClient> opdEventClientList = model.processRegistration(jsonString, registerParams.getFormTag());
            if (opdEventClientList == null || opdEventClientList.isEmpty()) {
                return;
            }
            interactor.saveRegistration(opdEventClientList, jsonString, registerParams, hivMemberObject, this);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public void startForm(String formName, String entityId, String metadata, String currentLocationId) throws Exception {

        if (StringUtils.isBlank(entityId)) {
            Triple<String, String, String> triple = Triple.of(formName, metadata, currentLocationId);
            interactor.getNextUniqueId(triple, this);
            return;
        }

        JSONObject form = model.getFormAsJson(formName, entityId, currentLocationId);

        if (formName.equalsIgnoreCase(CoreConstants.JSON_FORM.getHivIndexClientsContactsRegistrationForm())) {
            JSONObject global = form.getJSONObject("global");
            global.put("index_client_age", Utils.getAgeFromDate(hivMemberObject.getAge()));
        }

        if (getView() != null)
            getView().startFormActivity(form, hivMemberObject, ((HivProfileActivity) getView()).getString(R.string.register_hiv_index_clients_contacts));

    }

    @Override
    public void onRegistrationSaved(boolean editMode) {
        //Calling client processor to start processing events in the background
        CoreChildUtils.processClientProcessInBackground();

        HivProfileActivity view = (HivProfileActivity) getView();

        if (view != null) {
            view.hideProgressDialog();
        }

        view.showToast(view.getString(R.string.successful_index_contact_registration_toast));
        new SetIndexClientsTask(getHivMemberObject()).execute();

    }

    @Override
    public void onEventSaved() {

    }

    @Override
    public void onFetchedSavedDiagnosisAndTreatmentForm(@Nullable OpdDiagnosisAndTreatmentForm opdDiagnosisAndTreatmentForm, @NonNull String s, @Nullable String s1) {

    }

    @Override
    public void onNoUniqueId() {
        if (getView() != null)
            getView().displayToast(org.smartregister.family.R.string.no_unique_id);
    }

    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String entityId) {
        if (getView() != null) {
            try {
                startForm(triple.getLeft(), entityId, triple.getMiddle(), triple.getRight());
            } catch (Exception e) {
                Timber.e(e);
                getView().displayToast(org.smartregister.family.R.string.error_unable_to_start_form);
            }
        }
    }

    private class SetIndexClientsTask extends AsyncTask<Void, Void, Integer> {
        private HivMemberObject hivMemberObject;

        public SetIndexClientsTask(HivMemberObject hivMemberObject) {
            this.hivMemberObject = hivMemberObject;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            List<HivIndexContactObject> indexContactObjectList = HivIndexDao.getIndexContacts(hivMemberObject.getBaseEntityId());
            if (indexContactObjectList != null)
                return indexContactObjectList.size();
            else
                return 0;
        }

        @Override
        protected void onPostExecute(Integer param) {
            getView().setIndexClientsStatus(param > 0);
        }
    }
}