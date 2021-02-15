package org.odk.collect.android.activities.entities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.otto.Subscribe;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.adapters.EntityDataAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.events.BusProvider;
import org.odk.collect.android.events.DbChangeEvent;
import org.odk.collect.android.events.SyncEvent;
import org.odk.collect.android.external.dao.EntityDataDao;
import org.odk.collect.android.external.dao.ExEntityDao;
import org.odk.collect.android.external.model.EntityData;
import org.odk.collect.android.external.model.ExEntity;
import org.odk.collect.android.sync.PrefillSyncJob;
import org.odk.collect.android.utilities.ExEntityUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PrefillActivity extends CollectAbstractActivity {
    private static final String TAG = "PrefillActivity";
    private boolean isLegacyForm = false;
    private EntityDataAdapter entityDataAdapter;
    private EntityDataDao entityDataDao;
    private ExEntityDao exEntityDao;
    private ExEntity exEntity;
    // Search EditText
    private EditText inputSearch;
    private ListView listView;
    protected Toolbar toolbar;
    private ArrayList<String> returnFields = new ArrayList<>();
    private ProgressBar progressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
//        setTheme(new ThemeUtils(this).getAppTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefill_data_activity);
        initDb();
        listView = findViewById(android.R.id.list);
        Bundle bundle = getIntent().getExtras();
        String tableName = bundle.getString("table_name");
        String keyFld = bundle.getString("key_field");
        final String displayFlds = bundle.getString("display_field");
        String returnFld = bundle.getString("returnFld");
//        Legacy forms have not return fields;
        if(returnFld != null){
            returnFields = new ArrayList<>(Arrays.asList(returnFld.split(";")));
        }else {
            isLegacyForm = true;
        }
        List<EntityData> entityDataList=new ArrayList<>();
        if(tableName!=null){
            exEntity = exEntityDao.getExEntity("TABLE_NAME",tableName);
            if(exEntity!=null) {
                entityDataList = loadEntityDataFromDb(exEntity);
                setTitle(ExEntityUtils.toTitleCase(exEntity.getName()));
            }
        }

        if (!entityDataList.isEmpty()) {
            ((TextView) findViewById(R.id.prefill_data_status_text)).setText("finished scanning.All data loaded");
        }
        entityDataAdapter = new EntityDataAdapter(this, entityDataList,exEntity);
        listView.setAdapter(entityDataAdapter);
        entityDataAdapter.notifyDataSetChanged();
        inputSearch = findViewById(R.id.prefill_inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                PrefillActivity.this.entityDataAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    EntityData entityData = entityDataAdapter.getFilterList().get(position);
                    if (entityData != null) {
                        Intent intent = new Intent();
                        if(isLegacyForm){
                            List<String> display = new ArrayList<>(entityData.getDisplayFields(exEntity).values());
                            intent.putExtra("value", entityData.getKeyField() + "_" + display.get(0));
                        }else {
                            for(String field:returnFields){
                                intent.putExtra(field, entityData.getOtherFields().get(field));
                            }
                        }
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        initToolbar();

    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new Toolbar.LayoutParams(Gravity.END));
        progressBar.setIndeterminate(true);
        toolbar.addView(progressBar);
        progressBar.setVisibility(View.INVISIBLE);
    }


    private void initDb() {
        entityDataDao = new EntityDataDao();
        exEntityDao = new ExEntityDao();
    }

    private List<EntityData> loadEntityDataFromDb(ExEntity exEntity) {
        List<EntityData> entityDatas = new ArrayList<>();
        try {
            entityDatas = entityDataDao.loadAll(exEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entityDatas;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void onResume() {
        super.onResume();
//      register for db change event,sync event
        try {
            BusProvider.getInstance().register(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onReceiveDbChangeEvent(DbChangeEvent dbChangeEvent) {
        Log.e(TAG, "Received Database changed Event");
        if (dbChangeEvent.getEntity().getTableName().equalsIgnoreCase(exEntity.getTableName())) {
            List<EntityData> entityDataList = dbChangeEvent.getEntityDataList();
            entityDataAdapter.setEntityDataList(entityDataList);
        }
    }


    @Subscribe
    public void onReceiveSyncEvent(SyncEvent syncEvent) {
        Log.e(TAG, "Received Sync Trigger Event " + syncEvent.getStatus());

        if (syncEvent != null && syncEvent.getStatus().equals(SyncEvent.SYNC_START)) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
        } else if (syncEvent != null && syncEvent.getStatus().equals(SyncEvent.SYNC_END)) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
//            reload listview
            refreshListview();
        }
    }

    private void refreshListview() {
        List<EntityData> entityDataList = loadEntityDataFromDb(exEntity);
        entityDataAdapter.setEntityDataList(entityDataList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.prefill_menu, menu);
        try {
            menu.findItem(R.id.sync).setIcon(new IconicsDrawable(this)
                    .icon(FontAwesome.Icon.faw_refresh)
                    .sizeDp(18));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sync:
                try {
                    BusProvider.getInstance().post(new SyncEvent(SyncEvent.SYNC_START));
                    PrefillSyncJob.runJobImmediately();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
