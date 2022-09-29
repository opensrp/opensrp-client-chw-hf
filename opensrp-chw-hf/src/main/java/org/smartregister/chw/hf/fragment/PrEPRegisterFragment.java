package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.core.fragment.CoreKvpRegisterFragment;
import org.smartregister.chw.core.model.CoreKvpRegisterFragmentModel;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.PrEPProfileActivity;
import org.smartregister.chw.hf.presenter.PrEPRegisterFragmentPresenter;

public class PrEPRegisterFragment extends CoreKvpRegisterFragment {

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new PrEPRegisterFragmentPresenter(this, new CoreKvpRegisterFragmentModel(), null);
    }

    @Override
    protected int getTitleString() {
        return R.string.menu_prep;
    }

    @Override
    protected void openProfile(String baseEntityId) {
        PrEPProfileActivity.startProfile(requireActivity(), baseEntityId);
    }
}
