package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class GroupMessengerProvider extends ContentProvider {

    private Database_Helper database_helper;

    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    public final static String AUTHORITY = "edu.buffalo.cse.cse486586.groupmessenger2.provider";
    public final static String PROVIDER_URI = "content://" + AUTHORITY;

    private static final int KEYS = 1;
    private static final int KEY_ID = 2;

    private static final int TESTER_ACCESS = 3;

    private static int SIZE = 0;
    private static boolean IS_SORTED = false;

    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY,
                "keys",
                KEYS);
        URI_MATCHER.addURI(AUTHORITY,
                "keys/#",
                KEY_ID);
        URI_MATCHER.addURI(PROVIDER_URI,
                "",
                TESTER_ACCESS);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.v("insert", values.toString());

        SQLiteDatabase db = database_helper.getWritableDatabase();

        Log.d(TAG, "db insert content_value: " + values.toString());

        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(AUTHORITY);
        uriBuilder.scheme("content");
        Uri tester_uri = uriBuilder.build();


        if (uri.toString().equals(tester_uri.toString())) {
            long id = db.insert(Key_Value_Contract.TABLE_NAME, null, values);
            SIZE++;
            IS_SORTED = false;
            return uri;
        } else {
            throw new IllegalArgumentException(
                    "Unsupported URI for insertion: " + uri);
        }

    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        database_helper = new Database_Helper(getContext());
        return true;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }


    public void maintain_order(SQLiteDatabase db) {

        String query = "SELECT * FROM " + Key_Value_Contract.TABLE_NAME +
                " ORDER BY " + Key_Value_Contract.UID + " ASC ";

        Cursor result = db.rawQuery(query, null);

        for (int i = 0; i < result.getCount(); i++) {

            result.moveToPosition(i);
            int id_index = result.getColumnIndex(Key_Value_Contract.UID);
            double sequence_id = result.getDouble(id_index);

            String update_query = "UPDATE " + Key_Value_Contract.TABLE_NAME +
                    " SET " + Key_Value_Contract.COLUMN_KEY + "=" + "'" + Integer.toString(i) + "'" +
                    " WHERE " + Key_Value_Contract.UID + "=" + Double.toString(sequence_id);

            db.execSQL(update_query);

            Log.d(TAG, "just executed: " + update_query);

        }

        IS_SORTED = true;
        result.close();

    }


    boolean DEBUG_SIZE = true;
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase db = database_helper.getReadableDatabase();
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();

        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(AUTHORITY);
        uriBuilder.scheme("content");
        Uri tester_uri = uriBuilder.build();


        if (! IS_SORTED) {
            maintain_order(db);
        }

        Log.d(TAG, "db query selections is: " + selection);

        if (uri.toString().equals(tester_uri.toString())) {
            sqLiteQueryBuilder.setTables(Key_Value_Contract.TABLE_NAME);
            // limit query to one row at most:

            /*String query = "SELECT * FROM " + Key_Value_Contract.TABLE_NAME +
                    " ORDER BY " + Key_Value_Contract.COLUMN_KEY + " ASC " +
                    " limit 1 offset " + "'" + selection + "'";*/

            String query = "SELECT * FROM " + Key_Value_Contract.TABLE_NAME +
                    " ORDER BY " + Key_Value_Contract.UID + " ASC ";

            Cursor result = db.rawQuery(query, null);
            result.moveToPosition(Integer.parseInt(selection));

            if (DEBUG_SIZE) {
                Log.d(TAG, "size is: " + result.getCount());
                DEBUG_SIZE = false;
            }

            if (result.getCount() == 0) {
                Log.e(TAG, "no entry for query: " + query);
                return null;
            }

            int keyIndex = result.getColumnIndex(Key_Value_Contract.COLUMN_KEY);
            int valueIndex = result.getColumnIndex(Key_Value_Contract.COLUMN_VALUE);

            String returnKey = result.getString(keyIndex);
            String returnValue = result.getString(valueIndex);
            Log.d(TAG, "key is: " + returnKey + " value is: " + returnValue);

            return result;

        } else {
            throw new IllegalArgumentException(
                    "Unsupported URI for insertion: " + uri);
        }

    }
}
