package info.tongrenlu.android.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class BaseContentProvider extends ContentProvider {

    private static final int SINGLE_CODE = 1;
    public static final String SINGLE_PATH = "/#";

    private final String mAuthrity;
    private final String mTable;
    private DatabaseBuilder mDbBuilder = null;
    private SimpleDbHelper mDbHelper = null;
    private DataProviderTemplate mProvider = null;
    private UriMatcher mUriMatcher = null;
    private Uri mRootUri = null;

    public BaseContentProvider(String authority, String table, DatabaseBuilder dbBuilder) {
        this.mAuthrity = authority;
        this.mTable = table;
        this.mDbBuilder = dbBuilder;
    }

    @Override
    public boolean onCreate() {
        this.mDbHelper = new SimpleDbHelper(this.getContext(), this.mDbBuilder);
        this.mProvider = new DataProviderTemplate(this.mDbHelper, this.mTable);
        this.mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        this.mUriMatcher.addURI(this.mAuthrity,
                                BaseContentProvider.SINGLE_PATH,
                                BaseContentProvider.SINGLE_CODE);
        this.mRootUri = Uri.parse("content://" + this.mAuthrity);
        return true;
    }

    @Override
    public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
        switch (this.mUriMatcher.match(uri)) {
        case UriMatcher.NO_MATCH:
            return this.mProvider.query(projection,
                                        selection,
                                        selectionArgs,
                                        sortOrder);
        case SINGLE_CODE:
            final String _id = uri.getLastPathSegment();
            return this.mProvider.querySingle(projection, _id);
        }
        return null;
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        ContentResolver contentResolver = this.getContext()
                                              .getContentResolver();
        Uri newUri = null;
        switch (this.mUriMatcher.match(uri)) {
        case UriMatcher.NO_MATCH:
            newUri = this.mProvider.insert(uri, values);
            break;
        }
        if (newUri != null) {
            contentResolver.notifyChange(this.mRootUri, null);
        }
        return newUri;
    }

    @Override
    public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
        ContentResolver contentResolver = this.getContext()
                                              .getContentResolver();
        int rows = 0;
        switch (this.mUriMatcher.match(uri)) {
        case UriMatcher.NO_MATCH:
            rows = this.mProvider.update(values, selection, selectionArgs);
            if (rows > 0) {
                contentResolver.notifyChange(uri, null);
            }
            break;
        case SINGLE_CODE:
            rows = this.mProvider.updateSingle(values, uri.getLastPathSegment());
            break;
        }
        if (rows > 0) {
            contentResolver.notifyChange(uri, null);
            contentResolver.notifyChange(this.mRootUri, null);
        }
        return rows;
    }

    @Override
    public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
        ContentResolver contentResolver = this.getContext()
                                              .getContentResolver();
        int rows = 0;
        switch (this.mUriMatcher.match(uri)) {
        case UriMatcher.NO_MATCH:
            rows = this.mProvider.delete(selection, selectionArgs);
            break;
        case SINGLE_CODE:
            rows = this.mProvider.deleteSingle(uri.getLastPathSegment());
            break;
        }
        if (rows > 0) {
            contentResolver.notifyChange(uri, null);
            contentResolver.notifyChange(this.mRootUri, null);
        }
        return rows;
    }

    @Override
    public String getType(final Uri uri) {
        return null;
    }

}
