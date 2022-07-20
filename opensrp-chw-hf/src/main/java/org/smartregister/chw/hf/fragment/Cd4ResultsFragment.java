package org.smartregister.chw.hf.fragment;

import static org.smartregister.util.JsonFormUtils.FIELDS;
import static org.smartregister.util.JsonFormUtils.STEP1;

import android.os.Bundle;

import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.activity.Cd4ResultsViewActivity;
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

    public static final String BASE_ENTITY_ID = "BASE_ENTITY_ID";
    private String baseEntityId;

    public static Cd4ResultsFragment newInstance(String baseEntityId) {
        Cd4ResultsFragment cd4ResultsFragment = new Cd4ResultsFragment();
        Bundle b = new Bundle();
        b.putString(BASE_ENTITY_ID, baseEntityId);
        cd4ResultsFragment.setArguments(b);
        return cd4ResultsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            this.baseEntityId = getArguments().getString(BASE_ENTITY_ID);
        }
        super.onCreate(savedInstanceState);
    }

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
        presenter = new Cd4ResultsFragmentPresenter(baseEntityId, this, new Cd4ResultsFragmentModel(), null);
    }

    @Override
    public void openResultsForm(CommonPersonObjectClient client) {
        String sampleId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.CD4_SAMPLE_ID, false);
        String baseEntityId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
        String formSubmissionId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.ENTITY_ID, false);
        try {
            JSONObject jsonObject = (new FormUtils()).getFormJsonFromRepositoryOrAssets(requireContext(), org.smartregister.chw.hf.utils.Constants.JsonForm.getCd4TestResultsForm());
            assert jsonObject != null;
            jsonObject.getJSONObject(STEP1).getJSONArray(FIELDS).getJSONObject(0).put("value", sampleId);
            Cd4ResultsViewActivity.startResultsForm(getContext(), jsonObject.toString(), baseEntityId, formSubmissionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
