package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {

    private Database_Helper database_helper;

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
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
        Log.v("insert", values.toString());

        SQLiteDatabase db = database_helper.getWritableDatabase();


        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority("edu.buffalo.cse.cse486586.groupmessenger1.provider");
        uriBuilder.scheme("content");
        Uri tester_uri = uriBuilder.build();



        if (uri.toString().equals(tester_uri.toString())) {
            long id = db.insert(Key_Value_Contract.TABLE_NAME, null, values);
            return uri;
        } else {
            throw new IllegalArgumentException(
                    "Unsupported URI for insertion: " + uri);
        }

        /*else if (URI_MATCHER.match(uri) != KEYS
                && URI_MATCHER.match(uri) != KEY_ID) {
            throw new IllegalArgumentException(
                    "Unsupported URI for insertion: " + uri);
        }
        else if (URI_MATCHER.match(uri) == KEY_ID) {
            long id = db.insert(Key_Value_Contract.TABLE_NAME, null, values);
            return uri;
            //return Uri.parse(CONTENT_URI + "/" + id);
        }
        else {
            return uri;
        }*/

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
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */

        SQLiteDatabase db = database_helper.getReadableDatabase();
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();

        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority("edu.buffalo.cse.cse486586.groupmessenger1.provider");
        uriBuilder.scheme("content");
        Uri tester_uri = uriBuilder.build();



        if (uri.toString().equals(tester_uri.toString())) {
            sqLiteQueryBuilder.setTables(Key_Value_Contract.TABLE_NAME);
            // limit query to one row at most:

            //sqLiteQueryBuilder.appendWhere("key = key1");
            sqLiteQueryBuilder.appendWhere(Key_Value_Contract.COLUMN_KEY + " = " + "'" + selection + "'");
            //builder.appendWhere(Key_Value_Contract.UID + " = "
            //        + uri.getLastPathSegment());

            Cursor cursor = sqLiteQueryBuilder.query(db, Key_Value_Contract.PROJECTION, null, null,
                    null, null, sortOrder);
            return cursor;
        } else {
            throw new IllegalArgumentException(
                    "Unsupported URI for insertion: " + uri);
        }

        /*if (URI_MATCHER.match(uri) == KEY_ID || URI_MATCHER.match(uri) == TESTER_ACCESS) {

            builder.setTables(Key_Value_Contract.TABLE_NAME);
            // limit query to one row at most:
            builder.appendWhere(Key_Value_Contract.UID + " = "
                    + uri.getLastPathSegment());

            Cursor cursor = builder.query(db, projection, selection, selectionArgs,
                    null, null, sortOrder);
            return cursor;

        }*/

    }
}
