package org.smartregister.chw.hf.fragment;

import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.activity.Cd4ResultsViewActivity;
import org.smartregister.chw.hf.activity.HvlResultsViewActivity;
import org.smartregister.chw.hf.model.Cd4ResultsFragmentModel;
import org.smartregister.chw.hf.presenter.Cd4ResultsFragmentPresenter;
import org.smartregister.chw.hf.provider.Cd4ResultsViewProvider;
import org.smartregister.chw.pmtct.fragment.BaseHvlResultsFragment;
import org.smartregister.chw.pmtct.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.util.Utils;

import java.util.Set;

public class Cd4ResultsFragment extends BaseHvlResultsFragment {

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        Cd4ResultsViewProvider resultsViewProvider = new Cd4ResultsViewProvider(getActivity(), paginationViewHandler, registerActionHandler, visibleColumns);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, resultsViewProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new Cd4ResultsFragmentPresenter(this, new Cd4ResultsFragmentModel(), null);
    }

    @Override
    public void openResultsForm(CommonPersonObjectClient client) {
        String baseEntityId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
        String formSubmissionId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.ENTITY_ID, false);
        try {
            JSONObject jsonObject = (new FormUtils()).getFormJsonFromRepositoryOrAssets(requireContext(), org.smartregister.chw.hf.utils.Constants.JsonForm.getCd4TestResultsForm());
            assert jsonObject != null;
            Cd4ResultsViewActivity.startResultsForm(getContext(), jsonObject.toString(), baseEntityId, formSubmissionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
