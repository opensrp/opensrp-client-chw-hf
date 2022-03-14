package org.smartregister.chw.hf.fragment;

import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.activity.HeiHivResultsViewActivity;
import org.smartregister.chw.hf.model.HeiHivResultsFragmentModel;
import org.smartregister.chw.hf.presenter.HeiHivResultsFragmentPresenter;
import org.smartregister.chw.hf.provider.HeiHivResultsViewProvider;
import org.smartregister.chw.pmtct.fragment.BaseHvlResultsFragment;
import org.smartregister.chw.pmtct.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.util.Utils;

import java.util.Set;

public class HeiHivResultsFragment extends BaseHvlResultsFragment {

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        HeiHivResultsViewProvider resultsViewProvider = new HeiHivResultsViewProvider(getActivity(), paginationViewHandler, registerActionHandler, visibleColumns);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, resultsViewProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new HeiHivResultsFragmentPresenter(this, new HeiHivResultsFragmentModel(), null);
    }

    @Override
    public void openResultsForm(CommonPersonObjectClient client) {
        String baseEntityId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
        String formSubmissionId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.ENTITY_ID, false);
        try {
            //TODO UPDATE FORM
            JSONObject jsonObject = (new FormUtils()).getFormJsonFromRepositoryOrAssets(requireContext(), org.smartregister.chw.hf.utils.Constants.JsonForm.getCd4TestResultsForm());
            assert jsonObject != null;
            HeiHivResultsViewActivity.startResultsForm(getContext(), jsonObject.toString(), baseEntityId, formSubmissionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
