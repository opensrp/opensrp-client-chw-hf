package org.smartregister.chw.hf.fragment;

import android.widget.TextView;

import com.vijay.jsonwizard.utils.FormUtils;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.smartregister.chw.core.fragment.CoreHivIndexContactsRegisterFragment;
import org.smartregister.chw.core.provider.CoreHivIndexContactsProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.HivIndexContactProfileActivity;
import org.smartregister.chw.hf.activity.HivIndexContactsContactsRegisterActivity;
import org.smartregister.chw.hf.model.HivIndexContactsRegisterFragmentModel;
import org.smartregister.chw.hf.presenter.HivIndexContactsContactsRegisterFragmentPresenter;
import org.smartregister.chw.hf.provider.HfHivIndexContactsRegisterProvider;
import org.smartregister.chw.hiv.dao.HivIndexDao;
import org.smartregister.chw.hiv.domain.HivMemberObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;

import java.util.Objects;
import java.util.Set;

import timber.log.Timber;

public class HivIndexContactsRegisterFragment extends CoreHivIndexContactsRegisterFragment {

    @Override
    public void initializeAdapter(@Nullable Set<? extends View> visibleColumns) {
        CoreHivIndexContactsProvider hivRegisterProvider = new HfHivIndexContactsRegisterProvider(getActivity(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, hivRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }
    @Override
    public void setupViews(android.view.View view) {
        super.setupViews(view);
        TextView dueOnlyTextView = view.findViewById(R.id.due_only_text_view);
        dueOnlyTextView.setText(getString(R.string.feedback_due_only));
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        String viewConfigurationIdentifier = null;
        try {
            viewConfigurationIdentifier = ((HivIndexContactsContactsRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        } catch (NullPointerException e) {
            Timber.e(e);
        }
        presenter = new HivIndexContactsContactsRegisterFragmentPresenter(this, new HivIndexContactsRegisterFragmentModel(), viewConfigurationIdentifier);
    }

    @Override
    protected void openProfile(CommonPersonObjectClient client) {
        if (getActivity() != null) {
            HivIndexContactProfileActivity.startHivIndexContactProfileActivity(getActivity(), Objects.requireNonNull(HivIndexDao.getMember(client.getCaseId())));
        }
    }


    @Override
    protected void openFollowUpVisit(@Nullable HivMemberObject hivMemberObject) {
        if (getActivity() != null) {
            try {
                HivIndexContactsContactsRegisterActivity.startHIVFormActivity(getActivity(), hivMemberObject.getBaseEntityId(), CoreConstants.JSON_FORM.getHivIndexContactFollowupVisit(), (new FormUtils()).getFormJsonFromRepositoryOrAssets(getActivity(), CoreConstants.JSON_FORM.getHivIndexContactFollowupVisit()).toString());
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
    }
}


