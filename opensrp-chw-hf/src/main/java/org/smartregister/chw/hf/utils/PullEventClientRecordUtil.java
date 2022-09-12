package org.smartregister.chw.hf.utils;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.DristhiConfiguration;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.sync.HfClientProcessor;
import org.smartregister.domain.Client;
import org.smartregister.domain.Event;
import org.smartregister.domain.Response;
import org.smartregister.domain.db.EventClient;
import org.smartregister.event.Listener;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.sync.intent.SyncIntentService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class PullEventClientRecordUtil {

    public static void pullEventClientRecord(final String baseEntityId, final Listener<String> listener,
                                             final ProgressDialog progressDialog, String originalBaseEntityId) {

        org.smartregister.util.Utils.startAsyncTask(new AsyncTask<Void, Void, String[]>() {

            @Override
            protected void onPreExecute() {
                progressDialog.show();
            }

            @Override
            protected String[] doInBackground(Void... params) {
                publishProgress();
                return pull(baseEntityId);
            }

            @Override
            protected void onPostExecute(String[] result) {
                if (!result[1].equalsIgnoreCase(CoreConstants.EventType.FAMILY_REGISTRATION)) {
                    // pull family record
                    PullEventClientRecordUtil.pullEventClientRecord(result[2], listener, progressDialog, baseEntityId);
                } else {
                    listener.onEvent(originalBaseEntityId);
                    progressDialog.dismiss();
                }
            }
        }, null);
    }

    private static String[] pull(String baseEntityId) {
        if (baseEntityId == null || baseEntityId.isEmpty()) {
            Timber.d("entityId doesn't exist");
            return null;
        }

        Context context = CoreLibrary.getInstance().context();
        DristhiConfiguration configuration = context.configuration();

        String baseUrl = configuration.dristhiBaseURL();

        String paramString = "?baseEntityId=" + urlEncode(baseEntityId.trim()) + "&limit=1000";
        String uri = baseUrl + SyncIntentService.SYNC_URL + paramString;

        Timber.d(new StringBuilder(PullEventClientRecordUtil.class.getCanonicalName()).append(" ").append(uri).toString());

        String eventType = "";
        String familyId = "";
        try {
            Response<String> response = context.getHttpAgent().fetch(uri);
            // TO Do: validate response
            if (response == null || response.isFailure()) {
                return null;
            }

            JSONObject jsonObject = new JSONObject(response.payload());

            EventClientRepository eventClientRepository = new EventClientRepository();
            Event event = eventClientRepository.convert(jsonObject.getJSONArray("events").get(0).toString(), Event.class);
            Client client = eventClientRepository.convert(jsonObject.getJSONArray("clients").get(0).toString(), Client.class);

            eventType = event.getEventType();
            if (!eventType.equalsIgnoreCase(CoreConstants.EventType.FAMILY_REGISTRATION) && client.getRelationships().get("family") != null) {
                familyId = client.getRelationships().get("family").get(0);
            }

            List<EventClient> eventClientList = new ArrayList<>();
            eventClientList.add(new EventClient(event, client));

            JSONArray events = getOutOFCatchmentJsonArray(new JSONObject(response.payload()), "events");
            JSONArray clients = getOutOFCatchmentJsonArray(new JSONObject(response.payload()), "clients");

            HealthFacilityApplication.getInstance().getEcSyncHelper().batchSave(events, clients);

            List<EventClient> clientEventsSaved = CoreLibrary.getInstance().context().getEventClientRepository().getEventsByBaseEntityIdsAndSyncStatus("Synced", Arrays.asList(baseEntityId));
            for (EventClient eventClient : clientEventsSaved) {
                eventClient.setClient(client);
            }
            eventClientList.addAll(clientEventsSaved);
            HfClientProcessor.getInstance(context.applicationContext()).processClient(eventClientList);

        } catch (Exception e) {
            Log.d("Pull EventClient error", e.toString());
        }

        return new String[]{baseEntityId, eventType, familyId};
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    private static JSONArray getOutOFCatchmentJsonArray(JSONObject jsonObject, String clients) throws
            JSONException {
        return jsonObject.has(clients) ? jsonObject.getJSONArray(clients) : new JSONArray();
    }
}
