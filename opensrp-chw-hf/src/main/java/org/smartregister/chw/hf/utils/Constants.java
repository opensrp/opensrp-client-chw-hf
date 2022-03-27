package org.smartregister.chw.hf.utils;

import static org.smartregister.chw.core.utils.CoreConstants.JSON_FORM.assetManager;
import static org.smartregister.chw.core.utils.CoreConstants.JSON_FORM.locale;

import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;

public class Constants extends CoreConstants {
    public static String pregnancyOutcome = "preg_outcome";
    public static String LOST_TO_FOLLOWUP = "lost_to_followup";

    public enum FamilyRegisterOptionsUtil {Miscarriage, Other}

    public interface ScheduleGroups {
        String FACILITY_VISIT = "FACILITY_VISIT";
    }

    public interface PregnancyConfirmationGroups {
        String PREGNANCY_CONFIRMATION = "Pregnancy Confirmation";
    }

    public interface PartnerRegistrationConstants {
        int EXISTING_PARTNER_REQUEST_CODE = 12344;
        int NEW_PARTNER_REQUEST_CODE = 12345;
        String INTENT_BASE_ENTITY_ID = "BASE_ENTITY_ID";
        String PARTNER_BASE_ENTITY_ID = "partner_base_entity_id";
    }

    public static final class Events {
        public static final String ANC_PREGNANCY_CONFIRMATION = "Pregnancy Confirmation";
        public static final String ANC_FIRST_FACILITY_VISIT = "ANC First Facility Visit";
        public static final String ANC_RECURRING_FACILITY_VISIT = "ANC Recurring Facility Visit";
        public static final String ANC_FACILITY_VISIT_NOT_DONE = "ANC Facility Visit Not Done";
        public static final String ANC_FACILITY_VISIT_NOT_DONE_UNDO = "ANC Facility Visit Not Done Undo";
        public static final String PMTCT_FIRST_EAC_VISIT = "PMTCT EAC First Visit";
        public static final String PMTCT_SECOND_EAC_VISIT = "PMTCT EAC Second Visit";
        public static final String PMTCT_EAC_VISIT = "PMTCT EAC Visit";
        public static final String UPDATE_HIV_INDEX_TESTING_FOLLOWUP = "Update HIV Index Contact Testing Followup";
        public static final String PARTNER_REGISTRATION_EVENT = "Partner Registration";
        public static final String ANC_PARTNER_TESTING = "Partner Testing";
        public static final String HEI_REGISTRATION = "HEI Registration";
        public static final String HEI_FOLLOWUP = "HEI Followup";
        public static final String HEI_POSITIVE_INFANT = "HEI Positive Infant";
        public static final String HEI_NEGATIVE_INFANT = "HEI Negative Infant";
        public static final String PMTCT_CLOSE_VISITS = "PMTCT Close Visits";
        public static final String PNC_VISIT = "PNC VISIT";
        public static final String CLOSE_PNC_VISITS = "Close PNC Visits";
        public static final String PNC_CHILD_FOLLOWUP = "PNC Child Followup";
        public static final String MOTHER_CHAMPION_COMMUNITY_SERVICES_REFERRAL = "Mother Champion Community Services Referral";
        public static final String MARK_PMTCT_CLIENT_AS_LTF = "Mark PMTCT Client As LTF";
        public static final String MARK_HEI_CLIENT_AS_LTF = "Mark HEI Client As LTF";
        public static final String HEI_COMMUNITY_FOLLOWUP = "HEI Community Followup";
    }

    public static final class TableName {
        public static final String ANC_FIRST_FACILITY_VISIT = "ec_anc_first_facility_visit";
        public static final String ANC_RECURRING_FACILITY_VISIT = "ec_anc_recurring_facility_visit";
        public static final String PMTCT_EAC_VISIT = "ec_pmtct_eac_visit";
        public static final String HEI = "ec_hei";
        public static final String HEI_HIV_RESULTS = "ec_hei_hiv_results";
        public static final String HEI_FOLLOWUP = "ec_hei_followup";
        public static final String PNC_FOLLOWUP = "ec_pnc_followup";
        public static final String PMTCT_FOLLOWUP = "ec_pmtct_followup";
    }

    public static final class Visits {
        public static final String TERMINATED = "Terminated";
        public static final String FIRST_EAC = "First Eac";
        public static final String SECOND_EAC = "Second Eac";
        public static final String PMTCT_VISIT = "Pmtct";
    }

    public static final class ActionList {
        public static final String FOLLOWUP = "Followup_action";
    }

