package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database_Helper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "Group_messenger_db";
    private final static int DATABASE_VERSION = 2;

    public Database_Helper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(Key_Value_Contract.CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(Key_Value_Contract.DROP_TABLE);
        onCreate(db);
    }


}


