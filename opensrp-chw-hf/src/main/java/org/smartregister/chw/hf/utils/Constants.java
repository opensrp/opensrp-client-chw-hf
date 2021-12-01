package org.smartregister.chw.hf.utils;

import static org.smartregister.chw.core.utils.CoreConstants.JSON_FORM.assetManager;
import static org.smartregister.chw.core.utils.CoreConstants.JSON_FORM.locale;

import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;

public class Constants extends CoreConstants {
    public static final class Events {
        public static final String ANC_PREGNANCY_CONFIRMATION = "Pregnancy Confirmation";
        public static final String ANC_FIRST_FACILITY_VISIT = "ANC First Facility Visit";
        public static final String ANC_RECURRING_FACILITY_VISIT = "ANC Recurring Facility Visit";
        public static final String ANC_FACILITY_VISIT_NOT_DONE = "ANC Facility Visit Not Done";
        public static final String ANC_FACILITY_VISIT_NOT_DONE_UNDO = "ANC Facility Visit Not Done Undo";
        public static final String PMTCT_EAC_VISIT = "PMTCT EAC Visit";
    }

    public static final class Tables {
        public static final String ANC_FIRST_FACILITY_VISIT = "ec_anc_first_facility_visit";
        public static final String ANC_RECURRING_FACILITY_VISIT = "ec_anc_recurring_facility_visit";
        public static final String PMTCT_EAC_VISIT = "ec_pmtct_eac_visit" ;
    }

    public static final class Visits {
        public static final String TERMINATED = "Terminated";
    }

    public static final class Actions {
        public static final String FOLLOWUP = "Followup_action";
    }

    public static final class JSON_FORM {
        private static final String ANC_PREGANCY_CONFIRMATION = "anc_pregnancy_confirmation";
        public static final String HIV_REGISTRATION = "hiv_registration";

        public static String getPmtctRegistration() {
            return PMTCT_REGISTRATION;
        }

        private static final String PMTCT_REGISTRATION = "pmtct_first_visit";

        public static String getPmtctEacFirst() {
            return PMTCT_EAC_FIRST;
        }

        private static final String PMTCT_EAC_FIRST = "pmtct_eac_first";

        public static String getHvlTestForm() {
            return HVL_TEST_FORM;
        }

        public static String getHvlSuppressionForm() {
            return HVL_SUPPRESSION_FORM;
        }

        private static final String HVL_TEST_FORM = "pmtct_hvl_test";
        private static final String HVL_SUPPRESSION_FORM = "pmtct_hvl_suppression";

        public static String getAncPregnancyConfirmation() {
            return Utils.getLocalForm(ANC_PREGANCY_CONFIRMATION, locale, assetManager);
        }

        public static String getHivRegistration() {
            return Utils.getLocalForm(HIV_REGISTRATION, locale, assetManager);
        }

        public static String getAncPregnancyConfirmationForm() {
            return ANC_PREGANCY_CONFIRMATION;
        }

        public static class ANC_FIRST_VISIT {
            private static final String MEDICAL_AND_SURGICAL_HISTORY = "anc_fv_medical_and_surgical_history";
            private static final String BASELINE_INVESTIGATION = "anc_fv_baseline_investigation";
            public static final String OBSTETRIC_EXAMINATION = "anc_fv_obstetric_examination";
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

        public static class ANC_RECURRING_VISIT {
            public static final String TRIAGE = "anc_rv_triage";
            private static final String CONSULTATION = "anc_rv_consultation";
            private static final String LAB_TESTS = "anc_rv_lab_test";
            private static final String PHARMACY = "anc_rv_pharmacy";
            private static final String PREGNANCY_STATUS = "anc_rv_pregnancy_status";
            public static final String BIRTH_REVIEW_AND_EMERGENCY_PLAN = "anc_rv_birth_review_and_emergency_plan";

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
        }
    }

    public static final class JsonFormConstants {
        public static final String NAME_OF_HF = "name_of_hf";
        public static final String STEP1 = "step1";
    }

    public static final class DBConstants {
        public static final String CHW_REFERRAL_SERVICE = "chw_referral_service";
    }

    public interface SCHEDULE_GROUPS {
        String FACILITY_VISIT = "FACILITY_VISIT";
    }

    public interface PREGNANCY_CONFIRMATION_GROUPS {
        String PREGNANCY_CONFIRMATION = "Pregnancy Confirmation";
    }
}
