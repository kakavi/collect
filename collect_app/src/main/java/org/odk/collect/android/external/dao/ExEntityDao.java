package org.odk.collect.android.external.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.model.ExEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by victor on 20-Jan-16.
 */
public class ExEntityDao extends BaseEntityDao {
    public static final String TAG = "ExEntityDao";
    public static final String DATABASE_TABLE = "entities";
    private boolean mIsOpen = false;
    private Context context;

    //    column Names
    private static final String KEY_ID = "ID";
    private static final String KEY_NAME = "NAME";
    private static final String KEY_TABLE_NAME = "TABLE_NAME";
    private static final String KEY_DISPLAY_FIELD = "DISPLAY_FIELD";
    private static final String KEY_KEY_FIELD = "KEY_FIELD";
    private static final String KEY_KEY_FILTER_FIELD = "KEY_FILTER_FIELD";
    private static final String KEY_KEY_FILTER_VALUES = "KEY_FILTER_VALUES";
    private static final String KEY_KEY_FILTER_JOINER = "KEY_FILTER_JOINER";
    private static final String KEY_OTHER_FIELDS = "OTHER_FIELDS";

    public ExEntityDao() {
        this.context = Collect.getInstance();
    }

    public ExEntityDao(Context context){
        this.context = context;
    }

    public boolean isOpen() {
        return mIsOpen;
    }

    public void insert(ExEntity exEntity) {
        try {
            SQLiteDatabase database = getWritableDB();
            ContentValues values = new ContentValues();
            values.put(KEY_ID, exEntity.getId());
            values.put(KEY_NAME, exEntity.getName());
            values.put(KEY_TABLE_NAME, exEntity.getTableName());
            values.put(KEY_DISPLAY_FIELD, exEntity.getDisplayField());
            values.put(KEY_KEY_FIELD, exEntity.getKeyField());
            values.put(KEY_KEY_FILTER_FIELD, exEntity.getFilterFld());
            values.put(KEY_KEY_FILTER_VALUES, exEntity.getFilterValues());
            values.put(KEY_KEY_FILTER_JOINER, exEntity.getFilterJoiner());
            values.put(KEY_OTHER_FIELDS, TextUtils.join(",", exEntity.getOtherFields()));
            database.insert(DATABASE_TABLE, null, values);
            database.close();
        } catch (Exception ex) {
            Log.e("Error: ", ex.getMessage());
        }
    }

    public ExEntity getExEntity(String field, String value) {
        ExEntity exEntity = null;
        try {
            SQLiteDatabase db = getWritableDB();
            Cursor cursor = db.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_NAME, KEY_TABLE_NAME, KEY_DISPLAY_FIELD,
                            KEY_KEY_FIELD, KEY_KEY_FILTER_FIELD, KEY_KEY_FILTER_VALUES, KEY_KEY_FILTER_JOINER, KEY_OTHER_FIELDS},
                    field + "= ?", new String[]{value}, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }
            exEntity = new ExEntity(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getString(5),
                    cursor.getString(6), cursor.getString(7), cursor.getString(8));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return exEntity;
    }

    public List<ExEntity> loadAll() {
        List<ExEntity> exEntities = new ArrayList<>();
        try {
            String selectQuery = "SELECT * FROM " + DATABASE_TABLE;
            SQLiteDatabase database = getReadableDB();
            Cursor cursor = database.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    ExEntity exEntity = new ExEntity();
                    exEntity.setId(cursor.getString(0));
                    exEntity.setName(cursor.getString(1));
                    exEntity.setTableName(cursor.getString(2));
                    exEntity.setDisplayField(cursor.getString(3));
                    exEntity.setKeyField(cursor.getString(4));
                    exEntity.setFilterFld(cursor.getString(5));
                    exEntity.setFilterValues(cursor.getString(6));
                    exEntity.setFilterJoiner(cursor.getString(7));
                    String otherFields = cursor.getString(8);
                    if (otherFields != null) {
                        exEntity.setOtherFields(new ArrayList<>(Arrays.asList(otherFields.split(","))));
                    }

                    exEntities.add(exEntity);
                } while (cursor.moveToNext());
            }
            database.close();
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
        return exEntities;
    }

    public int countExEntities() {
        String countQuery = "SELECT * FROM " + DATABASE_TABLE;
        SQLiteDatabase database = getReadableDB();
        Cursor cursor = database.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int updateExEntity(ExEntity exEntity) {
        try {
            SQLiteDatabase database = getWritableDB();
            ContentValues values = new ContentValues();
            values.put(KEY_ID, exEntity.getId());
            values.put(KEY_NAME, exEntity.getName());
            values.put(KEY_TABLE_NAME, exEntity.getTableName());
            values.put(KEY_DISPLAY_FIELD, exEntity.getDisplayField());
            values.put(KEY_KEY_FIELD, exEntity.getKeyField());
            values.put(KEY_KEY_FILTER_FIELD, exEntity.getFilterFld());
            values.put(KEY_KEY_FILTER_VALUES, exEntity.getFilterValues());
            values.put(KEY_KEY_FILTER_JOINER, exEntity.getFilterJoiner());
            values.put(KEY_OTHER_FIELDS, TextUtils.join(",", exEntity.getOtherFields()));

            return database.update(DATABASE_TABLE, values, KEY_TABLE_NAME + "= ?", new String[]{exEntity.getTableName()});
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
        return -1;
    }

    public void deleteExEntity(ExEntity exEntity) {
        try {
            SQLiteDatabase database = getWritableDB();
            database.delete(DATABASE_TABLE, KEY_TABLE_NAME + " = ?", new String[]{exEntity.getTableName()});
            database.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    /**
     * Creates the underlying database table.
     */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        try {
            String constraint = ifNotExists ? "IF NOT EXISTS " : "";
            db.execSQL("CREATE TABLE " + constraint + "'" + DATABASE_TABLE + "' (" + //
                    "'" + KEY_ID + "' TEXT PRIMARY KEY NOT NULL UNIQUE ," + // 0: id
                    "'" + KEY_NAME + "' TEXT NOT NULL UNIQUE ," + // 1: name
                    "'" + KEY_TABLE_NAME + "' TEXT NOT NULL UNIQUE ," + // 2: tableName
                    "'" + KEY_DISPLAY_FIELD + "' TEXT," + // 3: displayField
                    "'" + KEY_KEY_FIELD + "' TEXT," + // 4: keyField
                    "'" + KEY_KEY_FILTER_FIELD + "' TEXT," + // 5: filterField
                    "'" + KEY_KEY_FILTER_VALUES + "' TEXT," + // 6: filterValuesField
                    "'" + KEY_KEY_FILTER_JOINER + "' TEXT);"); //8: otherFields

        } catch (Exception ex) {
            Log.e("Error: ", ex.getMessage());
        }
    }

    public static void upgradeToVersion2(SQLiteDatabase db){
        db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN " + KEY_OTHER_FIELDS + " TEXT");
    }

    /**
     * Drops the underlying database table.
     */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'" + DATABASE_TABLE + "'";
        db.execSQL(sql);
    }


}
