package org.smartregister.chw.hf.fragment;

import static org.smartregister.util.JsonFormUtils.FIELDS;
import static org.smartregister.util.JsonFormUtils.STEP1;

import android.os.Bundle;

import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.activity.HeiHivResultsViewActivity;
import org.smartregister.chw.hf.model.HeiHivResultsFragmentModel;
import org.smartregister.chw.hf.presenter.HeiHivResultsFragmentPresenter;
import org.smartregister.chw.hf.provider.HeiHivResultsViewProvider;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.fragment.BaseHvlResultsFragment;
import org.smartregister.chw.pmtct.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.util.Utils;

import java.util.Set;

public class HeiHivResultsFragment extends BaseHvlResultsFragment {

    public static final String BASE_ENTITY_ID = "BASE_ENTITY_ID";
    private String baseEntityId;

    public static HeiHivResultsFragment newInstance(String baseEntityId) {
        HeiHivResultsFragment heiHivResultsFragment = new HeiHivResultsFragment();
        Bundle b = new Bundle();
        b.putString(BASE_ENTITY_ID, baseEntityId);
        heiHivResultsFragment.setArguments(b);
        return heiHivResultsFragment;
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
        presenter = new HeiHivResultsFragmentPresenter(baseEntityId, this, new HeiHivResultsFragmentModel(), null);
    }

    @Override
    public void openResultsForm(CommonPersonObjectClient client) {
        String sampleId = Utils.getValue(client.getColumnmaps(), Constants.DBConstants.HEI_HIV_SAMPLE_ID, false);
        String baseEntityId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
        String formSubmissionId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.ENTITY_ID, false);
        try {
            JSONObject jsonForm = (new FormUtils()).getFormJsonFromRepositoryOrAssets(requireContext(), org.smartregister.chw.hf.utils.Constants.JsonForm.getHeiHivTestResults());
            if (jsonForm != null) {
                jsonForm.getJSONObject(STEP1).getJSONArray(FIELDS).getJSONObject(0).put("value", sampleId);
                HeiHivResultsViewActivity.startResultsForm(getContext(), jsonForm.toString(), baseEntityId, formSubmissionId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
