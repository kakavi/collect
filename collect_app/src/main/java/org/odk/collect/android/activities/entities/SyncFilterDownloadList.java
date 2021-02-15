package org.odk.collect.android.activities.entities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import com.squareup.otto.Subscribe;
import org.apache.commons.lang3.StringUtils;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormListActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.events.BusProvider;
import org.odk.collect.android.events.SyncEvent;
import org.odk.collect.android.external.model.EntityData;
import org.odk.collect.android.external.model.ExEntity;
import org.odk.collect.android.external.model.PreFillFilter;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.sync.KengaSyncroniser;
import org.odk.collect.android.utilities.AuthDialogUtility;
import org.odk.collect.android.utilities.ExEntityUtils;
import org.odk.collect.android.utilities.ThemeUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kakavi on 5/4/2016.
 */
public class SyncFilterDownloadList extends FormListActivity implements AuthDialogUtility.AuthDialogUtilityResultListener, AdapterView.OnItemClickListener {
    private static final String FORM_DOWNLOAD_LIST_SORTING_ORDER = "formDownloadListSortingOrder";
    private static final String TAG = "SyncFilterDownloadList";
    private ProgressDialog progressDialog;
    private Button syncButton;
    private Button downloadDataButton;
    private Button getSelectedButton;
    private Button toggleButton;
    private String mAlertMsg;
    private KengaSyncroniser restClient;
    private List<PreFillFilter> prefillFilters = new ArrayList<>();
    private ArrayAdapter filterListAdapter;
    private PreFillFilter currentFilter;
    private boolean toggleAll = false;
    //    this listview can either be showing entities or filters.
    private boolean isInfilterView = false;
    private ListView listView;
    protected Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeUtils(this).getAppTheme());
        setContentView(R.layout.form_download_list);
        super.onCreate(savedInstanceState);
        initToolbar();
        restClient = new KengaSyncroniser(this);
        getSupportActionBar().setTitle("Sync Prefill Data");
        listView = (ListView)findViewById(android.R.id.list);
        mAlertMsg = getString(R.string.please_wait);
        createProgressDialogue(mAlertMsg);
        getSelectedButton = (Button) findViewById(R.id.add_button);
        getSelectedButton.setText(R.string.more_filters);
        getSelectedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSyncEvent();
                if (isInfilterView) {
                    getSelectedFilters();
                    downloadFilters(prefillFilters);
                } else {
//                    after selecting entity
                    try{
                        ExEntity selectedEntity = (ExEntity) listView.getItemAtPosition(listView.getCheckedItemPosition());
                        restClient.insertEntity(selectedEntity);
                        prefillFilters.add(new PreFillFilter("", "", 0, null, selectedEntity.getTableName()));
                        downloadFilters(prefillFilters);
                        isInfilterView = true;
                        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    }catch (Exception ex){
                        Log.e(TAG,ex.getMessage());
                        stopSyncEvent();
                    }
                }
            }
        });

        downloadDataButton = (Button) findViewById(R.id.refresh_button);
        downloadDataButton.setText("Download Data");
        downloadDataButton.setEnabled(false);
        downloadDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSelectedFilters();
                downloadPrefillData();
            }
        });

        toggleButton = (Button) findViewById(R.id.toggle_button);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSwitch();
                for (int i = 0; i < listView.getCount(); i++) {
                    if (toggleAll) {
                        listView.setItemChecked(i, true);
                    } else {
                        listView.setItemChecked(i, false);
                    }
                }
            }
        });

        // need white background before load
        listView.setBackgroundColor(Color.WHITE);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemsCanFocus(false);
