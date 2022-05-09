package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.hf.model.HfPncRegisterFragmentModel;
import org.smartregister.chw.hf.presenter.PncRegisterFragmentPresenter;

public class PncNoMotherRegisterFragment extends PncRegisterFragment {

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new PncRegisterFragmentPresenter(this, new HfPncRegisterFragmentModel(), null);
    }
}
