package org.odk.collect.android.external.dao;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.odk.collect.android.application.Collect;

public class BaseEntityDao extends SQLiteOpenHelper {
    public static final String TAG = "BaseEntityDao";
    public static final String DATABASE_NAME = "external_entities.db";
    public static final int DATABASE_VERSION = 4;

    BaseEntityDao() {
        super(Collect.getInstance(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    BaseEntityDao(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ExEntityDao.createTable(db,true);
        onUpgrade(db,db.getVersion(),db.getVersion());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3)
            ExEntityDao.upgradeToVersion2(db);
        if(oldVersion < 4)
            PrefillDao.createTable(db, true);
            PrefillDataDao.createTable(db,true);
    }

    public SQLiteDatabase getWritableDB() throws SQLException {
        SQLiteDatabase database = null;
        try {
            database = getWritableDatabase();
            Log.e(TAG,"DB Version:"+database.getVersion());
//            createTable(database, true);
        } catch (SQLiteException e) {
            Log.e("Error: ", e.getMessage());
        }
        return database;
    }

    public SQLiteDatabase getReadableDB() {
        SQLiteDatabase database = null;
        try {
            database = getReadableDatabase();
        } catch (SQLiteException e) {
            Log.e("Error: ", e.getMessage());
            e.printStackTrace();
        }
        return database;
    }


}
