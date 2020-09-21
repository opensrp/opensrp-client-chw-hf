package org.smartregister.chw.hf.repository;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.AllConstants;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.repository.CoreChwRepository;
import org.smartregister.chw.core.repository.StockUsageReportRepository;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.dao.FamilyDao;
import org.smartregister.domain.db.Column;
import org.smartregister.family.util.DBConstants;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.IMDatabaseUtils;
import org.smartregister.reporting.ReportingLibrary;
import org.smartregister.repository.AlertRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.util.DatabaseMigrationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import timber.log.Timber;

public class HfChwRepository extends CoreChwRepository {
    private static String appVersionCodePref = "APP_VERSION_CODE";
    private Context context;

    public HfChwRepository(Context context, org.smartregister.Context openSRPContext) {
        super(context, AllConstants.DATABASE_NAME, BuildConfig.DATABASE_VERSION, openSRPContext.session(), CoreChwApplication.createCommonFtsObject(), openSRPContext.sharedRepositoriesArray());
        this.context = context;
    }

    private static void upgradeToVersion2(Context context, SQLiteDatabase db) {
        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
            db.execSQL(VaccineRepository.EVENT_ID_INDEX);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
            db.execSQL(VaccineRepository.FORMSUBMISSION_INDEX);

            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_HIA2_STATUS_COL);

            IMDatabaseUtils.accessAssetsAndFillDataBaseForVaccineTypes(context, db);

        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion2 ");
        }

    }

    private static void upgradeToVersion3(SQLiteDatabase db) {
        try {
            Column[] columns = {EventClientRepository.event_column.formSubmissionId};
            EventClientRepository.createIndex(db, EventClientRepository.Table.event, columns);

            db.execSQL(VaccineRepository.ALTER_ADD_CREATED_AT_COLUMN);
            VaccineRepository.migrateCreatedAt(db);

            db.execSQL(RecurringServiceRecordRepository.ALTER_ADD_CREATED_AT_COLUMN);
            RecurringServiceRecordRepository.migrateCreatedAt(db);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion3 ");
        }
        try {
            Column[] columns = {EventClientRepository.event_column.formSubmissionId};
            EventClientRepository.createIndex(db, EventClientRepository.Table.event, columns);


        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion3 %s", e.getMessage());
        }
    }

    private static void upgradeToVersion4(SQLiteDatabase db) {
        try {
            db.execSQL(AlertRepository.ALTER_ADD_OFFLINE_COLUMN);
            db.execSQL(AlertRepository.OFFLINE_INDEX);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_COL);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
            db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_TEAM_COL);
            db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion4 ");
        }

    }

    private static void upgradeToVersion5(SQLiteDatabase db) {
        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
            db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion5 ");
        }
    }

    private static void upgradeToVersion6(SQLiteDatabase db) {
        try {
            StockUsageReportRepository.createTable(db);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private static boolean checkIfAppUpdated() {
        String savedAppVersion = ReportingLibrary.getInstance().getContext().allSharedPreferences().getPreference(appVersionCodePref);
        if (savedAppVersion.isEmpty()) {
            return true;
        } else {
            int savedVersion = Integer.parseInt(savedAppVersion);
            return (org.smartregister.chw.core.BuildConfig.VERSION_CODE > savedVersion);
        }
    }

    private static void upgradeToVersion7(SQLiteDatabase db) {
        try {
            String indicatorsConfigFile = "config/indicator-definitions.yml";
            String indicatorDataInitialisedPref = "INDICATOR_DATA_INITIALISED";
            ReportingLibrary reportingLibraryInstance = ReportingLibrary.getInstance();

            boolean indicatorDataInitialised = Boolean.parseBoolean(reportingLibraryInstance.getContext().allSharedPreferences().getPreference(indicatorDataInitialisedPref));
            boolean isUpdated = checkIfAppUpdated();
            if (!indicatorDataInitialised || isUpdated) {
                reportingLibraryInstance.readConfigFile(indicatorsConfigFile, db);
                reportingLibraryInstance.initIndicatorData(indicatorsConfigFile, db); // This will persist the data in the DB
                reportingLibraryInstance.getContext().allSharedPreferences().savePreference(indicatorDataInitialisedPref, "true");
                reportingLibraryInstance.getContext().allSharedPreferences().savePreference(appVersionCodePref, String.valueOf(org.smartregister.chw.core.BuildConfig.VERSION_CODE));
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private static void upgradeToVersion10(SQLiteDatabase db) {
        try {
            String addMissingColumnsQuery = "ALTER TABLE ec_family_member ADD COLUMN reasons_for_registration TEXT NULL;\n" +
                    "ALTER TABLE ec_family_member ADD COLUMN has_primary_caregiver VARCHAR;\n" +
                    "ALTER TABLE ec_family_member ADD COLUMN primary_caregiver_name VARCHAR;";
            db.execSQL(addMissingColumnsQuery);

            DatabaseMigrationUtils.createAddedECTables(db,
                    new HashSet<>(Arrays.asList("ec_hiv_register", "ec_hiv_outcome", "ec_hiv_community_followup", "ec_tb_register", "ec_tb_outcome", "ec_tb_community_followup", "ec_hiv_community_feedback", "ec_tb_community_feedback")),
                    HealthFacilityApplication.createCommonFtsObject());
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion21");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.w(HfChwRepository.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 2:
                    upgradeToVersion2(context, db);
                    break;
                case 3:
                    upgradeToVersion3(db);
                    break;
                case 4:
                    upgradeToVersion4(db);
                    break;
                case 5:
                    upgradeToVersion5(db);
                    break;
                case 6:
                    upgradeToVersion6(db);
                    break;
                case 7:
                    upgradeToVersion7(db);
                    break;
                case 8:
                    upgradeToVersion8(db);
                case 9:
                    upgradeToVersion9(db);
                case 10:
                    upgradeToVersion10(db);
                default:
                    break;
            }
            upgradeTo++;
        }
    }

    private void upgradeToVersion8(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_family ADD COLUMN entity_type VARCHAR; " +
                    "UPDATE ec_family SET entity_type = 'ec_family' WHERE id is not null;");

            List<String> columns = new ArrayList<>();
            columns.add(CoreConstants.DB_CONSTANTS.DETAILS);
            columns.add(DBConstants.KEY.ENTITY_TYPE);
            DatabaseMigrationUtils.addFieldsToFTSTable(db, CoreChwApplication.createCommonFtsObject(), CoreConstants.TABLE_NAME.FAMILY, columns);

        } catch (Exception e) {
            Timber.e(e, "commonUpgrade -> Failed to add column 'entity_type' and 'details' to ec_family_search ");
        }
    }

    private void upgradeToVersion9(SQLiteDatabase db) {
        try {
            FamilyDao.migrateAddLocationIdColSQLString(db);
            FamilyDao.migrateInsertLocationIDs(db);
        } catch (Exception ex) {
            Timber.e(ex, "Problems adding sync location ids");
        }
    }
}
