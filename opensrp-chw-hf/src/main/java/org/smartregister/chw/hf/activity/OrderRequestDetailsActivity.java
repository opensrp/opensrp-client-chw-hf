package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.cdp.activity.BaseOrderDetailsActivity;
import org.smartregister.chw.cdp.util.Constants;
import org.smartregister.chw.hf.R;
import org.smartregister.commonregistry.CommonPersonObjectClient;

public class OrderRequestDetailsActivity extends BaseOrderDetailsActivity {
    public static void startMe(Activity activity, CommonPersonObjectClient pc) {
        Intent intent = new Intent(activity, OrderRequestDetailsActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.CLIENT, pc);
        activity.startActivity(intent);
    }

    @Override
    protected int getMainContentView() {
        return R.layout.activity_order_request_details;
    }
}
