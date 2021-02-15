package org.odk.collect.android.sync;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.events.BusProvider;
import org.odk.collect.android.events.DbChangeEvent;
import org.odk.collect.android.events.SyncEvent;
import org.odk.collect.android.external.dao.EntityDataDao;
import org.odk.collect.android.external.dao.ExEntityDao;
import org.odk.collect.android.external.dao.PrefillDao;
import org.odk.collect.android.external.dao.PrefillDataDao;
import org.odk.collect.android.external.model.*;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.restservices.EntityRestClient;
import org.odk.collect.android.restservices.ServiceGenerator;
import org.odk.collect.android.utilities.ExEntityUtils;
import org.odk.collect.android.utilities.ToastUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;

/**
 * Created by victor on 23-Oct-15.
 */
public class KengaSyncroniser {
    public static final String TAG = "KengaSyncroniser";
    public static String endPointUrl = ServiceGenerator.API_BASE_URL;
    private SQLiteDatabase db;
    private GeneralSharedPreferences settings;
    private EntityRestClient restClient;

    private Context context;
    private EntityDataDao entityDataDao;
    private ExEntityDao entityDao;
    private PrefillDao prefillDao;
    private PrefillDataDao prefillDataDao;

    public KengaSyncroniser(Context context) {
        this.context = context;
        init(context);
    }

