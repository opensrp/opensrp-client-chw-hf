package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.hf.model.HvlResultsFragmentModel;
import org.smartregister.chw.pmtct.fragment.BaseHvlResultsFragment;
import org.smartregister.chw.pmtct.presenter.BaseHvlResultsFragmentPresenter;

public class HvlResultsFragment extends BaseHvlResultsFragment {

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new BaseHvlResultsFragmentPresenter(this, new HvlResultsFragmentModel(), null);
    }
}