    public static final class JsonForm {
        //TODO: cleanup
        public static final String HIV_REGISTRATION = "hiv_registration";
        public static final String HVL_TEST_RESULTS = "pmtct_hvl_test_results";
        public static final String CD4_TEST_RESULTS = "pmtct_cd4_test_results";
        public static final String EAC_VISITS_FORM = "pmtct_eac_visits";
        public static final String HVL_SUPPRESSION_FORM_AFTER_EAC_1 = "pmtct_hvl_suppression_after_eac_1";
        public static final String HVL_SUPPRESSION_FORM_AFTER_EAC_2 = "pmtct_hvl_suppression_after_eac_2";
        public static final String HVL_CLINICIAN_DETAILS_FORM = "pmtct_hvl_sample_collection";
        private static final String ANC_PREGANCY_CONFIRMATION = "anc_pregnancy_confirmation";
        private static final String PMTCT_REGISTRATION = "pmtct_registration";
        private static final String PMTCT_REGISTRATION_FOR_CLIENTS_KNOWN_ON_ART_FORM = "pmtct_registration_for_clients_known_on_art";
        private static final String COUNSELLING = "anc_counselling";
        private static final String HIV_INDEX_CONTACT_CTC_ENROLLMENT = "hiv_index_contact_ctc_enrollment";
        private static final String PARTNER_REGISTRATION_FORM = "male_partner_registration_form";
        private static final String PMTCT_COUNSELLING = "pmtct_fv_counselling";
        private static final String PMTCT_BASELINE_INVESTIGATION = "pmtct_fv_baseline_investigation";
        private static final String PMTCT_CD4_SAMPLE_COLLECTION = "pmtct_cd4_sample_collection";
        private static final String PMTCT_CLINICAL_STAGING_OF_DISEASE = "pmtct_clinical_staging_of_disease";
        private static final String PMTCT_TB_SCREENING = "pmtct_tb_screening";
        private static final String PMTCT_ARV_LINE = "pmtct_prescription_line_selection";
        private static final String HEI_DNA_PCR_SAMPLE_COLLECTION = "hei_dna_pcr_sample_collection";
        private static final String HEI_ANTIBODY_TEST_SAMPLE_COLLECTION = "hei_antibody_test_sample_collection";
        private static final String HEI_ARV_PRESCRIPTION_HIGH_RISK_INFANT = "hei_arv_prescription_high_risk_infant";
        private static final String HEI_ARV_PRESCRIPTION_HIGH_OR_LOW_RISK_INFANT = "hei_arv_prescription_high_or_low_risk_infant";
        private static final String HEI_CTX_PRESCRIPTION = "hei_rv_ctx";
        private static final String HEI_HIV_TEST_RESULTS = "hei_hiv_test_results";
        private static final String HIV_CLIENT_UPDATE_CTC_NUMBER = "hiv_client_update_ctc_number";
        private static final String PNC_MOTHER_GENERAL_EXAMINATION = "pnc_mother_general_examination";
        private static final String PNC_CHILD_GENERAL_EXAMINATION = "pnc_child_general_examination";
        private static final String PNC_FAMILY_PLANNING_SERVICES = "pnc_family_planning_services";
        private static final String PNC_IMMUNIZATION = "pnc_immunization";
        private static final String PNC_HIV_TEST_RESULTS = "pnc_hiv_test_results";
        private static final String PNC_NUTRITIONAL_SUPPLEMENT = "pnc_nutritional_supplement";
        private static final String PMTCT_FOLLOWUP_STATUS = "pmtct_followup_status";
        public static final String MARK_CLIENT_AS_DECEASED = "mark_client_as_deceased";
        public static final String MARK_CHILD_AS_DECEASED = "mark_child_as_deceased";
        public static final String HEI_COMMUNITY_FOLLOWUP_REFERRAL = "hei_community_followup_referral";

        public static String getHeiCommunityFollowupReferral() {
            return HEI_COMMUNITY_FOLLOWUP_REFERRAL;
        }

        public static String getPncChildGeneralExamination() {
            return PNC_CHILD_GENERAL_EXAMINATION;
        }

        public static String getPncFamilyPlanningServices() {
            return PNC_FAMILY_PLANNING_SERVICES;
        }

        public static String getPncImmunization() {
            return PNC_IMMUNIZATION;
        }

        public static String getPncHivTestResults() {
            return PNC_HIV_TEST_RESULTS;
        }

        public static String getPncNutritionalSupplement() {
            return PNC_NUTRITIONAL_SUPPLEMENT;
        }

