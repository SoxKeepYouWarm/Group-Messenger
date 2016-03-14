package edu.buffalo.cse.cse486586.test;


import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;

//import com.example.android.app.Constants;
//import com.example.android.app.DataLayer;
//import com.example.android.app.provider.contract.ActiveUserContract;

import edu.buffalo.cse.cse486586.groupmessenger2.Database_Helper;
import edu.buffalo.cse.cse486586.groupmessenger2.GroupMessengerProvider;
import edu.buffalo.cse.cse486586.groupmessenger2.Key_Value_Contract;




public class provider_test extends ProviderTestCase2<GroupMessengerProvider> {

    final static Uri provider_uri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
    private static final String TAG = provider_test.class.getSimpleName();

    MockContentResolver mMockResolver;

    public provider_test() {
        super(GroupMessengerProvider.class, GroupMessengerProvider.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Log.d(TAG, "setUp: ");
        mMockResolver = getMockContentResolver();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Log.d(TAG, "tearDown:");
    }

    public void test_table_column_names() {

    }

    public void testActiveUserInsert__inserts_a_valid_record() {
        Uri uri = mMockResolver.insert(provider_uri, get_test_user());

        assertNotNull(uri);
        long id = ContentUris.parseId(uri);
        assertTrue(id > 0);

        //assertEquals(1L, ContentUris.parseId(uri));
    }

    public static ContentValues get_test_user() {
        ContentValues sample = new ContentValues();
        sample.put(Key_Value_Contract.UID, 1.0);
        sample.put(Key_Value_Contract.COLUMN_KEY, "1");
        sample.put(Key_Value_Contract.COLUMN_VALUE, "Hello World!");

        return sample;
    }

    public void test_math() {
        assertEquals(2, 1 + 1);
    }

    private static Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    /*
    public void testActiveUserInsert__cursor_contains_valid_data() {
        mMockResolver.insert(ActiveUserContract.CONTENT_URI, getFullActiveUserContentValues());
        Cursor cursor = mMockResolver.query(ActiveUserContract.CONTENT_URI, null, null, new String[] {}, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        assertEquals(VALID_USERNAME,     cursor.getString(cursor.getColumnIndex(ActiveUserContract.Columns.USERNAME)));
        assertEquals(VALID_COMPANY_CODE,    cursor.getString(cursor.getColumnIndex(ActiveUserContract.Columns.COMPANY_CODE)));
        assertEquals(VALID_COMPANY_LETTER,  cursor.getString(cursor.getColumnIndex(ActiveUserContract.Columns.COMPANY_LETTER)));
        assertEquals(VALID_DRIVER_CODE,     cursor.getString(cursor.getColumnIndex(ActiveUserContract.Columns.DRIVER_CODE)));
        assertEquals(VALID_SITE_NUMBER,     cursor.getString(cursor.getColumnIndex(ActiveUserContract.Columns.SITE_NUMBER)));
        assertEquals(VALID_FIRST_NAME,      cursor.getString(cursor.getColumnIndex(ActiveUserContract.Columns.FIRST_NAME)));
        assertEquals(VALID_SURNAME,         cursor.getString(cursor.getColumnIndex(ActiveUserContract.Columns.SURNAME)));
    }

    public void testActiveUserInsert__throws_SQLiteConstraintException_when_NOT_NULL_constraint_not_met() {
        try {
            mMockResolver.insert(ActiveUserContract.CONTENT_URI, getActiveUserContentValuesWithNullCompanyCode());
            fail("SQLiteConstraintException should have been thrown!");
        } catch (SQLiteConstraintException e) {
            assertEquals("active_user.comp_code may not be NULL (code 19)", e.getMessage());
        }
    }*/

    /** @return a ContentValues object with a value set for each ActiveUser column */
    /*public static ContentValues getFullActiveUserContentValues() {
        ContentValues v = new ContentValues(7);
        v.put(ActiveUserContract.Columns.USERNAME,       VALID_USERNAME);
        v.put(ActiveUserContract.Columns.COMPANY_CODE,   VALID_COMPANY_CODE);
        v.put(ActiveUserContract.Columns.COMPANY_LETTER, VALID_COMPANY_LETTER);
        v.put(ActiveUserContract.Columns.DRIVER_CODE,    VALID_DRIVER_CODE);
        v.put(ActiveUserContract.Columns.SITE_NUMBER,    VALID_SITE_NUMBER);
        v.put(ActiveUserContract.Columns.FIRST_NAME,     VALID_FIRST_NAME);
        v.put(ActiveUserContract.Columns.SURNAME,        VALID_SURNAME);
        return v;
    }

    public static ContentValues getActiveUserContentValuesWithNullCompanyCode() {
        ContentValues v = new ContentValues(7);
        v.put(ActiveUserContract.Columns.USERNAME,       VALID_USERNAME);
        v.putNull(ActiveUserContract.Columns.COMPANY_CODE);
        v.put(ActiveUserContract.Columns.COMPANY_LETTER, VALID_COMPANY_LETTER);
        v.put(ActiveUserContract.Columns.DRIVER_CODE,    VALID_DRIVER_CODE);
        v.put(ActiveUserContract.Columns.SITE_NUMBER,    VALID_SITE_NUMBER);
        v.put(ActiveUserContract.Columns.FIRST_NAME,     VALID_FIRST_NAME);
        v.put(ActiveUserContract.Columns.SURNAME,        VALID_SURNAME);
        return v;
    }

    private static final String VALID_SURNAME        = "Brin";
    private static final String VALID_FIRST_NAME     = "Sergey";
    private static final String VALID_SITE_NUMBER    = "9";
    private static final String VALID_DRIVER_CODE    = "SergB201";
    private static final String VALID_COMPANY_LETTER = "G";
    private static final String VALID_COMPANY_CODE   = "GOOGLE!";
    private static final String VALID_USERNAME       = "123456";
    */
}