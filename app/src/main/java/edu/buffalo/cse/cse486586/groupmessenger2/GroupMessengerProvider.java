package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class GroupMessengerProvider extends ContentProvider {

    private Database_Helper database_helper;

    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    private final static String AUTHORITY = "edu.buffalo.cse.cse486586.groupmessenger2.provider";
    private final static String PROVIDER_URI = "content://" + AUTHORITY;

    private static final int KEYS = 1;
    private static final int KEY_ID = 2;

    private static final int TESTER_ACCESS = 3;

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

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase db = database_helper.getReadableDatabase();
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();

        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(AUTHORITY);
        uriBuilder.scheme("content");
        Uri tester_uri = uriBuilder.build();

        Log.d(TAG, "db query selections is: " + selection);

        if (uri.toString().equals(tester_uri.toString())) {
            sqLiteQueryBuilder.setTables(Key_Value_Contract.TABLE_NAME);
            // limit query to one row at most:

            String query = "SELECT * FROM " + Key_Value_Contract.TABLE_NAME +
                    " ORDER BY " + Key_Value_Contract.COLUMN_KEY + " ASC " +
                    " limit 1 offset " + "'" + selection + "'";

            Cursor result = db.rawQuery(query, null);
            if (result.moveToFirst()) {
                int keyIndex = result.getColumnIndex(Key_Value_Contract.COLUMN_KEY);
                int valueIndex = result.getColumnIndex(Key_Value_Contract.COLUMN_VALUE);
                result.moveToFirst();
                String returnKey = result.getString(keyIndex);
                String returnValue = result.getString(valueIndex);
                Log.d(TAG, "key is: " + returnKey + " value is: " + returnValue);
            } else {
                Log.d(TAG, "no entry");
                return null;
            }


            return result;

        } else {
            throw new IllegalArgumentException(
                    "Unsupported URI for insertion: " + uri);
        }

    }
}