//        initToolbar();
        sortingOptions = new int[0];
    }


    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
    }


    private void downloadPrefillData() {
        startSyncEvent();
        if (prefillFilters.isEmpty()) {
            stopSyncEvent();
            ExEntityUtils.alertStayOnCurrent(SyncFilterDownloadList.this, "Info", "No Filters downloaded,Please try again");
        }
        final ExEntity exEntity = restClient.findEntityByTableName(prefillFilters.get(0).getTableName());

        if(exEntity == null){
            stopSyncEvent();
            ExEntityUtils.alertStayOnCurrent(SyncFilterDownloadList.this, "Info", "Unable to download data Please try again");
            return;
        }
        exEntity.setPrefillFilterList(prefillFilters);
        restClient.saveFilters(prefillFilters,exEntity.getTableName());
        Call<List<EntityData>> call = restClient.getEntityRestClient().downloadFilteredEntityDataMap(restClient.getPath(), exEntity);
        call.enqueue(new Callback<List<EntityData>>() {
            @Override
            public void onResponse(Call<List<EntityData>> call, Response<List<EntityData>> response) {
                try {
                    List<EntityData> entityDataList = response.body();
                    if (entityDataList != null) {
                        restClient.insertEntityDataTx(entityDataList, exEntity);
                    }
                    stopSyncEvent();
                    ExEntityUtils.alert(SyncFilterDownloadList.this, "Download Result", "Prefill Data Successfully Downloaded");
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }

            @Override
            public void onFailure(Call<List<EntityData>> call, Throwable t) {
                stopSyncEvent();
                if (t instanceof IOException) {
                    ExEntityUtils.alertStayOnCurrent(SyncFilterDownloadList.this, "Network Failure", "Lost Connection Please try again");
                }

            }
        });
    }

    private void getSelectedFilters() {
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        ArrayList<String> selectedItems = new ArrayList<>();
        for (int i = 0; i < checked.size(); i++) {
            int position = checked.keyAt(i);
            if (checked.valueAt(i)) {
                selectedItems.add(filterListAdapter.getItem(position).toString());
            }
        }
        if (!selectedItems.isEmpty()) {
            String values = TextUtils.join(",", selectedItems);
            currentFilter.setValue(values);
            prefillFilters.add(currentFilter);
        } else {
            Toast.makeText(this, "Please Select atleast one filter", Toast.LENGTH_LONG).show();
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (restClient.platformSettingsEntered()) {
                        startSyncEvent();
                        downloadPreFillEntities();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                ExEntityUtils.alert(SyncFilterDownloadList.this,"Authentication needed","Please set your username and password under platform settings");
                                authPromptDialog().show();
                            }
                        });
                    }
                }
            }).start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void updateAdapter() {

    }

    @Override
    protected String getSortingOrderKey() {
        return FORM_DOWNLOAD_LIST_SORTING_ORDER;
    }


    private ProgressDialog createProgressDialogue(String alertMessage) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.downloading_data));
        progressDialog.setMessage(alertMessage);
        progressDialog.setIcon(android.R.drawable.ic_dialog_info);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return progressDialog;
    }

    private void downloadFilters(final List<PreFillFilter> prefillFilters) {
        Call<PreFillFilter> call = restClient.getEntityRestClient().getPrefillFilters(restClient.getPath(), prefillFilters);
        call.enqueue(new Callback<PreFillFilter>() {
            @Override
            public void onResponse(Call<PreFillFilter> call, Response<PreFillFilter> response) {
                PreFillFilter prefillFilter = response.body();
                if (prefillFilter != null) {
                    currentFilter = prefillFilter;
                    filterListAdapter = new ArrayAdapter(SyncFilterDownloadList.this, android.R.layout.simple_list_item_multiple_choice, currentFilter.getDataList());
                    listView.setAdapter(filterListAdapter);
                    getSupportActionBar().setTitle(currentFilter.getField());
                    if ((prefillFilters.size()) == currentFilter.getFilterNumber()) {
                        getSelectedButton.setEnabled(false);
                        downloadDataButton.setEnabled(true);
                        getSelectedButton.setText(R.string.more_filters);
                    }
                } else {
                    Toast.makeText(SyncFilterDownloadList.this, "There are no Filters to download", Toast.LENGTH_LONG).show();
                }
                stopSyncEvent();
            }

            @Override
            public void onFailure(Call<PreFillFilter> call, Throwable t) {
                Log.e(TAG, t.getMessage() + "");
                t.printStackTrace();
                stopSyncEvent();
                if (t instanceof IOException) {
                    ExEntityUtils.alertStayOnCurrent(SyncFilterDownloadList.this, "Network Failure", "Lost Connection Please try again");
                }
            }
        });
    }

    private void downloadPreFillEntities() {
        try {
            Call<List<ExEntity>> call = restClient.getEntityRestClient().getPreloadEntities(restClient.getPath());
            call.enqueue(new Callback<List<ExEntity>>() {
                @Override
                public void onResponse(Call<List<ExEntity>> call, Response<List<ExEntity>> response) {
                    Log.e(TAG, "Response Code:" + response.code());
                    if (response.code() == 401 || response.code() == 404) {
                        authPromptDialog().show();
                    } else {
                        List<ExEntity> odxEntityList = response.body();
                        if (odxEntityList != null) {
                            Log.e(TAG, "new preload entities downloaded:" + odxEntityList.size());




                            List<ExEntity> odxEntityListTest = new ArrayList<ExEntity>();
                            File[] formDefs = getFormDetails();
                            System.out.println(":::::::::::COMPARE::::::::::::::");
                            for (File file : formDefs) {
                                String fName = "";
                                if (shouldAddFormFile(file.getName())) {
                                    String s = file.getName();
                                    fName = s.substring(0, s.length() - 4);

                                    for(int i=0;i<odxEntityList.size();i++){
                                        String sName = odxEntityList.get(i).getName();
                                        double similarity = compareStrings(fName, sName)*100;
                                        System.out.println(fName+" ::: "+sName+" ::: "+similarity+"% similarity");
                                        if(similarity>=50){
                                            odxEntityListTest.add(odxEntityList.get(i));
                                        }
                                    }
                                }
                            }



                            restClient.cleanDirtyEntities(odxEntityListTest);
                            filterListAdapter = new ArrayAdapter(SyncFilterDownloadList.this, android.R.layout.simple_list_item_multiple_choice, odxEntityListTest);
                            listView.setAdapter(filterListAdapter);
                            filterListAdapter.notifyDataSetChanged();
                            getSupportActionBar().setTitle("Entities");
                            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                            getSelectedButton.setEnabled(true);
                            getSelectedButton.setText(R.string.get_filters);
                            downloadDataButton.setEnabled(false);
                        } else {
                            ExEntityUtils.alertStayOnCurrent(SyncFilterDownloadList.this, "Network Failure", "Lost Connection Please try again");
                        }
                    }
                    stopSyncEvent();
                }

                @Override
                public void onFailure(Call<List<ExEntity>> call, Throwable t) {
//                    show entities in db
                    if (t instanceof IOException) {
                        ExEntityUtils.alertStayOnCurrent(SyncFilterDownloadList.this, "Network Failure", "Lost Connection Please try again");
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Subscribe
    public void onReceiveSyncEvent(SyncEvent syncEvent) {
        Log.e(TAG, "Received Sync Trigger Event " + syncEvent.getStatus());

        if (syncEvent != null && syncEvent.getStatus().equals(SyncEvent.SYNC_START)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
        } else if (syncEvent != null && syncEvent.getStatus().equals(SyncEvent.SYNC_END)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            });
        }
    }

    File[] getFormDetails(){
        File[] formDefs = null;

        StoragePathProvider storagePathProvider = new StoragePathProvider();
        File formDir = new File(storagePathProvider.getDirPath(StorageSubdirectory.FORMS));
        if (formDir.exists() && formDir.isDirectory()) {
            // Get all the files in the /odk/foms directory
            formDefs = formDir.listFiles();
        }
        return formDefs;
    }

    public double compareStrings(String stringA, String stringB) {
        return StringUtils.getJaroWinklerDistance(stringA, stringB);
    }

    public static boolean shouldAddFormFile(String fileName) {
        // discard files beginning with "."
        // discard files not ending with ".xml" or ".xhtml"
        boolean ignoredFile = fileName.startsWith(".");
        boolean xmlFile = fileName.endsWith(".xml");
        boolean xhtmlFile = fileName.endsWith(".xhtml");
        return !ignoredFile && (xmlFile || xhtmlFile);
    }

    private AlertDialog authPromptDialog() {
        return new  AuthDialogUtility().createDialog(this,this ,null);
    }


    private void toggleSwitch() {
        if (toggleAll) {
            toggleAll = false;
        } else {
            toggleAll = true;
        }
    }

    private void startSyncEvent() {
        BusProvider.getInstance().post(new SyncEvent(SyncEvent.SYNC_START));
    }

    private void stopSyncEvent() {
        BusProvider.getInstance().post(new SyncEvent(SyncEvent.SYNC_END));
    }
    @Override
    public void updatedCredentials() {
        downloadPreFillEntities();
    }

    @Override
    public void cancelledUpdatingCredentials() {
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
