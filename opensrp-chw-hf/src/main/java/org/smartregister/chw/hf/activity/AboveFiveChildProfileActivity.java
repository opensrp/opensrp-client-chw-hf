package org.smartregister.chw.hf.activity;

import org.smartregister.chw.core.activity.CoreAboveFiveChildProfileActivity;
import org.smartregister.chw.core.model.CoreChildProfileModel;
import org.smartregister.chw.hf.presenter.HfChildProfilePresenter;
import org.smartregister.family.util.Constants;

public class AboveFiveChildProfileActivity extends CoreAboveFiveChildProfileActivity {

    @Override
    protected void initializePresenter() {
        String familyName = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_NAME);
        if (familyName == null) {
            familyName = "";
        }

        presenter = new HfChildProfilePresenter(this, new CoreChildProfileModel(familyName), childBaseEntityId);
    }
}
