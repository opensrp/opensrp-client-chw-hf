package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.core.fragment.CoreKvpRegisterFragment;
import org.smartregister.chw.core.model.CoreKvpRegisterFragmentModel;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.KvpProfileActivity;
import org.smartregister.chw.hf.presenter.KvpRegisterFragmentPresenter;

public class KvpRegisterFragment extends CoreKvpRegisterFragment {

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new KvpRegisterFragmentPresenter(this, new CoreKvpRegisterFragmentModel(), null);
    }

    @Override
    protected void openProfile(String baseEntityId) {
        KvpProfileActivity.startProfile(requireActivity(), baseEntityId);
    }

    @Override
    protected int getTitleString() {
        return R.string.menu_kvp;
    }
}
