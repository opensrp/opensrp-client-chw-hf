package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.hf.listener.KvpServiceActionHandler;
import org.smartregister.chw.kvp.activity.BaseKvpServicesActivity;
import org.smartregister.chw.kvp.handlers.BaseServiceActionHandler;
import org.smartregister.chw.kvp.util.Constants;

public class KvpServiceActivity extends BaseKvpServicesActivity {
    public static void startMe(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, KvpServiceActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        activity.startActivity(intent);
    }

    @Override
    public BaseServiceActionHandler getServiceHandler() {
        return new KvpServiceActionHandler();
    }
}
