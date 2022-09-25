package org.smartregister.chw.hf.sync;

import static org.smartregister.chw.hf.utils.Constants.Events.ANC_FIRST_FACILITY_VISIT;
import static org.smartregister.chw.hf.utils.Constants.Events.ANC_RECURRING_FACILITY_VISIT;
import static org.smartregister.chw.hf.utils.Constants.Events.HEI_FOLLOWUP;
import static org.smartregister.chw.hf.utils.Constants.Events.HEI_NEGATIVE_INFANT;
import static org.smartregister.chw.hf.utils.Constants.Events.HEI_POSITIVE_INFANT;
import static org.smartregister.chw.hf.utils.Constants.Events.LD_ACTIVE_MANAGEMENT_OF_3RD_STAGE_OF_LABOUR;
import static org.smartregister.chw.hf.utils.Constants.Events.LD_GENERAL_EXAMINATION;
import static org.smartregister.chw.hf.utils.Constants.Events.LD_PARTOGRAPHY;
import static org.smartregister.chw.hf.utils.Constants.Events.LD_POST_DELIVERY_MOTHER_MANAGEMENT;
import static org.smartregister.chw.hf.utils.Constants.Events.LD_REGISTRATION;
import static org.smartregister.chw.hf.utils.Constants.Events.PNC_VISIT;
import static org.smartregister.chw.hf.utils.Constants.FormConstants.FormSubmissionFields.CTC_NUMBER;
import static org.smartregister.chw.hf.utils.Constants.FormConstants.FormSubmissionFields.HIV_TEST_RESULT;
import static org.smartregister.chw.hf.utils.Constants.FormConstants.FormSubmissionFields.HIV_TEST_RESULT_DATE;
import static org.smartregister.chw.hf.utils.Constants.FormConstants.FormSubmissionFields.TYPE_OF_HIV_TEST;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.sync.CoreClientProcessor;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.domain.Event;
import org.smartregister.domain.Obs;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.jsonmapping.ClientClassification;
import org.smartregister.domain.jsonmapping.Table;
import org.smartregister.sync.ClientProcessorForJava;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

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

        switch (eventType) {
            case ANC_FIRST_FACILITY_VISIT:
            case ANC_RECURRING_FACILITY_VISIT:
            case PNC_VISIT:
            case LD_PARTOGRAPHY:
            case LD_REGISTRATION:
            case LD_ACTIVE_MANAGEMENT_OF_3RD_STAGE_OF_LABOUR:
            case LD_GENERAL_EXAMINATION:
            case LD_POST_DELIVERY_MOTHER_MANAGEMENT:
            case Constants.EVENT_TYPE.PMTCT_FOLLOWUP:
                if (eventClient.getEvent() == null) {
                    return;
                }
                processVisitEvent(eventClient);
                processEvent(eventClient.getEvent(), eventClient.getClient(), clientClassification);
                break;
            case HEI_FOLLOWUP:
            case HEI_POSITIVE_INFANT:
            case HEI_NEGATIVE_INFANT:
                processVisitEvent(eventClient);
                processEvent(eventClient.getEvent(), eventClient.getClient(), clientClassification);
                processHeiFollowupCEvent(eventClient.getEvent());
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

    @Override
    protected String getHumanReadableConceptResponse(String value, Object object) {
        try {
            if (StringUtils.isBlank(value) || (object != null && !(object instanceof Obs))) {
                return value;
            }
            // Skip human readable values and just get values which would aid in translations
            final String VALUES = "values";
            List values = new ArrayList();

            Object valueObject = getValue(object, VALUES);
            if (valueObject instanceof List) {
                values = (List) valueObject;
            }
            if (object == null || values.isEmpty()) {
                return value;
            }

            return values.size() == 1 ? values.get(0).toString() : values.toString();

        } catch (Exception e) {
            Timber.e(e);
        }
        return value;
    }

    private void processHeiFollowupCEvent(Event event) {
        List<Obs> heiFollowupObs = event.getObs();
        String typeOfHivTest = null;
        String hivTestResult = null;
        String hivTestResultDate = null;
        String ctcNumber = null;
        if (heiFollowupObs.size() > 0) {
            for (Obs obs : heiFollowupObs) {
                if (TYPE_OF_HIV_TEST.equals(obs.getFormSubmissionField())) {
                    typeOfHivTest = (String) obs.getValue();
                } else if (HIV_TEST_RESULT.equals(obs.getFormSubmissionField())) {
                    hivTestResult = (String) obs.getValue();
                } else if (HIV_TEST_RESULT_DATE.equals(obs.getFormSubmissionField())) {
                    hivTestResultDate = (String) obs.getValue();
                } else if (CTC_NUMBER.equals(obs.getFormSubmissionField())) {
                    ctcNumber = (String) obs.getValue();
                }
            }

            if (typeOfHivTest != null && typeOfHivTest.equals("Antibody Test"))
                HeiDao.saveAntiBodyTestResults(event.getBaseEntityId(), event.getFormSubmissionId(), hivTestResult, hivTestResultDate, ctcNumber);
        }
    }
}
