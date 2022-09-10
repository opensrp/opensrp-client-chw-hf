package org.smartregister.chw.hf.sync.intent;

import android.content.Intent;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.core.sync.intent.SyncClientEventsPerTaskIntentService;
import org.smartregister.chw.hf.dao.LDDao;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Response;
import org.smartregister.domain.Task;

import java.util.List;

import timber.log.Timber;

public class HfSyncClientEventsPerTaskIntentService extends SyncClientEventsPerTaskIntentService {
    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        List<String> motherBaseEntityIds = LDDao.getBaseEntityIdsOfMothersForChildrenWithEmergencyReferrals();

        if (motherBaseEntityIds != null) {
            try {
                JSONArray baseEntityIds = new JSONArray(new Gson().toJson(motherBaseEntityIds));
                fetchEventsForBaseEntityIds(baseEntityIds);
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    public synchronized void fetchEventsForBaseEntityIds(JSONArray baseEntityIds) {
        Timber.i("Mother's Base Entity ids with missing Events = %s", baseEntityIds.toString());

        try {
            if (getHttpAgent() == null) {
                complete(FetchStatus.fetchedFailed);
                return;
            }

            Response resp = fetchClientEventsByBaseEntityIds(baseEntityIds);
            if (resp.isTimeoutError() || resp.isUrlError()) {
                FetchStatus.fetchedFailed.setDisplayValue(resp.status().displayValue());
                complete(FetchStatus.fetchedFailed);
                return;
            } else if (resp.isFailure()) {
                fetchEventsForBaseEntityIds(baseEntityIds);
                return;
            }

            JSONObject jsonObject = new JSONObject((String) resp.payload());
            int eCount = fetchNumberOfEvents(jsonObject);
            if (eCount < 0) {
                fetchEventsForBaseEntityIds(baseEntityIds);
                return;
            } else {
                processClientEvent(jsonObject); //Process the client and his/her events
            }
        } catch (Exception e) {
            Timber.e(e, "Fetch Retry Exception:  %s", e.getMessage());
            fetchEventsForBaseEntityIds(baseEntityIds);
        }
        complete(FetchStatus.fetched);

    }
}
