package com.cookoo.life.domain;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created By: travis.roberson
 * Date: 12/16/13
 * Time: 3:51 PM
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "statusBarNotification";
    private static final String TABLE_STATUS_BAR_NOTIFICATION = "status_bar_notification";
    private static final String KEY_ID = "id";
    private static final String KEY_PKG = "package_name";
    private static final String KEY_ICON = "icon";
    private static final String KEY_TAG = "tag";
    private static final String KEY_TICKER = "ticker";
    private static final String KEY_SEEN = "seen";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "creating a new database helper");

    }


    public long updateSbnAsSeen(android.service.notification.StatusBarNotification sbn){
        Log.d(TAG, "updating sbn as read");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SEEN, 1);
        return db.update(TABLE_STATUS_BAR_NOTIFICATION, values, KEY_ID + " = ?",
                new String[] { String.valueOf(sbn.getPostTime()) });
    }


    public long createSbn(android.service.notification.StatusBarNotification sbn) {
        Log.d(TAG, "creating a new sbn");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, String.valueOf(sbn.getPostTime()));
        values.put(KEY_PKG, sbn.getPackageName());
        values.put(KEY_ICON, sbn.getNotification().icon);
        values.put(KEY_TICKER, (sbn.getNotification().tickerText != null ?
                sbn.getNotification().tickerText.toString()  : " "));
        values.put(KEY_SEEN, false);
        long sbn_id = db.insert(TABLE_STATUS_BAR_NOTIFICATION, null, values);
        return sbn_id;
    }


    public void deleteAll() {
        Log.d(TAG,"deleting all notifications");
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STATUS_BAR_NOTIFICATION, null, null);
    }



    public StatusBarNotification getSbn(long sbn_id) {
        Log.d(TAG, "getting a sbn from the database");
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_STATUS_BAR_NOTIFICATION + " WHERE "
                + KEY_ID + " = " + String.valueOf(sbn_id);

        Log.d(TAG, selectQuery);
        Cursor c = null;
        StatusBarNotification sbn = null;
        try {
            c = db.rawQuery(selectQuery, null);
            sbn = new StatusBarNotification();
            if (c != null && c.moveToFirst()) {
                sbn.setId(Long.parseLong(c.getString(c.getColumnIndex(KEY_ID))));
                sbn.setPackageName((c.getString(c.getColumnIndex(KEY_PKG))));
                sbn.setIcon(c.getInt(c.getColumnIndex(KEY_ICON)));
                sbn.setTicker((c.getString(c.getColumnIndex(KEY_TICKER))));
                sbn.setSeen(c.getInt(c.getColumnIndex(KEY_SEEN)) == 1 ? true : false);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            c.close();
        }
        Log.d(TAG, "did get sbn?" +  (sbn.getId() != null? " yes" : " no"));
        return sbn;
    }


    public List<StatusBarNotification> getAllSbnsBySeen(boolean isSeen) {

        int seen = 0;
        if(isSeen)
            seen = 1;
        List<StatusBarNotification> sbns = new ArrayList<StatusBarNotification>();
        String selectQuery = "SELECT  * FROM " + TABLE_STATUS_BAR_NOTIFICATION +
                " WHERE " +  KEY_SEEN  + "  = " + seen;
        Log.e(TAG, selectQuery);
        Cursor c = null;
        SQLiteDatabase db = null;
        try {
             db = this.getReadableDatabase();
             c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    StatusBarNotification sbn = new StatusBarNotification();
                    sbn.setId(Long.parseLong(c.getString(c.getColumnIndex(KEY_ID))));
                    sbn.setPackageName((c.getString(c.getColumnIndex(KEY_PKG))));
                    sbn.setIcon(c.getInt(c.getColumnIndex(KEY_ICON)));
                    sbn.setTicker((c.getString(c.getColumnIndex(KEY_TICKER))));
                    sbn.setSeen(c.getInt(c.getColumnIndex(KEY_SEEN)) == 1 ? true : false);
                    sbns.add(sbn);
                } while (c.moveToNext());
        }
        } catch(Exception ex) {
            ex.printStackTrace();
        }  finally {
            c.close();
        }

        Log.d(TAG, "size was " + (sbns != null? sbns.size() : 0));
        return sbns;
    }


    private static final String CREATE_TABLE_TODO = "CREATE TABLE "
            + TABLE_STATUS_BAR_NOTIFICATION + "(" + KEY_ID + " TEXT PRIMARY KEY," + KEY_PKG
            + " TEXT," + KEY_ICON + " INTEGER," + KEY_TAG
            + " TEXT," + KEY_TICKER + " TEXT," + KEY_SEEN + " INTEGER)";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TODO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATUS_BAR_NOTIFICATION);
        onCreate(db);
    }


    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}
