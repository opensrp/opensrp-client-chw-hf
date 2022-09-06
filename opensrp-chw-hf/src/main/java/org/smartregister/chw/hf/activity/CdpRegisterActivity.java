package org.smartregister.chw.hf.activity;

import org.smartregister.chw.core.activity.CoreCdpRegisterActivity;
import org.smartregister.chw.core.fragment.CoreOrdersRegisterFragment;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class CdpRegisterActivity extends CoreCdpRegisterActivity {

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new CoreOrdersRegisterFragment();
    }
}