    private void init(Context context) {
        settings = GeneralSharedPreferences.getInstance();
        try {
            endPointUrl = (String) settings.get(GeneralKeys.KEY_SERVER_URL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        restClient = getEntityRestClient();
        entityDataDao = new EntityDataDao();
        entityDao = new ExEntityDao();
        prefillDao = new PrefillDao();
        prefillDataDao = new PrefillDataDao();
        setMisUrl();
    }

    public void sync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BusProvider.getInstance().post(new SyncEvent(SyncEvent.SYNC_START));
                    syncPreloadEntities();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    BusProvider.getInstance().post(new SyncEvent(SyncEvent.SYNC_END));
                }
            }
        }).start();

    }

    public void performDataSync(final ExEntity exEntity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BusProvider.getInstance().post(new SyncEvent(SyncEvent.SYNC_START));
                    syncDataForEnttity(exEntity);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    BusProvider.getInstance().post(new SyncEvent(SyncEvent.SYNC_END));
                }
            }
        }).start();
    }


    private boolean createDataTableForEntity(ExEntity exEntity) {
        try {
            String displayField = exEntity.getDisplayField().split(EntityData.DISPLAY_FIELD_SEPERATORE)[0];
            List<TableAttr> tableAttrs = new ArrayList<TableAttr>();
            tableAttrs.add(new TableAttr(exEntity.getKeyField(), true, "TEXT"));
            tableAttrs.add(new TableAttr(displayField, false, "TEXT"));

            EntityDataDao myDbHelper = new EntityDataDao(context);
            myDbHelper.createTable(exEntity.getTableName(), tableAttrs);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void syncPreloadEntities() {
        try {
            Call<List<ExEntity>> call = restClient.getPreloadEntities(getPath());
            call.enqueue(new Callback<List<ExEntity>>() {
                @Override
                public void onResponse(Call<List<ExEntity>> call, Response<List<ExEntity>> response) {
                    List<ExEntity> odxEntityList = response.body();
                    if (odxEntityList != null) {
                        Log.e(TAG, "new preload entities downloaded:" + odxEntityList.size());
                        insertEntities(odxEntityList);
                        syncData();
                    }
                }

                @Override
                public void onFailure(Call<List<ExEntity>> call, Throwable t) {

                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void insertEntities(List<ExEntity> odxEntityList) {
        if (odxEntityList.isEmpty()) {
            return;
        }
        entityDao.deleteAll();
        for (ExEntity entity : odxEntityList) {
            insertEntity(entity);
        }
    }

    public void insertEntity(ExEntity exEntity) {
        entityDao.deleteExEntity(exEntity);
        Log.e(TAG, "new preload entity found:" + exEntity.getTableName());
//      delete existing entity Data table
        entityDataDao.deleteTable(exEntity.getTableName());
        exEntity.setId(UUID.randomUUID().toString());
        entityDao.insert(exEntity);
        //Create data table
        entityDataDao.createDataTableForEntity(exEntity);
    }

    public void syncData() {
        List<ExEntity> odxEntityList = entityDao.loadAll();
        Log.e(TAG, "Entity to fetch data for:" + odxEntityList.size());
        for (final ExEntity odxEntity : odxEntityList) {
            Log.e(TAG, "Entity to fetch data for:" + odxEntity.getTableName());
            syncDataForEnttity(odxEntity);
        }
    }

    private void syncDataForEnttity(final ExEntity entity) {
        Call<List<EntityData>> call = restClient.getEntityData(getPath(), entity.getTableName(), entity.getKeyField(), entity.getDisplayField());
        call.enqueue(new Callback<List<EntityData>>() {
            @Override
            public void onResponse(Call<List<EntityData>> call, Response<List<EntityData>> response) {
                List<EntityData> entityDataList = response.body();
                try {
                    if (entityDataList != null) {
                        insertEntityData(entityDataList, entity);
                        if (!entityDataList.isEmpty()) {
                            BusProvider.getInstance().post(new DbChangeEvent(entityDataList, entity));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    BusProvider.getInstance().post(new SyncEvent(SyncEvent.SYNC_END));
                }
            }

            @Override
            public void onFailure(Call<List<EntityData>> call, Throwable t) {
                t.printStackTrace();
                BusProvider.getInstance().post(new SyncEvent(SyncEvent.SYNC_END));
            }
        });
    }

    public void insertEntityData(List<EntityData> entityDataList, ExEntity entity) {
        insertEntityDataTx(entityDataList, entity);
        Log.e(TAG, "Data Successfully inserted for this table " + entity.getTableName());
    }

    public void insertEntityDataTx(List<EntityData> entityDataList, ExEntity entity) {
        Log.e(TAG, "Deleting existing data..");
        entityDataDao.deleteAllData(entity.getTableName());
        Log.e(TAG, "Data deleted,Inserting new data...");
        entityDataDao.insertEntityDataInTX(entity.getTableName(), entityDataList);
        Log.e(TAG, "Data Successfully inserted for this table " + entity.getTableName());
    }

    public EntityRestClient getEntityRestClient() {
        String username = (String) settings.get(GeneralKeys.KEY_USERNAME);
        String password = (String) settings.get(GeneralKeys.KEY_PASSWORD);
        return ServiceGenerator.createService(EntityRestClient.class, username, password);
    }

    public boolean platformSettingsEntered() {
        String username = (String) settings.get(GeneralKeys.KEY_USERNAME);
        String password = (String) settings.get(GeneralKeys.KEY_PASSWORD);
        if (username == null || password == null) {
            return false;
        }
        return true;
    }


    public void setMisUrl() {
        String defaultMisUrl = (String) settings.get(GeneralKeys.KEY_MIS_URL);
        if (defaultMisUrl != null && !defaultMisUrl.equals("") && ExEntityUtils.isValidUrl(defaultMisUrl)) {
            if (!defaultMisUrl.endsWith("/")) {
                defaultMisUrl += "/";
            }
            ServiceGenerator.API_BASE_URL = defaultMisUrl;
            return;
        }
        String kengaUrl = (String) settings.get(GeneralKeys.KEY_SERVER_URL);
        if (kengaUrl != null) {
            String misUrl = ExEntityUtils.generateMisUrlFromKenga(kengaUrl);
            ServiceGenerator.API_BASE_URL = misUrl;
        }
    }

    public void setMisUrl(String url) {
        ServiceGenerator.API_BASE_URL = url;
    }

    public String getPath() {
        String path = ServiceGenerator.PATH;
        String misUrl = ServiceGenerator.API_BASE_URL;
        if (misUrl != null) {
            Uri uri = Uri.parse(misUrl);
            path = uri.getPath().replaceAll("/", "");
        }
        return path;
    }

    public List<ExEntity> loadEntitiesFromDb() {
        List<ExEntity> exEntities = entityDao.loadAll();
        return exEntities;
    }

    public ExEntity findEntityByTableName(String tableName) {
        return entityDao.getExEntity("TABLE_NAME", tableName);
    }

    public void saveFilters(List<PreFillFilter> prefillFilters) {
        prefillDao.deleteAll();
        for (PreFillFilter filter : prefillFilters) {
            saveFilter(filter);
        }
    }

    public void saveFilters(List<PreFillFilter> prefillFilters, String tableName) {
        prefillDao.deleteWhereTable(tableName);
        for (PreFillFilter filter : prefillFilters) {
            saveFilter(filter);
        }
    }

    public List<PreFillFilter> loadPrefillFilters() {
        return prefillDao.findAll();
    }

    public List<PreFillFilter> loadPrefillFilters(String tableName) {
        return prefillDao.findAllByTableName(tableName);
    }

    public List<PrefillData> findAllPrefillDataLists() {
        return prefillDataDao.selectAll();
    }

    private void saveFilter(PreFillFilter filter) {
        prefillDao.insert(filter);
    }

    public void syncPrefillData(final boolean isBackgroundJob) {
        Log.e(TAG, "Syncing pre-fill entities");
        List<ExEntity> entityList = entityDao.loadAll();
        forwardLoop(entityList,0,isBackgroundJob);

       /* for (final ExEntity exEntity : entityList) {
            List<PreFillFilter> preFillFilterList = prefillDao.findAllByTableName(exEntity.getTableName());
            System.out.println(preFillFilterList.size());
//            final ExEntity exEntity = entityDao.getExEntity("TABLE_NAME", preFillFilterList.get(0).getTableName());
            if (preFillFilterList != null || !preFillFilterList.isEmpty()) {
                exEntity.setPrefillFilterList(preFillFilterList);
                Call<List<EntityData>> call = getEntityRestClient().downloadFilteredEntityDataMap(getPath(), exEntity);
                call.enqueue(new Callback<List<EntityData>>() {
                    @Override
                    public void onResponse(Call<List<EntityData>> call, Response<List<EntityData>> response) {
                        try {
                            List<EntityData> entityDataList = response.body();
                            if (entityDataList != null) {
                                insertEntityDataTx(entityDataList, exEntity);
                            }
                            if(!isBackgroundJob){
                                ToastUtils.showLongToast("Sync successfull.downloaded " + entityDataList.size() + " items");
                            }
                            BusProvider.getInstance().post(new SyncEvent(SyncEvent.SYNC_END));
                        } catch (Exception ex) {
                            Log.e(TAG, ex.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<EntityData>> call, Throwable t) {

                    }
                });
            }
        }*/


    }

    private void forwardLoop(final List<ExEntity> entityList, int idx, final boolean isBackgroundJob){
        if(idx>= entityList.size()){
            BusProvider.getInstance().post(new SyncEvent(SyncEvent.SYNC_END));
            return;
        }
        final ExEntity exEntity = entityList.get(idx);
        final int nextid = idx+1;
        List<PreFillFilter> preFillFilterList = prefillDao.findAllByTableName(exEntity.getTableName());
        Log.e(TAG,"Filter Size:"+preFillFilterList.size());
        if (preFillFilterList != null || !preFillFilterList.isEmpty()) {
            exEntity.setPrefillFilterList(preFillFilterList);
            Call<List<EntityData>> call = getEntityRestClient().downloadFilteredEntityDataMap(getPath(), exEntity);
            call.enqueue(new Callback<List<EntityData>>() {
                @Override
                public void onResponse(Call<List<EntityData>> call, Response<List<EntityData>> response) {
                    try {
                        List<EntityData> entityDataList = response.body();
                        if (entityDataList != null) {
                            insertEntityDataTx(entityDataList, exEntity);
                        }
                        if(!isBackgroundJob){
                            ToastUtils.showLongToast("Sync successful. Downloaded " + entityDataList.size() + " items");
                        }
//                        BusProvider.getInstance().post(new SyncEvent(SyncEvent.SYNC_END));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                    }
                    forwardLoop(entityList,nextid,isBackgroundJob);
                }

                @Override
                public void onFailure(Call<List<EntityData>> call, Throwable t) {

                }
            });
        }
    }

    public void cleanDirtyEntities(List<ExEntity> odxEntityList) {
        try{
            List<ExEntity> entityList = entityDao.loadAll();
            for(ExEntity localEntity:entityList){
                if(!isEntityFound(localEntity,odxEntityList)){
                   entityDao.deleteExEntity(localEntity);
                   entityDataDao.deleteTable(localEntity.getTableName());
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private boolean isEntityFound(ExEntity localEntity, List<ExEntity> odxEntityList) {
        for(ExEntity exEntity:odxEntityList){
            if(exEntity.getTableName().equals(localEntity.getTableName())){
                return true;
            }
        }
        return false;
    }
}
