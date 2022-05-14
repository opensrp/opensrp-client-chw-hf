package org.smartregister.chw.hf.sync;

import android.content.Context;

import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.sync.CoreClientProcessor;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.domain.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.jsonmapping.ClientClassification;
import org.smartregister.domain.jsonmapping.Table;
import org.smartregister.sync.ClientProcessorForJava;

import timber.log.Timber;

import static org.smartregister.chw.hf.utils.Constants.Events.ANC_FIRST_FACILITY_VISIT;
import static org.smartregister.chw.hf.utils.Constants.Events.ANC_RECURRING_FACILITY_VISIT;
import static org.smartregister.chw.hf.utils.Constants.Events.HEI_FOLLOWUP;
import static org.smartregister.chw.hf.utils.Constants.Events.PNC_VISIT;

public class HfClientProcessor extends CoreClientProcessor {

    private HfClientProcessor(Context context) {
        super(context);
    }

    public static ClientProcessorForJava getInstance(Context context) {
        if (instance == null) {
            instance = new HfClientProcessor(context);
        }
        return instance;
    }

    @Override
    public boolean processHfReportEvents() {
        return true;
    }

    @Override
    public boolean saveReportDateSent() {
        return false;
    }

    @Override
    protected void processEvents(ClientClassification clientClassification, Table vaccineTable, Table serviceTable, EventClient eventClient, Event event, String eventType) throws Exception {
        super.processEvents(clientClassification, vaccineTable, serviceTable, eventClient, event, eventType);

        //TODO: For other events
        switch (eventType) {
            case ANC_FIRST_FACILITY_VISIT:
            case ANC_RECURRING_FACILITY_VISIT:
            case HEI_FOLLOWUP:
            case PNC_VISIT:
            case Constants.EVENT_TYPE.PMTCT_FOLLOWUP:
                if (eventClient.getEvent() == null) {
                    return;
                }
                processVisitEvent(eventClient);
                processEvent(eventClient.getEvent(), eventClient.getClient(), clientClassification);
                break;
            default:
                break;
        }

    }

    private void processVisitEvent(EventClient eventClient) {
        try {
            NCUtils.processHomeVisit(eventClient); // save locally
        } catch (Exception e) {
            String formID = (eventClient != null && eventClient.getEvent() != null) ? eventClient.getEvent().getFormSubmissionId() : "no form id";
            Timber.e("Form id " + formID + ". " + e.toString());
        }
    }
}
