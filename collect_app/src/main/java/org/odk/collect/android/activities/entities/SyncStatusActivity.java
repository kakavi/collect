package org.odk.collect.android.activities.entities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.otto.Subscribe;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.events.BusProvider;
import org.odk.collect.android.events.SyncEvent;
import org.odk.collect.android.external.model.ExEntity;
import org.odk.collect.android.external.model.PreFillFilter;
import org.odk.collect.android.sync.KengaSyncroniser;
import org.odk.collect.android.sync.PrefillSyncJob;

import java.util.List;

public class SyncStatusActivity extends CollectAbstractActivity {

    private static String TAG = "SyncStatusActivity";
    private KengaSyncroniser kengaSyncroniser;
    private LinearLayout layout = null;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sync_status_activity);
        initToolbar();
        kengaSyncroniser = new KengaSyncroniser(this);
        init();

    }

    private void init() {
        List<PreFillFilter> preFillFilterList = kengaSyncroniser.loadPrefillFilters();
        TextView lastSyncLbl = findViewById(R.id.yourlastsyncLbl);
        String lastSyncTxt = "Your Last Sync was on:" + PrefillSyncJob.getLastSync();
        lastSyncLbl.setText(lastSyncTxt);
        TextView nextSyncLbl = findViewById(R.id.yournextsynclbl);
        String nextSyncTxt = "Your next sync will be:" + PrefillSyncJob.getNextSync();
        nextSyncLbl.setText(nextSyncTxt);
        layout = findViewById(R.id.filterLayout);
        addItemsToLayout(layout, preFillFilterList);
    }

    private void destroy(){
        layout.removeAllViewsInLayout();
        TextView lastSyncLbl = findViewById(R.id.yourlastsyncLbl);
        lastSyncLbl.setText("");
        TextView nextSyncLbl = findViewById(R.id.yournextsynclbl);
        nextSyncLbl.setText("");
    }

    private void addItemsToLayout(LinearLayout layout, List<PreFillFilter> preFillFilterList) {
        List<ExEntity> exEntities = kengaSyncroniser.loadEntitiesFromDb();
        for (ExEntity entity : exEntities) {
            TextView entityTxt = createTextView("Entity:" + entity.getName(), this);
            entityTxt.setPadding(8, 8, 8, 8);
            entityTxt.setGravity(Gravity.CENTER);
            entityTxt.setTypeface(entityTxt.getTypeface(), Typeface.BOLD);
            entityTxt.setTextSize(17);
            layout.addView(entityTxt);


            LinearLayout filterWrapperLayout = createFilterWrapperLayout();
            layout.addView(filterWrapperLayout);
            for (PreFillFilter filter : kengaSyncroniser.loadPrefillFilters(entity.getTableName())) {
                if (filter.getField().equals("")) {
                    continue;
                }
                System.out.println(preFillFilterList.size());
                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
                LinearLayout filterLayt = new LinearLayout(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                params.gravity = Gravity.CENTER;
                params.leftMargin = 40;
                filterLayt.setLayoutParams(params);
                filterLayt.setOrientation(LinearLayout.VERTICAL);
                addDataListsToLayout(filterLayt, filter);
                filterWrapperLayout.addView(filterLayt);
            }
        }

    }

    private void addDataListsToLayout(LinearLayout filterLayt, PreFillFilter preFillFilter) {
        TextView textView = createTextView("  " + preFillFilter.getField().toUpperCase() + ": ", this);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        filterLayt.addView(textView);
        String[] vals = preFillFilter.getValue().split(",");
        for (String option : vals) {
            TextView dataTxtView = createTextView(option, this);
            filterLayt.addView(dataTxtView);
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

    protected void onResume() {
        super.onResume();
//      register for db change event,sync event
        destroy();
        init();
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
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.sync_status_menu, menu);
        menu.findItem(R.id.sync_status_sync).setIcon(new IconicsDrawable(getApplicationContext()).icon(FontAwesome.Icon.faw_refresh).sizeDp(20));
        menu.findItem(R.id.sync_status_change_filter).setIcon(new IconicsDrawable(getApplicationContext()).icon(FontAwesome.Icon.faw_filter).sizeDp(20));
        menu.findItem(R.id.sync_status_view_data).setIcon(new IconicsDrawable(getApplicationContext()).icon(FontAwesome.Icon.faw_list).sizeDp(20));
        return true;
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new Toolbar.LayoutParams(Gravity.END));
        progressBar.setIndeterminate(true);
        toolbar.addView(progressBar);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private TextView createTextView(String text, Context context) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(Color.WHITE);
        return textView;
    }

    private LinearLayout createFilterWrapperLayout() {
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        params.leftMargin = 40;
        params.bottomMargin = 30;
        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        return linearLayout;
    }

    private LinearLayout addEntityLabel(LinearLayout linearLayout, String lable) {
        TextView textView = createTextView("Entity:" + lable, this);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        linearLayout.addView(textView);
        return linearLayout;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sync_status_sync:
                BusProvider.getInstance().post(new SyncEvent(SyncEvent.SYNC_START));
                PrefillSyncJob.runJobImmediately();
                return true;
            case R.id.sync_status_change_filter:
                Intent i = new Intent(getApplicationContext(),
                        SyncFilterDownloadList.class);
                startActivity(i);
                return true;
            case R.id.sync_status_view_data:
                Intent intent = new Intent(getApplicationContext(), EntityListActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Subscribe
    public void onReceiveSyncEvent(SyncEvent syncEvent) {
        try{
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
            }
        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }


}
