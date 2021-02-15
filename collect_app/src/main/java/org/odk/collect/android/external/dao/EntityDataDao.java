package org.odk.collect.android.external.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Log;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.model.EntityData;
import org.odk.collect.android.external.model.ExEntity;
import org.odk.collect.android.external.model.TableAttr;

import java.util.*;

/**
 * Created by victor on 20-Jan-16.
 */
public class EntityDataDao extends BaseEntityDao {
    public static final String TAG = "EntityDataDao";
    private Context context;
    private static final String DATABASE_TABLE = "entities";

    public EntityDataDao() {
        this.context = Collect.getInstance();
    }

    public EntityDataDao(Context context) {
        this.context = context;
    }


    public void createTable(String tableName, List<TableAttr> tableAttrs) {
        SQLiteDatabase db = getWritableDB();
        String attrQuery = "";
        for (TableAttr attr : tableAttrs) {
            if (attrQuery.equals("")) {
                attrQuery += " '" + attr.getColumn() + "' " + attr.getDatatype() + (attr.isPrimaryKey() ? " PRIMARY KEY NOT NULL UNIQUE " : "");
            } else {
                attrQuery += ", '" + attr.getColumn() + "' " + attr.getDatatype() + (attr.isPrimaryKey() ? " PRIMARY KEY NOT NULL UNIQUE " : "");
            }
        }

        String query = "CREATE TABLE IF NOT EXISTS  '" + tableName + "' (" + attrQuery + ");";
        Log.e(TAG, "Query:" + query);
        db.execSQL(query);
        db.close();
    }

    public void deleteTable(String tableName) {
        try {
            SQLiteDatabase db = getWritableDB();
            String dropQuery = "DROP TABLE IF EXISTS " + tableName;
            db.execSQL(dropQuery);
            db.close();
        } catch (Exception ex) {
            Log.e("Error: ", ex.getMessage());
        }
    }

