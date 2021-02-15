package org.odk.collect.android.activities.entities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.otto.Subscribe;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.EntityListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.events.BusProvider;
import org.odk.collect.android.events.SyncEvent;
import org.odk.collect.android.external.dao.ExEntityDao;
import org.odk.collect.android.external.model.ExEntity;
import org.odk.collect.android.sync.KengaSyncroniser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by victor on 25-Jan-16.
 */
public class EntityListActivity extends ListActivity {
    private static final String TAG = "EntityListActivity";
    private List<ExEntity> exEntityList = new ArrayList<>();
    private EntityListAdapter entityListAdapter;
    private ExEntityDao exEntityDao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entity_list_activity);
        initDb();
        exEntityList = loadEntitiesFromDb();
        if(!exEntityList.isEmpty()){
            ((TextView)findViewById(R.id.entity_status_text)).setText("finished scanning.All data loaded");;
        }
        entityListAdapter = new EntityListAdapter(this,exEntityList);
        setListAdapter(entityListAdapter);
        entityListAdapter.notifyDataSetChanged();

    }

    private void initDb() {
        exEntityDao = new ExEntityDao();
    }

    private List<ExEntity> loadEntitiesFromDb() {
        List<ExEntity> exEntities = exEntityDao.loadAll();
        return exEntities;
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
//        Toast.makeText(getApplicationContext(),"Testing",Toast.LENGTH_LONG).show();
        try{
            Intent intent = new Intent(getApplicationContext(),EntityDataActivity.class);
            ExEntity entity = exEntityList.get(position);
            Bundle bundle = new Bundle();
            bundle.putString("table_name", entity.getTableName());
            bundle.putString("key_field", entity.getKeyField());
            bundle.putString("display_field",entity.getDisplayField());
            intent.putExtras(bundle);
            startActivity(intent);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
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
                    kengaSyncroniser.sync();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Subscribe
    public void onReceiveSyncEvent(SyncEvent syncEvent) {
        Log.e(TAG, "Received Sync Trigger Event " + syncEvent.getStatus());

        if (syncEvent != null && syncEvent.getStatus().equals(SyncEvent.SYNC_START)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.entity_status_text)).setText("Syncing Data..please wait");
                }
            });
        } else if (syncEvent != null && syncEvent.getStatus().equals(SyncEvent.SYNC_END)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.entity_status_text)).setText("finished syncing.All data refreshed");
                }
            });
        }
    }


}
