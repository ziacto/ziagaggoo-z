package net.everythingandroid.smspopup.util;

import net.everythingandroid.smspopup.BuildConfig;
import net.everythingandroid.smspopup.preferences.ButtonListPreference;
import net.everythingandroid.smspopup.provider.SmsPopupContract.ContactNotifications;
import net.everythingandroid.smspopup.service.SmsPopupUtilsService;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

public class ManagePreferences {
    private long mRowId = 0;
    private Context mContext;
    private Cursor mCursor;
    private boolean useDatabase;
    private SharedPreferences mPrefs;
    private static final String one = "1";

    /*
     * Define all default preferences in this static class. Unfortunately these are also stored in
     * the resource xml files for use by the preference xml so they should be updated in both places
     * if a change is required.
     */
    public static final class Defaults {
        public static final boolean PREFS_AUTOROTATE = true;
        public static final boolean PREFS_PRIVACY = false;
        public static final boolean PREFS_PRIVACY_SENDER = false;
        public static final boolean PREFS_PRIVACY_ALWAYS = false;
        public static final boolean PREFS_SHOW_BUTTONS = true;
        public static final boolean PREFS_USE_UNLOCK_BUTTON = false;
        public static final String PREFS_BUTTON1 = String
                .valueOf(ButtonListPreference.BUTTON_CLOSE);
        public static final String PREFS_BUTTON2 = String
                .valueOf(ButtonListPreference.BUTTON_DELETE);
        public static final String PREFS_BUTTON3 = String
                .valueOf(ButtonListPreference.BUTTON_QUICKREPLY);
        public static final boolean PREFS_SHOW_POPUP = true;
        public static final boolean PREFS_ONLY_SHOW_ON_KEYGUARD = false;
        public static final boolean PREFS_MARK_READ = true;

        public static final boolean PREFS_NOTIF_ENABLED = false;
        public static final String PREFS_NOTIF_ICON = "0";
        public static final boolean PREFS_NOTIFY_ON_CALL = false;
        public static final boolean PREFS_VIBRATE_ENABLED = true;
        public static final String PREFS_VIBRATE_PATTERN = "0,1200";
        public static final boolean PREFS_LED_ENABLED = true;
        public static final String PREFS_LED_PATTERN = "1000,1000";
        public static final String PREFS_LED_COLOR = "Yellow";
        public static final boolean PREFS_REPLY_TO_THREAD = true;
        public static final boolean PREFS_NOTIF_REPEAT = false;
        public static final String PREFS_NOTIF_REPEAT_INTERVAL = "5";
        public static final String PREFS_NOTIF_REPEAT_TIMES = "2";
        public static final Boolean PREFS_NOTIF_REPEAT_SCREEN_ON = false;
    }

