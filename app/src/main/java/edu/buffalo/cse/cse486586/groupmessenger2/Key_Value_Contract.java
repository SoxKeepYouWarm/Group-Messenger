package edu.buffalo.cse.cse486586.groupmessenger2;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Key_Value_Contract {

    public final static String TABLE_NAME = "Messages";
    public final static String UID = "_id";
    public final static String COLUMN_KEY = "key";
    public final static String COLUMN_VALUE = "value";

    public final static String[] PROJECTION = {UID, COLUMN_KEY, COLUMN_VALUE};

    public final static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + UID + " REAL PRIMARY KEY NOT NULL, "
            + COLUMN_KEY + " TEXT, "
            + COLUMN_VALUE + " TEXT, "
            + "UNIQUE (" + COLUMN_KEY + ") ON CONFLICT REPLACE);";

    public final static String DROP_TABLE = "DROP TABLE if exists " + TABLE_NAME;

    public static void onCreate(SQLiteDatabase db) {

        try {
            db.execSQL(CREATE_TABLE);
        } catch (SQLException e) {
            Log.d("ERROR", e.getMessage());
        }

    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        try {
            db.execSQL(DROP_TABLE);
            onCreate(db);
        } catch (SQLException e) {
            Log.d("ERROR", e.getMessage());
        }

    }


}
