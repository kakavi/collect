package org.odk.collect.android.external.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.model.ExEntity;
import org.odk.collect.android.external.model.PreFillFilter;
import org.odk.collect.android.external.model.PrefillData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrefillDao extends BaseEntityDao {
    private static final String TAG = "PrefillDao";
    public static final String DATABASE_TABLE = "prefill_filter";
    private Context context;
    private PrefillDataDao prefillDataDao = new PrefillDataDao();


    //    column Names
    private static final String KEY_ID = "ID";
    private static final String KEY_FIELD = "FIELD";
    private static final String KEY_VALUE = "VALUE";
    private static final String KEY_FILTER_NUMBER = "FILTER_NUMBER";
    private static final String KEY_TABLE_NAME = "TABLE_NAME";

    public PrefillDao() {
    }

    PrefillDao(Context context) {
    }

    /**
     * Creates the underlying database table.
     */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        try {
            String constraint = ifNotExists ? "IF NOT EXISTS " : "";
            db.execSQL("CREATE TABLE " + constraint + "'" + DATABASE_TABLE + "' (" + //
                    "'" + KEY_ID + "' TEXT PRIMARY KEY NOT NULL UNIQUE ," + // 0: id
                    "'" + KEY_FIELD + "' TEXT ," + // 1: name
                    "'" + KEY_VALUE + "' TEXT ," + // 2: tableName
                    "'" + KEY_FILTER_NUMBER + "' TEXT," + // 3: displayField
                    "'" + KEY_TABLE_NAME + "' TEXT);"); //4: otherFields

        } catch (Exception ex) {
            Log.e("Error: ", ex.getMessage());
        }
        PrefillDataDao.createTable(db,true);
    }

    public void insert(PreFillFilter preFillFilter) {
        try {
                SQLiteDatabase database = getWritableDB();
            ContentValues values = new ContentValues();
            String uuid = UUID.randomUUID().toString();
            values.put(KEY_ID, uuid);
            values.put(KEY_FIELD, preFillFilter.getField());
            values.put(KEY_VALUE, preFillFilter.getValue());
            values.put(KEY_FILTER_NUMBER, preFillFilter.getFilterNumber());
            values.put(KEY_TABLE_NAME, preFillFilter.getTableName());
            database.insert(DATABASE_TABLE, null, values);
            database.close();
//            add data vals if any
            for (PrefillData prefillData : preFillFilter.getDataList()) {
                prefillDataDao.insert(prefillData, uuid);
            }
        } catch (Exception ex) {
            Log.e("Error: ", ex.getMessage());
        }
    }

    public List<PreFillFilter> findAll() {
        List<PreFillFilter> preFillFilterList = new ArrayList<>();
        try {
            String selectQuery = "SELECT * FROM " + DATABASE_TABLE;
            SQLiteDatabase database = getReadableDB();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    PreFillFilter filter = toPrefillFilter(cursor);
                    preFillFilterList.add(filter);
                } while (cursor.moveToNext());
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();

        }
        return preFillFilterList;
    }

    public List<PreFillFilter> findAllByTableName(String tableName){
        List<PreFillFilter> preFillFilterList = new ArrayList<>();
        try {
            SQLiteDatabase db = getReadableDB();
            Cursor cursor = db.rawQuery("SELECT * FROM "+DATABASE_TABLE+" WHERE "+KEY_TABLE_NAME+"= ?", new String[]{tableName});
            if (cursor.moveToFirst()) {
                do {
                    PreFillFilter filter = toPrefillFilter(cursor);
                    preFillFilterList.add(filter);
                } while (cursor.moveToNext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return preFillFilterList;
    }

    public PreFillFilter get(String id){
        PreFillFilter preFillFilter = null;
        try {
            SQLiteDatabase db = getReadableDB();
            Cursor cursor = db.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_FIELD, KEY_VALUE},
                    "id = ?", new String[]{id}, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }
            preFillFilter = new PreFillFilter();
            preFillFilter.setField(cursor.getString(1));
            preFillFilter.setValue(cursor.getString(2));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return preFillFilter;
    }

    public void deleteAll() {
        try {
            SQLiteDatabase database = getWritableDB();
            database.delete(DATABASE_TABLE, null, null);
            database.close();
            prefillDataDao.deleteAll();
        } catch (Exception ex) {
            Log.e("Error: ", ex.getMessage());
        }
    }

    public void deleteWhereTable(String tableName){
        try {
            SQLiteDatabase database = getWritableDB();
            database.delete(DATABASE_TABLE, KEY_TABLE_NAME + " = ?", new String[]{tableName});
            database.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private PreFillFilter toPrefillFilter(Cursor cursor){
        PreFillFilter preFillFilter = new PreFillFilter();
        String uid = cursor.getString(0);
        preFillFilter.setField(cursor.getString(1));
        preFillFilter.setValue(cursor.getString(2));
        preFillFilter.setFilterNumber(Integer.parseInt(cursor.getString(3)));
        preFillFilter.setTableName(cursor.getString(4));
//      fetch children
        preFillFilter.setDataList(prefillDataDao.selectAll(uid));
        return preFillFilter;
    }

}