        public static String getPncMotherGeneralExamination() {
            return PNC_MOTHER_GENERAL_EXAMINATION;
        }

        public static String getHeiCtxPrescription() {
            return HEI_CTX_PRESCRIPTION;
        }

        public static String getHeiAntibodyTestSampleCollection() {
            return HEI_ANTIBODY_TEST_SAMPLE_COLLECTION;
        }

        public static String getHeiHivTestResults() {
            return HEI_HIV_TEST_RESULTS;
        }

        public static String getHeiArvPrescriptionHighOrLowRiskInfant() {
            return HEI_ARV_PRESCRIPTION_HIGH_OR_LOW_RISK_INFANT;
        }

        public static String getHeiDnaPcrSampleCollection() {
            return HEI_DNA_PCR_SAMPLE_COLLECTION;
        }

        public static String getHeiArvPrescriptionHighRiskInfant() {
            return HEI_ARV_PRESCRIPTION_HIGH_RISK_INFANT;
        }

        public static String getPmtctCounselling() {
            return PMTCT_COUNSELLING;
        }

        public static String getPmtctBaselineInvestigation() {
            return PMTCT_BASELINE_INVESTIGATION;
        }

        public static String getPmtctCd4SampleCollection() {
            return PMTCT_CD4_SAMPLE_COLLECTION;
        }

        public static String getPmtctClinicalStagingOfDisease() {
            return PMTCT_CLINICAL_STAGING_OF_DISEASE;
        }

        public static String getEacVisitsForm() {
            return EAC_VISITS_FORM;
        }

        public static String getPmtctTbScreening() {
            return PMTCT_TB_SCREENING;
        }

        public static String getPmtctArvLine() {
            return PMTCT_ARV_LINE;
        }

        public static String getPartnerRegistrationForm() {
            return Utils.getLocalForm(PARTNER_REGISTRATION_FORM, locale, assetManager);
        }

        public static String getCounselling() {
            return COUNSELLING;
        }

        public static String getPmtctRegistration() {
            return PMTCT_REGISTRATION;
        }

        public static String
        getPmtctRegistrationForClientsKnownOnArtForm() {
            return PMTCT_REGISTRATION_FOR_CLIENTS_KNOWN_ON_ART_FORM;
        }

        public static String getHvlTestResultsForm() {
            return HVL_TEST_RESULTS;
        }

        public static String getCd4TestResultsForm() {
            return CD4_TEST_RESULTS;
        }

        public static String getHvlSuppressionFormAfterEac1() {
            return HVL_SUPPRESSION_FORM_AFTER_EAC_1;
        }

        public static String getHivIndexContactCtcEnrollment() {
            return HIV_INDEX_CONTACT_CTC_ENROLLMENT;
        }

        public static String getHvlSuppressionFormAfterEac2() {
            return HVL_SUPPRESSION_FORM_AFTER_EAC_2;
        }

        public static String getAncPregnancyConfirmation() {
            return Utils.getLocalForm(ANC_PREGANCY_CONFIRMATION, locale, assetManager);
        }

        public static String getHivRegistration() {
            return Utils.getLocalForm(HIV_REGISTRATION, locale, assetManager);
        }

        public static String getMarkClientAsDeceased() {
            return Utils.getLocalForm(MARK_CLIENT_AS_DECEASED, locale, assetManager);
        }

        public static String getMarkChildAsDeceased() {
            return Utils.getLocalForm(MARK_CHILD_AS_DECEASED, locale, assetManager);
        }

        public static String getPmtctFollowupStatus() {
            return PMTCT_FOLLOWUP_STATUS;
        }

        public static String getAncPregnancyConfirmationForm() {
            return ANC_PREGANCY_CONFIRMATION;
        }

        public static String getHvlClinicianDetailsForm() {
            return HVL_CLINICIAN_DETAILS_FORM;
        }

        public static String getHivClientUpdateCtcNumber() {
            return HIV_CLIENT_UPDATE_CTC_NUMBER;
        }

        public static class EacVisits {
            public static final String PMTCT_EAC_VISIT = "pmtct_eac_visits";

            public static String getPmtctEacVisit() {
                return PMTCT_EAC_VISIT;
            }
        }

        public static class AncFirstVisit {
            public static final String OBSTETRIC_EXAMINATION = "anc_fv_obstetric_examination";
            private static final String MEDICAL_AND_SURGICAL_HISTORY = "anc_fv_medical_and_surgical_history";
            private static final String BASELINE_INVESTIGATION = "anc_fv_baseline_investigation";
            private static final String TT_VACCINATION = "anc_fv_tt_vaccination";

