package org.smartregister.chw.hf.sync;

import static org.smartregister.chw.anc.util.Constants.EVENT_TYPE.DELETE_EVENT;
import static org.smartregister.chw.core.utils.CoreConstants.EventType.ANC_FOLLOWUP_CLIENT_REGISTRATION;
import static org.smartregister.chw.core.utils.CoreConstants.EventType.ANC_PARTNER_TESTING;
import static org.smartregister.chw.core.utils.CoreConstants.EventType.ANC_PREGNANCY_CONFIRMATION;
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
import static org.smartregister.chw.hf.utils.Constants.Events.PNC_CHILD_FOLLOWUP;
import static org.smartregister.chw.hf.utils.Constants.Events.PNC_VISIT;
import static org.smartregister.chw.hf.utils.Constants.FormConstants.FormSubmissionFields.CTC_NUMBER;
import static org.smartregister.chw.hf.utils.Constants.FormConstants.FormSubmissionFields.HIV_TEST_RESULT;
import static org.smartregister.chw.hf.utils.Constants.FormConstants.FormSubmissionFields.HIV_TEST_RESULT_DATE;
import static org.smartregister.chw.hf.utils.Constants.FormConstants.FormSubmissionFields.TYPE_OF_HIV_TEST;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.dao.EventDao;
import org.smartregister.chw.core.sync.CoreClientProcessor;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hf.dao.HfPmtctDao;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.domain.Event;
import org.smartregister.domain.Obs;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.jsonmapping.ClientClassification;
import org.smartregister.domain.jsonmapping.Table;
import org.smartregister.sync.ClientProcessorForJava;

import java.util.ArrayList;
import java.util.Arrays;
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
            case ANC_PREGNANCY_CONFIRMATION:
            case ANC_FOLLOWUP_CLIENT_REGISTRATION:
            case ANC_FIRST_FACILITY_VISIT:
            case ANC_RECURRING_FACILITY_VISIT:
            case PNC_VISIT:
            case PNC_CHILD_FOLLOWUP:
            case LD_PARTOGRAPHY:
            case LD_REGISTRATION:
            case LD_ACTIVE_MANAGEMENT_OF_3RD_STAGE_OF_LABOUR:
            case LD_GENERAL_EXAMINATION:
            case LD_POST_DELIVERY_MOTHER_MANAGEMENT:
            case ANC_PARTNER_TESTING:
            case org.smartregister.chw.kvp.util.Constants.EVENT_TYPE.KVP_BEHAVIORAL_SERVICE_VISIT:
            case org.smartregister.chw.kvp.util.Constants.EVENT_TYPE.KVP_BIO_MEDICAL_SERVICE_VISIT:
            case org.smartregister.chw.kvp.util.Constants.EVENT_TYPE.KVP_STRUCTURAL_SERVICE_VISIT:
            case org.smartregister.chw.kvp.util.Constants.EVENT_TYPE.KVP_OTHER_SERVICE_VISIT:
            case org.smartregister.chw.kvp.util.Constants.EVENT_TYPE.PrEP_FOLLOWUP_VISIT:
            case org.smartregister.chw.vmmc.util.Constants.EVENT_TYPE.VMMC_SERVICES:
            case org.smartregister.chw.vmmc.util.Constants.EVENT_TYPE.VMMC_PROCEDURE:
            case org.smartregister.chw.vmmc.util.Constants.EVENT_TYPE.VMMC_DISCHARGE:
            case org.smartregister.chw.vmmc.util.Constants.EVENT_TYPE.VMMC_FOLLOW_UP_VISIT:
            case org.smartregister.chw.vmmc.util.Constants.EVENT_TYPE.VMMC_NOTIFIABLE_EVENTS:

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

            case org.smartregister.chw.ld.util.Constants.EVENT_TYPE.VOID_EVENT:
            case DELETE_EVENT:
                processDeleteEvent(eventClient.getEvent());
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

    @Override
    public void processDeleteEvent(Event event) {
        try {
            List<String> pmtctFollowupTables = Arrays.asList("ec_ld_partograph", "ec_pmtct_followup", "ec_pmtct_hvl_results", "ec_pmtct_cd4_results", "ec_hei_followup", "ec_hei_hiv_results", "ec_anc_followup", "ec_pnc_followup", "ec_prep_followup");
            if (event.getDetails().containsKey(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.DELETE_FORM_SUBMISSION_ID)) {
                // delete from vaccine table
                EventDao.deleteVaccineByFormSubmissionId(event.getDetails().get(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.DELETE_FORM_SUBMISSION_ID));
                // delete from visit table
                EventDao.deleteVisitByFormSubmissionId(event.getDetails().get(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.DELETE_FORM_SUBMISSION_ID));
                // delete from recurring service table
                EventDao.deleteServiceByFormSubmissionId(event.getDetails().get(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.DELETE_FORM_SUBMISSION_ID));

                //delete from all PMTCT Case Based Management tables that use formSubmissionIds as primaryKeys
                for (String tableName : pmtctFollowupTables) {
                    try {
                        HfPmtctDao.deleteEntryFromTableByFormSubmissionId(tableName, event.getDetails().get(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.DELETE_FORM_SUBMISSION_ID));
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
            } else {
                super.processDeleteEvent(event);
                //delete from all PMTCT Case Based Management tables that use formSubmissionIds as primaryKeys
                for (String tableName : pmtctFollowupTables) {
                    try {
                        HfPmtctDao.deleteEntryFromTableByFormSubmissionId(tableName, event.getFormSubmissionId());
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
            }

            Timber.d("Ending processDeleteEvent: %s", event.getEventId());
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
