package org.smartregister.chw.hf.fragment;

import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.activity.HvlResultsViewActivity;
import org.smartregister.chw.hf.model.HvlResultsFragmentModel;
import org.smartregister.chw.pmtct.fragment.BaseHvlResultsFragment;
import org.smartregister.chw.pmtct.presenter.BaseHvlResultsFragmentPresenter;
import org.smartregister.chw.pmtct.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.util.Utils;

public class HvlResultsFragment extends BaseHvlResultsFragment {

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new BaseHvlResultsFragmentPresenter(this, new HvlResultsFragmentModel(), null);
    }

    @Override
    public void openResultsForm(CommonPersonObjectClient client) {
        String baseEntityId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
        String formSubmissionId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.ENTITY_ID, false);
        try {
            JSONObject jsonObject = (new FormUtils()).getFormJsonFromRepositoryOrAssets(requireContext(), org.smartregister.chw.hf.utils.Constants.JsonForm.getHvlTestResultsForm());
            assert jsonObject != null;
            HvlResultsViewActivity.startResultsForm(getContext(), jsonObject.toString(), baseEntityId, formSubmissionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
