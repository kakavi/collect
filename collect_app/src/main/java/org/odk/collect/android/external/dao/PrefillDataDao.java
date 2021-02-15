package org.odk.collect.android.external.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.odk.collect.android.external.model.PrefillData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrefillDataDao extends BaseEntityDao {
    private static final String TAG = "PrefillDataDao";
    public static final String DATABASE_TABLE = "prefill_data";
    private Context context;

    //    column Names
    private static final String KEY_ID = "ID";
    private static final String KEY_VALUE = "VALUE";
    private static final String KEY_PREFILL_ID = "FILTER_ID";


    public PrefillDataDao() {
    }

    public PrefillDataDao(Context context) {

    }

    /**
     * Creates the underlying database table.
     */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        try {
            String constraint = ifNotExists ? "IF NOT EXISTS " : "";
            db.execSQL("CREATE TABLE " + constraint + "'" + DATABASE_TABLE + "' (" + //
                    "'" + KEY_ID + "' TEXT PRIMARY KEY NOT NULL UNIQUE ," + // 0: id
                    "'" + KEY_VALUE + "' TEXT ," + // 1: VALUE
                    "'" + KEY_PREFILL_ID + "' TEXT);"); //2: PREFIL ID

        } catch (Exception ex) {
            Log.e("Error: ", ex.getMessage());
        }
    }

    public void insert(PrefillData prefillData, String prefillID) {
        try {
            SQLiteDatabase database = getWritableDB();
            ContentValues values = new ContentValues();
            values.put(KEY_ID, UUID.randomUUID().toString());
            values.put(KEY_VALUE, prefillData.getValue());
            values.put(KEY_PREFILL_ID, prefillID);
            database.insert(DATABASE_TABLE, null, values);
            database.close();
        } catch (Exception ex) {
            Log.e("Error: ", ex.getMessage());
        }
    }

    public List<PrefillData> selectAll(String prefillId) {
        List<PrefillData> prefillDataList = new ArrayList<>();
        try {
            String selectQuery = "SELECT * FROM " + DATABASE_TABLE + " WHERE FILTER_ID = '" + prefillId + "'";
            SQLiteDatabase database = getReadableDB();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    PrefillData prefillData = new PrefillData();
                    prefillData.setValue(cursor.getString(1));
                    prefillData.setParentId(cursor.getString(2));
                    prefillDataList.add(prefillData);
                } while (cursor.moveToNext());
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }
        return prefillDataList;
    }

    public List<PrefillData> selectAll() {
        List<PrefillData> prefillDataList = new ArrayList<>();
        try {
            String selectQuery = "SELECT * FROM " + DATABASE_TABLE;
            SQLiteDatabase database = getReadableDB();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    PrefillData prefillData = new PrefillData();
                    prefillData.setValue(cursor.getString(1));
                    prefillData.setParentId(cursor.getString(2));
                    prefillDataList.add(prefillData);
                } while (cursor.moveToNext());
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }
        return prefillDataList;
    }

    public void deleteAll() {
        try {
            SQLiteDatabase database = getWritableDB();
            database.delete(DATABASE_TABLE, null, null);
            database.close();
        } catch (Exception ex) {
            Log.e("Error: ", ex.getMessage());
        }
    }
}
