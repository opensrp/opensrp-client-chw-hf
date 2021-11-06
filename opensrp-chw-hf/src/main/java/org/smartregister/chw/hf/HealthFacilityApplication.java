package org.smartregister.chw.hf;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.evernote.android.job.JobManager;

import org.jetbrains.annotations.NotNull;
import org.smartregister.AllConstants;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.P2POptions;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.contract.CoreApplication;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.loggers.CrashlyticsTree;
import org.smartregister.chw.core.provider.CoreAllClientsRegisterQueryProvider;
import org.smartregister.chw.core.service.CoreAuthorizationService;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.ChwDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.fp.FpLibrary;
import org.smartregister.chw.hf.activity.AllClientsRegisterActivity;
import org.smartregister.chw.hf.activity.AncRegisterActivity;
import org.smartregister.chw.hf.activity.ChildRegisterActivity;
import org.smartregister.chw.hf.activity.FamilyProfileActivity;
import org.smartregister.chw.hf.activity.FamilyRegisterActivity;
import org.smartregister.chw.hf.activity.FpRegisterActivity;
import org.smartregister.chw.hf.activity.HivIndexContactsContactsRegisterActivity;
import org.smartregister.chw.hf.activity.HivRegisterActivity;
import org.smartregister.chw.hf.activity.HtsRegisterActivity;
import org.smartregister.chw.hf.activity.LoginActivity;
import org.smartregister.chw.hf.activity.MalariaRegisterActivity;
import org.smartregister.chw.hf.activity.PncRegisterActivity;
import org.smartregister.chw.hf.activity.ReferralRegisterActivity;
import org.smartregister.chw.hf.activity.TbRegisterActivity;
import org.smartregister.chw.hf.configs.AllClientsRegisterRowOptions;
import org.smartregister.chw.hf.custom_view.HfNavigationMenu;
import org.smartregister.chw.hf.job.HfJobCreator;
import org.smartregister.chw.hf.model.NavigationModel;
import org.smartregister.chw.hf.repository.HfChwRepository;
import org.smartregister.chw.hf.repository.HfTaskRepository;
import org.smartregister.chw.hf.sync.HfClientProcessor;
import org.smartregister.chw.hf.sync.HfSyncConfiguration;
import org.smartregister.chw.hiv.HivLibrary;
import org.smartregister.chw.malaria.MalariaLibrary;
import org.smartregister.chw.pnc.PncLibrary;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.chw.tb.TbLibrary;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.helper.JsonSpecHelper;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.domain.FamilyMetadata;
import org.smartregister.family.util.DBConstants;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.configuration.OpdConfiguration;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.reporting.ReportingLibrary;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.Repository;
import org.smartregister.repository.TaskNotesRepository;
import org.smartregister.repository.TaskRepository;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class HealthFacilityApplication extends CoreChwApplication implements CoreApplication {
    private CommonFtsObject commonFtsObject;

    @Override
    public FamilyMetadata getMetadata() {
        return FormUtils.getFamilyMetadata(new FamilyProfileActivity(), getDefaultLocationLevel(), getFacilityHierarchy(), getFamilyLocationFields());
    }

    @Override
    public ArrayList<String> getAllowedLocationLevels() {
        return new ArrayList<>(Arrays.asList(BuildConfig.ALLOWED_LOCATION_LEVELS));
    }

    @Override
    public ArrayList<String> getFacilityHierarchy() {
        return new ArrayList<>(Arrays.asList(BuildConfig.LOCATION_HIERACHY));
    }

    @Override
    public String getDefaultLocationLevel() {
        return BuildConfig.DEFAULT_LOCATION;
    }

    public @NotNull Map<String, Class> getRegisteredActivities() {
        Map<String, Class> registeredActivities = new HashMap<>();
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.ANC_REGISTER_ACTIVITY, AncRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.FAMILY_REGISTER_ACTIVITY, FamilyRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.CHILD_REGISTER_ACTIVITY, ChildRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.PNC_REGISTER_ACTIVITY, PncRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.REFERRALS_REGISTER_ACTIVITY, ReferralRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.ALL_CLIENTS_REGISTERED_ACTIVITY, AllClientsRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.MALARIA_REGISTER_ACTIVITY, MalariaRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.FP_REGISTER_ACTIVITY, FpRegisterActivity.class);

        if (BuildConfig.BUILD_FOR_BORESHA_AFYA_SOUTH) {
            registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.HIV_REGISTER_ACTIVITY, HivRegisterActivity.class);
            registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.HTS_REGISTER_ACTIVITY, HtsRegisterActivity.class);
            registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.HIV_INDEX_REGISTER_ACTIVITY, HivIndexContactsContactsRegisterActivity.class);
            registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.TB_REGISTER_ACTIVITY, TbRegisterActivity.class);

        }
        return registeredActivities;
    }

    @Override
    public Repository getRepository() {
        try {
            if (repository == null) {
                repository = new HfChwRepository(getInstance().getApplicationContext(), context);
            }
        } catch (UnsatisfiedLinkError e) {
            Timber.e(e);
        }
        return repository;
    }

    public void setOpenSRPUrl() {
        AllSharedPreferences preferences = Utils.getAllSharedPreferences();
        if (BuildConfig.DEBUG) {
            preferences.savePreference(AllConstants.DRISHTI_BASE_URL, BuildConfig.opensrp_url_debug);
        } else {
            preferences.savePreference(AllConstants.DRISHTI_BASE_URL, BuildConfig.opensrp_url);
        }
    }

    @Override
    public TaskRepository getTaskRepository() {
        if (taskRepository == null) {
            taskRepository = new HfTaskRepository(new TaskNotesRepository());
        }
        return taskRepository;
    }

    public CommonFtsObject getCommonFtsObject() {
        if (commonFtsObject == null) {

            String[] tables = getFTSTables();

            Map<String, String[]> searchMap = getFTSSearchMap();
            Map<String, String[]> sortMap = getFTSSortMap();

            commonFtsObject = new CommonFtsObject(tables);
            for (String ftsTable : commonFtsObject.getTables()) {
                commonFtsObject.updateSearchFields(ftsTable, searchMap.get(ftsTable));
                commonFtsObject.updateSortFields(ftsTable, sortMap.get(ftsTable));
            }
        }
        return commonFtsObject;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        context = Context.getInstance();
        context.updateApplicationContext(getApplicationContext());
        context.updateCommonFtsObject(createCommonFtsObject());
        context.updateCommonFtsObject(getCommonFtsObject());

        //init Job Manager
        SyncStatusBroadcastReceiver.init(this);
        JobManager.create(this).addJobCreator(new HfJobCreator());

        //Necessary to determine the right form to pick from assets
        CoreConstants.JSON_FORM.setLocaleAndAssetManager(HealthFacilityApplication.getCurrentLocale(),
                HealthFacilityApplication.getInstance().getApplicationContext().getAssets());

        //Setup Navigation menu. Done only once when app is created
        NavigationMenu.setupNavigationMenu(this, new HfNavigationMenu(), new NavigationModel(),
                getRegisteredActivities(), false);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashlyticsTree(this.context.allSharedPreferences().fetchRegisteredANM()));
        }

        Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG).build()).build());

        // init json helper
        this.jsonSpecHelper = new JsonSpecHelper(this);

        //Initialize Peer to peer modules
        P2POptions p2POptions = new P2POptions(true);
        p2POptions.setAuthorizationService(new CoreAuthorizationService());

        // init libraries
        CoreLibrary.init(context, new HfSyncConfiguration(), BuildConfig.BUILD_TIMESTAMP, p2POptions);
        ConfigurableViewsLibrary.init(context);
        FamilyLibrary.init(context, getMetadata(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        ImmunizationLibrary.init(context, getRepository(), null, BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        LocationHelper.init(new ArrayList<>(Arrays.asList(BuildConfig.ALLOWED_LOCATION_LEVELS)), BuildConfig.DEFAULT_LOCATION);
        ReportingLibrary.init(context, getRepository(), null, BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        AncLibrary.init(context, getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        PncLibrary.init(context, getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        MalariaLibrary.init(context, getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        FpLibrary.init(context, getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);

        //Setup hiv library
        HivLibrary.init(this);
        HivLibrary.getInstance().setAppVersion(BuildConfig.VERSION_CODE);
        HivLibrary.getInstance().setDatabaseVersion(BuildConfig.DATABASE_VERSION);

        //Setup tb library
        TbLibrary.init(this);
        TbLibrary.getInstance().setAppVersion(BuildConfig.VERSION_CODE);
        TbLibrary.getInstance().setDatabaseVersion(BuildConfig.DATABASE_VERSION);

        //Needed for all clients register
        OpdLibrary.init(context, getRepository(),
                new OpdConfiguration.Builder(CoreAllClientsRegisterQueryProvider.class)
                        .setBottomNavigationEnabled(true)
                        .setOpdRegisterRowOptions(AllClientsRegisterRowOptions.class)
                        .build(),
                BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION
        );
        setOpenSRPUrl();

        Configuration configuration = getApplicationContext().getResources().getConfiguration();
        String language;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            language = configuration.getLocales().get(0).getLanguage();
        } else {
            language = configuration.locale.getLanguage();
        }

        if (language.equals(Locale.FRENCH.getLanguage())) {
            saveLanguage(Locale.FRENCH.getLanguage());
        }
        // set up processor
        FamilyLibrary.getInstance().setClientProcessorForJava(HfClientProcessor.getInstance(getApplicationContext()));
    }

    @Override
    public void logoutCurrentUser() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getApplicationContext().startActivity(intent);
        context.userService().logoutSession();
        Timber.i("Logged out user %s", getContext().allSharedPreferences().fetchRegisteredANM());
    }

    public boolean getChildFlavorUtil(){
        return true;
    }

    public String[] getFTSTables() {
        return new String[]{CoreConstants.TABLE_NAME.FAMILY, CoreConstants.TABLE_NAME.FAMILY_MEMBER, CoreConstants.TABLE_NAME.CHILD};
    }

    public Map<String, String[]> getFTSSearchMap() {
        Map<String, String[]> map = new HashMap<>();
        map.put(CoreConstants.TABLE_NAME.FAMILY, new String[]{
                DBConstants.KEY.BASE_ENTITY_ID, DBConstants.KEY.VILLAGE_TOWN, DBConstants.KEY.FIRST_NAME,
                DBConstants.KEY.LAST_NAME, DBConstants.KEY.UNIQUE_ID, ChwDBConstants.NEAREST_HEALTH_FACILITY
        });

        map.put(CoreConstants.TABLE_NAME.FAMILY_MEMBER, new String[]{
                DBConstants.KEY.BASE_ENTITY_ID, DBConstants.KEY.FIRST_NAME, DBConstants.KEY.MIDDLE_NAME,
                DBConstants.KEY.LAST_NAME, DBConstants.KEY.UNIQUE_ID, ChildDBConstants.KEY.ENTRY_POINT, DBConstants.KEY.DOB, DBConstants.KEY.DATE_REMOVED
        });

        map.put(CoreConstants.TABLE_NAME.CHILD, new String[]{
                DBConstants.KEY.BASE_ENTITY_ID, DBConstants.KEY.FIRST_NAME, DBConstants.KEY.MIDDLE_NAME,
                DBConstants.KEY.LAST_NAME, DBConstants.KEY.UNIQUE_ID, ChildDBConstants.KEY.ENTRY_POINT, DBConstants.KEY.DOB, DBConstants.KEY.DATE_REMOVED
        });
        return map;
    }

    public Map<String, String[]> getFTSSortMap() {
        Map<String, String[]> map = new HashMap<>();
        map.put(CoreConstants.TABLE_NAME.FAMILY, new String[]{DBConstants.KEY.LAST_INTERACTED_WITH, DBConstants.KEY.DATE_REMOVED,
                DBConstants.KEY.FAMILY_HEAD, DBConstants.KEY.PRIMARY_CAREGIVER, DBConstants.KEY.ENTITY_TYPE,
                CoreConstants.DB_CONSTANTS.DETAILS
        });

        map.put(CoreConstants.TABLE_NAME.FAMILY_MEMBER, new String[]{DBConstants.KEY.DOB, DBConstants.KEY.DOD,
                DBConstants.KEY.LAST_INTERACTED_WITH, DBConstants.KEY.DATE_REMOVED, DBConstants.KEY.RELATIONAL_ID
        });

        map.put(CoreConstants.TABLE_NAME.CHILD, new String[]{ChildDBConstants.KEY.LAST_HOME_VISIT, ChildDBConstants.KEY.VISIT_NOT_DONE, DBConstants.KEY
                .LAST_INTERACTED_WITH, ChildDBConstants.KEY.DATE_CREATED, DBConstants.KEY.DATE_REMOVED, DBConstants.KEY.DOB, ChildDBConstants.KEY.ENTRY_POINT
        });
        return map;
    }
}