            public static String getMedicalAndSurgicalHistory() {
                return Utils.getLocalForm(MEDICAL_AND_SURGICAL_HISTORY, locale, assetManager);
            }

            public static String getBaselineInvestigation() {
                return Utils.getLocalForm(BASELINE_INVESTIGATION, locale, assetManager);
            }

            public static String getObstetricExamination() {
                return Utils.getLocalForm(OBSTETRIC_EXAMINATION, locale, assetManager);
            }

            public static String getTtVaccination() {
                return Utils.getLocalForm(TT_VACCINATION, locale, assetManager);
            }


        }

        public static class AncRecurringVisit {
            public static final String TRIAGE = "anc_rv_triage";
            public static final String CONSULTATION = "anc_rv_consultation";
            public static final String LAB_TESTS = "anc_rv_lab_test";
            public static final String BIRTH_REVIEW_AND_EMERGENCY_PLAN = "anc_rv_birth_review_and_emergency_plan";
            public static final String PARTNER_TESTING = "anc_partner_testing";
            private static final String PHARMACY = "anc_rv_pharmacy";
            private static final String PREGNANCY_STATUS = "anc_rv_pregnancy_status";

            public static String getTriage() {
                return Utils.getLocalForm(TRIAGE, locale, assetManager);
            }

            public static String getConsultation() {
                return Utils.getLocalForm(CONSULTATION, locale, assetManager);
            }

            public static String getLabTests() {
                return Utils.getLocalForm(LAB_TESTS, locale, assetManager);
            }


            public static String getPharmacy() {
                return Utils.getLocalForm(PHARMACY, locale, assetManager);
            }

            public static String getPregnancyStatus() {
                return Utils.getLocalForm(PREGNANCY_STATUS, locale, assetManager);
            }

            public static String getBirthReviewAndEmergencyPlan() {
                return Utils.getLocalForm(BIRTH_REVIEW_AND_EMERGENCY_PLAN, locale, assetManager);
            }

            public static String getPartnerTesting() {
                return Utils.getLocalForm(PARTNER_TESTING, locale, assetManager);
            }
        }

    }

    public static final class JsonFormConstants {
        public static final String NAME_OF_HF = "name_of_hf";
        public static final String STEP1 = "step1";
    }

    public static final class DBConstants {
        public static final String CHW_REFERRAL_SERVICE = "chw_referral_service";
        public static final String ANC_MRDT_FOR_MALARIA = "mRDT_for_malaria";
        public static final String ANC_HIV = "hiv";
        public static final String ANC_SYPHILIS = "syphilis";
        public static final String ANC_HEPATITIS = "hepatitis";
        public static final String TASK_ID = "task_id";
        public static final String HEI_FOLLOWUP_FORM_SUBMISSION_ID = "hei_followup_form_submission_id";
        public static final String HEI_HIV_SAMPLE_ID = "sample_id";
        public static final String HEI_HIV_TEST_RESULT ="hiv_test_result";
        public static final String HEI_HIV_SAMPLE_COLLECTION_DATE = "sample_collection_date";
        public static final String HEI_HIV_RESULT_DATE = "hiv_test_result_date";
        public static final String HEI_HIV_TYPE_OF_TEST = "type_of_hiv_test";
        public static final String HIV_REGISTRATION_DATE = "hiv_registration_date";
    }

    public static final class EacVisitTypes {
        public static final String EAC_FIRST_VISIT = "EAC FIRST VISIT";
        public static final String EAC_SECOND_VISIT = "EAC SECOND VISIT";
    }

    public static final class JSON_FORM_EXTRA {
        public static final String RISK_CATEGORY = "risk_category";
        public static final String HIV_STATUS = "hiv_status";
    }

    public static final class HIV_STATUS {
        public static final String POSITIVE = "positive";
        public static final String NEGATIVE = "negative";
    }

    public static final class HeiHIVTestAtAge {
        public static final String AT_BIRTH = "At Birth";
        public static final String AT_6_WEEKS = "6 Weeks";
        public static final String AT_9_MONTHS = "9 Months";
        public static final String AT_15_MONTHS = "15 Months";
        public static final String AT_18_MONTHS = "18 Months";
    }

    public static final class FormConstants{
        public interface FormSubmissionFields{
            String VISIT_NUMBER = "visit_number";
            String FOLLOWUP_VISIT_DATE = "followup_visit_date";
            String FOLLOWUP_STATUS = "followup_status";
        }
    }
}
