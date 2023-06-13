package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.core.fragment.CoreHivstRegisterFragment;
import org.smartregister.chw.hf.activity.HivstProfileActivity;

public class HivstRegisterFragment extends CoreHivstRegisterFragment {

    @Override
    protected void openProfile(String baseEntityId) {
        HivstProfileActivity.startProfile(requireActivity(), baseEntityId, false);
    }
}
