package info.tongrenlu.android.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SimpleDbHelper extends SQLiteOpenHelper {

    private final DatabaseBuilder mBuilder;

    /*** 构造函数 **/
    public SimpleDbHelper(final Context context, DatabaseBuilder builder) {
        super(context, builder.getName(), null, builder.getVersion());
        this.mBuilder = builder;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.mBuilder.onCreate(db);
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.mBuilder.onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * 插入数据
     * 
     * @return
     **/
    public long insert(final String tableName, final ContentValues values) {
        final SQLiteDatabase db = this.getWritableDatabase();
        final long _id = db.insert(tableName, null, values);
        return _id;
    }

    /*** 更新数据 */
    public int update(final String tableName, final ContentValues values, final String selection, final String[] selectionArgs) {
        final SQLiteDatabase db = this.getWritableDatabase();
        final int rows = db.update(tableName, values, selection, selectionArgs);
        return rows;
    }

    /** 删除数据 */
    public int delete(final String tableName, final String selection, final String[] selectionArgs) {
        final SQLiteDatabase db = this.getWritableDatabase();
        final int rows = db.delete(tableName, selection, selectionArgs);
        return rows;
    }

    /***
     * 查找数据
     * 
     * @param sortOrder
     * @param projection
     */
    public Cursor query(final String tableName, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
        final SQLiteDatabase db = this.getReadableDatabase();
        final Cursor c = db.query(tableName,
                                  projection,
                                  selection,
                                  selectionArgs,
                                  null,
                                  null,
                                  sortOrder);
        return c;
    }

}
