package org.odk.collect.android.activities.entities;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.otto.Subscribe;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.EntityDataAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.events.BusProvider;
import org.odk.collect.android.events.DbChangeEvent;
import org.odk.collect.android.events.SyncEvent;
import org.odk.collect.android.external.dao.EntityDataDao;
import org.odk.collect.android.external.model.EntityData;
import org.odk.collect.android.external.model.ExEntity;
import org.odk.collect.android.sync.KengaSyncroniser;
import org.odk.collect.android.utilities.ExEntityUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by victor on 25-Jan-16.
 */
public class EntityDataActivity extends ListActivity {
    private static final String TAG = "EntityListActivity";
    private List<EntityData> entityDataList = new ArrayList<>();
    private EntityDataAdapter entityDataAdapter;
    private EntityDataDao entityDataDao;
    private ExEntity exEntity;
    // Search EditText
    private EditText inputSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entity_data_activity);
        initDb();
        Bundle bundle = getIntent().getExtras();
        String tableName = bundle.getString("table_name");
        if (tableName != null) {
            setTitle(ExEntityUtils.toTitleCase(tableName.replaceAll("_", " ")));
        }
        String keyFld = bundle.getString("key_field");
        String displayFld = bundle.getString("display_field");
        if (tableName != null && keyFld != null && displayFld != null) {
            exEntity = ExEntityUtils.reconstructEntity(tableName, displayFld, keyFld);
            entityDataList = loadEntityDataFromDb(exEntity);
        }

        if (!entityDataList.isEmpty()) {
            ((TextView) findViewById(R.id.entity_data_status_text)).setText("finished scanning.All data loaded");
        }

        entityDataAdapter = new EntityDataAdapter(this, entityDataList,exEntity);
        setListAdapter(entityDataAdapter);
        entityDataAdapter.notifyDataSetChanged();
        inputSearch = (EditText) findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                EntityDataActivity.this.entityDataAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initDb() {
        entityDataDao = new EntityDataDao();
    }

    private List<EntityData> loadEntityDataFromDb(ExEntity exEntity) {
        Log.e(TAG,exEntity.getTableName()+","+exEntity.getKeyField());
        List<EntityData> entityDatas = entityDataDao.loadAll(exEntity);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.prefill_menu, menu);
        try{
            menu.findItem(R.id.sync).setIcon(new IconicsDrawable(this)
                    .icon(FontAwesome.Icon.faw_refresh)
                    .sizeDp(18));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sync:
                try {
                    KengaSyncroniser kengaSyncroniser = new KengaSyncroniser(this);
                    kengaSyncroniser.performDataSync(exEntity);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.entity_data_status_text)).setText("Syncing Data..please wait");
                }
            });
        } else if (syncEvent != null && syncEvent.getStatus().equals(SyncEvent.SYNC_END)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.entity_data_status_text)).setText("finished syncing.All data refreshed");
                }
            });
        }
    }

}