    public void insertIntoEntityDatatable(String tableName, EntityData entityData) {
        try {
            SQLiteDatabase db = getWritableDB();
            String insertQuery = "INSERT INTO " + tableName + " VALUES ('" + entityData.getKeyField() + "','" + entityData.getDisplayField() + "')";
            Log.e(TAG, insertQuery);
            db.execSQL(insertQuery);
            db.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void insertEntityDataInTX(String tableName, List<EntityData> entityDataList) {
        try {
            int colCount = columnCount(tableName);
            String sql = generateInsertTemplate(tableName, colCount);
            SQLiteDatabase db = getWritableDB();
            db.beginTransactionNonExclusive();
            SQLiteStatement statement = db.compileStatement(sql);
            List<String> columnNames = getColumnNames(tableName);
            for (EntityData entityData : entityDataList) {
                try {
                    statement.bindString(1, entityData.getKeyField());
                    int idx = 2;
//                    add other fields
                    for (Map.Entry<String, String> entry : entityData.getOtherFields().entrySet()) {
                        Log.e(TAG, entry.getKey() + ":" + entry.getValue());
                        statement.bindString(getColumnindex(entry.getKey(), columnNames) + 1, entry.getValue() + "");
                        idx += 1;
                    }
                    statement.execute();
                    statement.clearBindings();
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int getColumnindex(String key, List<String> columnNames) {
        return columnNames.indexOf(key);
    }

    public String generateInsertTemplate(String tableName, int colCount) {
        if (colCount <= 0) return "";
        List<String> values = new ArrayList<>();
        for (int i = 0; i < colCount; ++i) {
            values.add("?");
        }
        String temp = "INSERT INTO " + tableName + " VALUES (" + TextUtils.join(",", values) + ")";
        return temp;
    }

    public List<EntityData> loadAll(ExEntity exEntity) {
        SQLiteDatabase db = getReadableDB();
        List<EntityData> entityDataList = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " + exEntity.getTableName(), null);
            if (cursor.moveToFirst()) {
                Log.e(TAG, cursor.getCount() + " Record(s) found");
                do {
                    EntityData entityData = new EntityData();
                    entityData.setKeyField(cursor.getString(cursor.getColumnIndexOrThrow(exEntity.getKeyField())));

                    Map<String, String> otherFlds = new HashMap<>();
                    for (String field : cursor.getColumnNames()) {
                        String attr = cursor.getString(cursor.getColumnIndexOrThrow(field));
                        if (attr != null) {
                            otherFlds.put(field, attr);
                        }
                    }
                    entityData.setOtherFields(otherFlds);
                    entityDataList.add(entityData);
                } while (cursor.moveToNext());
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return entityDataList;

    }

    public Cursor loadAllIntoCursor(String tableName, String keyField, String displayField) {
        SQLiteDatabase db = getReadableDB();
        String query = "SELECT * FROM " + tableName;
        Log.e(TAG, query);
        Cursor cursor = db.query(tableName, new String[]{keyField, displayField}, null, null, null, null, displayField + " ASC");
        return cursor;

    }

    public boolean deleteAllData(String tableName) {
//        logTables();
        SQLiteDatabase db = getWritableDB();
        int deleted = 0;
        Log.e(TAG, "deleting table " + tableName);
        deleted = db.delete(tableName, null, null);
        Log.e(TAG, Integer.toString(deleted));
        db.close();
        return deleted > 0;
    }

    public void logTables() {
        for (String table : showAllTables()) {
            Log.e("EntityDataDao[Table]", table);
        }
    }

    public ArrayList<String> showAllTables() {
        ArrayList tables = new ArrayList();
        SQLiteDatabase db = getReadableDB();
        try {
            Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    tables.add(c.getString(0));
                    c.moveToNext();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        db.close();
        return tables;
    }

    public void deleteAllTables() {
        SQLiteDatabase db = getWritableDB();
        try {
            Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            List<String> tables = new ArrayList<>();

            while (c.moveToNext()) {
                tables.add(c.getString(0));
            }

            for (String table : tables) {
                String dropQuery = "DROP TABLE IF EXISTS " + table;
                db.execSQL(dropQuery);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        db.close();
    }

    public List<List<String>> selectAll(String tableName, boolean returnHeaders) {
        List<List<String>> data = new ArrayList<>();
        SQLiteDatabase db = getReadableDB();
        try {
            Cursor c = db.rawQuery("SELECT * FROM " + tableName, null);
            if (returnHeaders) {
                data.add(Arrays.asList(c.getColumnNames()));
            }
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    List<String> row = new ArrayList<>();
                    for (int i = 0; i < c.getColumnCount(); ++i) {
                        row.add(c.getString(i));
                    }
                    data.add(row);
                    c.moveToNext();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        db.close();
        return data;
    }

    public List<String> getColumnNames(String tableName) {
        SQLiteDatabase db = getReadableDB();
        try {
            Cursor c = db.rawQuery("SELECT * FROM " + tableName, null);
            if (c != null) {
                return Arrays.asList(c.getColumnNames());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        db.close();
        return Collections.EMPTY_LIST;
    }

    public int columnCount(String tableName) {
        int result = 0;
        SQLiteDatabase db = getReadableDB();
        Cursor cursor = null;
        try {
            cursor = db.query(tableName, null, null, null, null, null, null);
            result = cursor.getColumnCount();
            if (result < 0) {
                result = 0;
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        db.close();
        return result;
    }

    public boolean createDataTableForEntity(ExEntity exEntity) {
        try {
            List<TableAttr> tableAttrs = new ArrayList<>();
            tableAttrs.add(new TableAttr(exEntity.getKeyField(), true, "TEXT"));
            List<String> displayFields = Arrays.asList(exEntity.getDisplayField().split(EntityData.DISPLAY_FIELD_SEPERATORE));
            displayFields = removeIdFieldIfAny(exEntity,displayFields);
            for (String fld : displayFields) {
                    tableAttrs.add(new TableAttr(fld, false, "TEXT"));
            }
            for (String str : exEntity.getOtherFields()) {
                tableAttrs.add(new TableAttr(str, false, "TEXT"));
            }
            createTable(exEntity.getTableName(), tableAttrs);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "Error creating data table " + ex.getMessage());
        }
        return false;
    }

    private List<String> removeIdFieldIfAny(ExEntity exEntity, List<String> fields) {
        List<String> result = new ArrayList<>();
        for (String fld:fields) {
            if (!fld.equals(exEntity.getKeyField())) {
                result.add(fld);
            }
        }
        return result;
    }

}
