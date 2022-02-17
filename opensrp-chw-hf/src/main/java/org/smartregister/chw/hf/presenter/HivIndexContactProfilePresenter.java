package org.smartregister.chw.hf.presenter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.chw.core.presenter.CoreHivIndexContactProfilePresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.HivProfileActivity;
import org.smartregister.chw.hf.contract.HivIndexContactProfileContract;
import org.smartregister.chw.hf.custom_view.HivFloatingMenu;
import org.smartregister.chw.hf.model.HfAllClientsRegisterModel;
import org.smartregister.chw.hf.model.HivIndexFollowupFeedbackDetailsModel;
import org.smartregister.chw.hiv.domain.HivIndexContactObject;
import org.smartregister.opd.contract.OpdRegisterActivityContract;
import org.smartregister.opd.pojo.OpdDiagnosisAndTreatmentForm;
import org.smartregister.opd.pojo.OpdEventClient;
import org.smartregister.opd.pojo.RegisterParams;

import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class HivIndexContactProfilePresenter extends CoreHivIndexContactProfilePresenter
        implements HivIndexContactProfileContract.Presenter, HivIndexContactProfileContract.InteractorCallback, OpdRegisterActivityContract.InteractorCallBack {

    private HivIndexContactObject hivIndexContactObject;
    private HivIndexContactProfileContract.Interactor interactor;
    private HfAllClientsRegisterModel model;

    public HivIndexContactProfilePresenter(HivIndexContactProfileContract.View view, HivIndexContactProfileContract.Interactor interactor,
                                           HivIndexContactObject hivIndexContactObject) {
        super(view, interactor, hivIndexContactObject);
        this.hivIndexContactObject = hivIndexContactObject;
        this.interactor = (HivIndexContactProfileContract.Interactor) interactor;
        this.model = new HfAllClientsRegisterModel(view.getContext());
    }

    @Override
    public void fetchReferralTasks() {
        interactor.getReferralTasks(CoreConstants.REFERRAL_PLAN_ID, hivIndexContactObject.getBaseEntityId(), this);
    }


    @Override
    public void getReferralAndFollowupFeedback(List<HivIndexFollowupFeedbackDetailsModel> followupFeedbackDetailsModel) {
        ((HivIndexContactProfileContract.View) getView()).setReferralAndFollowupFeedback(followupFeedbackDetailsModel);

    }

    public void saveForm(String jsonString, @NonNull RegisterParams registerParams) {
        try {
            List<OpdEventClient> opdEventClientList = model.processRegistration(jsonString, registerParams.getFormTag());
            if (opdEventClientList == null || opdEventClientList.isEmpty()) {
                return;
            }
            interactor.saveRegistration(opdEventClientList, jsonString, registerParams, hivIndexContactObject, this);
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
        if (getView() != null)
            getView().startFormActivity(form, hivIndexContactObject, ((HivProfileActivity) getView()).getString(R.string.register_hiv_index_clients_contacts));

    }

    @Override
    public void onRegistrationSaved(boolean editMode) {
        HivProfileActivity view = (HivProfileActivity) getView();

        if (view != null) {
            view.hideProgressDialog();
            ((HivFloatingMenu) Objects.requireNonNull(view.getHivFloatingMenu())).animateFAB();
        }

        view.showToast(view.getString(R.string.successful_index_contact_registration_toast));

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
}