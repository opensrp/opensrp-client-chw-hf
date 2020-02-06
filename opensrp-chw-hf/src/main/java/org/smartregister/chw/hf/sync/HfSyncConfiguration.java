package org.smartregister.chw.hf.sync;


import org.smartregister.SyncConfiguration;
import org.smartregister.SyncFilter;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.hf.BuildConfig;

import timber.log.Timber;

/**
 * @author Elly Nerdstone
 */
public class HfSyncConfiguration extends SyncConfiguration {
    @Override
    public int getSyncMaxRetries() {
        return BuildConfig.MAX_SYNC_RETRIES;
    }

    @Override
    public SyncFilter getSyncFilterParam() {
        return SyncFilter.TEAM_ID;
    }

    @Override
    public String getSyncFilterValue() {
        String providerId = org.smartregister.Context.getInstance().allSharedPreferences().fetchRegisteredANM();
        return org.smartregister.Context.getInstance().allSharedPreferences().fetchDefaultTeamId(providerId);
    }

    @Override
    public int getUniqueIdSource() {
        return BuildConfig.OPENMRS_UNIQUE_ID_SOURCE;
    }

    @Override
    public int getUniqueIdBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_BATCH_SIZE;
    }

    @Override
    public int getUniqueIdInitialBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE;
    }

    @Override
    public boolean isSyncSettings() {
        return BuildConfig.IS_SYNC_SETTINGS;
    }

    @Override
    public SyncFilter getEncryptionParam() {
        return SyncFilter.LOCATION;
    }

    @Override
    public boolean updateClientDetailsTable() {
        return false;
    }
}
