package org.odk.collect.android.utilities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import org.odk.collect.android.external.model.ExEntity;

import java.util.UUID;

/**
 * Created by victor on 25-Jan-16.
 */
public class ExEntityUtils {
    private static final String TAG = "ExEntityUtils";
    public static final String AUTHORITY = "org.odk.collect.android.provider.odk.entities";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "odk.org";
    // The account name
    public static final String ACCOUNT = "odkaccount";
    ContentResolver contentResolver;
    //    sync interval set to one hour
//    public static int SYNC_INTERVAL=3600;
    public static int SYNC_INTERVAL = 180;


    public static String toTitleCase(String str) {

        if (str == null) {
            return null;
        }

        boolean space = true;
        StringBuilder builder = new StringBuilder(str);
        final int len = builder.length();

        for (int i = 0; i < len; ++i) {
            char c = builder.charAt(i);
            if (space) {
                if (!Character.isWhitespace(c)) {
                    // Convert to title case and switch out of whitespace mode.
                    builder.setCharAt(i, Character.toTitleCase(c));
                    space = false;
                }
            } else if (Character.isWhitespace(c)) {
                space = true;
            } else {
                builder.setCharAt(i, Character.toLowerCase(c));
            }
        }

        return builder.toString();
    }

    public void triggerSync(Context context) {
        Account syncAccount = createSyncAccount(context);
        contentResolver = context.getContentResolver();
        contentResolver.setSyncAutomatically(syncAccount, AUTHORITY, true);
        contentResolver.addPeriodicSync(syncAccount, AUTHORITY, Bundle.EMPTY, SYNC_INTERVAL);
    }


    private Account createSyncAccount(Context context) {
        Account account = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        boolean addAccountExplicitly = accountManager.addAccountExplicitly(account, null, null);
        if (addAccountExplicitly) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            Log.e(TAG, "Error accured while creating account");
        }
        return account;
    }

    public static ExEntity reconstructEntity(String tableName, String displayField, String keyField) {
        ExEntity entity = new ExEntity(UUID.randomUUID().toString());
        entity.setTableName(tableName);
        entity.setDisplayField(displayField);
        entity.setKeyField(keyField);
        return entity;
    }

    public void triggerMnualSync(Context context) {
        Account syncAccount = createSyncAccount(context);
        contentResolver = context.getContentResolver();
        // Pass the settings flags by inserting them in a bundle
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        contentResolver.requestSync(syncAccount, AUTHORITY, settingsBundle);
    }

    public static void alert(final Activity activity, String title, String error) {
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE: // ok
                        activity.finish();
                        break;
                }
            }
        };
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(error)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("ok", quitListener)
                .show();
    }

    public static void alertStayOnCurrent(Activity activity, String title, String error) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(error)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("ok", null)
                .show();
    }

    public static String generateMisUrlFromKenga(String kengaUrl){
        if(null == kengaUrl){
            return "";
        }
        kengaUrl = kengaUrl.replace("mpsubmit/odk","").replace("8443","8080");
        Uri uri = Uri.parse(kengaUrl);
        String context = uri.getPath().replaceAll("/","");
        String miscontext = replaceLast(context,"data","-mis");
        return replaceLast(kengaUrl,context,miscontext);
    }

    public static String replaceLast(String string, String toReplace, String replacement)
    {
        int index = string.lastIndexOf(toReplace);
        if (index == -1)
            return string;
        return string.substring(0, index) + replacement + string.substring(index+toReplace.length());
    }

    public static boolean isValidUrl(String url){
        return Patterns.WEB_URL.matcher(url).matches();
    }

    public static String cleanColumnName(String columnName){
        columnName = columnName.replaceAll("\\s","");
        return columnName;
    }

    public static String getColumnLabel(String columnName){
        columnName = columnName.replaceAll("_"," ");
        return columnName;
    }


}
