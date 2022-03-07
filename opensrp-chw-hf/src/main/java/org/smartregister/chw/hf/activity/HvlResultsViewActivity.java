package org.smartregister.chw.hf.activity;

import android.view.View;
import android.widget.ImageView;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.fragment.HvlResultsFragment;
import org.smartregister.chw.pmtct.activity.BaseHvlResultsViewActivity;
import org.smartregister.chw.pmtct.fragment.BaseHvlResultsFragment;

public class HvlResultsViewActivity extends BaseHvlResultsViewActivity implements View.OnClickListener {
    @Override
    public BaseHvlResultsFragment getBaseFragment() {
        return new HvlResultsFragment();
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        ImageView backImageView = findViewById(R.id.back);
        backImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back) {
            finish();
        }
    }
}