    /**
     * Create an instance of ManagePreferences by database row id.
     * @param context a context.
     * @param rowId the database row id.
     */
    public ManagePreferences(Context context, long rowId) {
        mRowId = rowId;
        mContext = context;
        useDatabase = false;

        if (BuildConfig.DEBUG) Log.v("ManagePreferences init, rowId = " + mRowId);

        if (mRowId > 0) {
            mCursor = mContext.getContentResolver().query(
                    ContactNotifications.buildContactUri(mRowId), null, null, null, null);
            if (mCursor != null && mCursor.moveToFirst()) {
                if (BuildConfig.DEBUG) Log.v("Single contact found (rowId) - using database");
                useDatabase = true;
            }
        }

        if (BuildConfig.DEBUG && !useDatabase) {
            Log.v("Contact NOT found - using prefs");
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    /**
     * Create an instance of ManagePreferences by contact lookup key.
     * @param context a context.
     * @param contactId the system _id from the contacts table
     * @param contactLookupKey the system contact lookup key.
     */
    public ManagePreferences(Context context, String contactId, String contactLookupKey) {
        mContext = context;
        useDatabase = false;

        if (BuildConfig.DEBUG) {
            Log.v("Notification prefs for contactId = " + contactId
                    + ", lookup_key = " + contactLookupKey);
        }

        if (contactLookupKey != null && contactId != null) {
            mCursor = mContext.getContentResolver().query(
                    ContactNotifications.buildLookupUri(contactId, contactLookupKey),
                    null, // all rows are required
                    null, null, null);
            if (mCursor != null && mCursor.getCount() > 0) {
                useDatabase = true;
                if (mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    if (BuildConfig.DEBUG) Log.v("Contact(s) found in contact prefs - using database");
                    mRowId = mCursor.getLong(mCursor.getColumnIndexOrThrow(ContactNotifications._ID));
                    final String lookup = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactNotifications.CONTACT_LOOKUPKEY));
                    if (mCursor.getCount() > 1 || !contactLookupKey.equals(lookup)) {
                        if (BuildConfig.DEBUG) Log.v("Multiple contacts found or lookup key mismatch - stil using database but starting contact sync");
                        SmsPopupUtilsService.startSyncContactNames(context);
                    }
                }
            }
        }

        if (BuildConfig.DEBUG && !useDatabase) {
            Log.v("Contact NOT found - using prefs");
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public boolean getBoolean(int resPrefId, int resDefaultId, String dbColumnName) {
        if (useDatabase) {
            return one.equals(mCursor.getString(mCursor.getColumnIndexOrThrow(dbColumnName)));
        } else {
            return getBoolean(resPrefId, resDefaultId);
        }
    }

    public boolean getBoolean(int resPrefId, boolean prefDefault, String dbColumnName) {
        if (useDatabase) {
            return one.equals(mCursor.getString(mCursor.getColumnIndexOrThrow(dbColumnName)));
        } else {
            return getBoolean(resPrefId, prefDefault);
        }
    }

    public boolean getBoolean(int resPrefId, int resDefaultId) {
        return mPrefs.getBoolean(mContext.getString(resPrefId),
                Boolean.parseBoolean(mContext.getString(resDefaultId)));
    }

    public boolean getBoolean(int resPrefId, boolean prefDefault) {
        return mPrefs.getBoolean(mContext.getString(resPrefId), prefDefault);
    }

    public String getString(int resPrefId, int resDefaultId, String dbColumnName) {
        if (useDatabase) {
            return mCursor.getString(mCursor.getColumnIndexOrThrow(dbColumnName));
        } else {
            return getString(resPrefId, resDefaultId);
        }
    }

    public String getString(int resPrefId, String defaultVal, String dbColumnName) {
        if (useDatabase) {
            return mCursor.getString(mCursor.getColumnIndexOrThrow(dbColumnName));
        } else {
            return mPrefs.getString(mContext.getString(resPrefId), defaultVal);
        }
    }

    public String getString(int resPrefId, int resDefaultId) {
        return mPrefs.getString(mContext.getString(resPrefId), mContext.getString(resDefaultId));
    }

    public String getString(int resPrefId, String defaultVal) {
        return mPrefs.getString(mContext.getString(resPrefId), defaultVal);
    }

    public void putString(int resPrefId, String newVal, String dbColumnName) {
        if (useDatabase) {
            ContentValues vals = new ContentValues();
            vals.put(dbColumnName, newVal);
            mContext.getContentResolver().update(
                    ContactNotifications.buildContactUri(mRowId), vals, null, null);
        } else {
            putString(resPrefId, newVal);
        }
    }

    public void putString(int resPrefId, String newVal) {
        SharedPreferences.Editor settings = mPrefs.edit();
        settings.putString(mContext.getString(resPrefId), newVal);
        settings.commit();
    }

    public int getInt(String pref, int defaultVal) {
        return mPrefs.getInt(pref, defaultVal);
    }

    public int getInt(int resPrefId, int defaultVal) {
        return mPrefs.getInt(mContext.getString(resPrefId), defaultVal);
    }

    public void close() {
        if (mCursor != null) {
            mCursor.close();
        }
    }
}
