package org.smartregister.chw.hf.activity;

import org.smartregister.chw.core.activity.CoreLDRegisterActivity;
import org.smartregister.chw.hf.fragment.LDRegisterFragment;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class LDRegisterActivity extends CoreLDRegisterActivity {
    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new LDRegisterFragment();
    }
}
