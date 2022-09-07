package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import org.json.JSONObject;
import org.smartregister.chw.cdp.activity.BaseOrderDetailsActivity;
import org.smartregister.chw.cdp.util.Constants;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.JsonFormUtils;

public class OrderRequestDetailsActivity extends BaseOrderDetailsActivity {
    public static void startMe(Activity activity, CommonPersonObjectClient pc) {
        Intent intent = new Intent(activity, OrderRequestDetailsActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.CLIENT, pc);
        activity.startActivity(intent);
    }

    @Override
    public int getMainContentView() {
        return R.layout.activity_order_request_details;
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        startActivityForResult(FormUtils.getStartFormActivity(jsonForm, null, this), JsonFormUtils.REQUEST_CODE_GET_JSON);
    }
}
